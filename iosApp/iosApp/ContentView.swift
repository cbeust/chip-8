import SwiftUI
import shared


struct ContentView: View {
    @ObservedObject var emulatorViewModel = EmulatorViewModel(emulator: Emulator())
    
    var body: some View {
        
        NavigationView {
            GeometryReader { geometry in
                VStack {
                    EmulatorView(screenData: emulatorViewModel.screenData)
                        .frame(width: geometry.size.width)

                    HStack {
                        GameButton(emulatorViewModel: emulatorViewModel, number: 4, icon: "arrow.left")
                        GameButton(emulatorViewModel: emulatorViewModel, number: 5, icon: "arrow.up")
                        GameButton(emulatorViewModel: emulatorViewModel, number: 6, icon: "arrow.right")
                    }
                }
            }
            .padding()
            .navigationBarTitle(Text("Chip-8 Emulator"))
        }
    }
}

struct GameButton: View {
    var emulatorViewModel: EmulatorViewModel
    let number: Int32
    let icon: String
    
    var body: some View {
        Button(action: {  }) {
            Image(systemName: icon)
        }
        .frame(minWidth: 80, minHeight: 50).background(Color.gray).foregroundColor(.white)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged({ _ in
                    emulatorViewModel.keyPressed(key: number)
                })
                .onEnded({ _ in
                    emulatorViewModel.keyReleased()
                })
        )
    }
}


struct EmulatorView: View {
    let screenData: KotlinIntArray
    
    var body: some View {
        let displayWidth = 64
        let displayHeight = 32


        Canvas { context, size in
            let blockSize = size.width / CGFloat(displayWidth)

            for x in 0 ..< displayWidth {
                for y in 0 ..< displayHeight {
                    let index = x + displayWidth * y
                    if (screenData.get(index: Int32(index)) == 1) {
                        let xx = blockSize * CGFloat(x)
                        let yy = blockSize * CGFloat(y)

                        let rect = CGRect(x: xx, y: yy, width: blockSize, height: blockSize)
                        context.fill(Path(rect), with: .color(.black))
                    }
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
