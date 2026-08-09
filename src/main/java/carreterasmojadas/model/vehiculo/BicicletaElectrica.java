package carreterasmojadas.model.vehiculo;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;

public class BicicletaElectrica extends Vehiculo {
    public BicicletaElectrica(String id, String marca, String modelo, double velocidadMaxima, double peso, FuenteEnergia energia, Conductor conductor) {
        super(id, marca, modelo, velocidadMaxima, peso, TipoVehiculo.BICICLETA_ELECTRICA, energia, conductor);
    }

    protected double consumoBase() { return 0.8; }
}
