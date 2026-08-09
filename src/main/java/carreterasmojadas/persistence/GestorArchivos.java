package carreterasmojadas.persistence;

import carreterasmojadas.exception.ArchivoSimulacionException;
import carreterasmojadas.service.SimulacionGuardada;
import java.io.*;
import java.nio.file.*;

public class GestorArchivos {
    public void guardar(Path archivo, SimulacionGuardada simulacion) {
        try {
            if (archivo.getParent() != null) Files.createDirectories(archivo.getParent());
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(archivo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                salida.writeObject(simulacion);
            }
        } catch (IOException ex) {
            throw new ArchivoSimulacionException("No se pudo guardar la simulación", ex);
        }
    }

    public SimulacionGuardada cargar(Path archivo) {
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(archivo))) {
            Object objeto = entrada.readObject();
            return (SimulacionGuardada) objeto;
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            throw new ArchivoSimulacionException("No se pudo cargar la simulación", ex);
        }
    }
}
