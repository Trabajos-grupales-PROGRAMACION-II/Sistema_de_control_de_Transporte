package interfaz;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import modelo.Administrador;
import modelo.Conductor;
import modelo.Parada;
import modelo.Pasajero;
import modelo.Usuario;
import negocio.GestorAnomalias;
import negocio.GestorLlegadas;
import negocio.GestorMonitoreo;
import negocio.GestorRutasUnidades;
import negocio.GestorUsuarios;
import negocio.RelojSimulado;

/**
 * Punto de entrada del Sistema de Gestión y Monitoreo de Transporte (modo consola).
 * Crea los gestores de los cuatro módulos, carga datos de demostración y
 * muestra el menú según el rol del usuario que inicia sesión.
 */
public class Main {

    private final GestorUsuarios gestorUsuarios;
    private final GestorRutasUnidades gestorRutas;
    private final GestorMonitoreo monitoreo;
    private final GestorAnomalias anomalias;
    private final MenuLlegadas menuLlegadas;
    private final MenuUsuarios menuUsuarios;
    private final MenuRutasUnidades menuRutas;
    private final MenuMonitoreo menuMonitoreo;
    private final MenuAnomalias menuAnomalias;

    public Main() {
        RelojSimulado reloj = new RelojSimulado(LocalDate.now().atTime(7, 0));
        gestorUsuarios = new GestorUsuarios(reloj);
        gestorRutas = new GestorRutasUnidades();
        monitoreo = new GestorMonitoreo(gestorRutas, gestorUsuarios, reloj);
        anomalias = new GestorAnomalias(monitoreo);
        monitoreo.agregarObservador(anomalias);
        GestorLlegadas llegadas = new GestorLlegadas(monitoreo, gestorRutas);

        menuLlegadas = new MenuLlegadas(llegadas, gestorRutas);
        menuUsuarios = new MenuUsuarios(gestorUsuarios, monitoreo);
        menuRutas = new MenuRutasUnidades(gestorRutas, gestorUsuarios);
        menuMonitoreo = new MenuMonitoreo(monitoreo, anomalias, menuLlegadas);
        menuAnomalias = new MenuAnomalias(anomalias, monitoreo);
    }

    public static void main(String[] args) {
        Main sistema = new Main();
        sistema.cargarDatosIniciales();
        sistema.ejecutar();
    }

    public void ejecutar() {
        int opcion;
        do {
            Consola.titulo("SISTEMA DE GESTIÓN Y MONITOREO DE TRANSPORTE");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrarse como pasajero");
            System.out.println("0. Salir");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1: iniciarSesion(); break;
                    case 2: registrarPasajero(); break;
                    case 0: System.out.println("Hasta pronto."); break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                Consola.error(e);
            }
        } while (opcion != 0);
    }

    private void iniciarSesion() {
        String correo = Consola.leerTexto("Correo: ");
        String contrasena = Consola.leerTexto("Contraseña: ");
        Usuario usuario = gestorUsuarios.iniciarSesion(correo, contrasena);
        Consola.exito("Inicio de sesión exitoso. Bienvenido/a, " + usuario.getNombres() + " (" + usuario.getRol() + ").");

        if (usuario instanceof Administrador) {
            menuAdministrador();
        } else if (usuario instanceof Conductor) {
            new MenuConductor((Conductor) usuario, monitoreo, gestorRutas, gestorUsuarios, anomalias).mostrar();
        } else {
            new MenuPasajero((Pasajero) usuario, monitoreo, menuLlegadas).mostrar();
        }
    }

    private void menuAdministrador() {
        int opcion;
        do {
            Consola.titulo("ADMINISTRADOR   (alertas pendientes: " + anomalias.listarAnomalias(true).size() + ")");
            System.out.println("1. Usuarios y conductores");
            System.out.println("2. Rutas y unidades");
            System.out.println("3. Monitoreo y llegada");
            System.out.println("4. Anomalías y alertas");
            System.out.println("0. Cerrar sesión");
            opcion = Consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1: menuUsuarios.mostrar(); break;
                case 2: menuRutas.mostrar(); break;
                case 3: menuMonitoreo.mostrar(); break;
                case 4: menuAnomalias.mostrar(); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void registrarPasajero() {
        String nombres = Consola.leerTexto("Nombres: ");
        String apellidos = Consola.leerTexto("Apellidos: ");
        String correo = Consola.leerTexto("Correo: ");
        String contrasena = Consola.leerTexto("Contraseña (mínimo 8 caracteres): ");
        String telefono = Consola.leerTexto("Teléfono: ");
        gestorUsuarios.registrarUsuario(new Pasajero(nombres, apellidos, correo, contrasena, telefono));
        Consola.exito("Usuario registrado correctamente. Ya puede iniciar sesión.");
    }

    /**
     * Datos de demostración: un administrador, tres conductores, un pasajero,
     * una ruta con tres paradas en Quito y dos unidades asignadas.
     */
    private void cargarDatosIniciales() {
        LocalDate hoy = gestorUsuarios.getReloj().ahora().toLocalDate();
        gestorUsuarios.registrarUsuario(new Administrador("Ana", "Torres", "admin@transporte.ec", "Admin2026", "0991111111"));
        Conductor carlos = new Conductor("Carlos", "Pérez", "carlos@transporte.ec", "Conductor1", "0992222222",
                "LIC-1001", hoy.plusYears(2));
        Conductor maria = new Conductor("María", "López", "maria@transporte.ec", "Conductor2", "0993333333",
                "LIC-1002", hoy.plusYears(1));
        Conductor luis = new Conductor("Luis", "Andrade", "luis@transporte.ec", "Conductor3", "0994444444",
                "LIC-1003", hoy.minusDays(10));
        gestorUsuarios.registrarUsuario(carlos);
        gestorUsuarios.registrarUsuario(maria);
        gestorUsuarios.registrarUsuario(luis);
        gestorUsuarios.registrarUsuario(new Pasajero("Sofía", "Mena", "sofia@correo.ec", "Pasajero1", "0995555555"));

        gestorRutas.registrarRuta("R1", "Ruta Norte - UDLA", "Terminal Norte", "UDLA Park",
                LocalTime.of(6, 0), LocalTime.of(20, 0), Arrays.asList(
                        new Parada("Terminal Norte", 1, -0.1270, -78.4900),
                        new Parada("Parada intermedia", 2, -0.1650, -78.4820),
                        new Parada("UDLA Park", 3, -0.1960, -78.4780)));

        gestorRutas.registrarUnidad("PBA-1234", 1, 40);
        gestorRutas.registrarUnidad("PBB-5678", 2, 35);
        gestorRutas.registrarUnidad("PBC-9012", 3, 40);
        gestorRutas.asignar("PBA-1234", carlos, "R1", hoy);
        gestorRutas.asignar("PBB-5678", maria, "R1", hoy);
    }
}
