package vista;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import controlador.Pantalla;

public class Consola implements Pantalla {
    private static final String RESET = "\u001B[0m";
    private static final String NEGRITA = "\u001B[1m";
    private static final String ROJO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String CIAN = "\u001B[36m";
    private static final String GRIS = "\u001B[90m";

    private static final int ANCHO = 64;   // ancho de los títulos

    private final Scanner teclado;
    private final boolean colores;

    public Consola(Scanner teclado, boolean colores) {
        if (teclado == null) {
            throw new IllegalArgumentException("El teclado es obligatorio.");
        }
        this.teclado = teclado;
        this.colores = colores;
    }

    private String pintar(String texto, String color) {
        if (colores) {
            return color + texto + RESET;
        }
        return texto;
    }

    public void titulo(String texto) {
        System.out.println();
        System.out.println(pintar("=".repeat(ANCHO), CIAN));
        System.out.println(pintar(centrar(texto), NEGRITA + CIAN));
        System.out.println(pintar("=".repeat(ANCHO), CIAN));
    }

    public void subtitulo(String texto) {
        System.out.println();
        System.out.println(pintar(texto.toUpperCase(), NEGRITA + CIAN));
        System.out.println(pintar("-".repeat(texto.length()), CIAN));
    }

    public void texto(String texto) {
        System.out.println(texto);
    }

    public void linea() {
        System.out.println();
    }

    // lo que el controlador le ordena mostrar (así la consola cumple la interfaz Pantalla)
    @Override
    public void mostrarMensaje(String mensaje) {
        exito(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        error(mensaje);
    }

    @Override
    public void mostrarAviso(String mensaje) {
        aviso(mensaje);
    }

    public void exito(String texto) {
        System.out.println(pintar("  [OK] " + texto, VERDE));
    }

    public void error(String texto) {
        System.out.println(pintar("  [ERROR] " + texto, ROJO));
    }

    public void aviso(String texto) {
        System.out.println(pintar("  [!] " + texto, AMARILLO));
    }

    public void info(String texto) {
        System.out.println(pintar("  " + texto, GRIS));
    }

    private String centrar(String texto) {
        int espacios = (ANCHO - texto.length()) / 2;
        if (espacios < 0) {
            espacios = 0;
        }
        return " ".repeat(espacios) + texto;
    }

    // muestra una tabla con las columnas alineadas; cada fila es un arreglo con un texto por columna
    public void tabla(String[] encabezados, List<String[]> filas) {
        // 1. el ancho de cada columna es el del texto más largo que tenga
        int[] anchos = new int[encabezados.length];
        for (int i = 0; i < encabezados.length; i++) {
            anchos[i] = encabezados[i].length();
        }
        for (String[] fila : filas) {
            for (int i = 0; i < fila.length; i++) {
                if (fila[i].length() > anchos[i]) {
                    anchos[i] = fila[i].length();
                }
            }
        }

        // 2. se muestra el encabezado, una raya y las filas
        System.out.println(pintar(armarFila(encabezados, anchos), NEGRITA + CIAN));
        String raya = "";
        for (int i = 0; i < anchos.length; i++) {
            raya += "-".repeat(anchos[i] + 2);
            if (i < anchos.length - 1) {
                raya += "+";
            }
        }
        System.out.println(pintar(raya, CIAN));
        for (String[] fila : filas) {
            System.out.println(armarFila(fila, anchos));
        }
    }

    // une las celdas de una fila separándolas con " | " y dejándolas del ancho de su columna
    private String armarFila(String[] celdas, int[] anchos) {
        String fila = "";
        for (int i = 0; i < celdas.length; i++) {
            fila += " " + String.format("%-" + anchos[i] + "s", celdas[i]) + " ";   // %-10s alinea a la izquierda
            if (i < celdas.length - 1) {
                fila += "|";
            }
        }
        return fila;
    }

    // muestra las opciones numeradas desde 1 y la 0 para salir; devuelve el número que eligió el usuario
    public int menu(String titulo, String[] opciones, String textoSalida) {
        subtitulo(titulo);
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + pintar(String.valueOf(i + 1), AMARILLO) + ") " + opciones[i]);
        }
        System.out.println("  " + pintar("0", AMARILLO) + ") " + textoSalida);
        return leerEnteroEnRango("Opción", 0, opciones.length);
    }

    // muestra los elementos de una lista numerados desde 1 y devuelve su posición en la lista (que empieza en 0)
    public int elegirPosicion(String pregunta, List<?> opciones) {
        if (opciones.isEmpty()) {
            throw new IllegalArgumentException("No hay opciones para elegir.");
        }
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("  " + pintar(String.valueOf(i + 1), AMARILLO) + ") " + opciones.get(i));
        }
        return leerEnteroEnRango(pregunta, 1, opciones.size()) - 1;
    }

    public boolean confirmar(String pregunta) {
        while (true) {
            String respuesta = leerLinea(pregunta + " (s/n)");
            if (respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si")) {
                return true;
            }
            if (respuesta.equalsIgnoreCase("n") || respuesta.equalsIgnoreCase("no")) {
                return false;
            }
            error("Respondé s o n.");
        }
    }

    public void pausa() {
        leerLinea("Presioná ENTER para continuar");
    }

    private String leerLinea(String pregunta) {
        System.out.print(pintar(pregunta + ": ", NEGRITA));
        return teclado.nextLine().strip();
    }

    public String leerTexto(String pregunta) {
        while (true) {
            String texto = leerLinea(pregunta);
            if (!texto.isEmpty()) {
                return texto;
            }
            error("Este dato es obligatorio.");
        }
    }

    public String leerTextoOpcional(String pregunta) {
        return leerLinea(pregunta + " (opcional)");
    }

    public int leerEntero(String pregunta) {
        while (true) {
            try {
                return Integer.parseInt(leerLinea(pregunta));
            } catch (NumberFormatException e) {
                error("Escribí un número entero.");
            }
        }
    }

    private int leerEnteroEnRango(String pregunta, int minimo, int maximo) {
        while (true) {
            int numero = leerEntero(pregunta);
            if (numero >= minimo && numero <= maximo) {
                return numero;
            }
            error("Elegí un número entre " + minimo + " y " + maximo + ".");
        }
    }

    public double leerDecimal(String pregunta) {
        while (true) {
            try {
                double numero = Double.parseDouble(leerLinea(pregunta).replace(',', '.'));
                if (Double.isFinite(numero)) {   // descarta cosas como NaN o Infinity
                    return numero;
                }
                error("Escribí un número válido.");
            } catch (NumberFormatException e) {
                error("Escribí un número, por ejemplo 1500 o 1500,50.");
            }
        }
    }

    // se escribe día/mes/año
    public LocalDate leerFecha(String pregunta) {
        while (true) {
            String[] partes = leerLinea(pregunta + " (dd/mm/aaaa)").split("/");
            try {
                if (partes.length == 3) {
                    int dia = Integer.parseInt(partes[0].strip());
                    int mes = Integer.parseInt(partes[1].strip());
                    int anio = Integer.parseInt(partes[2].strip());
                    return LocalDate.of(anio, mes, dia);
                }
            } catch (NumberFormatException | DateTimeException e) {
                // cae al mensaje de error de abajo
            }
            error("Fecha inválida. Escribila así: 25/12/2026.");
        }
    }

    // se escribe hora:minutos
    public LocalTime leerHora(String pregunta) {
        while (true) {
            String[] partes = leerLinea(pregunta + " (hh:mm)").split(":");
            try {
                if (partes.length == 2) {
                    int hora = Integer.parseInt(partes[0].strip());
                    int minutos = Integer.parseInt(partes[1].strip());
                    return LocalTime.of(hora, minutos);
                }
            } catch (NumberFormatException | DateTimeException e) {
                // cae al mensaje de error de abajo
            }
            error("Hora inválida. Escribila así: 09:30.");
        }
    }

    public String formatear(LocalDate fecha) {
        return String.format("%02d/%02d/%d", fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    public String formatear(LocalTime hora) {
        return String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

    public String formatearDinero(double importe) {
        if (importe < 0) {
            return "-" + String.format("$%,.2f", -importe);
        }
        return String.format("$%,.2f", importe);
    }
}