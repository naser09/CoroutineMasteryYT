package com.example.coroutinemastery

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.coroutinemastery.ui.theme.CoroutineMasteryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Thread
import kotlin.concurrent.thread

@Composable
fun rememberFileState(filePath:String,onEvent:(String?)->Unit = {}):FileObserver= remember {
    val file = mutableStateOf(File(filePath))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        object : FileObserver(file.value) {
            override fun onEvent(event: Int, path: String?) {
                onEvent(path)
                if (event == DELETE){
                    Log.d("Naser","Delete Event")
                }else if (event == CREATE){
                    Log.d("Naser","Create Event")
                }
                if (path!=null){
                    file.value = File(path)
                }
            }
        }
    } else {
        object :FileObserver(filePath){
            override fun onEvent(event: Int, path: String?) {
                onEvent(path)
                if (path!=null){
                    file.value = File(path)
                }
            }
        }
    }
}
private lateinit var fileCopyService: FileCopyService
private var serviceBound = false

private val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as FileCopyService.LocalBinder
        fileCopyService = binder.getService()
        serviceBound = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        serviceBound = false
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val files = mutableStateOf(Environment.getExternalStorageDirectory())
        var previusFile = mutableStateOf<File?>(files.value)
        val selectedFile = mutableStateOf<File?>(null)
        val copyFile = mutableStateOf<File?>(null)

        val intent = Intent(this, FileCopyService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            CoroutineMasteryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startingPath = remember {
                        mutableStateOf(Environment.getExternalStorageDirectory().path)
                    }
                    val currentFile = rememberFileState(filePath = startingPath.value)
                    Log.d("Naser","Current file $currentFile")
                    val state = remember {
                        mutableStateOf(Status(0f,0f))
                    }
                    Column {
                        RequestPermission()
                        Button(onClick = { files.value = previusFile.value}) {
                            Text(text = "Back")
                        }
                        Button(onClick = {
                            copyFile.value = selectedFile.value
                        }) {
                            Text(text = "Copy File")
                        }
                        Button(onClick = {
                            Log.d("Naser","copy file = ${copyFile.value}")
//                            copyFile.value?.let { file ->
//                                fileOperation(file,files.value)
//                                    .onEach {
//                                        state.value = it
//                                    }.flowOn(Dispatchers.IO)
//                                    .launchIn(lifecycleScope)
//                            }
                            if (serviceBound) {
                                Log.d("Naser","copy file = ${copyFile.value} service $serviceBound")
                                copyFile.value?.let {
                                    fileCopyService.startFileCopy(it.absoluteFile, files.value)
                                }
                            }
                        }) {
                            Text(text = "Paste File")
                        }
                        Button(onClick = {
                            if (files.value.canWrite()){
                                Log.d("Naser",File(files.value.absolutePath+"/newFile").createNewFile().toString())
                            }
                        }) {
                            Text(text = "Create File")
                        }
                        Button(onClick = {
                            selectedFile.value?.let { it.delete() }
                        }) {
                            Text(text = "Delete File")
                        }
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(15.dp)
                                .clip(RoundedCornerShape(50)),
                            progress = state.value.percent,
                            color = Color.Cyan,
                            trackColor = Color.DarkGray,
                        )
                        Text(text = "Transfer ; ${state.value.speed} bytes")
                        ShowExtDir(files,selectedFile,previusFile,{
                            selectedFile.value = it
                        }){
                            files.value = it
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission() {
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
        rememberMultiplePermissionsState(permissions = listOf(
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ))
    } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
        rememberMultiplePermissionsState(permissions = listOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ))
    }else {
        rememberMultiplePermissionsState(permissions = listOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ))
    }
    if (permissionState.allPermissionsGranted){
        Column {
            Text(text = "All permission granted")

            Button(onClick = {
                Log.d("Naser",permissionState.permissions.map { it.permission.toString() }.toString())
            }) {
                Text("Request permission Manage")
            }
        }

    }else{
        Column {
            val textToShow = if (permissionState.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Composable
fun ShowExtDir(file: State<File>,
               selectedFile:State<File?>,
               previusFile:State<File?>,onClick:(File)->Unit,
               currentDir:(File)->Unit = { file.value }) {

    val listFiles = remember {
        mutableStateOf(file.value.listFiles()?: arrayOf())
    }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val changeFile = callbackFlow(file.value.path) {
        if (it == FileObserver.DELETE) {
            listFiles.value = file.value.listFiles() ?: arrayOf()
        } else if (it == FileObserver.CREATE) {
            listFiles.value = file.value.listFiles() ?: arrayOf()
        }
    }.collectAsState(initial = file)
    LaunchedEffect(key1 = changeFile,key2 = file.value){
        listFiles.value = file.value.listFiles()?: arrayOf()
    }
    LaunchedEffect(key1 = Unit){
        val mutableList = mutableListOf<String>()
        Log.d("Naser",file.value.absolutePath)
        Log.d("Naser",file.value.isDirectory.toString())
        Log.d("Naser",file.value.canRead().toString())
        Log.d("Naser",file.value.canWrite().toString())
    }
    LazyColumn(modifier = Modifier){
        item {
            Button(onClick = { previusFile.value?.let {
                listFiles.value = it.listFiles()?: arrayOf()
            } }) {
                Text(text = "Back")
            }
        }
        items(listFiles.value){f ->
            val mod =if (selectedFile.value ==f) Modifier.border(width = 2.dp, Color.Red)else Modifier
            Button(modifier = mod, onClick = { onClick(f) }) {
                Row {
                    val icon = if (f.isDirectory) Icons.Default.Folder else Icons.Default.FilePresent
                    Image(imageVector = icon, contentDescription = null, colorFilter = ColorFilter.tint(
                        Color.Yellow))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = f.name, modifier = Modifier.clickable {
                        if (f.isDirectory){
                            listFiles.value = f.listFiles()?: arrayOf()
                            currentDir(f)
                        }else{
                            onClick(f)
                        }

                    })
                }
            }
        }
    }
}
suspend fun fileOperation(contentResolver: ContentResolver,uri:Uri,file: File){
    coroutineScope{
        withContext(Dispatchers.IO){
            val output = file.outputStream()
            contentResolver.openInputStream(uri)?.use {
                var copied = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var byte = it.read(buffer)
                while (byte>=0){
                    output.write(buffer,0,byte)
                    byte = it.read(buffer)
                    copied+=byte
                    Log.d(TAG,"Copied $copied")
                }
                it.close()
            }
        }
    }
}
data class Status(
    val percent:Float,
    val speed:Float
)
fun fileOperation(fileToCopy: File,directory:File):Flow<Status> = flow {
    Log.d("Naser","inside operation")
    emit(Status(0f,0f))
            val input = fileToCopy.inputStream()
            if (directory.isDirectory){
                val file = File(directory.absolutePath,fileToCopy.name)
                if (file.exists()){
                    file.delete()
                }
                file.createNewFile()
                val outputStream = file.outputStream()
                input.use {
                    var copied = 0
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var byte = it.read(buffer)
                    while (byte>=0){
                        outputStream.write(buffer,0,byte)
                        byte = it.read(buffer)
                        copied+=byte
                        Log.d(TAG,"Copied $copied")
                        emit(
                            Status(
                            percent(copied.toFloat(),fileToCopy.length().toFloat()),
                            byte.toFloat().coerceAtLeast(0f)
                        )
                        )
                    }
                    it.close()
                }
            }
}
fun percent(numerator: Float, denominator: Float): Float {
    return numerator / denominator
}
suspend fun startCoroutine(){
    val scope = CoroutineScope(Dispatchers.IO)
    val data = scope.async {
        delay(5000)
        "100"
    }
    scope.launch {
        withContext(Dispatchers.IO) {
            repeat(100){
                delay(1000)
                Log.d(TAG,Thread.currentThread().name +" in Scope")
            }
        }
    }
    withContext(Dispatchers.Main){
        delay(5000)
        scope.cancel()
        Log.d(TAG,Thread.currentThread().name)
    }
    repeat(100){
        delay(1000)
        Log.d(TAG,Thread.currentThread().name)
    }
}
fun startThread():Int{
    var data = 0
   val thread =  thread(start = false) {
        repeat(15){
            Thread.sleep(1000)
            data += it
            Log.d(TAG,Thread.currentThread().name)
        }
    }
    thread.start()
    thread.join()
    return data
}
private val TAG = "Thread"

