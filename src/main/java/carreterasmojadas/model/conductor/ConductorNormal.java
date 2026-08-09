package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public class ConductorNormal implements Conductor {
    public double factorVelocidad(TipoClima clima, EstadoCarretera estado) {
        double factor = clima == TipoClima.TORMENTA ? 0.62 : clima == TipoClima.LLUVIA ? 0.76 : clima == TipoClima.NIEBLA ? 0.70 : 1.0;
        return estado == EstadoCarretera.INUNDADA ? Math.min(factor, 0.38) : estado == EstadoCarretera.MUY_MOJADA ? factor * 0.82 : estado == EstadoCarretera.MOJADA ? factor * 0.92 : factor;
    }

    public double distanciaSeguridad() { return 14.0; }
    public double factorRiesgo() { return 1.0; }
    public String nombre() { return "Normal"; }
}
