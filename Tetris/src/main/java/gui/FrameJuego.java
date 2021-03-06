package gui;

import modelo.Tetris;

import javax.swing.*;
import java.awt.*;

public class FrameJuego extends JFrame {

    private PanelMenuJuego panelMenuJuego;

    public FrameJuego() {
        panelMenuJuego = new PanelMenuJuego(this);
        this.add(panelMenuJuego);
        this.getContentPane().setLayout(new BorderLayout());
        aĆ±adirPanelMenuJuego();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void aĆ±adirPanelMenuJuego(){
        this.getContentPane().add(panelMenuJuego);
        this.pack();
        this.setLocationRelativeTo(null);
        this.revalidate();
        repaint();
    }

    public void quitarPanelMenuJuego(){
        this.getContentPane().remove(panelMenuJuego);
        this.revalidate();
    }

    public void aĆ±adirPanelTetris(Tetris tetris){
        this.getContentPane().add(tetris);
        this.pack();
        this.setLocationRelativeTo(null);
        this.revalidate();
    }

    public void quitarPanelTetris(Tetris tetris){
        this.getContentPane().remove(tetris);
        this.pack();
        this.setLocationRelativeTo(null);
        this.revalidate();
    }
    public static void main(String[] args) {
        new FrameJuego().setVisible(true);
    }
}
