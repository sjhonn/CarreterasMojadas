package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public class ConductorExperto implements Conductor {
    public double factorVelocidad(TipoClima clima, EstadoCarretera estado) {
        double factor = clima == TipoClima.TORMENTA ? 0.68 : clima == TipoClima.LLUVIA ? 0.80 : clima == TipoClima.NIEBLA ? 0.76 : 1.0;
        return estado == EstadoCarretera.INUNDADA ? Math.min(factor, 0.42) : estado == EstadoCarretera.MUY_MOJADA ? factor * 0.88 : estado == EstadoCarretera.MOJADA ? factor * 0.95 : factor;
    }

    public double distanciaSeguridad() { return 13.0; }
    public double factorRiesgo() { return 0.8; }
    public String nombre() { return "Experto"; }
}
