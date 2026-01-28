# `@whetware/react-native-stroke-text`

Stroke/outline text for React Native, implemented as a **Nitro Hybrid View** (Fabric / New Architecture only).

## Support

- iOS: ✅ (Swift)
- Android: ✅ (Kotlin)
- Web: ✅ (JS fallback)
- Old architecture: ❌ (not supported)

## Installation

```sh
pnpm add @whetware/react-native-stroke-text react-native-nitro-modules
```

### Expo (managed)

This package requires the **New Architecture**. Ensure it’s enabled in your app before installing.

## Usage

```tsx
import { StrokeText } from '@whetware/react-native-stroke-text'

export function Example() {
  return (
    <StrokeText
      strokeWidth={4}
      strokeColor="#000"
      numberOfLines={1}
      ellipsizeMode="tail"
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
