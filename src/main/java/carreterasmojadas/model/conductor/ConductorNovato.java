package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public class ConductorNovato implements Conductor {
    public double factorVelocidad(TipoClima clima, EstadoCarretera estado) {
        double factor = clima == TipoClima.TORMENTA ? 0.58 : clima == TipoClima.LLUVIA ? 0.72 : clima == TipoClima.NIEBLA ? 0.65 : 0.9;
        return estado == EstadoCarretera.INUNDADA ? Math.min(factor, 0.32) : estado == EstadoCarretera.MUY_MOJADA ? factor * 0.78 : estado == EstadoCarretera.MOJADA ? factor * 0.88 : factor;
    }

    public double distanciaSeguridad() { return 16.0; }
    public double factorRiesgo() { return 1.25; }
    public String nombre() { return "Novato"; }
}
