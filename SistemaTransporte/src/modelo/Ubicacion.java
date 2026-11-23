package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Posición GPS enviada por la unidad en un instante determinado.
 */
public class Ubicacion {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private double latitud;
    private double longitud;
    private LocalDateTime fechaHora;

    public Ubicacion(double latitud, double longitud, LocalDateTime fechaHora) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Las coordenadas GPS no son válidas.");
        }
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaHora = fechaHora;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s  (%.5f, %.5f)", fechaHora.format(FORMATO_HORA), latitud, longitud);
    }
}
