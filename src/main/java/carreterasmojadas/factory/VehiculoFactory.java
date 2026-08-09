package carreterasmojadas.factory;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;
import carreterasmojadas.model.vehiculo.*;

public final class VehiculoFactory {
    private VehiculoFactory() { }

    public static Vehiculo crear(TipoVehiculo tipo, String id, String marca, String modelo, double velocidadMaxima, double peso, FuenteEnergia energia, Conductor conductor) {
        return switch (tipo) {
            case AUTOMOVIL -> new Automovil(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case AUTOMOVIL_ELECTRICO -> new AutomovilElectrico(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case MOTOCICLETA -> new Motocicleta(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case BICICLETA -> new Bicicleta(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case BICICLETA_ELECTRICA -> new BicicletaElectrica(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case CAMION -> new Camion(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case AUTOBUS -> new Autobus(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case TAXI -> new Taxi(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case AMBULANCIA -> new Ambulancia(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case PATRULLA -> new Patrulla(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
            case BOMBEROS -> new Bomberos(id, marca, modelo, velocidadMaxima, peso, energia, conductor);
        };
    }
}
