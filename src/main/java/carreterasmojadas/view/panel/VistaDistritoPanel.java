package carreterasmojadas.view.panel;

import carreterasmojadas.enums.EstadoVehiculo;
import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.accidente.Accidente;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.carretera.Semaforo;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.service.MotorSimulacion;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

public class VistaDistritoPanel extends JPanel implements Actualizable {
    private static final double[] PUNTOS_ETIQUETA = {0.28, 0.45, 0.62, 0.78};
    private final MotorSimulacion motor;
    private String distritoId;

    public VistaDistritoPanel(MotorSimulacion motor) {
        this.motor = motor;
        this.distritoId = motor.getDistritoSeleccionadoId();
        setPreferredSize(new Dimension(720, 480));
        setMinimumSize(new Dimension(420, 320));
        setBackground(new Color(239, 239, 235));
    }

    public void setDistritoId(String distritoId) {
        this.distritoId = distritoId;
        repaint();
    }

    public String getDistritoId() {
        return distritoId;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Distrito distrito = motor.buscarDistrito(distritoId).orElse(null);
        if (distrito == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle marco = crearMarco();
        List<Rectangle> zonasOcupadas = new ArrayList<>();
        pintarMarco(g2, distrito, marco, zonasOcupadas);
        Shape clipAnterior = g2.getClip();
        g2.setClip(marco.x + 1, marco.y + 1, marco.width - 2, marco.height - 2);
        pintarCallesBase(g2, distrito, marco);
        List<Carretera> vias = viasDelDistrito(distrito);
        pintarVias(g2, distrito, vias, marco, zonasOcupadas);
        pintarVehiculos(g2, distrito, marco);
        pintarAccidentes(g2, distrito, marco);
        if (motor.getClimaActual() == TipoClima.LLUVIA || motor.getClimaActual() == TipoClima.TORMENTA) pintarLluvia(g2, marco);
        g2.setClip(clipAnterior);
        pintarEstado(g2, distrito, marco);
        g2.dispose();
    }

    private Rectangle crearMarco() {
        int margenX = 18;
        int margenY = 18;
        int lado = Math.min(getWidth() - margenX * 2, getHeight() - margenY * 2);
        lado = Math.max(180, lado);
        int x = (getWidth() - lado) / 2;
        int y = (getHeight() - lado) / 2;
        return new Rectangle(x, y, lado, lado);
    }

    private void pintarMarco(Graphics2D g2, Distrito distrito, Rectangle marco, List<Rectangle> zonasOcupadas) {
        g2.setColor(new Color(206, 206, 200, 120));
        g2.fillRoundRect(marco.x + 7, marco.y + 8, marco.width, marco.height, 18, 18);
        g2.setColor(new Color(227, 228, 222));
        g2.fillRoundRect(marco.x, marco.y, marco.width, marco.height, 18, 18);
        g2.setColor(new Color(183, 186, 178));
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawRoundRect(marco.x, marco.y, marco.width, marco.height, 18, 18);
        g2.setColor(new Color(42, 47, 45));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        g2.drawString(distrito.getNombre(), marco.x + 14, marco.y + 22);
        zonasOcupadas.add(new Rectangle(marco.x + 8, marco.y + 6, 170, 24));
    }

    private void pintarCallesBase(Graphics2D g2, Distrito distrito, Rectangle marco) {
        int interiorX = marco.x + 14;
        int interiorY = marco.y + 34;
        int interiorW = marco.width - 28;
        int interiorH = marco.height - 48;
        g2.setColor(new Color(229, 230, 223));
        g2.fillRect(interiorX, interiorY, interiorW, interiorH);

        int filas = 5 + Math.abs(distrito.getId().hashCode()) % 3;
        int columnas = 5 + Math.abs(distrito.getNombre().hashCode()) % 3;
        int pasoX = Math.max(24, interiorW / (columnas + 1));
        int pasoY = Math.max(22, interiorH / (filas + 1));
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int bx = interiorX + 10 + columna * pasoX + ((fila + columna) % 2) * 3;
                int by = interiorY + 12 + fila * pasoY + (columna % 2) * 2;
                int bw = Math.min(pasoX - 10, interiorX + interiorW - bx - 6);
                int bh = Math.min(pasoY - 8, interiorY + interiorH - by - 6);
                if (bw < 12 || bh < 10) continue;
                if ((fila + columna + distrito.getId().length()) % 7 == 0) {
                    g2.setColor(new Color(183, 206, 179));
                    g2.fillRoundRect(bx, by, bw, bh, 5, 5);
                } else {
                    g2.setColor(new Color(248, 248, 243));
                    g2.fillRoundRect(bx, by, bw, bh, 5, 5);
                    g2.setColor(new Color(214, 214, 208));
                    g2.drawRoundRect(bx, by, bw, bh, 5, 5);
                }
            }
        }

        Stroke trazoSecundario = new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(trazoSecundario);
        g2.setColor(new Color(237, 237, 232));
        g2.drawLine(interiorX + 16, interiorY + interiorH / 2, interiorX + interiorW - 16, interiorY + interiorH / 2);
        g2.drawLine(interiorX + interiorW / 2, interiorY + 16, interiorX + interiorW / 2, interiorY + interiorH - 16);
        g2.drawLine(interiorX + 32, interiorY + 34, interiorX + interiorW - 34, interiorY + interiorH - 30);
        g2.drawLine(interiorX + 32, interiorY + interiorH - 38, interiorX + interiorW - 42, interiorY + 42);

        double centroX = marco.getCenterX();
        double centroY = marco.getCenterY() + 6;
        double radio = Math.min(marco.width, marco.height) * 0.11;
        g2.setColor(new Color(238, 238, 234));
        g2.fill(new Ellipse2D.Double(centroX - radio, centroY - radio, radio * 2, radio * 2));
        g2.setColor(new Color(217, 217, 210));
        g2.setStroke(new BasicStroke(10f));
        g2.draw(new Ellipse2D.Double(centroX - radio, centroY - radio, radio * 2, radio * 2));
        g2.setColor(new Color(191, 206, 186));
        g2.fill(new Ellipse2D.Double(centroX - 14, centroY - 14, 28, 28));
        g2.setColor(new Color(113, 135, 112));
        g2.draw(new Ellipse2D.Double(centroX - 14, centroY - 14, 28, 28));
    }

    private void pintarVias(Graphics2D g2, Distrito distrito, List<Carretera> vias, Rectangle marco, List<Rectangle> zonasOcupadas) {
        for (Carretera carretera : vias) {
            RutaLocal ruta = rutaLocal(distrito, carretera, marco);
            Shape forma = formaRuta(ruta);
            float ancho = Math.max(10f, carretera.getNumeroCarriles() * 4.2f);
            g2.setStroke(new BasicStroke(ancho + 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(205, 202, 193));
            g2.draw(forma);
            g2.setStroke(new BasicStroke(ancho, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(colorVia(carretera));
            g2.draw(forma);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{7f, 8f}, 0f));
            g2.setColor(new Color(238, 228, 183, 190));
            g2.draw(forma);
            Point2D.Double acceso = ruta.inicio;
            Point2D.Double nodo = ruta.nodo;
            g2.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(108, 112, 109, 115));
            g2.draw(new QuadCurve2D.Double(acceso.x, acceso.y, (acceso.x + nodo.x) / 2.0, (acceso.y + nodo.y) / 2.0, nodo.x, nodo.y));
            g2.setColor(new Color(80, 82, 81));
            g2.fill(new Ellipse2D.Double(acceso.x - 4, acceso.y - 4, 8, 8));
            g2.fill(new Ellipse2D.Double(nodo.x - 4, nodo.y - 4, 8, 8));
            for (Semaforo semaforo : carretera.getSemaforos()) pintarSemaforo(g2, carretera, semaforo, ruta);
        }
        for (Carretera carretera : vias) pintarNombreVia(g2, distrito, carretera, marco, zonasOcupadas);
    }

    private Shape formaRuta(RutaLocal ruta) {
        return new QuadCurve2D.Double(ruta.inicio.x, ruta.inicio.y, ruta.control.x, ruta.control.y, ruta.fin.x, ruta.fin.y);
    }

    private void pintarNombreVia(Graphics2D g2, Distrito distrito, Carretera carretera, Rectangle marco, List<Rectangle> zonasOcupadas) {
        RutaLocal ruta = rutaLocal(distrito, carretera, marco);
        EtiquetaVial etiqueta = ubicarEtiqueta(ruta, carretera.getNombre(), g2, zonasOcupadas, marco);
        Rectangle r = etiqueta.area;
        g2.setColor(new Color(208, 208, 203, 120));
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width, r.height, 8, 8);
        g2.setColor(new Color(249, 248, 243, 228));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g2.setColor(new Color(211, 211, 205));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g2.setColor(new Color(63, 63, 60));
        g2.setFont(etiqueta.fuente);
        g2.drawString(carretera.getNombre(), r.x + 6, r.y + r.height - 5);
        zonasOcupadas.add(expandir(r, 4));
    }

    private EtiquetaVial ubicarEtiqueta(RutaLocal ruta, String nombre, Graphics2D g2, List<Rectangle> zonasOcupadas, Rectangle marco) {
        Font fuente = g2.getFont().deriveFont(Font.PLAIN, 10f);
        FontMetrics fm = g2.getFontMetrics(fuente);
        int ancho = fm.stringWidth(nombre) + 12;
        int alto = 17;
        Rectangle mejor = null;
        for (double punto : PUNTOS_ETIQUETA) {
            Point2D.Double tangente = tangenteRuta(ruta, punto);
            Point2D.Double base = puntoRuta(ruta, punto, 0);
            double norma = Math.max(1.0, Math.hypot(tangente.x, tangente.y));
            double nx = -tangente.y / norma;
            double ny = tangente.x / norma;
            for (double offset : new double[]{14, -14, 22, -22, 0}) {
                int mx = (int) Math.round(base.x + nx * offset);
                int my = (int) Math.round(base.y + ny * offset);
                Rectangle candidato = new Rectangle(mx - ancho / 2, my - alto / 2, ancho, alto);
                if (dentroMarco(candidato, marco) && !colisiona(candidato, zonasOcupadas)) return new EtiquetaVial(candidato, fuente);
                if (mejor == null && dentroMarco(candidato, marco)) mejor = candidato;
            }
        }
        if (mejor == null) {
            Point2D.Double punto = puntoRuta(ruta, 0.58, 0);
            mejor = new Rectangle((int) Math.round(punto.x) - ancho / 2, (int) Math.round(punto.y) - alto / 2, ancho, alto);
        }
        return new EtiquetaVial(mejor, fuente);
    }

    private boolean colisiona(Rectangle candidato, List<Rectangle> zonasOcupadas) {
        Rectangle prueba = expandir(candidato, 3);
        for (Rectangle ocupada : zonasOcupadas) {
            if (prueba.intersects(ocupada)) return true;
        }
        return false;
    }

    private Rectangle expandir(Rectangle base, int margen) {
        return new Rectangle(base.x - margen, base.y - margen, base.width + margen * 2, base.height + margen * 2);
    }

    private boolean dentroMarco(Rectangle r, Rectangle marco) {
        return r.x >= marco.x + 8 && r.y >= marco.y + 26 && r.x + r.width <= marco.x + marco.width - 8 && r.y + r.height <= marco.y + marco.height - 8;
    }

    private void pintarSemaforo(Graphics2D g2, Carretera carretera, Semaforo semaforo, RutaLocal ruta) {
        double p = Math.max(0, Math.min(1, semaforo.getPosicion() / carretera.getLongitud()));
        Point2D.Double punto = puntoRuta(ruta, p, 0);
        Color luz = switch (semaforo.getEstado()) {
            case ROJO -> new Color(203, 63, 58);
            case AMARILLO -> new Color(222, 167, 45);
            case VERDE -> new Color(67, 153, 82);
        };
        int x = (int) Math.round(punto.x);
        int y = (int) Math.round(punto.y);
        g2.setColor(new Color(41, 43, 42));
        g2.fillRect(x - 5, y - 5, 10, 10);
        g2.setColor(luz);
        g2.fillOval(x - 3, y - 3, 6, 6);
    }

    private void pintarVehiculos(Graphics2D g2, Distrito distrito, Rectangle marco) {
        for (Vehiculo vehiculo : motor.getVehiculos()) {
            if (!distrito.getId().equals(vehiculo.getDistritoActualId())) continue;
            Carretera carretera = motor.getCarreteras().stream().filter(c -> c.getId().equals(vehiculo.getCarreteraId())).findFirst().orElse(null);
            if (carretera == null) continue;
            RutaLocal ruta = rutaLocal(distrito, carretera, marco);
            double avance = Math.max(0, Math.min(1, vehiculo.getPosicion() / carretera.getLongitud()));
            if (distrito.getId().equals(carretera.getDistritoDestinoId())) avance = 1 - avance;
            double offset = (vehiculo.getCarril() - (carretera.getNumeroCarriles() - 1) / 2.0) * 5.0;
            Point2D.Double punto = puntoRuta(ruta, avance, offset);
            int x = (int) Math.round(punto.x);
            int y = (int) Math.round(punto.y);
            int largo = vehiculo.getTipo() == TipoVehiculo.CAMION || vehiculo.getTipo() == TipoVehiculo.AUTOBUS || vehiculo.getTipo() == TipoVehiculo.BOMBEROS ? 20 : 14;
            int alto = vehiculo.getTipo() == TipoVehiculo.BICICLETA || vehiculo.getTipo() == TipoVehiculo.MOTOCICLETA ? 6 : 9;
            g2.setColor(colorVehiculo(vehiculo));
            g2.fillRoundRect(x - largo / 2, y - alto / 2, largo, alto, 5, 5);
            g2.setColor(new Color(42, 43, 42, 170));
            g2.drawRoundRect(x - largo / 2, y - alto / 2, largo, alto, 5, 5);
            if (vehiculo.getEstado() == EstadoVehiculo.EN_EMERGENCIA) {
                g2.setColor(new Color(55, 126, 203));
                g2.drawOval(x - 11, y - 11, 22, 22);
            }
        }
    }

    private void pintarAccidentes(Graphics2D g2, Distrito distrito, Rectangle marco) {
        for (Accidente accidente : motor.getAccidentes()) {
            if (!distrito.getId().equals(accidente.distritoId())) continue;
            Carretera carretera = motor.getCarreteras().stream().filter(c -> c.getNombre().equals(accidente.carretera())).findFirst().orElse(null);
            if (carretera == null) continue;
            RutaLocal ruta = rutaLocal(distrito, carretera, marco);
            double avance = Math.max(0, Math.min(1, accidente.posicion() / carretera.getLongitud()));
            if (distrito.getId().equals(carretera.getDistritoDestinoId())) avance = 1 - avance;
            Point2D.Double punto = puntoRuta(ruta, avance, 0);
            int x = (int) Math.round(punto.x);
            int y = (int) Math.round(punto.y);
            g2.setColor(new Color(175, 57, 51));
            g2.fillOval(x - 9, y - 9, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            g2.drawString("!", x - 2, y + 4);
        }
    }

    private void pintarLluvia(Graphics2D g2, Rectangle marco) {
        int alpha = motor.getClimaActual() == TipoClima.TORMENTA ? 86 : 52;
        g2.setColor(new Color(67, 116, 151, alpha));
        for (int x = marco.x + 8; x < marco.x + marco.width; x += 24) {
            int offset = (x * 9 + (int) motor.getTicks() * 6) % Math.max(1, marco.height);
            for (int y = marco.y - marco.height; y < marco.y + marco.height; y += 72) g2.drawLine(x, y + offset, x - 6, y + offset + 13);
        }
    }

    private void pintarEstado(Graphics2D g2, Distrito distrito, Rectangle marco) {
        String linea = motor.getClimaActual() + "  |  " + motor.vehiculosEnDistrito(distrito.getId()) + " vehículos  |  " + motor.nivelCongestion(distrito.getId()) + "% congestión";
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();
        int ancho = fm.stringWidth(linea) + 18;
        int x = Math.max(10, marco.x + (marco.width - ancho) / 2);
        int y = Math.min(getHeight() - 34, marco.y + marco.height - 30);
        g2.setColor(new Color(37, 42, 41, 220));
        g2.fillRoundRect(x, y, ancho, 23, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(linea, x + 9, y + 16);
    }

    private List<Carretera> viasDelDistrito(Distrito distrito) {
        return motor.getCarreteras().stream()
                .filter(c -> distrito.getId().equals(c.getDistritoOrigenId()) || distrito.getId().equals(c.getDistritoDestinoId()))
                .toList();
    }

    private RutaLocal rutaLocal(Distrito distrito, Carretera carretera, Rectangle marco) {
        Distrito vecino = motor.buscarDistrito(distrito.getId().equals(carretera.getDistritoOrigenId()) ? carretera.getDistritoDestinoId() : carretera.getDistritoOrigenId()).orElse(distrito);
        double origenX = distrito.centroX();
        double origenY = distrito.centroY();
        double vecinoX = vecino.centroX();
        double vecinoY = vecino.centroY();
        double dx = vecinoX - origenX;
        double dy = vecinoY - origenY;
        double norma = Math.max(1.0, Math.hypot(dx, dy));
        dx /= norma;
        dy /= norma;
        double cx = marco.getCenterX();
        double cy = marco.getCenterY() + 6;
        double radioNodo = Math.min(marco.width, marco.height) * 0.12;
        double radioInicio = Math.min(marco.width, marco.height) * 0.2;
        Point2D.Double nodo = new Point2D.Double(cx + dx * radioNodo, cy + dy * radioNodo);
        Point2D.Double inicio = new Point2D.Double(cx + dx * radioInicio, cy + dy * radioInicio);
        double borde = distanciaBorde(marco, cx, cy, dx, dy) - 16;
        Point2D.Double fin = new Point2D.Double(cx + dx * borde, cy + dy * borde);
        double curvatura = carretera.tieneCurva() ? 26 : 14;
        double sentido = ((carretera.getId().hashCode() & 1) == 0 ? 1 : -1);
        Point2D.Double control = new Point2D.Double(
                (inicio.x + fin.x) / 2.0 - dy * curvatura * sentido,
                (inicio.y + fin.y) / 2.0 + dx * curvatura * sentido
        );
        return new RutaLocal(inicio, control, fin, nodo);
    }

    private double distanciaBorde(Rectangle marco, double cx, double cy, double dx, double dy) {
        double izquierda = marco.x + 14;
        double derecha = marco.x + marco.width - 14;
        double arriba = marco.y + 22;
        double abajo = marco.y + marco.height - 14;
        double tx = dx > 0 ? (derecha - cx) / dx : dx < 0 ? (izquierda - cx) / dx : Double.POSITIVE_INFINITY;
        double ty = dy > 0 ? (abajo - cy) / dy : dy < 0 ? (arriba - cy) / dy : Double.POSITIVE_INFINITY;
        double candidatoX = tx > 0 ? tx : Double.POSITIVE_INFINITY;
        double candidatoY = ty > 0 ? ty : Double.POSITIVE_INFINITY;
        return Math.min(candidatoX, candidatoY);
    }

    private Point2D.Double puntoRuta(RutaLocal ruta, double progreso, double offset) {
        double p = Math.max(0, Math.min(1, progreso));
        double unoMenos = 1 - p;
        double x = unoMenos * unoMenos * ruta.inicio.x + 2 * unoMenos * p * ruta.control.x + p * p * ruta.fin.x;
        double y = unoMenos * unoMenos * ruta.inicio.y + 2 * unoMenos * p * ruta.control.y + p * p * ruta.fin.y;
        Point2D.Double tangente = tangenteRuta(ruta, p);
        double norma = Math.max(1.0, Math.hypot(tangente.x, tangente.y));
        return new Point2D.Double(x + (-tangente.y / norma) * offset, y + (tangente.x / norma) * offset);
    }

    private Point2D.Double tangenteRuta(RutaLocal ruta, double progreso) {
        double p = Math.max(0, Math.min(1, progreso));
        double tx = 2 * (1 - p) * (ruta.control.x - ruta.inicio.x) + 2 * p * (ruta.fin.x - ruta.control.x);
        double ty = 2 * (1 - p) * (ruta.control.y - ruta.inicio.y) + 2 * p * (ruta.fin.y - ruta.control.y);
        return new Point2D.Double(tx, ty);
    }

    private Color colorVia(Carretera carretera) {
        if (carretera.isBloqueada()) return new Color(104, 57, 54);
        return switch (carretera.getEstado()) {
            case SECA -> new Color(76, 78, 78);
            case MOJADA -> new Color(57, 67, 71);
            case MUY_MOJADA -> new Color(45, 57, 64);
            case INUNDADA -> new Color(55, 94, 111);
            case BLOQUEADA -> new Color(104, 57, 54);
        };
    }

    private Color colorVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getEstado() == EstadoVehiculo.ACCIDENTADO) return new Color(165, 55, 49);
        return switch (vehiculo.getTipo()) {
            case AMBULANCIA -> new Color(238, 238, 234);
            case PATRULLA -> new Color(50, 89, 168);
            case BOMBEROS -> new Color(191, 55, 47);
            case AUTOMOVIL_ELECTRICO, BICICLETA_ELECTRICA -> new Color(74, 144, 105);
            case CAMION, AUTOBUS -> new Color(196, 126, 43);
            case MOTOCICLETA, BICICLETA -> new Color(122, 87, 151);
            case TAXI -> new Color(221, 180, 44);
            default -> new Color(62, 111, 157);
        };
    }

    public void actualizarVista() {
        repaint();
    }

    private record RutaLocal(Point2D.Double inicio, Point2D.Double control, Point2D.Double fin, Point2D.Double nodo) { }
    private record EtiquetaVial(Rectangle area, Font fuente) { }
}
