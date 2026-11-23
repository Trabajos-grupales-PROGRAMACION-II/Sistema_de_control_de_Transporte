package interfaz;

import java.util.List;
import java.util.Locale;

import modelo.Parada;
import modelo.Ruta;
import negocio.EstimacionLlegada;
import negocio.GestorLlegadas;
import negocio.GestorRutasUnidades;

/**
 * Consultas de tiempo estimado de llegada (RF8). Lo usan el
 * administrador y el pasajero.
 */
public class MenuLlegadas {

    private final GestorLlegadas gestor;
    private final GestorRutasUnidades gestorRutas;

    public MenuLlegadas(GestorLlegadas gestor, GestorRutasUnidades gestorRutas) {
        this.gestor = gestor;
        this.gestorRutas = gestorRutas;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("TIEMPOS ESTIMADOS DE LLEGADA");
            System.out.println("1. ¿Qué unidad llega primero a mi parada?");
            System.out.println("2. Tiempo de llegada de una unidad a una parada");
            System.out.println("3. Próximas llegadas de una unidad");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: unidadMasProxima(); break;
                    case 2: tiempoDeUnidad(); break;
                    case 3: proximasLlegadas(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void unidadMasProxima() {
        String codigo = Consola.leerTexto("Código de la ruta: ");
        mostrarParadas(gestorRutas.buscarRuta(codigo));
        int orden = Consola.leerEntero("Orden de su parada: ");
        EstimacionLlegada e = gestor.buscarUnidadMasProxima(codigo, orden);
        if (e == null) {
            System.out.println("Ninguna unidad en recorrido se dirige a esa parada.");
            return;
        }
        Consola.exito("La próxima unidad en llegar a " + e.getParada().getNombre() + " es "
                + e.getUnidad().getPlaca() + ": llega en " + e.getMinutos() + " min.");
    }

    private void tiempoDeUnidad() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        mostrarParadas(gestorRutas.buscarUnidad(placa).getRuta());
        int orden = Consola.leerEntero("Orden de la parada: ");
        EstimacionLlegada e = gestor.calcularTiempoEstimadoLlegada(placa, orden);
        System.out.printf(Locale.US, "Distancia restante: %.0f m | Velocidad: %.1f km/h%s%n",
                e.getDistanciaMetros(), e.getVelocidadKmh(),
                e.isVelocidadDeReferencia() ? " (referencia)" : " (promedio últimos 2 min)");
        Consola.exito("La unidad " + placa.toUpperCase() + " llega a " + e.getParada().getNombre()
                + " en " + e.getMinutos() + " min.");
    }

    private void proximasLlegadas() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        List<EstimacionLlegada> lista = gestor.calcularProximasLlegadas(placa);
        if (lista.isEmpty()) {
            System.out.println("La unidad ya pasó por todas las paradas.");
            return;
        }
        System.out.printf("%-22s %12s %10s%n", "PARADA", "DISTANCIA", "LLEGA EN");
        for (EstimacionLlegada e : lista) {
            System.out.printf(Locale.US, "%-22s %10.0f m %7d min%n",
                    e.getParada().getNombre(), e.getDistanciaMetros(), e.getMinutos());
        }
    }

    private void mostrarParadas(Ruta ruta) {
        if (ruta == null) {
            return;
        }
        System.out.println("Paradas de la ruta " + ruta.getCodigo() + ":");
        for (Parada p : ruta.getParadas()) {
            System.out.println("  " + p.getOrden() + ". " + p.getNombre());
        }
    }
}
