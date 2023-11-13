package com.example.coroutinemastery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.example.coroutinemastery.ui.theme.CoroutineMasteryTheme

class MainActivity : ComponentActivity() {
    private val viewModel:MyViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoroutineMasteryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val newFileDialog = remember {
                        mutableStateOf(false)
                    }
                    val newFileSize= remember {
                        mutableStateOf(0)
                    }
                    if (newFileDialog.value){
                        Dialog(onDismissRequest = { newFileDialog.value = false}) {
                            Column {
                                Text(text = "New File Name")
                                TextField(value = viewModel.newFileName.value, onValueChange = {
                                    viewModel.newFileName.value = it
                                })
                                Text(text = "New File Size in MB")
                                TextField(value = newFileSize.value.toString(), onValueChange = {
                                   newFileSize.value = it.toIntOrNull()?:0
                                })
                                Button(onClick = { viewModel.createNewFileWithSize(newFileSize.value)}) {
                                    Text(text = "Create New File")
                                }
                            }
                        }
                    }
                    Scaffold(modifier = Modifier.fillMaxSize(),
                        topBar = {
                                 TopAppBar(title = {
                                     Text(text = viewModel.currentDir.value.path.removeRange(0,17))
                                 }, navigationIcon = {
                                     Button(onClick = { viewModel.onBack()}) {
                                         Icon(
                                             imageVector = Icons.Default.ArrowBackIosNew,
                                             contentDescription = "back"
                                         )
                                     } })
                        },
                        bottomBar = {
                            BottomBar(
                                fileOpen = {
                                         newFileDialog.value = true
                                },
                                fileCopy = { viewModel.setCopyFile() },
                                fileDelete = { viewModel.deleteFile() }) { //paste
                                viewModel.pasteFile()
                            }
                    }) {
                        Box(modifier = Modifier.padding(it)){
                            FileViewer(modifier = Modifier, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}



