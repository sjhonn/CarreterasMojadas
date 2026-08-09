package carreterasmojadas;

import carreterasmojadas.service.EscenarioInicial;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EscenarioInicialTest {
    @Test
    void creaEscenarioDeLimaListoParaUsar() {
        var distritos = EscenarioInicial.crearDistritos();
        var carreteras = EscenarioInicial.crearCarreteras(distritos);
        var vehiculos = EscenarioInicial.crearVehiculos(carreteras, distritos);
        assertEquals(10, distritos.size());
        assertEquals(24, carreteras.size());
        assertEquals(44, vehiculos.size());
        assertTrue(carreteras.stream().allMatch(c -> !c.getSemaforos().isEmpty()));
        assertTrue(vehiculos.stream().allMatch(v -> v.getDistritoDestinoId() != null));
    }
}
