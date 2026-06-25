package com.pokevault.mobile.util

/**
 * Formatea un Double como precio en pesos argentinos.
 * Ejemplo: 1234.5 → "$1.234,50"
 * Se usa \u00A0 (non-breaking space) para evitar que el símbolo y los dígitos
 * se separen en líneas distintas al hacer wrap.
 */
fun Double.money(): String = ("$" + "%,.2f".format(this)).replace(" ", "\u00A0")
