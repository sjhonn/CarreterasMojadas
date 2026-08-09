package carreterasmojadas.view.panel;

import carreterasmojadas.model.accidente.Accidente;
import carreterasmojadas.service.MotorSimulacion;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

public class AccidentesPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Fecha", "Distrito", "Via", "Vehiculos", "Clima", "Estado", "Gravedad", "Dano"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public AccidentesPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout());
        tabla.setRowHeight(27);
        tabla.setAutoCreateRowSorter(true);
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) mostrarDetalle(); }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizarVista();
    }

    private void mostrarDetalle() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        int modelRow = tabla.convertRowIndexToModel(row);
        String id = String.valueOf(modelo.getValueAt(modelRow, 0));
        motor.getAccidentes().stream().filter(a -> a.id().equals(id)).findFirst().ifPresent(a -> {
            String texto = "Accidente: " + a.id() + System.lineSeparator() +
                    "Distrito: " + motor.nombreDistrito(a.distritoId()) + System.lineSeparator() +
                    "Via: " + a.carretera() + System.lineSeparator() +
                    "Vehiculos: " + String.join(" / ", a.vehiculos()) + System.lineSeparator() +
                    "Clima: " + a.clima() + System.lineSeparator() +
                    "Carretera: " + a.estadoCarretera() + System.lineSeparator() +
                    "Gravedad: " + a.gravedad() + System.lineSeparator() +
                    String.format("Dano promedio: %.1f%%", a.danoPromedio());
            JOptionPane.showMessageDialog(this, texto, "Detalle del accidente", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void actualizarVista() {
        modelo.setRowCount(0);
        for (Accidente a : motor.getAccidentes()) {
            modelo.addRow(new Object[]{a.id(), a.fechaHora().format(formato), motor.nombreDistrito(a.distritoId()), a.carretera(), String.join(" / ", a.vehiculos()), a.clima(), a.estadoCarretera(), a.gravedad(), String.format("%.1f%%", a.danoPromedio())});
        }
    }
}
