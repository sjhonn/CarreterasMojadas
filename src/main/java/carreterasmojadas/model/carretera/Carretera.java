package carreterasmojadas.model.carretera;

import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.model.vehiculo.Vehiculo;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Carretera implements Serializable {
    private final String id;
    private final String nombre;
    private final double longitud;
    private final int numeroCarriles;
    private final double limiteVelocidad;
    private final String distritoOrigenId;
    private final String distritoDestinoId;
    private final int xInicio;
    private final int yInicio;
    private final int xFin;
    private final int yFin;
    private final int controlX;
    private final int controlY;
    private final boolean curva;
    private EstadoCarretera estado = EstadoCarretera.SECA;
    private TipoClima clima = TipoClima.SOLEADO;
    private boolean bloqueada;
    private final List<Semaforo> semaforos = new ArrayList<>();
    private transient List<Vehiculo> vehiculos = new ArrayList<>();

    public Carretera(String id, String nombre, double longitud, int numeroCarriles, double limiteVelocidad) {
        this(id, nombre, longitud, numeroCarriles, limiteVelocidad, null, null, 0, 0, 1000, 0);
    }

    public Carretera(String id, String nombre, double longitud, int numeroCarriles, double limiteVelocidad,
                     String distritoOrigenId, String distritoDestinoId,
                     int xInicio, int yInicio, int xFin, int yFin) {
        this(id, nombre, longitud, numeroCarriles, limiteVelocidad, distritoOrigenId, distritoDestinoId,
                xInicio, yInicio, xFin, yFin, (xInicio + xFin) / 2, (yInicio + yFin) / 2, false);
    }

    public Carretera(String id, String nombre, double longitud, int numeroCarriles, double limiteVelocidad,
                     String distritoOrigenId, String distritoDestinoId,
                     int xInicio, int yInicio, int xFin, int yFin, int controlX, int controlY) {
        this(id, nombre, longitud, numeroCarriles, limiteVelocidad, distritoOrigenId, distritoDestinoId,
                xInicio, yInicio, xFin, yFin, controlX, controlY, true);
    }

    private Carretera(String id, String nombre, double longitud, int numeroCarriles, double limiteVelocidad,
                      String distritoOrigenId, String distritoDestinoId,
                      int xInicio, int yInicio, int xFin, int yFin,
                      int controlX, int controlY, boolean curva) {
        this.id = id;
        this.nombre = nombre;
        this.longitud = longitud;
        this.numeroCarriles = numeroCarriles;
        this.limiteVelocidad = limiteVelocidad;
        this.distritoOrigenId = distritoOrigenId;
        this.distritoDestinoId = distritoDestinoId;
        this.xInicio = xInicio;
        this.yInicio = yInicio;
        this.xFin = xFin;
        this.yFin = yFin;
        this.controlX = controlX;
        this.controlY = controlY;
        this.curva = curva;
    }

    public void aplicarClima(TipoClima clima) {
        this.clima = clima;
        if (bloqueada) {
            estado = EstadoCarretera.BLOQUEADA;
            return;
        }
        estado = switch (clima) {
            case LLUVIA -> EstadoCarretera.MOJADA;
            case TORMENTA -> EstadoCarretera.MUY_MOJADA;
            default -> EstadoCarretera.SECA;
        };
    }

    public synchronized void reconstruirVehiculos() { vehiculos = new ArrayList<>(); }
    public synchronized void agregarVehiculo(Vehiculo vehiculo) { if (!vehiculos.contains(vehiculo)) vehiculos.add(vehiculo); }
    public synchronized void quitarVehiculo(Vehiculo vehiculo) { vehiculos.remove(vehiculo); }

    public Point2D.Double puntoEn(double distancia) {
        return puntoEnProporcion(Math.max(0, Math.min(1, distancia / Math.max(1.0, longitud))));
    }

    public Point2D.Double puntoEnProporcion(double t) {
        double p = Math.max(0, Math.min(1, t));
        if (!curva) {
            return new Point2D.Double(xInicio + (xFin - xInicio) * p, yInicio + (yFin - yInicio) * p);
        }
        double unoMenos = 1 - p;
        double x = unoMenos * unoMenos * xInicio + 2 * unoMenos * p * controlX + p * p * xFin;
        double y = unoMenos * unoMenos * yInicio + 2 * unoMenos * p * controlY + p * p * yFin;
        return new Point2D.Double(x, y);
    }

    public Point2D.Double tangenteEnProporcion(double t) {
        double p = Math.max(0, Math.min(1, t));
        if (!curva) {
            return new Point2D.Double(xFin - xInicio, yFin - yInicio);
        }
        double x = 2 * (1 - p) * (controlX - xInicio) + 2 * p * (xFin - controlX);
        double y = 2 * (1 - p) * (controlY - yInicio) + 2 * p * (yFin - controlY);
        return new Point2D.Double(x, y);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getLongitud() { return longitud; }
    public int getNumeroCarriles() { return numeroCarriles; }
    public double getLimiteVelocidad() { return limiteVelocidad; }
    public EstadoCarretera getEstado() { return estado; }
    public TipoClima getClima() { return clima; }
    public List<Semaforo> getSemaforos() { return semaforos; }
    public synchronized List<Vehiculo> getVehiculos() { return List.copyOf(vehiculos); }
    public boolean isBloqueada() { return bloqueada; }
    public String getDistritoOrigenId() { return distritoOrigenId; }
    public String getDistritoDestinoId() { return distritoDestinoId; }
    public int getXInicio() { return xInicio; }
    public int getYInicio() { return yInicio; }
    public int getXFin() { return xFin; }
    public int getYFin() { return yFin; }
    public int getControlX() { return controlX; }
    public int getControlY() { return controlY; }
    public boolean tieneCurva() { return curva; }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
        estado = bloqueada ? EstadoCarretera.BLOQUEADA : EstadoCarretera.SECA;
    }
}
