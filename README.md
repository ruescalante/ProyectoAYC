# Proyecto AYC104: evaluador de un AFN con epsilon

Aplicación de consola en Java, orientada a objetos, basada en el caso de estudio. No necesita Maven, Gradle ni dependencias externas. Se compiló y probó con el JDK 25 instalado en esta máquina.

## Ejecutar

Haz doble clic en **ejecutar.bat**, o ejecútalo desde PowerShell en esta carpeta:

```powershell
.\ejecutar.bat
```

El archivo compila las clases en `out` y abre el programa. Busca el JDK en `JAVA_HOME`, en la ruta de Adoptium de esta máquina y, como alternativa, en el `PATH`. En otra máquina puedes definir `JAVA_HOME` con la carpeta de su JDK.

También puedes compilar y ejecutar directamente:

```powershell
& 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe' -encoding UTF-8 -d out src\*.java
& 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe' -cp out Main
```

## Uso del programa

El quíntuplo está definido en las variables al inicio de `src/Main.java`. Al ejecutar, el programa carga y valida esos datos, muestra la expresión y genera automáticamente la tabla. Después solo solicita palabras.

- Escribe una palabra y pulsa Enter para obtener `s`, `n` o `p`.
- Pulsa Enter sin escribir para comprobar la palabra vacía.
- Escribe `:salir` para terminar. Este comando se procesa antes de evaluar y no se envía al autómata.
- Los espacios se conservan y se rechazan porque no pertenecen al alfabeto.

Si la configuración contiene elementos inválidos o está incompleta, el programa muestra un error y termina antes de pedir palabras. Para cambiar la expresión, modifica el quíntuplo en el código y vuelve a ejecutar `ejecutar.bat`.

## Cómo definir el quíntuplo y las transiciones

Estas variables se encuentran al inicio de `Main.main`:

```java
String expresionRegular = "(a|b)*abb";
int[] estados = {0, 1, 2, 3, 4, 5};
char[] alfabeto = {'a', 'b'};
int estadoInicial = 0;
int[] estadosFinales = {5};

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
```

`estados` representa Q; `alfabeto` representa Σ; `estadoInicial` representa q₀; `estadosFinales` representa F. La matriz `transiciones` representa δ, la relación de transiciones.

**Cada fila representa una transición con el orden `{origen, símbolo, destino}`.** Una matriz `int[][]` es un arreglo de arreglos: `transiciones[i]` obtiene una fila, y las posiciones 0, 1 y 2 de esa fila contienen sus componentes.

| Fila `i` | Origen `[i][0]` | Símbolo `[i][1]` | Destino `[i][2]` | Significado |
| --- | --- | --- | --- | --- |
| 0 | 0 | `#` | 1 | De 0 a 1 sin consumir carácter. |
| 1 | 1 | `#` | 0 | De 1 a 0 sin consumir carácter. |
| 2 | 1 | `a` | 1 | De 1 a 1 consumiendo `a`. |
| 3 | 1 | `b` | 1 | De 1 a 1 consumiendo `b`. |
| 4 | 1 | `a` | 2 | De 1 a 2 consumiendo `a`. |
| 5 | 2 | `b` | 3 | De 2 a 3 consumiendo `b`. |
| 6 | 3 | `b` | 4 | De 3 a 4 consumiendo `b`. |
| 7 | 4 | `#` | 5 | De 4 a 5 sin consumir carácter. |

Para agregar una transición, agrega una fila como `{2, 'a', 3}`. Cada fila debe tener exactamente tres elementos. Para permitir dos destinos con el mismo símbolo, escribe dos filas con igual origen y símbolo, pero destinos diferentes: las filas 2 y 4 muestran este caso.

Como un arreglo de Java tiene un único tipo de dato, usamos `int[][]`: los estados se guardan como enteros y Java convierte automáticamente caracteres como `'a'` a su valor numérico. Al cargar, `(char) transiciones[i][1]` recupera el carácter. Escribe los símbolos entre comillas simples para que la configuración sea fácil de leer. Antes de convertir, se comprueba que el valor esté dentro del rango de `char`; después el evaluador valida que sea un símbolo permitido.

La tabla no se introduce por separado: el evaluador la construye con las transiciones cargadas. `expresionRegular` es únicamente el texto que se muestra; **cambiar ese texto no cambia el comportamiento**. Debes construir manualmente el AFN de la nueva expresión y actualizar sus cinco componentes.

Por ejemplo, para `a*`, reemplaza las variables de configuración por:

```java
String expresionRegular = "a*";
int[] estados = {0};
char[] alfabeto = {'a'};
int estadoInicial = 0;
int[] estadosFinales = {0};
int[][] transiciones = {
    {0, 'a', 0}
};
```

Ese autómata acepta la palabra vacía, `a`, `aa`, etc. El inicial también es final y tiene un bucle con `a`. En este caso no hacen falta transiciones epsilon.

## Clases y requisitos

| Archivo | Responsabilidad |
| --- | --- |
| `src/EvaluadorAFN.java` | Clase principal del AFN: valida el quíntuplo, evalúa e imprime la tabla. |
| `src/Main.java` | Define el quíntuplo, coordina los objetos y maneja la consola. |
| `src/Estado.java` | Clase complementaria: número de estado y marcas de inicial y final. |
| `src/Transicion.java` | Clase complementaria: origen, símbolo y destino de una transición. |
| `src/CargadorAutomata.java` | Comprueba los arreglos y carga sus elementos en el evaluador. |
| `src/ResultadoEvaluacion.java` | Devuelve un mensaje para cada código de resultado. |

- Los métodos de `EvaluadorAFN` solo reciben `int`, `char`, o `char[]`. No reciben listas, objetos `Estado`, objetos `Transicion` ni un `String`. `Main` es el punto de entrada de Java; la clase principal que resuelve la expresión, según el PDF, es `EvaluadorAFN`.
- `agregarEstado`, `agregarSimbolo`, `definirInicial`, `agregarFinal` y `agregarTransicion` devuelven `true` cuando aceptan el elemento. Rechazan duplicados y referencias a estados o símbolos inexistentes. Definir otro inicial reemplaza el anterior.
- `isQuintuploValido()` es la propiedad pública de consulta mediante un getter, que es la forma usual de representar una propiedad de solo lectura en Java. Requiere estados, alfabeto, exactamente un inicial y al menos un final.
- La relación de transiciones empieza como conjunto vacío y recibe sus elementos mediante `agregarTransicion`. Un conjunto de transiciones vacío es válido matemáticamente: solo puede aceptar la palabra vacía si el inicial es final. Para esta aplicación se exige un alfabeto y un conjunto de finales no vacíos.
- El alfabeto admite `a` a `z`, `A` a `Z` y `0` a `9`. El símbolo ASCII `#` está reservado para epsilon y no puede ser parte de una palabra aceptada.
- Se usan `ArrayList` internamente para almacenar los datos. Las búsquedas son ciclos escritos en el código. No se usan `contains`, `indexOf`, búsquedas de colecciones, reconocimiento de patrones ni paquetes de expresiones regulares.
- Las clases complementarias no reciben datos del teclado ni imprimen. Todos los métodos declarados en las clases de la aplicación tienen llamadas en la solución.
- No se convierte automáticamente una expresión textual a un autómata: el PDF solicita recibir el quíntuplo que representa la expresión y ejecutar ese AFN.

## Explicación del código, parte por parte

### 1. Cómo se relacionan los archivos

El programa comienza en `Main.main`, donde están las variables explícitas del quíntuplo. Crea el cargador, los mensajes y el evaluador. El cargador recibe los arreglos y el inicial y carga sus elementos en `EvaluadorAFN`. Este objeto guarda los estados y transiciones y evalúa las palabras.

```text
Main: variables del quíntuplo, punto de entrada y consola
  |-- CargadorAutomata: valida la matriz y carga cada elemento
  |-- ResultadoEvaluacion: devuelve los textos de resultados
  |-- EvaluadorAFN: ejecuta el autómata
        |-- Estado: número y marcas
        |-- Transicion: origen, símbolo y destino
```

Las variables de `Main.main` contienen los datos explícitos. Las listas privadas de `EvaluadorAFN` contienen los elementos ya cargados y se conservan entre evaluaciones. Los arreglos de estados activos se crean de nuevo para cada palabra.

### 2. Conceptos de Java que aparecen en el código

| Elemento | Qué significa en este proyecto |
| --- | --- |
| `class` | Define un tipo de objeto, con datos y operaciones. |
| `private` | El dato o método solo se utiliza directamente dentro de su clase. |
| `public` | Permite que otra clase utilice el constructor, método o constante. |
| `static` | El miembro pertenece a la clase; no requiere crear un objeto de ella para llamarlo. |
| `final` | Impide cambiar el valor de la constante `EPSILON`. |
| `new` | Crea un objeto o arreglo. |
| `this.numero = numero` | Guarda el parámetro `numero` en el campo del objeto actual. |
| `boolean` | Guarda `true` o `false`, por ejemplo para marcar un estado final. |
| `char` y `char[]` | Un carácter y un arreglo de caracteres, respectivamente. |
| `return` | Termina el método y, cuando corresponde, devuelve un resultado. |
| `&&`, `||` y `!` | Representan «y», «o» y «no» en una condición. |
| `condicion ? valor1 : valor2` | Elige `valor1` si la condición es verdadera; en otro caso elige `valor2`. |

### 3. `Estado.java`: representar un estado

Sus tres campos son:

```java
private int numero;
private boolean inicial;
private boolean fin;
```

`numero` identifica el estado, como 0 o 40. `inicial` indica si la ejecución comienza allí. `fin` indica si es un estado de aceptación. Un mismo estado puede ser inicial y final.

El constructor `Estado(int numero)` guarda el identificador y deja ambas marcas en `false`. Crear un estado no lo convierte automáticamente en inicial ni final.

| Método | Qué hace y dónde se utiliza |
| --- | --- |
| `getNumero()` | Devuelve el identificador. El evaluador lo usa para buscar estados e imprimir la tabla. |
| `esInicial()` | Devuelve la marca de inicial. Se consulta para validar el quíntuplo, iniciar una evaluación y mostrar la tabla. |
| `esFinal()` | Devuelve la marca de aceptación. Se consulta al agregar finales, validar, evaluar e imprimir. |
| `setInicial(boolean inicial)` | Cambia la marca de inicial. Lo llama `definirInicial`. |
| `setFinal(boolean fin)` | Cambia la marca de final. Lo llama `agregarFinal`. |

Estos métodos permiten que el evaluador trabaje con los datos del estado sin acceder directamente a sus campos privados.

### 4. `Transicion.java`: representar un movimiento

Una transición contiene `origen`, `simbolo` y `destino`. El constructor recibe esos tres valores y los guarda.

```java
new Transicion(1, 'a', 2)
```

Este objeto representa: «desde el estado 1, al consumir una `a`, se puede llegar al estado 2». Si el símbolo es `'#'`, el movimiento no consume ningún carácter.

`getOrigen()`, `getSimbolo()` y `getDestino()` devuelven los componentes. Se usan para comprobar duplicados, ejecutar movimientos, calcular la cerradura epsilon e imprimir la tabla. Esta clase solo representa una transición; las validaciones las hace `EvaluadorAFN` antes de crearla.

### 5. `EvaluadorAFN.java`: almacenar y ejecutar el autómata

#### 5.1. Constante y listas internas

```java
public static final char EPSILON = '#';
```

La constante define un único símbolo para las transiciones vacías. Desde otra clase se consulta con `EvaluadorAFN.EPSILON`.

Las listas `estados`, `alfabeto` y `transiciones` almacenan los datos del quíntuplo. El inicial y los finales se representan mediante las marcas de los objetos `Estado`; por eso no se necesitan otras dos listas.

`ArrayList<Estado>` guarda estados, `ArrayList<Character>` guarda símbolos y `ArrayList<Transicion>` guarda transiciones. Se utiliza `Character` porque las listas genéricas de Java requieren tipos de objeto. Java convierte automáticamente entre `char` y `Character` al agregar o leer un símbolo.

Las operaciones utilizadas son `add` para agregar, `get` para obtener un elemento por su posición, `size` para consultar la cantidad y `clear` para vaciar. Las búsquedas se realizan con ciclos propios.

#### 5.2. `agregarEstado(int numero)`

Primero rechaza números negativos. Después llama a `buscarEstado` para comprobar que el identificador no esté repetido. Si pasa ambas condiciones, crea un `Estado`, lo agrega a la lista y devuelve `true`. Si falla alguna condición, devuelve `false` sin agregarlo.

#### 5.3. `agregarSimbolo(char simbolo)`

Las variables `letra` y `digito` comprueban si el carácter está dentro de los rangos permitidos. Luego se comprueba que no exista en el alfabeto. Si es válido y nuevo, se agrega y se devuelve `true`.

`#` no pasa los rangos de letras o dígitos, por lo que queda reservado para epsilon. Las mayúsculas y minúsculas se consideran símbolos distintos.

#### 5.4. `definirInicial(int numero)`

Busca la posición del estado solicitado. Si no existe, devuelve `false`. Si existe, recorre todos los estados y ejecuta:

```java
estados.get(i).setInicial(i == posicion);
```

La comparación es verdadera únicamente para el estado seleccionado. Ese estado queda marcado y todos los demás quedan desmarcados. Así se garantiza que no haya dos iniciales y se permite cambiar el inicial anterior.

#### 5.5. `agregarFinal(int numero)`

Busca el estado y rechaza la operación si no existe o ya es final. Cuando es válido, cambia su marca de final a `true`. A diferencia del inicial, agregar un final no desmarca los demás: el autómata puede tener varios estados de aceptación.

#### 5.6. `agregarTransicion(int origen, char simbolo, int destino)`

Realiza tres comprobaciones antes de guardar la transición:

1. Los estados de origen y destino deben existir.
2. El símbolo debe pertenecer al alfabeto o ser epsilon.
3. No debe existir otra transición con los mismos tres componentes.

Si todo es correcto, crea el objeto `Transicion` y devuelve `true`. Tener el mismo origen y símbolo con destinos diferentes sí se permite: eso representa el no determinismo.

#### 5.7. `isQuintuploValido()`

Comprueba que haya estados y símbolos. Después recorre los estados para contar iniciales y finales. Devuelve `true` cuando existe exactamente un inicial y al menos un final.

No vuelve a comprobar cada transición porque estas solo se agregan mediante el método que valida sus componentes, y las listas son privadas. Consultar este método no cambia los datos del autómata.

#### 5.8. `buscarEstado(int numero)` y `perteneceAlAlfabeto(char simbolo)`

`buscarEstado` compara el número recibido con el identificador de cada estado. Devuelve la **posición en la lista** si lo encuentra, o `-1` si no existe. Por ejemplo, si la lista contiene los estados 10, 40 y 90, buscar 40 devuelve la posición 1.

`perteneceAlAlfabeto` compara el carácter recibido con cada símbolo guardado. Devuelve `true` al encontrarlo y `false` si termina el recorrido sin encontrarlo.

Ambos métodos son privados porque sirven como operaciones internas del evaluador.

#### 5.9. `evaluarPalabra(char[] palabra)`, paso a paso

**Paso 1: comprobar el quíntuplo.** Si `isQuintuploValido()` devuelve `false`, se retorna `'e'`. Esta condición tiene prioridad sobre las demás.

**Paso 2: comprobar la entrada.** Si el arreglo es `null`, se devuelve `'p'` como decisión de esta implementación. `null` significa que no se recibió un arreglo; es distinto de un arreglo vacío. Luego se revisan todos los caracteres y se devuelve `'p'` si alguno está fuera del alfabeto.

**Paso 3: activar el inicial.** Se crea `actuales`, un arreglo de booleanos con una posición por estado. Se copia a cada posición la marca de inicial del estado correspondiente. Después se calcula la cerradura epsilon para incluir los movimientos posibles antes de leer la primera letra.

**Paso 4: consumir cada carácter.** Para cada posición de la palabra se crea un arreglo nuevo, `siguientes`, inicialmente lleno de `false`. Se recorren todas las transiciones. Si su origen está activo y su símbolo coincide con el carácter actual, se marca su destino:

```java
if (actuales[origen] && transicion.getSimbolo() == palabra[i]) {
    int destino = buscarEstado(transicion.getDestino());
    siguientes[destino] = true;
}
```

Aquí `origen` y `destino` son posiciones obtenidas con `buscarEstado`. Se pueden marcar varios destinos al leer el mismo carácter, por lo que se conservan todas las posibilidades del AFN.

**Paso 5: aplicar epsilon después del movimiento.** Se ejecuta `actuales = cerraduraEpsilon(siguientes)`. Los nuevos estados activos son los destinos alcanzados y todos los estados a los que se puede llegar desde ellos sin consumir otro carácter.

El arreglo nuevo evita mantener activos estados del paso anterior que no tuvieron una transición válida. Si ningún camino continúa, todos los valores quedan en `false`.

**Paso 6: decidir el resultado.** Después de consumir toda la palabra se busca un estado que esté activo y sea final. Si hay al menos uno, se devuelve `'s'`; si no hay ninguno, se devuelve `'n'`. Llegar a un final antes de terminar de leer la palabra no basta para aceptarla.

Con una palabra vacía, el ciclo de caracteres no se ejecuta. Se decide usando solamente la cerradura del inicial.

#### 5.10. `cerraduraEpsilon(boolean[] conjunto)`

Este método obtiene todos los estados alcanzables mediante cero o más transiciones epsilon:

1. Crea `resultado` y copia las marcas de `conjunto`. Los estados de partida también pertenecen a su propia cerradura.
2. Inicia `cambio` en `true` para realizar al menos un recorrido.
3. Al comenzar cada vuelta del `while`, pone `cambio` en `false`.
4. Recorre las transiciones y solo procesa las que tienen símbolo `EPSILON`.
5. Si el origen está marcado y el destino todavía no lo está, marca el destino y pone `cambio` en `true`.
6. Repite mientras haya agregado algún estado. Finalmente devuelve el arreglo.

Repetir el recorrido permite encontrar cadenas de epsilon aunque las transiciones estén almacenadas en otro orden. La condición `!resultado[destino]` evita considerar como nuevo un estado ya alcanzado. Como hay una cantidad finita de estados, el proceso termina incluso cuando existen ciclos.

#### 5.11. `borrarQuintuplo()`

Ejecuta `clear()` sobre las tres listas. Al quitar los objetos `Estado`, también se eliminan sus marcas de inicial y final. El mismo evaluador queda listo para recibir nuevos datos; no se necesita crear otro objeto.

#### 5.12. `imprimirTablaTransiciones()`

Primero imprime los encabezados: una columna por símbolo del alfabeto y una columna adicional para epsilon. Después recorre los estados, formando una fila para cada uno.

La variable `marca` agrega `->` si el estado es inicial y `*` si es final. Puede contener ambas marcas. Para cada columna se recorren las transiciones y se concatenan los destinos que coinciden con ese estado y símbolo. Si no hay destinos, se imprime `-`.

El ciclo de columnas usa `j <= alfabeto.size()` para incluir una vuelta adicional: esa última columna corresponde a epsilon. `\t` separa las columnas con tabulaciones. Al final se muestra si el quíntuplo está completo y válido.

### 6. `CargadorAutomata.java`: comprobar y cargar el quíntuplo

Su método `cargar(EvaluadorAFN evaluador, int[] estados, char[] alfabeto, int inicial, int[] finales, int[][] transiciones)` recibe el evaluador y los cinco componentes del quíntuplo y devuelve `true` si la carga fue correcta. Al ser una clase complementaria, puede recibir objetos compuestos según el PDF. Al evaluador le sigue enviando únicamente datos simples.

Primero llama a `borrarQuintuplo()` y utiliza los arreglos recibidos como parámetros. Comprueba que no sean `null`, que cada fila de transiciones tenga tres componentes y que el símbolo esté dentro del rango de `char`. Si falla, devuelve `false` sin imprimir.

Después recorre y carga los estados, los símbolos, el inicial, los finales y las transiciones, en ese orden. Para una fila ejecuta:

```java
int origen = transiciones[i][0];
char simbolo = (char) transiciones[i][1];
int destino = transiciones[i][2];
correcto = evaluador.agregarTransicion(origen, simbolo, destino) && correcto;
```

La conversión `(char)` recupera el carácter guardado como entero. La variable `correcto` acumula los resultados de los métodos de carga. Como cada llamada está a la izquierda de `&&`, se ejecuta aunque haya fallado una operación anterior; una vez que `correcto` es `false`, permanece así.

Finalmente comprueba `correcto` y `isQuintuploValido()`. Si se rechazó algún elemento o faltan componentes, borra los datos parciales y devuelve `false`. Si la carga completa es válida, devuelve `true`. Por tanto, una carga fallida nunca deja un autómata parcial listo para evaluar.

### 7. `ResultadoEvaluacion.java`: convertir resultados en mensajes

Su método `obtenerMensaje(char resultado)` utiliza un `switch` y devuelve un `String` para cada código: `e`, `p`, `s` y `n`. Si recibe otro carácter, devuelve «Resultado desconocido». Cada `return` termina el método, por lo que no se necesita `break`.

Esta clase no imprime. `Main` toma el texto devuelto y lo muestra con `System.out.println`. Así la interpretación del resultado queda separada de la lectura del teclado y de la ejecución del AFN.

### 8. `Main.java`: iniciar y manejar la consola

`main` define los estados, alfabeto, inicial, finales y matriz de transiciones, junto con el texto de la expresión. Después crea un cargador, un objeto de mensajes y un `EvaluadorAFN`. Llama a `cargador.cargar(evaluador, estados, alfabeto, estadoInicial, estadosFinales, transiciones)`. Si falla, muestra un mensaje y termina con `return`. Si funciona, muestra la expresión y llama al método de la clase principal que imprime la tabla.

Después crea `Scanner teclado` y ejecuta un ciclo que solo solicita palabras. `hasNextLine()` permite salir cuando se cierra la entrada. `nextLine()` obtiene la línea completa y `equals(":salir")` reconoce el comando de salida en `Main`.

Para cualquier otra entrada, `toCharArray()` obtiene el arreglo que recibe `evaluarPalabra`. El carácter devuelto se pasa a `resultados.obtenerMensaje`, y `Main` imprime el texto. Una línea vacía se evalúa como palabra vacía. Al terminar, se cierra el lector con `teclado.close()`.

La consola permanece en `Main` porque el PDF exige que las clases complementarias se comuniquen por parámetros y retornos, sin leer del teclado ni imprimir. El método de impresión de la tabla está en la clase principal del AFN, `EvaluadorAFN`, como exige el PDF.

### 9. `ejecutar.bat`: compilar y abrir el programa

`@echo off` evita mostrar cada instrucción del archivo y `setlocal` mantiene sus variables dentro de esa ejecución. `cd /d "%~dp0"` sitúa la consola en la carpeta del archivo, aunque se abra desde otra ubicación.

Las instrucciones con `if` buscan el compilador en `JAVA_HOME` y en la instalación de Adoptium. La variable `AYC_JAVA_BIN` guarda la carpeta encontrada; si queda vacía, se intenta ejecutar Java mediante el `PATH`.

El archivo crea `out` si no existe y llama a `javac` para compilar los archivos de `src`. `-encoding UTF-8` indica la codificación del código y `-d out` indica dónde guardar los archivos `.class`.

Si la compilación falla, `if errorlevel 1` muestra un mensaje y termina el archivo. Si funciona, ejecuta `java -cp out Main`: `-cp out` indica dónde encontrar las clases y `Main` indica cuál contiene el punto de entrada. `endlocal` termina el ámbito de las variables del archivo.

### 10. `pruebas/PruebasAFN.java`: comprobar el comportamiento

Su `main` crea un evaluador y realiza comprobaciones sin pedir datos del teclado. El método `comprobar(boolean condicion, String mensaje)` lanza `AssertionError` si una condición no se cumple; así una falla detiene las pruebas e identifica el caso.

Primero revisa las validaciones y construye el mismo AFN del ejemplo. Después genera todas las palabras de longitud 0 a 8 sobre `{a,b}`. Para una longitud dada hay `2^longitud` combinaciones; `1 << longitud` calcula esa cantidad. La expresión `((codigo >> i) & 1)` consulta un bit del número `codigo` y elige `a` para 0 o `b` para 1.

Cada palabra se compara con una condición independiente: que sus últimos tres caracteres sean `a`, `b` y `b`. Se espera `'s'` cuando cumple esa condición y `'n'` cuando no la cumple. La suma `1 + 2 + 4 + ... + 256` da las 511 palabras comprobadas.

Los casos restantes revisan símbolos inválidos, borrado, reutilización, epsilon, estados con números no consecutivos, varios finales, mayúsculas y dígitos. Finalmente se prueba un autómata sin transiciones cuyo inicial también es final: acepta la palabra vacía y rechaza una palabra no vacía de su alfabeto.

El mensaje de éxito se imprime solo si todas las comprobaciones anteriores terminaron correctamente. Estas pruebas conservan el AFN de `(a|b)*abb` de forma independiente; cambiar los datos de `Main` no cambia automáticamente las pruebas de ese ejemplo.

## Quíntuplo del ejemplo

Expresión configurada: **`(a|b)*abb`**, palabras sobre `{a,b}` que terminan en `abb`.

- Q = {0, 1, 2, 3, 4, 5}.
- Alfabeto = {a, b}.
- Inicial = 0.
- Finales = {5}.
- Relación de transiciones:

| Estado | a | b | # (epsilon) |
| --- | --- | --- | --- |
| ->0 | - | - | {1} |
| 1 | {1, 2} | {1} | {0} |
| 2 | - | {3} | - |
| 3 | - | {4} | - |
| 4 | - | - | {5} |
| *5 | - | - | - |

El ciclo epsilon entre 0 y 1 permite demostrar que la cerradura termina aun con ciclos. El estado 1 tiene dos destinos con `a`, por lo que el ejemplo también demuestra no determinismo.

| Palabra | Resultado |
| --- | --- |
| Cualquier palabra antes de definir el autómata | `e` |
| `abb`, `aabb`, `babb`, `abababb` | `s` |
| Vacía, `ab`, `abba` | `n` |
| `abc`, `ax` | `p` |

## Explicación para la defensa

El quíntuplo es `(Q, alfabeto, transiciones, inicial, finales)`. Epsilon representa un movimiento que no consume un carácter.

La evaluación mantiene un arreglo booleano: la posición `i` indica si el estado almacenado en esa posición está activo. La posición no tiene que coincidir con el número del estado; por eso se permiten estados como 10, 40 y 90.

Primero se valida el quíntuplo y luego cada carácter de la palabra. Así, una palabra con símbolos fuera del alfabeto devuelve `p` aunque una parte anterior ya haya perdido todos los caminos posibles.

Después se activa el estado inicial y se calcula su **cerradura epsilon**: se agregan los estados alcanzables sin consumir caracteres. Para cada carácter se recorren todas las transiciones y se activan todos los destinos posibles; después se vuelve a calcular la cerradura. Al terminar, basta con que un estado activo sea final para aceptar.

La cerradura repite el recorrido mientras aparezcan estados nuevos. Un estado ya marcado no produce otro cambio, por lo que los ciclos epsilon no causan un ciclo infinito. No se necesita elegir un único camino ni convertir el AFN a un AFD.

Para `abb`, los conjuntos activos son:

```text
Inicio y cerradura: {0,1}
Leer a:            {0,1,2}
Leer b:            {0,1,3}
Leer b:            {0,1,4,5}
```

Se acepta porque 5 es final. Para la palabra vacía no se consume ningún carácter: se verifica directamente si la cerradura inicial contiene algún final.

El PDF también exige equipo de 2 o 3 integrantes y defensa individual. El código y esta explicación sirven como apoyo para estudiar; cada integrante debe poder explicar y demostrar su participación.

## Pruebas

`pruebas/PruebasAFN.java` es un programa de comprobación independiente de la consola. Conserva las 511 palabras de longitud 0 a 8 para el ejemplo original `(a|b)*abb`. Además, `probarCarga()` carga una copia de los datos del ejemplo de `Main` y comprueba las 1093 palabras de longitud 0 a 6 sobre `{a,b,c}`: verifica el resultado de `(a|b)*abb` para palabras de `{a,b}` y el código `p` para las que incluyen `c`. Revisa filas incorrectas, símbolos fuera del rango de `char`, cargas parciales y mensajes de resultados. Si cambias la expresión configurada, ajusta también estas pruebas de la configuración.

Las pruebas del evaluador también comprueban los cuatro códigos, validaciones, duplicados, ciclos epsilon, palabra vacía, estados no consecutivos, varios finales, mayúsculas, dígitos y reutilización del objeto.

```powershell
& 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe' -encoding UTF-8 -d out src\*.java pruebas\PruebasAFN.java
& 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe' -cp out PruebasAFN
```

El resultado esperado termina con `Pruebas correctas: 511 palabras y casos de validacion.`. Las pruebas no son necesarias para ejecutar la aplicación.






