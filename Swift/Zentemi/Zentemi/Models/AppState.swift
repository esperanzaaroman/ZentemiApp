import SwiftUI
import Combine

class AppState: ObservableObject {
    
    // Usuario
    @Published var userName = "Jugador"
    @Published var lastUserNameChange: Date? = nil

    let userNameChangeCooldown: TimeInterval = 7 * 24 * 60 * 60

    var canChangeUserName: Bool {

        guard let lastUserNameChange else {
            return true
        }

        return Date().timeIntervalSince(lastUserNameChange)
            >= userNameChangeCooldown
    }

    var daysUntilUserNameChange: Int {

        guard let lastUserNameChange else {
            return 0
        }

        let remaining =
            userNameChangeCooldown
            - Date().timeIntervalSince(lastUserNameChange)

        return max(
            0,
            Int(ceil(remaining / 86400))
        )
    }

    func changeUserName(to newName: String) {

        let cleanName =
            newName.trimmingCharacters(
                in: .whitespacesAndNewlines
            )

        guard
            canChangeUserName,
            !cleanName.isEmpty,
            cleanName != userName
        else {
            return
        }

        userName = cleanName
        lastUserNameChange = Date()
    }
    
    // Mascota
    @Published var petName = "Maicito"
    @Published var happiness = 99
    
    // Perfil
    @Published var coins = 1500
    @Published var streak = 5
    
    @Published var lessonsCompleted = 0
    let totalLessons = 30
    
    // MARK: - NIVEL Y EXPERIENCIA
    
    @Published var currentLevel = 1
    @Published var totalXP = 0
    
    let xpRequiredPerLevel = 100
    let maxLevel = 10
    
    // Personalización del avatar
    @Published var avatarBackgroundHue: Double = 0.72
    @Published var mascotHue: Double = 0.0
    
    // Color del fondo superior del perfil
    @Published var profileBackgroundHue: Double = 0.73
    
    // MARK: - Accesorios equipados
    
    @Published var glassesEquipped = false
    @Published var hairEquipped = false
    @Published var scarfEquipped = false
    
    // MARK: - Objetos comprados en tienda
    
    @Published var sunglassesPurchased = false
    @Published var pinkHairPurchased = false
    @Published var swordPurchased = false
    @Published var suitPurchased = false
    
    // MARK: - Nuevos accesorios equipados
    
    @Published var sunglassesEquipped = false
    @Published var pinkHairEquipped = false
    @Published var swordEquipped = false
    @Published var suitEquipped = false
    
    // MARK: - EQUIPAR OBJETOS POR CATEGORÍA
    
    // CARA
    func toggleGlasses() {
        
        if glassesEquipped {
            glassesEquipped = false
        } else {
            sunglassesEquipped = false
            glassesEquipped = true
        }
    }
    
    func toggleSunglasses() {
        
        if sunglassesEquipped {
            sunglassesEquipped = false
        } else {
            glassesEquipped = false
            sunglassesEquipped = true
        }
    }
    
    // CABELLO
    func toggleHair() {
        
        if hairEquipped {
            hairEquipped = false
        } else {
            pinkHairEquipped = false
            hairEquipped = true
        }
    }
    
    func togglePinkHair() {
        
        if pinkHairEquipped {
            pinkHairEquipped = false
        } else {
            hairEquipped = false
            pinkHairEquipped = true
        }
    }
    
    // ACCESORIOS
    func toggleScarf() {
        
        if scarfEquipped {
            scarfEquipped = false
        } else {
            swordEquipped = false
            scarfEquipped = true
        }
    }
    
    func toggleSword() {
        
        if swordEquipped {
            swordEquipped = false
        } else {
            scarfEquipped = false
            swordEquipped = true
        }
    }
    
    // ROPA
    func toggleSuit() {
        suitEquipped.toggle()
    }
    
    // MARK: - PROGRESO DEL CAMINO
    
    @Published var pathLessonsCompleted = 0
    @Published var unlockedPathLesson = 1
    
    let greetingsExerciseCount = 1
    let familyExerciseCount = 0
    
    // Evita entregar dos veces la recompensa de Saludos
    @Published var greetingsRewardClaimed = false
    
    // MARK: - RECOMPENSAS
    
    func addCoins(_ amount: Int) {
        coins += amount
    }
    
    func addXP(_ amount: Int) {
        
        guard amount > 0 else {
            return
        }
        
        totalXP += amount
        
        let calculatedLevel =
            (totalXP / xpRequiredPerLevel) + 1
        
        currentLevel = min(
            calculatedLevel,
            maxLevel
        )
    }
    
    func completeGreetingsLesson() {
        
        guard !greetingsRewardClaimed else {
            return
        }
        
        greetingsRewardClaimed = true
        
        addCoins(700)
        addXP(140)
        
        pathLessonsCompleted = max(
            pathLessonsCompleted,
            1
        )
        
        unlockedPathLesson = max(
            unlockedPathLesson,
            2
        )
        
        lessonsCompleted += 1
    }
    
    // MARK: - Animación compra → mochila
    
    @Published var purchasedItemAnimation: String? = nil
    @Published var purchaseAnimationID = UUID()
    @Published var backpackPopID = UUID()
    
    // MARK: - AUDIO
    
    @Published var musicVolume: Double = 0.35 {
        didSet {

            if musicVolume > 0 {

                MusicManager.shared.resumeMusic(
                    volume: Float(musicVolume)
                )

            } else {

                MusicManager.shared.pauseMusic()
            }
        }
    }

    @Published var soundVolume: Double = 1.0
    
    // MARK: - EXPERIENCIA CALCULADA
    
    var currentLevelXP: Int {
        
        if currentLevel >= maxLevel {
            return xpRequiredPerLevel
        }
        
        return totalXP % xpRequiredPerLevel
    }
    
    var levelProgress: Double {
        
        if currentLevel >= maxLevel {
            return 1.0
        }
        
        return Double(currentLevelXP)
            / Double(xpRequiredPerLevel)
    }
    
    var xpRemaining: Int {
        
        if currentLevel >= maxLevel {
            return 0
        }
        
        return xpRequiredPerLevel - currentLevelXP
    }
}
