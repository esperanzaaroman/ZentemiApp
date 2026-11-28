import Foundation
import AVFoundation
import Combine

/// Una palabra o frase en Yuhmu con su grabación e ícono ilustrado.
/// Espejo exacto de `YuhmuVocabulary.kt` en Android.
struct YuhmuWord: Identifiable, Equatable {
    let id: String
    let spanish: String
    /// Texto escrito en Yuhmu. Es nil cuando aún no está confirmada la escritura;
    /// en ese caso los juegos solo usan el audio.
    let yuhmu: String?
    /// Nombre del archivo .m4a en el bundle (ej. "buenosDias").
    let audio: String
    /// Nombre del imageset en Assets.xcassets (ej. "ic_buenos_dias").
    let icon: String
    /// Lección donde se aprende.
    let lesson: Int
}

enum YuhmuVocabulary {

    static let all: [YuhmuWord] = [
        YuhmuWord(
            id: "buenos_dias",
            spanish: "Buenos días",
            yuhmu: "Té xki hats’i",
            audio: "buenosDias",
            icon: "ic_buenos_dias",
            lesson: 1
        ),
        YuhmuWord(
            id: "como_te_llamas",
            spanish: "¿Cómo te llamas?",
            yuhmu: nil,
            audio: "comoTeLlamas",
            icon: "ic_como_te_llamas",
            lesson: 1
        ),
        YuhmuWord(
            id: "cuantos_anios",
            spanish: "¿Cuántos años tienes?",
            yuhmu: nil,
            audio: "cuantosAniosTienes",
            icon: "ic_cuantos_anios",
            lesson: 1
        ),
        YuhmuWord(
            id: "ayudame",
            spanish: "Ayúdame por favor",
            yuhmu: nil,
            audio: "ayudamePorfavor",
            icon: "ic_ayudame",
            lesson: 1
        )
    ]

    static func byId(_ id: String) -> YuhmuWord {
        all.first { $0.id == id } ?? all[0]
    }

    /// Palabras de las lecciones que ya se completaron.
    static func unlocked(lessonsCompleted: Int) -> [YuhmuWord] {
        all.filter { $0.lesson <= lessonsCompleted }
    }
}

/// Reproductor de frases en Yuhmu (una a la vez) con estado `isPlaying` observable.
/// Espejo exacto de `PhrasePlayer` en `SoundManager.kt` de Android.
final class PhrasePlayer: NSObject, ObservableObject, AVAudioPlayerDelegate {

    @Published private(set) var isPlaying: Bool = false

    private var player: AVAudioPlayer?

    func play(_ resourceName: String, volume: Double) {
        stop()
        guard volume > 0 else { return }

        let url = Bundle.main.url(forResource: resourceName, withExtension: "m4a")
            ?? Bundle.main.url(forResource: resourceName, withExtension: "m4a", subdirectory: "audios")

        guard let audioURL = url else {
            print("[PhrasePlayer] No se encontró el audio \(resourceName).m4a")
            return
        }

        do {
            let newPlayer = try AVAudioPlayer(contentsOf: audioURL)
            newPlayer.delegate = self
            newPlayer.volume = Float(volume)
            newPlayer.prepareToPlay()
            player = newPlayer
            isPlaying = true
            newPlayer.play()
        } catch {
            print("[PhrasePlayer] Error al reproducir \(resourceName): \(error.localizedDescription)")
            isPlaying = false
        }
    }

    func stop() {
        if let player {
            if player.isPlaying {
                player.stop()
            }
        }
        player = nil
        if isPlaying {
            isPlaying = false
        }
    }

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            if self.player === player {
                self.player = nil
            }
            self.isPlaying = false
        }
    }

    deinit {
        stop()
    }
}
