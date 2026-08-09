package carreterasmojadas.model.vehiculo;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;

public class Autobus extends Vehiculo {
    public Autobus(String id, String marca, String modelo, double velocidadMaxima, double peso, FuenteEnergia energia, Conductor conductor) {
        super(id, marca, modelo, velocidadMaxima, peso, TipoVehiculo.AUTOBUS, energia, conductor);
    }

    protected double consumoBase() { return 12.0; }
}
