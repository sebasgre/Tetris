package servidor;

import gui.Juego;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Servidor {

    private int puerto;
    public static int conexiones = 2;
    public static LinkedList<Socket> usuarios = new LinkedList<>();
    public static int id = 0;

    public Servidor(int puerto, int numConexiones) {
        conexiones = numConexiones;
        this.puerto = puerto;
    }

    public Servidor() {
        conexiones = 4;
        puerto = 1234;
    }

    public void escuchar() {
        try {
            ServerSocket serverSocket = new ServerSocket(puerto, conexiones);
            System.out.println("Esperando jugadores...");
            while (id < conexiones) {
                Socket jugador = serverSocket.accept();
                usuarios.add(jugador);
                Runnable run = new HiloServidor(jugador, usuarios, id);
                Thread hilo = new Thread(run);
                hilo.start();
                id = usuarios.size();
                System.out.println(id + " id");
                Servidor.id = usuarios.size();
            }
            JOptionPane.showMessageDialog(null, "Sala completa.");
            System.out.println("Sala de Juego Llena.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getUsuarios (){
        return usuarios.size();
    }
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.escuchar();
    }
}
