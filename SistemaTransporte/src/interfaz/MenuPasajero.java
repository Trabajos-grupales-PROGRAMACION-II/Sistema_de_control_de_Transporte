package interfaz;

import modelo.Pasajero;
import modelo.Unidad;
import negocio.GestorMonitoreo;

/**
 * Menú del pasajero: consulta las unidades en recorrido y los tiempos de llegada.
 */
public class MenuPasajero {

    private final Pasajero pasajero;
    private final GestorMonitoreo monitoreo;
    private final MenuLlegadas menuLlegadas;

    public MenuPasajero(Pasajero pasajero, GestorMonitoreo monitoreo, MenuLlegadas menuLlegadas) {
        this.pasajero = pasajero;
        this.monitoreo = monitoreo;
        this.menuLlegadas = menuLlegadas;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("PASAJERO: " + pasajero.getNombreCompleto());
            System.out.println("1. Ver unidades en recorrido");
            System.out.println("2. Consultar tiempos de llegada");
            System.out.println("0. Cerrar sesión");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: verUnidades(); break;
                    case 2: menuLlegadas.mostrar(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void verUnidades() {
        boolean hay = false;
        for (Unidad u : monitoreo.obtenerEstadoFlota()) {
            if (u.estaEnOperacion()) {
                hay = true;
                System.out.println("  " + u.getPlaca() + " - ruta " + u.getRuta().getCodigo() + " - "
                        + u.getEstado().getDescripcion() + " - última ubicación " + u.getUltimaUbicacion());
            }
        }
        if (!hay) {
            System.out.println("No hay unidades en recorrido en este momento.");
        }
    }
}
