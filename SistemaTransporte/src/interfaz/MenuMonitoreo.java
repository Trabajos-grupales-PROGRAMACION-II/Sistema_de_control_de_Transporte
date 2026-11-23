package interfaz;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import modelo.Anomalia;
import modelo.Recorrido;
import modelo.Ubicacion;
import modelo.Unidad;
import negocio.GestorAnomalias;
import negocio.GestorMonitoreo;

/**
 * Menú del módulo 3: Monitoreo y llegada (vista del administrador).
 */
public class MenuMonitoreo {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GestorMonitoreo gestor;
    private final GestorAnomalias anomalias;
    private final MenuLlegadas menuLlegadas;

    public MenuMonitoreo(GestorMonitoreo gestor, GestorAnomalias anomalias, MenuLlegadas menuLlegadas) {
        this.gestor = gestor;
        this.anomalias = anomalias;
        this.menuLlegadas = menuLlegadas;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("MÓDULO 3 - MONITOREO Y LLEGADA   [" + gestor.getReloj().ahora().format(HORA) + "]");
            System.out.println("1. Ver estado de la flota");
            System.out.println("2. Iniciar recorrido");
            System.out.println("3. Simular avance (GPS cada 5 s)");
            System.out.println("4. Simular detención de una unidad");
            System.out.println("5. Simular desvío de ruta");
            System.out.println("6. Simular pérdida de señal");
            System.out.println("7. Finalizar recorrido");
            System.out.println("8. Tiempos estimados de llegada");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: mostrarFlota(gestor); break;
                    case 2: iniciar(); break;
                    case 3: avanzar(); break;
                    case 4: detener(); break;
                    case 5: desviar(); break;
                    case 6: perderSenal(); break;
                    case 7: finalizar(); break;
                    case 8: menuLlegadas.mostrar(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
            mostrarAlertasNuevas(anomalias);
        } while (opcion != 0);
    }

    public static void mostrarFlota(GestorMonitoreo gestor) {
        System.out.printf("%-10s %-20s %-6s %-18s %-22s %s%n",
                "PLACA", "CONDUCTOR", "RUTA", "ESTADO", "ÚLTIMA UBICACIÓN", "SEÑAL");
        for (Unidad u : gestor.obtenerEstadoFlota()) {
            String ubicacion = "-";
            String senal = "-";
            if (u.getUltimaUbicacion() != null) {
                Ubicacion ub = u.getUltimaUbicacion();
                ubicacion = String.format(Locale.US, "(%.5f, %.5f)", ub.getLatitud(), ub.getLongitud());
                if (u.estaEnOperacion()) {
                    senal = "hace " + gestor.segundosDesdeUltimaSenal(u) + " s";
                }
            }
            System.out.printf("%-10s %-20s %-6s %-18s %-22s %s%n", u.getPlaca(),
                    u.getConductor() == null ? "-" : u.getConductor().getNombreCompleto(),
                    u.getRuta() == null ? "-" : u.getRuta().getCodigo(),
                    u.getEstado().getDescripcion(), ubicacion, senal);
        }
    }

    public static void mostrarAlertasNuevas(GestorAnomalias anomalias) {
        for (Anomalia a : anomalias.obtenerAlertasNuevas()) {
            System.out.println("!!! ALERTA: la unidad " + a.getUnidad().getPlaca() + " " + a.getDetalle()
                    + " (alerta #" + a.getId() + ")");
        }
    }

    private void iniciar() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        Recorrido r = gestor.iniciarRecorrido(placa);
        Consola.exito("Recorrido " + r.getId() + " iniciado en la ruta " + r.getRuta().getCodigo()
                + " con el conductor " + r.getConductor().getNombreCompleto() + ".");
    }

    private void avanzar() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        int minutos = Consola.leerEntero("Minutos a simular: ");
        int enviados = gestor.simularAvance(placa, minutos);
        java.util.List<Recorrido> historial = gestor.obtenerHistorial(placa);
        Ubicacion ultima = historial.get(historial.size() - 1).getUltimaUbicacion();
        Consola.exito(enviados + " ubicaciones enviadas, una cada " + GestorMonitoreo.INTERVALO_GPS_SEGUNDOS
                + " s. Última: " + ultima);
        if (gestor.llegoAlDestino(placa)) {
            System.out.println("La unidad llegó a la última parada. Puede finalizar el recorrido.");
        }
    }

    private void detener() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        int minutos = Consola.leerEntero("Minutos detenida: ");
        gestor.simularDetencion(placa, minutos);
        Consola.exito("La unidad permaneció detenida " + minutos + " min enviando su ubicación.");
    }

    private void desviar() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        int metros = Consola.leerEntero("Metros fuera de la ruta: ");
        gestor.simularDesvio(placa, metros);
        Consola.exito("La unidad envió 3 ubicaciones a " + metros + " m del trazado.");
    }

    private void perderSenal() {
        int segundos = Consola.leerEntero("Segundos sin recibir ubicaciones: ");
        gestor.simularPerdidaDeSenal(segundos);
        System.out.println("Pasaron " + segundos + " s sin señal. Las unidades con más de "
                + GestorMonitoreo.SEGUNDOS_SIN_SENAL + " s sin ubicación quedan \"Sin señal\".");
        mostrarFlota(gestor);
    }

    private void finalizar() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        Recorrido r = gestor.finalizarRecorrido(placa);
        Consola.exito(String.format(Locale.US, "Recorrido %d finalizado: %.2f km en %.0f min, %d ubicaciones.",
                r.getId(), r.getDistanciaRecorridaMetros() / 1000, r.getDuracionMinutos(), r.getUbicaciones().size()));
    }
}
