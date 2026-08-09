package carreterasmojadas.service;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.factory.VehiculoFactory;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.carretera.Semaforo;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.conductor.*;
import carreterasmojadas.model.energia.*;
import carreterasmojadas.model.vehiculo.Vehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class EscenarioInicial {
    private EscenarioInicial() { }

    public static List<Distrito> crearDistritos() {
        return List.of(
                new Distrito("LIM", "Cercado de Lima", 100, 80, 190, 140, 1.28),
                new Distrito("JSM", "Jesús María", 320, 70, 145, 120, 1.05),
                new Distrito("MAG", "Magdalena del Mar", 500, 55, 165, 115, 0.94),
                new Distrito("SIS", "San Isidro", 495, 210, 170, 125, 1.22),
                new Distrito("MIR", "Miraflores", 470, 365, 180, 125, 1.18),
                new Distrito("BAR", "Barranco", 385, 510, 150, 105, 0.91),
                new Distrito("CHO", "Chorrillos", 180, 515, 180, 110, 1.02),
                new Distrito("LVI", "La Victoria", 300, 245, 150, 125, 1.34),
                new Distrito("SBO", "San Borja", 690, 230, 160, 130, 1.06),
                new Distrito("SUR", "Santiago de Surco", 670, 410, 230, 175, 1.12)
        );
    }

    public static List<Carretera> crearCarreteras() {
        return crearCarreteras(crearDistritos());
    }

    public static List<Carretera> crearCarreteras(List<Distrito> distritos) {
        List<Carretera> vias = new ArrayList<>();
        vias.add(via("C-01", "Av. Paseo Colón", 820, 3, 60, distritos, "LIM", "JSM", 245, 110));
        vias.add(via("C-02", "Av. Brasil", 980, 3, 60, distritos, "JSM", "MAG", 430, 58));
        vias.add(via("C-03", "Costa Verde Norte", 1260, 3, 80, distritos, "MAG", "MIR", 430, 250));
        vias.add(via("C-04", "Av. Larco", 760, 2, 50, distritos, "MIR", "SIS", 610, 325));
        vias.add(via("C-05", "Av. Javier Prado", 1240, 4, 70, distritos, "SIS", "SBO", 690, 175));
        vias.add(via("C-06", "Av. Primavera", 920, 3, 60, distritos, "SBO", "SUR", 790, 370));
        vias.add(via("C-07", "Av. Tomás Marsano", 1170, 3, 60, distritos, "SUR", "LVI", 540, 470));
        vias.add(via("C-08", "Av. Grau", 870, 3, 50, distritos, "LVI", "LIM", 255, 260));
        vias.add(via("C-09", "Av. Arequipa", 1380, 3, 60, distritos, "LIM", "SIS", 390, 180));
        vias.add(via("C-10", "Vía Expresa Paseo de la República", 1320, 4, 80, distritos, "LVI", "MIR", 405, 430));
        vias.add(via("C-11", "Av. Angamos", 1030, 3, 60, distritos, "MIR", "SBO", 685, 345));
        vias.add(via("C-12", "Av. Benavides", 1110, 3, 60, distritos, "MIR", "SUR", 585, 470));
        vias.add(via("C-13", "Av. República de Panamá", 940, 3, 60, distritos, "SIS", "BAR", 465, 382));
        vias.add(via("C-14", "Av. Bolognesi", 680, 2, 50, distritos, "BAR", "CHO", 320, 560));
        vias.add(via("C-15", "Costa Verde Sur", 1180, 3, 80, distritos, "CHO", "MIR", 300, 360));
        vias.add(via("C-16", "Av. El Sol", 890, 2, 50, distritos, "BAR", "SUR", 610, 520));
        vias.add(via("C-17", "Av. Salaverry", 1040, 3, 60, distritos, "JSM", "SIS", 420, 190));
        vias.add(via("C-18", "Av. del Ejército", 850, 3, 60, distritos, "MAG", "SIS", 580, 155));
        vias.add(via("C-19", "Av. Petit Thouars", 1180, 3, 60, distritos, "LIM", "MIR", 350, 310));
        vias.add(via("C-20", "Av. Canadá", 920, 3, 60, distritos, "LVI", "SBO", 560, 230));
        vias.add(via("C-21", "Av. Sánchez Carrión", 780, 2, 50, distritos, "JSM", "SIS", 455, 150));
        vias.add(via("C-22", "Av. Parque Sur", 740, 2, 50, distritos, "SBO", "SUR", 730, 335));
        vias.add(via("C-23", "Av. Huaylas", 960, 3, 60, distritos, "CHO", "SUR", 460, 615));
        vias.add(via("C-24", "Circuito de Playas", 1040, 2, 70, distritos, "BAR", "MIR", 360, 405));
        return vias;
    }

    private static Carretera via(String id, String nombre, double longitud, int carriles, double limite,
                                 List<Distrito> distritos, String origenId, String destinoId, int controlX, int controlY) {
        Distrito origen = buscar(distritos, origenId);
        Distrito destino = buscar(distritos, destinoId);
        Carretera carretera = new Carretera(id, nombre, longitud, carriles, limite,
                origenId, destinoId, origen.centroX(), origen.centroY(), destino.centroX(), destino.centroY(), controlX, controlY);
        carretera.getSemaforos().add(new Semaforo("S-" + id + "-1", longitud * 0.36));
        carretera.getSemaforos().add(new Semaforo("S-" + id + "-2", longitud * 0.72));
        if (carriles >= 4) carretera.getSemaforos().add(new Semaforo("S-" + id + "-3", longitud * 0.88));
        return carretera;
    }

    private static Distrito buscar(List<Distrito> distritos, String id) {
        return distritos.stream().filter(d -> d.getId().equals(id)).findFirst().orElseThrow();
    }

    public static List<Vehiculo> crearVehiculos(List<Carretera> carreteras) {
        List<Distrito> distritos = crearDistritos();
        return crearVehiculos(carreteras, distritos);
    }

    public static List<Vehiculo> crearVehiculos(List<Carretera> carreteras, List<Distrito> distritos) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        Random random = new Random(24);
        TipoVehiculo[] tipos = TipoVehiculo.values();
        for (int i = 0; i < 44; i++) {
            TipoVehiculo tipo = tipos[i % tipos.length];
            Conductor conductor = switch (i % 5) {
                case 0 -> new ConductorPrudente();
                case 1 -> new ConductorNormal();
                case 2 -> new ConductorAgresivo();
                case 3 -> new ConductorNovato();
                default -> new ConductorExperto();
            };
            double velocidad = velocidad(tipo);
            double peso = peso(tipo);
            FuenteEnergia energia = energia(tipo);
            String id = prefijo(tipo) + "-" + String.format("%03d", i + 1);
            Vehiculo vehiculo = VehiculoFactory.crear(tipo, id, marca(tipo), "Modelo " + (i + 1), velocidad, peso, energia, conductor);
            Carretera carretera = carreteras.get(i % carreteras.size());
            int carril = i % carretera.getNumeroCarriles();
            double posicion = 30 + (i / carreteras.size()) * 210 + random.nextDouble() * 55;
            vehiculo.asignarCarretera(carretera, carril, Math.min(posicion, carretera.getLongitud() - 35));
            Distrito destino = distritos.get((i * 3 + 4) % distritos.size());
            if (destino.getId().equals(carretera.getDistritoOrigenId())) destino = distritos.get((i * 3 + 5) % distritos.size());
            vehiculo.asignarRuta(carretera.getDistritoOrigenId(), destino.getId());
            vehiculos.add(vehiculo);
        }
        return vehiculos;
    }

    private static FuenteEnergia energia(TipoVehiculo tipo) {
        return switch (tipo) {
            case AUTOMOVIL, MOTOCICLETA, TAXI, AMBULANCIA, PATRULLA -> new Gasolina(55);
            case CAMION, AUTOBUS, BOMBEROS -> new Diesel(110);
            case AUTOMOVIL_ELECTRICO, BICICLETA_ELECTRICA -> new Bateria(80);
            case BICICLETA -> new EnergiaHumana(100);
        };
    }

    private static double velocidad(TipoVehiculo tipo) {
        return switch (tipo) {
            case BICICLETA -> 28;
            case BICICLETA_ELECTRICA -> 35;
            case CAMION -> 75;
            case AUTOBUS -> 70;
            case AMBULANCIA, PATRULLA, BOMBEROS -> 110;
            default -> 95;
        };
    }

    private static double peso(TipoVehiculo tipo) {
        return switch (tipo) {
            case BICICLETA, BICICLETA_ELECTRICA -> 25;
            case MOTOCICLETA -> 190;
            case CAMION -> 8500;
            case AUTOBUS -> 7200;
            case BOMBEROS -> 9800;
            default -> 1450;
        };
    }

    private static String prefijo(TipoVehiculo tipo) {
        return switch (tipo) {
            case AUTOMOVIL -> "AUTO";
            case AUTOMOVIL_ELECTRICO -> "EV";
            case MOTOCICLETA -> "MOTO";
            case BICICLETA -> "BICI";
            case BICICLETA_ELECTRICA -> "EBICI";
            case CAMION -> "CAM";
            case AUTOBUS -> "BUS";
            case TAXI -> "TAXI";
            case AMBULANCIA -> "AMB";
            case PATRULLA -> "PAT";
            case BOMBEROS -> "BOM";
        };
    }

    private static String marca(TipoVehiculo tipo) {
        return switch (tipo) {
            case AUTOMOVIL, TAXI -> "Toyota";
            case AUTOMOVIL_ELECTRICO -> "Nissan";
            case MOTOCICLETA -> "Honda";
            case BICICLETA, BICICLETA_ELECTRICA -> "Trek";
            case CAMION -> "Volvo";
            case AUTOBUS -> "Mercedes-Benz";
            case AMBULANCIA -> "Ford";
            case PATRULLA -> "Kia";
            case BOMBEROS -> "Scania";
        };
    }
}
