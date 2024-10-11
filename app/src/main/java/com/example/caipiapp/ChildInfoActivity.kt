package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChildInfoActivity : AppCompatActivity() {

    private lateinit var childNameTextView: TextView
    private lateinit var childAgeTextView: TextView
    private lateinit var childGenderTextView: TextView
    private lateinit var childBirthDateTextView: TextView
    private lateinit var childPhoneTextView: TextView
    private lateinit var childParentsTextView: TextView
    private lateinit var logoutButton: Button

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_info)

        childNameTextView = findViewById(R.id.childNameTextView)
        childAgeTextView = findViewById(R.id.childAgeTextView)
        childGenderTextView = findViewById(R.id.childGenderTextView)
        childBirthDateTextView = findViewById(R.id.childBirthDateTextView)
        childPhoneTextView = findViewById(R.id.childPhoneTextView)
        childParentsTextView = findViewById(R.id.childParentsTextView)
        logoutButton = findViewById(R.id.logoutButton)

        // Obtener el ID del usuario
        val userId = intent.getStringExtra("USER_ID") ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId/children")

        fetchChildData(userId)

        logoutButton.setOnClickListener {
            // Implementar el cierre de sesión
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchChildData(userId: String) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val child = childSnapshot.getValue(Child::class.java)
                        if (child != null) {
                            displayChildData(child)
                        }
                    }
                } else {
                    Log.d("ChildInfoActivity", "No se pudo encontrar información del niño.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChildInfoActivity", "Error al acceder a la base de datos: ${error.message}")
            }
        })
    }

    private fun displayChildData(child: Child) {
        childNameTextView.text = "Nombre: ${child.name}"
        childAgeTextView.text = "Edad: ${child.age}"
        childGenderTextView.text = "Género: ${child.gender}"
        childBirthDateTextView.text = "Fecha de nacimiento: ${child.birthDate}"
        childPhoneTextView.text = "Teléfono: ${child.phone}"
        childParentsTextView.text = "Padres: ${child.parents}"
    }
}
