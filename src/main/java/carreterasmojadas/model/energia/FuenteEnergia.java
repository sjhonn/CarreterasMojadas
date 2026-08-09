package carreterasmojadas.model.energia;

public interface FuenteEnergia {
    void consumir(double cantidad);
    double nivel();
    double capacidad();
    String tipo();
    default boolean agotada() { return nivel() <= 0.001; }
}
