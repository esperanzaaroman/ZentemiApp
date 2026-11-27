import SwiftUI

/// Juegos disponibles en "Jugar con Maicito" (espejo exacto de `MaicitoGame` en `GamesHubScreen.kt`).
enum MaicitoGameType: String, CaseIterable, Identifiable {
    case maicitoPide
    case memorama
    case mercado

    var id: String { rawValue }

    var title: String {
        switch self {
        case .maicitoPide: return "Maicito pide"
        case .memorama: return "Memorama"
        case .mercado: return "El mercado de Ixtenco"
        }
    }

    var description: String {
        switch self {
        case .maicitoPide:
            return "Escucha lo que dice Maicito en Yuhmu y elige qué significa."
        case .memorama:
            return "Voltea las cartas y junta cada audio en Yuhmu con su significado."
        case .mercado:
            return "Visita el tianguis con Maicito y platica con el vendedor en Yuhmu."
        }
    }

    var tag: String {
        switch self {
        case .maicitoPide: return "Escuchar"
        case .memorama: return "Memoria"
        case .mercado: return "Historia"
        }
    }

    var icon: String {
        switch self {
        case .maicitoPide: return "ic_maicito_pide"
        case .memorama: return "ic_memorama"
        case .mercado: return "ic_mercado"
        }
    }

    var color: Color {
        switch self {
        case .maicitoPide: return GameTheme.purple
        case .memorama: return GameTheme.turquoise
        case .mercado: return GameTheme.marketOrange
        }
    }
}

/// Pantalla "Jugar con Maicito" con la lista de minijuegos (espejo exacto de `GamesHubScreen.kt`).
struct GamesHubView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var showClassicGames = false
    @State private var selectedGame: MaicitoGameType? = nil

    var body: some View {
        VStack(spacing: 0) {
            GameWaveHeader {
                HStack(spacing: 14) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.white.opacity(0.16))
                            .clipShape(Circle())
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Jugar con \(appState.petName)")
                            .font(.system(size: 28, weight: .bold))
                            .foregroundStyle(.white)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)

                        Text("Repasa Yuhmu jugando")
                            .font(.system(size: 15, weight: .medium))
                            .foregroundStyle(.white.opacity(0.82))
                    }
                }
            }

            ScrollView(showsIndicators: false) {
                VStack(spacing: 14) {
                    maicitoSpeechRow
                        .padding(.bottom, 4)

                    Button {
                        showClassicGames = true
                    } label: {
                        Label("Laberinto, Memorama y Sopa de letras", systemImage: "gamecontroller.fill")
                            .font(.headline)
                            .foregroundStyle(GameTheme.purple)
                            .padding()
                            .frame(maxWidth: .infinity)
                            .background(.white, in: RoundedRectangle(cornerRadius: 20))
                    }

                    ForEach(MaicitoGameType.allCases) { game in
                        gameCard(game)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 28)
            }
        }
        .background(GameTheme.backgroundLight.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .fullScreenCover(isPresented: $showClassicGames) {
            MaicitoGamesView()
                .environmentObject(appState)
        }
        .fullScreenCover(item: $selectedGame) { game in
            switch game {
            case .maicitoPide:
                MaicitoPideGameView()
                    .environmentObject(appState)
            case .memorama:
                MemoramaGameView()
                    .environmentObject(appState)
            case .mercado:
                MercadoGameView()
                    .environmentObject(appState)
            }
        }
    }

    // MARK: - Globo de diálogo de Maicito

    private var maicitoSpeechRow: some View {
        HStack(alignment: .center, spacing: 0) {
            PetView(size: 78)

            SpeechBubbleTail()
                .fill(GameTheme.bubbleLilac)
                .frame(width: 10, height: 18)

            VStack(alignment: .leading, spacing: 8) {
                Text(
                    appState.gamesUnlocked
                        ? "¡Juguemos y practiquemos!"
                        : "¡Primero aprendamos juntos!"
                )
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(GameTheme.purpleDark)

                if appState.gamesUnlocked {
                    HStack(spacing: 6) {
                        ZentemiIconView(name: "ic_regalo", size: 20)
                        Text("Recompensas de hoy: \(appState.gameRewardsLeft)/\(appState.maxGameRewardsPerDay)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(Color.white)
                    .clipShape(Capsule())

                    Text("Cada partida sube la felicidad de \(appState.petName).")
                        .font(.system(size: 12))
                        .foregroundStyle(GameTheme.subText)
                } else {
                    Text("Termina la lección «Primeras frases» para desbloquear los juegos.")
                        .font(.system(size: 13))
                        .foregroundStyle(GameTheme.subText)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(GameTheme.bubbleLilac)
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
    }

    // MARK: - Tarjeta de Juego

    private func gameCard(_ game: MaicitoGameType) -> some View {
        let unlocked = appState.gamesUnlocked

        return Button {
            guard unlocked else { return }
            selectedGame = game
        } label: {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(unlocked ? game.color.opacity(0.14) : GameTheme.lockedNode)
                        .frame(width: 58, height: 58)

                    ZentemiIconView(
                        name: unlocked ? game.icon : "ic_leccion_bloqueada",
                        size: 44
                    )
                }

                VStack(alignment: .leading, spacing: 5) {
                    GameTagChip(text: game.tag, color: game.color)

                    Text(game.title)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(unlocked ? GameTheme.darkText : GameTheme.darkText.opacity(0.5))
                        .multilineTextAlignment(.leading)

                    Text(game.description)
                        .font(.system(size: 13))
                        .foregroundStyle(unlocked ? GameTheme.subText : GameTheme.subText.opacity(0.5))
                        .multilineTextAlignment(.leading)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer(minLength: 4)

                if unlocked {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(GameTheme.purple.opacity(0.6))
                }
            }
            .padding(16)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .shadow(color: .black.opacity(0.05), radius: 5, y: 2)
        }
        .buttonStyle(.plain)
        .disabled(!unlocked)
    }
}

private struct SpeechBubbleTail: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}

#Preview {
    GamesHubView()
        .environmentObject(AppState())
}
