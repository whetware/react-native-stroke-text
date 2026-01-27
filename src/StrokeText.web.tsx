import React from 'react'
import { StyleSheet, Text, View, type TextStyle } from 'react-native'

import type { StrokeTextProps } from './types'

function resolveText(text: unknown, children: unknown): string {
  if (typeof text === 'string') return text
  if (typeof children === 'string') return children
  return ''
}

function toNumber(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined
}

function firstNumber(...values: unknown[]): number | undefined {
  for (const value of values) {
    const n = toNumber(value)
    if (n != null) return n
  }
  return undefined
}

export function StrokeText({ text, children, style, ...rest }: StrokeTextProps) {
  const resolvedText = resolveText(text, children)
  const flattened = StyleSheet.flatten(style) as TextStyle | undefined

  const {
    color: styleColor,
    fontSize: _styleFontSize,
    fontWeight: _styleFontWeight,
    fontFamily: _styleFontFamily,
    fontStyle: _styleFontStyle,
    lineHeight: _styleLineHeight,
    letterSpacing: _styleLetterSpacing,
    textAlign: _styleTextAlign,
    textDecorationLine: _styleTextDecorationLine,
    textTransform: _styleTextTransform,
    opacity: _styleOpacity,
    includeFontPadding: _styleIncludeFontPadding,
    padding: stylePadding,
    paddingVertical: stylePaddingVertical,
    paddingHorizontal: stylePaddingHorizontal,
    paddingTop: stylePaddingTop,
    paddingRight: stylePaddingRight,
    paddingBottom: stylePaddingBottom,
    paddingLeft: stylePaddingLeft,
    ...containerStyle
  } = flattened ?? {}

  const fillColor = rest.color ?? styleColor ?? '#000'
  const strokeColor = rest.strokeColor ?? 'transparent'
  const strokeWidth = Math.max(0, rest.strokeWidth ?? 0)
  const strokeInset = Math.ceil(strokeWidth) / 2

  const baseTop =
    firstNumber(
      rest.paddingTop,
      stylePaddingTop,
      rest.paddingVertical,
      stylePaddingVertical,
      rest.padding,
      stylePadding
    ) ?? 0
  const baseRight =
    firstNumber(
      rest.paddingRight,
      stylePaddingRight,
      rest.paddingHorizontal,
      stylePaddingHorizontal,
      rest.padding,
      stylePadding
    ) ?? 0
  const baseBottom =
    firstNumber(
      rest.paddingBottom,
      stylePaddingBottom,
      rest.paddingVertical,
      stylePaddingVertical,
      rest.padding,
      stylePadding
    ) ?? 0
  const baseLeft =
    firstNumber(
      rest.paddingLeft,
      stylePaddingLeft,
      rest.paddingHorizontal,
      stylePaddingHorizontal,
      rest.padding,
      stylePadding
    ) ?? 0

  const effectiveNumberOfLines =
    rest.numberOfLines != null && rest.numberOfLines > 0
      ? rest.numberOfLines
      : undefined
  const effectiveEllipsizeMode =
    effectiveNumberOfLines == null ? undefined : rest.ellipsizeMode ?? 'tail'

  return (
    <View style={[styles.container, containerStyle]}>
      <Text
        accessible={false}
        pointerEvents="none"
        numberOfLines={effectiveNumberOfLines}
        ellipsizeMode={effectiveEllipsizeMode}
        style={[
          style,
          {
            paddingTop: baseTop,
            paddingRight: baseRight,
            paddingBottom: baseBottom,
            paddingLeft: baseLeft,
          },
          styles.hiddenText,
        ]}
      >
        {resolvedText}
      </Text>

      <Text
        pointerEvents="none"
        numberOfLines={effectiveNumberOfLines}
        ellipsizeMode={effectiveEllipsizeMode}
        style={[
          style,
          {
            color: fillColor,
            paddingTop: baseTop + strokeInset,
            paddingRight: baseRight + strokeInset,
            paddingBottom: baseBottom + strokeInset,
            paddingLeft: baseLeft + strokeInset,
          },
          strokeWidth > 0 && strokeColor !== 'transparent'
            ? ({
                WebkitTextStrokeWidth: `${strokeWidth}px`,
                WebkitTextStrokeColor: strokeColor,
                WebkitTextFillColor: fillColor,
                textStroke: `${strokeWidth}px ${strokeColor}`,
                textFillColor: fillColor,
                paintOrder: 'stroke fill',
              } as any)
            : null,
          styles.overlay,
          strokeInset === 0
            ? null
            : {
                top: -strokeInset,
                right: -strokeInset,
                bottom: -strokeInset,
                left: -strokeInset,
              },
        ]}
      >
        {resolvedText}
      </Text>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    alignSelf: 'flex-start',
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
  },
  hiddenText: {
    opacity: 0,
  },
})
