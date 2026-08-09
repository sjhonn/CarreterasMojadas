package carreterasmojadas;

import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.persistence.GestorArchivos;
import carreterasmojadas.service.MotorSimulacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaTest {
    @TempDir
    Path temp;

    @Test
    void guardaYCargaCiudadCompleta() {
        MotorSimulacion origen = new MotorSimulacion();
        origen.cambiarClima(TipoClima.NIEBLA);
        GestorArchivos gestor = new GestorArchivos();
        Path archivo = temp.resolve("simulacion.cms");
        gestor.guardar(archivo, origen.snapshot());
        MotorSimulacion destino = new MotorSimulacion();
        destino.cargar(gestor.cargar(archivo));
        assertEquals(origen.getVehiculos().size(), destino.getVehiculos().size());
        assertEquals(origen.getDistritos().size(), destino.getDistritos().size());
        assertEquals(TipoClima.NIEBLA, destino.getClimaActual());
    }
}
