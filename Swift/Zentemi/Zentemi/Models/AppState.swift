
import SwiftUI
import Combine
import FirebaseFirestore
import FirebaseAuth

enum HomeBackground: String, CaseIterable, Identifiable {
    case beach = "backgroundBeach"
    case valley = "backgroundValley"

    var id: String { rawValue }

    var title: String {
        switch self {
        case .beach: return "Fondo de playa"
        case .valley: return "Fondo de valle"
        }
    }

    var imageName: String {
        switch self {
        case .beach: return "homeBeachBackground"
        case .valley: return "homeValleyBackground"
        }
    }

    var price: Int {
        switch self {
        case .beach: return 300
        case .valley: return 500
        }
    }
}


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
        "glasses", "hair", "scarf", "sunglasses", "pinkHair", "sword", "suit",
        "redShirt", "backgroundBeach", "backgroundValley"
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
        if let background = HomeBackground(rawValue: item.id) {
            return purchasedHomeBackgrounds.contains(background)
        }
        if item.id == "redShirt" { return redShirtPurchased }
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
        if let background = HomeBackground(rawValue: item.id) {
            return equippedHomeBackground == background
        }
        if item.id == "redShirt" { return redShirtEquipped }
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
        if let background = HomeBackground(rawValue: item.id) {
            if value { equippedHomeBackground = background }
            else if equippedHomeBackground == background { equippedHomeBackground = nil }
            return
        }
        if item.id == "redShirt" {
            redShirtEquipped = value
            return
        }
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
        if isOwned(item) { return false }
        if item.id == "redShirt" { return purchaseRedShirt() }
        if let background = HomeBackground(rawValue: item.id) {
            return purchaseHomeBackground(background)
        }
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

    /// Un punto cada dos horas, hasta diez en cada período de 24 horas.
    /// El origen fijo conserva las fracciones de tiempo entre aperturas.
    /// La primera instalación de esta regla no descuenta tiempo anterior.
    func updateHappinessDecay(now: Date = Date()) {
        guard !isLoading else { return }
        guard let origin = loadDate("happinessDecayOrigin"),
              let previous = loadDate("happinessDecayLastUpdate") else {
            save(now, key: "happinessDecayOrigin")
            save(now, key: "happinessDecayLastUpdate")
            return
        }
        // Si se atrasa el reloj, esperar hasta alcanzar la última fecha registrada.
        guard now >= previous, previous >= origin else { return }
        func scheduledLoss(at date: Date) -> Double {
            let elapsed = max(0, date.timeIntervalSince(origin))
            let days = floor(elapsed / 86_400)
            let remainder = elapsed.truncatingRemainder(dividingBy: 86_400)
            return days * 10 + min(10, floor(remainder / 7_200))
        }
        let due = max(0, scheduledLoss(at: now) - scheduledLoss(at: previous))
        if due > 0 {
            happiness = max(0, min(100, happiness) - Int(min(100, due)))
        }
        // Consumir también los intervalos a felicidad cero: no quedan deudas
        // que descuenten después la felicidad ganada al alimentar.
        save(now, key: "happinessDecayLastUpdate")
    }

    @Published var happiness = 50 {
        didSet { save(happiness, key: "happiness") }
    }
    
    // MARK: - PUNTOS DE LOS JUEGOS

    @Published var gamePoints = 0 {
        didSet { save(gamePoints, key: "gamePoints") }
    }

    let gamePointsGoal = 10_000


    // MARK: - MEMORAMA
    let memoryLevelCount = 3
    let memoryLevelOneCoins = MemoryLevel.one.coins
    let memoryLevelOnePoints = MemoryLevel.one.points

    @Published private(set) var memoryLevelOneCompleted = false {
        didSet { save(memoryLevelOneCompleted, key: "memoryLevelOneCompleted") }
    }
    @Published private(set) var memoryLevelOneBestSeconds: Double = 0 {
        didSet { save(memoryLevelOneBestSeconds, key: "memoryLevelOneBestSeconds") }
    }

    @MainActor
    func completeMemoryLevelOne(seconds: Double) -> Bool {
        guard seconds.isFinite, seconds >= 0 else { return false }
        if memoryLevelOneBestSeconds == 0 || seconds < memoryLevelOneBestSeconds {
            memoryLevelOneBestSeconds = max(0.01, seconds)
        }
        guard !memoryLevelOneCompleted else { return false }
        memoryLevelOneCompleted = true
        addCoins(memoryLevelOneCoins)
        gamePoints += memoryLevelOnePoints
        return true
    }


    @Published private(set) var memoryLevelTwoCompleted = false {
        didSet { save(memoryLevelTwoCompleted, key: "memoryLevelTwoCompleted") }
    }
    @Published private(set) var memoryLevelTwoBestSeconds: Double = 0 {
        didSet { save(memoryLevelTwoBestSeconds, key: "memoryLevelTwoBestSeconds") }
    }
    @Published private(set) var memoryLevelThreeCompleted = false {
        didSet { save(memoryLevelThreeCompleted, key: "memoryLevelThreeCompleted") }
    }
    @Published private(set) var memoryLevelThreeBestSeconds: Double = 0 {
        didSet { save(memoryLevelThreeBestSeconds, key: "memoryLevelThreeBestSeconds") }
    }

    func memoryCompleted(_ level: MemoryLevel) -> Bool {
        switch level {
        case .one: memoryLevelOneCompleted
        case .two: memoryLevelTwoCompleted
        case .three: memoryLevelThreeCompleted
        }
    }

    func memoryUnlocked(_ level: MemoryLevel) -> Bool {
        switch level {
        case .one: true
        case .two: memoryLevelOneCompleted
        case .three: memoryLevelTwoCompleted
        }
    }

    func memoryBestSeconds(_ level: MemoryLevel) -> Double {
        switch level {
        case .one: memoryLevelOneBestSeconds
        case .two: memoryLevelTwoBestSeconds
        case .three: memoryLevelThreeBestSeconds
        }
    }

    /// Devuelve si esta victoria entregó el premio; nunca se paga desde resultados.
    @MainActor
    func completeMemory(level: MemoryLevel, seconds: Double) -> Bool {
        guard memoryUnlocked(level), seconds.isFinite, seconds >= 0 else { return false }
        if level == .one { return completeMemoryLevelOne(seconds: seconds) }
        let best = memoryBestSeconds(level)
        if best == 0 || seconds < best {
            switch level {
            case .two: memoryLevelTwoBestSeconds = max(0.01, seconds)
            case .three: memoryLevelThreeBestSeconds = max(0.01, seconds)
            case .one: break
            }
        }
        guard !memoryCompleted(level) else { return false }
        switch level {
        case .two: memoryLevelTwoCompleted = true
        case .three: memoryLevelThreeCompleted = true
        case .one: break
        }
        addCoins(level.coins)
        gamePoints += level.points
        return true
    }

    // MARK: - SOPA DE LETRAS, NIVEL 1

    @Published private(set) var wordSearchLevelOneCompleted = false {
        didSet { save(wordSearchLevelOneCompleted, key: "wordSearchLevelOneCompleted") }
    }
    @Published private(set) var wordSearchLevelOneBestSeconds: Double = 0 {
        didSet { save(wordSearchLevelOneBestSeconds, key: "wordSearchLevelOneBestSeconds") }
    }

    @MainActor
    func completeWordSearchLevelOne(seconds: Double) -> Bool {
        guard seconds.isFinite, seconds >= 0 else { return false }
        if wordSearchLevelOneBestSeconds == 0 || seconds < wordSearchLevelOneBestSeconds {
            wordSearchLevelOneBestSeconds = max(0.01, seconds)
        }
        guard !wordSearchLevelOneCompleted else { return false }
        wordSearchLevelOneCompleted = true
        addCoins(WordSearchLevel.one.coins)
        gamePoints += WordSearchLevel.one.points
        return true
    }

    @Published private(set) var wordSearchLevelTwoCompleted = false {
        didSet { save(wordSearchLevelTwoCompleted, key: "wordSearchLevelTwoCompleted") }
    }
    @Published private(set) var wordSearchLevelTwoBestSeconds: Double = 0 {
        didSet { save(wordSearchLevelTwoBestSeconds, key: "wordSearchLevelTwoBestSeconds") }
    }
    @Published private(set) var wordSearchLevelThreeCompleted = false {
        didSet { save(wordSearchLevelThreeCompleted, key: "wordSearchLevelThreeCompleted") }
    }
    @Published private(set) var wordSearchLevelThreeBestSeconds: Double = 0 {
        didSet { save(wordSearchLevelThreeBestSeconds, key: "wordSearchLevelThreeBestSeconds") }
    }

    func wordSearchCompleted(_ level: WordSearchLevel) -> Bool {
        switch level {
        case .one: wordSearchLevelOneCompleted
        case .two: wordSearchLevelTwoCompleted
        case .three: wordSearchLevelThreeCompleted
        }
    }

    func wordSearchUnlocked(_ level: WordSearchLevel) -> Bool {
        switch level {
        case .one: true
        case .two: wordSearchLevelOneCompleted
        case .three: wordSearchLevelTwoCompleted
        }
    }

    func wordSearchBestSeconds(_ level: WordSearchLevel) -> Double {
        switch level {
        case .one: wordSearchLevelOneBestSeconds
        case .two: wordSearchLevelTwoBestSeconds
        case .three: wordSearchLevelThreeBestSeconds
        }
    }

    @MainActor
    func completeWordSearch(level: WordSearchLevel, seconds: Double) -> Bool {
        guard wordSearchUnlocked(level), seconds.isFinite, seconds >= 0 else { return false }
        if level == .one { return completeWordSearchLevelOne(seconds: seconds) }
        let best = wordSearchBestSeconds(level)
        if best == 0 || seconds < best {
            switch level {
            case .two: wordSearchLevelTwoBestSeconds = max(0.01, seconds)
            case .three: wordSearchLevelThreeBestSeconds = max(0.01, seconds)
            case .one: break
            }
        }
        guard !wordSearchCompleted(level) else { return false }
        switch level {
        case .two: wordSearchLevelTwoCompleted = true
        case .three: wordSearchLevelThreeCompleted = true
        case .one: break
        }
        addCoins(level.coins)
        gamePoints += level.points
        return true
    }

    // MARK: - LABERINTO, NIVEL 1

    @Published private(set) var mazeLevelOneCompleted = false {
        didSet { save(mazeLevelOneCompleted, key: "mazeLevelOneCompleted") }
    }

    @Published private(set) var mazeLevelOneBestSeconds: Double = 0 {
        didSet { save(mazeLevelOneBestSeconds, key: "mazeLevelOneBestSeconds") }
    }

    let mazeLevelOneCoins = 300
    let mazeLevelOnePoints = 500

    /// Se llama al alcanzar la salida correcta, nunca al mostrar resultados.
    /// Devuelve si esta victoria entregó la recompensa de primera vez.
    @MainActor
    @discardableResult
    func completeMazeLevelOne(seconds: Double) -> Bool {
        guard seconds.isFinite, seconds >= 0 else { return false }
        if mazeLevelOneBestSeconds == 0 || seconds < mazeLevelOneBestSeconds {
            mazeLevelOneBestSeconds = max(seconds, 0.01)
        }
        guard !mazeLevelOneCompleted else { return false }
        mazeLevelOneCompleted = true
        addCoins(mazeLevelOneCoins)
        gamePoints += mazeLevelOnePoints
        return true
    }

    // LABERINTO, NIVEL 2

    @Published private(set) var mazeLevelTwoCompleted = false {
        didSet { save(mazeLevelTwoCompleted, key: "mazeLevelTwoCompleted") }
    }

    @Published private(set) var mazeLevelTwoBestSeconds: Double = 0 {
        didSet { save(mazeLevelTwoBestSeconds, key: "mazeLevelTwoBestSeconds") }
    }

    // Un bit por moneda; las cinco posiciones conservan su identidad entre partidas.
    @Published private(set) var mazeLevelTwoClaimedCoinMask = 0 {
        didSet { save(mazeLevelTwoClaimedCoinMask, key: "mazeLevelTwoClaimedCoinMask") }
    }

    let mazeLevelTwoCoins = 450
    let mazeLevelTwoPoints = 1_400
    let plannedLevelsPerGame = 5

    // MARK: - LABERINTO, NIVEL 3

    @Published private(set) var mazeLevelThreeCompleted = false {
        didSet { save(mazeLevelThreeCompleted, key: "mazeLevelThreeCompleted") }
    }

    @Published private(set) var mazeLevelThreeBestSeconds: Double = 0 {
        didSet { save(mazeLevelThreeBestSeconds, key: "mazeLevelThreeBestSeconds") }
    }

    @Published private(set) var mazeLevelThreeClaimedCoinMask = 0 {
        didSet { save(mazeLevelThreeClaimedCoinMask, key: "mazeLevelThreeClaimedCoinMask") }
    }

    let mazeLevelThreeCoins = 600
    let mazeLevelThreePoints = 2_000

    @Published private(set) var mazeLevelFourCompleted = false {
        didSet { save(mazeLevelFourCompleted, key: "mazeLevelFourCompleted") }
    }

    @Published private(set) var mazeLevelFourBestSeconds: Double = 0 {
        didSet { save(mazeLevelFourBestSeconds, key: "mazeLevelFourBestSeconds") }
    }

    @Published private(set) var mazeLevelFourClaimedCoinMask = 0 {
        didSet { save(mazeLevelFourClaimedCoinMask, key: "mazeLevelFourClaimedCoinMask") }
    }

    let mazeLevelFourCoins = 800
    let mazeLevelFourPoints = 2_800

    @Published private(set) var mazeLevelFiveCompleted = false {
        didSet { save(mazeLevelFiveCompleted, key: "mazeLevelFiveCompleted") }
    }

    @Published private(set) var mazeLevelFiveBestSeconds: Double = 0 {
        didSet { save(mazeLevelFiveBestSeconds, key: "mazeLevelFiveBestSeconds") }
    }

    @Published private(set) var mazeLevelFiveClaimedCoinMask = 0 {
        didSet { save(mazeLevelFiveClaimedCoinMask, key: "mazeLevelFiveClaimedCoinMask") }
    }

    let mazeLevelFiveCoins = 1_000
    let mazeLevelFivePoints = 3_500

    func mazeBestSeconds(for level: Int) -> Double {
        switch level {
        case 5: return mazeLevelFiveBestSeconds
        case 4: return mazeLevelFourBestSeconds
        case 3: return mazeLevelThreeBestSeconds
        case 2: return mazeLevelTwoBestSeconds
        default: return mazeLevelOneBestSeconds
        }
    }

    func mazeClaimedCoinMask(for level: Int) -> Int {
        switch level {
        case 5: return mazeLevelFiveClaimedCoinMask
        case 4: return mazeLevelFourClaimedCoinMask
        case 3: return mazeLevelThreeClaimedCoinMask
        case 2: return mazeLevelTwoClaimedCoinMask
        default: return 0
        }
    }

    @MainActor
    private func completeMazeLevelThree(seconds: Double, collectedCoinIDs: Set<Int>) -> MazeReward {
        guard mazeLevelTwoCompleted, seconds.isFinite, seconds >= 0 else { return .none }
        if mazeLevelThreeBestSeconds == 0 || seconds < mazeLevelThreeBestSeconds {
            mazeLevelThreeBestSeconds = max(seconds, 0.01)
        }
        let firstWin = !mazeLevelThreeCompleted
        let baseCoins = firstWin ? mazeLevelThreeCoins : 0
        let points = firstWin ? mazeLevelThreePoints : 0
        var mask = mazeLevelThreeClaimedCoinMask
        var collectedCoins = 0
        for id in collectedCoinIDs where (0..<10).contains(id) {
            let bit = 1 << id
            if mask & bit == 0 {
                mask |= bit
                collectedCoins += 10
            }
        }
        mazeLevelThreeCompleted = true
        mazeLevelThreeClaimedCoinMask = mask
        addCoins(baseCoins + collectedCoins)
        gamePoints += points
        return MazeReward(baseCoins: baseCoins, collectedCoins: collectedCoins, points: points)
    }

    @MainActor
    private func completeMazeLevelFour(seconds: Double, collectedCoinIDs: Set<Int>) -> MazeReward {
        guard mazeLevelThreeCompleted, seconds.isFinite, seconds >= 0 else { return .none }
        if mazeLevelFourBestSeconds == 0 || seconds < mazeLevelFourBestSeconds {
            mazeLevelFourBestSeconds = max(seconds, 0.01)
        }
        let firstWin = !mazeLevelFourCompleted
        let baseCoins = firstWin ? mazeLevelFourCoins : 0
        let points = firstWin ? mazeLevelFourPoints : 0
        var mask = mazeLevelFourClaimedCoinMask
        var collectedCoins = 0
        for id in collectedCoinIDs where (0..<15).contains(id) {
            let bit = 1 << id
            if mask & bit == 0 {
                mask |= bit
                collectedCoins += 10
            }
        }
        mazeLevelFourCompleted = true
        mazeLevelFourClaimedCoinMask = mask
        addCoins(baseCoins + collectedCoins)
        gamePoints += points
        return MazeReward(baseCoins: baseCoins, collectedCoins: collectedCoins, points: points)
    }

    @MainActor
    private func completeMazeLevelFive(seconds: Double, collectedCoinIDs: Set<Int>) -> MazeReward {
        guard mazeLevelFourCompleted, seconds.isFinite, seconds >= 0 else { return .none }
        if mazeLevelFiveBestSeconds == 0 || seconds < mazeLevelFiveBestSeconds {
            mazeLevelFiveBestSeconds = max(seconds, 0.01)
        }
        let firstWin = !mazeLevelFiveCompleted
        let baseCoins = firstWin ? mazeLevelFiveCoins : 0
        let points = firstWin ? mazeLevelFivePoints : 0
        var mask = mazeLevelFiveClaimedCoinMask
        var collectedCoins = 0
        for id in collectedCoinIDs where (0..<20).contains(id) {
            let bit = 1 << id
            if mask & bit == 0 {
                mask |= bit
                collectedCoins += 10
            }
        }
        mazeLevelFiveCompleted = true
        mazeLevelFiveClaimedCoinMask = mask
        addCoins(baseCoins + collectedCoins)
        gamePoints += points
        return MazeReward(baseCoins: baseCoins, collectedCoins: collectedCoins, points: points)
    }

    @MainActor
    func completeMaze(level: Int, seconds: Double, collectedCoinIDs: Set<Int>) -> MazeReward {
        if level == 5 {
            return completeMazeLevelFive(seconds: seconds, collectedCoinIDs: collectedCoinIDs)
        }
        if level == 4 {
            return completeMazeLevelFour(seconds: seconds, collectedCoinIDs: collectedCoinIDs)
        }
        if level == 3 {
            return completeMazeLevelThree(seconds: seconds, collectedCoinIDs: collectedCoinIDs)
        }
        if level == 1 {
            let firstWin = completeMazeLevelOne(seconds: seconds)
            return MazeReward(baseCoins: firstWin ? mazeLevelOneCoins : 0,
                              collectedCoins: 0, points: firstWin ? mazeLevelOnePoints : 0)
        }
        guard level == 2, mazeLevelOneCompleted, seconds.isFinite, seconds >= 0 else {
            return .none
        }

        if mazeLevelTwoBestSeconds == 0 || seconds < mazeLevelTwoBestSeconds {
            mazeLevelTwoBestSeconds = max(seconds, 0.01)
        }
        let firstWin = !mazeLevelTwoCompleted
        let baseCoins = firstWin ? mazeLevelTwoCoins : 0
        let points = firstWin ? mazeLevelTwoPoints : 0
        var newCoinCount = 0
        var mask = mazeLevelTwoClaimedCoinMask
        for id in collectedCoinIDs where (0..<5).contains(id) {
            let bit = 1 << id
            if mask & bit == 0 {
                mask |= bit
                newCoinCount += 1
            }
        }

        mazeLevelTwoCompleted = true
        mazeLevelTwoClaimedCoinMask = mask
        let collectedCoins = newCoinCount * 10
        addCoins(baseCoins + collectedCoins)
        gamePoints += points
        return MazeReward(baseCoins: baseCoins, collectedCoins: collectedCoins, points: points)
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
        didSet {
            save(streak, key: "streak")
            if !isLoading { bestAchievementStreak = max(bestAchievementStreak, streak) }
        }
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

    let redShirtPrice = 100

    @Published private(set) var redShirtPurchased = false {
        didSet { save(redShirtPurchased, key: "redShirtPurchased") }
    }

    @Published private(set) var redShirtEquipped = false {
        didSet { save(redShirtEquipped, key: "redShirtEquipped") }
    }

    @discardableResult
    func purchaseRedShirt() -> Bool {
        guard !redShirtPurchased, coins >= redShirtPrice else { return false }
        coins -= redShirtPrice
        redShirtPurchased = true
        recordInventoryItem(itemId: "redShirt", purchased: true, equipped: false)
        return true
    }

    func toggleRedShirt() {
        guard let item = ShopCatalog.byId("redShirt") else { return }
        toggleEquip(item)
    }

    // MARK: - FONDOS DE HOME

    @Published private(set) var purchasedHomeBackgrounds: Set<HomeBackground> = [] {
        didSet {
            save(purchasedHomeBackgrounds.map(\.rawValue).sorted(), key: "purchasedHomeBackgrounds")
        }
    }

    @Published private(set) var equippedHomeBackground: HomeBackground? {
        didSet {
            save(equippedHomeBackground?.rawValue ?? "", key: "equippedHomeBackground")
        }
    }

    @discardableResult
    func purchaseHomeBackground(_ background: HomeBackground) -> Bool {
        guard !purchasedHomeBackgrounds.contains(background), coins >= background.price else {
            return false
        }
        coins -= background.price
        purchasedHomeBackgrounds.insert(background)
        recordInventoryItem(itemId: background.rawValue, purchased: true, equipped: false)
        return true
    }

    func toggleHomeBackground(_ background: HomeBackground) {
        guard let item = ShopCatalog.byId(background.rawValue) else { return }
        toggleEquip(item)
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
        if suitEquipped { redShirtEquipped = false }
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
    let familyExerciseCount = 5
    let familyRewardCoins = 900
    let familyRewardXP = 180

    @Published private(set) var familyRewardClaimed = false {
        didSet { save(familyRewardClaimed, key: "familyRewardClaimed") }
    }
    @Published private(set) var familyBestCorrectAnswers = 0 {
        didSet { save(familyBestCorrectAnswers, key: "familyBestCorrectAnswers") }
    }
    @Published private(set) var familyBestSeconds: Double = 0 {
        didSet { save(familyBestSeconds, key: "familyBestSeconds") }
    }

    /// Se registra al terminar los cinco ejercicios; resultados solo anima el recibo.
    func completeFamilyLesson(correctAnswers: Int, seconds: Double) -> FamilyLessonReward? {
        guard greetingsRewardClaimed, (0...familyExerciseCount).contains(correctAnswers),
              seconds.isFinite, seconds >= 0 else { return nil }
        familyBestCorrectAnswers = max(familyBestCorrectAnswers, correctAnswers)
        if familyBestSeconds == 0 || seconds < familyBestSeconds {
            familyBestSeconds = max(0.01, seconds)
        }
        let startingXP = totalXP
        let firstCompletion = !familyRewardClaimed
        if firstCompletion {
            familyRewardClaimed = true
            addCoins(familyRewardCoins)
            addXP(familyRewardXP)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = max(pathLessonsCompleted, 2)
        unlockedPathLesson = max(unlockedPathLesson, 3)
        return FamilyLessonReward(coins: firstCompletion ? familyRewardCoins : 0,
                                  xp: firstCompletion ? familyRewardXP : 0,
                                  startingXP: startingXP)
    }

    let numbersExerciseCount = 5
    let numbersRewardCoins = 1000
    let numbersRewardXP = 200

    @Published private(set) var numbersRewardClaimed = false {
        didSet { save(numbersRewardClaimed, key: "numbersRewardClaimed") }
    }
    @Published private(set) var numbersBestCorrectAnswers = 0 {
        didSet { save(numbersBestCorrectAnswers, key: "numbersBestCorrectAnswers") }
    }
    @Published private(set) var numbersBestSeconds: Double = 0 {
        didSet { save(numbersBestSeconds, key: "numbersBestSeconds") }
    }

    /// Se registra al terminar los cinco ejercicios; resultados solo anima el recibo.
    func completeNumbersLesson(correctAnswers: Int, seconds: Double) -> FamilyLessonReward? {
        guard familyRewardClaimed, (0...numbersExerciseCount).contains(correctAnswers),
              seconds.isFinite, seconds >= 0 else { return nil }
        numbersBestCorrectAnswers = max(numbersBestCorrectAnswers, correctAnswers)
        if numbersBestSeconds == 0 || seconds < numbersBestSeconds {
            numbersBestSeconds = max(0.01, seconds)
        }
        let startingXP = totalXP
        let firstCompletion = !numbersRewardClaimed
        if firstCompletion {
            numbersRewardClaimed = true
            addCoins(numbersRewardCoins)
            addXP(numbersRewardXP)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = max(pathLessonsCompleted, 3)
        unlockedPathLesson = max(unlockedPathLesson, 4)
        return FamilyLessonReward(coins: firstCompletion ? numbersRewardCoins : 0,
                                  xp: firstCompletion ? numbersRewardXP : 0,
                                  startingXP: startingXP)
    }

    let colorsExerciseCount = 5
    let colorsRewardCoins = 1100
    let colorsRewardXP = 220

    @Published private(set) var colorsRewardClaimed = false {
        didSet { save(colorsRewardClaimed, key: "colorsRewardClaimed") }
    }
    @Published private(set) var colorsBestCorrectAnswers = 0 {
        didSet { save(colorsBestCorrectAnswers, key: "colorsBestCorrectAnswers") }
    }
    @Published private(set) var colorsBestSeconds: Double = 0 {
        didSet { save(colorsBestSeconds, key: "colorsBestSeconds") }
    }

    /// Se registra al terminar los cinco ejercicios; resultados solo anima el recibo.
    func completeColorsLesson(correctAnswers: Int, seconds: Double) -> FamilyLessonReward? {
        guard numbersRewardClaimed, (0...colorsExerciseCount).contains(correctAnswers),
              seconds.isFinite, seconds >= 0 else { return nil }
        colorsBestCorrectAnswers = max(colorsBestCorrectAnswers, correctAnswers)
        if colorsBestSeconds == 0 || seconds < colorsBestSeconds {
            colorsBestSeconds = max(0.01, seconds)
        }
        let startingXP = totalXP
        let firstCompletion = !colorsRewardClaimed
        if firstCompletion {
            colorsRewardClaimed = true
            addCoins(colorsRewardCoins)
            addXP(colorsRewardXP)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = max(pathLessonsCompleted, 4)
        unlockedPathLesson = max(unlockedPathLesson, 5)
        return FamilyLessonReward(coins: firstCompletion ? colorsRewardCoins : 0,
                                  xp: firstCompletion ? colorsRewardXP : 0,
                                  startingXP: startingXP)
    }

    let animalsExerciseCount = 5
    let animalsRewardCoins = 1200
    let animalsRewardXP = 240

    @Published private(set) var animalsRewardClaimed = false {
        didSet { save(animalsRewardClaimed, key: "animalsRewardClaimed") }
    }
    @Published private(set) var animalsBestCorrectAnswers = 0 {
        didSet { save(animalsBestCorrectAnswers, key: "animalsBestCorrectAnswers") }
    }
    @Published private(set) var animalsBestSeconds: Double = 0 {
        didSet { save(animalsBestSeconds, key: "animalsBestSeconds") }
    }

    /// Se registra al terminar los cinco ejercicios; resultados solo anima el recibo.
    func completeAnimalsLesson(correctAnswers: Int, seconds: Double) -> FamilyLessonReward? {
        guard colorsRewardClaimed, (0...animalsExerciseCount).contains(correctAnswers),
              seconds.isFinite, seconds >= 0 else { return nil }
        animalsBestCorrectAnswers = max(animalsBestCorrectAnswers, correctAnswers)
        if animalsBestSeconds == 0 || seconds < animalsBestSeconds {
            animalsBestSeconds = max(0.01, seconds)
        }
        let startingXP = totalXP
        let firstCompletion = !animalsRewardClaimed
        if firstCompletion {
            animalsRewardClaimed = true
            addCoins(animalsRewardCoins)
            addXP(animalsRewardXP)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = max(pathLessonsCompleted, 5)
        unlockedPathLesson = max(unlockedPathLesson, 6)
        return FamilyLessonReward(coins: firstCompletion ? animalsRewardCoins : 0,
                                  xp: firstCompletion ? animalsRewardXP : 0,
                                  startingXP: startingXP)
    }

    let foodExerciseCount = 5
    let foodRewardCoins = 1300
    let foodRewardXP = 260

    @Published private(set) var foodRewardClaimed = false {
        didSet { save(foodRewardClaimed, key: "foodRewardClaimed") }
    }
    @Published private(set) var foodBestCorrectAnswers = 0 {
        didSet { save(foodBestCorrectAnswers, key: "foodBestCorrectAnswers") }
    }
    @Published private(set) var foodBestSeconds: Double = 0 {
        didSet { save(foodBestSeconds, key: "foodBestSeconds") }
    }

    /// Se registra al terminar los cinco ejercicios; resultados solo anima el recibo.
    func completeFoodLesson(correctAnswers: Int, seconds: Double) -> FamilyLessonReward? {
        guard animalsRewardClaimed, (0...foodExerciseCount).contains(correctAnswers),
              seconds.isFinite, seconds >= 0 else { return nil }
        foodBestCorrectAnswers = max(foodBestCorrectAnswers, correctAnswers)
        if foodBestSeconds == 0 || seconds < foodBestSeconds {
            foodBestSeconds = max(0.01, seconds)
        }
        let startingXP = totalXP
        let firstCompletion = !foodRewardClaimed
        if firstCompletion {
            foodRewardClaimed = true
            addCoins(foodRewardCoins)
            addXP(foodRewardXP)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = max(pathLessonsCompleted, 6)
        unlockedPathLesson = max(unlockedPathLesson, 7)
        return FamilyLessonReward(coins: firstCompletion ? foodRewardCoins : 0,
                                  xp: firstCompletion ? foodRewardXP : 0,
                                  startingXP: startingXP)
    }

    @Published var greetingsRewardClaimed = false {
        didSet {
            save(greetingsRewardClaimed, key: "greetingsRewardClaimed")
        }
    }

    // MARK: - LOGROS

    @Published private(set) var totalFeedings = 0 {
        didSet { save(totalFeedings, key: "totalFeedings") }
    }
    @Published private(set) var lifetimeEarnedCoins = 0 {
        didSet { save(lifetimeEarnedCoins, key: "lifetimeEarnedCoins") }
    }
    @Published private(set) var bestAchievementStreak = 0 {
        didSet { save(bestAchievementStreak, key: "bestAchievementStreak") }
    }

    func recordFeeding() {
        totalFeedings += 1
    }

    private func loadAchievementProgress() {
        // Older versions retained only recent meals, not a lifetime feeding count.
        let retainedMeals = (storage.array(forKey: "zentemi.mealDates") as? [Date])?.count ?? 0
        totalFeedings = max(0, loadInt("totalFeedings", default: retainedMeals))
        bestAchievementStreak = max(streak, loadInt("bestAchievementStreak", default: 0))

        // Reconstruct known rewards once, including coins already spent.
        // The initial gift of 1,500 coins is not an earned reward.
        let lessonRewards: [(Bool, Int)] = [
            (greetingsRewardClaimed, 700), (familyRewardClaimed, familyRewardCoins),
            (numbersRewardClaimed, numbersRewardCoins), (colorsRewardClaimed, colorsRewardCoins),
            (animalsRewardClaimed, animalsRewardCoins), (foodRewardClaimed, foodRewardCoins)
        ]
        let mazeRewards: [(Bool, Int)] = [
            (mazeLevelOneCompleted, mazeLevelOneCoins), (mazeLevelTwoCompleted, mazeLevelTwoCoins),
            (mazeLevelThreeCompleted, mazeLevelThreeCoins), (mazeLevelFourCompleted, mazeLevelFourCoins),
            (mazeLevelFiveCompleted, mazeLevelFiveCoins)
        ]
        var recovered = (lessonRewards + mazeRewards).reduce(0) { $0 + ($1.0 ? $1.1 : 0) }
        for level in [MemoryLevel.one, .two, .three] where memoryCompleted(level) {
            recovered += level.coins
        }
        for level in [WordSearchLevel.one, .two, .three] where wordSearchCompleted(level) {
            recovered += level.coins
        }
        for level in 2...5 {
            recovered += mazeClaimedCoinMask(for: level).nonzeroBitCount * 10
        }
        lifetimeEarnedCoins = max(0, loadInt("lifetimeEarnedCoins", default: recovered))
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
            addCoins(coinsEarned)
        }
        happiness = min(100, happiness + happinessEarned)
        return true
    }

    // MARK: - RECOMPENSAS

    func addCoins(_ amount: Int) {
        coins += amount
        if amount > 0 { lifetimeEarnedCoins += amount }
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
        loadAchievementProgress()
        updateHappinessDecay()
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
        gamePoints = max(0, loadInt("gamePoints", default: 0))
        wordSearchLevelTwoCompleted = loadBool("wordSearchLevelTwoCompleted", default: false)
        wordSearchLevelTwoBestSeconds = max(0, loadDouble("wordSearchLevelTwoBestSeconds", default: 0))
        wordSearchLevelThreeCompleted = loadBool("wordSearchLevelThreeCompleted", default: false)
        wordSearchLevelThreeBestSeconds = max(0, loadDouble("wordSearchLevelThreeBestSeconds", default: 0))
        wordSearchLevelOneCompleted = loadBool("wordSearchLevelOneCompleted", default: false)
        wordSearchLevelOneBestSeconds = max(0, loadDouble("wordSearchLevelOneBestSeconds", default: 0))
        memoryLevelOneCompleted = loadBool("memoryLevelOneCompleted", default: false)
        memoryLevelOneBestSeconds = max(0, loadDouble("memoryLevelOneBestSeconds", default: 0))
        memoryLevelTwoCompleted = loadBool("memoryLevelTwoCompleted", default: false)
        memoryLevelTwoBestSeconds = max(0, loadDouble("memoryLevelTwoBestSeconds", default: 0))
        memoryLevelThreeCompleted = loadBool("memoryLevelThreeCompleted", default: false)
        memoryLevelThreeBestSeconds = max(0, loadDouble("memoryLevelThreeBestSeconds", default: 0))
        mazeLevelOneCompleted = loadBool("mazeLevelOneCompleted", default: false)
        mazeLevelOneBestSeconds = max(0, loadDouble("mazeLevelOneBestSeconds", default: 0))
        mazeLevelTwoCompleted = loadBool("mazeLevelTwoCompleted", default: false)
        mazeLevelTwoBestSeconds = max(0, loadDouble("mazeLevelTwoBestSeconds", default: 0))
        mazeLevelTwoClaimedCoinMask = loadInt("mazeLevelTwoClaimedCoinMask", default: 0) & 31
        mazeLevelThreeCompleted = loadBool("mazeLevelThreeCompleted", default: false)
        mazeLevelThreeBestSeconds = max(0, loadDouble("mazeLevelThreeBestSeconds", default: 0))
        mazeLevelThreeClaimedCoinMask = loadInt("mazeLevelThreeClaimedCoinMask", default: 0) & 1023
        mazeLevelFourCompleted = loadBool("mazeLevelFourCompleted", default: false)
        mazeLevelFourBestSeconds = max(0, loadDouble("mazeLevelFourBestSeconds", default: 0))
        mazeLevelFourClaimedCoinMask = loadInt("mazeLevelFourClaimedCoinMask", default: 0) & 32767
        mazeLevelFiveCompleted = loadBool("mazeLevelFiveCompleted", default: false)
        mazeLevelFiveBestSeconds = max(0, loadDouble("mazeLevelFiveBestSeconds", default: 0))
        mazeLevelFiveClaimedCoinMask = loadInt("mazeLevelFiveClaimedCoinMask", default: 0) & 1_048_575

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

        redShirtPurchased = loadBool("redShirtPurchased", default: false)
        redShirtEquipped = redShirtPurchased && loadBool("redShirtEquipped", default: false)
        if redShirtEquipped { suitEquipped = false }

        let savedBackgrounds = storage.stringArray(forKey: "zentemi.purchasedHomeBackgrounds") ?? []
        purchasedHomeBackgrounds = Set(savedBackgrounds.compactMap { HomeBackground(rawValue: $0) })
        let savedBackground = HomeBackground(rawValue: loadString("equippedHomeBackground", default: ""))
        equippedHomeBackground = savedBackground.flatMap {
            purchasedHomeBackgrounds.contains($0) ? $0 : nil
        }

        // Lecciones
        pathLessonsCompleted = loadInt(
            "pathLessonsCompleted",
            default: pathLessonsCompleted
        )

        unlockedPathLesson = loadInt(
            "unlockedPathLesson",
            default: unlockedPathLesson
        )

        familyRewardClaimed = loadBool("familyRewardClaimed", default: false)
        familyBestCorrectAnswers = min(5, max(0, loadInt("familyBestCorrectAnswers", default: 0)))
        familyBestSeconds = max(0, loadDouble("familyBestSeconds", default: 0))
        numbersRewardClaimed = loadBool("numbersRewardClaimed", default: false)
        numbersBestCorrectAnswers = min(5, max(0, loadInt("numbersBestCorrectAnswers", default: 0)))
        numbersBestSeconds = max(0, loadDouble("numbersBestSeconds", default: 0))
        colorsRewardClaimed = loadBool("colorsRewardClaimed", default: false)
        colorsBestCorrectAnswers = min(5, max(0, loadInt("colorsBestCorrectAnswers", default: 0)))
        colorsBestSeconds = max(0, loadDouble("colorsBestSeconds", default: 0))
        animalsRewardClaimed = loadBool("animalsRewardClaimed", default: false)
        animalsBestCorrectAnswers = min(5, max(0, loadInt("animalsBestCorrectAnswers", default: 0)))
        animalsBestSeconds = max(0, loadDouble("animalsBestSeconds", default: 0))
        foodRewardClaimed = loadBool("foodRewardClaimed", default: false)
        foodBestCorrectAnswers = min(5, max(0, loadInt("foodBestCorrectAnswers", default: 0)))
        foodBestSeconds = max(0, loadDouble("foodBestSeconds", default: 0))
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
        if redShirtPurchased { ids.insert("redShirt") }
        ids.formUnion(purchasedHomeBackgrounds.map(\.rawValue))
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
        if redShirtEquipped { ids.insert("redShirt") }
        if let background = equippedHomeBackground { ids.insert(background.rawValue) }
        return ids.sorted()
    }

    private func applyOwnedItems(_ ids: Set<String>) {
        sunglassesPurchased = ids.contains("sunglasses")
        pinkHairPurchased = ids.contains("pinkHair")
        swordPurchased = ids.contains("sword")
        suitPurchased = ids.contains("suit")
        redShirtPurchased = ids.contains("redShirt")
        purchasedHomeBackgrounds = Set(ids.compactMap { HomeBackground(rawValue: $0) })
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
        redShirtEquipped = redShirtPurchased && ids.contains("redShirt")
        if redShirtEquipped { suitEquipped = false }
        equippedHomeBackground = HomeBackground.allCases.first {
            ids.contains($0.rawValue) && purchasedHomeBackgrounds.contains($0)
        }
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
        storage.set(redShirtPurchased, forKey: "zentemi.redShirtPurchased")
        storage.set(redShirtEquipped, forKey: "zentemi.redShirtEquipped")
        storage.set(purchasedHomeBackgrounds.map(\.rawValue).sorted(), forKey: "zentemi.purchasedHomeBackgrounds")
        storage.set(equippedHomeBackground?.rawValue ?? "", forKey: "zentemi.equippedHomeBackground")
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
        gamePoints = 0
        wordSearchLevelTwoCompleted = false
        wordSearchLevelTwoBestSeconds = 0
        wordSearchLevelThreeCompleted = false
        wordSearchLevelThreeBestSeconds = 0
        wordSearchLevelOneCompleted = false
        wordSearchLevelOneBestSeconds = 0
        memoryLevelOneCompleted = false
        memoryLevelOneBestSeconds = 0
        memoryLevelTwoCompleted = false
        memoryLevelTwoBestSeconds = 0
        memoryLevelThreeCompleted = false
        memoryLevelThreeBestSeconds = 0
        mazeLevelOneCompleted = false
        mazeLevelOneBestSeconds = 0
        mazeLevelTwoCompleted = false
        mazeLevelTwoBestSeconds = 0
        mazeLevelTwoClaimedCoinMask = 0
        mazeLevelThreeCompleted = false
        mazeLevelThreeBestSeconds = 0
        mazeLevelThreeClaimedCoinMask = 0
        mazeLevelFourCompleted = false
        mazeLevelFourBestSeconds = 0
        mazeLevelFourClaimedCoinMask = 0
        mazeLevelFiveCompleted = false
        mazeLevelFiveBestSeconds = 0
        mazeLevelFiveClaimedCoinMask = 0

        coins = 1500
        totalFeedings = 0
        lifetimeEarnedCoins = 0
        bestAchievementStreak = 0
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
        redShirtPurchased = false
        purchasedHomeBackgrounds = []
        equippedHomeBackground = nil
        redShirtEquipped = false

        sunglassesEquipped = false
        pinkHairEquipped = false
        swordEquipped = false
        suitEquipped = false

        pathLessonsCompleted = 0
        unlockedPathLesson = 1
        familyRewardClaimed = false
        familyBestCorrectAnswers = 0
        familyBestSeconds = 0
        numbersRewardClaimed = false
        numbersBestCorrectAnswers = 0
        numbersBestSeconds = 0
        colorsRewardClaimed = false
        colorsBestCorrectAnswers = 0
        colorsBestSeconds = 0
        animalsRewardClaimed = false
        animalsBestCorrectAnswers = 0
        animalsBestSeconds = 0
        foodRewardClaimed = false
        foodBestCorrectAnswers = 0
        foodBestSeconds = 0
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
