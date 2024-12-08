package com.msd.notebook.models

data class Lecture(
    var header: String = "",
    var description: String = "",
    var notes: String = "",
    var date: Long = 0
)
