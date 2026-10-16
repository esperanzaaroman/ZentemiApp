import AVFoundation

final class SoundManager {

    static let shared = SoundManager()

    private var players: [AVAudioPlayer] = []

    private init() {}

    func play(
        _ name: String,
        volume: Float
    ) {

        guard volume > 0 else {
            return
        }

        guard let url = Bundle.main.url(
            forResource: name,
            withExtension: "mp3"
        ) else {
            print("No se encontró \(name).mp3")
            return
        }

        do {

            let player = try AVAudioPlayer(
                contentsOf: url
            )

            player.volume = volume
            player.prepareToPlay()
            player.play()

            players.append(player)

            players.removeAll {
                !$0.isPlaying
            }

        } catch {

            print(
                "Error al reproducir \(name): \(error)"
            )
        }
    }
}
