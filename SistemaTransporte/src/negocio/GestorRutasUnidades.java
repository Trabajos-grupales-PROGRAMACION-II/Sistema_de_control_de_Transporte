package negocio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.Conductor;
import modelo.EstadoUnidad;
import modelo.Parada;
import modelo.Ruta;
import modelo.Unidad;

/**
 * Reglas de negocio del módulo 2, Rutas y unidades:
 *  - RF4 Registro, consulta y eliminación de rutas con sus paradas
 *  - RF5 Registro de unidades y asignación de conductor y ruta
 */
public class GestorRutasUnidades {

    private List<Ruta> rutas;
    private List<Unidad> unidades;

    public GestorRutasUnidades() {
        this.rutas = new ArrayList<>();
        this.unidades = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // RF4 Rutas y paradas
    // ------------------------------------------------------------------

    public Ruta registrarRuta(String codigo, String nombre, String origen, String destino,
                              LocalTime horaInicio, LocalTime horaFin, List<Parada> paradas) {
        if (codigo == null || codigo.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("Complete el código y el nombre de la ruta.");
        }
        if (buscarRutaOpcional(codigo) != null) {
            throw new ValidacionException("El código de ruta ya existe.");
        }
        if (paradas == null || paradas.size() < 2) {
            throw new ValidacionException("La ruta debe tener al menos dos paradas.");
        }
        Ruta ruta = new Ruta(codigo.trim().toUpperCase(), nombre.trim(), origen, destino, horaInicio, horaFin);
        for (Parada p : paradas) {
            ruta.agregarParada(p);
        }
        rutas.add(ruta);
        return ruta;
    }

    public void eliminarRuta(String codigo) {
        Ruta ruta = buscarRuta(codigo);
        for (Unidad u : unidades) {
            if (u.getRuta() == ruta) {
                throw new ValidacionException("No se puede eliminar: la unidad " + u.getPlaca()
                        + " tiene asignada esta ruta.");
            }
        }
        rutas.remove(ruta);
    }

    public Ruta buscarRuta(String codigo) {
        Ruta ruta = buscarRutaOpcional(codigo);
        if (ruta == null) {
            throw new ValidacionException("No existe la ruta " + codigo + ".");
        }
        return ruta;
    }

    public List<Ruta> listarRutas() {
        return Collections.unmodifiableList(rutas);
    }

    // ------------------------------------------------------------------
    // RF5 Unidades y asignación
    // ------------------------------------------------------------------

    public Unidad registrarUnidad(String placa, int numeroUnidad, int capacidad) {
        Unidad nueva = new Unidad(placa, numeroUnidad, capacidad);
        for (Unidad u : unidades) {
            if (u.getPlaca().equals(nueva.getPlaca())) {
                throw new ValidacionException("La placa ya está registrada.");
            }
            if (u.getNumeroUnidad() == numeroUnidad) {
                throw new ValidacionException("El número de unidad ya está registrado.");
            }
        }
        unidades.add(nueva);
        return nueva;
    }

    /**
     * Asigna un conductor y una ruta a la unidad. El conductor debe estar
     * activo, tener la licencia vigente y no estar asignado a otra unidad.
     */
    public void asignar(String placa, Conductor conductor, String codigoRuta, LocalDate hoy) {
        Unidad unidad = buscarUnidad(placa);
        if (unidad.estaEnOperacion()) {
            throw new ValidacionException("No se puede reasignar una unidad que está en recorrido.");
        }
        if (conductor == null || codigoRuta == null || codigoRuta.trim().isEmpty()) {
            throw new ValidacionException("Seleccione un conductor y una ruta.");
        }
        if (!conductor.isActivo()) {
            throw new ValidacionException("El conductor está desactivado.");
        }
        if (!conductor.licenciaVigente(hoy)) {
            throw new ValidacionException("La licencia del conductor está vencida desde "
                    + conductor.getVencimientoLicencia() + ".");
        }
        for (Unidad u : unidades) {
            if (u != unidad && u.getConductor() == conductor) {
                throw new ValidacionException("El conductor ya tiene una unidad asignada (" + u.getPlaca() + ").");
            }
        }
        Ruta ruta = buscarRuta(codigoRuta);
        unidad.setConductor(conductor);
        unidad.setRuta(ruta);
    }

    public void cambiarServicio(String placa, boolean fueraDeServicio) {
        Unidad unidad = buscarUnidad(placa);
        if (unidad.estaEnOperacion()) {
            throw new ValidacionException("La unidad está en recorrido; finalice el recorrido primero.");
        }
        unidad.setEstado(fueraDeServicio ? EstadoUnidad.FUERA_DE_SERVICIO : EstadoUnidad.DISPONIBLE);
    }

    public void eliminarUnidad(String placa) {
        Unidad unidad = buscarUnidad(placa);
        if (unidad.estaEnOperacion()) {
            throw new ValidacionException("No se puede eliminar una unidad en recorrido.");
        }
        unidades.remove(unidad);
    }

    public Unidad buscarUnidad(String placa) {
        if (placa != null) {
            for (Unidad u : unidades) {
                if (u.getPlaca().equalsIgnoreCase(placa.trim())) {
                    return u;
                }
            }
        }
        throw new ValidacionException("No existe una unidad con la placa " + placa + ".");
    }

    public Unidad buscarUnidadDeConductor(Conductor conductor) {
        for (Unidad u : unidades) {
            if (u.getConductor() == conductor) {
                return u;
            }
        }
        throw new ValidacionException("No tiene una unidad asignada.");
    }

    public List<Unidad> listarUnidades() {
        return Collections.unmodifiableList(unidades);
    }

    private Ruta buscarRutaOpcional(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Ruta r : rutas) {
            if (r.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return r;
            }
        }
        return null;
    }
}
