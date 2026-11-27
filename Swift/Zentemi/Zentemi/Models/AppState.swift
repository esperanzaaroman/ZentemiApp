
import SwiftUI
import Combine
import FirebaseFirestore
import FirebaseAuth

class AppState: ObservableObject {

    // MARK: - ALMACENAMIENTO LOCAL Y CLOUD

    private let storage = UserDefaults.standard
    private var isLoading = false

    // MARK: - FIRESTORE & MODO INVITADO

    /// Cuenta con perfil en la nube esperando que el alumno/tutor elija qué progreso conservar.
    public struct PendingProgressChoice: Identifiable {
        public let id = UUID()
        public let parentUID: String
        public let cloudProfile: ChildProfile
    }

    @Published public private(set) var activeParentUID: String? = nil
    @Published public private(set) var activeChildId: String? = nil
    @Published public private(set) var isSyncingWithCloud: Bool = false

    /// Ya completó el onboarding y escribió su nombre; la app abre directo en Inicio.
    @Published var hasCompletedOnboarding: Bool = false {
        didSet {
            guard !isLoading else { return }
            storage.set(hasCompletedOnboarding, forKey: "zentemi.hasCompletedOnboarding")
        }
    }

    /// true mientras no haya sesión iniciada (modo invitado).
    @Published public private(set) var isGuest: Bool = (AuthService.shared.currentUser == nil)

    /// Correo del tutor autenticado, o nil en modo invitado.
    @Published public private(set) var accountEmail: String? = AuthService.shared.userEmail

    /// Si el teléfono tiene progreso de invitado y se inicia sesión con otra cuenta, espera elección.
    @Published public private(set) var pendingProgressChoice: PendingProgressChoice? = nil

    private var boundAccountUid: String = "" {
        didSet {
            guard !isLoading else { return }
            storage.set(boundAccountUid, forKey: "zentemi.boundAccountUid")
        }
    }

    private var accountProgressBaseline: String = "" {
        didSet {
            guard !isLoading else { return }
            storage.set(accountProgressBaseline, forKey: "zentemi.accountProgressBaseline")
        }
    }

    private var lastAccountPromptAnswered: Date? = nil {
        didSet {
            guard !isLoading else { return }
            storage.set(lastAccountPromptAnswered, forKey: "zentemi.lastAccountPromptDismissed")
        }
    }

    private let accountPromptInterval: TimeInterval = 7 * 24 * 60 * 60
    private let initialProgressSignature = "0|1500|0|0|"

    /// true si conviene sugerir crear cuenta (invitado + pasaron 7 días desde la última respuesta).
    public var isAccountSuggestionDue: Bool {
        guard isGuest else { return false }
        guard let last = lastAccountPromptAnswered else { return true }
        return Date().timeIntervalSince(last) >= accountPromptInterval
    }

    /// Se llama al responder al diálogo de sugerencia de cuenta (con cualquiera de los dos botones).
    public func markAccountSuggestionAnswered() {
        lastAccountPromptAnswered = Date()
    }

    private func progressSignature() -> String {
        [
            String(totalXP),
            String(coins),
            String(pathLessonsCompleted),
            String(lessonsCompleted),
            ownedItemIDs().joined(separator: ",")
        ].joined(separator: "|")
    }

    /// Hay progreso hecho sin cuenta que todavía no pertenece a ninguna cuenta.
    private var hasGuestProgress: Bool {
        let baseline = accountProgressBaseline.isEmpty ? initialProgressSignature : accountProgressBaseline
        return progressSignature() != baseline
    }

    private var profileListener: ListenerRegistration?
    private var cancellables = Set<AnyCancellable>()
    private var syncDebounceWorkItem: DispatchWorkItem?
    private var childCreatedAt: Date? = nil
    private var lastSyncedProfile: ChildProfile? = nil

    /// IDs de los 7 artículos originales presentes en iOS y Android
    private static let legacyItemIDs: Set<String> = [
        "glasses", "hair", "scarf", "sunglasses", "pinkHair", "sword", "suit"
    ]

    /// Artículos adicionales (catálogo extendido v1.1) persistidos y sincronizados con Firestore
    @Published var ownedExtraItems: Set<String> = [] {
        didSet {
            guard !isLoading else { return }
            storage.set(ownedExtraItems.sorted(), forKey: "zentemi.ownedExtraItems")
            syncProfileToFirestore()
        }
    }

    @Published var equippedExtraItems: Set<String> = [] {
        didSet {
            guard !isLoading else { return }
            storage.set(equippedExtraItems.sorted(), forKey: "zentemi.equippedExtraItems")
            syncProfileToFirestore()
        }
    }

    // MARK: - CATÁLOGO DE TIENDA / VESTIDOR (50 ARTÍCULOS, ESPEJO DE AppState.kt)

    func isOwned(_ item: ShopItem) -> Bool {
        switch item.id {
        case "glasses", "hair", "scarf":
            return true
        case "sunglasses":
            return sunglassesPurchased
        case "pinkHair":
            return pinkHairPurchased
        case "sword":
            return swordPurchased
        case "suit":
            return suitPurchased
        default:
            return item.price == 0 || ownedExtraItems.contains(item.id)
        }
    }

    func isEquipped(_ item: ShopItem) -> Bool {
        switch item.id {
        case "glasses":
            return glassesEquipped
        case "hair":
            return hairEquipped
        case "scarf":
            return scarfEquipped
        case "sunglasses":
            return sunglassesEquipped
        case "pinkHair":
            return pinkHairEquipped
        case "sword":
            return swordEquipped
        case "suit":
            return suitEquipped
        default:
            return equippedExtraItems.contains(item.id)
        }
    }

    private func setEquipped(_ item: ShopItem, value: Bool) {
        switch item.id {
        case "glasses":
            glassesEquipped = value
        case "hair":
            hairEquipped = value
        case "scarf":
            scarfEquipped = value
        case "sunglasses":
            sunglassesEquipped = value
        case "pinkHair":
            pinkHairEquipped = value
        case "sword":
            swordEquipped = value
        case "suit":
            suitEquipped = value
        default:
            if value {
                equippedExtraItems.insert(item.id)
            } else {
                equippedExtraItems.remove(item.id)
            }
        }
    }

    /// Compra un artículo del catálogo. Devuelve false si no alcanzan las monedas.
    @discardableResult
    func purchase(_ item: ShopItem) -> Bool {
        if isOwned(item) { return true }
        guard coins >= item.price else { return false }
        coins -= item.price

        switch item.id {
        case "sunglasses":
            sunglassesPurchased = true
        case "pinkHair":
            pinkHairPurchased = true
        case "sword":
            swordPurchased = true
        case "suit":
            suitPurchased = true
        default:
            ownedExtraItems.insert(item.id)
        }

        recordInventoryItem(itemId: item.id, purchased: true, equipped: false)
        return true
    }

    /// Equipa o desequipa un artículo; al equipar uno reemplaza lo que había en su misma zona (`slot`).
    func toggleEquip(_ item: ShopItem) {
        guard isOwned(item) else { return }
        if isEquipped(item) {
            setEquipped(item, value: false)
            return
        }
        for other in ShopCatalog.items where other.slot == item.slot && other.id != item.id && isEquipped(other) {
            setEquipped(other, value: false)
        }
        setEquipped(item, value: true)
    }

    /// Quita todos los artículos equipados en las zonas dadas (botón "quitar" en la tienda).
    func unequipSlots(_ slots: Set<ItemSlot>) {
        for item in ShopCatalog.items where slots.contains(item.slot) && isEquipped(item) {
            setEquipped(item, value: false)
        }
    }

    /// Accesorios puestos actualmente, ordenados por capa de dibujo (`ItemSlot`).
    var equippedAccessories: [ShopItem] {
        ShopCatalog.items
            .filter { $0.isAccessory && isEquipped($0) }
            .sorted { $0.slot.rawValue < $1.slot.rawValue }
    }

    /// Fondo decorativo puesto actualmente detrás de Maicito, si hay alguno.
    var equippedBackdrop: ShopItem? {
        ShopCatalog.items.first { $0.slot == .background && isEquipped($0) }
    }

    private func save<T>(_ value: T, key: String) {
        guard !isLoading else { return }
        storage.set(value, forKey: "zentemi.\(key)")
        syncProfileToFirestore()
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

    @Published var lastPracticeDate: Date? = nil {
        didSet { save(lastPracticeDate, key: "lastPracticeDate") }
    }

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
        lastPracticeDate = today
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

    // MARK: - JUEGOS CON MAICITO (RECOMPENSA EN LAS PRIMERAS 3 PARTIDAS DEL DÍA)

    let maxGameRewardsPerDay = 3

    private var gameRewardDay: Int? = nil {
        didSet {
            guard !isLoading else { return }
            if let gameRewardDay {
                storage.set(gameRewardDay, forKey: "zentemi.gameRewardDay")
            } else {
                storage.removeObject(forKey: "zentemi.gameRewardDay")
            }
        }
    }

    @Published var gameRewardsToday: Int = 0 {
        didSet {
            guard !isLoading else { return }
            storage.set(gameRewardsToday, forKey: "zentemi.gameRewardsToday")
        }
    }

    /// Día actual en Ciudad de México contado como días desde 1970 (igual que Android).
    var todayEpochDay: Int {
        let start = streakCalendar.startOfDay(for: Date())
        let epoch = streakCalendar.startOfDay(for: Date(timeIntervalSince1970: 0))
        return streakCalendar.dateComponents([.day], from: epoch, to: start).day ?? 0
    }

    var gameRewardsLeft: Int {
        if gameRewardDay != todayEpochDay {
            return maxGameRewardsPerDay
        }
        return max(0, maxGameRewardsPerDay - gameRewardsToday)
    }

    /// Los juegos repasan lo aprendido, así que se desbloquean al terminar la lección 1 ("Primeras frases").
    var gamesUnlocked: Bool {
        greetingsRewardClaimed
    }

    /// Entrega monedas y felicidad por terminar un juego si aún quedan recompensas hoy.
    /// Devuelve false si ya se usaron las 3 del día (se puede seguir jugando para practicar).
    @discardableResult
    func claimGameReward(coinsEarned: Int, happinessEarned: Int) -> Bool {
        let today = todayEpochDay
        if gameRewardDay != today {
            gameRewardDay = today
            gameRewardsToday = 0
        }
        guard gameRewardsToday < maxGameRewardsPerDay else {
            return false
        }
        gameRewardsToday += 1
        if coinsEarned > 0 {
            coins += coinsEarned
        }
        happiness = min(100, happiness + happinessEarned)
        return true
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

        if let parentUID = activeParentUID, let childId = activeChildId {
            Task {
                let progress = LessonProgressRecord(
                    lessonId: "greetings",
                    completed: true,
                    rewardClaimed: true,
                    correctAnswers: 5,
                    totalAnswers: 5,
                    elapsedSeconds: 120,
                    completedAt: Date()
                )
                try? await FirestoreService.shared.saveLessonProgress(
                    parentUID: parentUID,
                    childId: childId,
                    progress: progress
                )
            }
        }
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
        setupAuthObserver()
    }

    deinit {
        profileListener?.remove()
        syncDebounceWorkItem?.cancel()
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
        lastPracticeDate = loadDate("lastPracticeDate")
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

        // Artículos extendidos (catálogo v1.1)
        ownedExtraItems = Set(storage.stringArray(forKey: "zentemi.ownedExtraItems") ?? [])
        equippedExtraItems = Set(storage.stringArray(forKey: "zentemi.equippedExtraItems") ?? [])

        // Estado de modo invitado y onboarding
        hasCompletedOnboarding = loadBool("hasCompletedOnboarding", default: false)
        boundAccountUid = loadString("boundAccountUid", default: "")
        accountProgressBaseline = loadString("accountProgressBaseline", default: "")
        lastAccountPromptAnswered = loadDate("lastAccountPromptDismissed")

        // Recompensas diarias de juegos con Maicito
        if storage.object(forKey: "zentemi.gameRewardDay") != nil {
            gameRewardDay = storage.integer(forKey: "zentemi.gameRewardDay")
        } else {
            gameRewardDay = nil
        }
        gameRewardsToday = loadInt("zentemi.gameRewardsToday".replacingOccurrences(of: "zentemi.", with: ""), default: 0)

        // Migración suave: si ya tenía sesión iniciada antes del modo invitado,
        // no vuelve a ver el onboarding y su progreso pertenece a su cuenta.
        if let user = AuthService.shared.currentUser {
            hasCompletedOnboarding = true
            storage.set(true, forKey: "zentemi.hasCompletedOnboarding")
            if boundAccountUid.isEmpty {
                boundAccountUid = user.uid
                storage.set(user.uid, forKey: "zentemi.boundAccountUid")
            }
        }

        isLoading = false
    }

    // MARK: - ELECCIÓN DE PROGRESO AL INICIAR SESIÓN (MODO INVITADO)

    /// "Usar el progreso de este teléfono": sube el progreso local a la cuenta y reemplaza el de la nube.
    @MainActor
    public func keepPhoneProgress() {
        guard let choice = pendingProgressChoice, let childId = choice.cloudProfile.id else { return }
        pendingProgressChoice = nil
        childCreatedAt = choice.cloudProfile.createdAt
        activeChildId = childId
        let profile = exportChildProfile(id: childId)
        lastSyncedProfile = profile
        Task {
            try? await FirestoreService.shared.saveChildProfile(
                parentUID: choice.parentUID,
                child: profile
            )
        }
        finishBinding(uid: choice.parentUID, childId: childId)
    }

    /// "Usar el progreso de mi cuenta": descarga y aplica el progreso guardado en la nube.
    @MainActor
    public func useAccountProgress() {
        guard let choice = pendingProgressChoice, let childId = choice.cloudProfile.id else { return }
        pendingProgressChoice = nil
        activeChildId = childId
        applyChildProfile(choice.cloudProfile)
        updateDailyStreak()
        finishBinding(uid: choice.parentUID, childId: childId)
    }

    // MARK: - SINCRONIZACIÓN CON FIRESTORE (ETAPA 2 + COMPATIBILIDAD ANDROID v1.1)

    func setupAuthObserver() {
        AuthService.shared.$currentUser
            .receive(on: DispatchQueue.main)
            .sink { [weak self] user in
                guard let self = self else { return }
                self.isGuest = (user == nil)
                self.accountEmail = user?.email
                if let user = user {
                    self.bindParentAccount(uid: user.uid, email: user.email ?? "")
                } else {
                    self.unbindAccount()
                }
            }
            .store(in: &cancellables)

        // Si la vinculación falló por falta de red, se reintenta automáticamente al volver internet.
        NetworkMonitor.shared.$isOnline
            .receive(on: DispatchQueue.main)
            .sink { [weak self] online in
                guard let self = self else { return }
                if online, let user = AuthService.shared.currentUser, self.activeParentUID == nil {
                    self.bindParentAccount(uid: user.uid, email: user.email ?? "")
                }
            }
            .store(in: &cancellables)
    }

    func bindParentAccount(uid: String, email: String) {
        guard activeParentUID != uid else { return }
        activeParentUID = uid

        Task { @MainActor in
            isSyncingWithCloud = true
            defer { isSyncingWithCloud = false }
            do {
                // 1. Asegurar documento del tutor
                try? await FirestoreService.shared.createOrUpdateParentProfile(uid: uid, email: email)

                // 2. Buscar el perfil del menor. Si es otra cuenta distinta a la del teléfono,
                // consultamos al servidor para no confundir "sin red" con "cuenta nueva".
                let children = try await FirestoreService.shared.fetchChildren(
                    parentUID: uid,
                    fromServer: boundAccountUid != uid
                )
                guard activeParentUID == uid else { return }

                let cloudChild = children.first { $0.id != nil }

                if let cloudChild = cloudChild, let childId = cloudChild.id {
                    if boundAccountUid != uid && hasGuestProgress {
                        // Cuenta con perfil + progreso de invitado en el iPhone: preguntar cuál usar
                        pendingProgressChoice = PendingProgressChoice(
                            parentUID: uid,
                            cloudProfile: cloudChild
                        )
                    } else {
                        // Misma cuenta anterior o teléfono sin progreso propio
                        activeChildId = childId
                        applyChildProfile(cloudChild)
                        updateDailyStreak()
                        finishBinding(uid: uid, childId: childId)
                    }
                } else {
                    // Cuenta nueva: el perfil nace conservando el progreso que el alumno ya tenía como invitado
                    childCreatedAt = Date()
                    let initialProfile = buildChildProfile(id: nil)
                    let newChild = try await FirestoreService.shared.createChildProfile(
                        parentUID: uid,
                        profile: initialProfile
                    )
                    guard let childId = newChild.id else { return }
                    activeChildId = childId
                    lastSyncedProfile = exportChildProfile(id: childId)
                    finishBinding(uid: uid, childId: childId)
                }
            } catch {
                print("[AppState] Error al sincronizar con Firestore: \(error.localizedDescription)")
                if activeParentUID == uid && activeChildId == nil && pendingProgressChoice == nil {
                    activeParentUID = nil
                }
            }
        }
    }

    /// Marca el iPhone como copia vinculada a esa cuenta y escucha sus cambios en tiempo real.
    private func finishBinding(uid: String, childId: String) {
        boundAccountUid = uid
        accountProgressBaseline = progressSignature()

        profileListener?.remove()
        profileListener = FirestoreService.shared.listenToChildProfile(
            parentUID: uid,
            childId: childId
        ) { [weak self] result in
            if case .success(let updated) = result {
                DispatchQueue.main.async {
                    self?.applyChildProfile(updated)
                }
            }
        }
    }

    func unbindAccount() {
        syncDebounceWorkItem?.cancel()
        profileListener?.remove()
        profileListener = nil
        pendingProgressChoice = nil
        activeParentUID = nil
        activeChildId = nil
        childCreatedAt = nil
        lastSyncedProfile = nil

        // Al cerrar sesión el progreso se queda en el iPhone y la app sigue en modo invitado.
        // Desde aquí, el nuevo progreso cuenta como progreso de invitado.
        if !boundAccountUid.isEmpty {
            boundAccountUid = ""
            accountProgressBaseline = progressSignature()
        }
    }

    private func applyChildProfile(_ child: ChildProfile) {
        isLoading = true
        self.userName = child.userName
        self.lastUserNameChange = child.lastUserNameChange
        self.petName = child.petName
        self.happiness = child.happiness
        self.coins = child.coins
        self.totalXP = child.totalXP
        self.currentLevel = child.currentLevel
        self.streak = child.streak
        self.lastPracticeDate = child.lastPracticeDate
        if let practiceDate = child.lastPracticeDate {
            storage.set(practiceDate, forKey: "zentemi.lastStreakDate")
        }
        self.lessonsCompleted = child.lessonsCompleted

        self.avatarBackgroundHue = child.avatar.avatarBackgroundHue
        self.mascotHue = child.avatar.mascotHue
        self.profileBackgroundHue = child.avatar.profileBackgroundHue

        self.musicVolume = child.settings.musicVolume
        self.soundVolume = child.settings.soundVolume

        // Campos extendidos compartidos con Android v1.1:
        // si vienen nil (perfil antiguo), conservamos el valor local sin borrarlo.
        if let pathCompleted = child.pathLessonsCompleted {
            self.pathLessonsCompleted = pathCompleted
        }
        if let unlockedLesson = child.unlockedPathLesson {
            self.unlockedPathLesson = unlockedLesson
        }
        if let rewardClaimed = child.greetingsRewardClaimed {
            self.greetingsRewardClaimed = rewardClaimed
        }
        if let owned = child.ownedItems {
            self.applyOwnedItems(Set(owned))
        }
        if let equipped = child.equippedItems {
            self.applyEquippedItems(Set(equipped))
        }

        self.childCreatedAt = child.createdAt
        self.persistLocalState()
        if let childId = child.id ?? self.activeChildId {
            self.lastSyncedProfile = self.exportChildProfile(id: childId)
        }
        isLoading = false
    }

    private func syncProfileToFirestore() {
        guard let parentUID = activeParentUID, let childId = activeChildId, !isLoading else { return }

        syncDebounceWorkItem?.cancel()
        let workItem = DispatchWorkItem { [weak self] in
            guard let self = self else { return }
            let profile = self.exportChildProfile(id: childId)
            guard profile != self.lastSyncedProfile else { return }
            self.lastSyncedProfile = profile

            Task {
                try? await FirestoreService.shared.saveChildProfile(
                    parentUID: parentUID,
                    child: profile
                )
            }
        }
        syncDebounceWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5, execute: workItem)
    }

    public func exportChildProfile(id: String) -> ChildProfile {
        buildChildProfile(id: id)
    }

    private func buildChildProfile(id: String?) -> ChildProfile {
        ChildProfile(
            id: id,
            userName: userName,
            lastUserNameChange: lastUserNameChange,
            petName: petName,
            happiness: happiness,
            coins: coins,
            totalXP: totalXP,
            currentLevel: currentLevel,
            streak: streak,
            lastPracticeDate: lastPracticeDate ?? (storage.object(forKey: "zentemi.lastStreakDate") as? Date),
            lessonsCompleted: lessonsCompleted,
            avatar: ChildAvatarSettings(
                avatarBackgroundHue: avatarBackgroundHue,
                mascotHue: mascotHue,
                profileBackgroundHue: profileBackgroundHue
            ),
            settings: ChildAppSettings(
                musicVolume: musicVolume,
                soundVolume: soundVolume
            ),
            createdAt: childCreatedAt ?? Date(),
            pathLessonsCompleted: pathLessonsCompleted,
            unlockedPathLesson: unlockedPathLesson,
            greetingsRewardClaimed: greetingsRewardClaimed,
            ownedItems: ownedItemIDs(),
            equippedItems: equippedItemIDs()
        )
    }

    /// IDs de los artículos comprados (sin los 3 gratuitos iniciales), compatible con Android
    private func ownedItemIDs() -> [String] {
        var ids = ownedExtraItems
        if sunglassesPurchased { ids.insert("sunglasses") }
        if pinkHairPurchased { ids.insert("pinkHair") }
        if swordPurchased { ids.insert("sword") }
        if suitPurchased { ids.insert("suit") }
        return ids.sorted()
    }

    /// IDs de los artículos equipados actualmente, compatible con Android
    private func equippedItemIDs() -> [String] {
        var ids = equippedExtraItems
        if glassesEquipped { ids.insert("glasses") }
        if hairEquipped { ids.insert("hair") }
        if scarfEquipped { ids.insert("scarf") }
        if sunglassesEquipped { ids.insert("sunglasses") }
        if pinkHairEquipped { ids.insert("pinkHair") }
        if swordEquipped { ids.insert("sword") }
        if suitEquipped { ids.insert("suit") }
        return ids.sorted()
    }

    private func applyOwnedItems(_ ids: Set<String>) {
        sunglassesPurchased = ids.contains("sunglasses")
        pinkHairPurchased = ids.contains("pinkHair")
        swordPurchased = ids.contains("sword")
        suitPurchased = ids.contains("suit")
        ownedExtraItems = ids.subtracting(Self.legacyItemIDs)
    }

    private func applyEquippedItems(_ ids: Set<String>) {
        glassesEquipped = ids.contains("glasses")
        hairEquipped = ids.contains("hair")
        scarfEquipped = ids.contains("scarf")
        sunglassesEquipped = ids.contains("sunglasses")
        pinkHairEquipped = ids.contains("pinkHair")
        swordEquipped = ids.contains("sword")
        suitEquipped = ids.contains("suit")
        equippedExtraItems = ids.subtracting(Self.legacyItemIDs)
    }

    /// Guarda en UserDefaults el estado recibido de la nube sin disparar re-subida a Firestore
    private func persistLocalState() {
        storage.set(userName, forKey: "zentemi.userName")
        storage.set(lastUserNameChange, forKey: "zentemi.lastUserNameChange")
        storage.set(petName, forKey: "zentemi.petName")
        storage.set(happiness, forKey: "zentemi.happiness")
        storage.set(coins, forKey: "zentemi.coins")
        storage.set(streak, forKey: "zentemi.streak")
        storage.set(lastPracticeDate, forKey: "zentemi.lastPracticeDate")
        storage.set(lessonsCompleted, forKey: "zentemi.lessonsCompleted")
        storage.set(currentLevel, forKey: "zentemi.currentLevel")
        storage.set(totalXP, forKey: "zentemi.totalXP")
        storage.set(avatarBackgroundHue, forKey: "zentemi.avatarBackgroundHue")
        storage.set(mascotHue, forKey: "zentemi.mascotHue")
        storage.set(profileBackgroundHue, forKey: "zentemi.profileBackgroundHue")
        storage.set(glassesEquipped, forKey: "zentemi.glassesEquipped")
        storage.set(hairEquipped, forKey: "zentemi.hairEquipped")
        storage.set(scarfEquipped, forKey: "zentemi.scarfEquipped")
        storage.set(sunglassesPurchased, forKey: "zentemi.sunglassesPurchased")
        storage.set(pinkHairPurchased, forKey: "zentemi.pinkHairPurchased")
        storage.set(swordPurchased, forKey: "zentemi.swordPurchased")
        storage.set(suitPurchased, forKey: "zentemi.suitPurchased")
        storage.set(sunglassesEquipped, forKey: "zentemi.sunglassesEquipped")
        storage.set(pinkHairEquipped, forKey: "zentemi.pinkHairEquipped")
        storage.set(swordEquipped, forKey: "zentemi.swordEquipped")
        storage.set(suitEquipped, forKey: "zentemi.suitEquipped")
        storage.set(pathLessonsCompleted, forKey: "zentemi.pathLessonsCompleted")
        storage.set(unlockedPathLesson, forKey: "zentemi.unlockedPathLesson")
        storage.set(greetingsRewardClaimed, forKey: "zentemi.greetingsRewardClaimed")
        storage.set(musicVolume, forKey: "zentemi.musicVolume")
        storage.set(soundVolume, forKey: "zentemi.soundVolume")
        storage.set(ownedExtraItems.sorted(), forKey: "zentemi.ownedExtraItems")
        storage.set(equippedExtraItems.sorted(), forKey: "zentemi.equippedExtraItems")
    }

    public func recordInventoryItem(itemId: String, purchased: Bool, equipped: Bool) {
        if let parentUID = activeParentUID, let childId = activeChildId {
            Task {
                let item = InventoryItemRecord(
                    itemId: itemId,
                    purchased: purchased,
                    equipped: equipped,
                    purchasedAt: Date()
                )
                try? await FirestoreService.shared.saveInventoryItem(
                    parentUID: parentUID,
                    childId: childId,
                    item: item
                )
            }
        }
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
        lastPracticeDate = nil

        ownedExtraItems = []
        equippedExtraItems = []
        lastSyncedProfile = nil
        pendingProgressChoice = nil
        boundAccountUid = ""
        accountProgressBaseline = ""
        lastAccountPromptAnswered = nil
        hasCompletedOnboarding = false
        gameRewardDay = nil
        gameRewardsToday = 0

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
