public class Estado {
    private int numero;
    private boolean inicial;
    private boolean fin;

    public Estado(int numero) {
        this.numero = numero;
        inicial = false;
        fin = false;
    }

    public int getNumero() {
        return numero;
    }

    public boolean esInicial() {
        return inicial;
    }

    public boolean esFinal() {
        return fin;
    }

    public void setInicial(boolean inicial) {
        this.inicial = inicial;
    }

    public void setFinal(boolean fin) {
        this.fin = fin;
    }
}
