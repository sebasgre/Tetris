package gui;

import modelo.Tetris;

import javax.swing.*;

public class Juego extends JFrame {

    public Juego() {
        setSize(500, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
//        Tetris tetris = new Tetris();
//        add(tetris);
//        if (Tetris.multiplayer) {
//            Thread hilo = new Thread(tetris);
//            hilo.start();
//            Tetris.enviarNickName();
//            Tetris.actualizarLista();
//        }
        setVisible(true);
    }
}
