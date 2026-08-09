package carreterasmojadas.view;

import carreterasmojadas.enums.EstadoSimulacion;
import carreterasmojadas.persistence.ExportadorCsv;
import carreterasmojadas.persistence.GestorArchivos;
import carreterasmojadas.service.Estadisticas;
import carreterasmojadas.service.MotorSimulacion;
import carreterasmojadas.view.panel.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final MotorSimulacion motor = new MotorSimulacion();
    private final GestorArchivos gestorArchivos = new GestorArchivos();
    private final ExportadorCsv exportadorCsv = new ExportadorCsv();
    private final List<Actualizable> actualizables = new ArrayList<>();
    private final JLabel estado = new JLabel();
    private final JButton iniciar = new JButton("Iniciar");
    private final JButton pausar = new JButton("Pausar");
    private final JButton continuar = new JButton("Continuar");
    private final JButton detener = new JButton("Detener");
    private final JButton reiniciar = new JButton("Reiniciar");
    private final JComboBox<String> velocidad = new JComboBox<>(new String[]{"0.5x", "1x", "2x", "4x"});
    private Path archivoActual;

    public MainFrame() {
        super("CarreterasMojadas");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        construirInterfaz();
        conectarAcciones();
        iniciarRefresco();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { cerrarAplicacion(); }
        });
    }

    private void construirInterfaz() {
        setJMenuBar(crearMenu());
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearCabecera(), BorderLayout.NORTH);
        raiz.add(crearPestanas(), BorderLayout.CENTER);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem nueva = new JMenuItem("Nueva simulación");
        JMenuItem guardar = new JMenuItem("Guardar");
        JMenuItem guardarComo = new JMenuItem("Guardar como...");
        JMenuItem cargar = new JMenuItem("Cargar...");
        JMenuItem exportar = new JMenuItem("Exportar CSV...");
        JMenuItem salir = new JMenuItem("Salir");
        nueva.addActionListener(e -> nuevaSimulacion());
        guardar.addActionListener(e -> guardar(false));
        guardarComo.addActionListener(e -> guardar(true));
        cargar.addActionListener(e -> cargar());
        exportar.addActionListener(e -> exportar());
        salir.addActionListener(e -> cerrarAplicacion());
        archivo.add(nueva);
        archivo.addSeparator();
        archivo.add(guardar);
        archivo.add(guardarComo);
        archivo.add(cargar);
        archivo.addSeparator();
        archivo.add(exportar);
        archivo.addSeparator();
        archivo.add(salir);
        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> JOptionPane.showMessageDialog(this, "CarreterasMojadas\nSimulación urbana inspirada en Lima, Perú.", "CarreterasMojadas", JOptionPane.INFORMATION_MESSAGE));
        ayuda.add(acerca);
        barra.add(archivo);
        barra.add(ayuda);
        return barra;
    }

    private JComponent crearCabecera() {
        JPanel contenedor = new JPanel(new BorderLayout(14, 8));
        contenedor.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel titulo = new JLabel("CarreterasMojadas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        JLabel zona = new JLabel("Lima Metropolitana");
        zona.setForeground(new Color(100, 100, 96));
        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.add(titulo);
        marca.add(zona);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 2));
        velocidad.setSelectedItem("1x");
        acciones.add(iniciar);
        acciones.add(pausar);
        acciones.add(continuar);
        acciones.add(detener);
        acciones.add(reiniciar);
        acciones.add(new JLabel("Velocidad"));
        acciones.add(velocidad);
        contenedor.add(marca, BorderLayout.WEST);
        contenedor.add(acciones, BorderLayout.CENTER);
        return contenedor;
    }

    private JComponent crearPestanas() {
        JTabbedPane tabs = new JTabbedPane();
        DashboardPanel dashboard = new DashboardPanel(motor);
        MapaSimulacionPanel mapa = new MapaSimulacionPanel(motor);
        DistritosPanel distritos = new DistritosPanel(motor);
        VehiculosPanel vehiculos = new VehiculosPanel(motor);
        CarreterasPanel carreteras = new CarreterasPanel(motor);
        AccidentesPanel accidentes = new AccidentesPanel(motor);
        EstadisticasPanel estadisticas = new EstadisticasPanel(motor);
        EventosPanel eventos = new EventosPanel(motor);
        ConfiguracionPanel configuracion = new ConfiguracionPanel(motor);
        actualizables.add(dashboard);
        actualizables.add(mapa);
        actualizables.add(distritos);
        actualizables.add(vehiculos);
        actualizables.add(carreteras);
        actualizables.add(accidentes);
        actualizables.add(estadisticas);
        actualizables.add(eventos);
        tabs.addTab("Dashboard", dashboard);
        tabs.addTab("Mapa general", new JScrollPane(mapa));
        tabs.addTab("Distritos", distritos);
        tabs.addTab("Vehículos", vehiculos);
        tabs.addTab("Carreteras", carreteras);
        tabs.addTab("Accidentes", accidentes);
        tabs.addTab("Estadísticas", estadisticas);
        tabs.addTab("Eventos", eventos);
        tabs.addTab("Configuración", configuracion);
        return tabs;
    }

    private JComponent crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(7, 12, 7, 12));
        panel.add(estado, BorderLayout.WEST);
        return panel;
    }

    private void conectarAcciones() {
        iniciar.addActionListener(e -> motor.iniciar());
        pausar.addActionListener(e -> motor.pausar());
        continuar.addActionListener(e -> motor.reanudar());
        detener.addActionListener(e -> motor.detener());
        reiniciar.addActionListener(e -> nuevaSimulacion());
        velocidad.addActionListener(e -> {
            String seleccion = (String) velocidad.getSelectedItem();
            motor.setVelocidadSimulacion(switch (seleccion) {
                case "0.5x" -> 0.5;
                case "2x" -> 2.0;
                case "4x" -> 4.0;
                default -> 1.0;
            });
        });
    }

    private void iniciarRefresco() {
        Timer timer = new Timer(250, e -> {
            for (Actualizable actualizable : actualizables) actualizable.actualizarVista();
            actualizarEstado();
        });
        timer.start();
        actualizarEstado();
    }

    private void actualizarEstado() {
        Estadisticas e = motor.estadisticas();
        estado.setText("Estado: " + motor.getEstado() + "   |   Clima: " + motor.getClimaActual() + "   |   Vehículos: " + e.totalVehiculos() + "   |   Accidentes: " + e.accidentes() + "   |   Distrito: " + motor.nombreDistrito(motor.getDistritoSeleccionadoId()) + "   |   Tick: " + motor.getTicks() + "   |   " + motor.getVelocidadSimulacion() + "x");
        EstadoSimulacion actual = motor.getEstado();
        iniciar.setEnabled(actual == EstadoSimulacion.DETENIDA);
        pausar.setEnabled(actual == EstadoSimulacion.EJECUTANDO);
        continuar.setEnabled(actual == EstadoSimulacion.PAUSADA);
        detener.setEnabled(actual != EstadoSimulacion.DETENIDA);
    }

    private void nuevaSimulacion() {
        if (motor.getEstado() != EstadoSimulacion.DETENIDA) {
            int opcion = JOptionPane.showConfirmDialog(this, "La simulación actual se detendrá. ¿Continuar?", "Nueva simulación", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) return;
        }
        motor.reiniciar();
        archivoActual = null;
    }

    private void guardar(boolean elegirArchivo) {
        if (archivoActual == null || elegirArchivo) {
            JFileChooser chooser = new JFileChooser(Path.of("data", "simulaciones").toFile());
            chooser.setSelectedFile(Path.of("CarreterasMojadas.cms").toFile());
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            archivoActual = chooser.getSelectedFile().toPath();
        }
        try {
            gestorArchivos.guardar(archivoActual, motor.snapshot());
            motor.registrarEvento("Simulación guardada en " + archivoActual.getFileName());
            JOptionPane.showMessageDialog(this, "Simulación guardada correctamente.", "CarreterasMojadas", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargar() {
        JFileChooser chooser = new JFileChooser(Path.of("data", "simulaciones").toFile());
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            archivoActual = chooser.getSelectedFile().toPath();
            motor.cargar(gestorArchivos.cargar(archivoActual));
            JOptionPane.showMessageDialog(this, "Simulación cargada correctamente.", "CarreterasMojadas", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void exportar() {
        JFileChooser chooser = new JFileChooser(Path.of("data", "exportaciones").toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            exportadorCsv.exportarTodo(chooser.getSelectedFile().toPath(), motor);
            motor.registrarEvento("Datos exportados a CSV");
            JOptionPane.showMessageDialog(this, "Archivos CSV exportados correctamente.", "CarreterasMojadas", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError("No se pudieron exportar los archivos CSV.");
        }
    }

    private void cerrarAplicacion() {
        int opcion = JOptionPane.showConfirmDialog(this, "¿Cerrar CarreterasMojadas?", "Salir", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;
        motor.detener();
        dispose();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
