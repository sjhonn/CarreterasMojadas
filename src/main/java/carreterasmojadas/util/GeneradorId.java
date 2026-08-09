package carreterasmojadas.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class GeneradorId {
    private static final AtomicInteger VEHICULOS = new AtomicInteger(100);
    private static final AtomicInteger ACCIDENTES = new AtomicInteger(1);
    private GeneradorId() { }
    public static String vehiculo(String prefijo) { return prefijo + "-" + String.format("%03d", VEHICULOS.getAndIncrement()); }
    public static String accidente() { return "ACC-" + String.format("%04d", ACCIDENTES.getAndIncrement()); }
}
