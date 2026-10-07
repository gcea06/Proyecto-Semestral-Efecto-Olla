import java.io.File

// El molde final para tu Inteligencia Artificial
data class RegistroEntrenamiento(
    val fecha: String,
    val mp25: Double,
    val tempMin: Double,
    val vientoPromedio: Double,
    val humedadPromedio: Double
)

// Función dinámica que busca la columna por su nombre exacto
fun procesarColumnaDinamica(archivos: List<String>, nombreColumna: String, esMinima: Boolean): Map<String, Double> {
    val datosPorDia = mutableMapOf<String, MutableList<Double>>()

    for (ruta in archivos) {
        File(ruta).useLines { lineas ->
            val iterador = lineas.iterator()
            if (!iterador.hasNext()) return@useLines // Salta si el archivo está vacío

            // Lee la primera línea (cabecera) y busca los índices reales
            val cabecera = iterador.next().split(";")
            val indiceDato = cabecera.indexOf(nombreColumna)
            val indiceMomento = cabecera.indexOf("momento")

            // Si encontró ambas columnas, procesa el resto del archivo
            if (indiceDato != -1 && indiceMomento != -1) {
                iterador.forEachRemaining { linea ->
                    val columnas = linea.split(";")
                    if (columnas.size > maxOf(indiceDato, indiceMomento)) {
                        val fecha = columnas[indiceMomento].substringBefore(" ")
                        val valor = columnas[indiceDato].toDoubleOrNull()
                        if (valor != null) {
                            datosPorDia.getOrPut(fecha) { mutableListOf() }.add(valor)
                        }
                    }
                }
            }
        }
    }

    // Devuelve el mínimo diario o el promedio según lo que pida la variable
    return if (esMinima) {
        datosPorDia.mapValues { it.value.minOrNull() ?: 0.0 }
    } else {
        datosPorDia.mapValues { it.value.average() }
    }
}

// El Algoritmo de Inteligencia Artificial (Descenso de Gradiente)
fun entrenarModelo(datos: List<RegistroEntrenamiento>) {
    println("\n=== INICIANDO ENTRENAMIENTO DEL MODELO ===")
    println("Procesando ${datos.size} dias de historial...")

    // 1. Empezamos con pesos en cero (El modelo no sabe nada aún)
    var pesoTemp = 0.0
    var pesoViento = 0.0
    var pesoHumedad = 0.0
    var constanteBase = 0.0

    // 2. Configuramos cómo va a aprender
    val tasaAprendizaje = 0.000001 // Qué tan rápido cambia de opinión (Si es muy alto, se vuelve loco)
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
            println("Epoca $epoca -> Nivel de Error (MSE): ${String.format("%.2f", errorTotal / datos.size)}")
        }
    }

    println("\n=== ENTRENAMIENTO FINALIZADO! ===")
    println("El modelo ha descubierto la formula del Efecto Olla para Talca.")
    println("Copia estos numeros para tu programa final (AlertaEnVivo.kt):\n")

    println("val constanteBase = $constanteBase")
    println("val pesoTemperatura = $pesoTemp")
    println("val pesoViento = $pesoViento")
    println("val pesoHumedad = $pesoHumedad")

    println("\nLa formula matematica resultante es:")
    println("MP2.5 = $constanteBase + ($pesoTemp * Temperatura) + ($pesoViento * Viento) + ($pesoHumedad * Humedad)")
}

// El Cruce de Datos
fun main() {
    println("Iniciando Laboratorio de Datos...")

    val archivosTemp = listOf(
        "Temp 2025/350028_202504_Temperatura.csv",
        "Temp 2025/350028_202505_Temperatura.csv",
        "Temp 2025/350028_202506_Temperatura.csv",
        "Temp 2025/350028_202507_Temperatura.csv",
        "Temp 2025/350028_202508_Temperatura.csv",
        "Temp 2025/350028_202509_Temperatura.csv",
        "Temp 2024/350028_202404_Temperatura.csv",
        "Temp 2024/350028_202405_Temperatura.csv",
        "Temp 2024/350028_202406_Temperatura.csv",
        "Temp 2024/350028_202407_Temperatura.csv",
        "Temp 2024/350028_202408_Temperatura.csv",
        "Temp 2024/350028_202409_Temperatura.csv",
        "Temp 2023/350028_202304_Temperatura.csv",
        "Temp 2023/350028_202305_Temperatura.csv",
        "Temp 2023/350028_202306_Temperatura.csv",
        "Temp 2023/350028_202307_Temperatura.csv",
        "Temp 2023/350028_202308_Temperatura.csv",
        "Temp 2023/350028_202309_Temperatura.csv"
    )

    val archivosViento = listOf(
        "Viento 2025/350028_202504_Viento.csv",
        "Viento 2025/350028_202505_Viento.csv",
        "Viento 2025/350028_202506_Viento.csv",
        "Viento 2025/350028_202507_Viento.csv",
        "Viento 2025/350028_202508_Viento.csv",
        "Viento 2025/350028_202509_Viento.csv",
        "Viento 2024/350028_202404_Viento.csv",
        "Viento 2024/350028_202405_Viento.csv",
        "Viento 2024/350028_202406_Viento.csv",
        "Viento 2024/350028_202407_Viento.csv",
        "Viento 2024/350028_202408_Viento.csv",
        "Viento 2024/350028_202409_Viento.csv",
        "Viento 2023/350028_202304_Viento.csv",
        "Viento 2023/350028_202305_Viento.csv",
        "Viento 2023/350028_202306_Viento.csv",
        "Viento 2023/350028_202307_Viento.csv",
        "Viento 2023/350028_202308_Viento.csv",
        "Viento 2023/350028_202309_Viento.csv"
    )

    val archivosHumedad = listOf(
        "Presion y Humedad 2025/350028_202504_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202505_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202506_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202507_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202508_PresionHumedad.csv",
        "Presion y Humedad 2025/350028_202509_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202404_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202405_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202406_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202407_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202408_PresionHumedad.csv",
        "Presion y Humedad 2024/350028_202409_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202304_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202305_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202306_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202307_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202308_PresionHumedad.csv",
        "Presion y Humedad 2023/350028_202309_PresionHumedad.csv"
    )

    println("Comprimiendo miles de registros de la DMC a datos diarios...")
    val mapaTempMin = procesarColumnaDinamica(archivosTemp, "ts", esMinima = true)
    val mapaVientoAvg = procesarColumnaDinamica(archivosViento, "ffInst", esMinima = false)
    val mapaHumedadAvg = procesarColumnaDinamica(archivosHumedad, "hr", esMinima = false)

    val registrosFinales = mutableListOf<RegistroEntrenamiento>()

    println("Cruzando datos con los archivos diarios del SINCA...")

    // B. Creamos una lista con todos los años de humo
    val archivosSinca = listOf(
        "datos_230401_230930.csv", // El humo de 2023
        "datos_240401_240930.csv", // El humo de 2024
        "datos_250401_250930.csv"  // El humo de 2025
    )

    // Envolvemos tu código actual en un for para leerlos todos
    for (archivoHumo in archivosSinca) {
        File(archivoHumo).useLines { lineas ->
            lineas.drop(1).forEach { linea ->
                val columnas = linea.split(";")
                if (columnas.size >= 3) {
                    val fechaSinca = columnas[0]
                    val mp25String = columnas[2]

                    if (fechaSinca.length == 6 && mp25String.isNotBlank()) {
                        // Traducción de fecha (Tu mismo código)
                        val anio = "20" + fechaSinca.substring(0, 2)
                        val mes = fechaSinca.substring(2, 4)
                        val dia = fechaSinca.substring(4, 6)
                        val fechaUniversal = "$anio-$mes-$dia"

                        val mp25 = mp25String.replace(",", ".").toDoubleOrNull()

                        val tempMin = mapaTempMin[fechaUniversal]
                        val vientoAvg = mapaVientoAvg[fechaUniversal]
                        val humedadAvg = mapaHumedadAvg[fechaUniversal]

                        if (mp25 != null && tempMin != null && vientoAvg != null && humedadAvg != null) {
                            registrosFinales.add(
                                RegistroEntrenamiento(fechaUniversal, mp25, tempMin, vientoAvg, humedadAvg)
                            )
                        }
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