import AVFoundation
import Observation

/// Solo confirma la escucha cuando AVAudioPlayer termina correctamente.
@MainActor
@Observable
final class PhraseSceneAudioPlayer: NSObject, AVAudioPlayerDelegate {
    private(set) var hasFinished = false
    private(set) var errorMessage: String?
    @ObservationIgnored private var player: AVAudioPlayer?

    func play(named name: String, volume: Float) {
        stop()
        hasFinished = false
        errorMessage = nil

        guard let url = Bundle.main.url(forResource: name, withExtension: "m4a")
            ?? Bundle.main.url(forResource: name, withExtension: "m4a", subdirectory: "Audios")
            ?? Bundle.main.url(forResource: name, withExtension: "m4a", subdirectory: "audios") else {
            errorMessage = "No se encontró el audio. Intenta escucharlo de nuevo."
            return
        }

        do {
            let audio = try AVAudioPlayer(contentsOf: url)
            audio.volume = volume
            audio.delegate = self
            player = audio
            if !audio.play() {
                errorMessage = "No se pudo reproducir el audio. Inténtalo de nuevo."
            }
        } catch {
            errorMessage = "No se pudo reproducir el audio. Inténtalo de nuevo."
        }
    }

    func stop() {
        player?.delegate = nil
        player?.stop()
        player = nil
    }

    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        let identifier = ObjectIdentifier(player)
        Task { @MainActor [weak self] in
            guard let self, let current = self.player,
                  ObjectIdentifier(current) == identifier else { return }
            self.hasFinished = flag
            self.errorMessage = flag ? nil : "El audio no terminó. Escúchalo de nuevo."
        }
    }

    nonisolated func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: (any Error)?) {
        let identifier = ObjectIdentifier(player)
        Task { @MainActor [weak self] in
            guard let self, let current = self.player,
                  ObjectIdentifier(current) == identifier else { return }
            self.hasFinished = false
            self.errorMessage = "No se pudo reproducir el audio. Inténtalo de nuevo."
        }
    }
}
