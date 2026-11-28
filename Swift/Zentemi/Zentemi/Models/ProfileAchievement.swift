import Foundation

struct ProfileAchievement: Identifiable {
    let id: String
    let title: String
    let detail: String
    let imageName: String
    let current: Int
    let target: Int

    var isCompleted: Bool { current >= target }
    var displayedProgress: Int { min(max(0, current), target) }
}

extension AppState {
    var profileAchievements: [ProfileAchievement] {
        let completedGames = [
            mazeLevelOneCompleted && mazeLevelTwoCompleted && mazeLevelThreeCompleted
                && mazeLevelFourCompleted && mazeLevelFiveCompleted,
            memoryLevelOneCompleted && memoryLevelTwoCompleted && memoryLevelThreeCompleted,
            wordSearchLevelOneCompleted && wordSearchLevelTwoCompleted && wordSearchLevelThreeCompleted
        ].filter { $0 }.count
        let completedLessons = [
            greetingsRewardClaimed, familyRewardClaimed, numbersRewardClaimed,
            colorsRewardClaimed, animalsRewardClaimed, foodRewardClaimed
        ].filter { $0 }.count

        return [
            ProfileAchievement(id: "games", title: "Todos los juegos",
                detail: "Completa todos los niveles de Laberinto, Memorama y Sopa de letras.",
                imageName: "achievementGames", current: completedGames, target: 3),
            ProfileAchievement(id: "lessons", title: "Todas las lecciones",
                detail: "Completa las seis lecciones disponibles, de Primeras frases a Alimentos.",
                imageName: "achievementLessons", current: completedLessons, target: 6),
            ProfileAchievement(id: "feed10", title: "10 comidas",
                detail: "Dale de comer a Maicito 10 veces.",
                imageName: "achievementFeed10", current: totalFeedings, target: 10),
            ProfileAchievement(id: "feed50", title: "50 comidas",
                detail: "Dale de comer a Maicito 50 veces.",
                imageName: "achievementFeed50", current: totalFeedings, target: 50),
            ProfileAchievement(id: "feed100", title: "100 comidas",
                detail: "Dale de comer a Maicito 100 veces.",
                imageName: "achievementFeed100", current: totalFeedings, target: 100),
            ProfileAchievement(id: "coins1000", title: "1,000 monedas",
                detail: "Gana 1,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                imageName: "achievementCoins1000", current: lifetimeEarnedCoins, target: 1_000),
            ProfileAchievement(id: "coins5000", title: "5,000 monedas",
                detail: "Gana 5,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                imageName: "achievementCoins5000", current: lifetimeEarnedCoins, target: 5_000),
            ProfileAchievement(id: "coins10000", title: "10,000 monedas",
                detail: "Gana 10,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                imageName: "achievementCoins10000", current: lifetimeEarnedCoins, target: 10_000),
            ProfileAchievement(id: "streak5", title: "Racha de 5 días",
                detail: "Entra 5 días consecutivos. El logro se conserva aunque termine la racha.",
                imageName: "achievementStreak5", current: bestAchievementStreak, target: 5),
            ProfileAchievement(id: "streak10", title: "Racha de 10 días",
                detail: "Entra 10 días consecutivos. El logro se conserva aunque termine la racha.",
                imageName: "achievementStreak10", current: bestAchievementStreak, target: 10),
            ProfileAchievement(id: "streak20", title: "Racha de 20 días",
                detail: "Entra 20 días consecutivos. El logro se conserva aunque termine la racha.",
                imageName: "achievementStreak20", current: bestAchievementStreak, target: 20)
        ]
    }
}
