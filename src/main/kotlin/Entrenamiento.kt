
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