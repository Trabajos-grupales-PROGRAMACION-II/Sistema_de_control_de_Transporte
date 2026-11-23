package negocio;

import modelo.Unidad;

/**
 * Resumen de la operación de una unidad: recorridos, distancia, duración
 * promedio y anomalías registradas.
 */
public class ReporteUnidad {

    private Unidad unidad;
    private int recorridosFinalizados;
    private double kilometros;
    private double duracionPromedioMinutos;
    private int desvios;
    private int detenciones;

    public ReporteUnidad(Unidad unidad, int recorridosFinalizados, double kilometros,
                         double duracionPromedioMinutos, int desvios, int detenciones) {
        this.unidad = unidad;
        this.recorridosFinalizados = recorridosFinalizados;
        this.kilometros = kilometros;
        this.duracionPromedioMinutos = duracionPromedioMinutos;
        this.desvios = desvios;
        this.detenciones = detenciones;
    }

    /** Nivel de cumplimiento según las anomalías por recorrido. */
    public String getNivelCumplimiento() {
        if (recorridosFinalizados == 0) {
            return "Sin datos";
        }
        double anomaliasPorRecorrido = (double) (desvios + detenciones) / recorridosFinalizados;
        if (anomaliasPorRecorrido == 0) {
            return "Óptimo";
        }
        return anomaliasPorRecorrido <= 1 ? "Aceptable" : "Requiere revisión";
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public int getRecorridosFinalizados() {
        return recorridosFinalizados;
    }

    public double getKilometros() {
        return kilometros;
    }

    public double getDuracionPromedioMinutos() {
        return duracionPromedioMinutos;
    }

    public int getDesvios() {
        return desvios;
    }

    public int getDetenciones() {
        return detenciones;
    }
}
