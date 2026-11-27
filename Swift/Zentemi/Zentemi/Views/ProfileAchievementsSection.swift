import SwiftUI

struct ProfileAchievementsSection: View {
    let achievements: [ProfileAchievement]
    @State private var selectedAchievement: ProfileAchievement?

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 10), count: 3)

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Logros")
                    .font(.headline)
                Spacer()
                Text("\(achievements.filter { $0.isCompleted }.count)/\(achievements.count)")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(.secondary)
            }

            LazyVGrid(columns: columns, alignment: .leading, spacing: 12) {
                ForEach(achievements) { achievement in
                    Button {
                        selectedAchievement = achievement
                    } label: {
                        ProfileAchievementCard(achievement: achievement)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(achievement.title)
                    .accessibilityValue(achievement.isCompleted
                        ? "Completado"
                        : "\(achievement.displayedProgress) de \(achievement.target)")
                    .accessibilityHint("Muestra los detalles del logro")
                }
            }
        }
        .sheet(item: $selectedAchievement) { achievement in
            ProfileAchievementDetail(achievement: achievement)
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
        }
    }
}

private struct ProfileAchievementCard: View {
    let achievement: ProfileAchievement

    var body: some View {
        VStack(spacing: 8) {
            Image(achievement.imageName)
                .resizable()
                .scaledToFit()
                .frame(maxWidth: 76)
                .aspectRatio(1, contentMode: .fit)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .saturation(achievement.isCompleted ? 1 : 0)
                .opacity(achievement.isCompleted ? 1 : 0.55)
                .overlay(alignment: .bottomTrailing) {
                    Image(systemName: achievement.isCompleted ? "checkmark.circle.fill" : "lock.circle.fill")
                        .foregroundStyle(achievement.isCompleted ? Color.teal : Color.gray)
                        .background(.white, in: Circle())
                        .font(.body)
                }
                .accessibilityHidden(true)

            Text(achievement.title)
                .font(.caption.weight(.semibold))
                .foregroundStyle(.primary)
                .multilineTextAlignment(.center)
                .frame(minHeight: 34)
                .fixedSize(horizontal: false, vertical: true)

            ProgressView(value: Double(achievement.displayedProgress), total: Double(achievement.target))
                .tint(achievement.isCompleted ? .teal : .purple)
                .accessibilityHidden(true)

            Text("\(achievement.displayedProgress)/\(achievement.target)")
                .font(.caption2.monospacedDigit())
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(.white, in: RoundedRectangle(cornerRadius: 15))
        .overlay {
            RoundedRectangle(cornerRadius: 15)
                .strokeBorder(achievement.isCompleted ? Color.teal.opacity(0.45) : Color.clear, lineWidth: 1.5)
        }
        .shadow(color: Color.purple.opacity(0.04), radius: 6, y: 3)
    }
}

private struct ProfileAchievementDetail: View {
    let achievement: ProfileAchievement
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Image(achievement.imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 110, height: 110)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .accessibilityHidden(true)
                Text(achievement.title)
                    .font(.title2.bold())
                Text(achievement.detail)
                    .multilineTextAlignment(.center)
                Text(achievement.isCompleted ? "¡Logro completado!" : "Sigue avanzando")
                    .font(.headline)
                    .foregroundStyle(achievement.isCompleted ? Color.teal : Color.purple)
                Text("\(achievement.displayedProgress) de \(achievement.target)")
                    .font(.subheadline.monospacedDigit())
                Button("Entendido") { dismiss() }
                    .buttonStyle(.borderedProminent)
                    .tint(.purple)
            }
            .frame(maxWidth: .infinity)
            .padding(24)
        }
    }
}
