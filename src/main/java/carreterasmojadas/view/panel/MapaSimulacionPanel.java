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
import carreterasmojadas.view.dialog.DistritoSimulacionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

public class MapaSimulacionPanel extends JPanel implements Actualizable {
    private static final int MAPA_ANCHO = 1000;
    private static final int MAPA_ALTO = 650;
    private static final double[] PUNTOS_ETIQUETA = {0.31, 0.48, 0.66, 0.79};
    private final MotorSimulacion motor;

    public MapaSimulacionPanel(MotorSimulacion motor) {
        this.motor = motor;
        setPreferredSize(new Dimension(1080, 720));
        setMinimumSize(new Dimension(820, 560));
        setBackground(new Color(228, 229, 224));
        setToolTipText("Un clic selecciona el distrito. Doble clic abre su vista local.");
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Distrito distrito = seleccionarDistrito(e.getX(), e.getY());
                if (distrito != null && e.getClickCount() == 2) {
                    Window owner = SwingUtilities.getWindowAncestor(MapaSimulacionPanel.this);
                    new DistritoSimulacionDialog(owner, motor, distrito).setVisible(true);
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Transformacion t = transformacion();
        List<Rectangle> zonasOcupadas = new ArrayList<>();
        pintarBase(g2, t);
        pintarDistritos(g2, t, zonasOcupadas);
        pintarCarreteras(g2, t, zonasOcupadas);
        pintarAccidentes(g2, t);
        pintarVehiculos(g2, t);
        if (motor.getClimaActual() == TipoClima.LLUVIA || motor.getClimaActual() == TipoClima.TORMENTA) pintarLluvia(g2);
        pintarIndicadores(g2);
        g2.dispose();
    }

    private void pintarBase(Graphics2D g2, Transformacion t) {
        g2.setColor(new Color(236, 236, 231));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(231, 231, 224));
        for (int x = 0; x < getWidth(); x += 44) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 44) g2.drawLine(0, y, getWidth(), y);
        int x = t.x(0);
        int y = t.y(0);
        int ancho = t.x(82) - x;
        int alto = t.y(MAPA_ALTO) - y;
        g2.setColor(new Color(156, 195, 211));
        g2.fillRoundRect(x, y, ancho, alto, 24, 24);
        g2.setColor(new Color(104, 152, 171));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString("OCEANO PACIFICO", x + 8, y + 24);
        g2.setColor(new Color(222, 226, 216));
        g2.fillRoundRect(t.x(105), t.y(38), t.longitud(815), t.longitud(568), 26, 26);
        g2.setColor(new Color(206, 210, 201));
        g2.drawRoundRect(t.x(105), t.y(38), t.longitud(815), t.longitud(568), 26, 26);
    }

    private void pintarDistritos(Graphics2D g2, Transformacion t, List<Rectangle> zonasOcupadas) {
        List<Distrito> distritos = motor.getDistritos();
        for (int i = 0; i < distritos.size(); i++) {
            Distrito distrito = distritos.get(i);
            int x = t.x(distrito.getX());
            int y = t.y(distrito.getY());
            int w = t.longitud(distrito.getAncho());
            int h = t.longitud(distrito.getAlto());
            boolean seleccionado = distrito.getId().equals(motor.getDistritoSeleccionadoId());
            Color base = colorDistrito(i);
            g2.setColor(new Color(180, 180, 172, 70));
            g2.fillRoundRect(x + 4, y + 5, w, h, 18, 18);
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), seleccionado ? 212 : 154));
            g2.fillRoundRect(x, y, w, h, 18, 18);
            g2.setStroke(new BasicStroke(seleccionado ? 2.8f : 1.2f));
            g2.setColor(seleccionado ? new Color(45, 79, 72) : new Color(150, 152, 144));
            g2.drawRoundRect(x, y, w, h, 18, 18);
            pintarManzanas(g2, distrito, t, i);
            int chipW = Math.min(w - 16, Math.max(88, g2.getFontMetrics(g2.getFont().deriveFont(Font.BOLD, seleccionado ? 12f : 11f)).stringWidth(distrito.getNombre()) + 16));
            g2.setColor(new Color(250, 250, 247, 208));
            g2.fillRoundRect(x + 8, y + 8, chipW, 18, 10, 10);
            g2.setColor(new Color(48, 52, 48));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, seleccionado ? 12f : 11f));
            g2.drawString(distrito.getNombre(), x + 14, y + 21);
            int congestion = motor.nivelCongestion(distrito.getId());
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
            g2.drawString("Tráfico " + congestion + "%", x + 10, y + 38);
            zonasOcupadas.add(new Rectangle(x + 6, y + 6, Math.max(72, chipW + 6), 36));
        }
    }

    private void pintarManzanas(Graphics2D g2, Distrito distrito, Transformacion t, int semilla) {
        int x = t.x(distrito.getX());
        int y = t.y(distrito.getY());
        int w = t.longitud(distrito.getAncho());
        int h = t.longitud(distrito.getAlto());
        int fila = 0;
        for (int by = y + 44; by < y + h - 12; by += 23 + (fila % 2) * 4) {
            int columna = 0;
            for (int bx = x + 12 + (fila % 2) * 6; bx < x + w - 12; bx += 24 + ((columna + semilla) % 3) * 3) {
                int bw = 13 + Math.abs((semilla * 7 + fila * 5 + columna * 3) % 8);
                int bh = 9 + Math.abs((semilla * 3 + fila * 4 + columna) % 7);
                bw = Math.min(bw, x + w - bx - 7);
                bh = Math.min(bh, y + h - by - 7);
                if (bw < 6 || bh < 6) continue;
                if ((fila + columna + semilla) % 8 == 0) g2.setColor(new Color(178, 199, 171, 98));
                else g2.setColor(new Color(255, 255, 252, 64));
                g2.fillRoundRect(bx, by, bw, bh, 4, 4);
                columna++;
            }
            fila++;
        }
    }

    private void pintarCarreteras(Graphics2D g2, Transformacion t, List<Rectangle> zonasOcupadas) {
        for (Carretera carretera : motor.getCarreteras()) {
            Shape forma = formaCarretera(carretera, t);
            float ancho = Math.max(7f, carretera.getNumeroCarriles() * 3.2f);
            g2.setStroke(new BasicStroke(ancho + 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(206, 204, 195));
            g2.draw(forma);
            g2.setStroke(new BasicStroke(ancho + 1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(91, 94, 92, 55));
            g2.draw(forma);
            g2.setStroke(new BasicStroke(ancho, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(colorCarretera(carretera));
            g2.draw(forma);
            float[] guiones = {8f, 9f};
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, guiones, 0f));
            g2.setColor(new Color(244, 235, 192, 195));
            g2.draw(forma);
            for (Semaforo semaforo : carretera.getSemaforos()) pintarSemaforo(g2, carretera, semaforo, t);
        }
        for (Carretera carretera : motor.getCarreteras()) pintarNombreVia(g2, carretera, t, zonasOcupadas);
    }

    private Shape formaCarretera(Carretera carretera, Transformacion t) {
        if (carretera.tieneCurva()) {
            return new QuadCurve2D.Double(
                    t.x(carretera.getXInicio()), t.y(carretera.getYInicio()),
                    t.x(carretera.getControlX()), t.y(carretera.getControlY()),
                    t.x(carretera.getXFin()), t.y(carretera.getYFin())
            );
        }
        return new Line2D.Double(t.x(carretera.getXInicio()), t.y(carretera.getYInicio()), t.x(carretera.getXFin()), t.y(carretera.getYFin()));
    }

    private void pintarNombreVia(Graphics2D g2, Carretera carretera, Transformacion t, List<Rectangle> zonasOcupadas) {
        EtiquetaVial etiqueta = ubicarEtiqueta(carretera, t, g2, zonasOcupadas);
        Rectangle r = etiqueta.area;
        g2.setColor(new Color(208, 208, 202, 120));
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width, r.height, 9, 9);
        g2.setColor(new Color(251, 250, 246, 226));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 9, 9);
        g2.setColor(new Color(210, 210, 206));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 9, 9);
        g2.setColor(new Color(60, 61, 58));
        g2.setFont(etiqueta.fuente);
        g2.drawString(carretera.getNombre(), r.x + 6, r.y + r.height - 5);
        zonasOcupadas.add(expandir(r, 4));
    }

    private EtiquetaVial ubicarEtiqueta(Carretera carretera, Transformacion t, Graphics2D g2, List<Rectangle> zonasOcupadas) {
        Font fuente = g2.getFont().deriveFont(Font.BOLD, 9f);
        FontMetrics fm = g2.getFontMetrics(fuente);
        int ancho = fm.stringWidth(carretera.getNombre()) + 12;
        int alto = 17;
        Rectangle mejor = null;
        for (double punto : PUNTOS_ETIQUETA) {
            Point2D.Double base = carretera.puntoEnProporcion(punto);
            Point2D.Double tangente = carretera.tangenteEnProporcion(punto);
            double norma = Math.max(1.0, Math.hypot(tangente.x, tangente.y));
            double nx = -tangente.y / norma;
            double ny = tangente.x / norma;
            for (double offset : new double[]{14, -14, 22, -22, 0}) {
                int mx = t.x(base.x + nx * offset);
                int my = t.y(base.y + ny * offset);
                Rectangle candidato = new Rectangle(mx - ancho / 2, my - alto / 2, ancho, alto);
                if (estaDentro(candidato) && !colisiona(candidato, zonasOcupadas)) return new EtiquetaVial(candidato, fuente);
                if (mejor == null && estaDentro(candidato)) mejor = candidato;
            }
        }
        if (mejor == null) {
            Point2D.Double centro = carretera.puntoEnProporcion(0.55);
            mejor = new Rectangle(t.x(centro.x) - ancho / 2, t.y(centro.y) - alto / 2, ancho, alto);
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

    private boolean estaDentro(Rectangle r) {
        return r.x >= 6 && r.y >= 6 && r.x + r.width <= getWidth() - 6 && r.y + r.height <= getHeight() - 6;
    }

    private void pintarSemaforo(Graphics2D g2, Carretera carretera, Semaforo semaforo, Transformacion t) {
        Point2D.Double punto = carretera.puntoEn(semaforo.getPosicion());
        int x = t.x(punto.x);
        int y = t.y(punto.y);
        Color color = switch (semaforo.getEstado()) {
            case ROJO -> new Color(219, 65, 65);
            case AMARILLO -> new Color(235, 185, 48);
            case VERDE -> new Color(56, 174, 92);
        };
        g2.setColor(new Color(35, 37, 37));
        g2.fillOval(x - 6, y - 6, 12, 12);
        g2.setColor(color);
        g2.fillOval(x - 4, y - 4, 8, 8);
    }

    private void pintarVehiculos(Graphics2D g2, Transformacion t) {
        for (Vehiculo vehiculo : motor.getVehiculos()) {
            Carretera carretera = motor.getCarreteras().stream().filter(c -> c.getId().equals(vehiculo.getCarreteraId())).findFirst().orElse(null);
            if (carretera == null) continue;
            Point2D.Double punto = posicionVehiculo(carretera, vehiculo.getPosicion(), (vehiculo.getCarril() - (carretera.getNumeroCarriles() - 1) / 2.0) * 4.0);
            int x = t.x(punto.x);
            int y = t.y(punto.y);
            int largo = vehiculo.getTipo() == TipoVehiculo.CAMION || vehiculo.getTipo() == TipoVehiculo.AUTOBUS || vehiculo.getTipo() == TipoVehiculo.BOMBEROS ? 18 : 12;
            int alto = vehiculo.getTipo() == TipoVehiculo.BICICLETA || vehiculo.getTipo() == TipoVehiculo.MOTOCICLETA ? 6 : 8;
            g2.setColor(colorVehiculo(vehiculo));
            g2.fillRoundRect(x - largo / 2, y - alto / 2, largo, alto, 5, 5);
            g2.setColor(new Color(28, 30, 30, 180));
            g2.drawRoundRect(x - largo / 2, y - alto / 2, largo, alto, 5, 5);
            if (vehiculo.getEstado() == EstadoVehiculo.EN_EMERGENCIA) {
                g2.setColor(new Color(51, 139, 226, 170));
                g2.drawOval(x - 10, y - 10, 20, 20);
            }
            if (vehiculo.getEstado() == EstadoVehiculo.ACCIDENTADO || vehiculo.getEstado() == EstadoVehiculo.AVERIADO) {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 9f));
                g2.drawString("!", x - 2, y + 3);
            }
        }
    }

    private Point2D.Double posicionVehiculo(Carretera carretera, double posicion, double offset) {
        double p = Math.max(0, Math.min(1, posicion / carretera.getLongitud()));
        Point2D.Double base = carretera.puntoEnProporcion(p);
        Point2D.Double tangente = carretera.tangenteEnProporcion(p);
        double norma = Math.max(1.0, Math.hypot(tangente.x, tangente.y));
        return new Point2D.Double(base.x + (-tangente.y / norma) * offset, base.y + (tangente.x / norma) * offset);
    }

    private void pintarAccidentes(Graphics2D g2, Transformacion t) {
        for (Accidente accidente : motor.getAccidentes()) {
            Carretera carretera = motor.getCarreteras().stream().filter(c -> c.getNombre().equals(accidente.carretera())).findFirst().orElse(null);
            if (carretera == null) continue;
            Point2D.Double punto = carretera.puntoEn(accidente.posicion());
            int x = t.x(punto.x);
            int y = t.y(punto.y);
            g2.setColor(new Color(176, 48, 42));
            g2.fillOval(x - 10, y - 10, 20, 20);
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            g2.drawString("!", x - 2, y + 5);
        }
    }

    private void pintarLluvia(Graphics2D g2) {
        int alpha = motor.getClimaActual() == TipoClima.TORMENTA ? 92 : 62;
        g2.setColor(new Color(55, 112, 160, alpha));
        for (int x = 0; x < getWidth(); x += 25) {
            int offset = (x * 13 + (int) motor.getTicks() * 7) % Math.max(1, getHeight());
            for (int y = -getHeight(); y < getHeight(); y += 82) g2.drawLine(x, y + offset, x - 7, y + offset + 15);
        }
    }

    private void pintarIndicadores(Graphics2D g2) {
        String clima = "Lima | " + motor.getClimaActual();
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(clima) + 24;
        g2.setColor(new Color(52, 58, 57, 228));
        g2.fillRoundRect(getWidth() - w - 16, 14, w, 28, 14, 14);
        g2.setColor(Color.WHITE);
        g2.drawString(clima, getWidth() - w - 4, 33);
        motor.buscarDistrito(motor.getDistritoSeleccionadoId()).ifPresent(d -> {
            String texto = d.getNombre() + " | " + motor.nivelCongestion(d.getId()) + "% trafico | " + motor.vehiculosEnDistrito(d.getId()) + " vehiculos";
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics infoFm = g2.getFontMetrics();
            int infoW = infoFm.stringWidth(texto) + 24;
            g2.setColor(new Color(250, 250, 247, 232));
            g2.fillRoundRect(16, getHeight() - 42, infoW, 28, 14, 14);
            g2.setColor(new Color(45, 52, 49));
            g2.drawString(texto, 28, getHeight() - 23);
        });
    }

    private Color colorDistrito(int i) {
        Color[] colores = {
                new Color(214, 201, 167), new Color(197, 215, 188), new Color(187, 209, 216),
                new Color(207, 196, 220), new Color(220, 199, 183), new Color(197, 214, 204),
                new Color(217, 208, 184), new Color(211, 188, 184), new Color(189, 209, 195), new Color(203, 204, 221)
        };
        return colores[i % colores.length];
    }

    private Color colorCarretera(Carretera carretera) {
        if (carretera.isBloqueada()) return new Color(107, 57, 57);
        return switch (carretera.getEstado()) {
            case SECA -> new Color(82, 84, 84);
            case MOJADA -> new Color(55, 67, 72);
            case MUY_MOJADA -> new Color(44, 57, 66);
            case INUNDADA -> new Color(51, 93, 114);
            case BLOQUEADA -> new Color(107, 57, 57);
        };
    }

    private Color colorVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getEstado() == EstadoVehiculo.ACCIDENTADO) return new Color(156, 48, 48);
        return switch (vehiculo.getTipo()) {
            case AMBULANCIA -> new Color(242, 242, 238);
            case PATRULLA -> new Color(47, 91, 185);
            case BOMBEROS -> new Color(202, 50, 43);
            case AUTOMOVIL_ELECTRICO, BICICLETA_ELECTRICA -> new Color(65, 166, 126);
            case CAMION, AUTOBUS -> new Color(220, 143, 42);
            case MOTOCICLETA, BICICLETA -> new Color(153, 83, 171);
            case TAXI -> new Color(232, 193, 51);
            default -> new Color(63, 132, 196);
        };
    }

    private Distrito seleccionarDistrito(int mouseX, int mouseY) {
        Transformacion t = transformacion();
        int lx = t.logicoX(mouseX);
        int ly = t.logicoY(mouseY);
        Distrito distrito = motor.getDistritos().stream().filter(d -> d.contiene(lx, ly)).findFirst().orElse(null);
        if (distrito != null) {
            motor.seleccionarDistrito(distrito.getId());
            repaint();
        }
        return distrito;
    }

    private Transformacion transformacion() {
        double escala = Math.min((getWidth() - 36.0) / MAPA_ANCHO, (getHeight() - 36.0) / MAPA_ALTO);
        int offsetX = (int) Math.round((getWidth() - MAPA_ANCHO * escala) / 2.0);
        int offsetY = (int) Math.round((getHeight() - MAPA_ALTO * escala) / 2.0);
        return new Transformacion(escala, offsetX, offsetY);
    }

    public void actualizarVista() { repaint(); }

    private record Transformacion(double escala, int offsetX, int offsetY) {
        int x(int valor) { return offsetX + (int) Math.round(valor * escala); }
        int y(int valor) { return offsetY + (int) Math.round(valor * escala); }
        int x(double valor) { return offsetX + (int) Math.round(valor * escala); }
        int y(double valor) { return offsetY + (int) Math.round(valor * escala); }
        int longitud(int valor) { return (int) Math.round(valor * escala); }
        int logicoX(int valor) { return (int) Math.round((valor - offsetX) / escala); }
        int logicoY(int valor) { return (int) Math.round((valor - offsetY) / escala); }
    }

    private record EtiquetaVial(Rectangle area, Font fuente) { }
}
