package com.example.caipiapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddActivityActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var childrenDatabase: DatabaseReference // Nueva referencia para los niños

    private lateinit var etActivityName: EditText
    private lateinit var etActivityDate: EditText
    private lateinit var etActivityTime: EditText
    private lateinit var etActivityDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_activity) // Asegúrate de que tienes este layout creado

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference.child("actividades")
        childrenDatabase = FirebaseDatabase.getInstance().reference.child("users") // Referencia para los niños

        // Referencias a los elementos de la vista
        etActivityName = findViewById(R.id.et_activity_name)
        etActivityDate = findViewById(R.id.et_activity_date)
        etActivityTime = findViewById(R.id.et_activity_time)
        etActivityDescription = findViewById(R.id.et_activity_description)
        btnSave = findViewById(R.id.btn_save)

        btnSave.setOnClickListener {
            saveActivity()
        }
    }

    private fun saveActivity() {
        val name = etActivityName.text.toString().trim()
        val date = etActivityDate.text.toString().trim()
        val time = etActivityTime.text.toString().trim()
        val description = etActivityDescription.text.toString().trim()

        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un nuevo ID único para la actividad
        val activityId = database.push().key ?: return

        // Crear un objeto de actividad
        val activity = mapOf(
            "nombre_actividad" to name,
            "fecha_actividad" to date,
            "hora_actividad" to time,
            "descripcion_actividad" to description
        )

        // Guardar la actividad en la base de datos
        database.child(activityId).setValue(activity)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Asignar "no asistió" a todos los niños
                    assignAttendanceToChildren(activityId)
                    Toast.makeText(this, "Actividad guardada", Toast.LENGTH_SHORT).show()
                    finish() // Cierra la actividad después de guardar
                } else {
                    Toast.makeText(this, "Error al guardar la actividad", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun assignAttendanceToChildren(activityId: String) {
        // Obtener la lista de niños
        childrenDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Recorre cada usuario
                for (userSnapshot in snapshot.children) {
                    // Recorre cada niño del usuario
                    for (childSnapshot in userSnapshot.child("children").children) {
                        val childId = childSnapshot.key // ID del niño
                        if (childId != null) {
                            // Asignar "no asistió" para este niño en la actividad
                            database.child(activityId).child("asistencias").child(childId).setValue("no asistió")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddActivityActivity, "Error al obtener la lista de niños", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
