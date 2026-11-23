package interfaz;

import java.time.LocalDate;
import java.util.Locale;

import modelo.Administrador;
import modelo.Conductor;
import modelo.Pasajero;
import modelo.Usuario;
import negocio.GestorMonitoreo;
import negocio.GestorUsuarios;
import negocio.ReporteJornada;

/**
 * Menú del módulo 1: Usuarios y conductores.
 */
public class MenuUsuarios {

    private final GestorUsuarios gestor;
    private final GestorMonitoreo monitoreo;

    public MenuUsuarios(GestorUsuarios gestor, GestorMonitoreo monitoreo) {
        this.gestor = gestor;
        this.monitoreo = monitoreo;
    }

    public void mostrar() {
        int opcion;
        do {
            Consola.titulo("MÓDULO 1 - USUARIOS Y CONDUCTORES");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Registrar usuario");
            System.out.println("3. Desactivar usuario");
            System.out.println("4. Activar usuario");
            System.out.println("5. Reporte de jornada de conductores");
            System.out.println("6. Configurar límite de horas de la jornada");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: listar(); break;
                    case 2: registrar(); break;
                    case 3: cambiarEstado(false); break;
                    case 4: cambiarEstado(true); break;
                    case 5: reporteJornada(); break;
                    case 6: configurarLimite(); break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void listar() {
        System.out.printf("%-24s %-30s %-14s %s%n", "NOMBRE", "CORREO", "ROL", "ESTADO");
        for (Usuario u : gestor.listarUsuarios()) {
            System.out.printf("%-24s %-30s %-14s %s%n", u.getNombreCompleto(), u.getCorreo(), u.getRol(),
                    u.isActivo() ? "Activo" : "Desactivado");
        }
    }

    private void registrar() {
        System.out.println("Rol: 1. Administrador  2. Conductor  3. Pasajero");
        int rol = Consola.leerEntero("Seleccione el rol: ");
        if (rol < 1 || rol > 3) {
            System.out.println("Rol no válido.");
            return;
        }
        String nombres = Consola.leerTexto("Nombres: ");
        String apellidos = Consola.leerTexto("Apellidos: ");
        String correo = Consola.leerTexto("Correo: ");
        String contrasena = Consola.leerTexto("Contraseña (mínimo 8 caracteres): ");
        String telefono = Consola.leerTexto("Teléfono: ");

        Usuario usuario;
        if (rol == 1) {
            usuario = new Administrador(nombres, apellidos, correo, contrasena, telefono);
        } else if (rol == 2) {
            String licencia = Consola.leerTexto("Número de licencia: ");
            LocalDate vencimiento = Consola.leerFecha("Vencimiento de la licencia (AAAA-MM-DD): ");
            usuario = new Conductor(nombres, apellidos, correo, contrasena, telefono, licencia, vencimiento);
        } else {
            usuario = new Pasajero(nombres, apellidos, correo, contrasena, telefono);
        }
        gestor.registrarUsuario(usuario);
        Consola.exito("Usuario registrado correctamente: " + usuario);
    }

    private void cambiarEstado(boolean activo) {
        String correo = Consola.leerTexto("Correo del usuario: ");
        gestor.cambiarEstado(correo, activo);
        Consola.exito(activo ? "Usuario activado." : "Usuario desactivado. Ya no podrá iniciar sesión.");
    }

    private void reporteJornada() {
        System.out.printf("Límite de la jornada: %.2f h%n", gestor.getLimiteHorasJornada());
        System.out.printf("%-22s %11s %10s %10s  %s%n", "CONDUCTOR", "RECORRIDOS", "HORAS", "RESTANTE", "ESTADO");
        for (ReporteJornada r : gestor.generarReporteJornada(monitoreo.getRecorridos())) {
            System.out.printf(Locale.US, "%-22s %11d %10.2f %10.2f  %s%n",
                    r.getConductor().getNombreCompleto(), r.getRecorridosDelDia(),
                    r.getHorasConducidas(), r.getHorasRestantes(), r.getEstado());
        }
    }

    private void configurarLimite() {
        double horas = Consola.leerDecimal("Nuevo límite de horas por día: ");
        gestor.setLimiteHorasJornada(horas);
        Consola.exito(String.format("Límite actualizado a %.2f horas.", horas));
    }
}
