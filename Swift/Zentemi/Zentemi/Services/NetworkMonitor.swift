import Foundation
import Network
import Combine

/// Monitor de conectividad de red en tiempo real (equivalente a NetworkMonitor en Android).
/// Se usa para sugerir crear cuenta solo con internet y reintentar la vinculación al volver la red.
public final class NetworkMonitor: ObservableObject {

    public static let shared = NetworkMonitor()

    @Published public private(set) var isOnline: Bool = true

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.zentemi.network.monitor")

    private init() {
        monitor.pathUpdateHandler = { [weak self] path in
            let online = (path.status == .satisfied)
            DispatchQueue.main.async {
                if self?.isOnline != online {
                    self?.isOnline = online
                }
            }
        }
        monitor.start(queue: queue)
    }

    deinit {
        monitor.cancel()
    }
}
