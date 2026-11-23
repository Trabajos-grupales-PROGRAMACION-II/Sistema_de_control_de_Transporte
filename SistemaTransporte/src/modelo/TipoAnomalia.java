package modelo;

/**
 * Tipos de anomalía que el sistema detecta durante un recorrido.
 */
public enum TipoAnomalia {
    DESVIO_RUTA("Desvío de ruta"),
    DETENCION_INUSUAL("Detención inusual");

    private final String descripcion;

    TipoAnomalia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
