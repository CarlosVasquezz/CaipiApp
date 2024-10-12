package com.example.caipiapp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.database.*

class AttendanceListActivity : AppCompatActivity() {

    private lateinit var attendanceLayout: LinearLayout
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_list)

        attendanceLayout = findViewById(R.id.attendance_layout)
        database = FirebaseDatabase.getInstance().reference

        loadActivitiesWithAttendance()
    }

    private fun loadActivitiesWithAttendance() {
        database.child("actividades").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    attendanceLayout.removeAllViews()

                    for (activitySnapshot in snapshot.children) {
                        val nombreActividad = activitySnapshot.child("nombre_actividad").getValue(String::class.java) ?: "Sin nombre"
                        val fechaActividad = activitySnapshot.child("fecha_actividad").getValue(String::class.java) ?: "Fecha no disponible"
                        val horaActividad = activitySnapshot.child("hora_actividad").getValue(String::class.java) ?: "Hora no disponible"

                        // Crear CardView para la actividad
                        val activityCard = CardView(this@AttendanceListActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(8, 0, 8, 16) // Configurar márgenes
                            }
                            radius = 8f
                            elevation = 4f
                            setCardBackgroundColor(0xFFF0F0F0.toInt())
                        }

                        // Crear LinearLayout dentro del CardView
                        val cardLayout = LinearLayout(this@AttendanceListActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(16, 16, 16, 16) // Configurar padding
                        }

                        // Crear vista para la actividad
                        val activityInfo = TextView(this@AttendanceListActivity).apply {
                            textSize = 20f
                            text = "Actividad: $nombreActividad\nFecha: $fechaActividad\nHora: $horaActividad"
                            setPadding(0, 12, 0, 12)
                        }

                        // Agregar la información de la actividad al layout de la tarjeta
                        cardLayout.addView(activityInfo)

                        // Mostrar la lista de niños y su asistencia
                        val childrenAttendance = activitySnapshot.child("asistencias")
                        if (childrenAttendance.exists()) {
                            displayAttendance(childrenAttendance, cardLayout)
                        } else {
                            val noChildrenView = TextView(this@AttendanceListActivity).apply {
                                text = "No hay niños registrados para esta actividad."
                                textSize = 16f
                            }
                            cardLayout.addView(noChildrenView)
                        }

                        // Agregar el layout de la tarjeta al CardView
                        activityCard.addView(cardLayout)

                        // Agregar el CardView al layout principal
                        attendanceLayout.addView(activityCard)
                    }
                } else {
                    Toast.makeText(this@AttendanceListActivity, "No se encontraron actividades", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AttendanceListActivity, "Error al cargar actividades", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayAttendance(childrenAttendance: DataSnapshot, cardLayout: LinearLayout) {
        for (childSnapshot in childrenAttendance.children) {
            val childId = childSnapshot.key ?: "Sin ID"
            val attendanceStatus = childSnapshot.value as? String ?: "Sin estado"
            loadChildName(childId, attendanceStatus, cardLayout)
        }
    }

    private fun loadChildName(childId: String, attendanceStatus: String, cardLayout: LinearLayout) {
        // Solo se busca en la rama de usuarios, en lugar de recorrer todos los usuarios.
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                for (user in userSnapshot.children) {
                    val child = user.child("children").child(childId)
                    if (child.exists()) {
                        val childName = child.child("name").getValue(String::class.java) ?: "Sin nombre"
                        val attendanceInfo = TextView(this@AttendanceListActivity).apply {
                            text = "$childName - $attendanceStatus"
                            textSize = 16f
                        }
                        cardLayout.addView(attendanceInfo) // Agregar el info de asistencia al layout de la tarjeta
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AttendanceListActivity, "Error al cargar nombres de niños", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
