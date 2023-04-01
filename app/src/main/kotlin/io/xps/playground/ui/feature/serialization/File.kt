package io.xps.playground.ui.feature.serialization

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class File(
    val id: String,
    val name: String = "",
    val parent: String? = null,
    val size: Float? = null,
    val type: Type = Type.TEXT
) {
    @Serializable
    enum class Type {
        TEXT,
        PDF,

        @SerialName("executable")
        EXE
    }
}
