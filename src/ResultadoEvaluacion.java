public class ResultadoEvaluacion {
    public String obtenerMensaje(char resultado) {
        switch (resultado) {
            case 'e':
                return "e: Error. Falta definir un quintuplo completo y valido.";
            case 'p':
                return "p: La palabra contiene simbolos fuera del alfabeto.";
            case 's':
                return "s: La palabra satisface la expresion regular.";
            case 'n':
                return "n: La palabra no satisface la expresion regular.";
            default:
                return "Resultado desconocido.";
        }
    }
}
