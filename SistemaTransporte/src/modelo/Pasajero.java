package modelo;

/**
 * Usuario que consulta las unidades en recorrido y sus tiempos de llegada.
 */
public class Pasajero extends Usuario {

    public Pasajero(String nombres, String apellidos, String correo, String contrasena, String telefono) {
        super(nombres, apellidos, correo, contrasena, telefono);
    }

    @Override
    public String getRol() {
        return "Pasajero";
    }
}
