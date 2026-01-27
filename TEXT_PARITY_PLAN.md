# StrokeText ↔ React Native `<Text>` styling parity (v1)

## Goal
Make `StrokeText` render (stroke + fill) with **roughly the same typography and layout rules** as React Native’s default `<Text>` for the most common props, without aiming for pixel-perfect parity.

## Why this matters in this package
`StrokeText` uses a **hidden `<Text>`** for layout/measurement and overlays a **native Nitro Hybrid View** for drawing. If the native view interprets typography props differently than RN, the overlay will look “off” (font choice, weight, spacing, baseline, clipping, etc.).

## Scope (keep simple)
### Must-have parity (common props)
- `fontFamily`, `fontWeight`, `fontStyle`: resolve the same fonts/weights RN would (including custom fonts).
- `fontSize`, `lineHeight`, `letterSpacing`: match RN’s **unit conversion + scaling rules**.
- `allowFontScaling`, `maxFontSizeMultiplier`: common RN props that affect fontSize/lineHeight/letterSpacing.
- `color`: default should match RN theme default; explicit color should match.
- `includeFontPadding` (Android): supported; default is `false` in this package to avoid Android’s
  extra font padding shifting glyphs downward. (RN `<Text>` defaults `true`.)
- `textAlign`, `numberOfLines`, `ellipsizeMode` (default `tail`): keep behavior close enough for typical usage.
  - Note: React Native Android only supports `tail` reliably when `numberOfLines > 1`.
- `textDecorationLine`, `textTransform`, `opacity`.

### Explicitly out of scope (for now)
- Nested `<Text>` children / rich text spans / inline views & images.
- `textAlignVertical`.
- Selection, links/press handling, accessibility parity.
- `fontVariant` / `fontFeatureSettings` and other advanced typography.
- Exact line-height semantics (RN uses span-based, web-like behavior; we’ll do a simpler approximation).

## Implementation checklist
### 1) JS wrapper (`src/StrokeText.tsx`)
- [x] Forward new props to **both** the hidden `<Text>` and the native view:
  - `allowFontScaling`, `maxFontSizeMultiplier`, `includeFontPadding` (Android only but safe to pass).
- [x] Keep extracting common text-style keys from `style` to preserve the ergonomic “drop-in Text” feel.
- [x] Keep fill text aligned with RN layout:
  - Do **not** pad the hidden `<Text>` for stroke.
  - Expand the native overlay bounds by `ceil(strokeWidth) / 2` so stroke can render outside without shifting the fill.

### 2) Nitro spec (`src/specs/StrokeTextView.nitro.ts`)
- [x] Add `allowFontScaling?: boolean`, `maxFontSizeMultiplier?: number`, `includeFontPadding?: boolean`.
- [x] Re-run Nitrogen (`pnpm specs`) so the generated view manager/specs stay in sync.

### 3) Android native (`android/src/main/java/...`)
**Primary goal:** match the same conversions and defaults RN uses in `TextAttributes` / `TextAttributeProps`.
- [x] Unit conversions:
  - **dp-like**: padding, strokeWidth → px using `density`.
  - **text-like**: fontSize/lineHeight/letterSpacing → px using `scaledDensity` when `allowFontScaling=true`, clamped by `maxFontSizeMultiplier`.
  - letterSpacing: compute **em** (`letterSpacingPx / fontSizePx`) before setting `TextPaint.letterSpacing`.
- [x] Default `color` to the theme’s default text color (not hardcoded black).
- [x] Support `includeFontPadding`; wire to `StaticLayout.Builder.setIncludePad(includeFontPadding)` (default `false`).
- [x] Typeface resolution:
  - Use RN’s helpers (e.g. `ReactTypefaceUtils.applyStyles(...)`) so numeric weights and custom fonts behave like `<Text>`.
- [x] Stroke inset:
  - Match JS logic: inset = `ceil(strokeWidth) / 2` in dp, then convert to px (avoid `ceil(px)`).

### 4) iOS native (`ios/*.swift`)
**Primary goal:** avoid obvious font/weight mismatches vs RN.
- [x] Font resolution:
  - When `fontFamily` is provided, pick the closest matching font within that family (weight + italic) rather than only `UIFont(name:)`.
- [x] Implement `allowFontScaling` + `maxFontSizeMultiplier` with a simple `UIFontMetrics`-based scaling (good enough for v1).
- [x] Keep `lineHeight` / `letterSpacing` applied via `NSAttributedString` attributes.
- [x] Default `color` to the platform default label color when not provided (avoid forcing black in dark mode).

## Validation checklist
- [ ] Visual sanity checks on Android + iOS:
  - weights: `400`, `600`, `700`, `bold`
  - families: system + one custom Expo font
  - `letterSpacing`, `lineHeight`, `textAlign`, `numberOfLines` + `ellipsizeMode`
  - `allowFontScaling` on/off and `maxFontSizeMultiplier`
- [x] `pnpm typecheck`
- [x] `pnpm specs` (if spec changed)
