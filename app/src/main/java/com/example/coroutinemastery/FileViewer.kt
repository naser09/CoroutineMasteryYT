package com.example.coroutinemastery

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun FileViewer(modifier: Modifier,viewModel: MyViewModel) {
    LazyColumn{
        item {
            RequestPermission()
        }
        when(val status = viewModel.copyStatus.value){
            Resource.NotStarted -> {}
            is Resource.Running -> {
                item {
                    Column {
                        LinearProgressIndicator(modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                            progress = status.status?.progress?:0f, color = Color.Cyan)
                        status.status?.speed?.let {
                            if (it>1024){
                                Text(text = "Transfer : ${it/1024} MB")
                            }else{
                                Text(text = "Transfer : $it KB")
                            }
                        }
                    }
                }
            }
            is Resource.Success -> {}
        }
        items(viewModel.files.value){
            val mod = if (viewModel.selectedFile.value==it) Modifier.border(2.dp, Color.Cyan) else Modifier
            ItemFile(modifier = mod, file = it){
                viewModel.onClick(it)
            }
        }
    }
}
@Composable
private fun ItemFile(modifier: Modifier,file:File,onClick:()->Unit){
    val style = if (file.isDirectory) Icons.Default.Folder else Icons.Default.FileCopy
    Button(modifier = modifier,onClick = { onClick() }) {
        Row {
            Icon(imageVector = style,
                contentDescription = "icon",
                tint = Color.Yellow)
            Text(text = file.name)
        }
    }
}