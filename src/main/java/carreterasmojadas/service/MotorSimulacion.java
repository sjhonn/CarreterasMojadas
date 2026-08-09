package carreterasmojadas.service;

import carreterasmojadas.enums.*;
import carreterasmojadas.exception.SimulacionException;
import carreterasmojadas.model.accidente.Accidente;
import carreterasmojadas.model.carretera.Carretera;
import carreterasmojadas.model.carretera.Semaforo;
import carreterasmojadas.model.ciudad.Distrito;
import carreterasmojadas.model.clima.ControladorClima;
import carreterasmojadas.model.evento.Evento;
import carreterasmojadas.model.vehiculo.Vehiculo;
import carreterasmojadas.util.GeneradorId;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MotorSimulacion {
    private final CopyOnWriteArrayList<Distrito> distritos = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Carretera> carreteras = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Vehiculo> vehiculos = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Accidente> accidentes = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedDeque<Evento> eventos = new ConcurrentLinkedDeque<>();
    private final Set<String> idsRegistrados = Collections.synchronizedSet(new HashSet<>());
    private final Queue<SolicitudEmergencia> emergenciasPendientes = new ConcurrentLinkedQueue<>();
    private final ControladorClima controladorClima = new ControladorClima();
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);
    private final AtomicBoolean pausada = new AtomicBoolean(false);
    private final Random random = new Random();
    private ScheduledExecutorService reloj;
    private ExecutorService trabajadores;
    private volatile double velocidadSimulacion = 1.0;
    private volatile long ticks;
    private volatile double probabilidadEventos = 0.002;
    private volatile String distritoSeleccionadoId;

    public MotorSimulacion() {
        reiniciarEscenario();
    }

    public synchronized void iniciar() {
        if (ejecutando.get()) return;
        ejecutando.set(true);
        pausada.set(false);
        trabajadores = Executors.newFixedThreadPool(Math.min(4, Math.max(2, Runtime.getRuntime().availableProcessors())), tarea -> crearHilo(tarea, "vehiculos-worker"));
        reloj = Executors.newSingleThreadScheduledExecutor(tarea -> crearHilo(tarea, "motor-simulacion"));
        reloj.scheduleAtFixedRate(this::tickSeguro, 0, 200, TimeUnit.MILLISECONDS);
        registrarEvento("Simulaci\u00f3n iniciada en Lima");
    }

    public void pausar() {
        if (ejecutando.get()) {
            pausada.set(true);
            registrarEvento("Simulaci\u00f3n pausada");
        }
    }

    public void reanudar() {
        if (ejecutando.get() && pausada.get()) {
            pausada.set(false);
            registrarEvento("Simulaci\u00f3n reanudada");
        }
    }

    public synchronized void detener() {
        if (!ejecutando.getAndSet(false)) return;
        pausada.set(false);
        if (reloj != null) reloj.shutdownNow();
        if (trabajadores != null) trabajadores.shutdownNow();
        registrarEvento("Simulaci\u00f3n detenida");
    }

    public synchronized void reiniciar() {
        detener();
        reiniciarEscenario();
        registrarEvento("Escenario de Lima reiniciado");
    }

    private void reiniciarEscenario() {
        distritos.clear();
        carreteras.clear();
        vehiculos.clear();
        accidentes.clear();
        eventos.clear();
        idsRegistrados.clear();
        emergenciasPendientes.clear();
        ticks = 0;
        controladorClima.setClimaActual(TipoClima.SOLEADO);
        distritos.addAll(EscenarioInicial.crearDistritos());
        carreteras.addAll(EscenarioInicial.crearCarreteras(distritos));
        vehiculos.addAll(EscenarioInicial.crearVehiculos(carreteras, distritos));
        vehiculos.forEach(v -> idsRegistrados.add(v.getId()));
        distritoSeleccionadoId = "MIR";
    }

    private void tickSeguro() {
        if (!ejecutando.get() || pausada.get()) return;
        try {
            actualizar();
        } catch (RuntimeException ex) {
            registrarEvento("Error de simulaci\u00f3n: " + ex.getMessage());
        }
    }

    public void actualizar() {
        ticks++;
        actualizarClima();
        actualizarSemaforos();
        actualizarVehiculos();
        detectarAccidentes();
        procesarEmergencias();
        generarEventos();
    }

    private void actualizarClima() {
        if (ticks % 5 != 0) return;
        if (controladorClima.intentarCambioAutomatico(0.0015 * velocidadSimulacion)) aplicarClima(controladorClima.getClimaActual(), true);
    }

    private void actualizarSemaforos() {
        if (ticks % 2 != 0) return;
        for (Carretera carretera : carreteras) {
            for (Semaforo semaforo : carretera.getSemaforos()) semaforo.actualizar();
        }
    }

    private void actualizarVehiculos() {
        if (trabajadores == null || trabajadores.isShutdown()) return;
        double segundos = 0.2 * velocidadSimulacion;
        List<Callable<Void>> tareas = new ArrayList<>();
        for (Carretera carretera : carreteras) {
            tareas.add(() -> {
                actualizarCarretera(carretera, segundos);
                return null;
            });
        }
        try {
            trabajadores.invokeAll(tareas);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void actualizarCarretera(Carretera carretera, double segundos) {
        List<Vehiculo> enCarretera = vehiculos.stream().filter(v -> carretera.getId().equals(v.getCarreteraId())).toList();
        for (Vehiculo vehiculo : enCarretera) {
            double objetivo = calcularVelocidadObjetivo(vehiculo, carretera, enCarretera);
            vehiculo.actualizarMovimiento(segundos, objetivo);
            if (vehiculo.getPosicion() >= carretera.getLongitud()) moverASiguienteVia(vehiculo, carretera);
            else if (objetivo < 8 && vehiculo.getVelocidadActual() < 6 && vehiculo.getEstado() == EstadoVehiculo.CIRCULANDO) vehiculo.setEstado(EstadoVehiculo.EN_ATASCO);
        }
    }

    private double calcularVelocidadObjetivo(Vehiculo vehiculo, Carretera carretera, List<Vehiculo> enCarretera) {
        if (carretera.isBloqueada() || vehiculo.getEstado() == EstadoVehiculo.ACCIDENTADO || vehiculo.getEstado() == EstadoVehiculo.AVERIADO) return 0;
        double limite = Math.min(vehiculo.getVelocidadMaxima(), carretera.getLimiteVelocidad());
        double factor = vehiculo.getConductor().factorVelocidad(controladorClima.getClimaActual(), carretera.getEstado());
        double objetivo = limite * factor * factorTrafico(carretera, enCarretera.size());
        Vehiculo delante = buscarDelante(vehiculo, carretera, enCarretera);
        if (delante != null) {
            double distancia = distanciaAdelante(vehiculo.getPosicion(), delante.getPosicion(), carretera.getLongitud());
            double seguridad = vehiculo.getConductor().distanciaSeguridad() + vehiculo.getVelocidadActual() * 0.18;
            if (distancia < 4.5) objetivo = 0;
            else if (distancia < seguridad) objetivo = Math.min(objetivo, Math.max(0, delante.getVelocidadActual() * 0.82));
        }
        for (Semaforo semaforo : carretera.getSemaforos()) {
            double distancia = semaforo.getPosicion() - vehiculo.getPosicion();
            if (distancia >= 0 && distancia < 24 && (semaforo.getEstado() == EstadoSemaforo.ROJO || semaforo.getEstado() == EstadoSemaforo.AMARILLO)) {
                if (!esEmergencia(vehiculo)) objetivo = 0;
            }
        }
        if (vehiculo.getDano() > 25) objetivo *= Math.max(0.25, 1.0 - vehiculo.getDano() / 120.0);
        return Math.max(0, objetivo);
    }

    private double factorTrafico(Carretera carretera, int cantidadVehiculos) {
        double base = buscarDistrito(carretera.getDistritoOrigenId()).map(Distrito::getFactorTrafico).orElse(1.0);
        double densidad = cantidadVehiculos / (double) Math.max(2, carretera.getNumeroCarriles() * 3);
        double reduccionZona = Math.max(0, base - 0.9) * 0.28;
        double reduccionDensidad = Math.min(0.42, Math.max(0, densidad - 0.5) * 0.24);
        return Math.max(0.48, 1.0 - reduccionZona - reduccionDensidad);
    }

    private Vehiculo buscarDelante(Vehiculo origen, Carretera carretera, List<Vehiculo> candidatos) {
        Vehiculo mejor = null;
        double distanciaMejor = carretera.getLongitud() + 1;
        for (Vehiculo candidato : candidatos) {
            if (candidato == origen || candidato.getCarril() != origen.getCarril()) continue;
            double distancia = candidato.getPosicion() - origen.getPosicion();
            if (distancia > 0.2 && distancia < distanciaMejor) {
                distanciaMejor = distancia;
                mejor = candidato;
            }
        }
        return mejor;
    }

    private double distanciaAdelante(double origen, double destino, double longitud) {
        double distancia = destino - origen;
        return distancia >= 0 ? distancia : longitud + distancia;
    }

    private void moverASiguienteVia(Vehiculo vehiculo, Carretera carreteraActual) {
        if (vehiculo.getEstado() == EstadoVehiculo.ACCIDENTADO || vehiculo.getEstado() == EstadoVehiculo.AVERIADO) return;
        String distritoLlegada = carreteraActual.getDistritoDestinoId();
        vehiculo.actualizarDistritoActual(distritoLlegada);
        String destino = vehiculo.getDistritoDestinoId();
        if (destino == null || destino.equals(distritoLlegada)) {
            destino = nuevoDestino(distritoLlegada);
            vehiculo.asignarRuta(distritoLlegada, destino);
            registrarEvento(vehiculo.getId() + " inicia ruta hacia " + nombreDistrito(destino));
        }
        Carretera siguiente = seleccionarSiguienteVia(distritoLlegada, destino).orElseGet(() -> salidaAleatoria(distritoLlegada).orElse(carreteraActual));
        vehiculo.asignarCarretera(siguiente, random.nextInt(siguiente.getNumeroCarriles()), 4 + random.nextDouble() * 8);
        vehiculo.actualizarDistritoActual(siguiente.getDistritoOrigenId());
    }

    private Optional<Carretera> seleccionarSiguienteVia(String actual, String destino) {
        List<Carretera> salidas = carreteras.stream().filter(c -> actual.equals(c.getDistritoOrigenId()) && !c.isBloqueada()).toList();
        if (salidas.isEmpty()) return Optional.empty();
        return salidas.stream().min(Comparator.comparingInt(c -> distanciaDistritos(c.getDistritoDestinoId(), destino)));
    }

    private int distanciaDistritos(String origen, String destino) {
        if (Objects.equals(origen, destino)) return 0;
        Queue<String> cola = new ArrayDeque<>();
        Map<String, Integer> distancia = new HashMap<>();
        cola.offer(origen);
        distancia.put(origen, 0);
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            int nivel = distancia.get(actual);
            for (Carretera via : carreteras) {
                if (!actual.equals(via.getDistritoOrigenId())) continue;
                String siguiente = via.getDistritoDestinoId();
                if (distancia.containsKey(siguiente)) continue;
                if (siguiente.equals(destino)) return nivel + 1;
                distancia.put(siguiente, nivel + 1);
                cola.offer(siguiente);
            }
        }
        return 99;
    }

    private Optional<Carretera> salidaAleatoria(String distritoId) {
        List<Carretera> disponibles = carreteras.stream().filter(c -> distritoId.equals(c.getDistritoOrigenId()) && !c.isBloqueada()).toList();
        if (disponibles.isEmpty()) return Optional.empty();
        return Optional.of(disponibles.get(random.nextInt(disponibles.size())));
    }

    private String nuevoDestino(String actual) {
        List<Distrito> opciones = distritos.stream().filter(d -> !d.getId().equals(actual)).toList();
        return opciones.get(random.nextInt(opciones.size())).getId();
    }

    private boolean esEmergencia(Vehiculo vehiculo) {
        return vehiculo.getTipo() == TipoVehiculo.AMBULANCIA || vehiculo.getTipo() == TipoVehiculo.PATRULLA || vehiculo.getTipo() == TipoVehiculo.BOMBEROS;
    }

    private void detectarAccidentes() {
        if (ticks % 3 != 0) return;
        for (Carretera carretera : carreteras) {
            List<Vehiculo> lista = vehiculos.stream().filter(v -> carretera.getId().equals(v.getCarreteraId())).filter(v -> v.getEstado() != EstadoVehiculo.ACCIDENTADO).toList();
            for (Vehiculo vehiculo : lista) {
                Vehiculo delante = buscarDelante(vehiculo, carretera, lista);
                if (delante == null) continue;
                double distancia = delante.getPosicion() - vehiculo.getPosicion();
                double diferencia = Math.max(0, vehiculo.getVelocidadActual() - delante.getVelocidadActual());
                double clima = switch (carretera.getEstado()) {
                    case MOJADA -> 1.6;
                    case MUY_MOJADA -> 2.4;
                    case INUNDADA -> 3.2;
                    default -> 1.0;
                };
                double congestion = 1.0 + nivelCongestion(distritoDeCarretera(carretera, vehiculo.getPosicion())) / 180.0;
                double riesgo = 0.0008 * clima * congestion * vehiculo.getConductor().factorRiesgo() * Math.max(1, diferencia / 8.0);
                if (distancia > 0 && distancia < 9 && diferencia > 6 && random.nextDouble() < riesgo) {
                    crearAccidente(vehiculo, delante, carretera);
                    return;
                }
            }
        }
    }

    private void crearAccidente(Vehiculo a, Vehiculo b, Carretera carretera) {
        double impacto = Math.max(8, Math.abs(a.getVelocidadActual() - b.getVelocidadActual()) + 10);
        double danoA = Math.min(80, impacto * (0.8 + random.nextDouble()));
        double danoB = Math.min(80, impacto * (0.6 + random.nextDouble()));
        a.aplicarDano(danoA);
        b.aplicarDano(danoB);
        a.setVelocidadActual(0);
        b.setVelocidadActual(0);
        a.setEstado(EstadoVehiculo.ACCIDENTADO);
        b.setEstado(EstadoVehiculo.ACCIDENTADO);
        double promedio = (a.getDano() + b.getDano()) / 2.0;
        GravedadAccidente gravedad = promedio >= 70 ? GravedadAccidente.CRITICO : promedio >= 50 ? GravedadAccidente.GRAVE : promedio >= 25 ? GravedadAccidente.MODERADO : GravedadAccidente.LEVE;
        String distritoId = distritoDeCarretera(carretera, a.getPosicion());
        Accidente accidente = new Accidente(GeneradorId.accidente(), LocalDateTime.now(), distritoId, carretera.getNombre(), a.getPosicion(), List.of(a.getId(), b.getId()), controladorClima.getClimaActual(), carretera.getEstado(), gravedad, promedio);
        accidentes.add(accidente);
        registrarEvento("Accidente " + accidente.id() + " en " + nombreDistrito(distritoId) + " - " + carretera.getNombre() + " (" + gravedad + ")");
        if (gravedad == GravedadAccidente.GRAVE || gravedad == GravedadAccidente.CRITICO) {
            emergenciasPendientes.offer(new SolicitudEmergencia(accidente.id(), distritoId));
            registrarEvento("Emergencia solicitada en " + nombreDistrito(distritoId));
        }
    }

    private void procesarEmergencias() {
        if (ticks % 5 != 0) return;
        SolicitudEmergencia solicitud = emergenciasPendientes.poll();
        if (solicitud == null) return;
        Optional<Vehiculo> disponible = vehiculos.stream()
                .filter(this::esEmergencia)
                .filter(v -> v.getEstado() != EstadoVehiculo.ACCIDENTADO && v.getEstado() != EstadoVehiculo.AVERIADO)
                .min(Comparator.comparingInt(v -> distanciaDistritos(distritoVehiculo(v), solicitud.distritoId())));
        if (disponible.isPresent()) {
            Vehiculo vehiculo = disponible.get();
            vehiculo.setEstado(EstadoVehiculo.EN_EMERGENCIA);
            vehiculo.asignarRuta(distritoVehiculo(vehiculo), solicitud.distritoId());
            registrarEvento(vehiculo.getId() + " asignado desde " + nombreDistrito(distritoVehiculo(vehiculo)) + " hacia " + nombreDistrito(solicitud.distritoId()));
        } else {
            emergenciasPendientes.offer(solicitud);
        }
    }

    private Thread crearHilo(Runnable tarea, String nombre) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        return hilo;
    }

    private void generarEventos() {
        if (ticks % 10 != 0 || random.nextDouble() >= probabilidadEventos * velocidadSimulacion) return;
        int tipo = random.nextInt(4);
        if (tipo == 0) {
            List<Vehiculo> disponibles = vehiculos.stream().filter(v -> v.getEstado() == EstadoVehiculo.CIRCULANDO).toList();
            if (!disponibles.isEmpty()) {
                Vehiculo vehiculo = disponibles.get(random.nextInt(disponibles.size()));
                vehiculo.setEstado(EstadoVehiculo.AVERIADO);
                vehiculo.setVelocidadActual(0);
                registrarEvento("Veh\u00edculo averiado en " + nombreDistrito(distritoVehiculo(vehiculo)) + ": " + vehiculo.getId());
            }
        } else if (tipo == 1) {
            Carretera carretera = carreteras.get(random.nextInt(carreteras.size()));
            carretera.setBloqueada(true);
            registrarEvento("V\u00eda bloqueada en " + nombreDistrito(carretera.getDistritoOrigenId()) + ": " + carretera.getNombre());
        } else if (tipo == 2) {
            List<Carretera> bloqueadas = carreteras.stream().filter(Carretera::isBloqueada).toList();
            if (!bloqueadas.isEmpty()) {
                Carretera carretera = bloqueadas.get(random.nextInt(bloqueadas.size()));
                carretera.setBloqueada(false);
                carretera.aplicarClima(controladorClima.getClimaActual());
                registrarEvento("V\u00eda habilitada: " + carretera.getNombre());
            }
        } else {
            TipoClima[] climas = TipoClima.values();
            cambiarClima(climas[random.nextInt(climas.length)]);
        }
    }

    public void cambiarClima(TipoClima clima) {
        controladorClima.setClimaActual(clima);
        aplicarClima(clima, true);
    }

    private void aplicarClima(TipoClima clima, boolean registrar) {
        for (Carretera carretera : carreteras) carretera.aplicarClima(clima);
        if (registrar) registrarEvento("Clima en Lima cambiado a " + clima);
    }

    public void agregarVehiculo(Vehiculo vehiculo, Carretera carretera, int carril, double posicion) {
        if (!idsRegistrados.add(vehiculo.getId())) throw new SimulacionException("Ya existe un veh\u00edculo con ID " + vehiculo.getId());
        vehiculo.asignarCarretera(carretera, carril, posicion);
        vehiculo.asignarRuta(carretera.getDistritoOrigenId(), nuevoDestino(carretera.getDistritoOrigenId()));
        vehiculos.addIfAbsent(vehiculo);
        registrarEvento("Veh\u00edculo creado en " + nombreDistrito(carretera.getDistritoOrigenId()) + ": " + vehiculo.getId());
    }

    public void eliminarVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getCarreteraActual() != null) vehiculo.getCarreteraActual().quitarVehiculo(vehiculo);
        vehiculos.remove(vehiculo);
        idsRegistrados.remove(vehiculo.getId());
        registrarEvento("Veh\u00edculo eliminado: " + vehiculo.getId());
    }

    public Estadisticas estadisticas() {
        long activos = vehiculos.stream().filter(Vehiculo::estaActivo).count();
        long averiados = vehiculos.stream().filter(v -> v.getEstado() == EstadoVehiculo.AVERIADO).count();
        long accidentados = vehiculos.stream().filter(v -> v.getEstado() == EstadoVehiculo.ACCIDENTADO).count();
        long detenidos = vehiculos.size() - activos;
        long emergencias = vehiculos.stream().filter(v -> esEmergencia(v) && v.estaActivo()).count();
        int bloqueadas = (int) carreteras.stream().filter(Carretera::isBloqueada).count();
        double distancia = vehiculos.stream().mapToDouble(Vehiculo::getDistanciaRecorrida).sum();
        double energiaConsumida = vehiculos.stream().mapToDouble(v -> v.getEnergia().capacidad() - v.getEnergia().nivel()).sum();
        return new Estadisticas(activos, detenidos, averiados, accidentados, accidentes.size(), bloqueadas, distancia, energiaConsumida, vehiculos.size(), emergencias);
    }

    public int nivelCongestion(String distritoId) {
        long cantidad = vehiculos.stream().filter(v -> distritoId.equals(distritoVehiculo(v))).filter(v -> v.getEstado() != EstadoVehiculo.ACCIDENTADO).count();
        long vias = carreteras.stream().filter(c -> distritoId.equals(c.getDistritoOrigenId()) || distritoId.equals(c.getDistritoDestinoId())).count();
        double factor = buscarDistrito(distritoId).map(Distrito::getFactorTrafico).orElse(1.0);
        return (int) Math.min(100, Math.round((cantidad / (double) Math.max(1, vias * 2)) * 34 * factor));
    }

    public long vehiculosEnDistrito(String distritoId) {
        return vehiculos.stream().filter(v -> distritoId.equals(distritoVehiculo(v))).count();
    }

    public long accidentesEnDistrito(String distritoId) {
        return accidentes.stream().filter(a -> distritoId.equals(a.distritoId())).count();
    }

    public double velocidadPromedioDistrito(String distritoId) {
        return vehiculos.stream().filter(v -> distritoId.equals(distritoVehiculo(v))).mapToDouble(Vehiculo::getVelocidadActual).average().orElse(0);
    }

    public String distritoMasCongestionado() {
        return distritos.stream().max(Comparator.comparingInt(d -> nivelCongestion(d.getId()))).map(Distrito::getNombre).orElse("-");
    }

    public String distritoMasActivo() {
        return distritos.stream().max(Comparator.comparingLong(d -> vehiculosEnDistrito(d.getId()))).map(Distrito::getNombre).orElse("-");
    }

    public String distritoMasAccidentado() {
        long max = distritos.stream().mapToLong(d -> accidentesEnDistrito(d.getId())).max().orElse(0);
        if (max == 0) return "Sin incidentes";
        return distritos.stream().max(Comparator.comparingLong(d -> accidentesEnDistrito(d.getId()))).map(Distrito::getNombre).orElse("-");
    }

    private String distritoVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getDistritoActualId() != null) return vehiculo.getDistritoActualId();
        return carreteras.stream().filter(c -> c.getId().equals(vehiculo.getCarreteraId())).map(Carretera::getDistritoOrigenId).findFirst().orElse("");
    }

    private String distritoDeCarretera(Carretera carretera, double posicion) {
        return posicion < carretera.getLongitud() * 0.55 ? carretera.getDistritoOrigenId() : carretera.getDistritoDestinoId();
    }

    public String nombreDistrito(String id) {
        return buscarDistrito(id).map(Distrito::getNombre).orElse(id == null ? "-" : id);
    }

    public Optional<Distrito> buscarDistrito(String id) {
        return distritos.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    public void seleccionarDistrito(String id) {
        if (buscarDistrito(id).isPresent()) distritoSeleccionadoId = id;
    }

    public SimulacionGuardada snapshot() {
        return new SimulacionGuardada(new ArrayList<>(distritos), new ArrayList<>(carreteras), new ArrayList<>(vehiculos), new ArrayList<>(accidentes), new ArrayList<>(eventos), controladorClima.getClimaActual(), ticks, distritoSeleccionadoId);
    }

    public synchronized void cargar(SimulacionGuardada guardada) {
        detener();
        distritos.clear();
        carreteras.clear();
        vehiculos.clear();
        accidentes.clear();
        eventos.clear();
        idsRegistrados.clear();
        emergenciasPendientes.clear();
        distritos.addAll(guardada.distritos());
        carreteras.addAll(guardada.carreteras());
        for (Carretera carretera : carreteras) carretera.reconstruirVehiculos();
        Map<String, Carretera> porId = new HashMap<>();
        for (Carretera carretera : carreteras) porId.put(carretera.getId(), carretera);
        for (Vehiculo vehiculo : guardada.vehiculos()) {
            Carretera carretera = porId.get(vehiculo.getCarreteraId());
            if (carretera != null) vehiculo.asignarCarretera(carretera, vehiculo.getCarril(), vehiculo.getPosicion());
            vehiculos.add(vehiculo);
            idsRegistrados.add(vehiculo.getId());
        }
        accidentes.addAll(guardada.accidentes());
        for (Evento evento : guardada.eventos()) eventos.addLast(evento);
        controladorClima.setClimaActual(guardada.clima());
        ticks = guardada.ticks();
        distritoSeleccionadoId = guardada.distritoSeleccionadoId();
        aplicarClima(controladorClima.getClimaActual(), false);
        registrarEvento("Simulaci\u00f3n cargada");
    }

    public void registrarEvento(String mensaje) {
        eventos.addFirst(new Evento(LocalDateTime.now(), mensaje));
        while (eventos.size() > 250) eventos.pollLast();
    }

    public Optional<Vehiculo> buscarVehiculo(String id) { return vehiculos.stream().filter(v -> v.getId().equalsIgnoreCase(id)).findFirst(); }
    public List<Distrito> getDistritos() { return List.copyOf(distritos); }
    public List<Carretera> getCarreteras() { return List.copyOf(carreteras); }
    public List<Vehiculo> getVehiculos() { return List.copyOf(vehiculos); }
    public List<Accidente> getAccidentes() { return List.copyOf(accidentes); }
    public List<Evento> getEventos() { return List.copyOf(eventos); }
    public TipoClima getClimaActual() { return controladorClima.getClimaActual(); }
    public String getDistritoSeleccionadoId() { return distritoSeleccionadoId; }
    public long getTicks() { return ticks; }
    public double getVelocidadSimulacion() { return velocidadSimulacion; }
    public void setVelocidadSimulacion(double velocidadSimulacion) { this.velocidadSimulacion = Math.max(0.5, Math.min(4.0, velocidadSimulacion)); }
    public void setProbabilidadEventos(double probabilidadEventos) { this.probabilidadEventos = Math.max(0, Math.min(0.05, probabilidadEventos)); }
    public double getProbabilidadEventos() { return probabilidadEventos; }
    public EstadoSimulacion getEstado() { return !ejecutando.get() ? EstadoSimulacion.DETENIDA : pausada.get() ? EstadoSimulacion.PAUSADA : EstadoSimulacion.EJECUTANDO; }

    private record SolicitudEmergencia(String accidenteId, String distritoId) { }
}
