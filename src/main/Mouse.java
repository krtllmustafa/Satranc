package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
    public int x, y; // Fare Konumlarını saklamak için 
    public boolean pressed;

    // Fareye Basıldığında 
    @Override
    public void mousePressed(MouseEvent e) { 
        pressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;
    }
    // Fare sürüklendiğinde 
    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
    // Fare Hareketinde 
    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
}
