package modelo;

import java.time.LocalDate;

/**
 * Usuario que conduce una unidad. Debe tener una licencia vigente para
 * que se le asigne una unidad.
 */
public class Conductor extends Usuario {

    private String numeroLicencia;
    private LocalDate vencimientoLicencia;

    public Conductor(String nombres, String apellidos, String correo, String contrasena, String telefono,
                     String numeroLicencia, LocalDate vencimientoLicencia) {
        super(nombres, apellidos, correo, contrasena, telefono);
        if (numeroLicencia == null || numeroLicencia.trim().isEmpty() || vencimientoLicencia == null) {
            throw new IllegalArgumentException("El conductor debe tener número y fecha de vencimiento de licencia.");
        }
        this.numeroLicencia = numeroLicencia.trim();
        this.vencimientoLicencia = vencimientoLicencia;
    }

    public boolean licenciaVigente(LocalDate fecha) {
        return !vencimientoLicencia.isBefore(fecha);
    }

    @Override
    public String getRol() {
        return "Conductor";
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public LocalDate getVencimientoLicencia() {
        return vencimientoLicencia;
    }
}
