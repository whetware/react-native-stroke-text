package com.margelo.nitro.stroketext

import android.view.View
import com.facebook.jni.HybridData
import com.facebook.react.uimanager.ThemedReactContext
import com.margelo.nitro.views.RecyclableView

class HybridStrokeTextView(context: ThemedReactContext) : HybridStrokeTextViewSpec(), RecyclableView {
  private val strokeTextView = StrokeTextView(context)
  override val view: View = strokeTextView

  private var isDisposed = false

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
  override var allowFontScaling: Boolean? = null
  override var maxFontSizeMultiplier: Double? = null
  override var includeFontPadding: Boolean? = null
  override var numberOfLines: Double? = null
  override var ellipsizeMode: StrokeTextEllipsizeMode? = null
  override var padding: Double? = null
  override var paddingVertical: Double? = null
  override var paddingHorizontal: Double? = null
  override var paddingTop: Double? = null
  override var paddingRight: Double? = null
  override var paddingBottom: Double? = null
  override var paddingLeft: Double? = null

  override fun dispose() {
    if (isDisposed) return
    isDisposed = true
    super.dispose()

    try {
      val hybridDataField = HybridStrokeTextViewSpec::class.java.getDeclaredField("mHybridData")
      hybridDataField.isAccessible = true
      val hybridData = hybridDataField.get(this) as? HybridData ?: return

      val resetNativeMethod = HybridData::class.java.getDeclaredMethod("resetNative")
      resetNativeMethod.isAccessible = true
      resetNativeMethod.invoke(hybridData)
    } catch (_: Throwable) {
    }
  }

  override fun prepareForRecycle() {
    text = ""
    color = null
    strokeColor = null
    strokeWidth = null
    fontSize = null
    fontWeight = null
    fontFamily = null
    fontStyle = null
    lineHeight = null
    letterSpacing = null
    textAlign = null
    textDecorationLine = null
    textTransform = null
    opacity = null
    allowFontScaling = null
    maxFontSizeMultiplier = null
    includeFontPadding = null
    numberOfLines = null
    ellipsizeMode = null
    padding = null
    paddingVertical = null
    paddingHorizontal = null
    paddingTop = null
    paddingRight = null
    paddingBottom = null
    paddingLeft = null

    afterUpdate()
  }

  override fun afterUpdate() {
    val displayMetrics = strokeTextView.resources.displayMetrics
    val resolvedAllowFontScaling = allowFontScaling ?: true
    val resolvedMaxFontSizeMultiplier =
        maxFontSizeMultiplier
            ?.toFloat()
            ?.takeIf { it.isFinite() && it >= 1f }

    strokeTextView.rawText = text

    strokeTextView.color =
        StrokeTextView.parseColor(color) ?: strokeTextView.resolvedDefaultTextColor()
    strokeTextView.strokeColor = StrokeTextView.parseColor(strokeColor) ?: android.graphics.Color.TRANSPARENT
    val resolvedStrokeWidth = (strokeWidth ?: 0.0).coerceAtLeast(0.0)
    strokeTextView.strokeWidthDp = resolvedStrokeWidth
    strokeTextView.strokeWidthPx =
        StrokeTextView.dpToPx(
            resolvedStrokeWidth.toFloat(),
            displayMetrics,
        )

    strokeTextView.fontSizePx =
        StrokeTextView.textToPx(
            fontSize ?: 14.0,
            resolvedAllowFontScaling,
            resolvedMaxFontSizeMultiplier,
            displayMetrics,
        )
    strokeTextView.fontWeight = fontWeight
    strokeTextView.fontFamily = fontFamily
    strokeTextView.fontStyle = fontStyle ?: StrokeTextFontStyle.NORMAL
    strokeTextView.lineHeightPx =
        lineHeight?.let {
          StrokeTextView.textToPx(
              it,
              resolvedAllowFontScaling,
              resolvedMaxFontSizeMultiplier,
              displayMetrics,
          )
        }
    strokeTextView.letterSpacingPx =
        letterSpacing?.let {
          StrokeTextView.textToPx(
              it,
              resolvedAllowFontScaling,
              resolvedMaxFontSizeMultiplier,
              displayMetrics,
          )
        }

    strokeTextView.textAlign = textAlign ?: StrokeTextAlign.AUTO
    strokeTextView.textDecorationLine = textDecorationLine ?: StrokeTextDecorationLine.NONE
    strokeTextView.textTransform = textTransform ?: StrokeTextTransform.NONE

    strokeTextView.includeFontPadding = includeFontPadding ?: false
    strokeTextView.numberOfLines = (numberOfLines ?: 0.0).toInt()
    strokeTextView.ellipsizeMode = ellipsizeMode

    strokeTextView.paddingAllPx = padding?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingVerticalPx = paddingVertical?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingHorizontalPx = paddingHorizontal?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingTopPx = paddingTop?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingRightPx = paddingRight?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingBottomPx = paddingBottom?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }
    strokeTextView.paddingLeftPx = paddingLeft?.let { StrokeTextView.dpToPx(it.toFloat(), displayMetrics) }

    strokeTextView.alpha = (opacity ?: 1.0).toFloat()

    strokeTextView.invalidateTextLayout()
  }
}
