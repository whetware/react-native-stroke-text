package com.margelo.nitro.stroketext

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.LineHeightSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlags
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.views.text.DefaultStyleValuesUtil
import com.facebook.react.views.text.ReactTypefaceUtils
import java.lang.reflect.Modifier
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

internal class StrokeTextView(context: ThemedReactContext) : TextView(context) {
  var rawText: String = ""
  var color: Int = resolvedDefaultTextColor()
  var strokeColor: Int = Color.TRANSPARENT
  var strokeWidthDp: Double = 0.0
  var strokeWidthPx: Float = 0f

  var fontSizePx: Float = spToPx(14.0)
  var fontWeight: String? = null
  var fontFamily: String? = null
  var fontStyle: StrokeTextFontStyle = StrokeTextFontStyle.NORMAL
  var lineHeightPx: Float? = null
  var letterSpacingPx: Float? = null

  var textAlign: StrokeTextAlign = StrokeTextAlign.AUTO
  var textDecorationLine: StrokeTextDecorationLine = StrokeTextDecorationLine.NONE
  var textTransform: StrokeTextTransform = StrokeTextTransform.NONE

  var numberOfLines: Int = 0
  var ellipsizeMode: StrokeTextEllipsizeMode? = null

  var paddingAllPx: Float? = null
  var paddingVerticalPx: Float? = null
  var paddingHorizontalPx: Float? = null
  var paddingTopPx: Float? = null
  var paddingRightPx: Float? = null
  var paddingBottomPx: Float? = null
  var paddingLeftPx: Float? = null

  init {
    // Default to no font padding to avoid Android's extra ascent/descent insets shifting the
    // glyphs downward. (React Native <Text> defaults includeFontPadding=true, but most designs
    // expect iOS/web-like top alignment.)
    gravity = Gravity.TOP or Gravity.START
    includeFontPadding = false
  }

  fun invalidateTextLayout() {
    applyProps()
    requestLayout()
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    // Draw stroke behind fill, using TextView's layout so metrics match RN <Text/> as closely as
    // possible (especially for bold fonts).
    val layout = layout
    if (layout != null && strokeWidthPx > 0f && strokeColor != Color.TRANSPARENT) {
      val textPaint = paint
      val prevStyle = textPaint.style
      val prevStrokeWidth = textPaint.strokeWidth
      val prevStrokeJoin = textPaint.strokeJoin
      val prevStrokeCap = textPaint.strokeCap
      val prevColor = textPaint.color
      val prevUnderline = textPaint.isUnderlineText
      val prevStrike = textPaint.isStrikeThruText

      val saveCount = canvas.save()
      val compoundPaddingLeft = compoundPaddingLeft
      val extendedPaddingTop = extendedPaddingTop

      canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
      canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())

      // Only stroke the glyph outlines; keep underline/strike in the fill pass.
      textPaint.isUnderlineText = false
      textPaint.isStrikeThruText = false

      textPaint.style = Paint.Style.STROKE
      textPaint.strokeJoin = Paint.Join.ROUND
      textPaint.strokeCap = Paint.Cap.ROUND
      textPaint.strokeWidth = strokeWidthPx
      textPaint.color = strokeColor
      layout.draw(canvas)

      canvas.restoreToCount(saveCount)

      textPaint.style = prevStyle
      textPaint.strokeWidth = prevStrokeWidth
      textPaint.strokeJoin = prevStrokeJoin
      textPaint.strokeCap = prevStrokeCap
      textPaint.color = prevColor
      textPaint.isUnderlineText = prevUnderline
      textPaint.isStrikeThruText = prevStrike
    }

    super.onDraw(canvas)
  }

  private fun applyProps() {
    // Padding: apply stroke inset in native to compensate for the overlay expansion in JS.
    val inset = strokeInsetPx()
    val left = floorToInt(resolvePadding(paddingLeftPx, paddingHorizontalPx, paddingAllPx) + inset)
    val top = floorToInt(resolvePadding(paddingTopPx, paddingVerticalPx, paddingAllPx) + inset)
    val right = floorToInt(resolvePadding(paddingRightPx, paddingHorizontalPx, paddingAllPx) + inset)
    val bottom = floorToInt(resolvePadding(paddingBottomPx, paddingVerticalPx, paddingAllPx) + inset)
    setPadding(left, top, right, bottom)

    // Font
    setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
    typeface = resolveTypeface(fontFamily, fontWeight, fontStyle)

    // Mirror RN's CustomStyleSpan flags.
    applyCustomStyleTextFlags(paint, fontFamily, fontWeight, fontStyle)

    // Letter spacing is specified in px/pt; TextView expects em.
    val letterSpacingEm =
      if (letterSpacingPx != null && !letterSpacingPx!!.isNaN() && fontSizePx > 0f) {
        letterSpacingPx!! / fontSizePx
      } else {
        0f
      }
    letterSpacing = letterSpacingEm

    // Text decorations
    val underline =
      textDecorationLine == StrokeTextDecorationLine.UNDERLINE ||
        textDecorationLine == StrokeTextDecorationLine.UNDERLINE_LINE_THROUGH
    val strike =
      textDecorationLine == StrokeTextDecorationLine.LINE_THROUGH ||
        textDecorationLine == StrokeTextDecorationLine.UNDERLINE_LINE_THROUGH
    paint.isUnderlineText = underline
    paint.isStrikeThruText = strike

    // Alignment
    val horizontalGravity =
      when (textAlign) {
        StrokeTextAlign.RIGHT -> Gravity.END
        StrokeTextAlign.CENTER -> Gravity.CENTER_HORIZONTAL
        else -> Gravity.START
      }
    gravity = Gravity.TOP or horizontalGravity

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      justificationMode =
        if (textAlign == StrokeTextAlign.JUSTIFY) Layout.JUSTIFICATION_MODE_INTER_WORD
        else Layout.JUSTIFICATION_MODE_NONE
    }

    // Line limits / ellipsizing
    val maxLines = if (numberOfLines > 0) numberOfLines else Int.MAX_VALUE
    setMaxLines(maxLines)
    ellipsize =
      if (numberOfLines <= 0) {
        null
      } else {
        when (ellipsizeMode ?: StrokeTextEllipsizeMode.TAIL) {
          StrokeTextEllipsizeMode.HEAD -> TextUtils.TruncateAt.START
          StrokeTextEllipsizeMode.MIDDLE -> TextUtils.TruncateAt.MIDDLE
          StrokeTextEllipsizeMode.CLIP -> null
          StrokeTextEllipsizeMode.TAIL -> TextUtils.TruncateAt.END
        }
      }

    // Colors
    setTextColor(color)

    // Text + transform + line height (set last so layout is created with the final paint settings).
    val transformedText = applyTextTransform(rawText, textTransform)
    val textForLayout: CharSequence =
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
    setText(textForLayout)
  }

  private fun resolvePadding(specific: Float?, axis: Float?, all: Float?): Float {
    return specific ?: axis ?: all ?: 0f
  }

  private fun strokeInsetPx(): Float {
    if (strokeWidthDp <= 0.0) return 0f
    val insetDp = (ceil(strokeWidthDp) / 2.0).toFloat()
    return dpToPx(insetDp.toDouble())
  }

  private fun floorToInt(value: Float): Int {
    // React Native consistently floors padding values when applying them to native views.
    return kotlin.math.floor(value.toDouble()).toInt()
  }

  private fun resolveTypeface(
    family: String?,
    weight: String?,
    style: StrokeTextFontStyle,
  ): Typeface {
    val fam = family?.takeIf { it.isNotBlank() }
    val weightInt = ReactTypefaceUtils.parseFontWeight(weight)
    val styleInt = if (style == StrokeTextFontStyle.ITALIC) Typeface.ITALIC else Typeface.NORMAL
    return ReactTypefaceUtils.applyStyles(null, styleInt, weightInt, fam, context.assets)
  }

  private fun applyCustomStyleTextFlags(
    paint: Paint,
    family: String?,
    weight: String?,
    style: StrokeTextFontStyle,
  ) {
    val isCustomFamily = !family.isNullOrBlank()
    val isCustomItalic = style == StrokeTextFontStyle.ITALIC
    val isCustomWeight =
      ReactTypefaceUtils.parseFontWeight(weight) != com.facebook.react.common.ReactConstants.UNSET
    val hasCustomStyle = isCustomFamily || isCustomItalic || isCustomWeight

    // Mirror React Native's CustomStyleSpan defaults.
    paint.isSubpixelText = hasCustomStyle
    paint.isLinearText = hasCustomStyle && isAndroidLinearTextEnabled()
  }

  private fun isAndroidLinearTextEnabled(): Boolean {
    return try {
      val method =
        ReactNativeFeatureFlags::class.java.methods.firstOrNull {
          it.name == "enableAndroidLinearText" && it.parameterCount == 0
        } ?: return false
      val receiver = if (Modifier.isStatic(method.modifiers)) null else ReactNativeFeatureFlags
      (method.invoke(receiver) as? Boolean) == true
    } catch (_: Throwable) {
      false
    }
  }

  fun resolvedDefaultTextColor(): Int {
    return DefaultStyleValuesUtil.getDefaultTextColor(context)?.defaultColor ?: Color.BLACK
  }

  private fun applyTextTransform(text: String, transform: StrokeTextTransform): String {
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
      fm.descent += kotlin.math.floor(leading / 2.0f).toInt()

      if (start == 0) {
        fm.top = fm.ascent
      }
      if (end == text.length) {
        fm.bottom = fm.descent
      }
    }
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
        if (
          maxFontSizeMultiplier == null ||
          maxFontSizeMultiplier.isNaN() ||
          maxFontSizeMultiplier <= 0f ||
          maxFontSizeMultiplier < 1f
        ) {
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
