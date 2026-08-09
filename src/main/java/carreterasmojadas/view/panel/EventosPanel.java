package carreterasmojadas.view.panel;

import carreterasmojadas.model.evento.Evento;
import carreterasmojadas.service.MotorSimulacion;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class EventosPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Hora", "Evento"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EventosPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout());
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setMaxWidth(90);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizarVista();
    }

    public void actualizarVista() {
        modelo.setRowCount(0);
        for (Evento e : motor.getEventos()) modelo.addRow(new Object[]{e.fechaHora().format(formato), e.mensaje()});
    }
}
