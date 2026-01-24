import type { ReactNode } from 'react'
import type { StyleProp, TextStyle } from 'react-native'
import type {
  StrokeTextAlign,
  StrokeTextDecorationLine,
  StrokeTextFontStyle,
  StrokeTextMethods,
  StrokeTextProps as StrokeTextNitroProps,
  StrokeTextTransform,
  StrokeTextView,
} from './specs/StrokeTextView.nitro'

export type {
  StrokeTextAlign,
  StrokeTextDecorationLine,
  StrokeTextFontStyle,
  StrokeTextMethods,
  StrokeTextTransform,
  StrokeTextView,
}

export type StrokeTextNativeProps = StrokeTextNitroProps

export type StrokeTextProps = Omit<StrokeTextNitroProps, 'text'> & {
  children?: ReactNode
  text?: string
  style?: StyleProp<TextStyle>
  hybridRef?: (ref: StrokeTextView) => void
}

