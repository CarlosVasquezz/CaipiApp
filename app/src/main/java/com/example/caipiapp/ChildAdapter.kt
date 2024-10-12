package com.example.caipiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caipiapp.model.Child

class ChildAdapter(private val children: List<Child>) : RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val childLogo: ImageView = view.findViewById(R.id.child_logo)
        val childName: TextView = view.findViewById(R.id.child_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val child = children[position]
        holder.childName.text = child.name // Asegúrate de tener un campo `name` en tu clase Child
        // Asigna un logo o imagen representativa
        holder.childLogo.setImageResource(R.drawable.ic_person)
    }

    override fun getItemCount() = children.size
}
