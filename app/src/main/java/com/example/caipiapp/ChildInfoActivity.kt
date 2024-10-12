package com.example.caipiapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChildInfoActivity : AppCompatActivity() {

    private lateinit var childNameTextView: TextView
    private lateinit var childAgeTextView: TextView
    private lateinit var childGenderTextView: TextView
    private lateinit var childBirthDateTextView: TextView
    private lateinit var childPhoneTextView: TextView
    private lateinit var childParentsTextView: TextView
    private lateinit var logoutButton: Button

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

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId/children")

        fetchChildData()

        logoutButton.setOnClickListener {
            auth.signOut()
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
