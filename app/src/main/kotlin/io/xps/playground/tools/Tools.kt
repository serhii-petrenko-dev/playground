package io.xps.playground.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

val Any.TAG: String
    get() = this.javaClass.simpleName

inline fun <reified T> T.toJson(json: Json): JsonElement {
    return json.encodeToJsonElement(this)
}

inline fun <reified T> fromJson(json: Json, element: JsonElement): T {
    return json.decodeFromJsonElement(element)
}
