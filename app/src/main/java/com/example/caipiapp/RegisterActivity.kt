package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val roleRadioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                val selectedRoleId = roleRadioGroup.checkedRadioButtonId
                val role = when (selectedRoleId) {
                    R.id.radioAdmin -> "admin"
                    R.id.radioUser -> "user"
                    else -> null
                }

                if (role != null) {
                    registerUser(email, password, role)
                } else {
                    Toast.makeText(this, "Por favor selecciona un rol", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos y verifica que las contraseñas coincidan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    val userId = auth.currentUser?.uid

                    // Guardar el rol y el ID del usuario en la base de datos
                    if (userId != null) {
                        val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")
                        databaseReference.child("role").setValue(role).addOnCompleteListener { roleTask ->
                            if (roleTask.isSuccessful) {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                // Redireccionar dependiendo del rol
                                val intent = if (role == "user") {
                                    Intent(this, ChildDataActivity::class.java).apply {
                                        putExtra("USER_ID", userId) // Pasar el ID del usuario
                                    }
                                } else {
                                    Intent(this, PlayschoolActivity::class.java) // Reemplaza esto con la actividad del admin
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                Log.w("RegisterActivity", "Error al guardar el rol: ${roleTask.exception?.message}")
                                Toast.makeText(this, "Registro exitoso, pero no se pudo guardar el rol", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ChildDataActivity::class.java).apply {
                                    putExtra("USER_ID", userId) // Asegúrate de redirigir aquí para un user
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                } else {
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "El registro falló: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
