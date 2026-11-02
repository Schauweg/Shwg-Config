# 0.2.0-alpha.3
- disabled widgets now gray out their text (colors configurable per widget)
- dropdowns open with the current value selected
- tooltips wrap at a max width (170 px by default)
- added GenericTooltip.builder() with bullet points
- BREAKING: custom widget builders implement create() instead of build()
- fixed float/double sliders and number inputs stepping to values like 1.3000001
- fixed sliders snapping past their max value
- list selection outline only shows while the list has focus
- dropdown and suggestion scrollbars sit flush with the right edge
