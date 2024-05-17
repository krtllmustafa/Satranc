package main;

import java.awt.AlphaComposite;
import java.awt.Color;
//import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.RenderingHints;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {

    private long whiteTurnTimeRemaining = 10 * 60 * 1000; // Beyaz oyuncunun kalan süresi (milisaniye cinsinden)
    private long blackTurnTimeRemaining = 10 * 60 * 1000; // Siyah oyuncunun kalan süresi (milisaniye cinsinden)
    private Timer turnTimer; // Oyun sırasını takip eden zamanlayıcı
    final int FPS = 60; // Oyunun kare hızı (frames per second)
    Thread gameThread; // Oyun döngüsünü çalıştıran thread
    Board board = new Board(); // Oyun tahtası nesnesi
    Mouse mouse = new Mouse(); // Fare olayları

    // Oyun Arraylistleri filan
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    ArrayList<Piece> capturedPieces = new ArrayList<>();
    private ArrayList<String> moveHistory = new ArrayList<>();
    private ArrayList<String> startPositions = new ArrayList<>();

    Piece activeP, checkingP;
    public static Piece castlingP;

    // Notasyon alanı ve kaydırıcı
    private JTextArea notationTextArea;
    private JScrollPane notationScrollPane;

    // Renkler
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    private int gameNumber = 1; // Oyun numarası
    private final String historyDirectory = "C:\\Users\\Mustafa\\Desktop\\Projects\\Satranc\\src\\historySave\\"; // Oyun
                                                                                                                             // hamlelerini
                                                                                                                             // kaydetiğimiz
                                                                                                                             // dosya

    // Boolean değerler
    boolean canMove; // Seçilen parçanın hareket edebilir olup olmadığını belirten flag
    boolean validSquare; // Hedef karenin geçerli olup olmadığını belirten flag
    boolean promotion; // Piyonun terfi etme durumunu belirten flag
    boolean gameover; // Oyunun bitip bitmediğini belirten flag
    boolean gameovertime; // Oyunun süresinin bitip bitmediğini belirten flag
    boolean stalemate; // Oyunun berabere olup olmadığını belirten flag

    public GamePanel() {
        setLayout(null);
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        determineGameNumber();
        startTurnTimer(); // Oyun sırasını başlatan zamanlayıcıyı başlat

        setPieces(); // Oyun parçalarını yerleştir
        // testPromotion();
        // testIllegal();
        copyPieces(pieces, simPieces);

        notationTextArea = new JTextArea();
        notationTextArea.setEditable(false);
        notationScrollPane = new JScrollPane(notationTextArea);
        notationTextArea.setForeground(Color.WHITE);
        notationScrollPane.setBounds(1000, 250, 200, 300);
        notationTextArea.setBackground(Color.GRAY);
        add(notationScrollPane); // JScrollPane'i GamePanel'e ekleyin
    }

    // Oyunu başlatan metot
    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Oyun taşlarını yerleştiren metot
    public void setPieces() {

        // Beyaz takımın taşlarını belirledidğin kolon ve satıra ekler
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        // Siyah takımın taşları
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }

    public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();

        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }

    @Override
    public void run() {
        // GAME LOOP - Oyun Döngüsü
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (promotion) {
            promoting();
        } else if (gameover == false && stalemate == false) {
            //// Fare buttonuna basıldığında ////
            if (mouse.pressed) {
                if (activeP == null) {
                    // Aktif parça null ise, bir parçayı seçip seçemediğinizi kontrol edin
                    for (Piece piece : simPieces) {
                        // Fare dost parçada ise, onu aktifP olarak seçin
                        if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
                                && piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;
                            addStartPosition(piece);
                        }
                    }
                } else {
                    // Oyuncu bir parçayı tutuyorsa, hareketi simüle edin
                    simulate();
                }
            }

            //// FARE BUTONU BIRAKILDI ////
            if (mouse.pressed == false) {
                if (activeP != null) {
                    if (validSquare) {
                        // HAREKET ONAYLANDI

                        // Yakalanma durumunda taş listesini güncelle ve yakalanantaşlar listesine
                        // ekleyin
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();

                        // Bir taş yakalandıysa ve capturedPieces listesine eklendi
                        if (activeP.hittingP != null) {
                            capturedPieces.add(activeP.hittingP);
                        }
                        addToMoveHistory(activeP);

                        // ... Diğer işlemler devam eder
                    } else {
                        // Geçersiz hareket, taşo sıfırlayın

                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

        if (promotion) {
            promoting();
        } else if (gameover == false && stalemate == false) {
            //// FARE BUTONU BASILDI ////
            if (mouse.pressed) {
                if (activeP == null) {
                    // Aktif parça null ise, bir taşı seçip seçemediğinizi kontrol edin
                    for (Piece piece : simPieces) {
                        // Fare dost parçada ise, onu aktifP olarak seçin
                        if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
                                && piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;
                        }
                    }
                } else {
                    // Oyuncu bir taşı tutuyorsa, hareketi simüle edin
                    simulate();
                }
            }

            //// FARE BUTONU BIRAKILDI ////
            if (mouse.pressed == false) {
                if (activeP != null) {
                    if (validSquare) {
                        // HAREKET ONAYLANDI

                        // Yakalanma durumunda taş listesini güncelle ve yakalanantaş listesine ekleyin
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();

                        if (castlingP != null) {
                            castlingP.updatePosition();
                        }

                        if (isKingInCheck() && isCheckMate()) {
                            // Şah mat ise oyun sonu
                            gameover = true;
                        } else if (isStalemate() && isKingInCheck() == false) {
                            stalemate = true;
                        } else { // Oyun devam ediyor
                            if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }

                        // if (canPromote()) {
                        // promotion = true;
                        // } else {
                        // changePlayer();
                        // }

                    } else {
                        // Geçersiz hareket, parçayı sıfırlayın
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

    }

    // Oynanan hamlelerimizi tutan textarea'ya yazdıran metot
    private void addToMoveHistory(Piece piece) {
        // Hamle geçmişine notasyonu ekle
        String notation = getNotation(startPositions.get(moveHistory.size()), piece, piece.col, piece.row,
                piece.hittingP != null);
        moveHistory.add(notation);

        // JTextArea'ya notasyonu ekle
        notationTextArea.append(notation + "\n");
        // JTextArea'nın en son eklenen notasyonu göstermesi için JScrollPane'i aşağı
        // kaydır
        notationScrollPane.getVerticalScrollBar().setValue(notationScrollPane.getVerticalScrollBar().getMaximum());

        // Notasyonu dosyaya kaydet
        saveToHistory(notation);
    }

    // Oynanan hamlelerimizi tutan metot
    private String getNotation(String startPosition, Piece piece, int endCol, int endRow, boolean capture) {
        char startColChar = startPosition.charAt(0);
        int startRowInt = Character.getNumericValue(startPosition.charAt(1));
        char endColChar = (char) ('a' + endCol);
        int endRowInt = 8 - endRow;
        String notation = "";

        if (piece.color == WHITE) {
            notation += "Beyaz: ";
        } else {
            notation += "Siyah: ";
        }

        switch (piece.type) {
            case ROOK:
                notation += "Kale ";
                break;
            case KNIGHT:
                notation += "At ";
                break;
            case BISHOP:
                notation += "Fil ";
                break;
            case QUEEN:
                notation += "Vezir ";
                break;
            case KING:
                notation += "Şah ";
                break;
            case PAWN:
                notation += "Piyon ";
                break;
            default:
                break;
        }

        notation += startColChar + Integer.toString(startRowInt) + " - " + endColChar + Integer.toString(endRowInt);

        if (capture) {
            notation += "x";
        }
        return notation;
    }

    // Bir taşın başlangıç pozisyonunu startPositions listesine ekler.
    private void addStartPosition(Piece piece) {
        char startColChar = (char) ('a' + piece.preCol);
        int startRowInt = 8 - piece.preRow;
        startPositions.add("" + startColChar + startRowInt);
    }

    /**
     * Simülasyon yapar.
     * Bu işlev, fare hareketiyle oyun tahtasında bir parçanın sürüklendiği zaman
     * çağrılır.
     * Parçanın hareket edebilirlik durumunu kontrol eder, hareket edilebilecek
     * karelerin durumunu belirler
     * ve eğer hareket geçerliyse, tahtadaki parça konumlarını günceller.
     */
    private void simulate() {
        canMove = false;
        validSquare = false;

        // Her döngüde tüm parçaların durumunu sıfırlar
        // Simülasyon aşamasında pozisyonları geri yükler
        copyPieces(pieces, simPieces);

        // Şahın rok durumunu sıfırlar
        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        // Eğer bir parça taşınıyorsa, pozisyonunu günceller
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        // Eğer parça hareket edebilir durumdaysa
        if (activeP.canMove(activeP.col, activeP.row)) {
            canMove = true;

            // Eğer bir parça vuruluyorsa, o parçayı listeden kaldırır
            if (activeP.hittingP != null) {
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            if (isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
                validSquare = true;
            }
        }
    }

    // Belirli bir şahın, rakip tarafın saldırısı altında olup olmadığını kontrol
    // eder.
    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Rakip tarafın şahını yakalayıp yakalayamayacağını kontrol eder.
    private boolean opponentCanCaptureKing() {
        Piece king = getKing(false);

        for (Piece piece : simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }

        return false;
    }

    // OYundaki Şah olayını kontrol eder
    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if (activeP.canMove(king.col, king.row)) {
            checkingP = activeP;
            return true;
        } else {
            checkingP = null;
        }

        return false;
    }

    // Belirli bir oyuncunun (mevcut oyuncunun ya da rakibin) şahını bulan metot
    private Piece getKing(boolean opponent) {
        Piece king = null;

        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }

        return king;
    }

    // Şahın mat durumunu kontrol eder
    private boolean isCheckMate() {

        Piece king = getKing(true);

        if (kingCanMove(king)) {
            return false;
        } else {
            // Oyuncunun hâlâ şansı var
            // Hamleyi taşlarıyla engelleyip engelleyemeyeceğini kontrol et
            // Şahın tehdit eden taşın ve tehdit altındaki şahın pozisyonunu kontrol etk
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0) {
                // Tehdit eden taş dikey olarak saldırıyor
                if (checkingP.row < king.row) {
                    // Tehdit eden taş, şahın üstünde
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // Tehdit eden taş, şahın altında
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

            } else if (rowDiff == 0) {
                // Tehdit eden taş yatay olarak saldırıyor
                if (checkingP.col < king.col) {
                    // Tehdit eden taş solda
                    for (int col = checkingP.col; col < king.row; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.col > king.col) {
                    // Tehdit eden taş sağda
                    for (int col = checkingP.col; col > king.row; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                // Tehdit eden taş çapraz saldırıyor.
                if (checkingP.row < king.row) {
                    // Tehdit eden taş şahın üstünde
                    if (checkingP.col < king.col) {
                        // Tehdit eden taş sol üstte
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // Tehdit eden taş sağ üstte
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // Tehdit eden taş şahın altında
                    if (checkingP.col < king.col) {
                        // Tehdit eden taş sol altta
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // Tehdit eden taş sağ altta
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                // Atın şahı tehdit editiği yer
            }
        }

        return true;
    }

    // Şahın herhangi bir yönde (dikey, yatay veya çapraz) bir kare ilerleyip
    // ilerleyemeyeceğini kontrol eder.
    private boolean kingCanMove(Piece king) {

        // Kralın hareket edebileceği bir kare var mı diye simüle et
        if (isValidMove(king, -1, -1)) {
            return true;
        }
        if (isValidMove(king, 0, -1)) {
            return true;
        }
        if (isValidMove(king, 1, -1)) {
            return true;
        }
        if (isValidMove(king, -1, 0)) {
            return true;
        }
        if (isValidMove(king, 1, 0)) {
            return true;
        }
        if (isValidMove(king, -1, 1)) {
            return true;
        }
        if (isValidMove(king, 0, 1)) {
            return true;
        }
        if (isValidMove(king, 1, 1)) {
            return true;
        }

        return false;
    }

    // Şahın belirli bir yöne yapabileceği bir hamlenin geçerli olup olmadığını
    // kontrol eder
    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;

        // Geçici Şah konumunu güncelle
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }

            if (isIllegal(king) == false) {
                isValidMove = true;
            }
        }

        // Geçici Şah konumunu sıfırla
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    // Pat durumunu kontrol eder
    private boolean isStalemate() {
        int count = 0;

        // Taş sayısını say
        for (Piece piece : simPieces) {
            if (piece.color != currentColor) {
                count++;
            }
        }

        // Sadece bir taş kaldıysa (kral)
        if (count == 1) {
            if (kingCanMove(getKing(true)) == false) {
                return true;
            }
        }

        return false;
    }

    // Rok yapmayı kontrol eder
    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }

            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    // Oyun sırasını değiştiren metot
    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;

            // Siyah iki adım durumunu sıfırla
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;

            // Beyaz iki adım durumunu sıfırla
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }

        activeP = null;
    }

    // Taşların terfi durumunu kontrol eder
    private boolean canPromote() {
        if (activeP.type == Type.PAWN) {
            if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));

                return true;
            }
        }

        return false;
    }

    // Taşların terfi durumunu işleyen metot
    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promoPieces) {
                if (piece.col == mouse.x / Board.SQUARE_SIZE - 4 && piece.row == mouse.y / Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK:
                            simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
                            break;
                        case KNIGHT:
                            simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
                            break;
                        case BISHOP:
                            simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
                            break;
                        case QUEEN:
                            simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
                            break;
                        default:
                            break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    // Oyun sırasını takip eden zamanlayıcıyı başlatan metot
    private void startTurnTimer() {
        turnTimer = new Timer();
        turnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentColor == WHITE) {
                    whiteTurnTimeRemaining -= 1000; // 1 saniye azalt
                    if (whiteTurnTimeRemaining <= 0) {
                        turnTimer.cancel();
                        gameovertime = true;
                    }
                } else {
                    blackTurnTimeRemaining -= 1000; // 1 saniye azalt
                    if (blackTurnTimeRemaining <= 0) {
                        turnTimer.cancel();
                        gameovertime = true;
                    }
                }
                repaint();
            }
        }, 1000, 1000);// 1 saniye gecikme, her 1 saniyede bir çalıştır
    }

    // Oyun süresini gösteren metot
    private String formatTime(long timeInMillis) {
        // Zamanı dakika:saniye formatına dönüştür
        long minutes = timeInMillis / (60 * 1000);
        long seconds = (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Mevcut oyun numarasını kontrol eden metot
    private void determineGameNumber() {
        // Mevcut oyun numarasını kontrol et
        File directory = new File("C:\\\\Users\\\\Mustafa\\\\Desktop\\\\Projects\\\\Satranc\\\\src\\\\historySave\\\\");
        File[] files = directory.listFiles((dir, name) -> name.startsWith("history") && name.endsWith(".txt"));
        if (files != null) {
            gameNumber = files.length + 1;
        }
    }

    // Hamleleri dosyaya kaydeden metot
    public void saveToHistory(String content) {
        String fileName = historyDirectory + "history" + gameNumber + ".txt"; // Oyun numarasına göre dosya adı oluştur
        try {
            // Dosya yolu belirleyerek FileWriter oluştur
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(content);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Oyunda taşları, süreyi, tahtayi v.s ekrana çizen metot
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Tahta
        board.draw(g2);

        // Taşlar
        for (Piece p : simPieces) {
            p.draw(g2);
        }

        if (activeP != null) {
            // Eğer hareket edilebilir bir parça seçiliyse ve bu hamle yasal ise renkli
            // kareyi göster
            if (canMove) {
                if (isIllegal(activeP) || opponentCanCaptureKing()) {
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                } else {
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            // Seçilen parçayı çiz
            activeP.draw(g2);
        }

        // STATUS MESSAGE
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
        g2.setColor(Color.white);

        // Beyaz ve siyahın yediği taşlarını ve kalan sürelerini göster
        int whiteCapturedXOffset = 0;
        for (Piece capturedPiece : capturedPieces) {
            if (capturedPiece.color == WHITE) {
                g2.drawImage(capturedPiece.image, 900 + whiteCapturedXOffset, 140, Board.SQUARE_SIZE / 3,
                        Board.SQUARE_SIZE / 3, null);
                whiteCapturedXOffset += 35;
            }
        }
        g2.drawString("Kalan Süresi: " + formatTime(whiteTurnTimeRemaining), 900, 700);

        int blackCapturedXOffset = 0;
        for (Piece capturedPiece : capturedPieces) {
            if (capturedPiece.color == BLACK) {
                g2.drawImage(capturedPiece.image, 900 + blackCapturedXOffset, 720, Board.SQUARE_SIZE / 3,
                        Board.SQUARE_SIZE / 3, null);
                blackCapturedXOffset += 35;
            }
        }
        g2.drawString("Kalan Süresi: " + formatTime(blackTurnTimeRemaining), 900, 125);

        // Promotion durumunda mesajı yazdır
        if (promotion) {
            g2.drawString("Terfi Etir ", 1300, 170);
            for (Piece piece : promoPieces) {
                g2.drawImage(piece.image, 1325, piece.getY(piece.row), Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, null);
            }
        } else {
            if (currentColor == WHITE) {
                g2.drawString("Beyaz oynuyor", 900, 650);

                if (checkingP != null && checkingP.color == BLACK) {
                    g2.setColor(Color.red);
                    g2.drawString("Şah Çekiliyor", 900, 800);
                }
            } else {
                g2.drawString("Siyah oynuyor", 900, 75);

                if (checkingP != null && checkingP.color == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("Şah Çekiliyor", 900, 210);
                }
            }
        }

        // Oyun durumu mesajlarını yazdır
        if (gameover) {
            String s;
            if (currentColor == WHITE) {
                s = "Beyaz Kazandı!";
                turnTimer.cancel();
            } else {
                s = "Siyah Kazandı!";
                turnTimer.cancel();
            }
            g2.setColor(new Color(0, 0, 0, 128));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);
        }
        if (gameovertime) {
            String s = "";
            if (currentColor == WHITE) {
                if (whiteTurnTimeRemaining <= 0) {
                    s = "Siyah Kazandı!";
                }
            } else {
                if (blackTurnTimeRemaining <= 0) {
                    s = "Beyaz Kazandı!";
                }
            }
            g2.setColor(new Color(0, 0, 0, 128));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);
        }

        if (stalemate) {
            g2.setColor(new Color(0, 0, 0, 128));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.lightGray);
            g2.drawString("Pat", 200, 420);
        }

        notationScrollPane.repaint();

        g2.dispose();
    }

}