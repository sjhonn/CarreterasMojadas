package carreterasmojadas;

import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.service.MotorSimulacion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MotorSimulacionTest {
    @Test
    void cambiaClimaYMojaLasViasDeLima() {
        MotorSimulacion motor = new MotorSimulacion();
        motor.cambiarClima(TipoClima.LLUVIA);
        assertEquals(TipoClima.LLUVIA, motor.getClimaActual());
        assertTrue(motor.getCarreteras().stream().allMatch(c -> c.getEstado().name().equals("MOJADA")));
    }

    @Test
    void avanzaLaSimulacionUrbana() throws Exception {
        MotorSimulacion motor = new MotorSimulacion();
        double inicio = motor.getVehiculos().getFirst().getPosicion();
        motor.iniciar();
        Thread.sleep(700);
        motor.detener();
        assertTrue(motor.getTicks() > 0);
        assertNotEquals(inicio, motor.getVehiculos().getFirst().getPosicion());
        assertEquals(10, motor.getDistritos().size());
    }
}
