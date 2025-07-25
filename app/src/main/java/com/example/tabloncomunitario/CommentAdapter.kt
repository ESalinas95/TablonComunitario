package com.example.tabloncomunitario

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private var comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    companion object {
        private const val TAG = "CommentAdapter"
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorImageView: CircleImageView = itemView.findViewById(R.id.imageViewCommentAuthorProfile)
        val authorTextView: TextView = itemView.findViewById(R.id.textViewCommentAuthor)
        val commentTextView: TextView = itemView.findViewById(R.id.textViewCommentText)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewCommentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        Log.d(TAG, "onBindViewHolder: Binding comentario en posición $position. Texto: '${comment.text}', Autor: '${comment.authorDisplayName}'")

        holder.authorTextView.text = comment.authorDisplayName
        holder.commentTextView.text = comment.text

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = comment.timestamp?.let { dateFormat.format(Date(it)) } ?: "Fecha desconocida"
        holder.dateTextView.text = dateString

        if (!comment.authorProfileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(comment.authorProfileImageUrl)
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(holder.authorImageView)
        } else {
            holder.authorImageView.setImageResource(R.drawable.ic_default_profile)
        }

        val clickListener = View.OnClickListener {
            Log.d(TAG, "Clic en autor de comentario: ${comment.authorDisplayName}")
            if (comment.authorId.isNullOrEmpty()) {
                Toast.makeText(holder.itemView.context, "ID de autor no disponible.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "ID de autor es nulo o vacío para el comentario.")
                return@OnClickListener
            }
            val intent = Intent(holder.itemView.context, UserProfilePreviewActivity::class.java)
            intent.putExtra(UserProfilePreviewActivity.EXTRA_USER_ID, comment.authorId)
            holder.itemView.context.startActivity(intent)
        }

        holder.authorTextView.setOnClickListener(clickListener)
        holder.authorImageView.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int {
        val count = comments.size
        Log.d(TAG, "getItemCount: El adaptador reporta $count ítems.")
        return count
    }

    /**
     * Actualiza la lista de comentarios del adaptador y notifica al RecyclerView.
     */
    fun updateComments(newComments: List<Comment>) {
        Log.d(TAG, "updateComments: Recibidos ${newComments.size} comentarios para actualizar.")
        // Reemplazamos la lista completa interna del adaptador
        // Aseguramos que la lista sea mutable y esté ordenada
        this.comments = newComments.sortedBy { it.timestamp ?: 0L }.toMutableList()
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado por completo
        Log.d(TAG, "updateComments: Adaptador actualizado. RecyclerView notificado.")
    }
}