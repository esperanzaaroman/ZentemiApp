import AVFoundation

/// Tonos provisionales generados en memoria, sin archivos de audio adicionales.
@MainActor
final class LessonResultSounds {
    private var players: [AVAudioPlayer] = []

    func playStar(_ index: Int, volume: Float) {
        play(frequencies: [660, 830, 990].prefix(max(1, min(index, 3))).map { Double($0) },
             duration: 0.22, rising: false, volume: volume)
    }

    func playCoin(volume: Float) {
        play(frequencies: [1046, 1568], duration: 0.18, rising: false, volume: volume)
    }

    func playCard(volume: Float) {
        play(frequencies: [740, 990], duration: 0.12, rising: false, volume: volume)
    }

    func playExperienceFill(volume: Float) {
        play(frequencies: [440], duration: 1.4, rising: true, volume: volume)
    }

    func playCompletionPulse(_ step: Int, volume: Float) {
        play(frequencies: [220 + Double(min(max(step, 0), 4)) * 110],
             duration: 0.18, rising: false, volume: volume)
    }

    func playMazeCompletion(volume: Float) {
        play(frequencies: [523.25, 659.25, 783.99, 1046.5],
             duration: 1.0, rising: false, volume: volume)
    }

    func stop() {
        players.forEach { $0.stop() }
        players.removeAll()
    }

    private func play(frequencies: [Double], duration: Double, rising: Bool, volume: Float) {
        guard volume > 0 else { return }

        do {
            let player = try AVAudioPlayer(data: waveData(
                frequencies: frequencies, duration: duration, rising: rising
            ))
            player.volume = min(volume, 1)
            players.removeAll { !$0.isPlaying }
            players.append(player)
            player.play()
        } catch {
            print("No se pudo reproducir el tono de resultados: \(error)")
        }
    }

    private func waveData(frequencies: [Double], duration: Double, rising: Bool) -> Data {
        let sampleRate = 22_050
        let sampleCount = Int(Double(sampleRate) * duration)
        let byteCount = UInt32(sampleCount * 2)
        var data = Data()

        func appendText(_ text: String) {
            data.append(contentsOf: text.utf8)
        }

        func appendInteger<T: FixedWidthInteger>(_ number: T) {
            var littleEndian = number.littleEndian
            withUnsafeBytes(of: &littleEndian) { data.append(contentsOf: $0) }
        }

        // WAV PCM mono de 16 bits, con ataque y salida suaves para evitar clics.
        appendText("RIFF")
        appendInteger(UInt32(36) + byteCount)
        appendText("WAVEfmt ")
        appendInteger(UInt32(16))
        appendInteger(UInt16(1))
        appendInteger(UInt16(1))
        appendInteger(UInt32(sampleRate))
        appendInteger(UInt32(sampleRate * 2))
        appendInteger(UInt16(2))
        appendInteger(UInt16(16))
        appendText("data")
        appendInteger(byteCount)

        for sample in 0..<sampleCount {
            let time = Double(sample) / Double(sampleRate)
            let attack = min(time / 0.012, 1)
            let release = min((duration - time) / 0.05, 1)
            let envelope = attack * release
            var signal = 0.0
            for frequency in frequencies {
                let cycles = rising
                    ? frequency * time + (660 / duration) * time * time / 2
                    : frequency * time
                signal += sin(2 * Double.pi * cycles)
            }
            signal /= Double(frequencies.count)
            appendInteger(Int16(signal * envelope * 0.20 * Double(Int16.max)))
        }

        return data
    }
}
