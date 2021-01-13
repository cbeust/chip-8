import SwiftUI
import shared


struct ContentView: View {
    @ObservedObject var emulatorViewModel = EmulatorViewModel(emulator: Emulator())
    
    var body: some View {
        
        GeometryReader { geometry in
            VStack {
                EmulatorView(screenData: emulatorViewModel.screenData)
                    .frame(width: geometry.size.width)

                HStack {
                    GameButton(emulatorViewModel: emulatorViewModel, number: 4)
                    GameButton(emulatorViewModel: emulatorViewModel, number: 5)
                    GameButton(emulatorViewModel: emulatorViewModel, number: 6)
                }
            }
        }
    }
}

struct GameButton: View {
    var emulatorViewModel: EmulatorViewModel
    let number: Int32
    
    var body: some View {
        Button(action: { emulatorViewModel.keyPressed(key: number) }) {
            Text("\(number)").font(.title)
        }
        .frame(minWidth: 80).background(Color.gray).foregroundColor(.white)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged({ _ in
                    
                })
                .onEnded({ _ in
                    emulatorViewModel.keyReleased()
                })
        )
    }
}

struct EmulatorView: Shape {
    let screenData: KotlinIntArray

    func path(in rect: CGRect) -> Path {
        let width = 64
        let height = 32

        let columnSize = rect.width / CGFloat(width)
        let rowSize = columnSize

        var path = Path()
        for x in 0 ..< width {
            for y in 0 ..< height {
                let index = x + 64 * y
                if (screenData.get(index: Int32(index)) == 1) {
                    let xx = columnSize * CGFloat(x)
                    let yy = rowSize * CGFloat(y)

                    let rect = CGRect(x: xx, y: yy, width: columnSize, height: rowSize)
                    path.addRect(rect)
                }
            }
        }

        return path
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
