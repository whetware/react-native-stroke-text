import UIKit

final class StrokeTextView: UIView {
  private let label = StrokedTextLabel()

  var text: String = "" {
    didSet { updateText() }
  }

  var color: UIColor = .black {
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
    didSet { label.numberOfLines = numberOfLines }
  }

  var ellipsis: Bool = false {
    didSet { label.lineBreakMode = ellipsis ? .byTruncatingTail : .byWordWrapping }
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
    let size = max(1, fontSize)

    let baseFont: UIFont = {
      if let family = fontFamily, let custom = UIFont(name: family, size: size) {
        return custom
      } else {
        return UIFont.systemFont(ofSize: size, weight: fontWeight.toUIFontWeight())
      }
    }()

    let styledFont: UIFont = {
      guard fontStyle == .italic else { return baseFont }
      guard let descriptor = baseFont.fontDescriptor.withSymbolicTraits([.traitItalic]) else {
        return baseFont
      }
      return UIFont(descriptor: descriptor, size: size)
    }()

    label.font = styledFont
    updateText()
  }

  private func updateText() {
    let transformed = textTransform.apply(to: text)

    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.alignment = textAlign.toNSTextAlignment()
    if let lh = lineHeight, lh > 0 {
      paragraphStyle.minimumLineHeight = lh
      paragraphStyle.maximumLineHeight = lh
    }

    var attributes: [NSAttributedString.Key: Any] = [
      .paragraphStyle: paragraphStyle,
      .font: label.font as Any,
    ]

    if let ls = letterSpacing {
      attributes[.kern] = ls
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

  private func invalidateMeasurements() {
    setNeedsLayout()
    invalidateIntrinsicContentSize()
    setNeedsDisplay()
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
