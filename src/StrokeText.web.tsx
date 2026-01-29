import React from 'react'
import { I18nManager, StyleSheet, Text, View, type TextStyle } from 'react-native'

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
    fontSize: styleFontSize,
    fontWeight: styleFontWeight,
    fontFamily: styleFontFamily,
    fontStyle: styleFontStyle,
    lineHeight: styleLineHeight,
    letterSpacing: styleLetterSpacing,
    textAlign: styleTextAlign,
    textDecorationLine: styleTextDecorationLine,
    textTransform: styleTextTransform,
    opacity: styleOpacity,
    padding: stylePadding,
    paddingVertical: stylePaddingVertical,
    paddingHorizontal: stylePaddingHorizontal,
    paddingTop: stylePaddingTop,
    paddingRight: stylePaddingRight,
    paddingBottom: stylePaddingBottom,
    paddingLeft: stylePaddingLeft,
    ...containerStyle
  } = flattened ?? {}

  const textStyle = {
    fontSize: rest.fontSize ?? styleFontSize,
    fontWeight: (rest.fontWeight ?? styleFontWeight) as TextStyle['fontWeight'],
    fontFamily: rest.fontFamily ?? styleFontFamily,
    fontStyle: rest.fontStyle ?? styleFontStyle,
    lineHeight: rest.lineHeight ?? styleLineHeight,
    letterSpacing: rest.letterSpacing ?? styleLetterSpacing,
    textAlign: rest.textAlign ?? styleTextAlign,
    textDecorationLine: rest.textDecorationLine ?? styleTextDecorationLine,
    textTransform: rest.textTransform ?? styleTextTransform,
    opacity: rest.opacity ?? styleOpacity,
  } satisfies TextStyle

  const fillColor = rest.color ?? styleColor ?? '#000'
  const strokeColor = rest.strokeColor ?? 'transparent'
  const strokeWidth = Math.max(0, rest.strokeWidth ?? 0)
  const strokeInset = Math.ceil(strokeWidth) / 2

  const baseMarginTop =
    firstNumber(
      containerStyle.marginTop,
      containerStyle.marginVertical,
      containerStyle.margin
    ) ?? 0
  const baseMarginRight =
    firstNumber(
      containerStyle.marginRight,
      I18nManager.isRTL ? containerStyle.marginStart : containerStyle.marginEnd,
      containerStyle.marginHorizontal,
      containerStyle.margin
    ) ?? 0
  const baseMarginBottom =
    firstNumber(
      containerStyle.marginBottom,
      containerStyle.marginVertical,
      containerStyle.margin
    ) ?? 0
  const baseMarginLeft =
    firstNumber(
      containerStyle.marginLeft,
      I18nManager.isRTL ? containerStyle.marginEnd : containerStyle.marginStart,
      containerStyle.marginHorizontal,
      containerStyle.margin
    ) ?? 0

  const baseMarginStart = toNumber(containerStyle.marginStart)
  const baseMarginEnd = toNumber(containerStyle.marginEnd)

  const strokeInsetMarginStyle =
    strokeInset === 0
      ? null
      : {
          marginTop: baseMarginTop - strokeInset,
          marginRight: baseMarginRight - strokeInset,
          marginBottom: baseMarginBottom - strokeInset,
          marginLeft: baseMarginLeft - strokeInset,
          ...(baseMarginStart == null
            ? {}
            : { marginStart: baseMarginStart - strokeInset }),
          ...(baseMarginEnd == null ? {} : { marginEnd: baseMarginEnd - strokeInset }),
        }

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
    <View style={[styles.container, containerStyle, strokeInsetMarginStyle]}>
      <Text
        accessible={false}
        pointerEvents="none"
        numberOfLines={effectiveNumberOfLines}
        ellipsizeMode={effectiveEllipsizeMode}
        style={[
          textStyle,
          {
            paddingTop: baseTop + strokeInset,
            paddingRight: baseRight + strokeInset,
            paddingBottom: baseBottom + strokeInset,
            paddingLeft: baseLeft + strokeInset,
          },
          strokeInset === 0 ? null : ({ maxWidth: 'none' } as any),
          strokeInset === 0 ? null : ({ boxSizing: 'content-box' } as any),
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
          textStyle,
          {
            color: fillColor,
            paddingTop: baseTop + strokeInset,
            paddingRight: baseRight + strokeInset,
            paddingBottom: baseBottom + strokeInset,
            paddingLeft: baseLeft + strokeInset,
          },
          strokeInset === 0 ? null : ({ maxWidth: 'none' } as any),
          // react-native-web uses `box-sizing: border-box` globally; with ellipsizing enabled
          // (`overflow: hidden` + `text-overflow: ellipsis`), the extra stroke padding can reduce
          // the content box by a couple pixels and cause false-positive ellipses for some fonts.
          // Use `content-box` so padding doesn't shrink the text's available width.
          strokeInset === 0 ? null : ({ boxSizing: 'content-box' } as any),
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
