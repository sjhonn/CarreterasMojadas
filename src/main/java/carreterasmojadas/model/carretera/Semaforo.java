package carreterasmojadas.model.carretera;

import carreterasmojadas.enums.EstadoSemaforo;
import java.io.Serializable;

public class Semaforo implements Serializable {
    private final String id;
    private final double posicion;
    private EstadoSemaforo estado = EstadoSemaforo.VERDE;
    private int ticks = 0;
    private int verde = 40;
    private int amarillo = 12;
    private int rojo = 40;

    public Semaforo(String id, double posicion) {
        this.id = id;
        this.posicion = posicion;
    }

    public void actualizar() {
        ticks++;
        int limite = switch (estado) {
            case VERDE -> verde;
            case AMARILLO -> amarillo;
            case ROJO -> rojo;
        };
        if (ticks >= limite) {
            ticks = 0;
            estado = switch (estado) {
                case VERDE -> EstadoSemaforo.AMARILLO;
                case AMARILLO -> EstadoSemaforo.ROJO;
                case ROJO -> EstadoSemaforo.VERDE;
            };
        }
    }

    public String getId() { return id; }
    public double getPosicion() { return posicion; }
    public EstadoSemaforo getEstado() { return estado; }
    public void setDuraciones(int verde, int amarillo, int rojo) {
        this.verde = Math.max(5, verde);
        this.amarillo = Math.max(2, amarillo);
        this.rojo = Math.max(5, rojo);
    }
}
