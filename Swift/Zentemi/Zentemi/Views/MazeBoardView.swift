import SwiftUI

struct MazeBoardView: View {
    let session: MazeGameSession

    var body: some View {
        GeometryReader { geometry in
            let layout = MazeBoardLayout(level: session.level, size: geometry.size)

            ZStack(alignment: .topLeading) {
                MazeWallsView(level: session.level)
                    .frame(width: layout.width, height: layout.height)
                    .offset(x: layout.sidePadding, y: 48)

                ForEach(session.level.exits) { exit in
                    MazeExitSign(exit: exit, tile: layout.tile)
                        .position(layout.signPosition(exit))
                }

                ForEach(session.remainingCoins) { coin in
                    MazeFloatingCoinView(size: layout.tile * 0.65)
                        .position(layout.screenPosition(coin.position))
                }

                if session.level.startsInside {
                    Image(systemName: "location.circle")
                        .font(.system(size: layout.tile * 0.85))
                        .foregroundStyle(Color.blue.opacity(0.55))
                        .position(layout.screenPosition(session.level.start))
                        .accessibilityLabel("Punto de inicio")
                }

                PetView(size: layout.tile * MazeLevel.petRadius * 2)
                    .frame(width: layout.tile * 0.7, height: layout.tile * 0.7)
                    .position(layout.screenPosition(session.position))
                    .allowsHitTesting(false)

                if let coin = session.lastCollectedCoin {
                    MazeCoinPickupView()
                        .id(coin.id)
                        .position(layout.screenPosition(coin.position))
                        .allowsHitTesting(false)
                }

                if !session.level.startsInside {
                    Text("ENTRADA")
                        .font(.caption2.bold())
                        .foregroundStyle(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 3)
                        .background(Color(red: 0.26, green: 0.39, blue: 0.12), in: Capsule())
                        .position(x: layout.sidePadding + session.level.start.x * layout.tile,
                                  y: layout.height + 61)
                }
            }
            .frame(width: layout.width + layout.sidePadding * 2, height: layout.contentHeight)
            .position(
                x: geometry.size.width / 2,
                y: layout.cameraCenterY(
                    player: session.position,
                    viewportHeight: geometry.size.height,
                    followsPlayer: session.level.followsPlayerVertically
                )
            )
        }
        .clipped()
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Laberinto con \(session.level.exits.count) salidas")
    }
}

private struct MazeBoardLayout {
    let tile: CGFloat
    let sidePadding: CGFloat
    let width: CGFloat
    let height: CGFloat
    let bottomPadding: CGFloat
    var contentHeight: CGFloat { height + 48 + bottomPadding }

    init(level: MazeLevel, size: CGSize) {
        sidePadding = level.exits.contains(where: { $0.side == .left || $0.side == .right }) ? 30 : 0
        bottomPadding = level.exits.contains(where: { $0.side == .bottom }) ? 48 : 24
        let widthTile = (size.width - sidePadding * 2) / CGFloat(level.columns)
        tile = max(1, level.followsPlayerVertically
                   ? widthTile
                   : min(widthTile, (size.height - 72) / CGFloat(level.rows)))
        width = tile * CGFloat(level.columns)
        height = tile * CGFloat(level.rows)
    }

    /// Desplaza todo el mundo (muros, monedas, letreros y mascota) con el mismo
    /// origen. No cambia las coordenadas usadas por las colisiones.
    func cameraCenterY(player: CGPoint, viewportHeight: CGFloat, followsPlayer: Bool) -> CGFloat {
        guard followsPlayer, contentHeight > viewportHeight else { return viewportHeight / 2 }
        let desiredOffset = 48 + player.y * tile - viewportHeight / 2
        let offset = min(max(desiredOffset, 0), contentHeight - viewportHeight)
        return contentHeight / 2 - offset
    }

    func screenPosition(_ point: CGPoint) -> CGPoint {
        CGPoint(x: sidePadding + point.x * tile, y: 48 + point.y * tile)
    }

    func signPosition(_ exit: MazeExit) -> CGPoint {
        switch exit.side {
        case .top: return CGPoint(x: sidePadding + exit.center.x * tile, y: 22)
        case .bottom: return CGPoint(x: sidePadding + exit.center.x * tile, y: height + 70)
        case .left: return CGPoint(x: sidePadding / 2, y: 48 + exit.center.y * tile)
        case .right: return CGPoint(x: sidePadding + width + sidePadding / 2,
                                   y: 48 + exit.center.y * tile)
        }
    }
}

private struct MazeExitSign: View {
    let exit: MazeExit
    let tile: CGFloat

    private var horizontalEdge: Bool { exit.side == .top || exit.side == .bottom }

    var body: some View {
        VStack(spacing: 1) {
            if exit.side == .bottom {
                Image(systemName: "arrowtriangle.up.fill")
                    .font(.system(size: 8))
            }
            Text(exit.translation)
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .lineLimit(2)
                .minimumScaleFactor(0.65)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, minHeight: horizontalEdge ? 28 : 18)
                .background(.white, in: RoundedRectangle(cornerRadius: 8))
            if exit.side != .bottom {
                Image(systemName: "arrowtriangle.down.fill")
                    .font(.system(size: 8))
            }
        }
        .foregroundStyle(Color(red: 0.35, green: 0.17, blue: 0.65))
        .frame(width: horizontalEdge ? tile * 2.9 : 100,
               height: horizontalEdge ? 44 : 28)
        .rotationEffect(.degrees(horizontalEdge ? 0 : exit.side == .left ? -90 : 90))
    }
}

private struct MazeWallsView: View {
    let level: MazeLevel
    var body: some View {
        Canvas { context, size in
            let tile = size.width / CGFloat(level.columns)
            for row in 0..<level.rows {
                for column in 0..<level.columns {
                    let rect = CGRect(x: CGFloat(column) * tile, y: CGFloat(row) * tile,
                                      width: tile, height: tile)
                    if level.isWall(column: column, row: row) && level.usesLava {
                        // Cubos sólidos: la base y las grietas respetan la celda de colisión.
                        context.fill(Path(rect), with: .color(Color(red: 0.78, green: 0.19, blue: 0.04)))
                        let block = rect.insetBy(dx: tile * 0.055, dy: tile * 0.055)
                        context.fill(Path(roundedRect: block, cornerRadius: tile * 0.09),
                                     with: .color(Color(red: 0.23, green: 0.15, blue: 0.18)))
                        let top = CGRect(x: block.minX + tile * 0.04, y: block.minY + tile * 0.03,
                                         width: block.width - tile * 0.08, height: tile * 0.20)
                        context.fill(Path(roundedRect: top, cornerRadius: tile * 0.04),
                                     with: .color(Color(red: 0.40, green: 0.23, blue: 0.22)))
                        var crack = Path()
                        let shift = CGFloat((column + row) % 3) * tile * 0.08
                        crack.move(to: CGPoint(x: rect.midX + shift, y: rect.minY + tile * 0.08))
                        crack.addLine(to: CGPoint(x: rect.midX - tile * 0.16, y: rect.midY))
                        crack.addLine(to: CGPoint(x: rect.midX + tile * 0.15, y: rect.midY + tile * 0.17))
                        crack.addLine(to: CGPoint(x: rect.midX - shift, y: rect.maxY - tile * 0.08))
                        context.stroke(crack, with: .color(.orange), lineWidth: max(1.5, tile * 0.10))
                        context.stroke(crack, with: .color(.yellow), lineWidth: max(0.7, tile * 0.035))
                    } else if level.isWall(column: column, row: row) && level.usesIce {
                        // El hielo ocupa toda la celda sólida; brillos y grietas
                        // quedan dentro del mismo borde usado por la colisión.
                        context.fill(Path(rect), with: .color(Color(red: 0.28, green: 0.62, blue: 0.78)))
                        let block = rect.insetBy(dx: tile * 0.04, dy: tile * 0.04)
                        context.fill(Path(roundedRect: block, cornerRadius: tile * 0.12),
                                     with: .color(Color(red: 0.60, green: 0.85, blue: 0.94)))
                        var facet = Path()
                        facet.move(to: CGPoint(x: rect.minX + tile * 0.10, y: rect.minY + tile * 0.12))
                        facet.addLine(to: CGPoint(x: rect.maxX - tile * 0.12, y: rect.minY + tile * 0.12))
                        facet.addLine(to: CGPoint(x: rect.minX + tile * 0.24, y: rect.maxY - tile * 0.14))
                        facet.closeSubpath()
                        context.fill(facet, with: .color(.white.opacity(0.45)))
                        var crack = Path()
                        crack.move(to: CGPoint(x: rect.midX, y: rect.minY + tile * 0.18))
                        crack.addLine(to: CGPoint(x: rect.midX - tile * 0.12, y: rect.midY))
                        crack.addLine(to: CGPoint(x: rect.midX + tile * 0.15, y: rect.maxY - tile * 0.13))
                        context.stroke(crack, with: .color(.white.opacity(0.75)), lineWidth: max(0.7, tile * 0.035))
                        let snow = CGRect(x: rect.minX + tile * 0.08, y: rect.minY + tile * 0.04,
                                          width: tile * 0.84, height: tile * 0.10)
                        context.fill(Path(roundedRect: snow, cornerRadius: tile * 0.04),
                                     with: .color(.white.opacity(0.9)))
                    } else if level.isWall(column: column, row: row) && level.usesBrick {
                        // Mortero y ladrillos ocupan exactamente la celda sólida.
                        context.fill(Path(rect), with: .color(Color(red: 0.78, green: 0.68, blue: 0.54)))
                        let mortar = max(1, tile * 0.055)
                        let halfHeight = tile / 2
                        for course in 0..<2 {
                            let y = rect.minY + CGFloat(course) * halfHeight
                            let split = (row + column + course) % 2 == 0 ? tile * 0.35 : tile * 0.65
                            let first = CGRect(x: rect.minX + mortar / 2, y: y + mortar / 2,
                                               width: split - mortar, height: halfHeight - mortar)
                            let second = CGRect(x: rect.minX + split + mortar / 2, y: y + mortar / 2,
                                                width: tile - split - mortar, height: halfHeight - mortar)
                            for brick in [first, second] {
                                context.fill(Path(roundedRect: brick, cornerRadius: tile * 0.035),
                                             with: .color(Color(red: 0.65, green: 0.29 + Double(course) * 0.04, blue: 0.19)))
                                let highlight = CGRect(x: brick.minX + mortar, y: brick.minY,
                                                       width: max(0, brick.width - mortar * 2), height: mortar)
                                context.fill(Path(highlight), with: .color(.white.opacity(0.20)))
                            }
                        }
                    } else if level.isWall(column: column, row: row) && level.usesStone {
                        context.fill(Path(rect), with: .color(Color(red: 0.32, green: 0.34, blue: 0.35)))
                        let stone = rect.insetBy(dx: tile * 0.04, dy: tile * 0.04)
                        let shade = Double((column * 3 + row) % 3) * 0.025
                        context.fill(Path(roundedRect: stone, cornerRadius: tile * 0.17),
                                     with: .color(Color(red: 0.57 + shade, green: 0.59 + shade, blue: 0.60 + shade)))
                        var facet = Path()
                        facet.move(to: CGPoint(x: rect.minX + tile * 0.13, y: rect.minY + tile * 0.16))
                        facet.addLine(to: CGPoint(x: rect.maxX - tile * 0.16, y: rect.minY + tile * 0.13))
                        facet.addLine(to: CGPoint(x: rect.maxX - tile * 0.28, y: rect.midY))
                        facet.addLine(to: CGPoint(x: rect.minX + tile * 0.19, y: rect.midY + tile * 0.12))
                        facet.closeSubpath()
                        context.fill(facet, with: .color(.white.opacity(0.19)))
                        var crack = Path()
                        crack.move(to: CGPoint(x: rect.midX, y: rect.maxY - tile * 0.10))
                        crack.addLine(to: CGPoint(x: rect.midX + tile * 0.12, y: rect.midY + tile * 0.10))
                        crack.addLine(to: CGPoint(x: rect.maxX - tile * 0.10, y: rect.midY))
                        context.stroke(crack, with: .color(.black.opacity(0.18)), lineWidth: max(0.6, tile * 0.035))
                    } else if level.isWall(column: column, row: row) {
                        // La base llena toda la celda sólida. El follaje queda dentro
                        // de ese mismo borde: no hay paredes decorativas sin colisión.
                        context.fill(Path(rect), with: .color(Color(red: 0.15, green: 0.39, blue: 0.15)))
                        let crown = rect.insetBy(dx: tile * 0.035, dy: tile * 0.035)
                        context.fill(Path(roundedRect: crown, cornerRadius: tile * 0.22),
                                     with: .color(Color(red: 0.23, green: 0.55, blue: 0.19)))
                        let leafA = CGRect(x: rect.minX + tile * 0.10, y: rect.minY + tile * 0.09,
                                           width: tile * 0.53, height: tile * 0.48)
                        let leafB = CGRect(x: rect.minX + tile * 0.42, y: rect.minY + tile * 0.32,
                                           width: tile * 0.45, height: tile * 0.47)
                        context.fill(Path(ellipseIn: leafA),
                                     with: .color(Color(red: 0.40, green: 0.68, blue: 0.24)))
                        context.fill(Path(ellipseIn: leafB),
                                     with: .color(Color(red: 0.30, green: 0.61, blue: 0.20)))
                        let glint = CGRect(x: rect.minX + tile * 0.21, y: rect.minY + tile * 0.17,
                                           width: tile * 0.16, height: tile * 0.09)
                        context.fill(Path(ellipseIn: glint), with: .color(.white.opacity(0.22)))
                    } else {
                        context.fill(Path(rect), with: .color(level.usesIce
                            ? Color(red: 0.94, green: 0.98, blue: 1)
                            : level.usesLava
                                ? Color(red: 0.78, green: 0.72, blue: 0.65)
                                : Color(red: 0.94, green: 0.88, blue: 0.66)))
                        let speck = CGRect(x: rect.midX + tile * 0.18, y: rect.midY + tile * 0.19,
                                           width: tile * 0.06, height: tile * 0.04)
                        context.fill(Path(ellipseIn: speck), with: .color(level.usesIce ? .cyan.opacity(0.22) : .brown.opacity(0.18)))
                    }
                }
            }
        }
        .accessibilityHidden(true)
    }
}
