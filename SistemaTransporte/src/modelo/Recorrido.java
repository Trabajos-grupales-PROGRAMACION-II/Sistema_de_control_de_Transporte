package modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Viaje de una unidad sobre su ruta, desde que el conductor lo inicia
 * hasta que lo finaliza. Composición: las ubicaciones registradas
 * pertenecen al recorrido.
 */
public class Recorrido {

    private int id;
    private Unidad unidad;
    private Conductor conductor;
    private Ruta ruta;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private List<Ubicacion> ubicaciones;
    private double distanciaRecorridaMetros;
    private boolean enDesvio;
    private boolean detencionReportada;

    public Recorrido(int id, Unidad unidad, LocalDateTime inicio) {
        this.id = id;
        this.unidad = unidad;
        this.conductor = unidad.getConductor();
        this.ruta = unidad.getRuta();
        this.inicio = inicio;
        this.ubicaciones = new ArrayList<>();
        this.distanciaRecorridaMetros = 0;
    }

    public void agregarUbicacion(Ubicacion ubicacion) {
        ubicaciones.add(ubicacion);
    }

    public void finalizar(LocalDateTime fechaHora) {
        this.fin = fechaHora;
    }

    public boolean estaEnCurso() {
        return fin == null;
    }

    public void sumarDistancia(double metros) {
        this.distanciaRecorridaMetros += metros;
    }

    public Ubicacion getUltimaUbicacion() {
        return ubicaciones.isEmpty() ? null : ubicaciones.get(ubicaciones.size() - 1);
    }

    /** Duración en minutos; si sigue en curso, hasta la última ubicación registrada. */
    public double getDuracionMinutos() {
        LocalDateTime hasta = fin;
        if (hasta == null) {
            hasta = ubicaciones.isEmpty() ? inicio : getUltimaUbicacion().getFechaHora();
        }
        return Duration.between(inicio, hasta).getSeconds() / 60.0;
    }

    public int getId() {
        return id;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public List<Ubicacion> getUbicaciones() {
        return Collections.unmodifiableList(ubicaciones);
    }

    public double getDistanciaRecorridaMetros() {
        return distanciaRecorridaMetros;
    }

    public boolean isEnDesvio() {
        return enDesvio;
    }

    public void setEnDesvio(boolean enDesvio) {
        this.enDesvio = enDesvio;
    }

    public boolean isDetencionReportada() {
        return detencionReportada;
    }

    public void setDetencionReportada(boolean detencionReportada) {
        this.detencionReportada = detencionReportada;
    }
}
