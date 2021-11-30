package gui;

import modelo.Tetris;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PanelMenuJuego extends JPanel {
    private JButton btnSinglePlayer;
    private JButton btnMultiPlayer;
    private JButton btnExit;
    private BufferedImage image;
    private FrameJuego menuJuego;

    public PanelMenuJuego(FrameJuego menuJuego) {
        this.menuJuego = menuJuego;
        try {
            image = ImageIO.read(new File("imagenes/OIP.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnSinglePlayer = new JButton("SINGLE PLAYER");
        btnMultiPlayer = new JButton("MULTIPLAYER");
        btnExit = new JButton("EXIT");
        this.setLayout(null);
        loadButtons();
        this.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(750, 500);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }

    private void loadButtons() {
        btnSinglePlayer.setBounds(300, 260, 150, 40);
        btnMultiPlayer.setBounds(300, 310, 150, 40);
        btnExit.setBounds(300, 360, 150, 40);
        btnSinglePlayer.setBackground(Color.decode("#181818"));
        btnMultiPlayer.setBackground(Color.decode("#181818"));
        btnExit.setBackground(Color.decode("#181818"));
        btnSinglePlayer.setForeground(Color.decode("#FFFFF7"));
        btnMultiPlayer.setForeground(Color.decode("#FFFFF7"));
        btnExit.setForeground(Color.decode("#FFFFF7"));
        this.add(btnSinglePlayer);
        this.add(btnMultiPlayer);
        this.add(btnExit);
        btnSinglePlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Aqui la logica para jugar solo
                Tetris.multiplayer = false;
                JTextField name = new JTextField();
                Object[] columnas = new Object[]{"nickName:", name};
                int option = JOptionPane.showConfirmDialog(null, columnas, "Ingrese su NickName", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    Tetris.nickName = name.getText();
                    Tetris tetris = new Tetris(menuJuego);
                    quitarPanel();
                    menuJuego.añadirPanelTetris(tetris);
                }
            }
        });

        btnMultiPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Tetris.multiplayer = true;
                menuJuego.quitarPanelMenuJuego();
                PanelMenuMultiplayer panelMenuMultiplayer = new PanelMenuMultiplayer(menuJuego);
                añadirPanelMultiplayer(panelMenuMultiplayer);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opcion = JOptionPane.showConfirmDialog(null, "Realmente deseas salir?", "Aviso", JOptionPane.YES_NO_OPTION);
                if (opcion == 0) {
                    System.exit(0);
                }
            }
        });
    }

    private void añadirPanelMultiplayer(PanelMenuMultiplayer panelMenuMultiplayer) {
        menuJuego.getContentPane().add(panelMenuMultiplayer);
        menuJuego.revalidate();
    }


    private void quitarPanel() {
        this.menuJuego.getContentPane().remove(this);
        this.menuJuego.revalidate();
    }
}