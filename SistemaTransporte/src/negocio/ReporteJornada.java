package negocio;

import modelo.Conductor;

/**
 * Resultado del control de jornada de un conductor en un día.
 */
public class ReporteJornada {

    private Conductor conductor;
    private int recorridosDelDia;
    private double horasConducidas;
    private double limiteHoras;

    public ReporteJornada(Conductor conductor, int recorridosDelDia, double horasConducidas, double limiteHoras) {
        this.conductor = conductor;
        this.recorridosDelDia = recorridosDelDia;
        this.horasConducidas = horasConducidas;
        this.limiteHoras = limiteHoras;
    }

    /** Estado de la jornada: dentro del límite, cerca del límite (80 %) o límite alcanzado. */
    public String getEstado() {
        if (horasConducidas >= limiteHoras) {
            return "Límite alcanzado";
        }
        if (horasConducidas >= limiteHoras * 0.8) {
            return "Cerca del límite";
        }
        return "Dentro del límite";
    }

    public double getHorasRestantes() {
        return Math.max(0, limiteHoras - horasConducidas);
    }

    public Conductor getConductor() {
        return conductor;
    }

    public int getRecorridosDelDia() {
        return recorridosDelDia;
    }

    public double getHorasConducidas() {
        return horasConducidas;
    }

    public double getLimiteHoras() {
        return limiteHoras;
    }
}
