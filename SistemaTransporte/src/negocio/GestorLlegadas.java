package negocio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Parada;
import modelo.Recorrido;
import modelo.Ruta;
import modelo.Ubicacion;
import modelo.Unidad;

/**
 * Reglas de negocio del módulo 3 para la información de llegada:
 *  - RF8 Tiempo estimado de llegada de una unidad a una parada
 * El cálculo usa la distancia restante sobre la ruta y la velocidad
 * promedio de los últimos 2 minutos; si la unidad está detenida o no hay
 * datos suficientes, se usa una velocidad urbana de referencia de 20 km/h.
 */
public class GestorLlegadas {

    public static final double VELOCIDAD_REFERENCIA_KMH = 20;
    public static final double VELOCIDAD_MINIMA_KMH = 5;
    public static final int VENTANA_VELOCIDAD_SEGUNDOS = 120;

    private GestorMonitoreo monitoreo;
    private GestorRutasUnidades gestorRutas;

    public GestorLlegadas(GestorMonitoreo monitoreo, GestorRutasUnidades gestorRutas) {
        this.monitoreo = monitoreo;
        this.gestorRutas = gestorRutas;
    }

    public EstimacionLlegada calcularTiempoEstimadoLlegada(String placa, int ordenParada) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = monitoreo.obtenerRecorridoEnCurso(unidad);
        Parada parada = obtenerParada(unidad.getRuta(), ordenParada);
        double restante = distanciaRestante(recorrido, parada);
        if (restante <= 0) {
            throw new ValidacionException("La unidad ya pasó por la parada " + parada.getNombre() + ".");
        }
        return estimar(unidad, recorrido, parada, restante);
    }

    /** Tiempo estimado a cada parada que la unidad todavía no ha pasado. */
    public List<EstimacionLlegada> calcularProximasLlegadas(String placa) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = monitoreo.obtenerRecorridoEnCurso(unidad);
        List<EstimacionLlegada> resultado = new ArrayList<>();
        for (Parada parada : unidad.getRuta().getParadas()) {
            double restante = distanciaRestante(recorrido, parada);
            if (restante > 0) {
                resultado.add(estimar(unidad, recorrido, parada, restante));
            }
        }
        return resultado;
    }

    /**
     * Entre las unidades en recorrido sobre la ruta, la que llegará primero
     * a la parada indicada; null si ninguna se dirige a ella.
     */
    public EstimacionLlegada buscarUnidadMasProxima(String codigoRuta, int ordenParada) {
        Ruta ruta = gestorRutas.buscarRuta(codigoRuta);
        Parada parada = obtenerParada(ruta, ordenParada);
        EstimacionLlegada mejor = null;
        for (Unidad unidad : monitoreo.obtenerEstadoFlota()) {
            Recorrido recorrido = monitoreo.buscarRecorridoEnCurso(unidad);
            if (recorrido == null || unidad.getRuta() != ruta) {
                continue;
            }
            double restante = distanciaRestante(recorrido, parada);
            if (restante <= 0) {
                continue;
            }
            EstimacionLlegada e = estimar(unidad, recorrido, parada, restante);
            if (mejor == null || e.getMinutos() < mejor.getMinutos()) {
                mejor = e;
            }
        }
        return mejor;
    }

    private Parada obtenerParada(Ruta ruta, int orden) {
        Parada parada = ruta.buscarParada(orden);
        if (parada == null) {
            throw new ValidacionException("La ruta no tiene una parada con orden " + orden + ".");
        }
        return parada;
    }

    private double distanciaRestante(Recorrido recorrido, Parada parada) {
        List<Parada> paradas = recorrido.getRuta().getParadas();
        double hastaParada = CalculadoraGeografica.distanciaHastaParada(paradas, paradas.indexOf(parada));
        return hastaParada - recorrido.getDistanciaRecorridaMetros();
    }

    private EstimacionLlegada estimar(Unidad unidad, Recorrido recorrido, Parada parada, double restante) {
        double velocidad = calcularVelocidadPromedio(recorrido);
        boolean deReferencia = velocidad < VELOCIDAD_MINIMA_KMH;
        if (deReferencia) {
            velocidad = VELOCIDAD_REFERENCIA_KMH;
        }
        int minutos = (int) Math.ceil(restante / (velocidad / 3.6) / 60);
        return new EstimacionLlegada(unidad, parada, restante, velocidad, deReferencia, minutos);
    }

    /** Velocidad promedio (km/h) con las ubicaciones de los últimos 2 minutos. */
    private double calcularVelocidadPromedio(Recorrido recorrido) {
        List<Ubicacion> lista = recorrido.getUbicaciones();
        if (lista.size() < 2) {
            return 0;
        }
        Ubicacion ultima = lista.get(lista.size() - 1);
        LocalDateTime limite = ultima.getFechaHora().minusSeconds(VENTANA_VELOCIDAD_SEGUNDOS);
        double metros = 0;
        Ubicacion primera = ultima;
        for (int i = lista.size() - 1; i > 0; i--) {
            Ubicacion actual = lista.get(i);
            Ubicacion anterior = lista.get(i - 1);
            if (anterior.getFechaHora().isBefore(limite)) {
                break;
            }
            metros += CalculadoraGeografica.distanciaMetros(anterior.getLatitud(), anterior.getLongitud(),
                    actual.getLatitud(), actual.getLongitud());
            primera = anterior;
        }
        long segundos = Duration.between(primera.getFechaHora(), ultima.getFechaHora()).getSeconds();
        return segundos == 0 ? 0 : metros / segundos * 3.6;
    }
}
