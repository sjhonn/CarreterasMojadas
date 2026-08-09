package carreterasmojadas.service;

import carreterasmojadas.enums.TipoClima;
import carreterasmojadas.model.accidente.Accidente;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.evento.Evento;
import carreterasmojadas.model.vehiculo.Vehiculo;
import java.io.Serializable;
import java.util.List;

public record SimulacionGuardada(List<Distrito> distritos, List<Carretera> carreteras, List<Vehiculo> vehiculos, List<Accidente> accidentes, List<Evento> eventos, TipoClima clima, long ticks, String distritoSeleccionadoId) implements Serializable { }
