#!/usr/bin/env python3
"""
Summarises an autotest run across all versions: autotest-report/<id>/ (written by the Gradle autotest tasks)
holds the check reports (<suite>.txt) and the screenshots (page-N-name.png) of every version.

Writes an index.html with
  * a table of every check suite per version (PASS / FAIL), and the failing checks with their messages,
  * the lists the reports contain (enchantments, biomes, ...): their size per version, what changed from
    one version to the next, and where loaders of the same version disagree,
  * one contact sheet per screenshot page (every version side by side, labelled), which can
      - show how much each version differs from a reference version (--ref, optionally --highlight),
      - check for regressions against the screenshots of an earlier run (--baseline).
A run without screenshots (autotestChecks) or without reports (autotestScreenshots) works too.

Above all of that, a table of every run: whether the game finished it (the FINISHED marker the testmod
writes), wrote a crash report, and links to its launcher.log / latest.log. A run that crashed before
writing anything still shows up there instead of silently missing from the other tables.

Needs Pillow:  pip install pillow

Examples:
    ./autotest.py
    ./autotest.py autotest-report --ref 1.21.1-fabric --highlight
    ./autotest.py --only "fabric$" --cols 2 --scale 2
    ./autotest.py --baseline old-autotest-report

Exit code 1 if a run didn't finish, a check failed or (with --baseline) a screenshot changed.
"""

import argparse
import html
import math
import os
import re
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

PAGE_RE = re.compile(r"^page-(\d+)-(.+)\.png$")
SECTION_RE = re.compile(r"^(.*?) \((\d+|could not be listed: .*)\)$")

# Regions (x, y, w, h in GUI pixels) that legitimately change from run to run, per page name.
# "text": the two always-scrolling text rows on the "text" page of RenderTestScreen:
# cell 2 and cell 4 of the 2-column grid (origin 10,24; cell 226x76; gap 4).
IGNORE = {
    "text": [(10, 104, 226, 76), (10, 184, 226, 76)],
    # The blinking text cursor of the focused column on the "inputs" page (WidgetGallery in the testmod):
    # the inside of each of the 7 focused text fields, so their focused border is still compared.
    "inputs": [(288, 38 + 28 * row + 6, 84, 16) for row in range(7)],
    # Same for the two suggestion boxes (rows 3 and 4) on the "dropdowns-search" page.
    "dropdowns-search": [(288, 38 + 28 * row + 6, 84, 16) for row in (2, 3)],
    # Interaction test (InteractionTestScreen): the focused int field with its blinking cursor.
    "interaction-int-above-max": [(342, 72, 90, 16)],
}

BG = (24, 24, 24)
LABEL_BG = (44, 44, 44)
TEXT = (230, 230, 230)
GOOD = (120, 220, 120)
BAD = (240, 110, 110)
LABEL_H = 22
GAP = 8


# Reading the screenshots

def version_key(identifier):
    """'1.21.11-fabric' -> sorted by loader first, then by numeric version (1.21.2 before 1.21.11)."""
    version, _, loader = identifier.rpartition("-")
    if not version:
        version, loader = identifier, ""
    numbers = [int(part) if part.isdigit() else 0 for part in version.split(".")]
    return loader, numbers, identifier


def collect(root):
    """{id: {(page number, page name): Path}}"""
    if not root.is_dir():
        sys.exit(f"error: {root} is not a folder")
    data = {}
    for folder in root.iterdir():
        if not folder.is_dir():
            continue
        pages = {}
        for file in folder.iterdir():
            match = PAGE_RE.match(file.name)
            if match:
                pages[(int(match.group(1)), match.group(2))] = file
        if pages:
            data[folder.name] = pages
    return data


# Reading the check reports

class Report:
    """One <suite>.txt: RESULT line, PASS / FAIL check lines, then sections (title, entry lines)."""

    def __init__(self, path):
        self.suite = path.stem
        self.passed = False
        self.checks = []      # (passed, text)
        self.sections = {}    # title -> [entry lines]
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
        if lines and lines[0].startswith("RESULT "):
            self.passed = lines[0] == "RESULT PASS"
            lines = lines[1:]
        section = None
        for line in lines:
            if not line.strip():
                section = None
            elif line.startswith("PASS ") and section is None:
                self.checks.append((True, line[5:]))
            elif line.startswith("FAIL ") and section is None:
                self.checks.append((False, line[5:]))
            elif line.startswith("  ") and section is not None:
                self.sections[section].append(line.strip())
            else:
                match = SECTION_RE.match(line)
                section = match.group(1) if match else line.strip()
                self.sections.setdefault(section, [])

    @property
    def failures(self):
        return [text for passed, text in self.checks if not passed]


def collect_reports(root):
    """{id: {suite: Report}} - every version folder with at least one report."""
    reports = {}
    for folder in root.iterdir():
        if folder.is_dir():
            found = {path.stem: Report(path) for path in sorted(folder.glob("*.txt"))}
            if found:
                reports[folder.name] = found
    return reports


# Reading the run status

FINISHED_MARKER = "FINISHED"
FAILED_RE = re.compile(r"\[autotest\] FAILED: (.*)")


class Run:
    """One version folder: did the game finish the run, and if not, the best guess why."""

    def __init__(self, folder):
        self.id = folder.name
        self.folder = folder
        self.finished = (folder / FINISHED_MARKER).exists()
        self.launcher_log = folder / "launcher.log"
        self.latest_log = folder / "latest.log"
        self.crash_reports = sorted((folder / "crash-reports").glob("*.txt")) if (folder / "crash-reports").is_dir() else []
        self.reason = None if self.finished and not self.crash_reports else self.find_reason()

    @property
    def status(self):
        """'finished', 'crashed', 'unfinished' or 'unknown' (no launcher.log: not from a launcher run)."""
        if self.crash_reports:
            return "crashed"
        if self.finished:
            return "finished"
        if not self.launcher_log.exists():
            return "unknown"
        return "unfinished"

    @property
    def failed(self):
        return self.status in ("crashed", "unfinished")

    def find_reason(self):
        """The testmod's own FAILED line, else the crash report's description, else the launcher's first error."""
        log = self.launcher_log.read_text(encoding="utf-8", errors="replace").splitlines() \
            if self.launcher_log.exists() else []
        for line in log:
            match = FAILED_RE.search(line)
            if match:
                return "autotest: " + match.group(1).strip()
        for report in self.crash_reports:
            for line in report.read_text(encoding="utf-8", errors="replace").splitlines():
                if line.startswith("Description:"):
                    return f"crash report: {line.removeprefix('Description:').strip()}"
        for i, line in enumerate(log):
            if "[ ERRO ]" in line:
                following = [l.strip() for l in log[i + 1:i + 3] if l.strip() and "[" not in l[:8]]
                return " ".join([line.split("[ ERRO ]", 1)[1].strip()] + following)
        return None


def collect_runs(root):
    """{id: Run} - every version folder, whatever it contains."""
    return {folder.name: Run(folder) for folder in root.iterdir() if folder.is_dir()}


def is_value_line(line):
    """'items   1234' - a name and a number."""
    words = line.split()
    return len(words) == 2 and words[1].isdigit()


def entry_key(line):
    """The first word of an entry line - the ID in lists like 'minecraft:fortune   Fortune'."""
    return line.split()[0] if line.split() else line


def split_id(identifier):
    """'1.21.11-fabric' -> ('1.21.11', 'fabric')"""
    version, _, loader = identifier.rpartition("-")
    return (version, loader) if version else (identifier, "")


# Comparing images

def difference(a, b, page_name, tolerance):
    """Returns (fraction of pixels that differ, mask of them) or (None, None) if the sizes differ."""
    if a.size != b.size:
        return None, None
    r, g, bl = ImageChops.difference(a.convert("RGB"), b.convert("RGB")).split()
    largest = ImageChops.lighter(ImageChops.lighter(r, g), bl)  # biggest channel difference per pixel
    mask = largest.point(lambda v: 255 if v > tolerance else 0)
    draw = ImageDraw.Draw(mask)
    for x, y, w, h in IGNORE.get(page_name, []):
        draw.rectangle([x, y, x + w - 1, y + h - 1], fill=0)
    return mask.histogram()[255] / (a.width * a.height), mask


def tint(image, mask):
    """Paints the differing pixels red (grown a bit so single pixels are visible)."""
    grown = mask.filter(ImageFilter.MaxFilter(5)).point(lambda v: 170 if v else 0)
    return Image.composite(Image.new("RGB", image.size, (255, 0, 0)), image.convert("RGB"), grown)


# Drawing

def load_font(size=13):
    try:
        return ImageFont.load_default(size=size)
    except TypeError:  # old Pillow: fixed size bitmap font
        return ImageFont.load_default()


def draw_grid(tiles, cols, scale, font):
    """tiles: list of (label, image or None, note, note colour) -> one image."""
    sizes = [t[1].size for t in tiles if t[1] is not None] or [(476, 270)]
    tile_w = max(w for w, _ in sizes) * scale
    tile_h = max(h for _, h in sizes) * scale
    cols = max(1, min(cols, len(tiles)))
    rows = math.ceil(len(tiles) / cols)

    sheet = Image.new("RGB", (GAP + cols * (tile_w + GAP), GAP + rows * (LABEL_H + tile_h + GAP)), BG)
    draw = ImageDraw.Draw(sheet)

    for index, (label, image, note, note_colour) in enumerate(tiles):
        x = GAP + (index % cols) * (tile_w + GAP)
        y = GAP + (index // cols) * (LABEL_H + tile_h + GAP)
        draw.rectangle([x, y, x + tile_w - 1, y + LABEL_H - 1], fill=LABEL_BG)
        draw.text((x + 6, y + 4), label, fill=TEXT, font=font)
        if note:
            width = draw.textlength(note, font=font)
            draw.text((x + tile_w - width - 6, y + 4), note, fill=note_colour, font=font)
        if image is None:
            draw.rectangle([x, y + LABEL_H, x + tile_w - 1, y + LABEL_H + tile_h - 1], fill=(60, 30, 30))
            draw.text((x + 10, y + LABEL_H + 10), "screenshot missing", fill=BAD, font=font)
        else:
            if scale != 1:
                image = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
            sheet.paste(image.convert("RGB"), (x, y + LABEL_H))
    return sheet


# Contact sheets

def build_sheet(ids, page, data, ref_id, args, font):
    """Returns (sheet image, {id: fraction different from ref or None})."""
    _, page_name = page
    ref_image = None
    if ref_id and page in data.get(ref_id, {}):
        ref_image = Image.open(data[ref_id][page]).convert("RGB")

    tiles, stats = [], {}
    for identifier in ids:
        path = data[identifier].get(page)
        if path is None:
            tiles.append((identifier, None, "", TEXT))
            continue
        image = Image.open(path).convert("RGB")
        note, colour = "", TEXT
        if ref_image is not None:
            if identifier == ref_id:
                note = "reference"
            else:
                fraction, mask = difference(ref_image, image, page_name, args.tolerance)
                stats[identifier] = fraction
                if fraction is None:
                    note, colour = "size differs", BAD
                else:
                    note, colour = f"{fraction * 100:.2f}% different", GOOD if fraction == 0 else BAD
                    if args.highlight and fraction > 0:
                        image = tint(image, mask)
        tiles.append((identifier, image, note, colour))
    return draw_grid(tiles, args.cols, args.scale, font), stats


def run_table(runs, out):
    """HTML: one row per run - finished or not, why not, and links to its logs."""
    if not runs:
        return ""
    labels = {"finished": ("good", "finished"), "crashed": ("bad", "crashed"),
              "unfinished": ("bad", "didn't finish"), "unknown": ("na", "no launcher.log")}
    rows = []
    for identifier in sorted(runs, key=version_key):
        run = runs[identifier]
        css, label = labels[run.status]
        links = [f"<a href='{html.escape(Path(os.path.relpath(path, out)).as_posix())}'>{name}</a>"
                 for name, path in [("launcher.log", run.launcher_log), ("latest.log", run.latest_log)]
                 + [(p.name, p) for p in run.crash_reports]
                 if path.exists()]
        reason = html.escape(run.reason) if run.reason else ""
        rows.append(f"<tr><th>{html.escape(identifier)}</th><td class='{css}'>{label}</td>"
                    f"<td>{' '.join(links)}</td><td class='left'>{reason}</td></tr>")
    failed = sum(run.failed for run in runs.values())
    summary = (f"<p class='bad'>{failed} of {len(runs)} run(s) didn't finish.</p>" if failed
               else f"<p class='good'>All {len(runs)} run(s) finished.</p>")
    return (f"<h2>Runs</h2>{summary}<table><tr><th></th><th>status</th><th>logs</th><th>reason</th></tr>"
            f"{''.join(rows)}</table>")


def check_tables(reports):
    """HTML: PASS / FAIL per suite and version, then every failing check."""
    if not reports:
        return ""
    ids = sorted(reports, key=version_key)
    suites = sorted({suite for found in reports.values() for suite in found})
    head = "".join(f"<th>{html.escape(suite)}</th>" for suite in suites)
    body = []
    for identifier in ids:
        cells = []
        for suite in suites:
            report = reports[identifier].get(suite)
            if report is None:
                cells.append("<td class='na'>-</td>")
            elif report.passed:
                cells.append(f"<td class='good'>PASS {len(report.checks)}</td>")
            else:
                cells.append(f"<td class='bad'>FAIL {len(report.failures)}/{len(report.checks)}</td>")
        body.append(f"<tr><th>{html.escape(identifier)}</th>{''.join(cells)}</tr>")
    table = f"<h2>Checks</h2><table><tr><th></th>{head}</tr>{''.join(body)}</table>"

    failures = []
    for identifier in ids:
        for suite, report in sorted(reports[identifier].items()):
            if not report.passed:
                items = "".join(f"<li>{html.escape(text)}</li>" for text in report.failures) \
                        or "<li>no FAIL line - the report doesn't start with RESULT PASS</li>"
                failures.append(f"<h3 class='bad'>{html.escape(identifier)} / {html.escape(suite)}</h3><ul>{items}</ul>")
    return table + (f"<h2>Failed checks</h2>{''.join(failures)}" if failures else "<p class='good'>Every check passed.</p>")


def list_tables(reports):
    """HTML: every list section - size per version, changes from version to version, loader differences."""
    sections = sorted({(suite, title) for found in reports.values()
                       for suite, report in found.items() for title in report.sections})
    if not sections:
        return ""
    ids = sorted(reports, key=version_key)
    parts = ["<h2>Lists</h2>"]
    for suite, title in sections:
        def entries(identifier):
            report = reports[identifier].get(suite)
            return None if report is None or title not in report.sections else report.sections[title]

        present = [i for i in ids if entries(i) is not None]

        # "name number" lines (e.g. SUGGESTION COUNTS): a table of the values instead of lists.
        if present and all(is_value_line(line) for i in present for line in entries(i)):
            names = sorted({line.split()[0] for i in present for line in entries(i)})
            head = "".join(f"<th>{html.escape(n)}</th>" for n in names)
            body = []
            for i in present:
                values = {line.split()[0]: line.split()[1] for line in entries(i)}
                body.append(f"<tr><th>{html.escape(i)}</th>"
                            + "".join(f"<td>{html.escape(values.get(n, '-'))}</td>" for n in names) + "</tr>")
            parts.append(f"<details><summary><b>{html.escape(title)}</b> <span class='na'>({html.escape(suite)})</span>"
                         f"</summary><table style='margin:8px 0 16px 16px'><tr><th></th>{head}</tr>{''.join(body)}</table>"
                         "</details>")
            continue

        sizes = "".join(f"<tr><th>{html.escape(i)}</th><td>{len(entries(i))}</td></tr>" for i in present)

        # Changes from one version to the next, per loader (ids are sorted by loader, then version).
        changes = []
        previous = {}
        for identifier in present:
            _, loader = split_id(identifier)
            keys = {entry_key(line) for line in entries(identifier)}
            if loader in previous:
                before_id, before = previous[loader]
                added, removed = sorted(keys - before), sorted(before - keys)
                if added or removed:
                    text = "".join(f"<div class='good'>+ {html.escape(k)}</div>" for k in added) \
                           + "".join(f"<div class='bad'>- {html.escape(k)}</div>" for k in removed)
                    changes.append(f"<h4>{html.escape(before_id)} &rarr; {html.escape(identifier)}</h4>{text}")
            previous[loader] = (identifier, keys)

        # Loaders of the same version that list different entries.
        differences = []
        by_version = {}
        for identifier in present:
            by_version.setdefault(split_id(identifier)[0], []).append(identifier)
        for version, same in by_version.items():
            first = {entry_key(line) for line in entries(same[0])}
            for other in same[1:]:
                keys = {entry_key(line) for line in entries(other)}
                if keys != first:
                    only_first, only_other = sorted(first - keys), sorted(keys - first)
                    differences.append(
                        f"<h4 class='bad'>{html.escape(same[0])} vs {html.escape(other)}</h4>"
                        + "".join(f"<div>only {html.escape(same[0])}: {html.escape(k)}</div>" for k in only_first)
                        + "".join(f"<div>only {html.escape(other)}: {html.escape(k)}</div>" for k in only_other))

        parts.append(
            f"<details><summary><b>{html.escape(title)}</b> <span class='na'>({html.escape(suite)})</span>"
            + (" <span class='bad'>loaders differ</span>" if differences else "") + "</summary>"
            f"<div class='cols'><div><h4>Size</h4><table>{sizes}</table></div>"
            f"<div><h4>Changes between versions</h4>{''.join(changes) or '<p class=na>none</p>'}</div>"
            f"<div><h4>Loader differences</h4>{''.join(differences) or '<p class=na>none</p>'}</div></div>"
            "</details>")
    return "".join(parts)


def screenshot_section(ids, pages, ref_id, all_stats):
    """HTML: the difference table to the reference version, then one collapsible block per page."""
    if not pages:
        return ""
    parts = ["<h2>Screenshots</h2>"]
    if ref_id:
        head = "".join(f"<th>{n}. {html.escape(name)}</th>" for n, name in pages)
        body = []
        for identifier in ids:
            cells = []
            for page in pages:
                if identifier == ref_id:
                    cells.append("<td class='ref'>ref</td>")
                elif page not in all_stats or identifier not in all_stats[page]:
                    cells.append("<td class='na'>-</td>")
                else:
                    fraction = all_stats[page][identifier]
                    if fraction is None:
                        cells.append("<td class='bad'>size</td>")
                    else:
                        cells.append(f"<td class='{'good' if fraction == 0 else 'bad'}'>{fraction * 100:.2f}%</td>")
            body.append(f"<tr><th>{html.escape(identifier)}</th>{''.join(cells)}</tr>")
        parts.append(f"<h3>Difference to {html.escape(ref_id)}</h3>"
                     f"<table><tr><th></th>{head}</tr>{''.join(body)}</table>")

    for page in pages:
        number, name = page
        stats = [f for f in all_stats.get(page, {}).values()]
        if not ref_id:
            note = ""
        elif any(f is None for f in stats):
            note = " <span class='bad'>size differs</span>"
        elif stats and max(stats) > 0:
            note = f" <span class='bad'>up to {max(stats) * 100:.2f}% different</span>"
        else:
            note = " <span class='good'>identical</span>"
        parts.append(f"<details><summary><b>{number}. {html.escape(name)}</b>{note}</summary>"
                     f"<img src='page-{number}-{html.escape(name)}.png'></details>")
    return "".join(parts)


def write_index(out, ids, pages, ref_id, all_stats, reports, runs):
    body = run_table(runs, out) + check_tables(reports) + list_tables(reports) \
           + screenshot_section(ids, pages, ref_id, all_stats)
    (out / "index.html").write_text(
        "<!doctype html><meta charset='utf-8'><title>Autotest comparison</title>"
        "<style>body{background:#181818;color:#ddd;font-family:sans-serif;margin:20px}"
        "table{border-collapse:collapse}td,th{border:1px solid #444;padding:4px 10px;text-align:center}"
        ".good{color:#78dc78}.bad{color:#f06e6e}.na,.ref{color:#888}td.left{text-align:left}a{color:#8ab4f8}img{max-width:100%;margin:8px 0 16px 16px}"
        "details{margin:6px 0}summary{cursor:pointer}.cols{display:flex;gap:40px;align-items:flex-start;"
        "margin:8px 0 16px 16px}.cols div{font-family:monospace}h4{margin:10px 0 4px}</style>"
        f"<h1>Autotest comparison</h1>{body}", encoding="utf-8")


# Regression check against an earlier run

def check_baseline(current, baseline_root, out, args, font):
    baseline = collect(baseline_root)
    changed = 0
    for identifier in sorted(current, key=version_key):
        if identifier not in baseline:
            print(f"{identifier}: not in the baseline, skipped")
            continue
        for page, path in sorted(current[identifier].items()):
            number, name = page
            if page not in baseline[identifier]:
                print(f"{identifier} page {number} ({name}): new page, no baseline")
                continue
            old = Image.open(baseline[identifier][page]).convert("RGB")
            new = Image.open(path).convert("RGB")
            fraction, mask = difference(old, new, name, args.tolerance)
            if fraction == 0:
                continue
            changed += 1
            print(f"{identifier} page {number} ({name}): "
                  + ("size changed" if fraction is None else f"{fraction * 100:.2f}% changed"))
            tiles = [("baseline", old, "", TEXT), ("now", new, "", TEXT)]
            if mask is not None:
                tiles.append(("changed", tint(new, mask), f"{fraction * 100:.2f}%", BAD))
            target = out / "regressions" / identifier
            target.mkdir(parents=True, exist_ok=True)
            draw_grid(tiles, 3, args.scale, font).save(target / path.name)
    print(f"\n{changed} screenshot(s) changed since the baseline"
          + (f" - see {out / 'regressions'}" if changed else ""))
    return changed


# Main

def default_reference(ids):
    """The newest Fabric version, or the newest version of whatever loader there is."""
    fabric = [i for i in ids if split_id(i)[1] == "fabric"]
    candidates = fabric or ids
    return max(candidates, key=lambda i: version_key(i)[1])


def main():
    parser = argparse.ArgumentParser(description="Compare the autotest screenshots of all versions.")
    parser.add_argument("root", nargs="?", default="autotest-report",
                        help="folder that contains one sub folder per version (default: autotest-report)")
    parser.add_argument("-o", "--out", default="autotest-comparison",
                        help="output folder (default: autotest-comparison) - not inside root")
    parser.add_argument("--ref", help="id of the reference version, e.g. 1.21.1-fabric: every other tile shows "
                                      "how much it differs from it (default: the newest Fabric version)")
    parser.add_argument("--no-ref", action="store_true", help="don't compare against a reference version at all")
    parser.add_argument("--highlight", action="store_true", help="paint the pixels that differ from --ref red")
    parser.add_argument("--only", help="regex: only use versions whose id matches")
    parser.add_argument("--cols", type=int, default=3, help="tiles per row (default: 3)")
    parser.add_argument("--scale", type=int, default=1, help="enlarge screenshots N times, no smoothing (default: 1)")
    parser.add_argument("--tolerance", type=int, default=0,
                        help="largest per-channel colour difference (0-255) still counted as equal (default: 0)")
    parser.add_argument("--baseline", help="folder of an earlier run; lists which screenshots changed "
                                           "(exit code 1 if any did)")
    args = parser.parse_args()

    root = Path(args.root)
    out = Path(args.out)
    if out.resolve() == root.resolve() or root.resolve() in out.resolve().parents:
        sys.exit(f"error: the output folder {out} must not be inside {root} - it would be read as a version")

    data = collect(root)
    reports = collect_reports(root)
    runs = collect_runs(root)
    if args.only:
        data = {i: p for i, p in data.items() if re.search(args.only, i)}
        reports = {i: r for i, r in reports.items() if re.search(args.only, i)}
        runs = {i: r for i, r in runs.items() if re.search(args.only, i)}
    if not data and not reports and not runs:
        sys.exit(f"error: nothing found in {root} (expected <id>/<suite>.txt or <id>/page-N-name.png)")
    if args.ref and args.ref not in data:
        sys.exit(f"error: reference '{args.ref}' has no screenshots, available: {', '.join(sorted(data, key=version_key))}")

    ids = sorted(data, key=version_key)
    if not args.ref and ids and not args.no_ref:
        args.ref = default_reference(ids)
    pages = sorted({page for pages in data.values() for page in pages})
    out.mkdir(parents=True, exist_ok=True)
    font = load_font()

    all_stats = {}
    for page in pages:
        sheet, stats = build_sheet(ids, page, data, args.ref, args, font)
        all_stats[page] = stats
        sheet.save(out / f"page-{page[0]}-{page[1]}.png")
    write_index(out, ids, pages, args.ref, all_stats, reports, runs)

    # Console summary of the runs.
    unfinished = [runs[i] for i in sorted(runs, key=version_key) if runs[i].failed]
    print(f"{len(runs)} run(s), {len(unfinished)} didn't finish")
    for run in unfinished:
        print(f"  {run.status.upper()} {run.id}" + (f": {run.reason}" if run.reason else ""))
        print(f"       {run.launcher_log}")

    # Console summary of the checks.
    failed = [(i, report) for i in sorted(reports, key=version_key)
              for report in reports[i].values() if not report.passed]
    report_count = sum(len(found) for found in reports.values())
    print(f"{len(reports)} version(s) with {report_count} report(s), {len(failed)} failed; "
          f"{len(ids)} version(s) with screenshots, {len(pages)} page(s) -> {out / 'index.html'}")
    for identifier, report in failed:
        print(f"  FAIL {identifier} / {report.suite}")
        for text in report.failures:
            print(f"       {text}")

    changed = 0
    if args.baseline:
        print()
        changed = check_baseline(data, Path(args.baseline), out, args, font)
    sys.exit(1 if unfinished or failed or changed else 0)


if __name__ == "__main__":
    main()
