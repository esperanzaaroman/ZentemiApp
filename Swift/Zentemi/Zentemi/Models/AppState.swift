
import SwiftUI
import Combine
import FirebaseFirestore
import FirebaseAuth

class AppState: ObservableObject {

    // MARK: - ALMACENAMIENTO LOCAL Y CLOUD

    private let storage = UserDefaults.standard
    private var isLoading = false

    // MARK: - FIRESTORE INTEGRATION

    @Published public private(set) var activeParentUID: String? = nil
    @Published public private(set) var activeChildId: String? = nil
    @Published public private(set) var isSyncingWithCloud: Bool = false

    private var profileListener: ListenerRegistration?
    private var cancellables = Set<AnyCancellable>()
    private var syncDebounceWorkItem: DispatchWorkItem?

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

        isLoading = false
    }

    // MARK: - SINCRONIZACIÓN CON FIRESTORE (ETAPA 2)

    func setupAuthObserver() {
        AuthService.shared.$currentUser
            .receive(on: DispatchQueue.main)
            .sink { [weak self] user in
                if let user = user {
                    self?.bindParentAccount(uid: user.uid, email: user.email ?? "")
                } else {
                    self?.unbindAccount()
                }
            }
            .store(in: &cancellables)
    }

    func bindParentAccount(uid: String, email: String) {
        guard activeParentUID != uid else { return }
        activeParentUID = uid

        Task { @MainActor in
            isSyncingWithCloud = true
            do {
                // 1. Asegurar documento del tutor
                try await FirestoreService.shared.createOrUpdateParentProfile(uid: uid, email: email)

                // 2. Obtener o crear perfil del menor
                let children = try await FirestoreService.shared.fetchChildren(parentUID: uid)
                let childProfile: ChildProfile
                if let firstChild = children.first, let childId = firstChild.id {
                    childProfile = firstChild
                    self.activeChildId = childId
                } else {
                    let newChild = try await FirestoreService.shared.createChildProfile(
                        parentUID: uid,
                        userName: self.userName
                    )
                    childProfile = newChild
                    self.activeChildId = newChild.id
                }

                // 3. Aplicar datos de la nube al estado local
                self.applyChildProfile(childProfile)

                // 4. Conectar listener en tiempo real
                if let childId = self.activeChildId {
                    self.profileListener?.remove()
                    self.profileListener = FirestoreService.shared.listenToChildProfile(
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
            } catch {
                print("[AppState] Error al sincronizar con Firestore: \(error.localizedDescription)")
            }
            isSyncingWithCloud = false
        }
    }

    func unbindAccount() {
        profileListener?.remove()
        profileListener = nil
        activeParentUID = nil
        activeChildId = nil
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
        self.lessonsCompleted = child.lessonsCompleted

        self.avatarBackgroundHue = child.avatar.avatarBackgroundHue
        self.mascotHue = child.avatar.mascotHue
        self.profileBackgroundHue = child.avatar.profileBackgroundHue

        self.musicVolume = child.settings.musicVolume
        self.soundVolume = child.settings.soundVolume
        isLoading = false
    }

    private func syncProfileToFirestore() {
        guard let parentUID = activeParentUID, let childId = activeChildId, !isLoading else { return }

        syncDebounceWorkItem?.cancel()
        let workItem = DispatchWorkItem { [weak self] in
            guard let self = self else { return }
            let profile = self.exportChildProfile(id: childId)
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
            lastPracticeDate: storage.object(forKey: "zentemi.lastStreakDate") as? Date,
            lessonsCompleted: lessonsCompleted,
            avatar: ChildAvatarSettings(
                avatarBackgroundHue: avatarBackgroundHue,
                mascotHue: mascotHue,
                profileBackgroundHue: profileBackgroundHue
            ),
            settings: ChildAppSettings(
                musicVolume: musicVolume,
                soundVolume: soundVolume
            )
        )
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
