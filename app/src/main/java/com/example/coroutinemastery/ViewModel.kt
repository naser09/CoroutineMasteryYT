package com.example.coroutinemastery

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile

class MyViewModel():ViewModel() {
    val currentDir = mutableStateOf(Environment.getExternalStorageDirectory())
    val files:MutableState<List<File>>  = mutableStateOf(currentDir.value.listFiles()?.toList()?: listOf())
    val selectedFile = mutableStateOf<File?>(null)
    val fileToCopy = mutableStateOf<File?>(null)
    val copyStatus = mutableStateOf<Resource>(Resource.NotStarted)
    val newFileName = mutableStateOf("newFile")
    fun createNewFileWithSize(sizeMB:Int){
        try {
            val file = File(currentDir.value,newFileName.value)
            val randomAccessFile = RandomAccessFile(file,"rw")
            randomAccessFile.setLength(sizeMB*1024L)
            randomAccessFile.close()
        }catch (ex:Exception){
            Log.e("New File Create",ex.message?:"create File Exception")
        }
        files.value = currentDir.value.listFiles()?.toList()?: listOf()
    }
    fun setCopyFile(){
        fileToCopy.value = selectedFile.value
    }
    fun pasteFile(){
        viewModelScope.launch {
            if (fileToCopy.value != null){
                copyFile(fileToCopy.value!!,currentDir.value).flowOn(Dispatchers.IO).collect{
                    copyStatus.value = it
                }
            }
            files.value = currentDir.value.listFiles()?.toList()?: listOf()
        }
    }
    fun onBack(){
        val path = currentDir.value.absolutePath.dropLastWhile { it!='/' }
        if (path.length<Environment.getExternalStorageDirectory().absolutePath.length
            || currentDir==Environment.getExternalStorageDirectory()) return
        currentDir.value = File(path)
        files.value = currentDir.value.listFiles()?.toList()?: listOf()
    }
    fun deleteFile(){
        selectedFile.value?.delete()
        files.value = emptyList()
        files.value = currentDir.value.listFiles()?.toList()?: listOf()
    }

    fun onClick(file: File){
        if (file.isDirectory){
            currentDir.value = file
            files.value = currentDir.value.listFiles()?.toList()?: listOf()
        }else{
            selectedFile.value = file
        }
    }
    private fun copyFile(fileToCopy:File, pasteDirectory:File):Flow<Resource>{
        return flow {
            CopyOperation.copyFile(fileToCopy,pasteDirectory){
                emit(Resource.Running(it))
            }
            emit(Resource.Success("${fileToCopy.name} Copied Successful"))
        }
    }
}