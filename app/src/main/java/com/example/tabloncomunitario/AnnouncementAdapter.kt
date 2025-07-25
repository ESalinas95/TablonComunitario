package com.example.tabloncomunitario // Ajusta tu paquete

import com.example.tabloncomunitario.Announcement
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Necesitarás añadir Glide para cargar imágenes
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementAdapter(private val announcements: MutableList<Announcement>) :
    RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    // ViewHolder para cada ítem del RecyclerView
    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val authorDateTextView: TextView = itemView.findViewById(R.id.textViewAuthorDate)
        val announcementImageView: ImageView = itemView.findViewById(R.id.imageViewAnnouncement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.titleTextView.text = announcement.title
        holder.descriptionTextView.text = announcement.description

        // Formatear la fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = announcement.timestamp?.let { dateFormat.format(it) } ?: "Fecha desconocida"
        holder.authorDateTextView.text = "Publicado por: ${announcement.authorEmail} el $dateString"

        // Cargar imagen con Glide si existe
        if (!announcement.imageUrl.isNullOrEmpty()) {
            holder.announcementImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(announcement.imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.sym_def_app_icon) // Placeholder temporal
                .error(android.R.drawable.ic_menu_close_clear_cancel) // Error temporal
                .into(holder.announcementImageView)
        } else {
            holder.announcementImageView.visibility = View.GONE
        }

        // Aquí puedes añadir un setOnClickListener para la tarjeta si quieres ver los detalles del anuncio
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailAnnouncementActivity::class.java)
            intent.putExtra("announcement", announcement) // Pasa el objeto Parcelable
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = announcements.size

    // Función para actualizar los datos del adaptador
    fun updateAnnouncements(newAnnouncements: List<Announcement>) {
        announcements.clear()
        announcements.addAll(newAnnouncements)
        notifyDataSetChanged() // Notificar al RecyclerView que los datos han cambiado
    }
}