public class CargadorAutomata {
    public boolean cargar(EvaluadorAFN evaluador, int[] estados, char[] alfabeto, int inicial, int[] finales, int[][] transiciones) {
        evaluador.borrarQuintuplo();

        if (estados == null || alfabeto == null || finales == null || transiciones == null) {
            return false;
        }
        for (int i = 0; i < transiciones.length; i++) {
            if (transiciones[i] == null || transiciones[i].length != 3) {
                return false;
            }
            if (transiciones[i][1] < Character.MIN_VALUE || transiciones[i][1] > Character.MAX_VALUE) {
                return false;
            }
        }

        boolean correcto = true;
        for (int i = 0; i < estados.length; i++) {
            correcto = evaluador.agregarEstado(estados[i]) && correcto;
        }
        for (int i = 0; i < alfabeto.length; i++) {
            correcto = evaluador.agregarSimbolo(alfabeto[i]) && correcto;
        }
        correcto = evaluador.definirInicial(inicial) && correcto;
        for (int i = 0; i < finales.length; i++) {
            correcto = evaluador.agregarFinal(finales[i]) && correcto;
        }
        for (int i = 0; i < transiciones.length; i++) {
            int origen = transiciones[i][0];
            char simbolo = (char) transiciones[i][1];
            int destino = transiciones[i][2];
            correcto = evaluador.agregarTransicion(origen, simbolo, destino) && correcto;
        }

        if (!correcto || !evaluador.isQuintuploValido()) {
            // Una carga fallida deja el objeto vacio para evitar usar datos parciales.
            evaluador.borrarQuintuplo();
            return false;
        }
        return true;
    }
}


