package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caipiapp.model.Child
import com.example.caipiapp.ChildInfoActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ChildDataActivity : AppCompatActivity() {

    private lateinit var childNameEditText: EditText
    private lateinit var childGenderSpinner: Spinner // Cambiar EditText a Spinner
    private lateinit var childBirthDateEditText: EditText
    private lateinit var childPhoneEditText: EditText
    private lateinit var childParentsEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var childInfoTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_data)

        // Inicialización de vistas
        childNameEditText = findViewById(R.id.childNameEditText)
        childGenderSpinner = findViewById(R.id.childGenderSpinner) // Inicializar Spinner
        childBirthDateEditText = findViewById(R.id.childBirthDateEditText)
        childPhoneEditText = findViewById(R.id.childPhoneEditText)
        childParentsEditText = findViewById(R.id.childParentsEditText)
        saveButton = findViewById(R.id.saveButton)
        childInfoTextView = findViewById(R.id.childInfoTextView)

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference

        // Configuración del Spinner
        setupGenderSpinner()

        saveButton.setOnClickListener {
            saveChildData()
        }
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Masculino", "Femenino") // Opciones de género
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        childGenderSpinner.adapter = adapter
    }

    private fun saveChildData() {
        val name = childNameEditText.text.toString()
        val gender = childGenderSpinner.selectedItem.toString() // Obtener género del Spinner
        val birthDate = childBirthDateEditText.text.toString()
        val phone = childPhoneEditText.text.toString()
        val parents = childParentsEditText.text.toString()

        if (name.isNotEmpty() && gender.isNotEmpty() && birthDate.isNotEmpty() && phone.isNotEmpty() && parents.isNotEmpty()) {
            val age = calculateAge(birthDate)

            val child = Child(name, age, gender, birthDate, phone, parents)
            val childId = database.child("users").child(userId).child("children").push().key

            if (childId != null) {
                database.child("users").child(userId).child("children").child(childId).setValue(child)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Datos del niño guardados exitosamente", Toast.LENGTH_SHORT).show()
                            // Redirigir a ChildInfoActivity después de guardar
                            val intent = Intent(this, ChildInfoActivity::class.java)
                            startActivity(intent)
                            finish() // Opcional: cierra la actividad actual para evitar regresar
                        } else {
                            Toast.makeText(this, "Error al guardar los datos: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAge(birthDate: String): Int {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = format.parse(birthDate)
            if (date != null) {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                calendar.time = date
                currentYear - calendar.get(Calendar.YEAR)
            } else 0
        } catch (e: Exception) {
            Log.e("ChildDataActivity", "Error al calcular la edad: ${e.message}")
            0
        }
    }
}
