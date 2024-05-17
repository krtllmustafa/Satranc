package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;
import main.Type;

public class Piece {
    public Type type; // Taşın tipi
    public BufferedImage image; // Taşın resmi
    public int x,y; // Taşın koordinatları
    public int col, row, preCol, preRow; // Taşın sütun ve satır bilgileri
    public int color; // Taşın rengi
    public Piece hittingP; // Hareket sırasında karşılaşılan diğer taş
    public boolean moved, twoStepped; // Taşın hareket ettiği ve iki kare ileri gittiği bilgileri

    // Constructor: Renk, sütun ve satır bilgilerini alarak bir taş oluşturur.
    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage (String imagePath){
        // Resmi yolu alarak bir BufferedImage nesnesi döndürür.
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    // Sütun bilgisinden X koordinatını hesaplar
    public int getX(int col){
        return col * Board.SQUARE_SIZE;
    }
    // Satır bilgisinden Y koordinatını hesaplar
    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }
     // X koordinatından sütun bilgisini hesaplar.
    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    // Y koordinatından satır bilgisini hesaplar
    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    // Taşın GamePanel içindeki sırasını döndürür
    public int getIndex(){
        for (int index = 0; index < GamePanel.simPieces.size(); index++) {
            if (GamePanel.simPieces.get(index) == this) {
                return index;
            }
        }
        return 0;
    }
    // Taşın pozisyonunu günceller 
    public void updatePosition(){

        // En Passant kontrolü için
        if (type == Type.PAWN) {
            if (Math.abs(row - preRow) == 2 ) {
                twoStepped = true;
            }
        }

        x = getX(col); // X koordinatını günceller
        y = getY(row); // Y koordinatını günceller
        preCol = getCol(x); // Önceki sütun bilgisini günceller
        preRow = getRow(y); // Önceki satır bilgisini günceller
        moved = true; // Taş hareket ettiği için moved değerini true yapar
    }
    // Taşın pozisyonunu resetler 
    public void resetPosition(){
        col = preCol; // Sütun bilgisini resetler
        row = preRow; // Satır bilgisini resetler
        x = getX(col); // X koordinatını günceller
        y = getY(row); // Y koordinatını günceller
    }
     // Taşın belirli bir hedefe hareket edip edemeyeceğini kontrol eder.
    public boolean canMove (int targetCol, int targetRow){
        return false;
    }
    // Belirtilen hedef sütun ve satırın tahta sınırları içinde olup olmadığını kontrol eder.
    public boolean isWithinBoard(int targetCol, int targetRow){
        if (targetCol >= 0 && targetCol <=7 && targetRow >= 0 && targetRow <= 7) {
            return true;            
        }
        return false;
    }
    // Belirtilen hedef sütun ve satırın mevcut sütun ve satır ile aynı olup olmadığını kontrol eder.
    public boolean isSameSquare (int targetCol, int targetRow){
        if (targetCol == preCol && targetRow == preRow) {
            return true;
        }
        return false;
    }
    // Belirtilen hedef sütun ve satırda bir taş varsa onu döndürür.
    public Piece getHittingP(int targetCol, int targetRow){
        for (Piece piece : GamePanel.simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece != this) {
                return piece;
            }
        }
        return null;
    }
    // Belirtilen hedef karenin geçerli olup olmadığını kontrol eder.   
    public boolean isValidSquare(int targetCol, int targetRow){
        hittingP = getHittingP(targetCol, targetRow); // Hedef karedeki taşı alır 
        if (hittingP == null) { // Kare boşsa geçerlidir
            return true;
        }else{ // Kare doluysa
            if (hittingP.color != this.color) { // Farklı renkte bir taşsa yakalanabilir
                return true;
            }else{
                hittingP = null;
            }
        }
        return false;
    }
    // Belirtilen hedef sütun ve satırda bir taşın, taşın hareket ettiği doğrultuda olup olmadığını kontrol eder.
    public boolean pieceIsOnSttraightLine(int targetCol, int targetRow){
        // Taşın sola doğru hareket ettiği durumda
        for (int c = preCol-1; c > targetCol; c--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingP = piece;
                    return true;
                }                
            }
        }
        // Taşın sağa doğru hareket ettiği durumda
        for (int c = preCol+1; c < targetCol; c++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingP = piece;
                    return true;
                }                
            }
        }
        // Taşın yukarı doğru hareket ettiği durumda
        for (int r = preRow-1; r > targetRow; r--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingP = piece;
                    return true;
                }                
            }
        }
        // Taşın aşağı doğru hareket ettiği durumda
        for (int r = preRow+1; r < targetRow; r++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingP = piece;
                    return true;
                }                
            }
        }
        return false;
    }
    // Belirtilen hedef sütun ve satırda bir taşın, taşın hareket ettiği çapraz doğrultuda olup olmadığını kontrol eder
    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow){
        
        if (targetRow < preRow) {    
             // Yukarı sola doğru
            for (int c = preCol-1; c > targetCol ; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece: GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
            // Yukarı sağa doğru
            for (int c = preCol+1; c < targetCol ; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece: GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }
        
        if (targetRow > preRow) {
            // Aşağı sola doğru
            for (int c = preCol-1; c > targetCol ; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece: GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
           // Aşağı sağa doğru
            for (int c = preCol+1; c < targetCol ; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece: GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // Taşın resmini oyun paneline çizer.
    public void draw(Graphics2D g2){
        g2.drawImage(image, x, y, Board.SQUARE_SIZE,  Board.SQUARE_SIZE, null);
    }

    
}

