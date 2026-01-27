import UIKit

final class StrokeTextView: UIView {
  private let label = StrokedTextLabel()

  var text: String = "" {
    didSet { updateText() }
  }

  var color: UIColor = StrokeTextView.defaultTextColor() {
    didSet { label.textColor = color }
  }

  var strokeColor: UIColor = .clear {
    didSet { label.outlineColor = strokeColor }
  }

  var strokeWidth: CGFloat = 0 {
    didSet { label.outlineWidth = strokeWidth }
  }

  var fontSize: CGFloat = 14 {
    didSet { updateFont() }
  }

  var fontWeight: String = "400" {
    didSet { updateFont() }
  }

  var fontFamily: String? = nil {
    didSet { updateFont() }
  }

  var fontStyle: StrokeTextFontStyle = .normal {
    didSet { updateFont() }
  }

  var allowFontScaling: Bool = true {
    didSet { updateFont() }
  }

  var maxFontSizeMultiplier: CGFloat? = nil {
    didSet { updateFont() }
  }

  var lineHeight: CGFloat? = nil {
    didSet { updateText() }
  }

  var letterSpacing: CGFloat? = nil {
    didSet { updateText() }
  }

  var textAlign: StrokeTextAlign = .auto {
    didSet { updateText() }
  }

  var textDecorationLine: StrokeTextDecorationLine = .none {
    didSet { updateText() }
  }

  var textTransform: StrokeTextTransform = .none {
    didSet { updateText() }
  }

  var numberOfLines: Int = 0 {
    didSet {
      label.numberOfLines = numberOfLines
      updateLineBreakMode()
    }
  }

  var ellipsizeMode: StrokeTextEllipsizeMode = .tail {
    didSet { updateLineBreakMode() }
  }

  var paddingInsets: UIEdgeInsets = .zero {
    didSet {
      label.textInsets = paddingInsets
      invalidateMeasurements()
    }
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
    isOpaque = false
    addSubview(label)
    label.textInsets = paddingInsets
    updateLineBreakMode()
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    label.frame = bounds
    label.preferredMaxLayoutWidth = max(
      0,
      bounds.width - paddingInsets.left - paddingInsets.right
    )
  }

  override func sizeThatFits(_ size: CGSize) -> CGSize {
    return label.sizeThatFits(size)
  }

  override var intrinsicContentSize: CGSize {
    return label.intrinsicContentSize
  }

  private func updateFont() {
    let baseSize = max(1, fontSize)
    let scaledSize = scaleTypography(baseSize)
    let weight = fontWeight.toUIFontWeight()
    let italic = fontStyle == .italic

    label.font = resolveFont(family: fontFamily, size: scaledSize, weight: weight, italic: italic)
    updateText()
  }

  private func updateText() {
    let transformed = textTransform.apply(to: text)

    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.alignment = textAlign.toNSTextAlignment()
    if let lh = lineHeight, lh > 0 {
      let scaledLineHeight = scaleTypography(lh)
      paragraphStyle.minimumLineHeight = scaledLineHeight
      paragraphStyle.maximumLineHeight = scaledLineHeight
    }

    var attributes: [NSAttributedString.Key: Any] = [
      .paragraphStyle: paragraphStyle,
      .font: label.font as Any,
    ]

    if let ls = letterSpacing {
      attributes[.kern] = scaleTypography(ls)
    }

    switch textDecorationLine {
    case .underline:
      attributes[.underlineStyle] = NSUnderlineStyle.single.rawValue
    case .lineThrough:
      attributes[.strikethroughStyle] = NSUnderlineStyle.single.rawValue
    case .underlineLineThrough:
      attributes[.underlineStyle] = NSUnderlineStyle.single.rawValue
      attributes[.strikethroughStyle] = NSUnderlineStyle.single.rawValue
    case .none:
      break
    }

    label.attributedText = NSAttributedString(string: transformed, attributes: attributes)
    invalidateMeasurements()
  }

  private func updateLineBreakMode() {
    if numberOfLines <= 0 {
      label.lineBreakMode = .byWordWrapping
      invalidateMeasurements()
      return
    }

    switch ellipsizeMode {
    case .head:
      label.lineBreakMode = .byTruncatingHead
    case .middle:
      label.lineBreakMode = .byTruncatingMiddle
    case .clip:
      label.lineBreakMode = .byClipping
    case .tail:
      label.lineBreakMode = .byTruncatingTail
    @unknown default:
      label.lineBreakMode = .byTruncatingTail
    }

    invalidateMeasurements()
  }

  private func scaleTypography(_ value: CGFloat) -> CGFloat {
    guard value > 0, allowFontScaling else { return value }
    let scaled = UIFontMetrics.default.scaledValue(for: value)
    guard let maxMultiplier = maxFontSizeMultiplier, maxMultiplier > 0 else { return scaled }
    return min(scaled, value * maxMultiplier)
  }

  private func resolveFont(
    family: String?,
    size: CGFloat,
    weight: UIFont.Weight,
    italic: Bool
  ) -> UIFont {
    let trimmedFamily = family?.trimmingCharacters(in: .whitespacesAndNewlines)

    if let name = trimmedFamily, !name.isEmpty {
      // 1) Treat as a concrete font name (PostScript name).
      if let fontByName = UIFont(name: name, size: size) {
        return italic ? fontByName.withItalicTrait(size: size) : fontByName
      }

      // 2) Treat as a family name and pick the closest matching variant.
      let familyName =
        UIFont.familyNames.first(where: { $0.caseInsensitiveCompare(name) == .orderedSame }) ?? name
      let candidates = UIFont.fontNames(forFamilyName: familyName).compactMap { UIFont(name: $0, size: size) }
      if let best = bestFontMatch(in: candidates, size: size, weight: weight, italic: italic) {
        return best
      }
    }

    // 3) Fallback to system font.
    let system = UIFont.systemFont(ofSize: size, weight: weight)
    return italic ? system.withItalicTrait(size: size) : system
  }

  private func bestFontMatch(
    in fonts: [UIFont],
    size: CGFloat,
    weight: UIFont.Weight,
    italic: Bool
  ) -> UIFont? {
    guard !fonts.isEmpty else { return nil }

    let targetWeight = Double(weight.rawValue)
    var best: UIFont = fonts[0]
    var bestScore = Double.greatestFiniteMagnitude

    for font in fonts {
      let traits = font.fontDescriptor.object(forKey: .traits) as? [UIFontDescriptor.TraitKey: Any]
      let fontWeight = (traits?[.weight] as? CGFloat) ?? 0
      let isItalic = font.fontDescriptor.symbolicTraits.contains(.traitItalic)

      let italicPenalty = isItalic == italic ? 0.0 : 1000.0
      let weightPenalty = Swift.abs(Double(fontWeight) - targetWeight)
      let score = italicPenalty + weightPenalty

      if score < bestScore {
        bestScore = score
        best = font
      }
    }

    return italic ? best.withItalicTrait(size: size) : best
  }

  private func invalidateMeasurements() {
    setNeedsLayout()
    invalidateIntrinsicContentSize()
    setNeedsDisplay()
  }

  static func defaultTextColor() -> UIColor {
    if #available(iOS 13.0, *) {
      return .label
    }
    return .black
  }
}

private extension String {
  func toUIFontWeight() -> UIFont.Weight {
    let lower = trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    switch lower {
    case "ultralight", "100": return .ultraLight
    case "thin", "200": return .thin
    case "light", "300": return .light
    case "normal", "regular", "400": return .regular
    case "medium", "500": return .medium
    case "semibold", "600": return .semibold
    case "bold", "700": return .bold
    case "heavy", "800": return .heavy
    case "black", "900": return .black
    default:
      if let value = Int(lower) {
        switch value {
        case ...150: return .ultraLight
        case 151...250: return .thin
        case 251...350: return .light
        case 351...450: return .regular
        case 451...550: return .medium
        case 551...650: return .semibold
        case 651...750: return .bold
        case 751...850: return .heavy
        default: return .black
        }
      }
      return .regular
    }
  }
}

private extension UIFont {
  func withItalicTrait(size: CGFloat) -> UIFont {
    let descriptor = fontDescriptor
    let traits = descriptor.symbolicTraits.union(.traitItalic)
    guard let italicDescriptor = descriptor.withSymbolicTraits(traits) else { return self }
    return UIFont(descriptor: italicDescriptor, size: size)
  }
}

private extension StrokeTextAlign {
  func toNSTextAlignment() -> NSTextAlignment {
    switch self {
    case .left: return .left
    case .right: return .right
    case .center: return .center
    case .justify: return .justified
    case .auto: return .natural
    @unknown default: return .natural
    }
  }
}

private extension StrokeTextTransform {
  func apply(to input: String) -> String {
    switch self {
    case .uppercase: return input.uppercased()
    case .lowercase: return input.lowercased()
    case .capitalize: return input.capitalized
    case .none: return input
    @unknown default: return input
    }
  }
}
