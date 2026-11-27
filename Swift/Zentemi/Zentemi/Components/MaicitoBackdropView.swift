import SwiftUI

/// Fondo decorativo detrás de Maicito (artículos de la zona "Fondo") y paisaje de Inicio.
/// Espejo exacto de `MaicitoBackdrop.kt`, `MaicitoBackdropsExtra.kt` y `HomeScene.kt` en Android.
struct MaicitoBackdropView: View {
    let backdrop: Backdrop

    var body: some View {
        Canvas { ctx, size in
            switch backdrop {
            case .sunset: drawSunset(&ctx, size: size)
            case .starryNight: drawStarryNight(&ctx, size: size)
            case .papelPicado: drawPapelPicado(&ctx, size: size)
            case .talavera: drawTalavera(&ctx, size: size)
            case .milpa: drawMilpa(&ctx, size: size)
            case .playa: drawPlaya(&ctx, size: size)
            case .bosque: drawBosque(&ctx, size: size)
            case .piramide: drawPiramide(&ctx, size: size)
            case .arcoiris: drawArcoiris(&ctx, size: size)
            case .diaDeMuertos: drawDiaDeMuertos(&ctx, size: size)
            }
        }
    }

    // MARK: - 1. Atardecer
    private func drawSunset(_ ctx: inout GraphicsContext, size: CGSize) {
        let rect = CGRect(origin: .zero, size: size)
        ctx.fill(
            Path(rect),
            with: .linearGradient(
                Gradient(colors: [hex(0xFFC371), hex(0xFF8A7A), hex(0xC86DD7)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        let r = min(size.width, size.height) * 0.22
        let center = CGPoint(x: size.width / 2, y: size.height * 0.78)
        fillCircle(&ctx, center: center, radius: r * 1.35, color: hex(0xFFE29A).opacity(0.35))
        fillCircle(&ctx, center: center, radius: r, color: hex(0xFFD166))

        var hills = Path()
        hills.move(to: CGPoint(x: 0, y: size.height * 0.82))
        hills.addCurve(
            to: CGPoint(x: size.width * 0.65, y: size.height * 0.80),
            control1: CGPoint(x: size.width * 0.25, y: size.height * 0.70),
            control2: CGPoint(x: size.width * 0.45, y: size.height * 0.88)
        )
        hills.addCurve(
            to: CGPoint(x: size.width, y: size.height * 0.78),
            control1: CGPoint(x: size.width * 0.80, y: size.height * 0.74),
            control2: CGPoint(x: size.width * 0.92, y: size.height * 0.80)
        )
        hills.addLine(to: CGPoint(x: size.width, y: size.height))
        hills.addLine(to: CGPoint(x: 0, y: size.height))
        hills.closeSubpath()
        ctx.fill(hills, with: .color(hex(0x8E4FB8)))
    }

    // MARK: - 2. Noche estrellada
    private func drawStarryNight(_ ctx: inout GraphicsContext, size: CGSize) {
        let rect = CGRect(origin: .zero, size: size)
        ctx.fill(
            Path(rect),
            with: .linearGradient(
                Gradient(colors: [hex(0x141842), hex(0x3B2A7A)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        let stars: [(CGFloat, CGFloat)] = [
            (0.12, 0.18), (0.30, 0.10), (0.52, 0.22), (0.74, 0.12), (0.88, 0.30),
            (0.18, 0.42), (0.40, 0.36), (0.66, 0.40), (0.84, 0.56), (0.10, 0.66),
            (0.28, 0.74), (0.58, 0.62), (0.76, 0.80), (0.46, 0.86), (0.92, 0.88)
        ]
        let base = min(size.width, size.height)
        for (i, pos) in stars.enumerated() {
            let r = base * (i % 3 == 0 ? 0.018 : 0.011)
            fillCircle(
                &ctx,
                center: CGPoint(x: size.width * pos.0, y: size.height * pos.1),
                radius: r,
                color: Color.white.opacity(i % 2 == 0 ? 1.0 : 0.7)
            )
        }
        let moonCenter = CGPoint(x: size.width * 0.78, y: size.height * 0.22)
        let moonR = base * 0.11
        fillCircle(&ctx, center: moonCenter, radius: moonR, color: hex(0xFFE9A8))
        fillCircle(
            &ctx,
            center: CGPoint(x: moonCenter.x + moonR * 0.45, y: moonCenter.y - moonR * 0.25),
            radius: moonR * 0.85,
            color: hex(0x181C48)
        )
    }

    // MARK: - 3. Papel picado
    private func drawPapelPicado(_ ctx: inout GraphicsContext, size: CGSize) {
        let cream = hex(0xFFF4E0)
        ctx.fill(Path(CGRect(origin: .zero, size: size)), with: .color(cream))
        let colors = [hex(0xFF4FA3), hex(0x2EC4B6), hex(0xFFC93C), hex(0x7B4ACA), hex(0xFF8C42)]
        let rows: [CGFloat] = [0.10, 0.42]
        let minDim = min(size.width, size.height)

        for (row, yFrac) in rows.enumerated() {
            let y = size.height * yFrac
            let flagW = size.width / 5.5
            let flagH = flagW * 1.05

            var rope = Path()
            rope.move(to: CGPoint(x: 0, y: y))
            rope.addLine(to: CGPoint(x: size.width, y: y))
            ctx.stroke(rope, with: .color(hex(0x8B5A2B)), lineWidth: minDim * 0.008)

            var x = -flagW * 0.3 * CGFloat(row)
            var i = row
            while x < size.width {
                var flag = Path()
                flag.move(to: CGPoint(x: x, y: y))
                flag.addLine(to: CGPoint(x: x + flagW * 0.9, y: y))
                flag.addLine(to: CGPoint(x: x + flagW * 0.9, y: y + flagH * 0.8))
                flag.addLine(to: CGPoint(x: x + flagW * 0.45, y: y + flagH))
                flag.addLine(to: CGPoint(x: x, y: y + flagH * 0.8))
                flag.closeSubpath()
                ctx.fill(flag, with: .color(colors[i % colors.count]))

                let cx = x + flagW * 0.45
                fillCircle(&ctx, center: CGPoint(x: cx, y: y + flagH * 0.35), radius: flagW * 0.09, color: cream)
                fillCircle(&ctx, center: CGPoint(x: cx - flagW * 0.2, y: y + flagH * 0.6), radius: flagW * 0.05, color: cream)
                fillCircle(&ctx, center: CGPoint(x: cx + flagW * 0.2, y: y + flagH * 0.6), radius: flagW * 0.05, color: cream)
                x += flagW
                i += 1
            }
        }
    }

    // MARK: - 4. Talavera
    private func drawTalavera(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(Path(CGRect(origin: .zero, size: size)), with: .color(hex(0xF7F3EA)))
        let blue = hex(0x1E4FA3)
        let yellow = hex(0xF2B33D)
        let tile = max(16, min(size.width, size.height) / 4)
        let cols = Int(size.width / tile) + 1
        let rows = Int(size.height / tile) + 1

        for r in 0..<rows {
            for c in 0..<cols {
                let cx = CGFloat(c) * tile + tile / 2
                let cy = CGFloat(r) * tile + tile / 2
                let center = CGPoint(x: cx, y: cy)

                for k in 0..<4 {
                    ctx.drawLayer { layer in
                        layer.translateBy(x: cx, y: cy)
                        layer.rotate(by: .degrees(Double(45 + k * 90)))
                        fillCircle(&layer, center: CGPoint(x: 0, y: -tile * 0.36), radius: tile * 0.05, color: blue)
                    }
                    ctx.drawLayer { layer in
                        layer.translateBy(x: cx, y: cy)
                        layer.rotate(by: .degrees(Double(k * 90)))
                        let oval = CGRect(x: -tile * 0.09, y: -tile * 0.42, width: tile * 0.18, height: tile * 0.32)
                        layer.fill(Path(ellipseIn: oval), with: .color(blue))
                    }
                }

                fillCircle(&ctx, center: center, radius: tile * 0.10, color: yellow)
                let ring = CGRect(x: cx - tile * 0.10, y: cy - tile * 0.10, width: tile * 0.20, height: tile * 0.20)
                ctx.stroke(Path(ellipseIn: ring), with: .color(blue), lineWidth: tile * 0.03)

                let border = CGRect(x: CGFloat(c) * tile, y: CGFloat(r) * tile, width: tile, height: tile)
                ctx.stroke(Path(border), with: .color(blue.opacity(0.25)), lineWidth: tile * 0.02)
            }
        }
    }

    // MARK: - 5. Milpa
    private func drawMilpa(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0x8FD3FF), hex(0xD8F1FF)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        fillCircle(
            &ctx,
            center: CGPoint(x: size.width * 0.82, y: size.height * 0.16),
            radius: min(size.width, size.height) * 0.10,
            color: hex(0xFFE08A)
        )
        ctx.fill(
            Path(CGRect(x: 0, y: size.height * 0.80, width: size.width, height: size.height * 0.20)),
            with: .color(hex(0x8B5A2B))
        )

        let stalks = 7
        for i in 0..<stalks {
            let x = size.width * (CGFloat(i) + 0.5) / CGFloat(stalks)
            let top = size.height * (i % 2 == 0 ? 0.38 : 0.46)
            let base = size.height * 0.82

            var stem = Path()
            stem.move(to: CGPoint(x: x, y: base))
            stem.addLine(to: CGPoint(x: x, y: top))
            ctx.stroke(stem, with: .color(hex(0x3F8A3A)), lineWidth: size.width * 0.018)

            let leafW = size.width * 0.07
            for k in 0...2 {
                let y = top + (base - top) * (0.25 + CGFloat(k) * 0.22)
                let dir: CGFloat = ((i + k) % 2 == 0) ? 1 : -1
                var leaf = Path()
                leaf.move(to: CGPoint(x: x, y: y))
                leaf.addQuadCurve(
                    to: CGPoint(x: x + dir * leafW * 1.4, y: y - leafW * 0.2),
                    control: CGPoint(x: x + dir * leafW, y: y - leafW * 0.9)
                )
                leaf.addQuadCurve(
                    to: CGPoint(x: x, y: y + leafW * 0.15),
                    control: CGPoint(x: x + dir * leafW * 0.6, y: y - leafW * 0.2)
                )
                leaf.closeSubpath()
                ctx.fill(leaf, with: .color(hex(0x6CC24A)))
            }

            let cob = CGRect(
                x: x + size.width * 0.008,
                y: top + (base - top) * 0.30,
                width: size.width * 0.03,
                height: size.height * 0.07
            )
            ctx.fill(Path(ellipseIn: cob), with: .color(hex(0xFFC93C)))

            let tassel = size.width * 0.02
            var tPath = Path()
            tPath.move(to: CGPoint(x: x, y: top))
            tPath.addLine(to: CGPoint(x: x - tassel, y: top - size.height * 0.05))
            tPath.move(to: CGPoint(x: x, y: top))
            tPath.addLine(to: CGPoint(x: x + tassel, y: top - size.height * 0.05))
            ctx.stroke(tPath, with: .color(hex(0xE0B84A)), lineWidth: size.width * 0.008)
        }
    }

    // MARK: - 6. Playa
    private func drawPlaya(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0x6EC8FF), hex(0xBDEBFF)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        fillCircle(
            &ctx,
            center: CGPoint(x: size.width * 0.20, y: size.height * 0.18),
            radius: min(size.width, size.height) * 0.12,
            color: hex(0xFFE08A)
        )
        let sea = CGRect(x: 0, y: size.height * 0.52, width: size.width, height: size.height * 0.22)
        ctx.fill(
            Path(sea),
            with: .linearGradient(
                Gradient(colors: [hex(0x2EA8D8), hex(0x1E7FB8)]),
                startPoint: CGPoint(x: 0, y: sea.minY),
                endPoint: CGPoint(x: 0, y: sea.maxY)
            )
        )

        var wave = Path()
        wave.move(to: CGPoint(x: 0, y: size.height * 0.70))
        var x: CGFloat = 0
        let step = size.width / 6
        while x < size.width {
            wave.addQuadCurve(
                to: CGPoint(x: x + step, y: size.height * 0.70),
                control: CGPoint(x: x + step / 2, y: size.height * 0.66)
            )
            x += step
        }
        wave.addLine(to: CGPoint(x: size.width, y: size.height * 0.74))
        wave.addLine(to: CGPoint(x: 0, y: size.height * 0.74))
        wave.closeSubpath()
        ctx.fill(wave, with: .color(Color.white.opacity(0.8)))

        ctx.fill(
            Path(CGRect(x: 0, y: size.height * 0.73, width: size.width, height: size.height * 0.27)),
            with: .color(hex(0xF6D9A0))
        )

        let px = size.width * 0.84
        let crown = CGPoint(x: px - size.width * 0.02, y: size.height * 0.34)
        var trunk = Path()
        trunk.move(to: CGPoint(x: px, y: size.height * 0.86))
        trunk.addQuadCurve(to: crown, control: CGPoint(x: px - size.width * 0.06, y: size.height * 0.55))
        ctx.stroke(trunk, with: .color(hex(0x9A6A3A)), lineWidth: size.width * 0.03)

        for k in 0..<6 {
            let a = Double(190 + k * 32) * .pi / 180
            let end = CGPoint(
                x: crown.x + CGFloat(cos(a)) * size.width * 0.17,
                y: crown.y + CGFloat(sin(a)) * size.width * 0.08 + size.height * 0.05
            )
            var leaf = Path()
            leaf.move(to: crown)
            leaf.addQuadCurve(
                to: end,
                control: CGPoint(x: (crown.x + end.x) / 2, y: (crown.y + end.y) / 2 - size.height * 0.06)
            )
            ctx.stroke(leaf, with: .color(hex(0x3F9A4A)), style: StrokeStyle(lineWidth: size.width * 0.03, lineCap: .round))
        }
    }

    // MARK: - 7. Bosque
    private func drawBosque(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0xBFE8C8), hex(0xE9F7E4)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )

        func tree(x: CGFloat, base: CGFloat, h: CGFloat, color: Color) {
            let w = h * 0.55
            var trunk = Path()
            trunk.move(to: CGPoint(x: x, y: base))
            trunk.addLine(to: CGPoint(x: x, y: base - h * 0.2))
            ctx.stroke(trunk, with: .color(hex(0x7A4A2A)), lineWidth: w * 0.16)

            for k in 0...2 {
                let bottom = base - h * (0.12 + CGFloat(k) * 0.25)
                let top = base - h * (0.55 + CGFloat(k) * 0.25)
                let half = w * (0.5 - CGFloat(k) * 0.1)
                var tri = Path()
                tri.move(to: CGPoint(x: x, y: top))
                tri.addLine(to: CGPoint(x: x + half, y: bottom))
                tri.addLine(to: CGPoint(x: x - half, y: bottom))
                tri.closeSubpath()
                ctx.fill(tri, with: .color(color))
            }
        }

        let far = hex(0x7CC08A)
        let near = hex(0x3F8A4A)
        for fx in [0.08, 0.30, 0.55, 0.78, 0.96] as [CGFloat] {
            tree(x: size.width * fx, base: size.height * 0.72, h: size.height * 0.45, color: far)
        }
        ctx.fill(
            Path(CGRect(x: 0, y: size.height * 0.78, width: size.width, height: size.height * 0.22)),
            with: .color(hex(0x8BCB6A))
        )
        for fx in [0.0, 0.18, 0.88] as [CGFloat] {
            tree(x: size.width * fx, base: size.height * 0.90, h: size.height * 0.58, color: near)
        }
        for (fx, fy) in [(0.40, 0.90), (0.66, 0.93)] as [(CGFloat, CGFloat)] {
            let c = CGPoint(x: size.width * fx, y: size.height * fy)
            let r = min(size.width, size.height) * 0.04
            ctx.fill(Path(CGRect(x: c.x - r * 0.3, y: c.y - r * 0.3, width: r * 0.6, height: r)), with: .color(hex(0xFFF6E5)))
            var cap = Path()
            cap.addArc(center: CGPoint(x: c.x, y: c.y - r * 0.3), radius: r, startAngle: .degrees(180), endAngle: .degrees(360), clockwise: false)
            cap.closeSubpath()
            ctx.fill(cap, with: .color(hex(0xE63946)))
            fillCircle(&ctx, center: CGPoint(x: c.x - r * 0.4, y: c.y - r * 0.55), radius: r * 0.16, color: .white)
            fillCircle(&ctx, center: CGPoint(x: c.x + r * 0.35, y: c.y - r * 0.60), radius: r * 0.12, color: .white)
        }
    }

    // MARK: - 8. Pirámide
    private func drawPiramide(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0xFFB86B), hex(0xFFE2A8)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        fillCircle(
            &ctx,
            center: CGPoint(x: size.width * 0.80, y: size.height * 0.20),
            radius: min(size.width, size.height) * 0.10,
            color: hex(0xFFF1C2)
        )
        ctx.fill(
            Path(CGRect(x: 0, y: size.height * 0.78, width: size.width, height: size.height * 0.22)),
            with: .color(hex(0xE6B877))
        )

        let cx = size.width * 0.5
        let base = size.height * 0.80
        let levels = 5
        let maxW = size.width * 0.86
        let levelH = size.height * 0.08
        for i in 0..<levels {
            let w = maxW * (1.0 - CGFloat(i) * 0.16)
            let y = base - CGFloat(i + 1) * levelH
            ctx.fill(
                Path(CGRect(x: cx - w / 2, y: y, width: w, height: levelH)),
                with: .color(i % 2 == 0 ? hex(0xB9794A) : hex(0xA8693E))
            )
        }
        let tw = maxW * 0.22
        ctx.fill(
            Path(CGRect(x: cx - tw / 2, y: base - CGFloat(levels + 1) * levelH, width: tw, height: levelH)),
            with: .color(hex(0x8E5530))
        )
        ctx.fill(
            Path(CGRect(x: cx - size.width * 0.06, y: base - CGFloat(levels) * levelH, width: size.width * 0.12, height: CGFloat(levels) * levelH)),
            with: .color(hex(0xD9A46E))
        )
        for fx in [0.08, 0.92] as [CGFloat] {
            let x = size.width * fx
            ctx.fill(Path(ellipseIn: CGRect(x: x - size.width * 0.035, y: size.height * 0.70, width: size.width * 0.07, height: size.height * 0.11)), with: .color(hex(0x4CAF50)))
            ctx.fill(Path(ellipseIn: CGRect(x: x - size.width * 0.01, y: size.height * 0.64, width: size.width * 0.05, height: size.height * 0.08)), with: .color(hex(0x5CBF60)))
        }
    }

    // MARK: - 9. Arcoíris
    private func drawArcoiris(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0xB9E6FF), hex(0xFDE7FF)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        let colors = [hex(0xFF6B6B), hex(0xFFA94D), hex(0xFFD43B), hex(0x69DB7C), hex(0x4DABF7), hex(0x9775FA)]
        let center = CGPoint(x: size.width / 2, y: size.height * 0.80)
        let outer = size.width * 0.46
        let band = outer * 0.10
        for (i, c) in colors.enumerated() {
            let r = outer - CGFloat(i) * band - band / 2
            var arc = Path()
            arc.addArc(center: center, radius: r, startAngle: .degrees(180), endAngle: .degrees(360), clockwise: false)
            ctx.stroke(arc, with: .color(c), lineWidth: band)
        }

        func cloud(x: CGFloat, y: CGFloat, s: CGFloat) {
            fillCircle(&ctx, center: CGPoint(x: x, y: y), radius: s, color: .white)
            fillCircle(&ctx, center: CGPoint(x: x - s, y: y + s * 0.2), radius: s * 0.8, color: .white)
            fillCircle(&ctx, center: CGPoint(x: x + s, y: y + s * 0.2), radius: s * 0.8, color: .white)
            ctx.fill(Path(CGRect(x: x - s * 1.6, y: y + s * 0.1, width: s * 3.2, height: s * 0.9)), with: .color(.white))
        }
        let s = min(size.width, size.height) * 0.08
        cloud(x: size.width * 0.12, y: size.height * 0.78, s: s)
        cloud(x: size.width * 0.88, y: size.height * 0.78, s: s)
        cloud(x: size.width * 0.70, y: size.height * 0.16, s: s * 0.7)
    }

    // MARK: - 10. Día de Muertos
    private func drawDiaDeMuertos(_ ctx: inout GraphicsContext, size: CGSize) {
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(
                Gradient(colors: [hex(0x2A1450), hex(0x5B2A86)]),
                startPoint: .zero,
                endPoint: CGPoint(x: 0, y: size.height)
            )
        )
        let colors = [hex(0xFF4FA3), hex(0xFF8C42), hex(0x2EC4B6), hex(0xFFC93C)]
        let flagW = size.width / 6
        let y0 = size.height * 0.06
        var rope = Path()
        rope.move(to: CGPoint(x: 0, y: y0))
        rope.addLine(to: CGPoint(x: size.width, y: y0))
        ctx.stroke(rope, with: .color(hex(0xFFF4E0)), lineWidth: min(size.width, size.height) * 0.006)

        for i in 0..<6 {
            let x = CGFloat(i) * flagW + flagW * 0.06
            var flag = Path()
            flag.move(to: CGPoint(x: x, y: y0))
            flag.addLine(to: CGPoint(x: x + flagW * 0.88, y: y0))
            flag.addLine(to: CGPoint(x: x + flagW * 0.88, y: y0 + flagW * 0.8))
            flag.addLine(to: CGPoint(x: x + flagW * 0.44, y: y0 + flagW))
            flag.addLine(to: CGPoint(x: x, y: y0 + flagW * 0.8))
            flag.closeSubpath()
            ctx.fill(flag, with: .color(colors[i % colors.count]))
            fillCircle(&ctx, center: CGPoint(x: x + flagW * 0.44, y: y0 + flagW * 0.4), radius: flagW * 0.09, color: hex(0x2A1450))
        }

        ctx.fill(Path(CGRect(x: 0, y: size.height * 0.80, width: size.width, height: size.height * 0.20)), with: .color(hex(0x8E4FB8)))
        ctx.fill(Path(CGRect(x: 0, y: size.height * 0.80, width: size.width, height: size.height * 0.03)), with: .color(hex(0xB073D6)))

        for (i, fx) in ([0.10, 0.26, 0.74, 0.90] as [CGFloat]).enumerated() {
            let x = size.width * fx
            let h = size.height * (i % 2 == 0 ? 0.12 : 0.09)
            let w = size.width * 0.045
            let top = size.height * 0.80 - h
            fillCircle(&ctx, center: CGPoint(x: x, y: top - w * 0.6), radius: w * 1.6, color: hex(0xFFC93C).opacity(0.25))
            ctx.fill(Path(CGRect(x: x - w / 2, y: top, width: w, height: h)), with: .color(hex(0xFFF4E0)))
            ctx.fill(Path(ellipseIn: CGRect(x: x - w * 0.25, y: top - w * 1.1, width: w * 0.5, height: w * 0.9)), with: .color(hex(0xFFB020)))
        }

        for fx in [0.18, 0.40, 0.60, 0.82] as [CGFloat] {
            let c = CGPoint(x: size.width * fx, y: size.height * 0.88)
            let r = min(size.width, size.height) * 0.045
            for k in 0..<8 {
                let a = Double(k * 45) * .pi / 180
                fillCircle(
                    &ctx,
                    center: CGPoint(x: c.x + CGFloat(cos(a)) * r * 0.6, y: c.y + CGFloat(sin(a)) * r * 0.6),
                    radius: r * 0.55,
                    color: hex(0xFF8C1A)
                )
            }
            fillCircle(&ctx, center: c, radius: r * 0.5, color: hex(0xFFB020))
        }
    }

    private func fillCircle(_ ctx: inout GraphicsContext, center: CGPoint, radius: CGFloat, color: Color) {
        let rect = CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2)
        ctx.fill(Path(ellipseIn: rect), with: .color(color))
    }

    private func hex(_ value: UInt32) -> Color {
        Color(
            red: Double((value >> 16) & 0xFF) / 255.0,
            green: Double((value >> 8) & 0xFF) / 255.0,
            blue: Double(value & 0xFF) / 255.0
        )
    }
}

/// Paisaje suave detrás de Maicito en Inicio (espejo exacto de `HomeScene.kt`).
struct HomeSceneView: View {
    var body: some View {
        Canvas { ctx, size in
            let w = size.width
            let h = size.height

            // Sol con resplandor
            let sun = CGPoint(x: w * 0.80, y: h * 0.20)
            let glowR = w * 0.26
            ctx.fill(
                Path(ellipseIn: CGRect(x: sun.x - glowR, y: sun.y - glowR, width: glowR * 2, height: glowR * 2)),
                with: .radialGradient(
                    Gradient(colors: [
                        Color(red: 0.957, green: 0.769, blue: 0.416).opacity(0.45),
                        Color.clear
                    ]),
                    center: sun,
                    startRadius: 0,
                    endRadius: glowR
                )
            )
            let sunR = w * 0.07
            ctx.fill(
                Path(ellipseIn: CGRect(x: sun.x - sunR, y: sun.y - sunR, width: sunR * 2, height: sunR * 2)),
                with: .color(Color(red: 1.0, green: 0.890, blue: 0.627))
            )

            // Nubes
            drawCloud(&ctx, center: CGPoint(x: w * 0.18, y: h * 0.16), s: w * 0.05)
            drawCloud(&ctx, center: CGPoint(x: w * 0.62, y: h * 0.08), s: w * 0.035)

            // Cerro de atrás (lila)
            var back = Path()
            back.move(to: CGPoint(x: 0, y: h * 0.62))
            back.addCurve(
                to: CGPoint(x: w * 0.55, y: h * 0.52),
                control1: CGPoint(x: w * 0.22, y: h * 0.48),
                control2: CGPoint(x: w * 0.40, y: h * 0.56)
            )
            back.addCurve(
                to: CGPoint(x: w, y: h * 0.50),
                control1: CGPoint(x: w * 0.72, y: h * 0.47),
                control2: CGPoint(x: w * 0.88, y: h * 0.55)
            )
            back.addLine(to: CGPoint(x: w, y: h))
            back.addLine(to: CGPoint(x: 0, y: h))
            back.closeSubpath()
            ctx.fill(back, with: .color(GameTheme.purple.opacity(0.10)))

            // Matas de maíz a los lados
            drawCornPlant(&ctx, base: CGPoint(x: w * 0.08, y: h * 0.70), height: h * 0.22)
            drawCornPlant(&ctx, base: CGPoint(x: w * 0.17, y: h * 0.72), height: h * 0.16)
            drawCornPlant(&ctx, base: CGPoint(x: w * 0.90, y: h * 0.68), height: h * 0.20)

            // Cerro de enfrente (menta)
            var front = Path()
            front.move(to: CGPoint(x: 0, y: h * 0.76))
            front.addCurve(
                to: CGPoint(x: w * 0.62, y: h * 0.71),
                control1: CGPoint(x: w * 0.25, y: h * 0.68),
                control2: CGPoint(x: w * 0.45, y: h * 0.74)
            )
            front.addCurve(
                to: CGPoint(x: w, y: h * 0.70),
                control1: CGPoint(x: w * 0.80, y: h * 0.68),
                control2: CGPoint(x: w * 0.92, y: h * 0.73)
            )
            front.addLine(to: CGPoint(x: w, y: h))
            front.addLine(to: CGPoint(x: 0, y: h))
            front.closeSubpath()
            ctx.fill(front, with: .color(GameTheme.turquoise.opacity(0.13)))

            // Florecitas sueltas
            for (fx, fy) in [(0.26, 0.80), (0.74, 0.78), (0.86, 0.84)] as [(CGFloat, CGFloat)] {
                drawFlower(&ctx, center: CGPoint(x: w * fx, y: h * fy), r: w * 0.012)
            }

            // Desvanecido hacia BackgroundLight abajo
            ctx.fill(
                Path(CGRect(origin: .zero, size: size)),
                with: .linearGradient(
                    Gradient(stops: [
                        .init(color: .clear, location: 0.80),
                        .init(color: GameTheme.backgroundLight, location: 1.0)
                    ]),
                    startPoint: .zero,
                    endPoint: CGPoint(x: 0, y: h)
                )
            )
        }
        .allowsHitTesting(false)
    }

    private func drawCloud(_ ctx: inout GraphicsContext, center: CGPoint, s: CGFloat) {
        let color = Color.white.opacity(0.85)
        ctx.fill(Path(ellipseIn: CGRect(x: center.x - s, y: center.y - s, width: s * 2, height: s * 2)), with: .color(color))
        ctx.fill(Path(ellipseIn: CGRect(x: center.x - s - s * 0.75, y: center.y + s * 0.25 - s * 0.75, width: s * 1.5, height: s * 1.5)), with: .color(color))
        ctx.fill(Path(ellipseIn: CGRect(x: center.x + s - s * 0.75, y: center.y + s * 0.25 - s * 0.75, width: s * 1.5, height: s * 1.5)), with: .color(color))
        ctx.fill(Path(CGRect(x: center.x - s * 1.6, y: center.y + s * 0.1, width: s * 3.2, height: s * 0.9)), with: .color(color))
    }

    private func drawCornPlant(_ ctx: inout GraphicsContext, base: CGPoint, height: CGFloat) {
        let green = Color(red: 0.482, green: 0.776, blue: 0.494).opacity(0.55)
        var stem = Path()
        stem.move(to: base)
        stem.addLine(to: CGPoint(x: base.x, y: base.y - height))
        ctx.stroke(stem, with: .color(green), lineWidth: height * 0.06)

        for k in 0...2 {
            let y = base.y - height * (0.25 + CGFloat(k) * 0.22)
            let dir: CGFloat = (k % 2 == 0) ? 1 : -1
            var leaf = Path()
            leaf.move(to: CGPoint(x: base.x, y: y))
            leaf.addQuadCurve(
                to: CGPoint(x: base.x + dir * height * 0.30, y: y - height * 0.04),
                control: CGPoint(x: base.x + dir * height * 0.22, y: y - height * 0.20)
            )
            leaf.addQuadCurve(
                to: CGPoint(x: base.x, y: y + height * 0.03),
                control: CGPoint(x: base.x + dir * height * 0.12, y: y - height * 0.04)
            )
            leaf.closeSubpath()
            ctx.fill(leaf, with: .color(green))
        }

        let cob = CGRect(x: base.x + height * 0.02, y: base.y - height * 0.62, width: height * 0.09, height: height * 0.20)
        ctx.fill(Path(ellipseIn: cob), with: .color(Color(red: 1.0, green: 0.827, blue: 0.420).opacity(0.75)))
    }

    private func drawFlower(_ ctx: inout GraphicsContext, center: CGPoint, r: CGFloat) {
        let petal = Color(red: 1.0, green: 0.702, blue: 0.812).opacity(0.8)
        for (dx, dy) in [(-r, 0), (r, 0), (0, -r), (0, r)] as [(CGFloat, CGFloat)] {
            ctx.fill(Path(ellipseIn: CGRect(x: center.x + dx - r, y: center.y + dy - r, width: r * 2, height: r * 2)), with: .color(petal))
        }
        let cr = r * 0.8
        ctx.fill(Path(ellipseIn: CGRect(x: center.x - cr, y: center.y - cr, width: cr * 2, height: cr * 2)), with: .color(Color(red: 1.0, green: 0.827, blue: 0.420)))
    }
}
