package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public class ConductorAgresivo implements Conductor {
    public double factorVelocidad(TipoClima clima, EstadoCarretera estado) {
        double factor = clima == TipoClima.TORMENTA ? 0.75 : clima == TipoClima.LLUVIA ? 0.88 : clima == TipoClima.NIEBLA ? 0.82 : 1.08;
        return estado == EstadoCarretera.INUNDADA ? Math.min(factor, 0.45) : estado == EstadoCarretera.MUY_MOJADA ? factor * 0.90 : estado == EstadoCarretera.MOJADA ? factor * 0.97 : factor;
    }

    public double distanciaSeguridad() { return 8.0; }
    public double factorRiesgo() { return 1.65; }
    public String nombre() { return "Agresivo"; }
}
