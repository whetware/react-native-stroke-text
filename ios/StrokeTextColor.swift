import UIKit

enum StrokeTextColor {
  static func parse(_ colorString: String) -> UIColor? {
    let trimmed = colorString.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty {
      return nil
    }

    if trimmed.hasPrefix("#") {
      return parseHex(trimmed)
    }

    let lower = trimmed.lowercased()
    if lower.hasPrefix("rgba(") {
      return parseRGBA(trimmed)
    }
    if lower.hasPrefix("rgb(") {
      return parseRGB(trimmed)
    }

    return nil
  }

  private static func parseHex(_ hexString: String) -> UIColor? {
    var hex = String(hexString.dropFirst())

    if hex.count == 3 {
      hex = hex.map { "\($0)\($0)" }.joined()
    }

    var alpha: CGFloat = 1
    if hex.count == 8 {
      let a = hex.suffix(2)
      hex = String(hex.prefix(6))
      guard let aByte = UInt8(a, radix: 16) else { return nil }
      alpha = CGFloat(aByte) / 255.0
    }

    guard hex.count == 6, let rgb = UInt32(hex, radix: 16) else {
      return nil
    }

    let r = CGFloat((rgb & 0xFF0000) >> 16) / 255.0
    let g = CGFloat((rgb & 0x00FF00) >> 8) / 255.0
    let b = CGFloat(rgb & 0x0000FF) / 255.0
    return UIColor(red: r, green: g, blue: b, alpha: alpha)
  }

  private static func parseRGB(_ rgbString: String) -> UIColor? {
    let inner = rgbString.dropFirst(4).dropLast(1)
    let parts = inner.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }
    guard parts.count == 3 else { return nil }
    guard
      let r = Double(parts[0]),
      let g = Double(parts[1]),
      let b = Double(parts[2])
    else { return nil }
    return UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: 1)
  }

  private static func parseRGBA(_ rgbaString: String) -> UIColor? {
    let inner = rgbaString.dropFirst(5).dropLast(1)
    let parts = inner.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }
    guard parts.count == 4 else { return nil }
    guard
      let r = Double(parts[0]),
      let g = Double(parts[1]),
      let b = Double(parts[2]),
      let a = Double(parts[3])
    else { return nil }
    return UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: a)
  }
}

