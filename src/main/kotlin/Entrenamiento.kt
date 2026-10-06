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

// 5. El Algoritmo de Inteligencia Artificial (Descenso de Gradiente)
fun entrenarModelo(datos: List<RegistroEntrenamiento>) {
    println("\n=== INICIANDO ENTRENAMIENTO DEL MODELO ===")
    println("Procesando ${datos.size} días de historial...")

    // 1. Empezamos con pesos en cero (El modelo no sabe nada aún)
    var pesoTemp = 0.0
    var pesoViento = 0.0
    var pesoHumedad = 0.0
    var constanteBase = 0.0

    // 2. Configuramos cómo va a aprender
    val tasaAprendizaje = 0.00001 // Qué tan rápido cambia de opinión (Si es muy alto, se vuelve loco)
    val epocas = 50000 // Cuántas veces va a repasar el historial completo

    // 3. Empieza a estudiar
    for (epoca in 1..epocas) {
        var errorTotal = 0.0

        for (registro in datos) {
            // A. Intenta adivinar el humo de ese día con su fórmula actual
            val prediccion = constanteBase +
                    (pesoTemp * registro.tempMin) +
                    (pesoViento * registro.vientoPromedio) +
                    (pesoHumedad * registro.humedadPromedio)

            // B. Calcula qué tan equivocado estaba comparado al SINCA real
            val error = prediccion - registro.mp25
            errorTotal += error * error // Se eleva al cuadrado para castigar los errores grandes

            // C. ¡Aprende! Corrige sus pesos ligeramente en la dirección correcta
            constanteBase -= tasaAprendizaje * error
            pesoTemp -= tasaAprendizaje * error * registro.tempMin
            pesoViento -= tasaAprendizaje * error * registro.vientoPromedio
            pesoHumedad -= tasaAprendizaje * error * registro.humedadPromedio
        }

        // Cada 10.000 repasadas, nos avisa cómo va su nivel de error
        if (epoca % 10000 == 0) {
            println("Época $epoca -> Nivel de Error (MSE): ${String.format("%.2f", errorTotal / datos.size)}")
        }
    }

    println("\n=== ¡ENTRENAMIENTO FINALIZADO! ===")
    println("El modelo ha descubierto la fórmula del Efecto Olla para Talca.")
    println("Copia estos números para tu programa final (AlertaEnVivo.kt):\n")

    println("val constanteBase = $constanteBase")
    println("val pesoTemperatura = $pesoTemp")
    println("val pesoViento = $pesoViento")
    println("val pesoHumedad = $pesoHumedad")

    println("\nLa fórmula matemática resultante es:")
    println("MP2.5 = $constanteBase + ($pesoTemp * Temperatura) + ($pesoViento * Viento) + ($pesoHumedad * Humedad)")
}

// 4. El Gran Cruce de Datos
fun main() {
    println("Iniciando Laboratorio de Datos...")

    val archivosTemp = listOf(
        "Temp 2025/350028_202504_Temperatura.csv",
        "Temp 2025/350028_202505_Temperatura.csv",
        "Temp 2025/350028_202506_Temperatura.csv",
        "Temp 2025/350028_202507_Temperatura.csv",
        "Temp 2025/350028_202508_Temperatura.csv",
        "Temp 2025/350028_202509_Temperatura.csv"
    )

    val archivosViento = listOf(
        "Viento 2025/350028_202504_Viento.csv",
        "Viento 2025/350028_202505_Viento.csv",
        "Viento 2025/350028_202506_Viento.csv",
        "Viento 2025/350028_202507_Viento.csv",
        "Viento 2025/350028_202508_Viento.csv",
        "Viento 2025/350028_202509_Viento.csv"
    )

    val archivosHumedad = listOf(
        "Presion y Humedad 2025/350028_202504_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202505_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202506_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202507_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202508_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202509_PresionHumedad.csv"
    )

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
    println("\nEXITO! Se generaron ${registrosFinales.size} dias completos para entrenar el modelo.")
    println("Muestra de los primeros 3 dias:")
    registrosFinales.take(3).forEach { reg ->
        println("Dia: ${reg.fecha} | MP2.5: ${reg.mp25} | T.Min: ${String.format("%.1f", reg.tempMin)} C | Viento: ${String.format("%.1f", reg.vientoPromedio)} | Humedad: ${String.format("%.1f", reg.humedadPromedio)}%")
    }
    // D. ¡A entrenar!
    entrenarModelo(registrosFinales)
}