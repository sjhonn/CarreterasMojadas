package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public class ConductorPrudente implements Conductor {
    public double factorVelocidad(TipoClima clima, EstadoCarretera estado) {
        double factor = clima == TipoClima.TORMENTA ? 0.55 : clima == TipoClima.LLUVIA ? 0.68 : clima == TipoClima.NIEBLA ? 0.62 : 0.92;
        return estado == EstadoCarretera.INUNDADA ? Math.min(factor, 0.35) : estado == EstadoCarretera.MUY_MOJADA ? factor * 0.78 : estado == EstadoCarretera.MOJADA ? factor * 0.88 : factor;
    }

    public double distanciaSeguridad() { return 20.0; }
    public double factorRiesgo() { return 0.65; }
    public String nombre() { return "Prudente"; }
}
