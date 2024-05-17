package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece{  //Piece sınıfından türetilmiştir

    public King(int color, int col, int row) {
        super(color, col, row); // Piece sınıfının constructor'ını(nesneden çağrılan özellikler) çağırır.
        
        type = Type.KING; // Taşın tipini belirtir.
        // Renk bilgisine göre resim yolunu belirler.
        if (color == GamePanel.WHITE) {
            image = getImage("../res/piece/w-king");
        }else{
            image = getImage("../res/piece/b-king");
        }
    }
    // Hedef sütun ve satıra hareket edip edemeyeceğini kontrol eder.
    public boolean canMove (int targetCol, int targetRow){
        if (isWithinBoard(targetCol, targetRow)) { // Hedef tahta sınırları içinde mi
            //Hareket 
            if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow)==1 || 
            Math.abs(targetCol - preCol) * Math.abs(targetRow-preRow) == 1) {
                if (isValidSquare(targetCol, targetRow)) { // Hedef kare uygun mu?
                    return true; // Hareket geçerlidir
                }
            }
            //Rok yapma 
            if (moved == false) { //Kral henüz hareket etmediyse
                // Sağ rok yapma
                if (targetCol==preCol+2 && targetRow == preRow && pieceIsOnSttraightLine(targetCol, targetRow) == false ) {
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece.col == preCol+3 && piece.row == preRow && piece.moved == false) { // Rok yapacak kale belirlenir.
                            GamePanel.castlingP = piece;
                            return true; // Hareket geçerlidir.
                        }
                        
                    }
                }
                // Sol rok yapma
                if (targetCol==preCol-2 && targetRow == preRow && pieceIsOnSttraightLine(targetCol, targetRow) == false ) {
                    Piece p[] = new Piece[2];
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece.col == preCol -3 && piece.row == targetRow) {
                            p[0]= piece;
                        }
                        if (piece.col == preCol-4 && piece.row == targetRow) {
                            p[1]= piece;
                        }
                        if (p[0] == null && p[1] != null && p[1].moved == false ) { // Rok yapacak kale belirlenir 
                            GamePanel.castlingP = p[1];
                            return true;  // Hareket geçerlidir
                        }
                    }
                }
            }

        }
        return false; // Hareket geçersizdir
    }
    
}
