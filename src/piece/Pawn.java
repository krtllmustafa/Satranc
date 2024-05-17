package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row); // Piece sınıfının constructor'ını(nesneden çağrılan özellikler) çağırır.

        type = Type.PAWN; // Taşın tipini belirtir.
        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-pawn");
        }else{
            image = getImage("../res/piece/b-pawn");
        }
    }
    // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder
    public boolean canMove(int targetCol, int targetRow){
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
             // Taşın rengine göre hareket değeri belirlenir.
            int moveValue;
            if (color == GamePanel.WHITE) {
                moveValue = -1; // Beyaz piyonlar yukarı doğru ilerler.
            }else{
                moveValue = 1; // Siyah piyonlar aşağı doğru ilerler.
            }
            //Check the hitting piece
            hittingP = getHittingP(targetCol, targetRow);
            // Hedef kareye ulaşmak için taşın hareketlerini kontrol eder.
            // 1 karelik hareket
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
                return true; // Hareket geçerlidir.
            }
            // 2 karelik hareket (ilk hareket)
            if (targetCol == preCol && targetRow == preRow + moveValue*2 && hittingP == null && moved == false && pieceIsOnSttraightLine(targetCol, targetRow) == false ) {
                return true; // Hareket geçerlidir.
            }
             // Çapraz hareket ve yakalama (önünde çaprazda bir taş varsa)
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null && hittingP.color != color) {
                return true; // Hareket geçerlidir.
            }
             // Yoldan geçme (En Passant) 2 kare oynadıktan sonra onun arkasından onu yeme denebilir 
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue){
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
                        hittingP = piece; // Yakalanacak taşı belirler.
                        return true; // Hareket geçerlidir.
                    }
                }
            }
        }
        return false;// Hareket geçersizdir.
    } 
}
