package interfaz;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import modelo.Anomalia;
import modelo.Recorrido;
import negocio.GestorAnomalias;
import negocio.GestorMonitoreo;
import negocio.ReporteUnidad;

/**
 * Menú del módulo 4: Anomalías y alertas.
 */
public class MenuAnomalias {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GestorAnomalias gestor;
    private final GestorMonitoreo monitoreo;

    public MenuAnomalias(GestorAnomalias gestor, GestorMonitoreo monitoreo) {
        this.gestor = gestor;
        this.monitoreo = monitoreo;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("MÓDULO 4 - ANOMALÍAS Y ALERTAS");
            System.out.println("1. Alertas pendientes");
            System.out.println("2. Historial de todas las alertas");
            System.out.println("3. Marcar alerta como atendida");
            System.out.println("4. Historial de recorridos de una unidad");
            System.out.println("5. Reporte de operación por unidad");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: listar(true); break;
                    case 2: listar(false); break;
                    case 3: atender(); break;
                    case 4: historial(); break;
                    case 5: reporte(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void listar(boolean pendientes) {
        List<Anomalia> lista = gestor.listarAnomalias(pendientes);
        if (lista.isEmpty()) {
            System.out.println(pendientes ? "No hay alertas pendientes." : "No se han registrado alertas.");
        }
        for (Anomalia a : lista) {
            System.out.println("  " + a);
        }
    }

    private void atender() {
        int id = Consola.leerEntero("Número de la alerta: ");
        gestor.atenderAnomalia(id);
        Consola.exito("Alerta #" + id + " marcada como atendida.");
    }

    private void historial() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        List<Recorrido> lista = monitoreo.obtenerHistorial(placa);
        if (lista.isEmpty()) {
            System.out.println("La unidad todavía no tiene recorridos.");
            return;
        }
        System.out.printf("%-4s %-20s %-10s %-10s %10s %9s%n", "N.º", "CONDUCTOR", "INICIO", "FIN", "DISTANCIA", "DURACIÓN");
        for (Recorrido r : lista) {
            System.out.printf(Locale.US, "%-4d %-20s %-10s %-10s %7.2f km %5.0f min%n", r.getId(),
                    r.getConductor().getNombreCompleto(), r.getInicio().format(HORA),
                    r.getFin() == null ? "en curso" : r.getFin().format(HORA),
                    r.getDistanciaRecorridaMetros() / 1000, r.getDuracionMinutos());
        }
    }

    private void reporte() {
        System.out.printf("%-10s %11s %8s %13s %8s %12s  %s%n",
                "PLACA", "RECORRIDOS", "KM", "PROMEDIO", "DESVÍOS", "DETENCIONES", "CUMPLIMIENTO");
        for (ReporteUnidad r : gestor.generarReportePorUnidad()) {
            System.out.printf(Locale.US, "%-10s %11d %8.2f %9.1f min %8d %12d  %s%n",
                    r.getUnidad().getPlaca(), r.getRecorridosFinalizados(), r.getKilometros(),
                    r.getDuracionPromedioMinutos(), r.getDesvios(), r.getDetenciones(), r.getNivelCumplimiento());
        }
    }
}
