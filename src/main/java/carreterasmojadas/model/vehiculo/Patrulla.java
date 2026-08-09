package carreterasmojadas.model.vehiculo;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;

public class Patrulla extends Vehiculo {
    public Patrulla(String id, String marca, String modelo, double velocidadMaxima, double peso, FuenteEnergia energia, Conductor conductor) {
        super(id, marca, modelo, velocidadMaxima, peso, TipoVehiculo.PATRULLA, energia, conductor);
    }

    protected double consumoBase() { return 9.2; }
}
