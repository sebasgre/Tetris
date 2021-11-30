package gui;

import modelo.Tetris;
import servidor.Servidor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PanelMenuMultiplayer extends JPanel {
    private JButton btnJoinGame;
    private JButton btnCreateGame;
    private JButton btnBack;
    private BufferedImage image;
    private FrameJuego menuJuego;

    public PanelMenuMultiplayer(FrameJuego menuJuego) {
        this.menuJuego = menuJuego;
        try {
            image = ImageIO.read(new File("imagenes/tetris.jpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnBack = new JButton("BACK");
        btnJoinGame = new JButton("JOIN THE GAME");
        btnCreateGame = new JButton("CREATE A NEW GAME");
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
        btnBack.setBounds(0, 0, 150, 40);
        btnJoinGame.setBounds(300, 150, 200, 40);
        btnCreateGame.setBounds(300, 250, 200, 40);
        btnBack.setBackground(Color.decode("#181818"));
        btnJoinGame.setBackground(Color.decode("#181818"));
        btnCreateGame.setBackground(Color.decode("#181818"));
        btnJoinGame.setForeground(Color.decode("#FFFFF7"));
        btnCreateGame.setForeground(Color.decode("#FFFFF7"));
        btnBack.setForeground(Color.decode("#FFFFF7"));
        this.add(btnBack);
        this.add(btnJoinGame);
        this.add(btnCreateGame);

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitarPanel();
                menuJuego.setSize(500, 700);
                menuJuego.añadirPanelMenuJuego();
            }
        });

//        PanelMenuMultiplayer panelMenuMultiplayer = this;
        btnJoinGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField ip = new JTextField();
                JTextField name = new JTextField();
                JTextField port = new JTextField();
                Object[] columnas = new Object[]{"nickName:", name, "port:", port};
                int option = JOptionPane.showConfirmDialog(null, columnas, "Ingrese: ip, NickName, port", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    Tetris.nickName = name.getText();
                    Tetris.puerto = Integer.parseInt(port.getText());
                    if (Tetris.multiplayer) {
//                        JOptionPane.showMessageDialog(panelMenuMultiplayer, "Espera a los demas jugadores.");
//                        System.out.println(Servidor.usuarios.size() + " usuarios");
//                        System.out.println(Servidor.conexiones + " conexiones");
//                        boolean validar = true;
//                        while(validar){
//                            if (Servidor.id == Servidor.conexiones) {
//                                System.out.println(Servidor.usuarios.size() + " usuarios");
//                                System.out.println(Servidor.conexiones + " conexiones");
                                Tetris tetris = new Tetris(menuJuego);
                                Thread hilo = new Thread(tetris);
                                hilo.start();
                                Tetris.enviarNickName();
                                Tetris.actualizarLista();
                                quitarPanel();
                                menuJuego.añadirPanelTetris(tetris);
//                                validar = false;
//                            }
//                        }
//                        System.out.println("salir de while");
                    }
                }
            }
        });
        btnCreateGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField jugadores = new JTextField();
                JTextField port = new JTextField();
                Object[] columnas = new Object[]{"limite jugadores:", jugadores, "port:", port};
                int option = JOptionPane.showConfirmDialog(null, columnas, "Ingrese: ip, jugadores y puerto", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    //String ip = JOptionPane.showInputDialog("Ingrese la IP de la nueva\n partida");
                    //Tetris.host = ip.getText();
                    //Tetris.puerto = Integer.parseInt(port.getText());
                    JOptionPane.showMessageDialog(null, "Partida Creada");
                    menuJuego.dispose();
                    Tetris.puerto = Integer.parseInt(port.getText());
                    Servidor.conexiones = Integer.parseInt(jugadores.getText());
                    Servidor servidor = new Servidor(Integer.parseInt(port.getText()), Integer.parseInt(jugadores.getText()));
                    servidor.escuchar();
                }
            }
        });
    }

    private void quitarPanel() {
        this.menuJuego.getContentPane().remove(this);
        this.menuJuego.revalidate();
    }
}
