package carreterasmojadas.model.evento;

import java.io.Serializable;
import java.time.LocalDateTime;

public record Evento(LocalDateTime fechaHora, String mensaje) implements Serializable { }
