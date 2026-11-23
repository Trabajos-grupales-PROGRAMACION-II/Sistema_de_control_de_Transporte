package negocio;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.Anomalia;
import modelo.Parada;
import modelo.Recorrido;
import modelo.TipoAnomalia;
import modelo.Ubicacion;
import modelo.Unidad;

/**
 * Reglas de negocio del módulo 4, Anomalías y alertas:
 *  - RF9  Detección de desvío de ruta (más de 100 m del trazado)
 *  - RF10 Detección de detención inusual (5 min sin moverse fuera de una parada)
 *  - RF11 Reporte de operación y cumplimiento por unidad
 * Analiza cada ubicación que registra el monitoreo (observador).
 */
public class GestorAnomalias implements ObservadorUbicacion {

    public static final double TOLERANCIA_DESVIO_METROS = 100;
    public static final int MINUTOS_DETENCION = 5;
    public static final double RADIO_DETENCION_METROS = 30;
    public static final double RADIO_PARADA_METROS = 50;

    private GestorMonitoreo monitoreo;
    private List<Anomalia> anomalias;
    private List<Anomalia> alertasNuevas;
    private int contador;

    public GestorAnomalias(GestorMonitoreo monitoreo) {
        this.monitoreo = monitoreo;
        this.anomalias = new ArrayList<>();
        this.alertasNuevas = new ArrayList<>();
    }

    @Override
    public void ubicacionRegistrada(Recorrido recorrido, Ubicacion ubicacion) {
        analizarDesvio(recorrido, ubicacion);
        analizarDetencion(recorrido, ubicacion);
    }

    // ------------------------------------------------------------------
    // RF9 Desvío de ruta
    // ------------------------------------------------------------------

    private void analizarDesvio(Recorrido recorrido, Ubicacion ubicacion) {
        double distancia = CalculadoraGeografica.distanciaAlTrazado(
                recorrido.getRuta().getParadas(), ubicacion.getLatitud(), ubicacion.getLongitud());
        if (distancia > TOLERANCIA_DESVIO_METROS) {
            if (!recorrido.isEnDesvio()) {
                recorrido.setEnDesvio(true);
                registrar(TipoAnomalia.DESVIO_RUTA, recorrido, ubicacion,
                        String.format("se alejó %.0f m del trazado de la ruta %s (tolerancia %.0f m).",
                                distancia, recorrido.getRuta().getCodigo(), TOLERANCIA_DESVIO_METROS));
            }
        } else {
            recorrido.setEnDesvio(false);
        }
    }

    // ------------------------------------------------------------------
    // RF10 Detención inusual
    // ------------------------------------------------------------------

    private void analizarDetencion(Recorrido recorrido, Ubicacion actual) {
        List<Ubicacion> lista = recorrido.getUbicaciones();
        Ubicacion inicioQuieto = actual;
        for (int i = lista.size() - 2; i >= 0; i--) {
            Ubicacion u = lista.get(i);
            double d = CalculadoraGeografica.distanciaMetros(u.getLatitud(), u.getLongitud(),
                    actual.getLatitud(), actual.getLongitud());
            if (d > RADIO_DETENCION_METROS) {
                break;
            }
            inicioQuieto = u;
        }
        long minutosQuieto = Duration.between(inicioQuieto.getFechaHora(), actual.getFechaHora()).toMinutes();

        if (minutosQuieto < MINUTOS_DETENCION) {
            if (inicioQuieto == actual) {
                recorrido.setDetencionReportada(false);
            }
            return;
        }
        if (!recorrido.isDetencionReportada() && !estaEnParada(recorrido, actual)) {
            recorrido.setDetencionReportada(true);
            registrar(TipoAnomalia.DETENCION_INUSUAL, recorrido, actual,
                    "permanece detenida " + minutosQuieto + " min fuera de una parada.");
        }
    }

    private boolean estaEnParada(Recorrido recorrido, Ubicacion ubicacion) {
        for (Parada p : recorrido.getRuta().getParadas()) {
            double d = CalculadoraGeografica.distanciaMetros(p.getLatitud(), p.getLongitud(),
                    ubicacion.getLatitud(), ubicacion.getLongitud());
            if (d <= RADIO_PARADA_METROS) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Gestión de alertas
    // ------------------------------------------------------------------

    /** Alertas detectadas desde la última consulta; se vacía al leerlas. */
    public List<Anomalia> obtenerAlertasNuevas() {
        List<Anomalia> copia = new ArrayList<>(alertasNuevas);
        alertasNuevas.clear();
        return copia;
    }

    public List<Anomalia> listarAnomalias(boolean soloPendientes) {
        if (!soloPendientes) {
            return Collections.unmodifiableList(anomalias);
        }
        List<Anomalia> pendientes = new ArrayList<>();
        for (Anomalia a : anomalias) {
            if (!a.isAtendida()) {
                pendientes.add(a);
            }
        }
        return pendientes;
    }

    public void atenderAnomalia(int id) {
        for (Anomalia a : anomalias) {
            if (a.getId() == id) {
                if (a.isAtendida()) {
                    throw new ValidacionException("La alerta #" + id + " ya fue atendida.");
                }
                a.atender();
                return;
            }
        }
        throw new ValidacionException("No existe la alerta #" + id + ".");
    }

    // ------------------------------------------------------------------
    // RF11 Reporte de operación
    // ------------------------------------------------------------------

    public List<ReporteUnidad> generarReportePorUnidad() {
        List<ReporteUnidad> reporte = new ArrayList<>();
        for (Unidad unidad : monitoreo.obtenerEstadoFlota()) {
            int finalizados = 0;
            double metros = 0;
            double minutos = 0;
            for (Recorrido r : monitoreo.getRecorridos()) {
                if (r.getUnidad() != unidad) {
                    continue;
                }
                metros += r.getDistanciaRecorridaMetros();
                if (!r.estaEnCurso()) {
                    finalizados++;
                    minutos += r.getDuracionMinutos();
                }
            }
            int desvios = 0;
            int detenciones = 0;
            for (Anomalia a : anomalias) {
                if (a.getUnidad() == unidad) {
                    if (a.getTipo() == TipoAnomalia.DESVIO_RUTA) {
                        desvios++;
                    } else {
                        detenciones++;
                    }
                }
            }
            double promedio = finalizados == 0 ? 0 : minutos / finalizados;
            reporte.add(new ReporteUnidad(unidad, finalizados, metros / 1000, promedio, desvios, detenciones));
        }
        return reporte;
    }

    private void registrar(TipoAnomalia tipo, Recorrido recorrido, Ubicacion ubicacion, String detalle) {
        contador++;
        Anomalia anomalia = new Anomalia(contador, tipo, recorrido, ubicacion, detalle);
        anomalias.add(anomalia);
        alertasNuevas.add(anomalia);
    }
}
