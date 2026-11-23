package negocio;

import modelo.Recorrido;
import modelo.Ubicacion;

/**
 * Interfaz para las clases que deben reaccionar cada vez que una unidad
 * registra una nueva ubicación GPS (por ejemplo, la detección de anomalías).
 */
public interface ObservadorUbicacion {

    void ubicacionRegistrada(Recorrido recorrido, Ubicacion ubicacion);
}
