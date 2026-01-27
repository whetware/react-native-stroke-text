# Plan: `@whetware/react-native-stroke-text`

## Goals
- Ship a **new-architecture-only** React Native library that renders **stroke/outline text** as a native view on **iOS (Swift)** and **Android (Kotlin)**, plus a **web** implementation.
- Use **Nitro Modules + Nitrogen** to generate the Fabric host component bindings.
- Provide **Expo** support (managed workflow via prebuild/EAS), including an optional config plugin to enable New Arch flags.

## Non-goals (initial cut)
- Full parity with React Native `<Text>` (nested children, inline spans, rich text).
- Pixel-perfect typography parity across platforms (some differences are expected).
- Supporting the old architecture.

## Public API (TS)
- Export a single component: `StrokeText`.
- Props (v1):
  - Content: `text?: string`, `children?: string`
  - Stroke: `strokeColor?: string`, `strokeWidth?: number`
  - Fill: `color?: string`
  - Layout behavior: `numberOfLines?: number`, `ellipsizeMode?: 'head' | 'middle' | 'tail' | 'clip'`
  - Typography: `fontSize?`, `fontWeight?`, `fontFamily?`, `fontStyle?`, `letterSpacing?`, `lineHeight?`, `textAlign?`, `textDecorationLine?`, `textTransform?`, `opacity?`
  - Optional padding props (to avoid clipping): `padding?`, `paddingHorizontal?`, `paddingVertical?`, `paddingTop?`, `paddingRight?`, `paddingBottom?`, `paddingLeft?`
  - `style` is accepted for layout; supported text-style keys are also read from `style` and forwarded as explicit props.

## Nitro/Nitrogen wiring
- Add a `*.nitro.ts` spec that declares a Nitro **Hybrid View** for the component (no methods in v1).
- Update `nitro.json` `autolinking` to register the Swift/Kotlin implementations.
- Run `pnpm specs` (Nitrogen) to generate and commit `nitrogen/generated/**` (C++/Swift/Kotlin + Fabric view config JSON).
- Android: ensure the library `ReactPackage` registers the generated `ViewManager` via `ViewManagerOnDemandReactPackage` (required when React Native's lazy view managers are enabled).

## Native implementation
### iOS (Swift)
- Implement a `StrokedTextLabel` (UILabel subclass) that draws stroke + fill in `drawText(in:)` and expands `intrinsicContentSize` based on `strokeWidth`.
- Implement a container `StrokeTextView` (UIView) that hosts the label, applies padding, handles multiline/truncation, and invalidates intrinsic size on prop changes.
- Implement `HybridStrokeTextView : HybridStrokeTextViewSpec` that owns the native `StrokeTextView` and maps Nitro props → native view updates.

### Android (Kotlin)
- Implement a custom `StrokeTextView` (View) that:
  - Uses `TextPaint` + `StaticLayout` to render both stroke and fill.
  - Handles padding, multiline, alignment, `ellipsizeMode`, and `numberOfLines`.
  - Converts RN-style units (dp/sp-like numbers) to px.
  - Requests layout + invalidates when props change.
- Implement `HybridStrokeTextView : HybridStrokeTextViewSpec` that owns the `StrokeTextView` and maps Nitro props → view updates.

### Web (JS)
- Provide `src/index.web.ts(x)` that exports `StrokeText` implemented in pure React:
  - Render a `<Text>` with multi-direction `textShadow` to approximate an outline.
  - Keep the same prop surface as native (best-effort).

## Expo support
- Ensure the package autolinks cleanly in prebuild projects (podspec + Gradle already in template).
- Add an optional `app.plugin.js` config plugin that enables New Architecture flags for Expo prebuild:
  - Android: set `newArchEnabled=true` in `android/gradle.properties`
  - iOS: set `newArchEnabled=true` in `ios/Podfile.properties.json` (Expo-managed iOS projects)
- Document required Expo settings + versions in `README.md`.

## Package hygiene
- Update `package.json` name to `@whetware/react-native-stroke-text` and align README metadata.
- Keep native module name (`NitroStrokeText`) as-is unless renaming becomes necessary.

## Validation
- `pnpm typecheck`
- `pnpm specs` (ensures `nitrogen/generated/**` is up-to-date)

## Open questions
- Should `strokeWidth` be interpreted as **dp** or **sp**? (Plan assumes “dp-like” numeric and converts to px; font-size uses sp-like scaling.)
- Do we want `strokeLinejoin`/`strokeLinecap` options, or keep it round-join only for now?
