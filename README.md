# `@whetware/react-native-stroke-text`

Stroke/outline text for React Native, implemented as a **Nitro Hybrid View** (Fabric / New Architecture).

## Support
- iOS: ✅ (Swift)
- Android: ✅ (Kotlin)
- Web: ✅ (JS fallback)
- Old architecture: ❌ (not supported)

## Installation
```sh
pnpm add @whetware/react-native-stroke-text react-native-nitro-modules
```

### Local (path) install
For local development, prefer `link:` so changes are picked up without repacking:
```sh
pnpm add @whetware/react-native-stroke-text@link:../path/to/react-native-stroke-text
```

### Expo (managed)
This package requires the **New Architecture**.

- `app.json` / `app.config.js`:
  - Add the plugin: `["@whetware/react-native-stroke-text"]`
  - Ensure `newArchEnabled: true`
- Then run: `npx expo prebuild`

## Usage
```tsx
import { StrokeText } from '@whetware/react-native-stroke-text'

export function Example() {
  return (
    <StrokeText
      strokeWidth={4}
      strokeColor="#000"
      style={{ fontSize: 48, fontWeight: '800', color: '#fff' }}
    >
      Hello
    </StrokeText>
  )
}
```

## Development
- Generate Nitro bindings: `pnpm specs`
- Typecheck: `pnpm typecheck`

## Troubleshooting
- Android: if you see `Can't find ViewManager 'StrokeTextView'...`, run a clean rebuild (`cd android && ./gradlew clean`) and ensure your app autolinks `NitroStrokeTextPackage`.
