import java.util.ArrayList;

public class EvaluadorAFN {
    // '#' es el simbolo ASCII reservado para las transiciones vacias.
    public static final char EPSILON = '#';

    private ArrayList<Estado> estados = new ArrayList<Estado>();
    private ArrayList<Character> alfabeto = new ArrayList<Character>();
    private ArrayList<Transicion> transiciones = new ArrayList<Transicion>();

    public boolean agregarEstado(int numero) {
        if (numero < 0 || buscarEstado(numero) != -1) {
            return false;
        }
        estados.add(new Estado(numero));
        return true;
    }

    public boolean agregarSimbolo(char simbolo) {
        boolean letra = (simbolo >= 'a' && simbolo <= 'z')
                || (simbolo >= 'A' && simbolo <= 'Z');
        boolean digito = simbolo >= '0' && simbolo <= '9';
        if ((!letra && !digito) || perteneceAlAlfabeto(simbolo)) {
            return false;
        }
        alfabeto.add(simbolo);
        return true;
    }

    public boolean definirInicial(int numero) {
        int posicion = buscarEstado(numero);
        if (posicion == -1) {
            return false;
        }
        for (int i = 0; i < estados.size(); i++) {
            estados.get(i).setInicial(i == posicion);
        }
        return true;
    }

    public boolean agregarFinal(int numero) {
        int posicion = buscarEstado(numero);
        if (posicion == -1 || estados.get(posicion).esFinal()) {
            return false;
        }
        estados.get(posicion).setFinal(true);
        return true;
    }

    public boolean agregarTransicion(int origen, char simbolo, int destino) {
        if (buscarEstado(origen) == -1 || buscarEstado(destino) == -1) {
            return false;
        }
        if (simbolo != EPSILON && !perteneceAlAlfabeto(simbolo)) {
            return false;
        }
        for (int i = 0; i < transiciones.size(); i++) {
            Transicion actual = transiciones.get(i);
            if (actual.getOrigen() == origen && actual.getSimbolo() == simbolo
                    && actual.getDestino() == destino) {
                return false;
            }
        }
        transiciones.add(new Transicion(origen, simbolo, destino));
        return true;
    }

    // Propiedad de solo lectura en Java: se consulta mediante este getter.
    public boolean isQuintuploValido() {
        if (estados.size() == 0 || alfabeto.size() == 0) {
            return false;
        }
        int iniciales = 0;
        int finales = 0;
        for (int i = 0; i < estados.size(); i++) {
            if (estados.get(i).esInicial()) {
                iniciales++;
            }
            if (estados.get(i).esFinal()) {
                finales++;
            }
        }
        // La relacion de transiciones puede ser vacia: sigue siendo un AFN.
        // Los elementos solo ingresan por los metodos que los validan.
        return iniciales == 1 && finales > 0;
    }

    public char evaluarPalabra(char[] palabra) {
        if (!isQuintuploValido()) {
            return 'e';
        }
        if (palabra == null) {
            return 'p';
        }
        // Revisar la palabra completa antes de ejecutar el automata.
        for (int i = 0; i < palabra.length; i++) {
            if (!perteneceAlAlfabeto(palabra[i])) {
                return 'p';
            }
        }

        boolean[] actuales = new boolean[estados.size()];
        for (int i = 0; i < estados.size(); i++) {
            actuales[i] = estados.get(i).esInicial();
        }
        actuales = cerraduraEpsilon(actuales);

        for (int i = 0; i < palabra.length; i++) {
            boolean[] siguientes = new boolean[estados.size()];
            for (int j = 0; j < transiciones.size(); j++) {
                Transicion transicion = transiciones.get(j);
                int origen = buscarEstado(transicion.getOrigen());
                if (actuales[origen] && transicion.getSimbolo() == palabra[i]) {
                    int destino = buscarEstado(transicion.getDestino());
                    siguientes[destino] = true;
                }
            }
            actuales = cerraduraEpsilon(siguientes);
        }

        for (int i = 0; i < estados.size(); i++) {
            if (actuales[i] && estados.get(i).esFinal()) {
                return 's';
            }
        }
        return 'n';
    }

    private boolean[] cerraduraEpsilon(boolean[] conjunto) {
        boolean[] resultado = new boolean[estados.size()];
        for (int i = 0; i < conjunto.length; i++) {
            resultado[i] = conjunto[i];
        }
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int i = 0; i < transiciones.size(); i++) {
                Transicion transicion = transiciones.get(i);
                if (transicion.getSimbolo() == EPSILON) {
                    int origen = buscarEstado(transicion.getOrigen());
                    int destino = buscarEstado(transicion.getDestino());
                    if (resultado[origen] && !resultado[destino]) {
                        resultado[destino] = true;
                        cambio = true;
                    }
                }
            }
        }
        return resultado;
    }

    private int buscarEstado(int numero) {
        for (int i = 0; i < estados.size(); i++) {
            if (estados.get(i).getNumero() == numero) {
                return i;
            }
        }
        return -1;
    }

    private boolean perteneceAlAlfabeto(char simbolo) {
        for (int i = 0; i < alfabeto.size(); i++) {
            if (alfabeto.get(i) == simbolo) {
                return true;
            }
        }
        return false;
    }

    public void borrarQuintuplo() {
        estados.clear();
        alfabeto.clear();
        transiciones.clear();
    }

    // La clase principal imprime la tabla exigida por el PDF.
    public void imprimirTablaTransiciones() {
        System.out.println("\nTABLA DE TRANSICIONES (# = epsilon)");
        System.out.println("-> inicial, * final, - sin transicion");
        System.out.print("Estado\t");
        for (int i = 0; i < alfabeto.size(); i++) {
            System.out.print(alfabeto.get(i) + "\t");
        }
        System.out.println(EPSILON);

        for (int i = 0; i < estados.size(); i++) {
            Estado estado = estados.get(i);
            String marca = "";
            if (estado.esInicial()) {
                marca += "->";
            }
            if (estado.esFinal()) {
                marca += "*";
            }
            System.out.print(marca + "q" + estado.getNumero() + "\t");
            for (int j = 0; j <= alfabeto.size(); j++) {
                char simbolo = EPSILON;
                if (j < alfabeto.size()) {
                    simbolo = alfabeto.get(j);
                }
                String destinos = "";
                for (int k = 0; k < transiciones.size(); k++) {
                    Transicion transicion = transiciones.get(k);
                    if (transicion.getOrigen() == estado.getNumero()
                            && transicion.getSimbolo() == simbolo) {
                        if (destinos.length() > 0) {
                            destinos += ",";
                        }
                        destinos += "q" + transicion.getDestino();
                    }
                }
                if (destinos.length() == 0) {
                    destinos = "-";
                }
                System.out.print(destinos + "\t");
            }
            System.out.println();
        }
        System.out.println("Quintuplo completo y valido: " + isQuintuploValido());
    }
}



