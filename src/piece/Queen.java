package piece;

import main.GamePanel;
import main.Type;

public class Queen extends Piece{

    public Queen(int color, int col, int row) {
        super(color, col, row); // Piece sınıfının constructor'ını(nesneden çağrılan özellikler) çağırır.
        
        type = Type.QUEEN;// Taşın tipini belirtir.
        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-queen");
        }else{
            image = getImage("../res/piece/b-queen");
        }
    }
    // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder
    public boolean canMove(int targetCol, int targetRow){
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
           // Dikey ve yatay hareketler
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnSttraightLine(targetCol, targetRow) == false) {
                    return true; // Hareket geçerlidir.
                } 
            }  
            // Çapraz hareketler
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false) {
                    return true; // Hareket geçerlidir.
                }
                
            }
        }
        return false;  // Hareket geçersizdir.
    }
}
