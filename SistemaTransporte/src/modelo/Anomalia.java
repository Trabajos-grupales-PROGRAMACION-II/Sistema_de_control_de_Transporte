package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Evento irregular detectado en un recorrido (desvío o detención),
 * que el administrador debe revisar y marcar como atendido.
 */
public class Anomalia {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private int id;
    private TipoAnomalia tipo;
    private Recorrido recorrido;
    private LocalDateTime fechaHora;
    private double latitud;
    private double longitud;
    private String detalle;
    private boolean atendida;

    public Anomalia(int id, TipoAnomalia tipo, Recorrido recorrido, Ubicacion ubicacion, String detalle) {
        this.id = id;
        this.tipo = tipo;
        this.recorrido = recorrido;
        this.fechaHora = ubicacion.getFechaHora();
        this.latitud = ubicacion.getLatitud();
        this.longitud = ubicacion.getLongitud();
        this.detalle = detalle;
        this.atendida = false;
    }

    public void atender() {
        this.atendida = true;
    }

    public int getId() {
        return id;
    }

    public TipoAnomalia getTipo() {
        return tipo;
    }

    public Recorrido getRecorrido() {
        return recorrido;
    }

    public Unidad getUnidad() {
        return recorrido.getUnidad();
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getDetalle() {
        return detalle;
    }

    public boolean isAtendida() {
        return atendida;
    }

    @Override
    public String toString() {
        return "#" + id + " [" + fechaHora.format(FORMATO_HORA) + "] " + tipo.getDescripcion()
                + " - Unidad " + getUnidad().getPlaca() + ": " + detalle
                + (atendida ? " (atendida)" : " (PENDIENTE)");
    }
}
