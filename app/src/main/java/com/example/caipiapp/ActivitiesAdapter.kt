package com.example.caipiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caipiapp.model.Activity

class ActivitiesAdapter(private val activities: List<Activity>) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewActivityName)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewActivityDate)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewActivityTime)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewActivityDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val currentActivity = activities[position]
        holder.textViewName.text = currentActivity.nombre_actividad
        holder.textViewDate.text = currentActivity.fecha_actividad
        holder.textViewTime.text = currentActivity.hora_actividad
        holder.textViewDescription.text = currentActivity.descripcion_actividad
    }

    override fun getItemCount() = activities.size
}
