package com.pos10.data.repository

data class ListMain(
    val item: String,
    val subList: List<String>,
)

val listStatic = listOf(
    ListMain("A", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("B", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("C", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("D", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("E", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("F", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("G", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("H", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
    ListMain("I", listOf("ac", "ffvdfjsdjsdjskdfsdfsaldv")),
)