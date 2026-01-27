import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules'

export type StrokeTextAlign = 'auto' | 'left' | 'right' | 'center' | 'justify'
export type StrokeTextDecorationLine =
  | 'none'
  | 'underline'
  | 'line-through'
  | 'underline line-through'
export type StrokeTextTransform = 'none' | 'uppercase' | 'lowercase' | 'capitalize'
export type StrokeTextFontStyle = 'normal' | 'italic'
export type StrokeTextEllipsizeMode = 'head' | 'middle' | 'tail' | 'clip'

export interface StrokeTextProps extends HybridViewProps {
  text: string

  color?: string
  strokeColor?: string
  strokeWidth?: number

  fontSize?: number
  fontWeight?: string
  fontFamily?: string
  fontStyle?: StrokeTextFontStyle
  lineHeight?: number
  letterSpacing?: number
  textAlign?: StrokeTextAlign
  textDecorationLine?: StrokeTextDecorationLine
  textTransform?: StrokeTextTransform
  opacity?: number

  allowFontScaling?: boolean
  maxFontSizeMultiplier?: number
  includeFontPadding?: boolean

  numberOfLines?: number
  ellipsizeMode?: StrokeTextEllipsizeMode

  padding?: number
  paddingVertical?: number
  paddingHorizontal?: number
  paddingTop?: number
  paddingRight?: number
  paddingBottom?: number
  paddingLeft?: number
}

export interface StrokeTextMethods extends HybridViewMethods {}

export type StrokeTextView = HybridView<StrokeTextProps, StrokeTextMethods>
