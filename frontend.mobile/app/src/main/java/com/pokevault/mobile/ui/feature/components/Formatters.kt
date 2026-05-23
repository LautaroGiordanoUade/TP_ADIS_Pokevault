package com.pokevault.mobile.ui.feature.components

fun Double.money(): String = "$" + "%,.2f".format(this)
