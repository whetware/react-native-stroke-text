import type { ReactNode } from 'react'
import type { StyleProp, TextStyle } from 'react-native'
import type {
  StrokeTextAlign,
  StrokeTextDecorationLine,
  StrokeTextEllipsizeMode,
  StrokeTextFontStyle,
  StrokeTextMethods,
  StrokeTextProps as StrokeTextNitroProps,
  StrokeTextTransform,
  StrokeTextView,
} from './specs/StrokeTextView.nitro'

export type {
  StrokeTextAlign,
  StrokeTextDecorationLine,
  StrokeTextEllipsizeMode,
  StrokeTextFontStyle,
  StrokeTextMethods,
  StrokeTextTransform,
  StrokeTextView,
}

type OptionalKeys<T> = {
  [K in keyof T]-?: {} extends Pick<T, K> ? K : never
}[keyof T]

// When consumers use `exactOptionalPropertyTypes: true`, `prop?: T` does *not* accept
// `T | undefined` if you explicitly pass the prop. Most React code passes `T | undefined`, so we
// widen optional prop value types to include `undefined` for compatibility.
type OptionalPropsAcceptUndefined<T> = Omit<T, OptionalKeys<T>> & {
  [K in OptionalKeys<T>]?: T[K] | undefined
}

export type StrokeTextNativeProps = OptionalPropsAcceptUndefined<StrokeTextNitroProps>

export type StrokeTextProps = OptionalPropsAcceptUndefined<
  Omit<StrokeTextNitroProps, 'text'> & {
    children?: ReactNode
    text?: string
    style?: StyleProp<TextStyle>
    hybridRef?: (ref: StrokeTextView) => void
  }
>
