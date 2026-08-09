package carreterasmojadas.model.clima;

import carreterasmojadas.enums.TipoClima;
import java.io.Serializable;
import java.util.Random;

public class ControladorClima implements Serializable {
    private TipoClima climaActual = TipoClima.SOLEADO;
    private transient Random random = new Random();

    public TipoClima getClimaActual() { return climaActual; }
    public void setClimaActual(TipoClima climaActual) { this.climaActual = climaActual; }

    public boolean intentarCambioAutomatico(double probabilidad) {
        if (random == null) random = new Random();
        if (random.nextDouble() >= probabilidad) return false;
        TipoClima anterior = climaActual;
        TipoClima[] valores = TipoClima.values();
        do {
            climaActual = valores[random.nextInt(valores.length)];
        } while (climaActual == anterior && valores.length > 1);
        return true;
    }
}
