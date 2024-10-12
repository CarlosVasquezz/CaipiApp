package com.example.caipiapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.google.firebase.database.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var btnGuardar: Button
    private lateinit var btnBack: Button
    private lateinit var tvFechaAsistencia: TextView
    private lateinit var tvNombreActividad: TextView
    private lateinit var childrenLayout: LinearLayout
    private lateinit var childCheckboxes: MutableList<Pair<CheckBox, String>> // (CheckBox, ID del Niño)
    private lateinit var database: DatabaseReference

    private var selectedDate: String = ""
    private var selectedActivity: String = ""
    private lateinit var activityId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        database = FirebaseDatabase.getInstance().reference

        btnBack = findViewById(R.id.btn_back)
        btnGuardar = findViewById(R.id.btn_guardar)
        tvFechaAsistencia = findViewById(R.id.tv_fecha_asistencia)
        tvNombreActividad = findViewById(R.id.tv_nombre_actividad)
        childrenLayout = findViewById(R.id.children_layout)

        activityId = intent.getStringExtra("ACTIVITY_ID") ?: ""
        childCheckboxes = mutableListOf()

        if (activityId.isNotEmpty()) {
            loadSelectedActivity(activityId)
        } else {
            Toast.makeText(this, "ID de actividad no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBack.setOnClickListener { finish() }

        btnGuardar.setOnClickListener { saveAttendance() }
    }

    private fun loadSelectedActivity(activityId: String) {
        database.child("actividades").child(activityId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    selectedActivity = snapshot.child("nombre_actividad").value.toString()
                    selectedDate = snapshot.child("fecha_actividad").value.toString()

                    tvFechaAsistencia.text = "Fecha de asistencia: $selectedDate"
                    tvNombreActividad.text = "Actividad: $selectedActivity"

                    loadChildrenList()
                } else {
                    Toast.makeText(this@AttendanceActivity, "No se encontró la actividad", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AttendanceActivity, "Error al cargar la actividad", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadChildrenList() {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                childCheckboxes.clear()
                childrenLayout.removeAllViews()

                for (user in userSnapshot.children) {
                    for (child in user.child("children").children) {
                        val childObj = child.getValue(Child::class.java)
                        val childId = child.key ?: ""

                        childObj?.let {
                            val checkBox = CheckBox(this@AttendanceActivity).apply {
                                text = it.name
                                isChecked = false // Por defecto no está seleccionado
                            }

                            childCheckboxes.add(Pair(checkBox, childId))
                            childrenLayout.addView(checkBox)
                        }
                    }
                }

                loadAttendanceState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AttendanceActivity, "Error al cargar la lista de niños", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAttendanceState() {
        database.child("actividades").child(activityId).child("asistencias")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val childId = childSnapshot.key
                        val attendanceStatus = childSnapshot.value as? String ?: "no asistió"

                        // Marcar el CheckBox si el niño asistió
                        childCheckboxes.find { it.second == childId }?.first?.isChecked = attendanceStatus == "asistió"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AttendanceActivity, "Error al cargar estado de asistencia", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveAttendance() {
        // Cambiar a MutableMap<String, Any>
        val attendanceUpdates = mutableMapOf<String, Any>()

        for ((checkBox, childId) in childCheckboxes) {
            // Guardamos el estado (asistió/no asistió) como string
            attendanceUpdates[childId] = if (checkBox.isChecked) "asistió" else "no asistió"
        }

        // Actualizar la asistencia en la entrada de actividad
        database.child("actividades").child(activityId).child("asistencias")
            .updateChildren(attendanceUpdates as Map<String, Any>) // Conversión explícita aquí
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Asistencia guardada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar asistencia", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
