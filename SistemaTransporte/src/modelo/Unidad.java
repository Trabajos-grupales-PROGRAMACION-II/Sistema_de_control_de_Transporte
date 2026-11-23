package modelo;

import java.time.LocalDateTime;

/**
 * Vehículo de la flota. Se asocia con un conductor y con la ruta que cubre.
 */
public class Unidad {

    private String placa;
    private int numeroUnidad;
    private int capacidad;
    private Conductor conductor;
    private Ruta ruta;
    private EstadoUnidad estado;
    private Ubicacion ultimaUbicacion;
    private LocalDateTime ultimaSenal;

    public Unidad(String placa, int numeroUnidad, int capacidad) {
        if (placa == null || !placa.trim().toUpperCase().matches("^[A-Z]{3}-\\d{3,4}$")) {
            throw new IllegalArgumentException("La placa debe tener el formato ABC-1234.");
        }
        if (numeroUnidad <= 0) {
            throw new IllegalArgumentException("El número de unidad debe ser mayor que cero.");
        }
        if (capacidad <= 0 || capacidad > 120) {
            throw new IllegalArgumentException("La capacidad debe estar entre 1 y 120 pasajeros.");
        }
        this.placa = placa.trim().toUpperCase();
        this.numeroUnidad = numeroUnidad;
        this.capacidad = capacidad;
        this.estado = EstadoUnidad.DISPONIBLE;
    }

    /**
     * Registra una nueva posición GPS y renueva la señal de la unidad.
     */
    public void actualizarUbicacion(Ubicacion ubicacion) {
        this.ultimaUbicacion = ubicacion;
        this.ultimaSenal = ubicacion.getFechaHora();
        this.estado = EstadoUnidad.EN_RECORRIDO;
    }

    public boolean estaEnOperacion() {
        return estado == EstadoUnidad.EN_RECORRIDO || estado == EstadoUnidad.SIN_SENAL;
    }

    public String getPlaca() {
        return placa;
    }

    public int getNumeroUnidad() {
        return numeroUnidad;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public EstadoUnidad getEstado() {
        return estado;
    }

    public void setEstado(EstadoUnidad estado) {
        this.estado = estado;
    }

    public Ubicacion getUltimaUbicacion() {
        return ultimaUbicacion;
    }

    public LocalDateTime getUltimaSenal() {
        return ultimaSenal;
    }

    @Override
    public String toString() {
        return placa + " (unidad " + numeroUnidad + ")";
    }
}
