package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.example.caipiapp.model.Activity // Asegúrate de tener esta clase definida para las actividades
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChildInfoActivity : AppCompatActivity() {

    private lateinit var childNameTextView: TextView
    private lateinit var childAgeTextView: TextView
    private lateinit var childGenderTextView: TextView
    private lateinit var childBirthDateTextView: TextView
    private lateinit var childPhoneTextView: TextView
    private lateinit var childParentsTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var attendanceLayout: LinearLayout // LinearLayout para mostrar las actividades

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_info)

        // Inicialización de vistas
        childNameTextView = findViewById(R.id.childNameTextView)
        childAgeTextView = findViewById(R.id.childAgeTextView)
        childGenderTextView = findViewById(R.id.childGenderTextView)
        childBirthDateTextView = findViewById(R.id.childBirthDateTextView)
        childPhoneTextView = findViewById(R.id.childPhoneTextView)
        childParentsTextView = findViewById(R.id.childParentsTextView)
        logoutButton = findViewById(R.id.logoutButton)
        attendanceLayout = findViewById(R.id.attendanceLayout) // Inicializa el layout para las actividades

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId/children")

        fetchChildData()

        logoutButton.setOnClickListener {
            auth.signOut()
            // Iniciar la actividad de inicio de sesión
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Finalizar la actividad actual
            finish()
        }

    }

    private fun fetchChildData() {
        // Limpiar contenido previo antes de hacer la consulta
        clearChildInfo()

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val child = childSnapshot.getValue(Child::class.java)
                    if (child != null) {
                        displayChildData(child) // Mostrar datos del niño
                        loadChildActivities(childSnapshot.key) // Cargar actividades del niño
                    }
                }
            } else {
                Log.d("ChildInfoActivity", "No se pudo encontrar información del niño.")
                showNoChildInfo()
            }
        }.addOnFailureListener { error ->
            Log.e("ChildInfoActivity", "Error al acceder a la base de datos: ${error.message}")
            showErrorLoadingInfo()
        }
    }

    private fun loadChildActivities(childId: String?) {
        // Limpia las actividades anteriores antes de cargar nuevas
        attendanceLayout.removeAllViews()

        // Obtener las actividades de la base de datos
        val activitiesReference = FirebaseDatabase.getInstance().getReference("actividades")
        activitiesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (activitySnapshot in snapshot.children) {
                        val activity = activitySnapshot.getValue(Activity::class.java)
                        if (activity != null) {
                            // Crear un TextView para mostrar la información de la actividad
                            val activityInfo = StringBuilder()
                            activityInfo.append("Actividad: ${activity.nombre_actividad}\n")
                            activityInfo.append("Fecha: ${activity.fecha_actividad}\n")
                            activityInfo.append("Hora: ${activity.hora_actividad}\n")

                            // Comprobar la asistencia del niño
                            val childAttendance = activitySnapshot.child("asistencias").child(childId ?: "")
                            if (childAttendance.exists()) {
                                val attendanceStatus = childAttendance.getValue(String::class.java) ?: "Sin estado"
                                activityInfo.append("Asistencia: $attendanceStatus\n")
                            } else {
                                activityInfo.append("Asistencia: No registrada\n")
                            }

                            // Agregar el TextView al LinearLayout de asistencia
                            val activityTextView = TextView(this@ChildInfoActivity)
                            activityTextView.text = activityInfo.toString()
                            attendanceLayout.addView(activityTextView)
                        }
                    }
                } else {
                    Log.d("ChildInfoActivity", "No se encontraron actividades.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChildInfoActivity", "Error al cargar actividades: ${error.message}")
            }
        })
    }

    private fun clearChildInfo() {
        childNameTextView.text = "Nombre: "
        childAgeTextView.text = "Edad: "
        childGenderTextView.text = "Género: "
        childBirthDateTextView.text = "Fecha de nacimiento: "
        childPhoneTextView.text = "Teléfono: "
        childParentsTextView.text = "Padres: "
    }

    private fun displayChildData(child: Child) {
        childNameTextView.text = "Nombre: ${child.name}"
        childAgeTextView.text = "Edad: ${child.age}"
        childGenderTextView.text = "Género: ${child.gender}"
        childBirthDateTextView.text = "Fecha de nacimiento: ${child.birthDate}"
        childPhoneTextView.text = "Teléfono: ${child.phone}"
        childParentsTextView.text = "Padres: ${child.parents}"
    }

    private fun showNoChildInfo() {
        clearChildInfo()
        childNameTextView.text = "No hay información del niño registrada."
    }

    private fun showErrorLoadingInfo() {
        clearChildInfo()
        childNameTextView.text = "Error al cargar la información."
    }
}
