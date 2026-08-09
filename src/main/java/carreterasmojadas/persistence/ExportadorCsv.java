package carreterasmojadas.persistence;

import carreterasmojadas.model.accidente.Accidente;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.evento.Evento;
import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.service.Estadisticas;
import carreterasmojadas.service.MotorSimulacion;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

public class ExportadorCsv {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportarTodo(Path directorio, MotorSimulacion motor) throws IOException {
        Files.createDirectories(directorio);
        exportarVehiculos(directorio.resolve("vehiculos.csv"), motor);
        exportarDistritos(directorio.resolve("distritos.csv"), motor);
        exportarAccidentes(directorio.resolve("accidentes.csv"), motor);
        exportarEventos(directorio.resolve("eventos.csv"), motor);
        exportarEstadisticas(directorio.resolve("estadisticas.csv"), motor);
    }

    private void exportarVehiculos(Path archivo, MotorSimulacion motor) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("id,tipo,marca,modelo,velocidad,via,distrito,destino,carril,energia,dano,estado");
            writer.newLine();
            for (Vehiculo v : motor.getVehiculos()) {
                writer.write(String.join(",", limpiar(v.getId()), v.getTipo().name(), limpiar(v.getMarca()), limpiar(v.getModelo()), f(v.getVelocidadActual()), limpiar(v.getCarreteraId()), limpiar(motor.nombreDistrito(v.getDistritoActualId())), limpiar(motor.nombreDistrito(v.getDistritoDestinoId())), String.valueOf(v.getCarril() + 1), f(v.getEnergia().nivel()), f(v.getDano()), v.getEstado().name()));
                writer.newLine();
            }
        }
    }

    private void exportarDistritos(Path archivo, MotorSimulacion motor) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("id,distrito,vehiculos,congestion,velocidad_promedio,accidentes");
            writer.newLine();
            for (Distrito d : motor.getDistritos()) {
                writer.write(String.join(",", d.getId(), limpiar(d.getNombre()), String.valueOf(motor.vehiculosEnDistrito(d.getId())), String.valueOf(motor.nivelCongestion(d.getId())), f(motor.velocidadPromedioDistrito(d.getId())), String.valueOf(motor.accidentesEnDistrito(d.getId()))));
                writer.newLine();
            }
        }
    }

    private void exportarAccidentes(Path archivo, MotorSimulacion motor) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("id,fecha,distrito,via,vehiculos,clima,estado,gravedad,dano_promedio");
            writer.newLine();
            for (Accidente a : motor.getAccidentes()) {
                writer.write(String.join(",", a.id(), a.fechaHora().format(FORMATO), limpiar(motor.nombreDistrito(a.distritoId())), limpiar(a.carretera()), limpiar(String.join("|", a.vehiculos())), a.clima().name(), a.estadoCarretera().name(), a.gravedad().name(), f(a.danoPromedio())));
                writer.newLine();
            }
        }
    }

    private void exportarEventos(Path archivo, MotorSimulacion motor) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("fecha,mensaje");
            writer.newLine();
            for (Evento e : motor.getEventos()) {
                writer.write(e.fechaHora().format(FORMATO) + "," + limpiar(e.mensaje()));
                writer.newLine();
            }
        }
    }

    private void exportarEstadisticas(Path archivo, MotorSimulacion motor) throws IOException {
        Estadisticas e = motor.estadisticas();
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("vehiculos,activos,detenidos,averiados,accidentados,accidentes,bloqueadas,distancia_km,energia_consumida,emergencias,zona_congestionada,distrito_activo");
            writer.newLine();
            writer.write(String.join(",", String.valueOf(e.totalVehiculos()), String.valueOf(e.activos()), String.valueOf(e.detenidos()), String.valueOf(e.averiados()), String.valueOf(e.accidentados()), String.valueOf(e.accidentes()), String.valueOf(e.bloqueadas()), f(e.distanciaKm()), f(e.energiaConsumida()), String.valueOf(e.emergencias()), limpiar(motor.distritoMasCongestionado()), limpiar(motor.distritoMasActivo())));
            writer.newLine();
        }
    }

    private String limpiar(String valor) { return "\"" + valor.replace("\"", "\"\"") + "\""; }
    private String f(double valor) { return String.format(java.util.Locale.US, "%.2f", valor); }
}
