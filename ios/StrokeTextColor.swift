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
    if let keyword = parseKeyword(lower) {
      return keyword
    }
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

    if hex.count == 4 {
      // #RGBA (CSS Color Module Level 4 / React Native)
      hex = hex.map { "\($0)\($0)" }.joined()
    }

    let alpha: CGFloat
    let rgbHex: String

    switch hex.count {
    case 6:
      alpha = 1
      rgbHex = hex
    case 8:
      // #RRGGBBAA (CSS Color Module Level 4 / React Native)
      rgbHex = String(hex.prefix(6))
      let a = String(hex.suffix(2))
      guard let aByte = UInt8(a, radix: 16) else { return nil }
      alpha = CGFloat(aByte) / 255.0
    default:
      return nil
    }

    guard let rgb = UInt32(rgbHex, radix: 16) else { return nil }

    let r = CGFloat((rgb & 0xFF0000) >> 16) / 255.0
    let g = CGFloat((rgb & 0x00FF00) >> 8) / 255.0
    let b = CGFloat(rgb & 0x0000FF) / 255.0
    return UIColor(red: r, green: g, blue: b, alpha: alpha)
  }

  private static func parseKeyword(_ lower: String) -> UIColor? {
    switch lower {
    case "transparent", "clear":
      return .clear
    case "black":
      return .black
    case "white":
      return .white
    case "red":
      return .red
    case "green":
      return .green
    case "blue":
      return .blue
    case "cyan", "aqua":
      return .cyan
    case "magenta", "fuchsia":
      return .magenta
    case "yellow":
      return .yellow
    case "gray", "grey":
      return .gray
    case "lightgray", "lightgrey":
      return .lightGray
    case "darkgray", "darkgrey":
      return .darkGray
    default:
      return nil
    }
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
