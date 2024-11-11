package com.example.poomagnet.mangaRepositoryManager

data class SimpleDate(
    val day: Int,
    val month: Int,
    val year: Int,
){
    override fun toString(): String {
        return "$day/$month/$year"
    }

    constructor(date: String): this(
        date.split("-")[2].substring(0..1).toInt(),
        date.split("-")[1].toInt(),
        date.split("-")[0].toInt()
    )
}