import React from 'react'
import { StyleSheet, Text, type TextStyle } from 'react-native'

import type { StrokeTextProps } from './types'

function resolveText(text: unknown, children: unknown): string {
  if (typeof text === 'string') return text
  if (typeof children === 'string') return children
  return ''
}

function buildTextShadow(width: number, color: string): string {
  const w = Math.max(0, width)
  if (w === 0) return ''
  const o = w
  const shadows = [
    `${-o}px 0 0 ${color}`,
    `${o}px 0 0 ${color}`,
    `0 ${-o}px 0 ${color}`,
    `0 ${o}px 0 ${color}`,
    `${-o}px ${-o}px 0 ${color}`,
    `${o}px ${-o}px 0 ${color}`,
    `${-o}px ${o}px 0 ${color}`,
    `${o}px ${o}px 0 ${color}`,
  ]
  return shadows.join(', ')
}

export function StrokeText({ text, children, style, ...rest }: StrokeTextProps) {
  const resolvedText = resolveText(text, children)
  const flattened = StyleSheet.flatten(style) as TextStyle | undefined

  const fillColor = rest.color ?? flattened?.color ?? '#000'
  const strokeColor = rest.strokeColor ?? '#000'
  const strokeWidth = rest.strokeWidth ?? 0
  const strokeInset = Math.ceil(strokeWidth)

  const baseAll = rest.padding ?? (flattened as any)?.padding ?? 0
  const baseVertical = rest.paddingVertical ?? (flattened as any)?.paddingVertical
  const baseHorizontal =
    rest.paddingHorizontal ?? (flattened as any)?.paddingHorizontal
  const baseTop = rest.paddingTop ?? (flattened as any)?.paddingTop
  const baseRight = rest.paddingRight ?? (flattened as any)?.paddingRight
  const baseBottom = rest.paddingBottom ?? (flattened as any)?.paddingBottom
  const baseLeft = rest.paddingLeft ?? (flattened as any)?.paddingLeft

  const paddingTop = (baseTop ?? baseVertical ?? baseAll) + strokeInset
  const paddingRight = (baseRight ?? baseHorizontal ?? baseAll) + strokeInset
  const paddingBottom = (baseBottom ?? baseVertical ?? baseAll) + strokeInset
  const paddingLeft = (baseLeft ?? baseHorizontal ?? baseAll) + strokeInset

  const webStyle: any = {
    ...(flattened as any),
    color: fillColor,
    paddingTop,
    paddingRight,
    paddingBottom,
    paddingLeft,
  }

  if (strokeWidth > 0) {
    webStyle.WebkitTextStrokeWidth = `${strokeWidth}px`
    webStyle.WebkitTextStrokeColor = strokeColor
    webStyle.textShadow = buildTextShadow(strokeWidth, strokeColor)
  }

  const effectiveNumberOfLines =
    rest.numberOfLines != null && rest.numberOfLines > 0
      ? rest.numberOfLines
      : undefined

  return (
    <Text
      numberOfLines={effectiveNumberOfLines}
      ellipsizeMode={rest.ellipsis ? 'tail' : undefined}
      style={webStyle}
    >
      {resolvedText}
    </Text>
  )
}
