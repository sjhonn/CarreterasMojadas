package carreterasmojadas.view.panel;

import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.service.MotorSimulacion;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConfiguracionPanel extends JPanel implements Actualizable {
    private final MotorSimulacion motor;
    private final JComboBox<TipoClima> clima = new JComboBox<>(TipoClima.values());
    private final JComboBox<String> velocidad = new JComboBox<>(new String[]{"0.5x", "1x", "2x", "4x"});
    private final JSpinner eventos = new JSpinner(new SpinnerNumberModel(0.2, 0.0, 5.0, 0.1));

    public ConfiguracionPanel(MotorSimulacion motor) {
        this.motor = motor;
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Clima"), c);
        c.gridx = 1; form.add(clima, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Velocidad"), c);
        c.gridx = 1; form.add(velocidad, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Frecuencia de eventos (%)"), c);
        c.gridx = 1; form.add(eventos, c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        JButton aplicar = new JButton("Aplicar configuración");
        form.add(aplicar, c);
        aplicar.addActionListener(e -> aplicar());
        add(form, BorderLayout.NORTH);
        actualizarVista();
    }

    private void aplicar() {
        motor.cambiarClima((TipoClima) clima.getSelectedItem());
        String seleccion = (String) velocidad.getSelectedItem();
        double factor = switch (seleccion) {
            case "0.5x" -> 0.5;
            case "2x" -> 2.0;
            case "4x" -> 4.0;
            default -> 1.0;
        };
        motor.setVelocidadSimulacion(factor);
        motor.setProbabilidadEventos(((Number) eventos.getValue()).doubleValue() / 100.0);
        JOptionPane.showMessageDialog(this, "Configuración aplicada.", "CarreterasMojadas", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarVista() {
        clima.setSelectedItem(motor.getClimaActual());
        double factor = motor.getVelocidadSimulacion();
        velocidad.setSelectedItem(factor == 0.5 ? "0.5x" : factor == 2.0 ? "2x" : factor == 4.0 ? "4x" : "1x");
        eventos.setValue(motor.getProbabilidadEventos() * 100.0);
    }
}
