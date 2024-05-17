package piece;

import main.GamePanel;
import main.Type;

public class Knight extends Piece{ //Piece sınıfından türetilmiştir

    public Knight(int color, int col, int row) {
        super(color, col, row); // Piece sınıfının constructor'ını(nesneden çağrılan özellikler) çağırır.
        
        type = Type.KNIGHT; // Taşın tipini belirtir.
        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-knight");
        }else{
            image = getImage("../res/piece/b-knight");
        }
    }
      // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder.
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow)){// Hedef tahta sınırları içinde mi?
            // At, sütun ve satır arasındaki hareket oranı 1:2 veya 2:1 ise hareket edebilir.
            if (Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) ==2 ) { 
                if (isValidSquare(targetCol, targetRow)) { // Hedef kare uygun mu?
                    return true;// Hareket geçerlidir.
                }               
            }
        }
        return false;// Hareket geçersizdir.
    }
    
}
