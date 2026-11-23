package negocio;

import java.util.List;

import modelo.Parada;

/**
 * Cálculos geográficos sobre coordenadas GPS.
 */
public final class CalculadoraGeografica {

    private static final double RADIO_TIERRA_METROS = 6371000;

    private CalculadoraGeografica() {
    }

    /**
     * Distancia en metros entre dos coordenadas (fórmula de Haversine).
     */
    public static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RADIO_TIERRA_METROS * Math.asin(Math.sqrt(a));
    }

    /**
     * Distancia acumulada desde la primera parada hasta la parada indicada (índice en la lista).
     */
    public static double distanciaHastaParada(List<Parada> paradas, int indice) {
        double total = 0;
        for (int i = 1; i <= indice; i++) {
            Parada a = paradas.get(i - 1);
            Parada b = paradas.get(i);
            total += distanciaMetros(a.getLatitud(), a.getLongitud(), b.getLatitud(), b.getLongitud());
        }
        return total;
    }

    public static double longitudTotal(List<Parada> paradas) {
        return distanciaHastaParada(paradas, paradas.size() - 1);
    }

    /**
     * Punto que se encuentra a una distancia dada a lo largo del trazado de paradas.
     * Devuelve {latitud, longitud}.
     */
    public static double[] puntoSobreTrazado(List<Parada> paradas, double distancia) {
        double acumulada = 0;
        for (int i = 1; i < paradas.size(); i++) {
            Parada a = paradas.get(i - 1);
            Parada b = paradas.get(i);
            double tramo = distanciaMetros(a.getLatitud(), a.getLongitud(), b.getLatitud(), b.getLongitud());
            if (acumulada + tramo >= distancia) {
                double fraccion = (distancia - acumulada) / tramo;
                return new double[] {
                    a.getLatitud() + (b.getLatitud() - a.getLatitud()) * fraccion,
                    a.getLongitud() + (b.getLongitud() - a.getLongitud()) * fraccion
                };
            }
            acumulada += tramo;
        }
        Parada ultima = paradas.get(paradas.size() - 1);
        return new double[] { ultima.getLatitud(), ultima.getLongitud() };
    }

    /**
     * Distancia mínima en metros entre un punto y el trazado de la ruta
     * (segmentos entre paradas consecutivas).
     */
    public static double distanciaAlTrazado(List<Parada> paradas, double lat, double lon) {
        double minima = Double.MAX_VALUE;
        for (int i = 1; i < paradas.size(); i++) {
            Parada a = paradas.get(i - 1);
            Parada b = paradas.get(i);
            minima = Math.min(minima, distanciaASegmento(lat, lon,
                    a.getLatitud(), a.getLongitud(), b.getLatitud(), b.getLongitud()));
        }
        return minima;
    }

    /**
     * Devuelve un punto desplazado unos metros hacia el este y el norte.
     */
    public static double[] desplazar(double lat, double lon, double metrosEste, double metrosNorte) {
        double nuevaLat = lat + Math.toDegrees(metrosNorte / RADIO_TIERRA_METROS);
        double nuevaLon = lon + Math.toDegrees(metrosEste / (RADIO_TIERRA_METROS * Math.cos(Math.toRadians(lat))));
        return new double[] { nuevaLat, nuevaLon };
    }

    /**
     * Distancia de un punto P a un segmento AB, usando una proyección plana
     * local (válida para distancias urbanas).
     */
    private static double distanciaASegmento(double lat, double lon,
                                             double latA, double lonA, double latB, double lonB) {
        double escalaLon = Math.cos(Math.toRadians(lat));
        double px = Math.toRadians(lon) * escalaLon * RADIO_TIERRA_METROS;
        double py = Math.toRadians(lat) * RADIO_TIERRA_METROS;
        double ax = Math.toRadians(lonA) * escalaLon * RADIO_TIERRA_METROS;
        double ay = Math.toRadians(latA) * RADIO_TIERRA_METROS;
        double bx = Math.toRadians(lonB) * escalaLon * RADIO_TIERRA_METROS;
        double by = Math.toRadians(latB) * RADIO_TIERRA_METROS;

        double dx = bx - ax;
        double dy = by - ay;
        double largo2 = dx * dx + dy * dy;
        double t = largo2 == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / largo2;
        t = Math.max(0, Math.min(1, t));
        double cx = ax + t * dx;
        double cy = ay + t * dy;
        return Math.hypot(px - cx, py - cy);
    }
}
