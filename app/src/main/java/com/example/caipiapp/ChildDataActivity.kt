package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChildDataActivity : AppCompatActivity() {

    private lateinit var childNameEditText: EditText
    private lateinit var childAgeEditText: EditText
    private lateinit var childGenderEditText: EditText
    private lateinit var childBirthDateEditText: EditText
    private lateinit var childPhoneEditText: EditText
    private lateinit var childParentsEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_data)

        childNameEditText = findViewById(R.id.childNameEditText)
        childAgeEditText = findViewById(R.id.childAgeEditText)
        childGenderEditText = findViewById(R.id.childGenderEditText)
        childBirthDateEditText = findViewById(R.id.childBirthDateEditText)
        childPhoneEditText = findViewById(R.id.childPhoneEditText)
        childParentsEditText = findViewById(R.id.childParentsEditText)
        saveButton = findViewById(R.id.saveButton)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        database = FirebaseDatabase.getInstance().reference

        saveButton.setOnClickListener {
            saveChildData()
        }
    }

    private fun saveChildData() {
        val name = childNameEditText.text.toString()
        val age = childAgeEditText.text.toString().toIntOrNull()
        val gender = childGenderEditText.text.toString()
        val birthDate = childBirthDateEditText.text.toString()
        val phone = childPhoneEditText.text.toString()
        val parents = childParentsEditText.text.toString()

        if (name.isNotEmpty() && age != null && gender.isNotEmpty() && birthDate.isNotEmpty() && phone.isNotEmpty() && parents.isNotEmpty()) {
            val child = Child(name, age, gender, birthDate, phone, parents)
            val childId = database.child("users").child(userId).child("children").push().key // Genera un ID único

            if (childId != null) {
                database.child("users").child(userId).child("children").child(childId).setValue(child)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Datos del niño guardados exitosamente", Toast.LENGTH_SHORT).show()
                            // Redirigir a ChildInfoActivity después de guardar los datos
                            val intent = Intent(this, ChildInfoActivity::class.java)
                            startActivity(intent)
                            finish() // Cerrar esta actividad
                        } else {
                            Toast.makeText(this, "Error al guardar los datos: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

}
