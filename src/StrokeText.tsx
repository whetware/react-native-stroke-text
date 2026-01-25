import React from 'react'
import { StyleSheet, Text, View, type TextStyle } from 'react-native'
import { callback, getHostComponent } from 'react-native-nitro-modules'

import StrokeTextViewConfig from '../nitrogen/generated/shared/json/StrokeTextViewConfig.json'
import type { StrokeTextMethods, StrokeTextNativeProps, StrokeTextProps } from './types'

const NativeStrokeTextView = getHostComponent<
  StrokeTextNativeProps,
  StrokeTextMethods
>('StrokeTextView', () => StrokeTextViewConfig)

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

function toFontWeightString(value: unknown): string | undefined {
  if (typeof value === 'string') return value
  if (typeof value === 'number') return `${value}`
  return undefined
}

function toColorString(value: unknown): string | undefined {
  if (typeof value === 'string') return value
  if (typeof value === 'number') {
    const c = value >>> 0
    const a = ((c >>> 24) & 0xff) / 255
    const r = (c >>> 16) & 0xff
    const g = (c >>> 8) & 0xff
    const b = c & 0xff
    return `rgba(${r},${g},${b},${a})`
  }
  return undefined
}

function warnOnInvalidChildren(children: unknown) {
  if (!__DEV__) return
  if (children == null) return
  if (typeof children === 'string') return
  // eslint-disable-next-line no-console
  console.warn(
    '[StrokeText] Children must be a string. Use the `text` prop instead.'
  )
}

export function StrokeText({
  text,
  children,
  style,
  hybridRef,
  ...rest
}: StrokeTextProps) {
  warnOnInvalidChildren(children)
  const resolvedText = resolveText(text, children)
  const flattened = StyleSheet.flatten(style) as TextStyle | undefined

  const {
    padding,
    paddingVertical,
    paddingHorizontal,
    paddingTop,
    paddingRight,
    paddingBottom,
    paddingLeft,
    ...nativeProps
  } = rest

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
    includeFontPadding: styleIncludeFontPadding,
    padding: stylePadding,
    paddingVertical: stylePaddingVertical,
    paddingHorizontal: stylePaddingHorizontal,
    paddingTop: stylePaddingTop,
    paddingRight: stylePaddingRight,
    paddingBottom: stylePaddingBottom,
    paddingLeft: stylePaddingLeft,
    ...containerStyle
  } = flattened ?? {}

  const strokeWidth = Math.max(0, nativeProps.strokeWidth ?? 0)
  const strokeInset = Math.ceil(strokeWidth) / 2

  const baseTop =
    firstNumber(
      paddingTop,
      stylePaddingTop,
      paddingVertical,
      stylePaddingVertical,
      padding,
      stylePadding
    ) ?? 0
  const baseRight =
    firstNumber(
      paddingRight,
      stylePaddingRight,
      paddingHorizontal,
      stylePaddingHorizontal,
      padding,
      stylePadding
    ) ?? 0
  const baseBottom =
    firstNumber(
      paddingBottom,
      stylePaddingBottom,
      paddingVertical,
      stylePaddingVertical,
      padding,
      stylePadding
    ) ?? 0
  const baseLeft =
    firstNumber(
      paddingLeft,
      stylePaddingLeft,
      paddingHorizontal,
      stylePaddingHorizontal,
      padding,
      stylePadding
    ) ?? 0

  const effectiveNumberOfLines =
    nativeProps.numberOfLines != null && nativeProps.numberOfLines > 0
      ? nativeProps.numberOfLines
      : undefined
  const effectiveIncludeFontPadding =
    nativeProps.includeFontPadding ?? styleIncludeFontPadding

  return (
    <View style={[styles.container, containerStyle]}>
      <Text
        accessible={false}
        pointerEvents="none"
        numberOfLines={effectiveNumberOfLines}
        ellipsizeMode={rest.ellipsis ? 'tail' : undefined}
        allowFontScaling={nativeProps.allowFontScaling}
        maxFontSizeMultiplier={nativeProps.maxFontSizeMultiplier}
        style={[
          style,
          {
            paddingTop: baseTop,
            paddingRight: baseRight,
            paddingBottom: baseBottom,
            paddingLeft: baseLeft,
          },
          effectiveIncludeFontPadding == null
            ? null
            : { includeFontPadding: effectiveIncludeFontPadding },
          styles.hiddenText,
        ]}
      >
        {resolvedText}
      </Text>

      <NativeStrokeTextView
        {...nativeProps}
        text={resolvedText}
        color={nativeProps.color ?? toColorString(styleColor)}
        fontSize={nativeProps.fontSize ?? toNumber(styleFontSize)}
        fontWeight={
          nativeProps.fontWeight ?? toFontWeightString(styleFontWeight)
        }
        fontFamily={nativeProps.fontFamily ?? styleFontFamily}
        fontStyle={nativeProps.fontStyle ?? styleFontStyle}
        lineHeight={nativeProps.lineHeight ?? toNumber(styleLineHeight)}
        letterSpacing={nativeProps.letterSpacing ?? toNumber(styleLetterSpacing)}
        textAlign={nativeProps.textAlign ?? styleTextAlign}
        textDecorationLine={
          nativeProps.textDecorationLine ?? styleTextDecorationLine
        }
        textTransform={nativeProps.textTransform ?? styleTextTransform}
        opacity={nativeProps.opacity ?? toNumber(styleOpacity)}
        includeFontPadding={effectiveIncludeFontPadding}
        numberOfLines={nativeProps.numberOfLines}
        paddingTop={baseTop}
        paddingRight={baseRight}
        paddingBottom={baseBottom}
        paddingLeft={baseLeft}
        hybridRef={hybridRef ? callback(hybridRef) : undefined}
        pointerEvents="none"
        style={[
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
      />
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
