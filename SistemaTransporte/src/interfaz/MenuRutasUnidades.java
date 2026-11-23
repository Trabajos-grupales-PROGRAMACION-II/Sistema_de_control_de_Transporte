package interfaz;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Conductor;
import modelo.Parada;
import modelo.Ruta;
import modelo.Unidad;
import negocio.GestorRutasUnidades;
import negocio.GestorUsuarios;

/**
 * Menú del módulo 2: Rutas y unidades.
 */
public class MenuRutasUnidades {

    private final GestorRutasUnidades gestor;
    private final GestorUsuarios gestorUsuarios;

    public MenuRutasUnidades(GestorRutasUnidades gestor, GestorUsuarios gestorUsuarios) {
        this.gestor = gestor;
        this.gestorUsuarios = gestorUsuarios;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("MÓDULO 2 - RUTAS Y UNIDADES");
            System.out.println("1. Listar rutas y paradas");
            System.out.println("2. Registrar ruta");
            System.out.println("3. Eliminar ruta");
            System.out.println("4. Listar unidades");
            System.out.println("5. Registrar unidad");
            System.out.println("6. Asignar conductor y ruta a una unidad");
            System.out.println("7. Poner o quitar una unidad fuera de servicio");
            System.out.println("8. Eliminar unidad");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: listarRutas(); break;
                    case 2: registrarRuta(); break;
                    case 3: eliminarRuta(); break;
                    case 4: listarUnidades(); break;
                    case 5: registrarUnidad(); break;
                    case 6: asignar(); break;
                    case 7: cambiarServicio(); break;
                    case 8: eliminarUnidad(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void listarRutas() {
        if (gestor.listarRutas().isEmpty()) {
            System.out.println("No hay rutas registradas.");
        }
        for (Ruta r : gestor.listarRutas()) {
            System.out.println(r + "  Horario " + r.getHoraInicio() + " - " + r.getHoraFin());
            for (Parada p : r.getParadas()) {
                System.out.println("    " + p);
            }
        }
    }

    private void registrarRuta() {
        String codigo = Consola.leerTexto("Código de la ruta: ");
        String nombre = Consola.leerTexto("Nombre: ");
        String origen = Consola.leerTexto("Origen: ");
        String destino = Consola.leerTexto("Destino: ");
        LocalTime inicio = Consola.leerHora("Hora de inicio (HH:mm): ");
        LocalTime fin = Consola.leerHora("Hora de fin (HH:mm): ");
        int cantidad = Consola.leerEntero("Cantidad de paradas: ");

        List<Parada> paradas = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            System.out.println("Parada " + i + ":");
            String nombreParada = Consola.leerTexto("  Nombre: ");
            int orden = Consola.leerEntero("  Orden: ");
            double latitud = Consola.leerDecimal("  Latitud: ");
            double longitud = Consola.leerDecimal("  Longitud: ");
            paradas.add(new Parada(nombreParada, orden, latitud, longitud));
        }
        Ruta ruta = gestor.registrarRuta(codigo, nombre, origen, destino, inicio, fin, paradas);
        Consola.exito("Ruta registrada correctamente: " + ruta);
    }

    private void eliminarRuta() {
        String codigo = Consola.leerTexto("Código de la ruta: ");
        if (Consola.confirmar("¿Eliminar la ruta " + codigo + "?")) {
            gestor.eliminarRuta(codigo);
            Consola.exito("Ruta eliminada.");
        }
    }

    private void listarUnidades() {
        System.out.printf("%-10s %4s %6s %-22s %-6s %s%n", "PLACA", "N.º", "CAP.", "CONDUCTOR", "RUTA", "ESTADO");
        for (Unidad u : gestor.listarUnidades()) {
            System.out.printf("%-10s %4d %6d %-22s %-6s %s%n", u.getPlaca(), u.getNumeroUnidad(), u.getCapacidad(),
                    u.getConductor() == null ? "-" : u.getConductor().getNombreCompleto(),
                    u.getRuta() == null ? "-" : u.getRuta().getCodigo(),
                    u.getEstado().getDescripcion());
        }
    }

    private void registrarUnidad() {
        String placa = Consola.leerTexto("Placa (ABC-1234): ");
        int numero = Consola.leerEntero("Número de unidad: ");
        int capacidad = Consola.leerEntero("Capacidad de pasajeros: ");
        Unidad unidad = gestor.registrarUnidad(placa, numero, capacidad);
        Consola.exito("Unidad registrada: " + unidad + ". Asígnele un conductor y una ruta.");
    }

    private void asignar() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        System.out.println("Conductores registrados:");
        for (Conductor c : gestorUsuarios.listarConductores()) {
            System.out.println("  " + c.getCorreo() + " - " + c.getNombreCompleto()
                    + " (licencia vence " + c.getVencimientoLicencia() + ")");
        }
        String correo = Consola.leerTexto("Correo del conductor: ");
        Conductor conductor = gestorUsuarios.buscarConductor(correo);
        String ruta = Consola.leerTexto("Código de la ruta: ");
        gestor.asignar(placa, conductor, ruta, gestorUsuarios.getReloj().ahora().toLocalDate());
        Consola.exito("Unidad registrada y asignada correctamente.");
    }

    private void cambiarServicio() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        boolean fuera = Consola.confirmar("¿Poner la unidad fuera de servicio? (n = volver a disponible)");
        gestor.cambiarServicio(placa, fuera);
        Consola.exito(fuera ? "Unidad fuera de servicio." : "Unidad disponible.");
    }

    private void eliminarUnidad() {
        String placa = Consola.leerTexto("Placa de la unidad: ");
        if (Consola.confirmar("¿Eliminar la unidad " + placa + "?")) {
            gestor.eliminarUnidad(placa);
            Consola.exito("Unidad eliminada.");
        }
    }
}
