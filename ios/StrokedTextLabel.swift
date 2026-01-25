import UIKit

final class StrokedTextLabel: UILabel {
  var textInsets: UIEdgeInsets = .zero {
    didSet { invalidateMeasurements() }
  }

  var outlineColor: UIColor = .clear {
    didSet { setNeedsDisplay() }
  }

  var outlineWidth: CGFloat = 0 {
    didSet { invalidateMeasurements() }
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
    numberOfLines = 0
    lineBreakMode = .byWordWrapping
    clipsToBounds = false
    layer.masksToBounds = false
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func drawText(in rect: CGRect) {
    let insetRect = rect.inset(by: textInsets)

    guard outlineWidth > 0, let ctx = UIGraphicsGetCurrentContext() else {
      super.drawText(in: insetRect)
      return
    }

    let fillColor = textColor

    ctx.setLineWidth(outlineWidth)
    ctx.setLineJoin(.round)

    ctx.setTextDrawingMode(.stroke)
    textColor = outlineColor
    super.drawText(in: insetRect)

    ctx.setTextDrawingMode(.fill)
    textColor = fillColor
    super.drawText(in: insetRect)
  }

  override func textRect(
    forBounds bounds: CGRect,
    limitedToNumberOfLines numberOfLines: Int
  ) -> CGRect {
    let insetBounds = bounds.inset(by: textInsets)
    var rect = super.textRect(forBounds: insetBounds, limitedToNumberOfLines: numberOfLines)
    // Match React Native <Text> behavior (top-aligned). UILabel may otherwise vertically center
    // text when the bounds are taller than the rendered text.
    rect.origin.y = insetBounds.origin.y
    rect.origin.x -= textInsets.left
    rect.origin.y -= textInsets.top
    rect.size.width += textInsets.left + textInsets.right
    rect.size.height += textInsets.top + textInsets.bottom
    return rect
  }

  private func invalidateMeasurements() {
    setNeedsLayout()
    invalidateIntrinsicContentSize()
    setNeedsDisplay()
  }
}
