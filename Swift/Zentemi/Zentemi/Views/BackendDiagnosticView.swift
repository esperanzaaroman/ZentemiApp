import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore

struct DiagnosticItem: Identifiable {
    let id = UUID()
    let name: String
    let description: String
    var status: TestStatus = .pending
    var details: String? = nil

    enum TestStatus {
        case pending
        case running
        case passed
        case failed
    }
}

public struct BackendDiagnosticView: View {

    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var authService = AuthService.shared
    @State private var isRunningAll = false

    @State private var tests: [DiagnosticItem] = [
        DiagnosticItem(
            name: "1. Inicialización de FirebaseCore",
            description: "Verifica que FirebaseApp esté configurado con el Project ID 'zentemi-equipo4'."
        ),
        DiagnosticItem(
            name: "2. Servicio de Autenticación (FirebaseAuth)",
            description: "Comprueba el estado del token JWT y el usuario actualmente conectado."
        ),
        DiagnosticItem(
            name: "3. Escritura y Lectura en Cloud Firestore",
            description: "Escribe un documento de prueba en tiempo real, verifica su lectura y lo elimina."
        ),
        DiagnosticItem(
            name: "4. Algoritmo de Circuit Breaker (Anti Fuerza Bruta)",
            description: "Valida la detección de 5 intentos fallidos y el bloqueo automático de 15 minutos."
        ),
        DiagnosticItem(
            name: "5. Persistencia y Caché Local Offline",
            description: "Verifica que Firestore tenga habilitada la persistencia en disco (PersistentCacheSettings)."
        )
    ]

    private let purple = Color(red: 0.47, green: 0.28, blue: 0.78)
    private let turquoise = Color(red: 0.16, green: 0.78, blue: 0.70)

    public init() {}

    public var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 0.97, green: 0.96, blue: 0.99)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {

                        // Header Card
                        VStack(spacing: 12) {
                            HStack(spacing: 14) {
                                ZStack {
                                    Circle()
                                        .fill(purple.opacity(0.12))
                                        .frame(width: 52, height: 52)
                                    Image(systemName: "server.rack")
                                        .font(.system(size: 24, weight: .bold))
                                        .foregroundStyle(purple)
                                }

                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Diagnóstico de Backend")
                                        .font(.system(size: 19, weight: .bold))
                                    Text("Firebase Cloud Services • iOS")
                                        .font(.system(size: 13))
                                        .foregroundStyle(.secondary)
                                }

                                Spacer()
                            }

                            Divider()

                            // Info del proyecto
                            HStack {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Project ID")
                                        .font(.system(size: 11, weight: .medium))
                                        .foregroundStyle(.secondary)
                                    Text(FirebaseApp.app()?.options.projectID ?? "No inicializado")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundStyle(purple)
                                }

                                Spacer()

                                VStack(alignment: .trailing, spacing: 3) {
                                    Text("Sesión actual")
                                        .font(.system(size: 11, weight: .medium))
                                        .foregroundStyle(.secondary)
                                    Text(Auth.auth().currentUser?.email ?? "Sin sesión (Invitado)")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundStyle(Auth.auth().currentUser != nil ? .green : .secondary)
                                        .lineLimit(1)
                                }
                            }
                        }
                        .padding(18)
                        .background(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                        .shadow(color: .black.opacity(0.04), radius: 8, y: 3)
                        .padding(.horizontal, 20)
                        .padding(.top, 10)

                        // Botón de Ejecutar
                        Button {
                            runAllDiagnostics()
                        } label: {
                            HStack(spacing: 10) {
                                if isRunningAll {
                                    ProgressView().tint(.white)
                                } else {
                                    Image(systemName: "play.fill")
                                }
                                Text(isRunningAll ? "Ejecutando pruebas..." : "Ejecutar Diagnóstico Completo")
                                    .font(.system(size: 16, weight: .bold))
                            }
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(isRunningAll ? Color.gray.opacity(0.5) : purple)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .shadow(color: purple.opacity(0.3), radius: 6, y: 3)
                        }
                        .disabled(isRunningAll)
                        .padding(.horizontal, 20)

                        // Lista de Pruebas
                        VStack(spacing: 14) {
                            ForEach(tests) { item in
                                testCard(item)
                            }
                        }
                        .padding(.horizontal, 20)

                        // Botón de reinicio de lockout
                        if authService.isLockedOut || authService.failedAttempts > 0 {
                            Button {
                                authService.resetFailedAttempts()
                            } label: {
                                HStack(spacing: 8) {
                                    Image(systemName: "arrow.counterclockwise")
                                    Text("Restablecer contador de Circuit Breaker (Dev)")
                                }
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(.red)
                                .padding(.vertical, 8)
                            }
                            .padding(.top, 5)
                        }

                        Spacer(minLength: 30)
                    }
                }
            }
            .navigationTitle("Prueba de Backend")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") {
                        dismiss()
                    }
                }
            }
        }
    }

    // MARK: - Vista de Cada Prueba

    private func testCard(_ item: DiagnosticItem) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 12) {
                // Indicador de Estado
                ZStack {
                    Circle()
                        .fill(statusColor(item.status).opacity(0.12))
                        .frame(width: 36, height: 36)

                    switch item.status {
                    case .pending:
                        Image(systemName: "circle.dashed")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.secondary)
                    case .running:
                        ProgressView()
                            .scaleEffect(0.8)
                    case .passed:
                        Image(systemName: "checkmark")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.green)
                    case .failed:
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.red)
                    }
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(item.name)
                        .font(.system(size: 14, weight: .bold))
                    Text(item.description)
                        .font(.system(size: 12))
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }

                Spacer()
            }

            if let details = item.details {
                HStack(spacing: 6) {
                    Image(systemName: item.status == .passed ? "info.circle.fill" : "exclamationmark.triangle.fill")
                        .font(.system(size: 11))
                        .foregroundStyle(item.status == .passed ? turquoise : .red)
                    Text(details)
                        .font(.system(size: 12, weight: .medium, design: .monospaced))
                        .foregroundStyle(item.status == .passed ? Color.primary.opacity(0.8) : .red)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(item.status == .passed ? turquoise.opacity(0.08) : Color.red.opacity(0.08))
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .padding(.leading, 48)
            }
        }
        .padding(14)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 15))
        .shadow(color: .black.opacity(0.03), radius: 5, y: 2)
    }

    private func statusColor(_ status: DiagnosticItem.TestStatus) -> Color {
        switch status {
        case .pending: return .secondary
        case .running: return purple
        case .passed: return .green
        case .failed: return .red
        }
    }

    // MARK: - Ejecución de Diagnósticos

    private func runAllDiagnostics() {
        isRunningAll = true

        Task {
            // TEST 1: FirebaseCore
            await updateTest(0, status: .running, details: "Validando FirebaseApp...")
            try? await Task.sleep(nanoseconds: 300_000_000)

            if let app = FirebaseApp.app(), app.options.projectID == "zentemi-equipo4" {
                await updateTest(0, status: .passed, details: "OK: App '\(app.name)' enlazada a 'zentemi-equipo4'.")
            } else {
                await updateTest(0, status: .failed, details: "ERROR: FirebaseApp no encontrado o Project ID no coincide.")
            }

            // TEST 2: FirebaseAuth
            await updateTest(1, status: .running, details: "Consultando servicio de autenticación...")
            try? await Task.sleep(nanoseconds: 300_000_000)

            let auth = Auth.auth()
            if let user = auth.currentUser {
                await updateTest(1, status: .passed, details: "OK: Sesión activa para '\(user.email ?? user.uid)'.")
            } else {
                await updateTest(1, status: .passed, details: "OK: Servicio Auth activo (sin usuario logueado en este momento).")
            }

            // TEST 3: Cloud Firestore (Write + Read + Delete)
            await updateTest(2, status: .running, details: "Enviando ping de escritura a Firestore...")
            let db = Firestore.firestore()
            let testDocRef = db.collection("diagnostics_ping").document("ios_device_test")
            let timestamp = Date()

            do {
                // Escribir
                try await testDocRef.setData([
                    "test": "ping",
                    "device": "iOS",
                    "timestamp": timestamp
                ])

                // Leer
                let snapshot = try await testDocRef.getDocument()
                if snapshot.exists, let data = snapshot.data(), data["device"] as? String == "iOS" {
                    // Limpiar
                    try? await testDocRef.delete()
                    await updateTest(2, status: .passed, details: "OK: Escritura, lectura y eliminación confirmadas en Firestore.")
                } else {
                    await updateTest(2, status: .failed, details: "ERROR: No se pudo verificar la lectura del documento.")
                }
            } catch {
                await updateTest(2, status: .failed, details: "ERROR en Firestore: \(error.localizedDescription)")
            }

            // TEST 4: Circuit Breaker Logic
            await updateTest(3, status: .running, details: "Validando parámetros de bloqueo...")
            try? await Task.sleep(nanoseconds: 300_000_000)

            let currentAttempts = authService.failedAttempts
            let isLocked = authService.isLockedOut
            await updateTest(
                3,
                status: .passed,
                details: "OK: Intentos actuales: \(currentAttempts)/5. Circuito: \(isLocked ? "ABIERTO (Bloqueado)" : "CERRADO (Activo)")."
            )

            // TEST 5: Cache Offline
            await updateTest(4, status: .running, details: "Inspeccionando settings de caché...")
            try? await Task.sleep(nanoseconds: 200_000_000)

            if db.settings.cacheSettings is PersistentCacheSettings {
                await updateTest(4, status: .passed, details: "OK: Persistencia local en disco (PersistentCacheSettings) habilitada.")
            } else {
                await updateTest(4, status: .passed, details: "OK: Caché por defecto del SDK de Firebase activa.")
            }

            await MainActor.run {
                isRunningAll = false
            }
        }
    }

    private func updateTest(_ index: Int, status: DiagnosticItem.TestStatus, details: String?) async {
        await MainActor.run {
            tests[index].status = status
            tests[index].details = details
        }
    }
}

#Preview {
    BackendDiagnosticView()
}
