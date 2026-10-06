import java.io.File
import java.net.URL
import java.net.HttpURLConnection

// Estructura para la calidad del aire.
data class RegistroCalidadAire(
    val fechaHora: String,
    val mp25: Double
)

// Estructura para el clima.
data class RegistroClima(
    val fechaHora: String,
    val temperatura: Double,
    val viento: Double,
    val humedad: Double
)

data class RegistroConsolidado(
    val fechaHora: String,
    val mp25: Double,
    val temperatura: Double,
    val viento: Double
)

// Función que lee y procesa el archivo (NO ES EN VIVO)
fun leerDatosSinca(rutaArchivo: String): List<RegistroCalidadAire> {
    val registros = mutableListOf<RegistroCalidadAire>()

    // File().useLines abre el archivo, lo lee línea por línea y lo cierra automáticamente
    File(rutaArchivo).useLines { lineas ->

        // drop(1) se salta la primera línea porque son los nombres de las columnas
        val lineasDeDatos = lineas.drop(1)

        for (linea in lineasDeDatos) {
            // Aquí está la magia: cortamos la línea en cada punto y coma
            val columnas = linea.split(";")

            // Nos aseguramos de que la línea tenga al menos 3 datos (Fecha, MP10, MP2.5)
            if (columnas.size >= 3) {

                // Las fechas en tu archivo vienen con comillas ("2026-08-30 11:00"). Las quitamos.
                val fecha = columnas[0].replace("\"", "")

                // El dato de MP2.5 está en la posición 2.
                val mp25String = columnas[2]

                // toDoubleOrNull() intenta convertir el texto a número. Si está vacío o falla, devuelve null.
                val mp25 = mp25String.toDoubleOrNull()

                // Solo guardamos el registro si el sensor efectivamente capturó un número válido
                if (mp25 != null) {
                    registros.add(RegistroCalidadAire(fecha, mp25))
                }
            }
        }
    }
    return registros
}

// Función que obtiene EN VIVO el ultimo dato de MP2.5
fun obtenerDatoEnVivo() {
    val urlFea = "https://sinca.mma.gob.cl/cgi-bin/registros2k19.cgi?domain=CONAMA&stn=710&output=U.C.%20Maule"

    // 1. Descargamos el texto de forma invisible (toma 1 o 2 segundos)
    val textoDescargado = URL(urlFea).readText()

    // 2. Cortamos el texto gigante separándolo en una lista de líneas
    val lineas = textoDescargado.lines()

    // 3. Buscamos la última línea de abajo hacia arriba que tenga texto
    val ultimaLineaValida = lineas.last { it.isNotBlank() }

    // 4. Cortamos esa última línea usando el punto y coma
    val columnas = ultimaLineaValida.split(";")

    // ¡Listo! Aislamos la fecha y el dato de MP 2,5
    val fechaHora = columnas[0].replace("\"", "")
    val mp25Actual = columnas[2]

    println("Lectura en vivo exitosa: A las $fechaHora el MP2.5 es de $mp25Actual")
}

// Funcion para obtener datos "Y" EN VIVO desde DMC (Funciona mal actualmente)
fun obtenerClimaEnVivo() {
    val correo = "gcea06@gmail.com"
    val estacion = "350028" // (350028 = Club Aéreo Talca)
    val apiKey = "979281ec20368ec532418f52"

    val urlDmc = "https://climatologia.meteochile.gob.cl/application/servicios/getDatosRecientesEma/$estacion/$correo/$apiKey"

    println("Conectando a la estación DMC $estacion en Talca...")

    try {
        val conexion = URL(urlDmc).openConnection() as HttpURLConnection
        conexion.requestMethod = "GET"

        // Camuflamos el programa para que el firewall no lo bloquee por ser un "bot"
        conexion.setRequestProperty("User-Agent", "Mozilla/5.0")

        val codigoRespuesta = conexion.responseCode

        if (codigoRespuesta == 200) {
            // Si el servidor dice OK (200), leemos los datos
            val respuesta = conexion.inputStream.bufferedReader().readText()
            println("¡Conexión exitosa! Los datos recibidos son:")
            println(respuesta)
        } else {
            // Si hay error, leemos el mensaje real del gobierno
            val errorReal = conexion.errorStream?.bufferedReader()?.readText() ?: "Sin detalles"
            println("El servidor rechazó la petición. Código HTTP: $codigoRespuesta")
            println("Mensaje: $errorReal")
        }

    } catch (e: Exception) {
        println("Error de red crítico: ${e.message}")
    }
}

// Mock para hacer funcionar obtenerClimaEnVivo().
fun obtenerClimaEnVivoMock(): RegistroClima {
    println("SIMULACIÓN: Generando datos climáticos falsos para pruebas...")
    // Devolvemos un clima perfecto para el "efecto olla" (mucho frío, sin viento)
    return RegistroClima(
        fechaHora = "2026-10-05 14:00",
        temperatura = 3.5,
        viento = 0.0,
        humedad = 85.0
    )
}

fun cruzarDatos(aire: List<RegistroCalidadAire>, clima: List<RegistroClima>): List<RegistroConsolidado> {
    val consolidados = mutableListOf<RegistroConsolidado>()

    // Convertimos la lista de clima en un "Diccionario" para buscar rapidísimo por fecha
    val diccionarioClima = clima.associateBy { it.fechaHora }

    // Recorremos los datos del aire
    for (registroAire in aire) {
        // Buscamos si para esa misma fecha/hora exacta hay un dato de clima
        val registroClimaCoincidente = diccionarioClima[registroAire.fechaHora]

        // Si encontramos ambas piezas, armamos el registro final (el Match)
        if (registroClimaCoincidente != null) {
            consolidados.add(
                RegistroConsolidado(
                    fechaHora = registroAire.fechaHora,
                    mp25 = registroAire.mp25,
                    temperatura = registroClimaCoincidente.temperatura,
                    viento = registroClimaCoincidente.viento
                )
            )
        }
    }
    return consolidados
}

fun main() {
    println("=== FASE 1: PREPARANDO EL ENTRENAMIENTO (HISTORICO) ===")

    // 1. Leemos el aire (nombre exacto del archivo)
    val historialAire = leerDatosSinca("U.C._Maule_2026-10-05-10-24.csv")

    // 2. Simulamos la lectura del historial de clima (Tu próxima tarea será leer esto de un CSV real)
    val historialClimaFalso = listOf(
        RegistroClima("2026-10-04 10:00", 12.0, 5.0, 60.0),
        RegistroClima("2026-10-04 11:00", 14.5, 8.0, 55.0),
        RegistroClima("2026-10-04 12:00", 16.0, 10.0, 50.0)
    )

    // 3. ¡El Match! Unimos ambos mundos
    val datosConsolidados = cruzarDatos(historialAire, historialClimaFalso)

    println("Se logro hacer Match en ${datosConsolidados.size} horas distintas.")
    println("Muestra de la Tabla Maestra (Aire + Clima):")
    datosConsolidados.take(3).forEach {
        println("Fecha: ${it.fechaHora} -> MP2.5: ${it.mp25} | Temp: ${it.temperatura} C | Viento: ${it.viento} km/h")
    }

    println("\n=== FASE 2: SISTEMA DE ALERTA EN VIVO ===")

    // 4. Tu conexión real al SINCA que ya dominaste
    obtenerDatoEnVivo()

    // 5. La conexión simulada al clima (hasta que la DMC active la llave)
    val climaActual = obtenerClimaEnVivoMock()
    println("Lectura climatica (Simulada): A las ${climaActual.fechaHora} la Temp es ${climaActual.temperatura}°C y Viento ${climaActual.viento} km/h")

    // Aquí es donde, en la Semana 10, programarás el IF que detona la alarma
    println("\n-> Analisis en progreso... (Algoritmo pendiente de implementacion)")
}