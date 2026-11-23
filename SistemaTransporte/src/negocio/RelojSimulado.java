package negocio;

import java.time.LocalDateTime;

/**
 * Reloj del sistema en modo consola. Permite simular el paso del tiempo
 * para demostrar el envío de ubicaciones cada 5 segundos y la pérdida
 * de señal sin esperar en tiempo real.
 */
public class RelojSimulado {

    private LocalDateTime ahora;

    public RelojSimulado(LocalDateTime inicio) {
        this.ahora = inicio;
    }

    public LocalDateTime ahora() {
        return ahora;
    }

    public void avanzarSegundos(long segundos) {
        ahora = ahora.plusSeconds(segundos);
    }
}
