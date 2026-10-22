#!/usr/bin/env python3
"""
Compares the screenshots written by AutoTest (autotest/<id>/page-N-name.png) across all versions.

Makes one contact sheet per page (every version side by side, labelled) plus an index.html, and can
  * show how much each version differs from a reference version (--ref, optionally --highlight), and
  * check for regressions against the screenshots of an earlier run (--baseline).

Needs Pillow:  pip install pillow

Examples:
    ./compare_autotest.py run/autotest
    ./compare_autotest.py run/autotest --ref 1.21.1-fabric --highlight
    ./compare_autotest.py run/autotest --only "fabric$" --cols 2 --scale 2
    ./compare_autotest.py run/autotest --baseline old-run/autotest      (exit code 1 if anything changed)
"""

import argparse
import html
import math
import re
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

PAGE_RE = re.compile(r"^page-(\d+)-(.+)\.png$")

# Regions (x, y, w, h in GUI pixels) that legitimately change from run to run, per page name.
# These are the two always-scrolling text rows on the "text" page of RenderTestScreen:
# cell 2 and cell 4 of the 2-column grid (origin 10,24; cell 226x76; gap 4).
IGNORE = {
    "text": [(10, 104, 226, 76), (10, 184, 226, 76)],
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


def write_index(out, ids, pages, ref_id, all_stats):
    rows = []
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
        rows.append(f"<h2>Difference to {html.escape(ref_id)}</h2>"
                    f"<table><tr><th></th>{head}</tr>{''.join(body)}</table>")

    sheets = "".join(
        f"<h2>{n}. {html.escape(name)}</h2><img src='page-{n}-{html.escape(name)}.png'>" for n, name in pages)
    (out / "index.html").write_text(
        "<!doctype html><meta charset='utf-8'><title>Autotest comparison</title>"
        "<style>body{background:#181818;color:#ddd;font-family:sans-serif;margin:20px}"
        "table{border-collapse:collapse}td,th{border:1px solid #444;padding:4px 10px;text-align:center}"
        ".good{color:#78dc78}.bad{color:#f06e6e}.na,.ref{color:#888}img{max-width:100%}</style>"
        f"<h1>Autotest comparison</h1>{''.join(rows)}{sheets}", encoding="utf-8")


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

def main():
    parser = argparse.ArgumentParser(description="Compare the autotest screenshots of all versions.")
    parser.add_argument("root", nargs="?", default="run/autotest",
                        help="folder that contains one sub folder per version (default: run/autotest)")
    parser.add_argument("-o", "--out", default="autotest-report", help="output folder (default: autotest-report)")
    parser.add_argument("--ref", help="id of a reference version, e.g. 1.21.1-fabric: every other tile shows "
                                      "how much it differs from it")
    parser.add_argument("--highlight", action="store_true", help="paint the pixels that differ from --ref red")
    parser.add_argument("--only", help="regex: only use versions whose id matches")
    parser.add_argument("--cols", type=int, default=3, help="tiles per row (default: 3)")
    parser.add_argument("--scale", type=int, default=1, help="enlarge screenshots N times, no smoothing (default: 1)")
    parser.add_argument("--tolerance", type=int, default=0,
                        help="largest per-channel colour difference (0-255) still counted as equal (default: 0)")
    parser.add_argument("--baseline", help="folder of an earlier run; lists which screenshots changed "
                                           "(exit code 1 if any did)")
    args = parser.parse_args()

    data = collect(Path(args.root))
    if args.only:
        data = {i: p for i, p in data.items() if re.search(args.only, i)}
    if not data:
        sys.exit("error: no screenshots found (expected <folder>/<id>/page-N-name.png)")
    if args.ref and args.ref not in data:
        sys.exit(f"error: reference '{args.ref}' not found, available: {', '.join(sorted(data, key=version_key))}")

    ids = sorted(data, key=version_key)
    pages = sorted({page for pages in data.values() for page in pages})
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    font = load_font()

    all_stats = {}
    for page in pages:
        sheet, stats = build_sheet(ids, page, data, args.ref, args, font)
        all_stats[page] = stats
        sheet.save(out / f"page-{page[0]}-{page[1]}.png")
    write_index(out, ids, pages, args.ref, all_stats)
    print(f"{len(ids)} version(s), {len(pages)} page(s) -> {out / 'index.html'}")

    if args.baseline:
        print()
        sys.exit(1 if check_baseline(data, Path(args.baseline), out, args, font) else 0)


if __name__ == "__main__":
    main()
