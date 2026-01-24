package com.margelo.nitro.stroketext

import android.view.View
import com.facebook.react.uimanager.ThemedReactContext

class HybridStrokeTextView(context: ThemedReactContext) : HybridStrokeTextViewSpec() {
  private val strokeTextView = StrokeTextView(context)
  override val view: View = strokeTextView

  override var text: String = ""
  override var color: String? = null
  override var strokeColor: String? = null
  override var strokeWidth: Double? = null
  override var fontSize: Double? = null
  override var fontWeight: String? = null
  override var fontFamily: String? = null
  override var fontStyle: StrokeTextFontStyle? = null
  override var lineHeight: Double? = null
  override var letterSpacing: Double? = null
  override var textAlign: StrokeTextAlign? = null
  override var textDecorationLine: StrokeTextDecorationLine? = null
  override var textTransform: StrokeTextTransform? = null
  override var opacity: Double? = null
  override var numberOfLines: Double? = null
  override var ellipsis: Boolean? = null
  override var padding: Double? = null
  override var paddingVertical: Double? = null
  override var paddingHorizontal: Double? = null
  override var paddingTop: Double? = null
  override var paddingRight: Double? = null
  override var paddingBottom: Double? = null
  override var paddingLeft: Double? = null

  override fun afterUpdate() {
    strokeTextView.text = text

    strokeTextView.color = StrokeTextView.parseColor(color) ?: android.graphics.Color.BLACK
    strokeTextView.strokeColor = StrokeTextView.parseColor(strokeColor) ?: android.graphics.Color.TRANSPARENT
    strokeTextView.strokeWidthPx = StrokeTextView.spToPx(strokeWidth ?: 0.0, strokeTextView.resources.displayMetrics)

    strokeTextView.fontSizePx = StrokeTextView.spToPx(fontSize ?: 14.0, strokeTextView.resources.displayMetrics)
    strokeTextView.fontWeight = fontWeight ?: "400"
    strokeTextView.fontFamily = fontFamily
    strokeTextView.fontStyle = fontStyle ?: StrokeTextFontStyle.NORMAL
    strokeTextView.lineHeightPx = lineHeight?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.letterSpacingPx = letterSpacing?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }

    strokeTextView.textAlign = textAlign ?: StrokeTextAlign.AUTO
    strokeTextView.textDecorationLine = textDecorationLine ?: StrokeTextDecorationLine.NONE
    strokeTextView.textTransform = textTransform ?: StrokeTextTransform.NONE

    strokeTextView.numberOfLines = (numberOfLines ?: 0.0).toInt()
    strokeTextView.ellipsis = ellipsis ?: false

    strokeTextView.paddingAllPx = padding?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingVerticalPx = paddingVertical?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingHorizontalPx = paddingHorizontal?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingTopPx = paddingTop?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingRightPx = paddingRight?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingBottomPx = paddingBottom?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }
    strokeTextView.paddingLeftPx = paddingLeft?.let { StrokeTextView.spToPx(it, strokeTextView.resources.displayMetrics) }

    strokeTextView.alpha = (opacity ?: 1.0).toFloat()

    strokeTextView.invalidateTextLayout()
  }
}

