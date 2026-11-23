package modelo;

/**
 * Estados operativos posibles de una unidad de transporte.
 */
public enum EstadoUnidad {
    DISPONIBLE("Disponible"),
    EN_RECORRIDO("En recorrido"),
    SIN_SENAL("Sin señal"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    private final String descripcion;

    EstadoUnidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
