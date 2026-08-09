package carreterasmojadas.view.panel;

import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.service.MotorSimulacion;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CarreterasPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Via", "Origen", "Destino", "Longitud", "Carriles", "Limite", "Estado", "Vehiculos"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public CarreterasPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout());
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(27);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizarVista();
    }

    public void actualizarVista() {
        modelo.setRowCount(0);
        for (Carretera c : motor.getCarreteras()) {
            long vehiculos = motor.getVehiculos().stream().filter(v -> c.getId().equals(v.getCarreteraId())).count();
            modelo.addRow(new Object[]{c.getId(), c.getNombre(), motor.nombreDistrito(c.getDistritoOrigenId()), motor.nombreDistrito(c.getDistritoDestinoId()), String.format("%.0f m", c.getLongitud()), c.getNumeroCarriles(), String.format("%.0f km/h", c.getLimiteVelocidad()), c.getEstado(), vehiculos});
        }
    }
}
