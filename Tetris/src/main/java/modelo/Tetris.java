package modelo;

import gui.FrameJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tetris extends Panel implements KeyListener, Runnable {

    private Socket jugador;
    private DataInputStream in;
    private static DataOutputStream out;
    private static HashMap<String, String> jugadores;

    public static boolean multiplayer = true;
    public static String host = "localhost";
    public static String nickName = "";
    public static int puerto;

    private static int id = 1;
    private int filas = 22;
    private final int columnas = 10;
    private int[][] tablero = new int[filas][columnas];
    private int[][] paredes = new int[24][12];

    private BufferedImage bi;
    private Graphics graficos;
    private Dimension dim;

    private final int[] RETRASO_GLOBAL = {800, 720, 640, 560, 480, 380, 280, 180, 80, 10};

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(530, 710);
    }

    private FrameJuego menuJuego;
    public Tetris(FrameJuego menuJuego) {
        this.menuJuego = menuJuego;
        cargarParedes();
        addKeyListener(this);

        if (multiplayer) {
            jugadores = new HashMap<>();
            timer.scheduleAtFixedRate(mover, 1000, 1);
            try {
                jugador = new Socket(host, puerto);
                in = new DataInputStream(jugador.getInputStream());
                out = new DataOutputStream(jugador.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(Tetris.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!multiplayer) {
            timer.scheduleAtFixedRate(mover, 1000, 1);
        }
    }

    // coloresPiezas representando los diferentes tipos de bloques
    private final Color[] coloresPiezas = {Color.decode("#181818"), Color.YELLOW, Color.CYAN, Color.BLUE, Color.ORANGE, Color.GREEN, Color.RED, Color.MAGENTA};
    private final Color fondoPanel = Color.BLACK;
    private final Color piezaOscura = Color.DARK_GRAY;

    // maneja la colaPiezas de piezas
    private final Queue<Integer> colaPiezas = new ArrayDeque<>();
    // genera las piezas
    private final Pieza pieza = new Pieza();
    // representa la pieza que esta actualmente activa
    private Pieza.Activo piezaActiva = null;

    // variables para gestionar el mecanismo de espera
    private int esperaId = 0;
    private boolean estaEnEspera = false;

    // variable de tiempo y niveles
    private int tiempo = 0;
    private int retraso = RETRASO_GLOBAL[0];
    private int nivel = 0;
    private int tiempoBloqueo = 0;
    private int lineasDespejadas = 0;

    // constantes para la interfaz grafica
    private final int[] posCola = {50, 100, 150, 200, 300};

    // variables del estado del juego
    private boolean estadoPausado;
    public static boolean juegoFinalizado;

    // hilo que gestiona la gravedad de las piezas
    private Timer timer = new Timer();
    private TimerTask mover = new TimerTask() {
        @Override
        public void run() {
            //System.out.println(tiempo);
            // consultando el estado del juego
            if (estadoPausado || juegoFinalizado) {
                return;
            }

            // rellenar la colaPiezas si tiene espacio libre
            if (colaPiezas.size() < 4) {
                for (int id : pieza.obtenerPermutacion()) {
                    colaPiezas.offer(id);
                }
            }

            if (tiempo >= retraso) {
                //System.out.println("-------------------------------");
                if (piezaActiva == null) {
                    // Saco una pieza de la cola y la asigno como pieza activa, esta se graficara e ira bajando
                    piezaActiva = pieza.obtenerActivo(colaPiezas.poll());
                }

                int BLOQUEO_GLOBAL = 1000;
                // intento mover la pieza una posicion mas abajo
                if (moverPieza(1, 0)) {
                    tiempoBloqueo = 0;
                    tiempo = 0;
                } else if (tiempoBloqueo >= BLOQUEO_GLOBAL) {
                    //System.out.println("------------------- guardo pieza");
                    // la pieza no se puede mover mas hacia abajo y la demora de bloqueo ha expirado,
                    // entonces coloque la pieza y verifique el fin
                    juegoFinalizado = true;
                    int puntosDelBloque = 4;
                    for (int i = 0; i < puntosDelBloque; i++) {
                        if (piezaActiva.pos[i].fila >= 0) {
                            // si la fila es mayor o igual a 0 -> al tablero le voy a asignar el id de la pieza
                            // esto para que cuando pinte vea el color y en base al id use un color para pintarlo
                            tablero[piezaActiva.pos[i].fila][piezaActiva.pos[i].columna] = piezaActiva.id;
                            if (piezaActiva.pos[i].fila >= 2) {
                                // la la fila del bloque es mayor o igual a 2 el juego no a terminado
                                juegoFinalizado = false;
                            }
                            // caso contrario el juego a terminado
                        }
                    }
                    if (juegoFinalizado) {
                        System.out.println("Juego Finalizado -- Puntaje Final " + lineasDespejadas);
                    }

                    // coloque la pieza y permita que el usuario sostenga una pieza.
                    // El tiempo de bloqueo también se restablece
                    synchronized (piezaActiva) {
                        piezaActiva = null;
                        estaEnEspera = false;
                        tiempoBloqueo = 0;
                    }

                    //limpiar las líneas y ajustar el nivel
                    despejarLineas();
                    ajustarNivel();

                    // inmediatamente consigue otra pieza
                    tiempo = retraso;
                }
                //System.out.println("No puedo bajar pieza");
                repaint();
            }
            tiempo++;
            tiempoBloqueo++;
        }
    };

    // ajustar el nivel segun el numero de lineas despejadas
    private void ajustarNivel() {
        nivel = lineasDespejadas / 4;
        System.out.println(nivel + " nivel");
        if (nivel >= 10) {
            retraso = RETRASO_GLOBAL[9];
        } else {
            retraso = RETRASO_GLOBAL[nivel];
        }
    }

    public void paint(Graphics g) {
        dim = getSize();
        bi = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_BGR);
        graficos = bi.getGraphics();
        update(g);
    }

    public void update(Graphics g) {
        graficos.setColor(fondoPanel);
        graficos.fillRect(0, 0, dim.width, dim.height);
        mostrarGrid();
        mostrarPiezas();
        mostrarUI();
        g.drawImage(bi, 0, 0, this);
    }

    private void mostrarGrid() {
        for (int i = 0; i < paredes.length; i++) {
            for (int j = 0; j < paredes[i].length; j++) {
                graficos.setColor(Color.white);
                graficos.fillRect(180 + j * 28, 20 + i * 28, 27, 27);
                graficos.setColor(Color.decode("#7f4f24"));
                //System.out.print(tablero[i][j] +" ");
                if(paredes[i][j] == -1){
                    graficos.fillRect(180 + j * 28, 20 + i * 28, 27, 27);
                }
            }
        }
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                graficos.setColor(Color.white);
                graficos.fillRect(207 + j * 28, 47 + i * 28, 29, 29);
                graficos.setColor(coloresPiezas[tablero[i][j]]);
                //System.out.print(tablero[i][j] +" ");
                graficos.fillRect(208 + j * 28, 48 + i * 28, 27, 27);
            }
            //System.out.println();
        }
    }

    // Pinta la pieza actual
    private void mostrarPiezas() {
        if (piezaActiva == null) {
            return;
        }
        synchronized (piezaActiva) {
            int d = -1;

            // mostrando la pieza fantasma
            boolean esValido = true;
            while (esValido) {
                d++;
                // si no sobrepasa los limites de la fila o si la posicion es en el tablero es diferentes de 0 es valido
                for (Pieza.Punto puntos : piezaActiva.pos) {
                    if (puntos.fila >= 0) {
                        if (puntos.fila + d >= filas || tablero[puntos.fila + d][puntos.columna] != 0) {
                            esValido = false;
                        }
                    }
                }
            }
            d--;

            //pintamos la pieza fantasma y la pieza activa
            graficos.setColor(piezaOscura);
            for (Pieza.Punto puntos : piezaActiva.pos) {
                if (puntos.fila + d >= 0) {
                    graficos.fillRect(208 + puntos.columna * 28, 48 + (puntos.fila + d) * 28, 27, 27);
                }
            }

            graficos.setColor(coloresPiezas[piezaActiva.id]);
            for (Pieza.Punto puntos : piezaActiva.pos) {
                if (puntos.fila >= 0) {
                    graficos.fillRect(208 + puntos.columna * 28, 48 + puntos.fila * 28, 27, 27);
                }
            }
        }
    }

    private void mostrarUI() {
        graficos.setColor(Color.white);
        graficos.drawString("LINEAS DESPEJADAS: " + lineasDespejadas, 10, 20);
        graficos.drawString("NIVEL ACTUAL: " + nivel, 10, 40);
        if (!multiplayer) {
            graficos.drawString("Nick Name: " + nickName, 10, 60);
        } else {
            graficos.drawString("JUGADORES: ", 10, 60);
            int y = 80;
            for (String nombre : jugadores.keySet()) {
                graficos.drawString(nombre + " " + jugadores.get(nombre), 10, y);
                y += 20;
            }
        }
        if (estadoPausado) {
            graficos.drawString("PAUSADO", 10, 30);
        }
        if (juegoFinalizado) {
            System.out.println("Juego finalizado entre");
            if (multiplayer) {
                actualizarLista();
                JOptionPane.showMessageDialog(this, "HAS PERDIDO");
                piezaActiva = null;
                tablero = new int[filas][columnas];
                colaPiezas.clear();
                nivel = 0;
                lineasDespejadas = 0;
                esperaId = 0;
                estaEnEspera = false;
                juegoFinalizado = false;
                repaint();
                menuJuego.quitarPanelTetris(this);
                menuJuego.añadirPanelMenuJuego();
            } else {
                String[] options = {"SALIR", "REINICIAR"};
                int x = JOptionPane.showOptionDialog(this, "SCORE: " + lineasDespejadas,
                        "Informacion", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (x == 0) {
                    piezaActiva = null;
                    tablero = new int[filas][columnas];
                    colaPiezas.clear();
                    nivel = 0;
                    lineasDespejadas = 0;
                    esperaId = 0;
                    estaEnEspera = false;
                    juegoFinalizado = false;
                    menuJuego.quitarPanelTetris(this);
                    menuJuego.añadirPanelMenuJuego();
                } else {
                    piezaActiva = null;
                    tablero = new int[filas][columnas];
                    colaPiezas.clear();
                    nivel = 0;
                    lineasDespejadas = 0;
                    esperaId = 0;
                    estaEnEspera = false;
                    juegoFinalizado = false;
                    repaint();
                }
            }
            //graficos.drawString("JUEGO FINALIZADO -- Q PARA SALIR; R PARA REINICIAR", 10, 580);
        }

        graficos.drawString("EN ESPERA", 40, 450);
        graficos.drawString("SIGUIENTE", 40, 200);
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 4; j++) {
                    graficos.fillRect(40 + j * 20, 150 + i * 20 + posCola[k], 19, 19);
                }
            }
        }
        // pinta la pieza en espera
        if (esperaId != 0) {
            Pieza.Activo piezaEspera = pieza.obtenerActivo(esperaId - 1);
            graficos.setColor(coloresPiezas[piezaEspera.id]);
            for (Pieza.Punto puntos : piezaEspera.pos) {
                graficos.fillRect(40 + (puntos.columna - 3) * 20, 170 + puntos.fila * 20 + posCola[4], 19, 19);
            }
        }
        // pinta la colaPiezas de piezas
        int i = 0;
        for (int id : colaPiezas) {
            Pieza.Activo piezaSiguiente = pieza.obtenerActivo(id);
            graficos.setColor(coloresPiezas[piezaSiguiente.id]);
            for (Pieza.Punto puntos : piezaSiguiente.pos) {
                graficos.fillRect(40 + (puntos.columna - 3) * 20, 170 + puntos.fila * 20 + posCola[i], 19, 19);
            }
            i++;
            if (i >= 4) {
                break;
            }
        }
    }

    // se borran las líneas completas y se incrementa la variable respectiva
    private void despejarLineas() {
        while (true) {
            // verificamos si hay una línea que está llena
            int filaCompleta = -1;
            for (int j = 0; j < filas; j++) {
                int contador = 0;
                for (int i = 0; i < columnas; i++) {
                    contador += tablero[j][i] != 0 ? 1 : 0;
                }
                if (contador == columnas) {
                    filaCompleta = j;
                    if (multiplayer) {
                        enviarPiso();
                    }
                    break;
                }
            }
            if (filaCompleta == -1) {
                break;
            }
            // eliminando las líneas completas una por una
            int[][] temp = new int[filas][columnas];
            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    temp[i][j] = tablero[i][j];
                }
            }
            for (int i = 0; i < filaCompleta + 1; i++) {
                for (int j = 0; j < columnas; j++) {
                    if (i == 0) {
                        tablero[i][j] = 0;
                    } else {
                        tablero[i][j] = temp[i - 1][j];
                    }
                }
            }
            lineasDespejadas++;
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // cuando se suelta hacia abajo, la caída suave se desactiva
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            retraso = nivel >= 20 ? RETRASO_GLOBAL[19] : RETRASO_GLOBAL[nivel];
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // tres casos que manejan cuando el usuario ajusta los estados del juego (ACTIVO, PAUSADO, CERRADO)
        if (e.getKeyCode() == KeyEvent.VK_P) {
            estadoPausado = !estadoPausado;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            juegoFinalizado = true;
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            piezaActiva = null;
            tablero = new int[filas][columnas];
            colaPiezas.clear();
            nivel = 0;
            lineasDespejadas = 0;
            esperaId = 0;
            estaEnEspera = false;
            juegoFinalizado = false;
            repaint();
            return;
        }
        if (piezaActiva == null || estadoPausado) {
            return;
        }
        switch (e.getKeyCode()) {
            // Mover pieza a la izquierda
            case KeyEvent.VK_LEFT:
                moverPieza(0, -1);
                repaint();
                break;
            // Mover pieza a la derecha
            case KeyEvent.VK_RIGHT:
                moverPieza(0, 1);
                repaint();
                break;
            // rotar sentido horario
            case KeyEvent.VK_UP:
            case KeyEvent.VK_X:
                girarDerecha();
                break;
            // rotar sentido antihorario
            case KeyEvent.VK_Z:
                girarIzquierda();
                break;
            // caída suave
            case KeyEvent.VK_DOWN:
                retraso = (nivel >= 20 ? RETRASO_GLOBAL[19] : RETRASO_GLOBAL[nivel]) / 8;
                break;
            // pieza en espera
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_C:
                if (estaEnEspera) {
                    break;
                }
                if (esperaId == 0) {
                    esperaId = piezaActiva.id;
                    piezaActiva = null;
                } else {
                    int temp = esperaId;
                    esperaId = piezaActiva.id;
                    piezaActiva = pieza.obtenerActivo(temp - 1);
                }
                estaEnEspera = true;
                tiempo = 1 << 30;
                break;
                // caída dura
            case KeyEvent.VK_SPACE:
                tiempo = 1 << 30;
                tiempoBloqueo = 1 << 30;
                // caída firme
            case KeyEvent.VK_CONTROL:
                tiempo = 1 << 30;
                while (moverPieza(1, 0)) ;
                break;
        }
        repaint();
    }

    // intentando girar la pieza en sentido antihorario
    // Condición posterior: la pieza actual se rotará en sentido antihorario si hay una caja (de cinco) que funcione
    private void girarIzquierda() {
        if (piezaActiva.id == 1) {
            return;
        }
        Pieza.Punto[] np = new Pieza.Punto[4];
        for (int i = 0; i < 4; i++) {
            int numeroFila = piezaActiva.pos[i].columna - piezaActiva.loColumna + piezaActiva.loFila;
            int numeroColumna = piezaActiva.pos[i].fila - piezaActiva.loFila + piezaActiva.loColumna;
            np[i] = new Pieza.Punto(numeroFila, numeroColumna);
        }
        int loFila = piezaActiva.loFila;
        int hiFila = piezaActiva.hiFila;
        for (int i = 0; i < 4; i++) {
            np[i].fila = hiFila - (np[i].fila - loFila);
        }
        girar(np, piezaActiva.estado * 2 + 1);
        repaint();
    }

    // intentando girar la pieza en sentido horario
    // Condición de publicación: la pieza actual se rotará en el sentido del horario si hay una caja (de cinco) que funcione
    private void girarDerecha() {
        if (piezaActiva.id == 1) {
            return;
        }
        Pieza.Punto[] np = new Pieza.Punto[4];
        for (int i = 0; i < 4; i++) {
            int nFila = piezaActiva.pos[i].columna - piezaActiva.loColumna + piezaActiva.loFila;
            int nColumna = piezaActiva.pos[i].fila - piezaActiva.loFila + piezaActiva.loColumna;
            np[i] = new Pieza.Punto(nFila, nColumna);
        }
        int loColumna = piezaActiva.loColumna;
        int loFila = piezaActiva.hiColumna;
        for (int i = 0; i < 4; i++) {
            np[i].columna = loFila - (np[i].columna - loColumna);
        }
        girar(np, piezaActiva.estado * 2);
        repaint();
    }

    // método qué gira de acuerdo a los parámetros recibidos
    private void girar(Pieza.Punto[] pos, int id) {
        for (int i = 0; i < 5; i++) {
            boolean valid = true;
            int dFila = piezaActiva.id == 2 ? moverFila2[id][i] : moverFila1[id][i];
            int dColumna = piezaActiva.id == 2 ? moverColumna2[id][i] : moverColumna1[id][i];
            for (Pieza.Punto puntos : pos) {
                if (puntos.fila + dFila < 0 || puntos.fila + dFila >= filas) {
                    valid = false;
                } else if (puntos.columna + dColumna < 0 || puntos.columna + dColumna >= 10) {
                    valid = false;
                } else if (tablero[puntos.fila + dFila][puntos.columna + dColumna] != 0) {
                    valid = false;
                }
            }
            if (valid) {
                for (int j = 0; j < 4; j++) {
                    piezaActiva.pos[j].fila = pos[j].fila + dFila;
                    piezaActiva.pos[j].columna = pos[j].columna + dColumna;
                }
                piezaActiva.hiColumna += dColumna;
                piezaActiva.loColumna += dColumna;
                piezaActiva.hiFila += dFila;
                piezaActiva.loFila += dFila;
                if (id % 2 == 1) {
                    piezaActiva.estado = (piezaActiva.estado + 3) % 4;
                } else {
                    piezaActiva.estado = (piezaActiva.estado + 1) % 4;
                }
                return;
            }
        }
    }

    // método encargado de mover las piezas
    private boolean moverPieza(int dFila, int dColumna) {
        if (piezaActiva == null) {
            return false;
        }
        for (Pieza.Punto puntos : piezaActiva.pos) {
            if (puntos.fila + dFila >= filas) {
                return false;
            }
            if (puntos.columna + dColumna < 0 || puntos.columna + dColumna >= columnas) {
                return false;
            }
            if (tablero[puntos.fila + dFila][puntos.columna + dColumna] != 0) {
                return false;
            }
        }
        int puntosDelBloque = 4;
        for (int i = 0; i < puntosDelBloque; i++) {
            piezaActiva.pos[i].fila += dFila;
            piezaActiva.pos[i].columna += dColumna;
        }
        piezaActiva.loColumna += dColumna;
        piezaActiva.hiColumna += dColumna;
        piezaActiva.loFila += dFila;
        piezaActiva.hiFila += dFila;
        return true;
    }

    // envia una fila para añadirlo en el piso del otro jugadores
    public void enviarPiso() {
        String dato = "id," + id;
        try {
            out.writeUTF(dato);
        } catch (IOException ex) {
            Logger.getLogger(Tetris.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void enviarNickName() {
        try {
            out.writeUTF("nickName," + id + "," + nickName + "," + (!juegoFinalizado ? " Jugando" : " Eliminado"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // añade un piso al otro jugadores
    protected void añadirPiso(int linea) {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (tablero[i][j] != 0 && i - linea < 0) {
                    juegoFinalizado = true;
                } else if (i - linea >= 0) {
                    tablero[i - linea][j] = tablero[i][j];
                }
            }
        }
        if (filas > 0) {
            filas--;
        }
        repaint();
    }

    public static void actualizarLista() {
        try {
            out.writeUTF("actualizar,lista");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String mensaje = in.readUTF();
            id = Integer.parseInt(mensaje);

            //Ciclo infinito, para estar escuchando por los movimientos de los jugadores
            while (true) {
                //Recibimos el mensaje
                mensaje = in.readUTF();
                String[] mensajes = mensaje.split(",");
                if (mensajes[0].equals("nickName")) {
                    //System.out.println(mensajes[1] +"+"+ mensajes[2]);
                    jugadores.put(mensajes[2], mensajes[3]);
                } else if (mensajes[0].equals("actualizar")) {
                    enviarNickName();
                } else if (mensajes[0].equals("id")) {
                    int aux1 = Integer.parseInt(mensajes[1]);
                    if (aux1 != id) {
                        añadirPiso(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // bloques J L S T Z
    private final int[][] moverColumna1 = {
            {0, -1, -1, 0, -1},
            {0, +1, +1, 0, +1},
            {0, +1, +1, 0, +1},
            {0, +1, +1, 0, +1},
            {0, +1, +1, 0, +1},
            {0, -1, -1, 0, -1},
            {0, -1, -1, 0, -1},
            {0, -1, -1, 0, -1}
    };

    private final int[][] moverFila1 = {
            {0, 0, +1, 0, -2},
            {0, 0, -1, 0, +2},
            {0, 0, -1, 0, +2},
            {0, 0, +1, 0, -2},
            {0, 0, +1, 0, -2},
            {0, 0, +1, 0, -2},
            {0, 0, -1, 0, +2},
            {0, 0, -1, 0, +2}
    };

    // bloque I
    private final int[][] moverColumna2 = {
            {0, -2, +1, -2, +1},
            {0, -1, +2, -1, +2},
            {0, -1, +2, -1, +2},
            {0, +2, -1, +2, -1},
            {0, +2, -1, +2, -1},
            {0, +1, -2, +1, -2},
            {0, +1, -2, +1, -2},
            {0, -2, +1, -2, +1}
    };

    private final int[][] moverFila2 = {
            {0, 0, 0, -1, +2},
            {0, 0, 0, +2, -1},
            {0, 0, 0, +2, -1},
            {0, 0, 0, +1, -2},
            {0, 0, 0, +1, -2},
            {0, 0, 0, -2, +1},
            {0, 0, 0, -2, +1},
            {0, 0, 0, -1, +2}
    };

    public void cargarParedes(){
        for (int i = 0; i < paredes.length; i++){
            for (int j = 0; j < paredes[i].length; j++) {
                if(i == 0 || i == 23 || j == 0 || j == 11){
                    paredes[i][j] = -1;
                } else {
                    paredes[i][j] = -2;
                }
            }
        }
    }
}


