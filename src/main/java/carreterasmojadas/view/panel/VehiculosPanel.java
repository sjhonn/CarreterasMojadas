package carreterasmojadas.view.panel;

import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.service.MotorSimulacion;
import carreterasmojadas.view.dialog.VehiculoDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VehiculosPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Tipo", "Marca", "Modelo", "Velocidad", "Via", "Distrito", "Destino", "Carril", "Energia", "Dano", "Estado", "Conductor"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField buscar = new JTextField(18);

    public VehiculosPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout(8, 8));
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Nuevo");
        JButton editar = new JButton("Editar");
        JButton eliminar = new JButton("Eliminar");
        JButton ver = new JButton("Ver detalles");
        barra.add(new JLabel("Buscar"));
        barra.add(buscar);
        barra.add(nuevo);
        barra.add(editar);
        barra.add(eliminar);
        barra.add(ver);
        nuevo.addActionListener(e -> abrirNuevo());
        editar.addActionListener(e -> editarSeleccionado());
        eliminar.addActionListener(e -> eliminarSeleccionado());
        ver.addActionListener(e -> verSeleccionado());
        buscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarVista(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarVista(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarVista(); }
        });
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) verSeleccionado(); }
        });
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(25);
        add(barra, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizarVista();
    }

    private void abrirNuevo() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        VehiculoDialog dialog = new VehiculoDialog(owner, motor);
        dialog.setVisible(true);
        actualizarVista();
    }

    private void editarSeleccionado() {
        Vehiculo vehiculo = seleccionado();
        if (vehiculo == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        VehiculoDialog dialog = new VehiculoDialog(owner, motor, vehiculo);
        dialog.setVisible(true);
        actualizarVista();
    }

    private void eliminarSeleccionado() {
        Vehiculo vehiculo = seleccionado();
        if (vehiculo == null) return;
        int opcion = JOptionPane.showConfirmDialog(this, "Eliminar " + vehiculo.getId() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) motor.eliminarVehiculo(vehiculo);
        actualizarVista();
    }

    private void verSeleccionado() {
        Vehiculo v = seleccionado();
        if (v == null) return;
        String texto = "ID: " + v.getId() + System.lineSeparator() +
                "Tipo: " + v.getTipo() + System.lineSeparator() +
                "Marca: " + v.getMarca() + System.lineSeparator() +
                "Modelo: " + v.getModelo() + System.lineSeparator() +
                String.format("Velocidad: %.1f km/h%n", v.getVelocidadActual()) +
                "Via: " + v.getCarreteraId() + System.lineSeparator() +
                "Distrito actual: " + motor.nombreDistrito(v.getDistritoActualId()) + System.lineSeparator() +
                "Destino: " + motor.nombreDistrito(v.getDistritoDestinoId()) + System.lineSeparator() +
                "Carril: " + (v.getCarril() + 1) + System.lineSeparator() +
                String.format("Energia: %.1f / %.1f%n", v.getEnergia().nivel(), v.getEnergia().capacidad()) +
                String.format("Dano: %.1f%%%n", v.getDano()) +
                "Estado: " + v.getEstado() + System.lineSeparator() +
                "Conductor: " + v.getConductor().nombre();
        JOptionPane.showMessageDialog(this, texto, "Detalle del vehiculo", JOptionPane.INFORMATION_MESSAGE);
    }

    private Vehiculo seleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) return null;
        int modelRow = tabla.convertRowIndexToModel(row);
        String id = String.valueOf(modelo.getValueAt(modelRow, 0));
        return motor.buscarVehiculo(id).orElse(null);
    }

    public void actualizarVista() {
        String filtro = buscar.getText().trim().toLowerCase();
        modelo.setRowCount(0);
        for (Vehiculo v : motor.getVehiculos()) {
            String texto = (v.getId() + " " + v.getTipo() + " " + v.getMarca() + " " + v.getModelo() + " " + v.getEstado() + " " + motor.nombreDistrito(v.getDistritoActualId())).toLowerCase();
            if (!filtro.isBlank() && !texto.contains(filtro)) continue;
            modelo.addRow(new Object[]{v.getId(), v.getTipo(), v.getMarca(), v.getModelo(), String.format("%.1f", v.getVelocidadActual()), v.getCarreteraId(), motor.nombreDistrito(v.getDistritoActualId()), motor.nombreDistrito(v.getDistritoDestinoId()), v.getCarril() + 1, String.format("%.1f", v.getEnergia().nivel()), String.format("%.1f%%", v.getDano()), v.getEstado(), v.getConductor().nombre()});
        }
    }
}
