package negocio;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.Conductor;
import modelo.EstadoUnidad;
import modelo.Parada;
import modelo.Recorrido;
import modelo.Ubicacion;
import modelo.Unidad;

/**
 * Reglas de negocio del módulo 3, Monitoreo y llegada:
 *  - RF6 Inicio y fin de recorridos, envío de ubicación cada 5 s y
 *        estado "sin señal" tras 30 s sin ubicación
 *  - RF7 Estado de la flota en tiempo real
 * Cada ubicación registrada se notifica a los observadores (módulo 4).
 */
public class GestorMonitoreo {

    public static final int INTERVALO_GPS_SEGUNDOS = 5;
    public static final int SEGUNDOS_SIN_SENAL = 30;
    public static final double VELOCIDAD_SIMULADA_KMH = 30;

    private GestorRutasUnidades gestorRutas;
    private GestorUsuarios gestorUsuarios;
    private RelojSimulado reloj;
    private List<Recorrido> recorridos;
    private List<ObservadorUbicacion> observadores;
    private int contadorRecorridos;

    public GestorMonitoreo(GestorRutasUnidades gestorRutas, GestorUsuarios gestorUsuarios, RelojSimulado reloj) {
        this.gestorRutas = gestorRutas;
        this.gestorUsuarios = gestorUsuarios;
        this.reloj = reloj;
        this.recorridos = new ArrayList<>();
        this.observadores = new ArrayList<>();
    }

    public void agregarObservador(ObservadorUbicacion observador) {
        observadores.add(observador);
    }

    // ------------------------------------------------------------------
    // RF6 Recorridos y envío de ubicación
    // ------------------------------------------------------------------

    /**
     * Inicia el recorrido de la unidad y registra la primera ubicación en la parada inicial.
     */
    public Recorrido iniciarRecorrido(String placa) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Conductor conductor = unidad.getConductor();
        if (conductor == null || unidad.getRuta() == null) {
            throw new ValidacionException("La unidad no tiene conductor y ruta asignados.");
        }
        if (unidad.getEstado() == EstadoUnidad.FUERA_DE_SERVICIO) {
            throw new ValidacionException("La unidad está fuera de servicio.");
        }
        if (buscarRecorridoEnCurso(unidad) != null) {
            throw new ValidacionException("La unidad ya tiene un recorrido en curso.");
        }
        if (!conductor.isActivo()) {
            throw new ValidacionException("El conductor asignado está desactivado.");
        }
        gestorUsuarios.verificarJornada(conductor, recorridos);

        contadorRecorridos++;
        Recorrido recorrido = new Recorrido(contadorRecorridos, unidad, reloj.ahora());
        recorridos.add(recorrido);
        Parada primera = unidad.getRuta().getParadas().get(0);
        registrarUbicacion(recorrido, primera.getLatitud(), primera.getLongitud());
        return recorrido;
    }

    public Recorrido finalizarRecorrido(String placa) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = obtenerRecorridoEnCurso(unidad);
        recorrido.finalizar(reloj.ahora());
        unidad.setEstado(EstadoUnidad.DISPONIBLE);
        return recorrido;
    }

    /**
     * Simula el envío de ubicaciones cada 5 segundos durante los minutos
     * indicados, con la unidad avanzando sobre su ruta a 30 km/h.
     * Devuelve la cantidad de ubicaciones enviadas.
     */
    public int simularAvance(String placa, int minutos) {
        validarMinutos(minutos);
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = obtenerRecorridoEnCurso(unidad);
        List<Parada> paradas = unidad.getRuta().getParadas();
        double total = CalculadoraGeografica.longitudTotal(paradas);
        double metrosPorEnvio = VELOCIDAD_SIMULADA_KMH / 3.6 * INTERVALO_GPS_SEGUNDOS;

        int enviados = 0;
        int envios = minutos * 60 / INTERVALO_GPS_SEGUNDOS;
        for (int i = 0; i < envios && recorrido.getDistanciaRecorridaMetros() < total; i++) {
            reloj.avanzarSegundos(INTERVALO_GPS_SEGUNDOS);
            recorrido.sumarDistancia(Math.min(metrosPorEnvio, total - recorrido.getDistanciaRecorridaMetros()));
            double[] punto = CalculadoraGeografica.puntoSobreTrazado(paradas, recorrido.getDistanciaRecorridaMetros());
            registrarUbicacion(recorrido, punto[0], punto[1]);
            enviados++;
        }
        return enviados;
    }

    /**
     * Simula que la unidad permanece detenida en el mismo lugar, enviando
     * su ubicación cada 5 segundos.
     */
    public int simularDetencion(String placa, int minutos) {
        validarMinutos(minutos);
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = obtenerRecorridoEnCurso(unidad);
        Ubicacion actual = recorrido.getUltimaUbicacion();
        int envios = minutos * 60 / INTERVALO_GPS_SEGUNDOS;
        for (int i = 0; i < envios; i++) {
            reloj.avanzarSegundos(INTERVALO_GPS_SEGUNDOS);
            registrarUbicacion(recorrido, actual.getLatitud(), actual.getLongitud());
        }
        return envios;
    }

    /**
     * Simula que la unidad sale de su ruta: envía tres ubicaciones
     * desplazadas los metros indicados respecto al trazado.
     */
    public void simularDesvio(String placa, int metros) {
        if (metros <= 0) {
            throw new ValidacionException("Los metros de desvío deben ser mayores que cero.");
        }
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = obtenerRecorridoEnCurso(unidad);
        double[] base = CalculadoraGeografica.puntoSobreTrazado(
                unidad.getRuta().getParadas(), recorrido.getDistanciaRecorridaMetros());
        for (int i = 0; i < 3; i++) {
            reloj.avanzarSegundos(INTERVALO_GPS_SEGUNDOS);
            double[] punto = CalculadoraGeografica.desplazar(base[0], base[1], metros, 0);
            registrarUbicacion(recorrido, punto[0], punto[1]);
        }
    }

    /**
     * Simula un periodo sin recibir ubicaciones (el teléfono pierde conexión).
     */
    public void simularPerdidaDeSenal(int segundos) {
        if (segundos <= 0) {
            throw new ValidacionException("Los segundos deben ser mayores que cero.");
        }
        reloj.avanzarSegundos(segundos);
        actualizarEstados();
    }

    public boolean llegoAlDestino(String placa) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        Recorrido recorrido = buscarRecorridoEnCurso(unidad);
        return recorrido != null && recorrido.getDistanciaRecorridaMetros()
                >= CalculadoraGeografica.longitudTotal(unidad.getRuta().getParadas());
    }

    // ------------------------------------------------------------------
    // RF7 Estado de la flota
    // ------------------------------------------------------------------

    /**
     * Marca como "sin señal" las unidades en recorrido cuya última
     * ubicación tiene más de 30 segundos.
     */
    public void actualizarEstados() {
        for (Unidad u : gestorRutas.listarUnidades()) {
            if (u.getEstado() == EstadoUnidad.EN_RECORRIDO && segundosDesdeUltimaSenal(u) > SEGUNDOS_SIN_SENAL) {
                u.setEstado(EstadoUnidad.SIN_SENAL);
            }
        }
    }

    public List<Unidad> obtenerEstadoFlota() {
        actualizarEstados();
        return gestorRutas.listarUnidades();
    }

    public long segundosDesdeUltimaSenal(Unidad unidad) {
        if (unidad.getUltimaSenal() == null) {
            return -1;
        }
        return Duration.between(unidad.getUltimaSenal(), reloj.ahora()).getSeconds();
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public Recorrido buscarRecorridoEnCurso(Unidad unidad) {
        for (Recorrido r : recorridos) {
            if (r.getUnidad() == unidad && r.estaEnCurso()) {
                return r;
            }
        }
        return null;
    }

    public Recorrido obtenerRecorridoEnCurso(Unidad unidad) {
        Recorrido recorrido = buscarRecorridoEnCurso(unidad);
        if (recorrido == null) {
            throw new ValidacionException("La unidad " + unidad.getPlaca() + " no tiene un recorrido en curso.");
        }
        return recorrido;
    }

    public List<Recorrido> obtenerHistorial(String placa) {
        Unidad unidad = gestorRutas.buscarUnidad(placa);
        List<Recorrido> historial = new ArrayList<>();
        for (Recorrido r : recorridos) {
            if (r.getUnidad() == unidad) {
                historial.add(r);
            }
        }
        return historial;
    }

    public List<Recorrido> getRecorridos() {
        return Collections.unmodifiableList(recorridos);
    }

    public RelojSimulado getReloj() {
        return reloj;
    }

    // ------------------------------------------------------------------
    // Soporte interno
    // ------------------------------------------------------------------

    private void registrarUbicacion(Recorrido recorrido, double latitud, double longitud) {
        Ubicacion ubicacion = new Ubicacion(latitud, longitud, reloj.ahora());
        recorrido.agregarUbicacion(ubicacion);
        recorrido.getUnidad().actualizarUbicacion(ubicacion);
        for (ObservadorUbicacion o : observadores) {
            o.ubicacionRegistrada(recorrido, ubicacion);
        }
    }

    private void validarMinutos(int minutos) {
        if (minutos <= 0 || minutos > 60) {
            throw new ValidacionException("Los minutos deben estar entre 1 y 60.");
        }
    }
}
