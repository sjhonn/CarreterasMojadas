package carreterasmojadas.model.ciudad;

import java.io.Serializable;

public class Distrito implements Serializable {
    private final String id;
    private final String nombre;
    private final int x;
    private final int y;
    private final int ancho;
    private final int alto;
    private final double factorTrafico;

    public Distrito(String id, String nombre, int x, int y, int ancho, int alto, double factorTrafico) {
        this.id = id;
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.factorTrafico = factorTrafico;
    }

    public boolean contiene(int px, int py) {
        return px >= x && px <= x + ancho && py >= y && py <= y + alto;
    }

    public int centroX() { return x + ancho / 2; }
    public int centroY() { return y + alto / 2; }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public double getFactorTrafico() { return factorTrafico; }

    public String toString() { return nombre; }
}
