package modelo;

/**
 * Usuario de la operadora que gestiona usuarios, rutas, unidades y alertas.
 */
public class Administrador extends Usuario {

    public Administrador(String nombres, String apellidos, String correo, String contrasena, String telefono) {
        super(nombres, apellidos, correo, contrasena, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}
