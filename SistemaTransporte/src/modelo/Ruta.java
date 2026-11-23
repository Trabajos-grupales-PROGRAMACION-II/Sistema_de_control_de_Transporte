package modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Recorrido definido con un horario de operación y una secuencia de paradas.
 * Composición: las paradas solo existen como parte de su ruta.
 */
public class Ruta {

    private String codigo;
    private String nombre;
    private String origen;
    private String destino;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<Parada> paradas;

    public Ruta(String codigo, String nombre, String origen, String destino,
                LocalTime horaInicio, LocalTime horaFin) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.paradas = new ArrayList<>();
    }

    /**
     * Agrega una parada validando que su orden no se repita en la ruta.
     */
    public void agregarParada(Parada parada) {
        for (Parada p : paradas) {
            if (p.getOrden() == parada.getOrden()) {
                throw new IllegalArgumentException("El orden de las paradas no puede repetirse.");
            }
        }
        paradas.add(parada);
        paradas.sort(Comparator.comparingInt(Parada::getOrden));
    }

    public Parada buscarParada(int orden) {
        for (Parada p : paradas) {
            if (p.getOrden() == orden) {
                return p;
            }
        }
        return null;
    }

    /** Una ruta es válida para operar si tiene al menos dos paradas. */
    public boolean esValida() {
        return paradas.size() >= 2;
    }

    public List<Parada> getParadas() {
        return Collections.unmodifiableList(paradas);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + origen + " → " + destino + ")";
    }
}
