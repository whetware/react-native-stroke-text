import NitroModules
import UIKit

final class HybridStrokeTextView: HybridStrokeTextViewSpec {
  typealias ViewType = StrokeTextView

  let view = StrokeTextView()

  var text: String = ""
  var color: String? = nil
  var strokeColor: String? = nil
  var strokeWidth: Double? = nil
  var fontSize: Double? = nil
  var fontWeight: String? = nil
  var fontFamily: String? = nil
  var fontStyle: StrokeTextFontStyle? = nil
  var lineHeight: Double? = nil
  var letterSpacing: Double? = nil
  var textAlign: StrokeTextAlign? = nil
  var textDecorationLine: StrokeTextDecorationLine? = nil
  var textTransform: StrokeTextTransform? = nil
  var opacity: Double? = nil
  var allowFontScaling: Bool? = nil
  var maxFontSizeMultiplier: Double? = nil
  var includeFontPadding: Bool? = nil
  var numberOfLines: Double? = nil
  var ellipsis: Bool? = nil
  var padding: Double? = nil
  var paddingVertical: Double? = nil
  var paddingHorizontal: Double? = nil
  var paddingTop: Double? = nil
  var paddingRight: Double? = nil
  var paddingBottom: Double? = nil
  var paddingLeft: Double? = nil

  func afterUpdate() {
    view.text = text

    if let color = color, let parsed = StrokeTextColor.parse(color) {
      view.color = parsed
    } else {
      view.color = StrokeTextView.defaultTextColor()
    }

    if let strokeColor = strokeColor, let parsed = StrokeTextColor.parse(strokeColor) {
      view.strokeColor = parsed
    } else {
      view.strokeColor = .clear
    }

    let resolvedStrokeWidth = max(0, strokeWidth ?? 0)
    view.strokeWidth = CGFloat(resolvedStrokeWidth)

    view.fontSize = CGFloat(fontSize ?? 14)
    view.fontWeight = fontWeight ?? "400"
    view.fontFamily = fontFamily
    view.fontStyle = fontStyle ?? .normal

    view.allowFontScaling = allowFontScaling ?? true
    if let multiplier = maxFontSizeMultiplier, multiplier.isFinite, multiplier >= 1 {
      view.maxFontSizeMultiplier = CGFloat(multiplier)
    } else {
      view.maxFontSizeMultiplier = nil
    }

    view.lineHeight = lineHeight.map { CGFloat($0) }
    view.letterSpacing = letterSpacing.map { CGFloat($0) }
    view.textAlign = textAlign ?? .auto
    view.textDecorationLine = textDecorationLine ?? .none
    view.textTransform = textTransform ?? .none

    view.numberOfLines = Int(numberOfLines ?? 0)
    view.ellipsis = ellipsis ?? false

    view.alpha = CGFloat(opacity ?? 1)

    let baseInsets = resolvedPaddingInsets(
      padding: padding,
      paddingVertical: paddingVertical,
      paddingHorizontal: paddingHorizontal,
      paddingTop: paddingTop,
      paddingRight: paddingRight,
      paddingBottom: paddingBottom,
      paddingLeft: paddingLeft
    )
    let strokeInset = CGFloat(ceil(resolvedStrokeWidth) / 2.0)
    view.paddingInsets = UIEdgeInsets(
      top: baseInsets.top + strokeInset,
      left: baseInsets.left + strokeInset,
      bottom: baseInsets.bottom + strokeInset,
      right: baseInsets.right + strokeInset
    )
  }

  private func resolvedPaddingInsets(
    padding: Double?,
    paddingVertical: Double?,
    paddingHorizontal: Double?,
    paddingTop: Double?,
    paddingRight: Double?,
    paddingBottom: Double?,
    paddingLeft: Double?
  ) -> UIEdgeInsets {
    func resolve(_ specific: Double?, _ axis: Double?, _ all: Double?) -> CGFloat {
      return CGFloat(specific ?? axis ?? all ?? 0)
    }

    let top = resolve(paddingTop, paddingVertical, padding)
    let bottom = resolve(paddingBottom, paddingVertical, padding)
    let left = resolve(paddingLeft, paddingHorizontal, padding)
    let right = resolve(paddingRight, paddingHorizontal, padding)
    return UIEdgeInsets(top: top, left: left, bottom: bottom, right: right)
  }
}
