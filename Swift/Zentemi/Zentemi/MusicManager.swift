import AVFoundation

final class MusicManager {

    static let shared = MusicManager()

    private var audioPlayer: AVAudioPlayer?
    private var currentTrack: String?

    private var savedTimes: [String: TimeInterval] = [:]

    private init() {}

    func playMusic(
        named name: String,
        volume: Float
    ) {

        if currentTrack == name,
           let audioPlayer {

            audioPlayer.volume = volume

            if !audioPlayer.isPlaying {
                audioPlayer.play()
            }

            return
        }

        if let currentTrack,
           let audioPlayer {

            savedTimes[currentTrack] =
                audioPlayer.currentTime
        }

        audioPlayer?.stop()
        audioPlayer = nil

        guard let url = Bundle.main.url(
            forResource: name,
            withExtension: "mp3"
        ) else {
            print("No se encontró \(name).mp3")
            return
        }

        do {

            audioPlayer = try AVAudioPlayer(
                contentsOf: url
            )

            currentTrack = name

            audioPlayer?.numberOfLoops = -1
            audioPlayer?.volume = volume

            if let savedTime = savedTimes[name] {
                audioPlayer?.currentTime = savedTime
            }

            audioPlayer?.prepareToPlay()
            audioPlayer?.play()

        } catch {

            print(
                "Error al reproducir \(name): \(error)"
            )
        }
    }

    func pauseMusic() {

        guard
            let currentTrack,
            let audioPlayer
        else {
            return
        }

        savedTimes[currentTrack] =
            audioPlayer.currentTime

        audioPlayer.pause()
    }

    func setVolume(_ volume: Float) {

        audioPlayer?.volume = volume
    }
    func resumeMusic(volume: Float) {

        guard let audioPlayer else {
            return
        }

        audioPlayer.volume = volume

        if !audioPlayer.isPlaying {
            audioPlayer.play()
        }
    }
}
