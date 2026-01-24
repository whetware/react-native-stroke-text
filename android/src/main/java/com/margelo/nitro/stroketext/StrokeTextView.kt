package com.margelo.nitro.stroketext

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.views.text.ReactFontManager
import kotlin.math.max
import kotlin.math.min
import kotlin.math.ceil

internal class StrokeTextView(context: ThemedReactContext) : View(context) {
  var text: String = ""
  var color: Int = Color.BLACK
  var strokeColor: Int = Color.TRANSPARENT
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

    val maxLines = if (numberOfLines > 0) numberOfLines else Int.MAX_VALUE

    val spacingAdd = lineHeightPx?.let { lh ->
      max(0f, lh - fillPaint.fontSpacing)
    } ?: 0f

    val fillBuilder =
      StaticLayout.Builder.obtain(transformedText, 0, transformedText.length, fillPaint, availableWidth)
      .setAlignment(alignment)
      .setIncludePad(false)
      .setLineSpacing(spacingAdd, 1f)
      .setMaxLines(maxLines)
    if (ellipsis) {
      fillBuilder.setEllipsize(TextUtils.TruncateAt.END)
      fillBuilder.setEllipsizedWidth(availableWidth)
    }
    fillLayout = fillBuilder.build()

    val strokeBuilder =
      StaticLayout.Builder.obtain(transformedText, 0, transformedText.length, strokePaint, availableWidth)
      .setAlignment(alignment)
      .setIncludePad(false)
      .setLineSpacing(spacingAdd, 1f)
      .setMaxLines(maxLines)
    if (ellipsis) {
      strokeBuilder.setEllipsize(TextUtils.TruncateAt.END)
      strokeBuilder.setEllipsizedWidth(availableWidth)
    }
    strokeLayout = strokeBuilder.build()

    layoutDirty = false
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
    if (strokeWidthPx <= 0f) return 0f
    return ceil(strokeWidthPx.toDouble()).toFloat()
  }

  private fun resolveTypeface(
    family: String?,
    weight: String,
    style: StrokeTextFontStyle,
  ): Typeface {
    val bold = isBold(weight)
    val italic = style == StrokeTextFontStyle.ITALIC

    val typefaceStyle = when {
      bold && italic -> Typeface.BOLD_ITALIC
      bold -> Typeface.BOLD
      italic -> Typeface.ITALIC
      else -> Typeface.NORMAL
    }

    val fam = family?.takeIf { it.isNotBlank() }
    if (fam != null) {
      return ReactFontManager.getInstance().getTypeface(fam, typefaceStyle, context.assets)
    }

    return Typeface.defaultFromStyle(typefaceStyle)
  }

  private fun isBold(weight: String): Boolean {
    val trimmed = weight.trim()
    if (trimmed.equals("bold", ignoreCase = true)) return true
    return trimmed.toIntOrNull()?.let { it >= 600 } ?: false
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

      if (trimmed.startsWith("#")) {
        return parseHex(trimmed)
      }

      val lower = trimmed.lowercase()
      if (lower.startsWith("rgba(")) return parseRgba(trimmed)
      if (lower.startsWith("rgb(")) return parseRgb(trimmed)

      return null
    }

    private fun parseHex(color: String): Int? {
      var hex = color.drop(1)

      if (hex.length == 3) {
        hex = hex.map { "$it$it" }.joinToString("")
      }

      if (hex.length == 6) {
        val rgb = hex.toLongOrNull(16) ?: return null
        return Color.rgb(
          ((rgb shr 16) and 0xFF).toInt(),
          ((rgb shr 8) and 0xFF).toInt(),
          (rgb and 0xFF).toInt()
        )
      }

      if (hex.length == 8) {
        val rgba = hex.toLongOrNull(16) ?: return null
        val r = ((rgba shr 24) and 0xFF).toInt()
        val g = ((rgba shr 16) and 0xFF).toInt()
        val b = ((rgba shr 8) and 0xFF).toInt()
        val a = (rgba and 0xFF).toInt()
        return Color.argb(a, r, g, b)
      }

      return null
    }

    private fun parseRgb(color: String): Int? {
      val inner = color.removePrefix("rgb(").removeSuffix(")")
      val parts = inner.split(",").map { it.trim() }
      if (parts.size != 3) return null
      val r = parts[0].toIntOrNull() ?: return null
      val g = parts[1].toIntOrNull() ?: return null
      val b = parts[2].toIntOrNull() ?: return null
      return Color.rgb(r, g, b)
    }

    private fun parseRgba(color: String): Int? {
      val inner = color.removePrefix("rgba(").removeSuffix(")")
      val parts = inner.split(",").map { it.trim() }
      if (parts.size != 4) return null
      val r = parts[0].toIntOrNull() ?: return null
      val g = parts[1].toIntOrNull() ?: return null
      val b = parts[2].toIntOrNull() ?: return null
      val aFloat = parts[3].toFloatOrNull() ?: return null
      val a = min(255, max(0, (aFloat * 255f).toInt()))
      return Color.argb(a, r, g, b)
    }

    fun spToPx(sp: Double, displayMetrics: android.util.DisplayMetrics): Float {
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), displayMetrics)
    }
  }

  private fun spToPx(sp: Double): Float {
    return spToPx(sp, resources.displayMetrics)
  }
}
