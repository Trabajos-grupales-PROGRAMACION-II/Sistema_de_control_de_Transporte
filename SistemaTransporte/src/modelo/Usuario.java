package modelo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Persona que utiliza el sistema. Clase abstracta: cada tipo de usuario
 * (administrador, conductor, pasajero) define su propio rol.
 */
public abstract class Usuario {

    public static final int LONGITUD_MINIMA_CONTRASENA = 8;
    public static final int MAXIMO_INTENTOS_FALLIDOS = 5;
    public static final int MINUTOS_BLOQUEO = 15;

    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasenaCifrada;
    private String telefono;
    private boolean activo;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    protected Usuario(String nombres, String apellidos, String correo, String contrasena, String telefono) {
        if (esVacio(nombres) || esVacio(apellidos) || esVacio(correo) || esVacio(contrasena)) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }
        if (!correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("El formato del correo no es válido.");
        }
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.correo = correo.trim().toLowerCase();
        this.contrasenaCifrada = cifrar(contrasena);
        this.telefono = telefono;
        this.activo = true;
        this.intentosFallidos = 0;
    }

    /** Nombre del rol del usuario; cada subclase lo define. */
    public abstract String getRol();

    public boolean verificarContrasena(String contrasena) {
        return contrasena != null && contrasenaCifrada.equals(cifrar(contrasena));
    }

    /**
     * Registra un intento fallido de inicio de sesión. Al llegar a cinco
     * intentos consecutivos, la cuenta se bloquea durante 15 minutos.
     */
    public void registrarIntentoFallido(LocalDateTime ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAXIMO_INTENTOS_FALLIDOS) {
            bloqueadoHasta = ahora.plusMinutes(MINUTOS_BLOQUEO);
            intentosFallidos = 0;
        }
    }

    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    /** Cifra la contraseña con SHA-256 para no almacenarla en texto plano. */
    private static String cifrar(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña.", e);
        }
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " <" + correo + "> - " + getRol() + (activo ? "" : " (desactivado)");
    }
}
