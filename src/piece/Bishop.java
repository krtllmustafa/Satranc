package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece{ //  Piece sınıfından türetilmiş 

    public Bishop(int color, int col, int row) {
        super(color, col, row); // Piece sınıfının constructor'ını (nesneden çağrılan özellikler)  çağırır.
        
        type = Type.BISHOP; // Taşın tipini belirtir.

        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-bishop");
        }else{
            image = getImage("../res/piece/b-bishop");
        }
    }
     // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder.
    public boolean canMove (int targetCol, int targetRow){
         // Hedef tahta sınırları içinde mi ve mevcut konumla farklı bir kare mi?
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // Hedef sütun ve satır arasındaki mesafe eşit mi?
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
                // Hedef kare uygun mu ve üzerinde başka bir taş yok mu?
                if (isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false) {
                    return true; // Hareket geçerlidir.
                }
            }
        }
        return false; // Hareket geçersizdir.
    }
    
}
