import java.io.File

// 1. El molde final para tu Inteligencia Artificial
data class RegistroEntrenamiento(
    val fecha: String,
    val mp25: Double,
    val tempMin: Double,
    val vientoPromedio: Double,
    val humedadPromedio: Double
)

// 2. Función para procesar archivos de Temperatura (Buscamos la MÍNIMA del día)
fun procesarTemperatura(archivos: List<String>): Map<String, Double> {
    val datosPorDia = mutableMapOf<String, MutableList<Double>>()

    for (ruta in archivos) {
        File(ruta).useLines { lineas ->
            lineas.drop(1).forEach { linea ->
                val columnas = linea.split(";")
                if (columnas.size > 5) {
                    val fecha = columnas[3].substringBefore(" ") // Corta "2025-04-01 00:00:00" a "2025-04-01"
                    val temp = columnas[5].toDoubleOrNull()
                    if (temp != null) {
                        datosPorDia.getOrPut(fecha) { mutableListOf() }.add(temp)
                    }
                }
            }
        }
    }
    // Agrupamos y sacamos el valor mínimo de cada día (¡El detonante del Efecto Olla!)
    return datosPorDia.mapValues { it.value.minOrNull() ?: 0.0 }
}

// 3. Función para procesar Viento y Humedad (Buscamos el PROMEDIO del día)
fun procesarPromedios(archivos: List<String>, indiceColumna: Int): Map<String, Double> {
    val datosPorDia = mutableMapOf<String, MutableList<Double>>()

    for (ruta in archivos) {
        File(ruta).useLines { lineas ->
            lineas.drop(1).forEach { linea ->
                val columnas = linea.split(";")
                if (columnas.size > indiceColumna) {
                    val fecha = columnas[3].substringBefore(" ")
                    val valor = columnas[indiceColumna].toDoubleOrNull()
                    if (valor != null) {
                        datosPorDia.getOrPut(fecha) { mutableListOf() }.add(valor)
                    }
                }
            }
        }
    }
    // Agrupamos y sacamos el promedio matemático del día
    return datosPorDia.mapValues { it.value.average() }
}

// 4. El Gran Cruce de Datos
fun main() {
    println("Iniciando Laboratorio de Datos...")

    // A. Pon aquí los nombres de tus 6 archivos de cada cosa (Aquí puse 1 de ejemplo)
    val archivosTemp = listOf("350028_202504_Temperatura.csv" /*, "mayo.csv", "junio.csv"... */)
    val archivosViento = listOf("350028_202504_Viento.csv")
    val archivosHumedad = listOf("350028_202504_PresionHumedad.csv")

    println("Comprimiendo miles de registros de la DMC a datos diarios...")
    val mapaTempMin = procesarTemperatura(archivosTemp)
    val mapaVientoAvg = procesarPromedios(archivosViento, 6) // El viento está en la columna 6
    val mapaHumedadAvg = procesarPromedios(archivosHumedad, 5) // La humedad está en la columna 5

    val registrosFinales = mutableListOf<RegistroEntrenamiento>()

    println("Cruzando datos con el archivo diario del SINCA...")
    // B. Leemos el archivo del SINCA
    File("datos_250401_250930.csv").useLines { lineas ->
        lineas.drop(1).forEach { linea ->
            val columnas = linea.split(";")
            if (columnas.size >= 3) {
                val fechaSinca = columnas[0] // Ej: "250401"
                val mp25String = columnas[2]

                if (fechaSinca.length == 6 && mp25String.isNotBlank()) {
                    // TRUCO DE TRADUCCIÓN: Transformamos "250401" a "2025-04-01"
                    val anio = "20" + fechaSinca.substring(0, 2)
                    val mes = fechaSinca.substring(2, 4)
                    val dia = fechaSinca.substring(4, 6)
                    val fechaUniversal = "$anio-$mes-$dia"

                    val mp25 = mp25String.replace(",", ".").toDoubleOrNull()

                    // Buscamos si para esa fecha tenemos los datos de clima comprimidos
                    val tempMin = mapaTempMin[fechaUniversal]
                    val vientoAvg = mapaVientoAvg[fechaUniversal]
                    val humedadAvg = mapaHumedadAvg[fechaUniversal]

                    // Si tenemos todas las variables de ese día, ¡Hacemos Match!
                    if (mp25 != null && tempMin != null && vientoAvg != null && humedadAvg != null) {
                        registrosFinales.add(
                            RegistroEntrenamiento(fechaUniversal, mp25, tempMin, vientoAvg, humedadAvg)
                        )
                    }
                }
            }
        }
    }

    // C. Verificamos el éxito
    println("\n¡ÉXITO! Se generaron ${registrosFinales.size} días completos para entrenar el modelo.")
    println("Muestra de los primeros 3 días:")
    registrosFinales.take(3).forEach { reg ->
        println("Día: ${reg.fecha} | MP2.5: ${reg.mp25} | T.Min: ${String.format("%.1f", reg.tempMin)}°C | Viento: ${String.format("%.1f", reg.vientoPromedio)} | Humedad: ${String.format("%.1f", reg.humedadPromedio)}%")
    }
}