<div align="center">
  <h1>CarreterasMojadas</h1>

  <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/VS_Code-007ACC?style=for-the-badge&logo=visual-studio-code&logoColor=white" alt="VS Code" />

  <p><em>Simulación interactiva de tráfico urbano basada en Lima Metropolitana.</em></p>
</div>

---

## Características Destacadas

- **Lima en vivo:** 10 distritos interconectados por vías conocidas (Javier Prado, Vía Expresa, Costa Verde, etc.).
- **Tráfico dinámico:** Múltiples tipos de transporte (autos, motos, buses, bicicletas) con rutas y destinos independientes.
- **Clima y físicas:** Sol, lluvia, tormenta y niebla. Las carreteras mojadas reducen la velocidad y aumentan el riesgo de accidentes.
- **Incidentes:** Vehículos averiados, choques y despliegue automático de ambulancias, patrullas o bomberos.
- **Gestión completa:** Estadísticas en tiempo real, ventanas independientes por distrito, guardado/carga de simulaciones y exportación a CSV.

## Requisitos

- Java 21 o superior.
- Maven instalado.
- Visual Studio Code (con *Extension Pack for Java* recomendado).

## Ejecución Rápida

Para iniciar la simulación directamente en **Visual Studio Code**, sigue estos pasos:

1. Abre la carpeta del proyecto en el editor.
2. Abre la terminal integrada del programa.
3. Escribe el comando `mvn compile exec:java` y presiona Enter.

*Nota: Alternativamente, puedes abrir el archivo `src/main/java/carreterasmojadas/Main.java` y utilizar la opción "Run Java" que aparece en el propio editor.*
