import Foundation
import FirebaseFirestore

// MARK: - Perfil de Adulto / Tutor (`users/{uid}`)

/// Modelo documental para la cuenta del tutor legal en Firestore
public struct ParentProfile: Codable, Identifiable {
    @DocumentID public var id: String?
    public var email: String
    public var acceptedPrivacyPolicy: Bool
    public var createdAt: Date
    public var lastLogin: Date?

    public init(
        id: String? = nil,
        email: String,
        acceptedPrivacyPolicy: Bool = true,
        createdAt: Date = Date(),
        lastLogin: Date? = Date()
    ) {
        self.id = id
        self.email = email
        self.acceptedPrivacyPolicy = acceptedPrivacyPolicy
        self.createdAt = createdAt
        self.lastLogin = lastLogin
    }
}

// MARK: - Perfil del Menor (`users/{uid}/children/{childId}`)

/// Modelo documental para el perfil infantil subordinado a la cuenta del tutor
public struct ChildProfile: Codable, Identifiable, Equatable {
    @DocumentID public var id: String?
    public var userName: String
    public var lastUserNameChange: Date?
    public var petName: String
    public var happiness: Int
    public var coins: Int
    public var totalXP: Int
    public var currentLevel: Int
    public var streak: Int
    public var lastPracticeDate: Date?
    public var lessonsCompleted: Int
    public var avatar: ChildAvatarSettings
    public var settings: ChildAppSettings
    public var createdAt: Date

    // Campos compartidos con Android v1.1 (opcionales para compatibilidad con perfiles viejos)
    public var pathLessonsCompleted: Int?
    public var unlockedPathLesson: Int?
    public var greetingsRewardClaimed: Bool?
    public var ownedItems: [String]?
    public var equippedItems: [String]?

    public init(
        id: String? = nil,
        userName: String = "Jugador",
        lastUserNameChange: Date? = nil,
        petName: String = "Maicito",
        happiness: Int = 50,
        coins: Int = 1500,
        totalXP: Int = 0,
        currentLevel: Int = 1,
        streak: Int = 0,
        lastPracticeDate: Date? = nil,
        lessonsCompleted: Int = 0,
        avatar: ChildAvatarSettings = ChildAvatarSettings(),
        settings: ChildAppSettings = ChildAppSettings(),
        createdAt: Date = Date(),
        pathLessonsCompleted: Int? = nil,
        unlockedPathLesson: Int? = nil,
        greetingsRewardClaimed: Bool? = nil,
        ownedItems: [String]? = nil,
        equippedItems: [String]? = nil
    ) {
        self.id = id
        self.userName = userName
        self.lastUserNameChange = lastUserNameChange
        self.petName = petName
        self.happiness = happiness
        self.coins = coins
        self.totalXP = totalXP
        self.currentLevel = currentLevel
        self.streak = streak
        self.lastPracticeDate = lastPracticeDate
        self.lessonsCompleted = lessonsCompleted
        self.avatar = avatar
        self.settings = settings
        self.createdAt = createdAt
        self.pathLessonsCompleted = pathLessonsCompleted
        self.unlockedPathLesson = unlockedPathLesson
        self.greetingsRewardClaimed = greetingsRewardClaimed
        self.ownedItems = ownedItems
        self.equippedItems = equippedItems
    }

    public static func == (lhs: ChildProfile, rhs: ChildProfile) -> Bool {
        lhs.id == rhs.id &&
        lhs.userName == rhs.userName &&
        lhs.lastUserNameChange == rhs.lastUserNameChange &&
        lhs.petName == rhs.petName &&
        lhs.happiness == rhs.happiness &&
        lhs.coins == rhs.coins &&
        lhs.totalXP == rhs.totalXP &&
        lhs.currentLevel == rhs.currentLevel &&
        lhs.streak == rhs.streak &&
        lhs.lastPracticeDate == rhs.lastPracticeDate &&
        lhs.lessonsCompleted == rhs.lessonsCompleted &&
        lhs.avatar == rhs.avatar &&
        lhs.settings == rhs.settings &&
        lhs.createdAt == rhs.createdAt &&
        lhs.pathLessonsCompleted == rhs.pathLessonsCompleted &&
        lhs.unlockedPathLesson == rhs.unlockedPathLesson &&
        lhs.greetingsRewardClaimed == rhs.greetingsRewardClaimed &&
        lhs.ownedItems == rhs.ownedItems &&
        lhs.equippedItems == rhs.equippedItems
    }
}

// MARK: - Configuraciones de Avatar

public struct ChildAvatarSettings: Codable, Equatable {
    public var avatarBackgroundHue: Double
    public var mascotHue: Double
    public var profileBackgroundHue: Double

    public init(
        avatarBackgroundHue: Double = 0.72,
        mascotHue: Double = 0.0,
        profileBackgroundHue: Double = 0.73
    ) {
        self.avatarBackgroundHue = avatarBackgroundHue
        self.mascotHue = mascotHue
        self.profileBackgroundHue = profileBackgroundHue
    }
}

// MARK: - Configuraciones de la App

public struct ChildAppSettings: Codable, Equatable {
    public var musicVolume: Double
    public var soundVolume: Double

    public init(musicVolume: Double = 0.35, soundVolume: Double = 1.0) {
        self.musicVolume = musicVolume
        self.soundVolume = soundVolume
    }
}

// MARK: - Progreso de Lección (`users/{uid}/children/{childId}/progress/{lessonId}`)

public struct LessonProgressRecord: Codable, Identifiable {
    @DocumentID public var id: String?
    public var lessonId: String
    public var completed: Bool
    public var rewardClaimed: Bool
    public var correctAnswers: Int
    public var totalAnswers: Int
    public var elapsedSeconds: Int
    public var completedAt: Date

    public init(
        id: String? = nil,
        lessonId: String,
        completed: Bool = false,
        rewardClaimed: Bool = false,
        correctAnswers: Int = 0,
        totalAnswers: Int = 0,
        elapsedSeconds: Int = 0,
        completedAt: Date = Date()
    ) {
        self.id = id
        self.lessonId = lessonId
        self.completed = completed
        self.rewardClaimed = rewardClaimed
        self.correctAnswers = correctAnswers
        self.totalAnswers = totalAnswers
        self.elapsedSeconds = elapsedSeconds
        self.completedAt = completedAt
    }
}

// MARK: - Inventario y Tienda (`users/{uid}/children/{childId}/inventory/{itemId}`)

public struct InventoryItemRecord: Codable, Identifiable {
    @DocumentID public var id: String?
    public var itemId: String
    public var purchased: Bool
    public var equipped: Bool
    public var purchasedAt: Date?

    public init(
        id: String? = nil,
        itemId: String,
        purchased: Bool = false,
        equipped: Bool = false,
        purchasedAt: Date? = nil
    ) {
        self.id = id
        self.itemId = itemId
        self.purchased = purchased
        self.equipped = equipped
        self.purchasedAt = purchasedAt
    }
}

// MARK: - Bitácora de Auditoría en la Nube (`users/{uid}/audit_logs/{logId}`)

public struct AuditLogRecord: Codable, Identifiable {
    @DocumentID public var id: String?
    public var eventType: String
    public var details: String
    public var deviceModel: String
    @ServerTimestamp public var timestamp: Date?

    public init(
        id: String? = nil,
        eventType: String,
        details: String,
        deviceModel: String = "iOS Device",
        timestamp: Date? = nil
    ) {
        self.id = id
        self.eventType = eventType
        self.details = details
        self.deviceModel = deviceModel
        self.timestamp = timestamp
    }
}
