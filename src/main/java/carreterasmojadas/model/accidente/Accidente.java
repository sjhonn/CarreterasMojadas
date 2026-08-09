package carreterasmojadas.model.accidente;

import carreterasmojadas.enums.GravedadAccidente;
import carreterasmojadas.enums.EstadoCarretera;
import carreterasmojadas.enums.TipoClima;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record Accidente(String id, LocalDateTime fechaHora, String distritoId, String carretera, double posicion, List<String> vehiculos, TipoClima clima, EstadoCarretera estadoCarretera, GravedadAccidente gravedad, double danoPromedio) implements Serializable { }
