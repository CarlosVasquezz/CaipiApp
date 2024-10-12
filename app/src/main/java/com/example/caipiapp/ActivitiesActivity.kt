package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ActivitiesActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var activitiesLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activities)

        // Referencia a Firebase
        database = FirebaseDatabase.getInstance().reference.child("actividades")

        // Referencias a los elementos de la vista
        val btnBack: Button = findViewById(R.id.btn_back)
        val btnAddActivity: Button = findViewById(R.id.btn_add_activity)
        activitiesLayout = findViewById(R.id.activities_layout) // LinearLayout donde se agregarán las actividades dinámicamente

        btnBack.setOnClickListener {
            finish() // Cerrar la actividad actual
        }

        btnAddActivity.setOnClickListener {
            // Iniciar la actividad para añadir una nueva actividad
            val intent = Intent(this, AddActivityActivity::class.java)
            startActivity(intent)
        }

        // Cargar las actividades desde Firebase
        loadActivitiesFromFirebase()
    }

    private fun loadActivitiesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activitiesLayout.removeAllViews() // Limpiar cualquier vista existente

                // Iterar sobre cada actividad en Firebase
                for (activitySnapshot in snapshot.children) {
                    val activityId = activitySnapshot.key ?: "" // Obtener el ID de la actividad
                    val nombre = activitySnapshot.child("nombre_actividad").value.toString()
                    val fecha = activitySnapshot.child("fecha_actividad").value.toString()
                    val hora = activitySnapshot.child("hora_actividad").value.toString()
                    val descripcion = activitySnapshot.child("descripcion_actividad").value.toString()

                    // Crear un LinearLayout para cada actividad
                    val activityLayout = LinearLayout(this@ActivitiesActivity)
                    activityLayout.orientation = LinearLayout.VERTICAL
                    activityLayout.setPadding(0, 16, 0, 16)

                    // Crear un TextView para mostrar la actividad
                    val activityTextView = TextView(this@ActivitiesActivity)
                    activityTextView.text = "$nombre\nFecha: $fecha - Hora: $hora\nDescripción: $descripcion"
                    activityTextView.textSize = 16f

                    // Crear un botón para registrar asistencia
                    val attendanceButton = Button(this@ActivitiesActivity)
                    attendanceButton.text = "Registrar Asistencia"
                    attendanceButton.setOnClickListener {
                        // Crear un Intent para iniciar la AttendanceActivity
                        val intent = Intent(this@ActivitiesActivity, AttendanceActivity::class.java)
                        intent.putExtra("ACTIVITY_ID", activityId) // Pasar el ID de la actividad
                        startActivity(intent)
                    }

                    // Añadir el TextView y el botón al LinearLayout de la actividad
                    activityLayout.addView(activityTextView)
                    activityLayout.addView(attendanceButton)

                    // Añadir el LinearLayout de la actividad al LinearLayout principal
                    activitiesLayout.addView(activityLayout)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores en la consulta
                Toast.makeText(this@ActivitiesActivity, "Error al cargar actividades: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
