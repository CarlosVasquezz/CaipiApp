package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.widget.Button
import android.widget.Toast

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
                    val nombre = activitySnapshot.child("nombre_actividad").value.toString()
                    val fecha = activitySnapshot.child("fecha_actividad").value.toString()
                    val hora = activitySnapshot.child("hora_actividad").value.toString()
                    val descripcion = activitySnapshot.child("descripcion_actividad").value.toString()

                    // Crear una nueva vista TextView para cada actividad
                    val activityTextView = TextView(this@ActivitiesActivity)
                    activityTextView.text = "$nombre\nFecha: $fecha - Hora: $hora\nDescripción: $descripcion"
                    activityTextView.textSize = 16f
                    activityTextView.setPadding(0, 16, 0, 16)

                    // Añadir el TextView al LinearLayout
                    activitiesLayout.addView(activityTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores en la consulta
                Toast.makeText(this@ActivitiesActivity, "Error al cargar actividades: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
