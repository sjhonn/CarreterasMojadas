package carreterasmojadas.model.energia;

import java.io.Serializable;

public abstract class EnergiaBase implements FuenteEnergia, Serializable {
    private final double capacidad;
    private double nivel;

    protected EnergiaBase(double capacidad) {
        this.capacidad = Math.max(1.0, capacidad);
        this.nivel = this.capacidad;
    }

    public void consumir(double cantidad) {
        nivel = Math.max(0.0, nivel - Math.max(0.0, cantidad));
    }

    public double nivel() { return nivel; }
    public double capacidad() { return capacidad; }
    public double porcentaje() { return (nivel / capacidad) * 100.0; }
}
