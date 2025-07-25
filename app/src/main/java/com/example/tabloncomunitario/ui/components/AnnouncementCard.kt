package com.example.tabloncomunitario.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// (AnnouncementCard Composable - sin cambios)
@Composable
fun AnnouncementCard(announcement: Announcement, onAnnouncementClick: (Announcement) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onAnnouncementClick(announcement) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = if (announcement.authorProfileImageUrl != null) {
                        rememberAsyncImagePainter(announcement.authorProfileImageUrl)
                    } else {
                        painterResource(id = R.drawable.ic_default_profile)
                    },
                    contentDescription = "Foto de perfil del autor",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(announcement.authorDisplayName.ifEmpty { announcement.authorEmail }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(announcement.timestamp ?: 0L)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Text(announcement.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(announcement.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}