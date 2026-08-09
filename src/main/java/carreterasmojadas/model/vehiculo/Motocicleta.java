package carreterasmojadas.model.vehiculo;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;

public class Motocicleta extends Vehiculo {
    public Motocicleta(String id, String marca, String modelo, double velocidadMaxima, double peso, FuenteEnergia energia, Conductor conductor) {
        super(id, marca, modelo, velocidadMaxima, peso, TipoVehiculo.MOTOCICLETA, energia, conductor);
    }

    protected double consumoBase() { return 3.4; }
}
