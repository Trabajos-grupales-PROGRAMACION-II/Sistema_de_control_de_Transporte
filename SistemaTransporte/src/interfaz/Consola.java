package interfaz;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lectura y validación de datos ingresados por teclado.
 */
public final class Consola {

    private static final Scanner ENTRADA = new Scanner(System.in);

    private Consola() {
    }

    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!ENTRADA.hasNextLine()) {
            return "0";
        }
        return ENTRADA.nextLine().trim();
    }

    public static String leerTextoObligatorio(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este campo es obligatorio.");
        }
    }

    public static int leerEntero(String mensaje) {
        while (true) {
            String linea = leerTexto(mensaje);
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    public static double leerDecimal(String mensaje) {
        while (true) {
            String linea = leerTexto(mensaje).replace(',', '.');
            try {
                return Double.parseDouble(linea);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido (use punto para los decimales).");
            }
        }
    }

    public static LocalTime leerHora(String mensaje) {
        while (true) {
            String linea = leerTexto(mensaje);
            try {
                return LocalTime.parse(linea);
            } catch (DateTimeParseException e) {
                System.out.println("Ingrese la hora con el formato HH:mm, por ejemplo 06:30.");
            }
        }
    }

    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            String linea = leerTexto(mensaje);
            try {
                return LocalDate.parse(linea);
            } catch (DateTimeParseException e) {
                System.out.println("Ingrese la fecha con el formato AAAA-MM-DD, por ejemplo 2027-05-30.");
            }
        }
    }

    public static boolean confirmar(String mensaje) {
        String respuesta = leerTexto(mensaje + " (s/n): ");
        return respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si") || respuesta.equalsIgnoreCase("sí");
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  " + texto);
        System.out.println("==================================================");
    }

    public static void error(RuntimeException e) {
        System.out.println(">> Error: " + e.getMessage());
    }

    public static void exito(String mensaje) {
        System.out.println(">> " + mensaje);
    }
}
