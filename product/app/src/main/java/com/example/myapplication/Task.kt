package com.example.myapplication

// Task object which has id and task info
data class Task(
    val id: Int,
    val title: String,
    val tag: String,
    val hours: Int,
    val username: String,
)
