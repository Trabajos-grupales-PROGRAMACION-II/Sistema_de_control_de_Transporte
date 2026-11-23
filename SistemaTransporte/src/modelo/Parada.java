package modelo;

import java.util.Locale;

/**
 * Punto de una ruta donde la unidad recoge o deja pasajeros.
 * El orden indica su posición dentro de la ruta (1, 2, 3, ...).
 */
public class Parada {

    private String nombre;
    private int orden;
    private double latitud;
    private double longitud;

    public Parada(String nombre, int orden, double latitud, double longitud) {
        if (orden <= 0) {
            throw new IllegalArgumentException("El orden de la parada debe ser mayor que cero.");
        }
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Las coordenadas de la parada no son válidas.");
        }
        this.nombre = nombre;
        this.orden = orden;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOrden() {
        return orden;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%d. %s (%.4f, %.4f)", orden, nombre, latitud, longitud);
    }
}
