public class Transicion {
    private int origen;
    private char simbolo;
    private int destino;

    public Transicion(int origen, char simbolo, int destino) {
        this.origen = origen;
        this.simbolo = simbolo;
        this.destino = destino;
    }

    public int getOrigen() {
        return origen;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public int getDestino() {
        return destino;
    }
}
