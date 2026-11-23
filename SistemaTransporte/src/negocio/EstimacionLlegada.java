package negocio;

import modelo.Parada;
import modelo.Unidad;

/**
 * Resultado del cálculo del tiempo estimado de llegada de una unidad a una parada.
 */
public class EstimacionLlegada {

    private Unidad unidad;
    private Parada parada;
    private double distanciaMetros;
    private double velocidadKmh;
    private boolean velocidadDeReferencia;
    private int minutos;

    public EstimacionLlegada(Unidad unidad, Parada parada, double distanciaMetros, double velocidadKmh,
                             boolean velocidadDeReferencia, int minutos) {
        this.unidad = unidad;
        this.parada = parada;
        this.distanciaMetros = distanciaMetros;
        this.velocidadKmh = velocidadKmh;
        this.velocidadDeReferencia = velocidadDeReferencia;
        this.minutos = minutos;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public Parada getParada() {
        return parada;
    }

    public double getDistanciaMetros() {
        return distanciaMetros;
    }

    public double getVelocidadKmh() {
        return velocidadKmh;
    }

    public boolean isVelocidadDeReferencia() {
        return velocidadDeReferencia;
    }

    public int getMinutos() {
        return minutos;
    }
}
