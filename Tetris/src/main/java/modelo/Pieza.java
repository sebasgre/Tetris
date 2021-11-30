package modelo;

public class Pieza {

    // Punto en la cuadricula
    static class Punto {
        int fila;
        int columna;

        Punto(int fila, int columna) {
            this.fila = fila;
            this.columna = columna;
        }
    }

    // metodo creador de puntos
    private Punto mp(int fila, int columna) {
        return new Punto(fila, columna);
    }

    // puntos para la posicion inicial de cada pieza
    private final Punto[][] piezas = {

            {mp(-1, 4), mp(-1, 5), mp(0, 4), mp(0, 5)},
            {mp(-1, 3), mp(-1, 4), mp(-1, 5), mp(-1, 6)},
            {mp(-1, 3), mp(0, 3), mp(0, 4), mp(0, 5)},
            {mp(-1, 5), mp(0, 3), mp(0, 4), mp(0, 5)},
            {mp(0, 3), mp(0, 4), mp(-1, 4), mp(-1, 5)},
            {mp(-1, 3), mp(-1, 4), mp(0, 4), mp(0, 5)},
            {mp(0, 3), mp(0, 4), mp(0, 5), mp(-1, 4)}

    };

    // Pieza Activa
    static class Activo {
        Punto[] pos;
        int id;
        int loFila, hiFila, loColumna, hiColumna;
        int estado = 0;

        public Activo(Punto[] pos, int id) {
            this.pos = pos;
            this.id = id;
            if (id != 2) {
                loFila = 0;
                hiFila = 2;
                loColumna = 3;
                hiColumna = 5;
            } else {
                loFila = 0;
                hiFila = 3;
                loColumna = 3;
                hiColumna = 6;
            }
        }
    }

    // retorna una pieza con una identificacion especifica
    public Activo obtenerActivo(int id) {
        // Guardamos puntos que sacamos de nuestra matriz de piezas
        Punto[] puntoPiezaNueva = new Punto[4];
        for (int i = 0; i < piezas[i].length; i++) {
            puntoPiezaNueva[i] = new Punto(piezas[id][i].fila, piezas[id][i].columna);
        }
        //retorna un objetos activo que contiene un vector de puntos de 4 piezas, id, filas y columnas
        return new Activo(puntoPiezaNueva, id + 1);
    }


    /*
    static class Activo {
        Punto[] pos;
        int id;
        int filaInicio, filaFinal, columnaInicio, columnaFinal;
        int estado = 0;

        Activo(Punto[] pos, int id) {
            this.pos = pos;
            this.id = id;
            filaInicio = 0;
            filaFinal = 0;
            columnaInicio = 3;
            columnaFinal = 6;
            if (id != 2) {
                filaInicio = 0;
                filaFinal = 2;
                columnaInicio = 3;
                columnaFinal = 5;
            }
        }

    }
     */

    // genera una permutacion de las siete piezas y la retorna
    public int[] obtenerPermutacion() {
        int[] piezas = new int[7];
        for (int i = 0; i < piezas.length; i++) {
            piezas[i] = i;
        }
        permutar(0, piezas);
        return piezas;
    }

    private void permutar(int i, int[] a) {
        if (i == 6) {
            return;
        }
        int intercambiar = (int) ((Math.random() * (6 - i)) + i + 1);
        //System.out.println(intercambiar + " Intercambiar");
        int temp = a[i]; // 0
        a[i] = a[intercambiar]; // 0 = 4
        a[intercambiar] = temp; // 4 = 0
        permutar(i + 1, a);
    }
}
