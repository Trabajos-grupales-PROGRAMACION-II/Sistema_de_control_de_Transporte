package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.Conductor;
import modelo.Recorrido;
import modelo.Usuario;

/**
 * Reglas de negocio del módulo 1, Usuarios y conductores:
 *  - RF1 Inicio de sesión con bloqueo tras 5 intentos fallidos
 *  - RF2 Registro, activación y desactivación de usuarios
 *  - RF3 Control de la jornada de conducción
 */
public class GestorUsuarios {

    private List<Usuario> usuarios;
    private RelojSimulado reloj;
    private double limiteHorasJornada;

    public GestorUsuarios(RelojSimulado reloj) {
        this.reloj = reloj;
        this.usuarios = new ArrayList<>();
        this.limiteHorasJornada = 8;
    }

    // ------------------------------------------------------------------
    // RF1 Inicio de sesión
    // ------------------------------------------------------------------

    public Usuario iniciarSesion(String correo, String contrasena) {
        Usuario usuario = buscarPorCorreoOpcional(correo);
        if (usuario == null) {
            throw new ValidacionException("Correo o contraseña incorrectos.");
        }
        if (usuario.estaBloqueado(reloj.ahora())) {
            throw new ValidacionException("Demasiados intentos. Intente de nuevo después de "
                    + Usuario.MINUTOS_BLOQUEO + " minutos.");
        }
        if (!usuario.isActivo()) {
            throw new ValidacionException("Cuenta desactivada. Contacte al administrador.");
        }
        if (!usuario.verificarContrasena(contrasena)) {
            usuario.registrarIntentoFallido(reloj.ahora());
            if (usuario.estaBloqueado(reloj.ahora())) {
                throw new ValidacionException("Demasiados intentos. La cuenta se bloqueó por "
                        + Usuario.MINUTOS_BLOQUEO + " minutos.");
            }
            int restantes = Usuario.MAXIMO_INTENTOS_FALLIDOS - usuario.getIntentosFallidos();
            throw new ValidacionException("Correo o contraseña incorrectos. Intentos restantes: " + restantes + ".");
        }
        usuario.reiniciarIntentos();
        return usuario;
    }

    // ------------------------------------------------------------------
    // RF2 Registro y gestión de usuarios
    // ------------------------------------------------------------------

    public void registrarUsuario(Usuario usuario) {
        if (buscarPorCorreoOpcional(usuario.getCorreo()) != null) {
            throw new ValidacionException("El correo ya está registrado.");
        }
        usuarios.add(usuario);
    }

    public void cambiarEstado(String correo, boolean activo) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setActivo(activo);
        if (activo) {
            usuario.reiniciarIntentos();
        }
    }

    public Usuario buscarPorCorreo(String correo) {
        Usuario usuario = buscarPorCorreoOpcional(correo);
        if (usuario == null) {
            throw new ValidacionException("No existe un usuario con el correo " + correo + ".");
        }
        return usuario;
    }

    public Conductor buscarConductor(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        if (!(usuario instanceof Conductor)) {
            throw new ValidacionException("El usuario " + correo + " no es un conductor.");
        }
        return (Conductor) usuario;
    }

    public List<Usuario> listarUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public List<Conductor> listarConductores() {
        List<Conductor> conductores = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Conductor) {
                conductores.add((Conductor) u);
            }
        }
        return conductores;
    }

    // ------------------------------------------------------------------
    // RF3 Control de jornada de conducción
    // ------------------------------------------------------------------

    /**
     * Horas que el conductor ha conducido en el día, sumando la duración de
     * sus recorridos (finalizados y en curso).
     */
    public double calcularHorasConducidas(Conductor conductor, List<Recorrido> recorridos, LocalDate dia) {
        double minutos = 0;
        for (Recorrido r : recorridos) {
            if (r.getConductor() == conductor && r.getInicio().toLocalDate().equals(dia)) {
                minutos += r.getDuracionMinutos();
            }
        }
        return minutos / 60.0;
    }

    /**
     * Reporte de la jornada de cada conductor en el día actual.
     */
    public List<ReporteJornada> generarReporteJornada(List<Recorrido> recorridos) {
        LocalDate hoy = reloj.ahora().toLocalDate();
        List<ReporteJornada> reporte = new ArrayList<>();
        for (Conductor c : listarConductores()) {
            int cantidad = 0;
            for (Recorrido r : recorridos) {
                if (r.getConductor() == c && r.getInicio().toLocalDate().equals(hoy)) {
                    cantidad++;
                }
            }
            reporte.add(new ReporteJornada(c, cantidad, calcularHorasConducidas(c, recorridos, hoy), limiteHorasJornada));
        }
        return reporte;
    }

    /**
     * Impide iniciar un recorrido si el conductor ya alcanzó el límite de horas del día.
     */
    public void verificarJornada(Conductor conductor, List<Recorrido> recorridos) {
        double horas = calcularHorasConducidas(conductor, recorridos, reloj.ahora().toLocalDate());
        if (horas >= limiteHorasJornada) {
            throw new ValidacionException(String.format(
                    "El conductor %s ya conduce %.2f h hoy y alcanzó el límite de %.2f h.",
                    conductor.getNombreCompleto(), horas, limiteHorasJornada));
        }
    }

    public double getLimiteHorasJornada() {
        return limiteHorasJornada;
    }

    public void setLimiteHorasJornada(double horas) {
        if (horas <= 0 || horas > 12) {
            throw new ValidacionException("El límite de la jornada debe estar entre 0 y 12 horas.");
        }
        this.limiteHorasJornada = horas;
    }

    public RelojSimulado getReloj() {
        return reloj;
    }

    private Usuario buscarPorCorreoOpcional(String correo) {
        if (correo == null) {
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getCorreo().equalsIgnoreCase(correo.trim())) {
                return u;
            }
        }
        return null;
    }
}
