import Foundation
import shared

class EmulatorViewModel: ObservableObject {
    @Published var screenData = KotlinIntArray(size: 2048)
    
    private let emulator: Emulator
    init(emulator: Emulator) {
        self.emulator = emulator
        
        if let data = loadFile() {
            let intArray : [Int8] = data.map { Int8(bitPattern: $0) }
            let kotlinByteArray: KotlinByteArray = KotlinByteArray.init(size: Int32(data.count))
            for (index, element) in intArray.enumerated() {
                kotlinByteArray.set(index: Int32(index), value: element)
            }
            self.emulator.loadRom(romData: kotlinByteArray)

            self.emulator.observeScreenUpdates(success: { screenData in
                self.screenData = screenData
            })            
        }
    }
    
    func keyPressed(key: Int32) {
        emulator.keyPressed(key: key)
    }

    func keyReleased() {
        emulator.keyReleased()
    }

    func loadFile() -> Data? {
        guard let fileURL = Bundle.main.url(forResource: "Space Invaders [David Winter]", withExtension: "ch8") else {
            print("Failed to create URL for file.")
            return nil
        }
        do {
            let data = try Data(contentsOf: fileURL)
            return data
        }
        catch {
            print("Error opening file: \(error)")
            return nil
        }
    }
    
}
