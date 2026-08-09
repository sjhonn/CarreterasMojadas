package carreterasmojadas.view.panel;

import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.service.Estadisticas;
import carreterasmojadas.service.MotorSimulacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final Map<String, JLabel> indicadores = new LinkedHashMap<>();
    private final JLabel estadoGeneral = new JLabel();
    private final JTextArea actividad = new JTextArea();
    private final DefaultTableModel modeloDistritos = new DefaultTableModel(new String[]{"Distrito", "Vehículos", "Congestión", "Velocidad", "Incidentes"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public DashboardPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        actualizarVista();
    }

    private JComponent crearEncabezado() {
        JPanel bloque = new JPanel(new BorderLayout(0, 12));
        JPanel titulo = new JPanel(new BorderLayout());
        JLabel principal = new JLabel("Estado de la ciudad");
        principal.setFont(principal.getFont().deriveFont(Font.BOLD, 21f));
        JLabel subtitulo = new JLabel("Seguimiento general de tránsito en Lima");
        subtitulo.setForeground(new Color(98, 98, 95));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(principal);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        estadoGeneral.setHorizontalAlignment(SwingConstants.RIGHT);
        titulo.add(textos, BorderLayout.WEST);
        titulo.add(estadoGeneral, BorderLayout.EAST);

        JPanel fila = new JPanel(new GridLayout(1, 5, 8, 0));
        agregarIndicador(fila, "Vehículos", "total");
        agregarIndicador(fila, "En movimiento", "activos");
        agregarIndicador(fila, "Accidentes", "accidentes");
        agregarIndicador(fila, "Emergencias", "emergencias");
        agregarIndicador(fila, "Vías bloqueadas", "bloqueadas");
        bloque.add(titulo, BorderLayout.NORTH);
        bloque.add(fila, BorderLayout.CENTER);
        return bloque;
    }

    private void agregarIndicador(JPanel fila, String texto, String clave) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(211, 211, 207)),
                new EmptyBorder(10, 11, 10, 11)
        ));
        JLabel nombre = new JLabel(texto);
        nombre.setForeground(new Color(91, 91, 88));
        JLabel valor = new JLabel("0");
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(nombre, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        indicadores.put(clave, valor);
        fila.add(panel);
    }

    private JComponent crearCuerpo() {
        JPanel izquierdo = new JPanel(new BorderLayout(0, 9));
        izquierdo.setBorder(BorderFactory.createTitledBorder("Situación por distrito"));
        JTable tabla = new JTable(modeloDistritos);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);
        izquierdo.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel derecho = new JPanel(new BorderLayout(0, 9));
        derecho.setBorder(BorderFactory.createTitledBorder("Actividad reciente"));
        actividad.setEditable(false);
        actividad.setLineWrap(true);
        actividad.setWrapStyleWord(true);
        actividad.setMargin(new Insets(8, 8, 8, 8));
        derecho.add(new JScrollPane(actividad), BorderLayout.CENTER);
        derecho.add(crearResumenOperativo(), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierdo, derecho);
        split.setResizeWeight(0.62);
        split.setDividerLocation(700);
        split.setBorder(null);
        return split;
    }

    private JComponent crearResumenOperativo() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 4));
        panel.setBorder(new EmptyBorder(6, 4, 4, 4));
        agregarDato(panel, "Clima", "clima");
        agregarDato(panel, "Zona con más tráfico", "congestion");
        agregarDato(panel, "Distrito más activo", "activo");
        agregarDato(panel, "Mayor incidencia", "incidentes");
        agregarDato(panel, "Distancia recorrida", "distancia");
        agregarDato(panel, "Energía consumida", "energia");
        return panel;
    }

    private void agregarDato(JPanel panel, String texto, String clave) {
        JLabel nombre = new JLabel(texto + ":");
        JLabel valor = new JLabel("-");
        valor.setFont(valor.getFont().deriveFont(Font.BOLD));
        indicadores.put(clave, valor);
        panel.add(nombre);
        panel.add(valor);
    }

    public void actualizarVista() {
        Estadisticas e = motor.estadisticas();
        indicadores.get("total").setText(String.valueOf(e.totalVehiculos()));
        indicadores.get("activos").setText(String.valueOf(e.activos()));
        indicadores.get("accidentes").setText(String.valueOf(e.accidentes()));
        indicadores.get("emergencias").setText(String.valueOf(e.emergencias()));
        indicadores.get("bloqueadas").setText(String.valueOf(e.bloqueadas()));
        indicadores.get("clima").setText(motor.getClimaActual().name());
        indicadores.get("congestion").setText(motor.distritoMasCongestionado());
        indicadores.get("activo").setText(motor.distritoMasActivo());
        indicadores.get("incidentes").setText(motor.distritoMasAccidentado());
        indicadores.get("distancia").setText(String.format("%.2f km", e.distanciaKm()));
        indicadores.get("energia").setText(String.format("%.1f", e.energiaConsumida()));
        estadoGeneral.setText(motor.getEstado().name() + "  ·  Tick " + motor.getTicks());

        modeloDistritos.setRowCount(0);
        for (Distrito distrito : motor.getDistritos()) {
            modeloDistritos.addRow(new Object[]{
                    distrito.getNombre(),
                    motor.vehiculosEnDistrito(distrito.getId()),
                    motor.nivelCongestion(distrito.getId()) + "%",
                    String.format("%.1f km/h", motor.velocidadPromedioDistrito(distrito.getId())),
                    motor.accidentesEnDistrito(distrito.getId())
            });
        }

        StringBuilder texto = new StringBuilder();
        motor.getEventos().stream().limit(12).forEach(evento -> texto.append(evento.fechaHora().toLocalTime().withNano(0)).append("  ").append(evento.mensaje()).append(System.lineSeparator()));
        actividad.setText(texto.toString());
        actividad.setCaretPosition(0);
    }
}
