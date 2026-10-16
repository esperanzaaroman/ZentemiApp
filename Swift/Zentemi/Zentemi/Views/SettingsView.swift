import SwiftUI

struct SettingsView: View {
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState
    
    // MARK: - Estados
    
    @State private var notificationsEnabled = false
    
    @State private var selectedLanguage = "Español"
    @State private var showLanguageSelector = false
    @State private var showAboutZentemi = false
    
    @State private var showLogoutConfirmation = false
    @State private var goToLogin = false
    
    
    // MARK: - Colores
    
    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )
    
    private let backgroundColor = Color(
        red: 0.975,
        green: 0.96,
        blue: 0.995
    )
    
    
    var body: some View {
        
        ZStack {
            
            // MARK: - Fondo
            
            backgroundColor
                .ignoresSafeArea()
            
            
            // Círculo decorativo inferior
            
            Circle()
                .fill(
                    Color.purple.opacity(0.025)
                )
                .frame(
                    width: 260,
                    height: 260
                )
                .offset(
                    x: 150,
                    y: 260
                )
            
            
            VStack(spacing: 0) {
                
                // MARK: - Encabezado
                
                HStack(spacing: 12) {
                    
                    Button {
                        dismiss()
                    } label: {
                        
                        Image(
                            systemName: "chevron.left"
                        )
                        .font(
                            .system(
                                size: 14,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(purple)
                        .frame(
                            width: 34,
                            height: 34
                        )
                        .background(.white)
                        .clipShape(Circle())
                    }
                    
                    
                    Text("Ajustes")
                        .font(
                            .system(
                                size: 21,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(
                            Color.primary
                        )
                    
                    
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                
                
                ScrollView(
                    showsIndicators: false
                ) {
                    
                    VStack(spacing: 16) {
                        
                        // MARK: - Opciones principales
                        
                        VStack(spacing: 0) {
                            
                            // Música

                            settingVolumeRow(
                                icon: "music.note",
                                iconColor: purple,
                                iconBackground:
                                    purple.opacity(0.10),
                                title: "Música",
                                volume: $appState.musicVolume
                            )

                            Divider()
                                .padding(.leading, 55)

                            // Sonido

                            settingVolumeRow(
                                icon: "speaker.wave.2.fill",
                                iconColor: purple,
                                iconBackground:
                                    purple.opacity(0.10),
                                title: "Sonido",
                                volume: $appState.soundVolume
                            )

                            Divider()
                                .padding(.leading, 55)
                                                
                            // Notificaciones
                            
                            settingToggleRow(
                                icon: "bell.fill",
                                iconColor: .red.opacity(0.65),
                                iconBackground:
                                    Color.red.opacity(0.09),
                                title: "Notificaciones",
                                isOn:
                                    $notificationsEnabled
                            )
                            
                            
                            Divider()
                                .padding(.leading, 55)
                            
                            
                            // Idioma
                            
                            Button {
                                
                                showLanguageSelector = true
                                
                            } label: {
                                
                                settingNavigationRow(
                                    icon: "globe",
                                    iconColor:
                                        Color.teal,
                                    iconBackground:
                                        Color.teal.opacity(0.10),
                                    title:
                                        "Idioma de la app",
                                    trailingText:
                                        selectedLanguage
                                )
                            }
                            
                            
                            Divider()
                                .padding(.leading, 55)
                            
                            
                            // Sobre Zentemi
                            
                            Button {
                                
                                showAboutZentemi = true
                                
                            } label: {
                                
                                settingNavigationRow(
                                    icon:
                                        "info.circle.fill",
                                    iconColor:
                                        Color.orange,
                                    iconBackground:
                                        Color.yellow.opacity(0.14),
                                    title:
                                        "Sobre Zentemi",
                                    trailingText: nil
                                )
                            }
                        }
                        .background(.white)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 17
                            )
                        )
                        .shadow(
                            color:
                                Color.black.opacity(0.025),
                            radius: 5,
                            x: 0,
                            y: 2
                        )
                        
                        
                        // MARK: - Yuhmu de Ixtenco
                        
                        HStack(spacing: 15) {
                            
                            ZStack {
                                
                                RoundedRectangle(
                                    cornerRadius: 12
                                )
                                .fill(
                                    Color.yellow.opacity(0.13)
                                )
                                .frame(
                                    width: 58,
                                    height: 58
                                )
                                
                                
                                Image("mascota")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(
                                        width: 48,
                                        height: 48
                                    )
                            }
                            
                            
                            VStack(
                                alignment: .leading,
                                spacing: 5
                            ) {
                                
                                Text(
                                    "Yuhmu de Ixtenco"
                                )
                                .font(
                                    .system(
                                        size: 14,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(
                                    Color.primary
                                )
                                
                                
                                Text(
                                    "Hecho con la comunidad de San Juan Ixtenco, Tlaxcala."
                                )
                                .font(
                                    .system(size: 11)
                                )
                                .foregroundStyle(
                                    Color.secondary
                                )
                                .fixedSize(
                                    horizontal: false,
                                    vertical: true
                                )
                            }
                            
                            
                            Spacer()
                        }
                        .padding(16)
                        .background(.white)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 17
                            )
                        )
                        .shadow(
                            color:
                                Color.black.opacity(0.025),
                            radius: 5,
                            x: 0,
                            y: 2
                        )
                        
                        
                        Spacer()
                            .frame(height: 100)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                }
                
                
                // MARK: - Cerrar sesión
                
                VStack(spacing: 9) {
                    
                    Button {
                        
                        showLogoutConfirmation = true
                        
                    } label: {
                        
                        Text("Cerrar sesión")
                            .font(
                                .system(
                                    size: 13,
                                    weight: .semibold
                                )
                            )
                            .foregroundStyle(.white)
                            .padding(
                                .horizontal,
                                22
                            )
                            .padding(
                                .vertical,
                                11
                            )
                            .background(
                                Color.red.opacity(0.65)
                            )
                            .clipShape(Capsule())
                    }
                    
                    
                    Text("Versión 1.0")
                        .font(
                            .system(size: 10)
                        )
                        .foregroundStyle(
                            Color.secondary.opacity(0.55)
                        )
                }
                .padding(.bottom, 22)
            }
        }
        .navigationBarBackButtonHidden(true)
        
        
        // MARK: - Selector de idioma
        
        .confirmationDialog(
            "Idioma de la app",
            isPresented: $showLanguageSelector,
            titleVisibility: .visible
        ) {
            
            Button("Español") {
                selectedLanguage = "Español"
            }
            
            Button("English") {
                selectedLanguage = "English"
            }
            
            Button("Yuhmu") {
                selectedLanguage = "Yuhmu"
            }
            
            Button(
                "Cancelar",
                role: .cancel
            ) { }
        }
        
        
        // MARK: - Sobre Zentemi
        
        .sheet(
            isPresented: $showAboutZentemi
        ) {
            
            AboutZentemiView()
        }
        
        
        // MARK: - Confirmación cerrar sesión
        
        .alert(
            "¿Cerrar sesión?",
            isPresented:
                $showLogoutConfirmation
        ) {
            
            Button(
                "Cancelar",
                role: .cancel
            ) { }
            
            
            Button(
                "Cerrar sesión",
                role: .destructive
            ) {

                if appState.musicVolume > 0 {

                    MusicManager.shared.playMusic(
                        named: "musicaInicio",
                        volume: Float(appState.musicVolume)
                    )
                }

                goToLogin = true
            }
            
        } message: {
            
            Text(
                "Tendrás que ingresar nuevamente tu nombre de usuario y PIN para entrar a tu cuenta."
            )
        }
        
        
        // MARK: - Regresar al login
        
        .fullScreenCover(
            isPresented: $goToLogin
        ) {
            
            NavigationStack {
                
                LoginView()
                    .environmentObject(
                        appState
                    )
            }
        }
    }
    
    // MARK: - Fila de volumen

    private func settingVolumeRow(
        icon: String,
        iconColor: Color,
        iconBackground: Color,
        title: String,
        volume: Binding<Double>
    ) -> some View {

        VStack(spacing: 5) {

            HStack(spacing: 12) {

                ZStack {

                    RoundedRectangle(
                        cornerRadius: 8
                    )
                    .fill(iconBackground)
                    .frame(
                        width: 30,
                        height: 30
                    )

                    Image(systemName: icon)
                        .font(.system(size: 13))
                        .foregroundStyle(iconColor)
                }

                Text(title)
                    .font(
                        .system(
                            size: 14,
                            weight: .medium
                        )
                    )

                Spacer()

                Text(
                    "\(Int(volume.wrappedValue * 100))%"
                )
                .font(
                    .system(
                        size: 11,
                        weight: .semibold
                    )
                )
                .foregroundStyle(.secondary)
            }

            Slider(
                value: volume,
                in: 0...1
            )
            .tint(purple)
            .padding(.leading, 42)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 9)
    }
    
    
    // MARK: - Fila con interruptor
    
    private func settingToggleRow(
        icon: String,
        iconColor: Color,
        iconBackground: Color,
        title: String,
        isOn: Binding<Bool>
    ) -> some View {
        
        HStack(spacing: 12) {
            
            ZStack {
                
                RoundedRectangle(
                    cornerRadius: 8
                )
                .fill(iconBackground)
                .frame(
                    width: 30,
                    height: 30
                )
                
                
                Image(
                    systemName: icon
                )
                .font(
                    .system(size: 13)
                )
                .foregroundStyle(
                    iconColor
                )
            }
            
            
            Text(title)
                .font(
                    .system(
                        size: 14,
                        weight: .medium
                    )
                )
                .foregroundStyle(
                    Color.primary
                )
            
            
            Spacer()
            
            
            Toggle(
                "",
                isOn: isOn
            )
            .labelsHidden()
            .tint(purple)
            .scaleEffect(0.82)
        }
        .padding(
            .horizontal,
            13
        )
        .frame(height: 52)
    }
    
    
    // MARK: - Fila de navegación
    
    private func settingNavigationRow(
        icon: String,
        iconColor: Color,
        iconBackground: Color,
        title: String,
        trailingText: String?
    ) -> some View {
        
        HStack(spacing: 12) {
            
            ZStack {
                
                RoundedRectangle(
                    cornerRadius: 8
                )
                .fill(iconBackground)
                .frame(
                    width: 30,
                    height: 30
                )
                
                
                Image(
                    systemName: icon
                )
                .font(
                    .system(size: 13)
                )
                .foregroundStyle(
                    iconColor
                )
            }
            
            
            Text(title)
                .font(
                    .system(
                        size: 14,
                        weight: .medium
                    )
                )
                .foregroundStyle(
                    Color.primary
                )
            
            
            Spacer()
            
            
            if let trailingText {
                
                Text(trailingText)
                    .font(
                        .system(size: 11)
                    )
                    .foregroundStyle(
                        Color.secondary
                    )
            }
            
            
            Image(
                systemName: "chevron.right"
            )
            .font(
                .system(
                    size: 11,
                    weight: .semibold
                )
            )
            .foregroundStyle(
                Color.secondary.opacity(0.45)
            )
        }
        .padding(
            .horizontal,
            13
        )
        .frame(height: 52)
    }
}


// MARK: - SOBRE ZENTEMI

struct AboutZentemiView: View {
    
    @Environment(\.dismiss)
    private var dismiss
    
    
    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )
    
    
    var body: some View {
        
        ZStack {
            
            Color(
                red: 0.975,
                green: 0.96,
                blue: 0.995
            )
            .ignoresSafeArea()
            
            
            VStack(spacing: 0) {
                
                // MARK: Encabezado
                
                HStack(spacing: 12) {
                    
                    Button {
                        
                        dismiss()
                        
                    } label: {
                        
                        Image(
                            systemName:
                                "chevron.left"
                        )
                        .font(
                            .system(
                                size: 14,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(purple)
                        .frame(
                            width: 34,
                            height: 34
                        )
                        .background(.white)
                        .clipShape(Circle())
                    }
                    
                    
                    Text("Sobre Zentemi")
                        .font(
                            .system(
                                size: 21,
                                weight: .bold
                            )
                        )
                    
                    
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                
                
                ScrollView(
                    showsIndicators: false
                ) {
                    
                    VStack(spacing: 18) {
                        
                        // Mascota
                        
                        Image("mascota")
                            .resizable()
                            .scaledToFit()
                            .frame(
                                width: 110,
                                height: 110
                            )
                            .padding(.top, 20)
                        
                        
                        Text("Zentemi")
                            .font(
                                .system(
                                    size: 27,
                                    weight: .bold
                                )
                            )
                        
                        
                        Text(
                            "Aprende y descubre la lengua Yuhmu de San Juan Ixtenco."
                        )
                        .font(
                            .system(
                                size: 14,
                                weight: .medium
                            )
                        )
                        .foregroundStyle(purple)
                        .multilineTextAlignment(
                            .center
                        )
                        
                        
                        VStack(
                            alignment: .leading,
                            spacing: 15
                        ) {
                            
                            Text(
                                "¿Qué es Zentemi?"
                            )
                            .font(
                                .system(
                                    size: 17,
                                    weight: .bold
                                )
                            )
                            
                            
                            Text(
                                "Zentemi es una aplicación educativa creada para apoyar el aprendizaje y la preservación de la lengua Yuhmu de San Juan Ixtenco, Tlaxcala."
                            )
                            
                            
                            Text(
                                "A través de lecciones, actividades y juegos, Zentemi busca acercar la lengua a nuevas generaciones de una forma sencilla, visual e interactiva."
                            )
                            
                            
                            Text(
                                "El proyecto reconoce la importancia de la comunidad de San Juan Ixtenco y de las personas que mantienen viva su lengua, conocimientos y cultura."
                            )
                        }
                        .font(
                            .system(size: 14)
                        )
                        .foregroundStyle(
                            Color.primary.opacity(0.78)
                        )
                        .lineSpacing(4)
                        .padding(20)
                        .background(.white)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 18
                            )
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 30)
                }
            }
        }
    }
}


// MARK: - PREVIEW

#Preview {
    
    NavigationStack {
        
        SettingsView()
            .environmentObject(
                AppState()
            )
    }
}
