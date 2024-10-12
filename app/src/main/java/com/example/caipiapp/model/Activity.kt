package com.example.caipiapp.model

data class Activity(
    val nombre_actividad: String = "",
    val fecha_actividad: String = "",
    val hora_actividad: String = "",
    val descripcion_actividad: String = "",
    val asistencia: Map<String, Boolean> = mapOf() // 'true' para asistió, 'false' para no asistió
)

