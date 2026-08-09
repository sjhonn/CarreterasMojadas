package carreterasmojadas.view.panel;

import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.service.MotorSimulacion;
import carreterasmojadas.view.dialog.DistritoSimulacionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DistritosPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final DefaultListModel<Distrito> modeloDistritos = new DefaultListModel<>();
    private final JList<Distrito> listaDistritos = new JList<>(modeloDistritos);
    private final VistaDistritoPanel vistaLocal;
    private final JLabel nombre = new JLabel();
    private final JLabel resumen = new JLabel();
    private final DefaultTableModel modeloVehiculos = new DefaultTableModel(new String[]{"Vehículo", "Tipo", "Vía", "Velocidad", "Estado", "Destino"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaVehiculos = new JTable(modeloVehiculos);
    private String ultimaSeleccion;

    public DistritosPanel(MotorSimulacion motor) {
        this.motor = motor;
        this.vistaLocal = new VistaDistritoPanel(motor);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        cargarDistritos();
        add(crearContenido(), BorderLayout.CENTER);
        seleccionarActual();
        actualizarVista();
    }

    private JComponent crearContenido() {
        JPanel listado = crearListado();
        JPanel detalle = crearDetalle();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listado, detalle);
        split.setResizeWeight(0.2);
        split.setDividerLocation(220);
        split.setContinuousLayout(true);
        split.setBorder(null);
        return split;
    }

    private JPanel crearListado() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(4, 0, 4, 10));
        JLabel titulo = new JLabel("Distritos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel ayuda = new JLabel("Selecciona una zona para seguir su tránsito");
        ayuda.setForeground(new Color(105, 105, 102));
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.add(titulo);
        cabecera.add(Box.createVerticalStrut(3));
        cabecera.add(ayuda);
        listaDistritos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaDistritos.setFixedCellHeight(36);
        listaDistritos.setCellRenderer(new DistritoRenderer());
        listaDistritos.addListSelectionListener(this::cambiarDistrito);
        JScrollPane scroll = new JScrollPane(listaDistritos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 206)));
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDetalle() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(4, 10, 4, 0));
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 20f));
        resumen.setForeground(new Color(92, 92, 88));
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(3));
        textos.add(resumen);
        JButton abrir = new JButton("Abrir vista local");
        abrir.addActionListener(e -> abrirVistaLocal());
        cabecera.add(textos, BorderLayout.WEST);
        cabecera.add(abrir, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        vistaLocal.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 198)));
        centro.add(vistaLocal, BorderLayout.CENTER);

        tablaVehiculos.setRowHeight(24);
        tablaVehiculos.setFillsViewportHeight(true);
        JScrollPane tablaScroll = new JScrollPane(tablaVehiculos);
        tablaScroll.setPreferredSize(new Dimension(500, 160));
        tablaScroll.setBorder(BorderFactory.createTitledBorder("Vehículos en la zona"));
        centro.add(tablaScroll, BorderLayout.SOUTH);

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private void cargarDistritos() {
        modeloDistritos.clear();
        for (Distrito distrito : motor.getDistritos()) modeloDistritos.addElement(distrito);
    }

    private void seleccionarActual() {
        String actual = motor.getDistritoSeleccionadoId();
        for (int i = 0; i < modeloDistritos.size(); i++) {
            if (modeloDistritos.get(i).getId().equals(actual)) {
                listaDistritos.setSelectedIndex(i);
                listaDistritos.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    private void cambiarDistrito(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        Distrito distrito = listaDistritos.getSelectedValue();
        if (distrito == null) return;
        motor.seleccionarDistrito(distrito.getId());
        vistaLocal.setDistritoId(distrito.getId());
        ultimaSeleccion = distrito.getId();
        actualizarDetalle(distrito);
    }

    private void abrirVistaLocal() {
        Distrito distrito = listaDistritos.getSelectedValue();
        if (distrito == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        DistritoSimulacionDialog dialog = new DistritoSimulacionDialog(owner, motor, distrito);
        dialog.setVisible(true);
    }

    private void actualizarDetalle(Distrito distrito) {
        nombre.setText(distrito.getNombre());
        long vias = motor.getCarreteras().stream()
                .filter(c -> distrito.getId().equals(c.getDistritoOrigenId()) || distrito.getId().equals(c.getDistritoDestinoId()))
                .count();
        resumen.setText(vias + " vías  ·  " + motor.vehiculosEnDistrito(distrito.getId()) + " vehículos  ·  " + motor.nivelCongestion(distrito.getId()) + "% congestión  ·  " + String.format("%.1f km/h", motor.velocidadPromedioDistrito(distrito.getId())));
        modeloVehiculos.setRowCount(0);
        List<Vehiculo> locales = motor.getVehiculos().stream()
                .filter(v -> distrito.getId().equals(v.getDistritoActualId()))
                .limit(18)
                .toList();
        for (Vehiculo vehiculo : locales) {
            String via = motor.getCarreteras().stream().filter(c -> c.getId().equals(vehiculo.getCarreteraId())).map(c -> c.getNombre()).findFirst().orElse("-");
            modeloVehiculos.addRow(new Object[]{
                    vehiculo.getId(),
                    vehiculo.getTipo().name(),
                    via,
                    String.format("%.1f km/h", vehiculo.getVelocidadActual()),
                    vehiculo.getEstado().name(),
                    motor.nombreDistrito(vehiculo.getDistritoDestinoId())
            });
        }
    }

    public void actualizarVista() {
        String seleccionado = motor.getDistritoSeleccionadoId();
        if (seleccionado == null) return;
        if (!seleccionado.equals(ultimaSeleccion)) seleccionarActual();
        Distrito distrito = listaDistritos.getSelectedValue();
        if (distrito == null) distrito = motor.buscarDistrito(seleccionado).orElse(null);
        if (distrito == null) return;
        ultimaSeleccion = distrito.getId();
        vistaLocal.setDistritoId(distrito.getId());
        actualizarDetalle(distrito);
        vistaLocal.actualizarVista();
        listaDistritos.repaint();
    }

    private class DistritoRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            if (value instanceof Distrito distrito) {
                int congestion = motor.nivelCongestion(distrito.getId());
                label.setText("  " + distrito.getNombre() + "   " + congestion + "%");
                label.setBorder(new EmptyBorder(0, 5, 0, 5));
                if (!selected) label.setBackground(index % 2 == 0 ? new Color(248, 248, 246) : new Color(242, 242, 239));
            }
            return label;
        }
    }
}
