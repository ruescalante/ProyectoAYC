import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // QUINTUPLO: modificar estas variables para usar otro automata.
        String expresionRegular = "(a|b)*abb";
        int[] estados = {0, 1, 2, 3, 4, 5};
        char[] alfabeto = {'a', 'b'};
        int estadoInicial = 0;
        int[] estadosFinales = {5};

        // Cada fila representa {origen, simbolo, destino}. '#' es epsilon.
        int[][] transiciones = {
            {0, '#', 1},
            {1, '#', 0},
            {1, 'a', 1},
            {1, 'b', 1},
            {1, 'a', 2},
            {2, 'b', 3},
            {3, 'b', 4},
            {4, '#', 5}
        };

        CargadorAutomata cargador = new CargadorAutomata();
        ResultadoEvaluacion resultados = new ResultadoEvaluacion();
        EvaluadorAFN evaluador = new EvaluadorAFN();

        if (!cargador.cargar(evaluador, estados, alfabeto, estadoInicial, estadosFinales, transiciones)) {
            System.out.println("Error de configuracion: revise el quintuplo en Main.java.");
            System.out.println(resultados.obtenerMensaje('e'));
            return;
        }

        System.out.println("EVALUADOR DE EXPRESIONES REGULARES CON AFN-EPSILON");
        System.out.println("Expresion regular: " + expresionRegular);
        evaluador.imprimirTablaTransiciones();
        System.out.println("\nEscriba palabras para comprobar la expresion regular.");
        System.out.println("Enter sin texto evalua la palabra vacia. Escriba :salir para terminar.");

        Scanner teclado = new Scanner(System.in);
        boolean continuar = true;
        while (continuar) {
            System.out.print("\nPalabra: ");
            if (!teclado.hasNextLine()) {
                continuar = false;
            } else {
                String palabra = teclado.nextLine();
                if (palabra.equals(":salir")) {
                    continuar = false;
                } else {
                    char resultado = evaluador.evaluarPalabra(palabra.toCharArray());
                    System.out.println(resultados.obtenerMensaje(resultado));
                }
            }
        }
        teclado.close();
        System.out.println("Fin del programa.");
    }
}

