package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece{

    public Rook(int color, int col, int row) {
        super(color, col, row);// Piece sınıfının constructor'ını(nesneden çağrılan özellikler) çağırır.
        
        type = Type.ROOK;// Taşın tipini belirtir.
        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-rook");
        }else{
            image = getImage("../res/piece/b-rook");
        }
    }
     // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
           // Kale, hedef sütun veya satırı aynı olduğu sürece hareket edebilir
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnSttraightLine(targetCol, targetRow) == false) {
                    return true; // Hareket geçerlidir
                }               
            }
        }
        return false;  // Hareket geçersizdir
    }
}