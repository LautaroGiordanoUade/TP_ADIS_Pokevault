package com.pokevault.mobile.ui.feature.search.state

import com.pokevault.mobile.domain.model.PokemonCard

object SearchFilterOptions {
    const val ALL_TYPES = "Todos los tipos"
    const val ALL_RARITIES = "Todas las rarezas"
    const val ANY_PRICE = "Cualquier precio"
    const val NO_SORT = "Sin ordenar"

    val priceOptions = listOf(
        ANY_PRICE,
        "Hasta $5",
        "Hasta $20",
        "Hasta $50",
        "Hasta $100",
    )

    val sortOptions = listOf(
        NO_SORT,
        "Mayor precio primero",
        "Menor precio primero",
        "Nombre A-Z",
        "Nombre Z-A",
    )

    fun availableTypeOptions(cards: List<PokemonCard>, selectedType: String? = null): List<String> {
        val dynamicOptions = cards
            .flatMap { it.types }
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)
            .sortedBy(String::lowercase)

        return buildList {
            add(ALL_TYPES)
            selectedType
                ?.takeIf { it.isNotBlank() && it != ALL_TYPES && dynamicOptions.none { option -> option.equals(it, ignoreCase = true) } }
                ?.let(::add)
            addAll(dynamicOptions)
        }
    }

    fun availableRarityOptions(cards: List<PokemonCard>, selectedRarity: String? = null): List<String> {
        val dynamicOptions = cards
            .mapNotNull { it.rarity }
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)
            .sortedBy(String::lowercase)

        return buildList {
            add(ALL_RARITIES)
            selectedRarity
                ?.takeIf { it.isNotBlank() && it != ALL_RARITIES && dynamicOptions.none { option -> option.equals(it, ignoreCase = true) } }
                ?.let(::add)
            addAll(dynamicOptions)
        }
    }
}

data class SearchCriteria(
    val query: String = "",
    val selectedType: String = SearchFilterOptions.ALL_TYPES,
    val selectedRarity: String = SearchFilterOptions.ALL_RARITIES,
    val selectedPrice: String = SearchFilterOptions.ANY_PRICE,
    val selectedSort: String = SearchFilterOptions.NO_SORT,
) {
    fun hasActiveLocalFilters(): Boolean =
        selectedType != SearchFilterOptions.ALL_TYPES ||
                selectedRarity != SearchFilterOptions.ALL_RARITIES ||
                selectedPrice != SearchFilterOptions.ANY_PRICE ||
                selectedSort != SearchFilterOptions.NO_SORT
}

object SearchFilterEngine {
    fun apply(cards: List<PokemonCard>, criteria: SearchCriteria): List<PokemonCard> {
        val normalizedQuery = criteria.query.trim()

        return cards
            .asSequence()
            .filter { card -> matchesQuery(card, normalizedQuery) }
            .filter { card -> matchesType(card, criteria.selectedType) }
            .filter { card -> matchesRarity(card, criteria.selectedRarity) }
            .filter { card -> matchesPrice(card, criteria.selectedPrice) }
            .let { cardsSequence -> sort(cardsSequence.toList(), criteria.selectedSort) }
    }

    private fun matchesQuery(card: PokemonCard, query: String): Boolean {
        if (query.isBlank()) return true
        val normalizedQuery = query.lowercase()
        return listOfNotNull(card.name, card.setName, card.artist, card.number, card.description)
            .any { candidate -> candidate.lowercase().contains(normalizedQuery) }
    }

    private fun matchesType(card: PokemonCard, selectedType: String): Boolean {
        if (selectedType == SearchFilterOptions.ALL_TYPES) return true
        return card.types.any { it.equals(selectedType, ignoreCase = true) }
    }

    private fun matchesRarity(card: PokemonCard, selectedRarity: String): Boolean {
        if (selectedRarity == SearchFilterOptions.ALL_RARITIES) return true
        val rarity = card.rarity ?: return false
        return rarity.contains(selectedRarity, ignoreCase = true)
    }

    private fun matchesPrice(card: PokemonCard, selectedPrice: String): Boolean {
        val maxPrice = when (selectedPrice) {
            "Hasta $5" -> 5.0
            "Hasta $20" -> 20.0
            "Hasta $50" -> 50.0
            "Hasta $100" -> 100.0
            else -> null
        }
        return maxPrice == null || card.price <= maxPrice
    }

    private fun sort(cards: List<PokemonCard>, selectedSort: String): List<PokemonCard> =
        when (selectedSort) {
            "Mayor precio primero" -> cards.sortedByDescending { it.price }
            "Menor precio primero" -> cards.sortedBy { it.price }
            "Nombre A-Z" -> cards.sortedBy { it.name.lowercase() }
            "Nombre Z-A" -> cards.sortedByDescending { it.name.lowercase() }
            else -> cards
        }
}
