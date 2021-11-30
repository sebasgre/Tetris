package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class HiloServidor implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final LinkedList<Socket> usuarios;

    private final int id;

    public HiloServidor(Socket socket, LinkedList<Socket> usuarios, int id) {
        this.socket = socket;
        this.usuarios = usuarios;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            String mensaje = String.valueOf(id);
            out.writeUTF(mensaje);

            while (true) {
                String recibido = in.readUTF();
                for (Socket jugador : usuarios) {
                    out = new DataOutputStream(jugador.getOutputStream());
                    // le envio el mensaje a todos los jugadores
                    out.writeUTF(recibido);
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i) == socket) {
                    usuarios.remove(i);
                    break;
                }
            }
        }
    }
}
