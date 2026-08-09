package carreterasmojadas.view.panel;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.service.Estadisticas;
import carreterasmojadas.service.MotorSimulacion;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class EstadisticasPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final JTextArea resumen = new JTextArea();
    private final GraficoResumen grafico;

    public EstadisticasPanel(MotorSimulacion motor) {
        this.motor = motor;
        grafico = new GraficoResumen(motor);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        resumen.setEditable(false);
        resumen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resumen.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(resumen), BorderLayout.WEST);
        add(grafico, BorderLayout.CENTER);
        actualizarVista();
    }

    public void actualizarVista() {
        Estadisticas e = motor.estadisticas();
        StringBuilder texto = new StringBuilder();
        texto.append("Vehiculos totales      ").append(e.totalVehiculos()).append(System.lineSeparator());
        texto.append("Vehiculos activos      ").append(e.activos()).append(System.lineSeparator());
        texto.append("Vehiculos detenidos    ").append(e.detenidos()).append(System.lineSeparator());
        texto.append("Vehiculos averiados    ").append(e.averiados()).append(System.lineSeparator());
        texto.append("Vehiculos accidentados ").append(e.accidentados()).append(System.lineSeparator());
        texto.append("Accidentes             ").append(e.accidentes()).append(System.lineSeparator());
        texto.append("Vias bloqueadas        ").append(e.bloqueadas()).append(System.lineSeparator());
        texto.append("Emergencias activas    ").append(e.emergencias()).append(System.lineSeparator());
        texto.append(String.format("Distancia recorrida    %.2f km%n", e.distanciaKm()));
        texto.append(String.format("Energia consumida      %.2f%n", e.energiaConsumida()));
        texto.append("Clima en Lima          ").append(motor.getClimaActual()).append(System.lineSeparator());
        texto.append("Zona congestionada     ").append(motor.distritoMasCongestionado()).append(System.lineSeparator());
        texto.append("Distrito mas activo    ").append(motor.distritoMasActivo()).append(System.lineSeparator());
        texto.append("Mas incidentes         ").append(motor.distritoMasAccidentado()).append(System.lineSeparator());
        texto.append("Tick                    ").append(motor.getTicks());
        resumen.setText(texto.toString());
        grafico.repaint();
    }

    private static class GraficoResumen extends JPanel {
        private final MotorSimulacion motor;

        GraficoResumen(MotorSimulacion motor) {
            this.motor = motor;
            setPreferredSize(new Dimension(650, 500));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            pintarVehiculos(g2);
            pintarDistritos(g2);
            g2.dispose();
        }

        private void pintarVehiculos(Graphics2D g2) {
            Map<TipoVehiculo, Integer> conteo = new EnumMap<>(TipoVehiculo.class);
            for (TipoVehiculo tipo : TipoVehiculo.values()) conteo.put(tipo, 0);
            motor.getVehiculos().forEach(v -> conteo.merge(v.getTipo(), 1, Integer::sum));
            int max = Math.max(1, conteo.values().stream().mapToInt(Integer::intValue).max().orElse(1));
            int y = 42;
            int anchoMax = Math.max(120, getWidth() / 2 - 190);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            g2.drawString("Vehiculos por tipo", 20, 22);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
            for (var entrada : conteo.entrySet()) {
                int ancho = (int) (anchoMax * entrada.getValue() / (double) max);
                g2.setColor(new Color(75, 135, 195));
                g2.fillRoundRect(135, y, ancho, 14, 7, 7);
                g2.setColor(getForeground());
                g2.drawString(entrada.getKey().name(), 18, y + 11);
                g2.drawString(String.valueOf(entrada.getValue()), 141 + ancho, y + 11);
                y += 24;
            }
        }

        private void pintarDistritos(Graphics2D g2) {
            int x = Math.max(330, getWidth() / 2 + 15);
            int y = 42;
            int anchoMax = Math.max(130, getWidth() - x - 90);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            g2.setColor(getForeground());
            g2.drawString("Congestion por distrito", x, 22);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
            for (Distrito d : motor.getDistritos()) {
                int congestion = motor.nivelCongestion(d.getId());
                int ancho = (int) (anchoMax * congestion / 100.0);
                g2.setColor(congestion >= 70 ? new Color(184, 75, 63) : congestion >= 45 ? new Color(201, 145, 57) : new Color(67, 145, 112));
                g2.fillRoundRect(x + 120, y, ancho, 15, 7, 7);
                g2.setColor(getForeground());
                g2.drawString(d.getNombre(), x, y + 12);
                g2.drawString(congestion + "%", x + 126 + ancho, y + 12);
                y += 28;
            }
        }
    }
}
