package com.example.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PersonEntity

@Composable
fun AddEditPersonDialog(
    personToEdit: PersonEntity? = null,
    onSave: (PersonEntity) -> Unit,
    onDelete: ((PersonEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(personToEdit?.name ?: "") }
    var relationship by remember { mutableStateOf(personToEdit?.relationship ?: "Titular") }
    var selectedEmoji by remember { mutableStateOf(personToEdit?.avatarEmoji ?: "👤") }
    var selectedColor by remember { mutableStateOf(personToEdit?.avatarColorHex ?: 0xFF0284C7) }

    val presetRelationships = listOf("Papá", "Mamá", "Esposa", "Esposo", "Hijo", "Hija", "Hermano/a", "Amigo/a", "Yo")
    val emojis = listOf("👨", "👩", "👦", "👧", "🧑", "👴", "👵", "🚗", "🏎️", "👤")
    val colors = listOf(0xFF0284C7, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFFEF4444, 0xFF0EA5E9)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (personToEdit == null) "Añadir Conductor / Familiar" else "Editar Conductor",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 28.sp)
                    }
                }

                // Emoji picker
                Text("Icono / Avatar:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (emoji == selectedEmoji) Color(selectedColor).copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                // Color picker
                Text("Color:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Papá, Carlos)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Relationship chips
                Text("Rol / Relación:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetRelationships) { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = {
                                relationship = rel
                                if (name.isBlank()) name = rel
                            },
                            label = { Text(rel, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val person = personToEdit?.copy(
                            name = name.trim(),
                            relationship = relationship.trim(),
                            avatarEmoji = selectedEmoji,
                            avatarColorHex = selectedColor
                        ) ?: PersonEntity(
                            name = name.trim(),
                            relationship = relationship.trim(),
                            avatarEmoji = selectedEmoji,
                            avatarColorHex = selectedColor
                        )
                        onSave(person)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (personToEdit != null && onDelete != null) {
                    IconButton(onClick = {
                        onDelete(personToEdit)
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
