package io.xps.playground.ui.feature.composePerformance

import kotlinx.collections.immutable.ImmutableList

data class Contact(
    val id: Long,
    val isLoading: Boolean,
    // val names: List<String>, // Unstable
    val names: ImmutableList<String> // Stable
)
