package carreterasmojadas.view.dialog;

import carreterasmojadas.enums.TipoVehiculo;
import carreterasmojadas.factory.VehiculoFactory;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.conductor.*;
import carreterasmojadas.model.energia.*;
import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.service.MotorSimulacion;
import carreterasmojadas.util.GeneradorId;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VehiculoDialog extends JDialog {
    private final MotorSimulacion motor;
    private final Vehiculo existente;
    private final JComboBox<TipoVehiculo> tipo = new JComboBox<>(TipoVehiculo.values());
    private final JTextField marca = new JTextField("Toyota");
    private final JTextField modelo = new JTextField("Modelo urbano");
    private final JSpinner velocidad = new JSpinner(new SpinnerNumberModel(90, 20, 180, 5));
    private final JComboBox<String> conductor = new JComboBox<>(new String[]{"Normal", "Prudente", "Agresivo", "Novato", "Experto"});
    private final JComboBox<Carretera> carretera;
    private final JSpinner carril = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    private boolean guardado;

    public VehiculoDialog(Window owner, MotorSimulacion motor) {
        this(owner, motor, null);
    }

    public VehiculoDialog(Window owner, MotorSimulacion motor, Vehiculo existente) {
        super(owner, existente == null ? "Nuevo vehículo" : "Editar vehículo", ModalityType.APPLICATION_MODAL);
        this.motor = motor;
        this.existente = existente;
        carretera = new JComboBox<>(motor.getCarreteras().toArray(Carretera[]::new));
        carretera.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Carretera c) setText(c.getNombre());
                return this;
            }
        });
        construir();
        if (existente != null) cargarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        agregar(form, "Tipo", tipo);
        agregar(form, "Marca", marca);
        agregar(form, "Modelo", modelo);
        agregar(form, "Velocidad máxima", velocidad);
        agregar(form, "Conductor", conductor);
        agregar(form, "Carretera", carretera);
        agregar(form, "Carril", carril);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelar = new JButton("Cancelar");
        JButton guardar = new JButton(existente == null ? "Crear" : "Guardar");
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardarVehiculo());
        acciones.add(cancelar);
        acciones.add(guardar);
        add(form, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(470, 360));
        setLocationRelativeTo(getOwner());
    }

    private void cargarDatos() {
        tipo.setSelectedItem(existente.getTipo());
        tipo.setEnabled(false);
        marca.setText(existente.getMarca());
        modelo.setText(existente.getModelo());
        velocidad.setValue((int) Math.round(existente.getVelocidadMaxima()));
        conductor.setSelectedItem(existente.getConductor().nombre());
        for (int i = 0; i < carretera.getItemCount(); i++) {
            if (carretera.getItemAt(i).getId().equals(existente.getCarreteraId())) {
                carretera.setSelectedIndex(i);
                break;
            }
        }
        carril.setValue(existente.getCarril() + 1);
    }

    private void agregar(JPanel panel, String texto, JComponent componente) {
        panel.add(new JLabel(texto));
        panel.add(componente);
    }

    private void guardarVehiculo() {
        String marcaTexto = marca.getText().trim();
        String modeloTexto = modelo.getText().trim();
        Carretera via = (Carretera) carretera.getSelectedItem();
        if (marcaTexto.isBlank() || modeloTexto.isBlank() || via == null) {
            JOptionPane.showMessageDialog(this, "Completa los campos obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int carrilSeleccionado = Math.min((Integer) carril.getValue(), via.getNumeroCarriles());
        Conductor conductorSeleccionado = crearConductor((String) conductor.getSelectedItem());
        double velocidadMaxima = ((Number) velocidad.getValue()).doubleValue();
        if (existente != null) {
            existente.actualizarDatos(marcaTexto, modeloTexto, velocidadMaxima, conductorSeleccionado);
            existente.asignarCarretera(via, carrilSeleccionado - 1, existente.getPosicion());
            motor.registrarEvento("Vehículo actualizado: " + existente.getId());
        } else {
            TipoVehiculo tipoSeleccionado = (TipoVehiculo) tipo.getSelectedItem();
            FuenteEnergia energia = crearEnergia(tipoSeleccionado);
            String id = GeneradorId.vehiculo(prefijo(tipoSeleccionado));
            Vehiculo vehiculo = VehiculoFactory.crear(tipoSeleccionado, id, marcaTexto, modeloTexto, velocidadMaxima, peso(tipoSeleccionado), energia, conductorSeleccionado);
            motor.agregarVehiculo(vehiculo, via, carrilSeleccionado - 1, 10);
        }
        guardado = true;
        dispose();
    }

    private Conductor crearConductor(String nombre) {
        return switch (nombre) {
            case "Prudente" -> new ConductorPrudente();
            case "Agresivo" -> new ConductorAgresivo();
            case "Novato" -> new ConductorNovato();
            case "Experto" -> new ConductorExperto();
            default -> new ConductorNormal();
        };
    }

    private FuenteEnergia crearEnergia(TipoVehiculo tipo) {
        return switch (tipo) {
            case CAMION, AUTOBUS, BOMBEROS -> new Diesel(110);
            case AUTOMOVIL_ELECTRICO, BICICLETA_ELECTRICA -> new Bateria(80);
            case BICICLETA -> new EnergiaHumana(100);
            default -> new Gasolina(55);
        };
    }

    private String prefijo(TipoVehiculo tipo) {
        return switch (tipo) {
            case AUTOMOVIL -> "AUTO";
            case AUTOMOVIL_ELECTRICO -> "EV";
            case MOTOCICLETA -> "MOTO";
            case BICICLETA -> "BICI";
            case BICICLETA_ELECTRICA -> "EBICI";
            case CAMION -> "CAM";
            case AUTOBUS -> "BUS";
            case TAXI -> "TAXI";
            case AMBULANCIA -> "AMB";
            case PATRULLA -> "PAT";
            case BOMBEROS -> "BOM";
        };
    }

    private double peso(TipoVehiculo tipo) {
        return switch (tipo) {
            case BICICLETA, BICICLETA_ELECTRICA -> 25;
            case MOTOCICLETA -> 190;
            case CAMION -> 8500;
            case AUTOBUS -> 7200;
            case BOMBEROS -> 9800;
            default -> 1450;
        };
    }

    public boolean isGuardado() { return guardado; }
}
