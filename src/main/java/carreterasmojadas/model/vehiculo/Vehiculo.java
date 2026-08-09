package carreterasmojadas.model.vehiculo;

import carreterasmojadas.enums.EstadoVehiculo;
import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.conductor.Conductor;
import carreterasmojadas.model.energia.FuenteEnergia;
import java.io.Serializable;

public abstract class Vehiculo implements Serializable {
    private final String id;
    private String marca;
    private String modelo;
    private double velocidadMaxima;
    private final double peso;
    private final TipoVehiculo tipo;
    private final FuenteEnergia energia;
    private Conductor conductor;
    private double velocidadActual;
    private double posicion;
    private int carril;
    private double dano;
    private double distanciaRecorrida;
    private EstadoVehiculo estado = EstadoVehiculo.DETENIDO;
    private transient Carretera carreteraActual;
    private String carreteraId;
    private String distritoOrigenId;
    private String distritoDestinoId;
    private String distritoActualId;

    protected Vehiculo(String id, String marca, String modelo, double velocidadMaxima, double peso, TipoVehiculo tipo, FuenteEnergia energia, Conductor conductor) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.velocidadMaxima = velocidadMaxima;
        this.peso = peso;
        this.tipo = tipo;
        this.energia = energia;
        this.conductor = conductor;
    }

    public synchronized void asignarCarretera(Carretera carretera, int carril, double posicion) {
        if (carreteraActual != null) carreteraActual.quitarVehiculo(this);
        carreteraActual = carretera;
        carreteraId = carretera.getId();
        distritoActualId = carretera.getDistritoOrigenId();
        this.carril = Math.max(0, Math.min(carril, carretera.getNumeroCarriles() - 1));
        this.posicion = Math.max(0, posicion);
        carretera.agregarVehiculo(this);
    }

    public synchronized void asignarRuta(String origenId, String destinoId) {
        distritoOrigenId = origenId;
        distritoDestinoId = destinoId;
    }

    public synchronized void actualizarDistritoActual(String distritoId) { distritoActualId = distritoId; }

    public synchronized void actualizarMovimiento(double segundos, double velocidadObjetivo) {
        if (estado == EstadoVehiculo.ACCIDENTADO || estado == EstadoVehiculo.AVERIADO || estado == EstadoVehiculo.FUERA_DE_SERVICIO || energia.agotada()) {
            velocidadActual = 0;
            if (energia.agotada() && estado != EstadoVehiculo.ACCIDENTADO) estado = EstadoVehiculo.SIN_ENERGIA;
            return;
        }
        double aceleracion = velocidadActual < velocidadObjetivo ? 10.0 : 18.0;
        double cambio = aceleracion * segundos;
        if (velocidadActual < velocidadObjetivo) velocidadActual = Math.min(velocidadObjetivo, velocidadActual + cambio);
        else velocidadActual = Math.max(velocidadObjetivo, velocidadActual - cambio);
        estado = velocidadActual > 0.5 ? EstadoVehiculo.CIRCULANDO : EstadoVehiculo.DETENIDO;
        double metros = velocidadActual / 3.6 * segundos;
        posicion += metros;
        distanciaRecorrida += metros / 1000.0;
        double consumo = consumoBase() * Math.max(0.15, metros / 1000.0) * (1.0 + peso / 12000.0);
        energia.consumir(consumo);
    }

    protected abstract double consumoBase();
    public String getId() { return id; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public double getVelocidadMaxima() { return velocidadMaxima; }
    public double getPeso() { return peso; }
    public TipoVehiculo getTipo() { return tipo; }
    public FuenteEnergia getEnergia() { return energia; }
    public Conductor getConductor() { return conductor; }
    public double getVelocidadActual() { return velocidadActual; }
    public double getPosicion() { return posicion; }
    public int getCarril() { return carril; }
    public double getDano() { return dano; }
    public double getDistanciaRecorrida() { return distanciaRecorrida; }
    public EstadoVehiculo getEstado() { return estado; }
    public Carretera getCarreteraActual() { return carreteraActual; }
    public String getCarreteraId() { return carreteraId; }
    public String getDistritoOrigenId() { return distritoOrigenId; }
    public String getDistritoDestinoId() { return distritoDestinoId; }
    public String getDistritoActualId() { return distritoActualId; }
    public void setEstado(EstadoVehiculo estado) { this.estado = estado; }
    public void setVelocidadActual(double velocidadActual) { this.velocidadActual = Math.max(0, Math.min(velocidadActual, velocidadMaxima)); }

    public void actualizarDatos(String marca, String modelo, double velocidadMaxima, Conductor conductor) {
        this.marca = marca;
        this.modelo = modelo;
        this.velocidadMaxima = Math.max(10, velocidadMaxima);
        this.conductor = conductor;
        if (velocidadActual > this.velocidadMaxima) velocidadActual = this.velocidadMaxima;
    }

    public void aplicarDano(double cantidad) {
        dano = Math.max(0, Math.min(100, dano + cantidad));
        if (dano >= 55) estado = EstadoVehiculo.ACCIDENTADO;
    }

    public boolean estaActivo() { return estado == EstadoVehiculo.CIRCULANDO || estado == EstadoVehiculo.EN_EMERGENCIA || estado == EstadoVehiculo.ESPERANDO_SEMAFORO || estado == EstadoVehiculo.EN_ATASCO; }
}
