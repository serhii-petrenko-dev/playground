package io.xps.playground.tools

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

fun Long.byteSizeToString(showDecimal: Boolean = true): String {
    if (this <= 0 ) return "0"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    val pattern = if(showDecimal) "#,##0.#" else "#,##0"
    return DecimalFormat(pattern).format(this / 1024.0.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
}