package interfaz;

import java.util.Locale;

import modelo.Conductor;
import modelo.Recorrido;
import modelo.Unidad;
import negocio.GestorAnomalias;
import negocio.GestorMonitoreo;
import negocio.GestorRutasUnidades;
import negocio.GestorUsuarios;

/**
 * Menú del conductor: opera el recorrido de la unidad que tiene asignada.
 */
public class MenuConductor {

    private final Conductor conductor;
    private final GestorMonitoreo monitoreo;
    private final GestorRutasUnidades gestorRutas;
    private final GestorUsuarios gestorUsuarios;
    private final GestorAnomalias anomalias;

    public MenuConductor(Conductor conductor, GestorMonitoreo monitoreo, GestorRutasUnidades gestorRutas,
                         GestorUsuarios gestorUsuarios, GestorAnomalias anomalias) {
        this.conductor = conductor;
        this.monitoreo = monitoreo;
        this.gestorRutas = gestorRutas;
        this.gestorUsuarios = gestorUsuarios;
        this.anomalias = anomalias;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("CONDUCTOR: " + conductor.getNombreCompleto());
            System.out.println("1. Ver mi unidad y ruta");
            System.out.println("2. Iniciar recorrido");
            System.out.println("3. Conducir (simular avance con GPS cada 5 s)");
            System.out.println("4. Finalizar recorrido");
            System.out.println("5. Mis horas de conducción de hoy");
            System.out.println("0. Cerrar sesión");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: verUnidad(); break;
                    case 2: iniciar(); break;
                    case 3: conducir(); break;
                    case 4: finalizar(); break;
                    case 5: horas(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
            MenuMonitoreo.mostrarAlertasNuevas(anomalias);
        } while (opcion != 0);
    }

    private Unidad miUnidad() {
        return gestorRutas.buscarUnidadDeConductor(conductor);
    }

    private void verUnidad() {
        Unidad u = miUnidad();
        System.out.println("Unidad: " + u + " - " + u.getEstado().getDescripcion());
        System.out.println("Ruta: " + (u.getRuta() == null ? "sin asignar" : u.getRuta()));
    }

    private void iniciar() {
        Recorrido r = monitoreo.iniciarRecorrido(miUnidad().getPlaca());
        Consola.exito("Recorrido iniciado (N.º " + r.getId() + "). Su ubicación se enviará cada 5 segundos.");
    }

    private void conducir() {
        int minutos = Consola.leerEntero("Minutos de conducción a simular: ");
        int enviados = monitoreo.simularAvance(miUnidad().getPlaca(), minutos);
        Consola.exito(enviados + " ubicaciones enviadas.");
        if (monitoreo.llegoAlDestino(miUnidad().getPlaca())) {
            System.out.println("Llegó a la última parada. Finalice el recorrido.");
        }
    }

    private void finalizar() {
        Recorrido r = monitoreo.finalizarRecorrido(miUnidad().getPlaca());
        Consola.exito(String.format(Locale.US, "Recorrido finalizado: %.2f km en %.0f min.",
                r.getDistanciaRecorridaMetros() / 1000, r.getDuracionMinutos()));
    }

    private void horas() {
        double horas = gestorUsuarios.calcularHorasConducidas(conductor, monitoreo.getRecorridos(),
                gestorUsuarios.getReloj().ahora().toLocalDate());
        System.out.printf(Locale.US, "Hoy ha conducido %.2f h de un máximo de %.2f h.%n",
                horas, gestorUsuarios.getLimiteHorasJornada());
    }
}
