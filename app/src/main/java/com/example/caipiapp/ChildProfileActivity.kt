package com.example.caipiapp

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caipiapp.model.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChildProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var childAdapter: ChildAdapter
    private val childrenList = mutableListOf<Child>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_profile)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnRegresar: Button = findViewById(R.id.btn_regresar)
        btnRegresar.setOnClickListener {
            finish() // Regresar a la actividad anterior
        }

        // Cargar datos de todos los niños
        loadAllChildrenData()
    }

    private fun loadAllChildrenData() {
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                childrenList.clear()
                for (userSnapshot in snapshot.children) {
                    for (childSnapshot in userSnapshot.child("children").children) {
                        val child = childSnapshot.getValue(Child::class.java)
                        child?.let { childrenList.add(it) }
                    }
                }
                childAdapter = ChildAdapter(childrenList)
                recyclerView.adapter = childAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChildProfileActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
