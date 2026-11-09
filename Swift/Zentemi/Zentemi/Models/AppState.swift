
import SwiftUI
import Combine

class AppState: ObservableObject {

    // MARK: - ALMACENAMIENTO LOCAL

    private let storage = UserDefaults.standard
    private var isLoading = false

    private func save<T>(_ value: T, key: String) {
        guard !isLoading else { return }
        storage.set(value, forKey: "zentemi.\(key)")
    }

    private func loadString(_ key: String, default value: String) -> String {
        storage.string(forKey: "zentemi.\(key)") ?? value
    }

    private func loadInt(_ key: String, default value: Int) -> Int {
        guard storage.object(forKey: "zentemi.\(key)") != nil else {
            return value
        }
        return storage.integer(forKey: "zentemi.\(key)")
    }

    private func loadDouble(_ key: String, default value: Double) -> Double {
        guard storage.object(forKey: "zentemi.\(key)") != nil else {
            return value
        }
        return storage.double(forKey: "zentemi.\(key)")
    }

    private func loadBool(_ key: String, default value: Bool) -> Bool {
        guard storage.object(forKey: "zentemi.\(key)") != nil else {
            return value
        }
        return storage.bool(forKey: "zentemi.\(key)")
    }

    private func loadDate(_ key: String) -> Date? {
        storage.object(forKey: "zentemi.\(key)") as? Date
    }

    // MARK: - USUARIO

    @Published var userName = "Jugador" {
        didSet { save(userName, key: "userName") }
    }

    @Published var lastUserNameChange: Date? = nil {
        didSet { save(lastUserNameChange, key: "lastUserNameChange") }
    }

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

        return max(0, Int(ceil(remaining / 86400)))
    }

    func changeUserName(to newName: String) {
        let cleanName = newName.trimmingCharacters(
            in: .whitespacesAndNewlines
        )

        guard canChangeUserName,
              !cleanName.isEmpty,
              cleanName != userName else {
            return
        }

        userName = cleanName
        lastUserNameChange = Date()
    }

    // MARK: - MASCOTA

    @Published var petName = "Maicito" {
        didSet { save(petName, key: "petName") }
    }

    @Published var happiness = 50 {
        didSet { save(happiness, key: "happiness") }
    }
    
    // MARK: - RACHA DIARIA

    private var streakCalendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "America/Mexico_City")!
        return calendar
    }

    func updateDailyStreak() {
        let today = streakCalendar.startOfDay(for: Date())
        let key = "zentemi.lastStreakDate"

        if let previous = storage.object(forKey: key) as? Date {
            let lastDay = streakCalendar.startOfDay(for: previous)

            let days = streakCalendar.dateComponents(
                [.day],
                from: lastDay,
                to: today
            ).day ?? 0

            if days <= 0 {
                return
            }

            streak = days == 1 ? streak + 1 : 1
        } else {
            streak = 1
        }

        storage.set(today, forKey: key)
    }

    // MARK: - PERFIL

    @Published var coins = 1500 {
        didSet { save(coins, key: "coins") }
    }

    @Published var streak = 0 {
        didSet { save(streak, key: "streak") }
    }
    @Published var lessonsCompleted = 0 {
        didSet { save(lessonsCompleted, key: "lessonsCompleted") }
    }

    let totalLessons = 30

    // MARK: - NIVEL Y EXPERIENCIA

    @Published var currentLevel = 1 {
        didSet { save(currentLevel, key: "currentLevel") }
    }

    @Published var totalXP = 0 {
        didSet { save(totalXP, key: "totalXP") }
    }

    let xpRequiredPerLevel = 100
    let maxLevel = 10

    // MARK: - PERSONALIZACIÓN

    @Published var avatarBackgroundHue: Double = 0.72 {
        didSet {
            save(avatarBackgroundHue, key: "avatarBackgroundHue")
        }
    }

    @Published var mascotHue: Double = 0.0 {
        didSet { save(mascotHue, key: "mascotHue") }
    }

    @Published var profileBackgroundHue: Double = 0.73 {
        didSet {
            save(profileBackgroundHue, key: "profileBackgroundHue")
        }
    }

    // MARK: - ACCESORIOS EQUIPADOS

    @Published var glassesEquipped = false {
        didSet { save(glassesEquipped, key: "glassesEquipped") }
    }

    @Published var hairEquipped = false {
        didSet { save(hairEquipped, key: "hairEquipped") }
    }

    @Published var scarfEquipped = false {
        didSet { save(scarfEquipped, key: "scarfEquipped") }
    }

    // MARK: - OBJETOS COMPRADOS

    @Published var sunglassesPurchased = false {
        didSet {
            save(sunglassesPurchased, key: "sunglassesPurchased")
        }
    }

    @Published var pinkHairPurchased = false {
        didSet {
            save(pinkHairPurchased, key: "pinkHairPurchased")
        }
    }

    @Published var swordPurchased = false {
        didSet { save(swordPurchased, key: "swordPurchased") }
    }

    @Published var suitPurchased = false {
        didSet { save(suitPurchased, key: "suitPurchased") }
    }

    // MARK: - NUEVOS ACCESORIOS EQUIPADOS

    @Published var sunglassesEquipped = false {
        didSet {
            save(sunglassesEquipped, key: "sunglassesEquipped")
        }
    }

    @Published var pinkHairEquipped = false {
        didSet {
            save(pinkHairEquipped, key: "pinkHairEquipped")
        }
    }

    @Published var swordEquipped = false {
        didSet { save(swordEquipped, key: "swordEquipped") }
    }

    @Published var suitEquipped = false {
        didSet { save(suitEquipped, key: "suitEquipped") }
    }

    // MARK: - EQUIPAR OBJETOS POR CATEGORÍA

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

    func toggleSuit() {
        suitEquipped.toggle()
    }

    // MARK: - PROGRESO DEL CAMINO

    @Published var pathLessonsCompleted = 0 {
        didSet {
            save(pathLessonsCompleted, key: "pathLessonsCompleted")
        }
    }

    @Published var unlockedPathLesson = 1 {
        didSet {
            save(unlockedPathLesson, key: "unlockedPathLesson")
        }
    }

    let greetingsExerciseCount = 5
    let familyExerciseCount = 0

    @Published var greetingsRewardClaimed = false {
        didSet {
            save(greetingsRewardClaimed, key: "greetingsRewardClaimed")
        }
    }

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

        currentLevel = min(calculatedLevel, maxLevel)
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

    // MARK: - ANIMACIÓN COMPRA A MOCHILA

    // Estos valores son temporales y no necesitan guardarse.

    @Published var purchasedItemAnimation: String? = nil
    @Published var purchaseAnimationID = UUID()
    @Published var backpackPopID = UUID()

    // MARK: - AUDIO

    @Published var musicVolume: Double = 0.35 {
        didSet {
            save(musicVolume, key: "musicVolume")

            if !isLoading {
                if musicVolume > 0 {
                    MusicManager.shared.resumeMusic(
                        volume: Float(musicVolume)
                    )
                } else {
                    MusicManager.shared.pauseMusic()
                }
            }
        }
    }

    @Published var soundVolume: Double = 1.0 {
        didSet { save(soundVolume, key: "soundVolume") }
    }

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

    // MARK: - INICIALIZACIÓN

    init() {
        loadSavedData()
        updateDailyStreak()
    }

    // MARK: - RECUPERAR DATOS GUARDADOS

    private func loadSavedData() {
        isLoading = true

        // Usuario
        userName = loadString("userName", default: userName)
        lastUserNameChange = loadDate("lastUserNameChange")

        // Mascota
        petName = loadString("petName", default: petName)
        happiness = loadInt("happiness", default: happiness)

        // Perfil
        coins = loadInt("coins", default: coins)
        streak = loadInt("streak", default: streak)
        lessonsCompleted = loadInt(
            "lessonsCompleted",
            default: lessonsCompleted
        )

        // Nivel
        currentLevel = loadInt(
            "currentLevel",
            default: currentLevel
        )

        totalXP = loadInt("totalXP", default: totalXP)

        // Personalización
        avatarBackgroundHue = loadDouble(
            "avatarBackgroundHue",
            default: avatarBackgroundHue
        )

        mascotHue = loadDouble(
            "mascotHue",
            default: mascotHue
        )

        profileBackgroundHue = loadDouble(
            "profileBackgroundHue",
            default: profileBackgroundHue
        )

        // Accesorios equipados
        glassesEquipped = loadBool(
            "glassesEquipped",
            default: glassesEquipped
        )

        hairEquipped = loadBool(
            "hairEquipped",
            default: hairEquipped
        )

        scarfEquipped = loadBool(
            "scarfEquipped",
            default: scarfEquipped
        )

        // Compras
        sunglassesPurchased = loadBool(
            "sunglassesPurchased",
            default: sunglassesPurchased
        )

        pinkHairPurchased = loadBool(
            "pinkHairPurchased",
            default: pinkHairPurchased
        )

        swordPurchased = loadBool(
            "swordPurchased",
            default: swordPurchased
        )

        suitPurchased = loadBool(
            "suitPurchased",
            default: suitPurchased
        )

        // Nuevos accesorios equipados
        sunglassesEquipped = loadBool(
            "sunglassesEquipped",
            default: sunglassesEquipped
        )

        pinkHairEquipped = loadBool(
            "pinkHairEquipped",
            default: pinkHairEquipped
        )

        swordEquipped = loadBool(
            "swordEquipped",
            default: swordEquipped
        )

        suitEquipped = loadBool(
            "suitEquipped",
            default: suitEquipped
        )

        // Lecciones
        pathLessonsCompleted = loadInt(
            "pathLessonsCompleted",
            default: pathLessonsCompleted
        )

        unlockedPathLesson = loadInt(
            "unlockedPathLesson",
            default: unlockedPathLesson
        )

        greetingsRewardClaimed = loadBool(
            "greetingsRewardClaimed",
            default: greetingsRewardClaimed
        )

        // Audio
        musicVolume = loadDouble(
            "musicVolume",
            default: musicVolume
        )

        soundVolume = loadDouble(
            "soundVolume",
            default: soundVolume
        )

        isLoading = false
    }
    
    
    // MARK: - REINICIAR DATOS DE DESARROLLO

    #if DEBUG
    func resetDevelopmentProgress() {

        // Eliminar datos locales de Zentemi
        let keys = storage.dictionaryRepresentation().keys

        for key in keys where key.hasPrefix("zentemi.") {
            storage.removeObject(forKey: key)
        }

        // Restaurar los valores iniciales
        isLoading = true

        userName = "Jugador"
        lastUserNameChange = nil

        petName = "Maicito"
        happiness = 50

        coins = 1500
        streak = 0
        lessonsCompleted = 0

        currentLevel = 1
        totalXP = 0

        avatarBackgroundHue = 0.72
        mascotHue = 0.0
        profileBackgroundHue = 0.73

        glassesEquipped = false
        hairEquipped = false
        scarfEquipped = false

        sunglassesPurchased = false
        pinkHairPurchased = false
        swordPurchased = false
        suitPurchased = false

        sunglassesEquipped = false
        pinkHairEquipped = false
        swordEquipped = false
        suitEquipped = false

        pathLessonsCompleted = 0
        unlockedPathLesson = 1
        greetingsRewardClaimed = false

        purchasedItemAnimation = nil
        purchaseAnimationID = UUID()
        backpackPopID = UUID()

        musicVolume = 0.35
        soundVolume = 1.0

        isLoading = false

        // Registrar el primer día de la nueva cuenta
        updateDailyStreak()
    }
    #endif

}
