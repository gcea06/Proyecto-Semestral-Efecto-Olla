import java.net.HttpURLConnection
import java.net.URL

// Molde para guardar el clima actual
data class ClimaActual(
    val temperatura: Double,
    val viento: Double,
    val humedad: Double,
    val esSimulado: Boolean
)

// 1. Función que se conecta al Gobierno en tiempo real
fun obtenerClimaDMC(): ClimaActual {
    // Tus credenciales (Recuerda que si el 404 persiste, la llave aún no está activa)
    val urlDMC = "https://climatologia.meteochile.gob.cl/application/servicios/getDatosRecientesEma/350028/gcea06@gmail.com/979281ec20368ec532418f52"

    return try {
        println("Conectando a la estacion DMC 350028 (Talca)...")
        val conexion = URL(urlDMC).openConnection() as HttpURLConnection
        conexion.requestMethod = "GET"
        conexion.connectTimeout = 5000 // Si el gobierno tarda más de 5 segundos, aborta
        conexion.readTimeout = 5000

        if (conexion.responseCode == 200) {
            val respuestaJson = conexion.inputStream.bufferedReader().readText()
            println("Conexion exitosa! Leyendo sensores...")

            // Extracción manual (sin librerías externas) buscando las etiquetas de la DMC
            val tempStr = respuestaJson.substringAfter("\"ts\":").substringBefore(",").trim()
            val vientoStr = respuestaJson.substringAfter("\"ffInst\":").substringBefore(",").trim()
            val humedadStr = respuestaJson.substringAfter("\"hr\":").substringBefore(",").trim()

            ClimaActual(
                temperatura = tempStr.toDoubleOrNull() ?: 0.0,
                viento = vientoStr.toDoubleOrNull() ?: 0.0,
                humedad = humedadStr.toDoubleOrNull() ?: 0.0,
                esSimulado = false
            )
        } else {
            throw Exception("Error del servidor: HTTP ${conexion.responseCode}")
        }
    } catch (e: Exception) {
        println("ADVERTENCIA: La API del gobierno no responde (${e.message}).")
        println("Activando protocolo de respaldo (Datos Simulados)...")

        // El "Plan B" para que tu presentación nunca falle
        ClimaActual(
            temperatura = 2.5,  // Mucho frío
            viento = 0.5,       // Casi sin viento
            humedad = 88.0,     // Muy húmedo
            esSimulado = true
        )
    }
}

// 2. Tu Motor de Inteligencia Artificial (La fórmula descubierta)
fun evaluarEfectoOlla(clima: ClimaActual) {
    // Los pesos matemáticos de tu entrenamiento de 3 años
    val constanteBase = 5.788166121767532
    val pesoTemperatura = -1.861654305662162
    val pesoViento = -4.1206265150483325
    val pesoHumedad = 0.5984243936770394

    // Ecuación lineal predictiva
    val mp25Proyectado = constanteBase +
            (pesoTemperatura * clima.temperatura) +
            (pesoViento * clima.viento) +
            (pesoHumedad * clima.humedad)

    println("\n=======================================================")
    println("Condiciones Actuales en la Cuenca de Talca:")
    println("Temperatura: ${clima.temperatura} C")
    println("Viento:      ${clima.viento} nudos")
    println("Humedad:     ${clima.humedad} %")
    println("-------------------------------------------------------")
    println("PRONOSTICO IA: Se proyectan ${String.format("%.1f", mp25Proyectado)} ug/m3 de MP2.5")
    println("=======================================================\n")

    // 3. Sistema de Alertas
    when {
        mp25Proyectado >= 110.0 -> {
            println("EMERGENCIA AMBIENTAL DECLARADA")
            println("Condiciones criticas de Efecto Olla. Ventilacion nula.")
            println("Accion: Sugerir prohibicion total de humos visibles.")
        }
        mp25Proyectado >= 80.0 -> {
            println("ALERTA DE PRE-EMERGENCIA")
            println("Alta probabilidad de estancamiento termico.")
            println("Accion: Sugerir restriccion de calefactores a lena.")
        }
        mp25Proyectado >= 50.0 -> {
            println("ALERTA AMARILLA")
            println("Condiciones propicias para la acumulación de humo.")
            println("Accion: Monitoreo preventivo.")
        }
        else -> {
            println("ESTADO NORMAL")
            println("La atmosfera permite una circulacion segura. Sin riesgo inminente.")
        }
    }
}

// 4. El punto de entrada principal
fun main() {
    println("=======================================================")
    println("  SISTEMA PREDICTIVO DE CALIDAD DE AIRE - TALCA 2026")
    println("=======================================================\n")

    val climaHoy = obtenerClimaDMC()

    evaluarEfectoOlla(climaHoy)
}