package carreterasmojadas;

import carreterasmojadas.view.MainFrame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarApariencia();
            MainFrame ventana = new MainFrame();
            ventana.setVisible(true);
        });
    }

    private static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            UIManager.getDefaults();
        }
        Font fuente = new Font("Segoe UI", Font.PLAIN, 13);
        if (!"Segoe UI".equalsIgnoreCase(fuente.getFamily())) fuente = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        FontUIResource recurso = new FontUIResource(fuente);
        Enumeration<Object> claves = UIManager.getDefaults().keys();
        while (claves.hasMoreElements()) {
            Object clave = claves.nextElement();
            Object valor = UIManager.get(clave);
            if (valor instanceof FontUIResource) UIManager.put(clave, recurso);
        }
        UIManager.put("Table.rowHeight", 25);
        UIManager.put("TabbedPane.tabInsets", new Insets(7, 12, 7, 12));
    }
}