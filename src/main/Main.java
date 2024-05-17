package main;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Main {
    static ImageIcon logo = new ImageIcon(Main.class.getClassLoader().getResource("res/chess2.jpg"));
    public static void main(String[] args) {  
        JFrame window = new JFrame("Satranç"); // Pencere Oluşturuyoruz
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pencereyi kapatığımızda uygulamayı kapatması için.
        window.setResizable(false);// Boyutlandırılmasını
        window.setIconImage(logo.getImage()); 

        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();

        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setVisible(true);// pencerenin görünürlüğü

        gp.launchGame();
    }
}
