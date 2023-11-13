package com.example.coroutinemastery

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomBar(
    fileOpen:()->Unit,
    fileCopy:()->Unit,
    fileDelete:()->Unit,
    filePaste:()->Unit,
) {
    BottomAppBar(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { fileOpen() }) {
            Icon(imageVector = Icons.Default.FileOpen, contentDescription = "Create file")
        }
        Button(onClick = { fileCopy() }) {
            Icon(imageVector = Icons.Default.CopyAll, contentDescription = "Copy file")
        }
        Button(onClick = { fileDelete()}) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete file")
        }
        Button(onClick = { filePaste()}) {
            Icon(imageVector = Icons.Default.ContentPasteGo, contentDescription = "paste file")
        }
    }
}