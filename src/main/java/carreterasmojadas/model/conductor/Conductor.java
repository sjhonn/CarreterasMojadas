package carreterasmojadas.model.conductor;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;

public interface Conductor extends java.io.Serializable {
    double factorVelocidad(TipoClima clima, EstadoCarretera estadoCarretera);
    double distanciaSeguridad();
    double factorRiesgo();
    String nombre();
}
