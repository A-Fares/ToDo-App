package com.afares.todo.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.afares.todo.data.ToDoDatabase
import com.afares.todo.data.model.ToDoData
import com.afares.todo.data.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoDao = ToDoDatabase.getDatabase(
        application
    ).toDoDao()

    private val repository: ToDoRepository

    val getAllData: LiveData<List<ToDoData>>

    init {
        repository = ToDoRepository(toDoDao)
        getAllData = repository.getAllData
    }

    fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(IO) {
            repository.insertData(toDoData)
        }
    }

    fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(IO) {
            repository.updateData(toDoData)
        }
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(IO) {
            repository.deleteItem(toDoData)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(IO) {
            repository.deleteAll()
        }
    }
}