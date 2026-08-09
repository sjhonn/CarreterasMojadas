package carreterasmojadas.view.dialog;

import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.service.MotorSimulacion;
import carreterasmojadas.view.panel.VistaDistritoPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DistritoSimulacionDialog extends JDialog {
    private final MotorSimulacion motor;
    private final Distrito distrito;
    private final VistaDistritoPanel vista;
    private final JLabel estado = new JLabel();
    private final Timer refresco;

    public DistritoSimulacionDialog(Window owner, MotorSimulacion motor, Distrito distrito) {
        super(owner, distrito.getNombre() + " | CarreterasMojadas", ModalityType.MODELESS);
        this.motor = motor;
        this.distrito = distrito;
        this.vista = new VistaDistritoPanel(motor);
        vista.setDistritoId(distrito.getId());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 620);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(owner);
        construir();
        refresco = new Timer(220, e -> actualizar());
        refresco.start();
        actualizar();
    }

    private void construir() {
        JPanel raiz = new JPanel(new BorderLayout());
        JPanel superior = new JPanel(new BorderLayout(12, 4));
        superior.setBorder(new EmptyBorder(12, 14, 10, 14));
        JLabel nombre = new JLabel(distrito.getNombre());
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitulo = new JLabel("Vista local de tránsito");
        subtitulo.setForeground(new Color(95, 95, 92));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(subtitulo);
        superior.add(textos, BorderLayout.WEST);
        superior.add(estado, BorderLayout.EAST);
        raiz.add(superior, BorderLayout.NORTH);
        raiz.add(vista, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private void actualizar() {
        if (!isDisplayable()) {
            refresco.stop();
            return;
        }
        estado.setText(motor.getClimaActual() + "  |  " + motor.nivelCongestion(distrito.getId()) + "% congestión  |  " + motor.vehiculosEnDistrito(distrito.getId()) + " vehículos");
        vista.actualizarVista();
    }
}
