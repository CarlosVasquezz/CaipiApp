package com.example.caipiapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caipiapp.model.Child
import com.example.caipiapp.model.Activity // Asegúrate de que esta línea sea correcta según tu estructura de paquete
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PlayschoolActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var childAdapter: ChildAdapter
    private val childrenList = mutableListOf<Child>()
    private val activitiesList = mutableListOf<Activity>() // Lista para las actividades programadas
    private lateinit var activitiesAdapter: ActivitiesAdapter // Asegúrate de tener un adapter para las actividades

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playschool)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Referencias a los botones y vistas
        val btnCalendario: Button = findViewById(R.id.btn_calendario)
        val btnAsistencia: Button = findViewById(R.id.btn_asistencia)
        val btnActividades: Button = findViewById(R.id.btn_actividades)
        val btnPerfilNinos: Button = findViewById(R.id.btn_perfil_ninos)
        val btnLogout: Button = findViewById(R.id.btn_logout)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar los listeners para los botones

        btnAsistencia.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        btnActividades.setOnClickListener {
            startActivity(Intent(this, ActivitiesActivity::class.java))
        }

        btnPerfilNinos.setOnClickListener {
            startActivity(Intent(this, ChildProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            logout()
        }

        // Cargar datos de niños y actividades
        loadChildrenData()
        loadActivitiesData()
    }

    private fun loadChildrenData() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("children")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    childrenList.clear()
                    for (childSnapshot in snapshot.children) {
                        val child = childSnapshot.getValue(Child::class.java)
                        child?.let { childrenList.add(it) }
                    }
                    childAdapter = ChildAdapter(childrenList)
                    recyclerView.adapter = childAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PlayschoolActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadActivitiesData() {
        database.child("actividades").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activitiesList.clear()
                for (activitySnapshot in snapshot.children) {
                    val activity = activitySnapshot.getValue(Activity::class.java)
                    activity?.let { activitiesList.add(it) }
                }
                activitiesAdapter = ActivitiesAdapter(activitiesList) // Asegúrate de tener un adapter para las actividades
                recyclerView.adapter = activitiesAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PlayschoolActivity, "Error al cargar actividades: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        auth.signOut() // Cierra sesión
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        // Redirigir a la pantalla de inicio de sesión
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Finaliza la actividad actual
    }
}
