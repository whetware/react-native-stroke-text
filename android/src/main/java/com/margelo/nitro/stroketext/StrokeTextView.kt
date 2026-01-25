package com.margelo.nitro.stroketext

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Paint.FontMetricsInt
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.LineHeightSpan
import android.util.TypedValue
import android.view.View
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.views.text.DefaultStyleValuesUtil
import com.facebook.react.views.text.ReactTypefaceUtils
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.ceil

internal class StrokeTextView(context: ThemedReactContext) : View(context) {
  var text: String = ""
  var color: Int = resolvedDefaultTextColor()
  var strokeColor: Int = Color.TRANSPARENT
  var strokeWidthDp: Double = 0.0
  var strokeWidthPx: Float = 0f

  var fontSizePx: Float = spToPx(14.0)
  var fontWeight: String = "400"
  var fontFamily: String? = null
  var fontStyle: StrokeTextFontStyle = StrokeTextFontStyle.NORMAL
  var lineHeightPx: Float? = null
  var letterSpacingPx: Float? = null

  var textAlign: StrokeTextAlign = StrokeTextAlign.AUTO
  var textDecorationLine: StrokeTextDecorationLine = StrokeTextDecorationLine.NONE
  var textTransform: StrokeTextTransform = StrokeTextTransform.NONE

  var numberOfLines: Int = 0
  var ellipsis: Boolean = false
  var includeFontPadding: Boolean = true

  var paddingAllPx: Float? = null
  var paddingVerticalPx: Float? = null
  var paddingHorizontalPx: Float? = null
  var paddingTopPx: Float? = null
  var paddingRightPx: Float? = null
  var paddingBottomPx: Float? = null
  var paddingLeftPx: Float? = null

  private val fillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
  private val strokePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

  private var fillLayout: StaticLayout? = null
  private var strokeLayout: StaticLayout? = null
  private var layoutDirty: Boolean = true

  fun invalidateTextLayout() {
    layoutDirty = true
    requestLayout()
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    ensureLayout()

    val sLayout = strokeLayout ?: return
    val fLayout = fillLayout ?: return

    canvas.save()
    canvas.translate(effectivePaddingLeft(), effectivePaddingTop())
    sLayout.draw(canvas)
    fLayout.draw(canvas)
    canvas.restore()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    if (w != oldw) {
      layoutDirty = true
    }
  }

  private fun ensureLayout() {
    if (!layoutDirty) return

    val availableWidth =
      max(0, width - effectivePaddingLeft().toInt() - effectivePaddingRight().toInt())
    if (availableWidth == 0) {
      fillLayout = null
      strokeLayout = null
      return
    }

    val transformedText = applyTextTransform(text, textTransform)
    val textForLayout =
      if (lineHeightPx != null && !lineHeightPx!!.isNaN() && transformedText.isNotEmpty()) {
        SpannableString(transformedText).apply {
          setSpan(
            StrokeTextLineHeightSpan(lineHeightPx!!),
            0,
            length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
          )
        }
      } else {
        transformedText
      }

    val tf = resolveTypeface(fontFamily, fontWeight, fontStyle)

    fillPaint.reset()
    fillPaint.isAntiAlias = true
    fillPaint.typeface = tf
    fillPaint.textSize = fontSizePx
    fillPaint.color = color

    strokePaint.reset()
    strokePaint.isAntiAlias = true
    strokePaint.typeface = tf
    strokePaint.textSize = fontSizePx
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeJoin = Paint.Join.ROUND
    strokePaint.strokeCap = Paint.Cap.ROUND
    strokePaint.strokeWidth = strokeWidthPx
    strokePaint.color = strokeColor

    val underline = textDecorationLine == StrokeTextDecorationLine.UNDERLINE ||
      textDecorationLine == StrokeTextDecorationLine.UNDERLINE_LINE_THROUGH
    val strike = textDecorationLine == StrokeTextDecorationLine.LINE_THROUGH ||
      textDecorationLine == StrokeTextDecorationLine.UNDERLINE_LINE_THROUGH

    fillPaint.isUnderlineText = underline
    fillPaint.isStrikeThruText = strike
    strokePaint.isUnderlineText = underline
    strokePaint.isStrikeThruText = strike

    val letterSpacingEm = if (letterSpacingPx != null && fontSizePx > 0f) {
      letterSpacingPx!! / fontSizePx
    } else {
      0f
    }
    fillPaint.letterSpacing = letterSpacingEm
    strokePaint.letterSpacing = letterSpacingEm

    val alignment = toAlignment(textAlign)

    val maxLines = numberOfLines.takeIf { it > 0 }

    val fillBuilder =
      StaticLayout.Builder.obtain(textForLayout, 0, textForLayout.length, fillPaint, availableWidth)
      .setAlignment(alignment)
      .setIncludePad(includeFontPadding)
      .setLineSpacing(0f, 1f)
      .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
      .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      fillBuilder.setUseLineSpacingFromFallbacks(true)
    }
    if (textAlign == StrokeTextAlign.JUSTIFY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      fillBuilder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
    }
    if (maxLines != null) {
      fillBuilder.setMaxLines(maxLines)
      if (ellipsis) {
        fillBuilder.setEllipsize(TextUtils.TruncateAt.END)
        fillBuilder.setEllipsizedWidth(availableWidth)
      }
    }
    fillLayout = fillBuilder.build()

    val strokeBuilder =
      StaticLayout.Builder.obtain(textForLayout, 0, textForLayout.length, strokePaint, availableWidth)
      .setAlignment(alignment)
      .setIncludePad(includeFontPadding)
      .setLineSpacing(0f, 1f)
      .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
      .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      strokeBuilder.setUseLineSpacingFromFallbacks(true)
    }
    if (textAlign == StrokeTextAlign.JUSTIFY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      strokeBuilder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
    }
    if (maxLines != null) {
      strokeBuilder.setMaxLines(maxLines)
      if (ellipsis) {
        strokeBuilder.setEllipsize(TextUtils.TruncateAt.END)
        strokeBuilder.setEllipsizedWidth(availableWidth)
      }
    }
    strokeLayout = strokeBuilder.build()

    layoutDirty = false
  }

  private class StrokeTextLineHeightSpan(heightPx: Float) : LineHeightSpan {
    private val lineHeight: Int = ceil(heightPx.toDouble()).toInt()

    override fun chooseHeight(
      text: CharSequence,
      start: Int,
      end: Int,
      spanstartv: Int,
      v: Int,
      fm: FontMetricsInt,
    ) {
      val leading = lineHeight - ((-fm.ascent) + fm.descent)
      fm.ascent -= ceil(leading / 2.0f).toInt()
      fm.descent += floor(leading / 2.0f).toInt()

      if (start == 0) {
        fm.top = fm.ascent
      }
      if (end == text.length) {
        fm.bottom = fm.descent
      }
    }
  }

  private fun toAlignment(align: StrokeTextAlign): Layout.Alignment {
    return when (align) {
      StrokeTextAlign.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
      StrokeTextAlign.CENTER -> Layout.Alignment.ALIGN_CENTER
      StrokeTextAlign.LEFT, StrokeTextAlign.AUTO, StrokeTextAlign.JUSTIFY -> Layout.Alignment.ALIGN_NORMAL
    }
  }

  private fun effectivePaddingTop(): Float =
    resolvePadding(paddingTopPx, paddingVerticalPx, paddingAllPx) + strokeInsetPx()
  private fun effectivePaddingBottom(): Float =
    resolvePadding(paddingBottomPx, paddingVerticalPx, paddingAllPx) + strokeInsetPx()
  private fun effectivePaddingLeft(): Float =
    resolvePadding(paddingLeftPx, paddingHorizontalPx, paddingAllPx) + strokeInsetPx()
  private fun effectivePaddingRight(): Float =
    resolvePadding(paddingRightPx, paddingHorizontalPx, paddingAllPx) + strokeInsetPx()

  private fun resolvePadding(specific: Float?, axis: Float?, all: Float?): Float {
    return specific ?: axis ?: all ?: 0f
  }

  private fun strokeInsetPx(): Float {
    if (strokeWidthDp <= 0.0) return 0f
    val insetDp = (ceil(strokeWidthDp) / 2.0).toFloat()
    return dpToPx(insetDp.toDouble())
  }

  private fun resolveTypeface(
    family: String?,
    weight: String,
    style: StrokeTextFontStyle,
  ): Typeface {
    val fam = family?.takeIf { it.isNotBlank() }
    val weightInt = ReactTypefaceUtils.parseFontWeight(weight)
    val styleInt = if (style == StrokeTextFontStyle.ITALIC) Typeface.ITALIC else Typeface.NORMAL
    return ReactTypefaceUtils.applyStyles(null, styleInt, weightInt, fam, context.assets)
  }

  fun resolvedDefaultTextColor(): Int {
    return DefaultStyleValuesUtil.getDefaultTextColor(context)?.defaultColor ?: Color.BLACK
  }

  private fun applyTextTransform(text: String, transform: StrokeTextTransform): CharSequence {
    return when (transform) {
      StrokeTextTransform.UPPERCASE -> text.uppercase()
      StrokeTextTransform.LOWERCASE -> text.lowercase()
      StrokeTextTransform.CAPITALIZE -> capitalizeWords(text)
      StrokeTextTransform.NONE -> text
    }
  }

  private fun capitalizeWords(input: String): String {
    val sb = StringBuilder(input.length)
    var cap = true
    for (c in input) {
      if (c.isWhitespace()) {
        cap = true
        sb.append(c)
      } else {
        sb.append(if (cap) c.titlecaseChar() else c)
        cap = false
      }
    }
    return sb.toString()
  }

  companion object {
    fun parseColor(color: String?): Int? {
      val trimmed = color?.trim().orEmpty()
      if (trimmed.isEmpty()) return null

      val lower = trimmed.lowercase()
      if (lower.startsWith("#")) return parseHexColor(trimmed)
      if (lower.startsWith("rgba(")) return parseRgba(trimmed)
      if (lower.startsWith("rgb(")) return parseRgb(trimmed)

      return runCatching { Color.parseColor(trimmed) }.getOrNull()
    }

    private fun parseHexColor(color: String): Int? {
      var hex = color.removePrefix("#").trim()
      if (hex.isEmpty()) return null

      if (hex.length == 3) {
        // #RGB
        hex = "${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
      } else if (hex.length == 4) {
        // #RGBA (CSS Color Module Level 4 / React Native)
        hex = "${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}"
      }

      return when (hex.length) {
        6 -> {
          val rgb = hex.toLongOrNull(16) ?: return null
          val r = ((rgb shr 16) and 0xFF).toInt()
          val g = ((rgb shr 8) and 0xFF).toInt()
          val b = (rgb and 0xFF).toInt()
          Color.argb(255, r, g, b)
        }
        8 -> {
          // #RRGGBBAA (CSS Color Module Level 4 / React Native)
          val r = hex.substring(0, 2).toIntOrNull(16) ?: return null
          val g = hex.substring(2, 4).toIntOrNull(16) ?: return null
          val b = hex.substring(4, 6).toIntOrNull(16) ?: return null
          val a = hex.substring(6, 8).toIntOrNull(16) ?: return null
          Color.argb(a, r, g, b)
        }
        else -> null
      }
    }

    private fun parseRgb(color: String): Int? {
      val inner = color.removePrefix("rgb(").removeSuffix(")")
      val parts = inner.split(",").map { it.trim() }
      if (parts.size != 3) return null
      val r = parts[0].toIntOrNull()?.coerceIn(0, 255) ?: return null
      val g = parts[1].toIntOrNull()?.coerceIn(0, 255) ?: return null
      val b = parts[2].toIntOrNull()?.coerceIn(0, 255) ?: return null
      return Color.rgb(r, g, b)
    }

    private fun parseRgba(color: String): Int? {
      val inner = color.removePrefix("rgba(").removeSuffix(")")
      val parts = inner.split(",").map { it.trim() }
      if (parts.size != 4) return null
      val r = parts[0].toIntOrNull()?.coerceIn(0, 255) ?: return null
      val g = parts[1].toIntOrNull()?.coerceIn(0, 255) ?: return null
      val b = parts[2].toIntOrNull()?.coerceIn(0, 255) ?: return null
      val aFloat = parts[3].toFloatOrNull() ?: return null
      val a = min(255, max(0, (aFloat * 255f).toInt()))
      return Color.argb(a, r, g, b)
    }

    fun dpToPx(dp: Float, displayMetrics: android.util.DisplayMetrics): Float {
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
    }

    fun spToPx(sp: Double, displayMetrics: android.util.DisplayMetrics): Float {
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), displayMetrics)
    }

    fun spToPx(
      sp: Double,
      displayMetrics: android.util.DisplayMetrics,
      maxFontSizeMultiplier: Float?,
    ): Float {
      val density = displayMetrics.density
      if (density == 0f) return spToPx(sp, displayMetrics)

      val fontScale = displayMetrics.scaledDensity / density
      val effectiveFontScale =
        if (maxFontSizeMultiplier == null || maxFontSizeMultiplier.isNaN() || maxFontSizeMultiplier <= 0f || maxFontSizeMultiplier < 1f) {
          fontScale
        } else {
          min(fontScale, maxFontSizeMultiplier)
        }

      return (sp.toFloat() * density * effectiveFontScale)
    }

    fun textToPx(
      value: Double,
      allowFontScaling: Boolean,
      maxFontSizeMultiplier: Float?,
      displayMetrics: android.util.DisplayMetrics,
    ): Float {
      return if (allowFontScaling) {
        spToPx(value, displayMetrics, maxFontSizeMultiplier)
      } else {
        dpToPx(value.toFloat(), displayMetrics)
      }
    }
  }

  private fun spToPx(sp: Double): Float {
    return spToPx(sp, resources.displayMetrics)
  }

  private fun dpToPx(dp: Double): Float {
    return dpToPx(dp.toFloat(), resources.displayMetrics)
  }
}
