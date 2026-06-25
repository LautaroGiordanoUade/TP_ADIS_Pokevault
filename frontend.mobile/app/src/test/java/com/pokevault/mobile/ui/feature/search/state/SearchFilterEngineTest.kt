package com.pokevault.mobile.ui.feature.search.state

import com.pokevault.mobile.domain.model.PokemonCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterEngineTest {
    private val cards = listOf(
        pokemonCard(
            id = 1,
            name = "Charizard",
            types = listOf("Fire"),
            rarity = "Rare Holo",
            price = 80.0,
            setName = "Base Set",
        ),
        pokemonCard(
            id = 2,
            name = "Squirtle",
            types = listOf("Water"),
            rarity = "Common",
            price = 4.0,
            setName = "Team Rocket",
        ),
        pokemonCard(
            id = 3,
            name = "Bulbasaur",
            types = listOf("Grass"),
            rarity = "Promo",
            price = 12.0,
            setName = "Promo Pack",
        ),
        pokemonCard(
            id = 4,
            name = "Charmander",
            types = listOf("Fire"),
            rarity = "Common",
            price = 6.0,
            setName = "Gym Heroes",
        ),
    )

    @Test
    fun `applies search with filters together`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(
                query = "char",
                selectedType = "Fire",
                selectedRarity = "Rare",
                selectedPrice = "Hasta $100",
                selectedSort = "Mayor precio primero",
            ),
        )

        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun `applies filters without search`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(
                selectedType = "Fire",
                selectedPrice = "Hasta $20",
                selectedSort = "Menor precio primero",
            ),
        )

        assertEquals(listOf(4), result.map { it.id })
    }

    @Test
    fun `applies rarity filter without search`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(selectedRarity = "Common"),
        )

        assertEquals(listOf(2, 4), result.map { it.id })
    }

    @Test
    fun `applies max price filter without search`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(selectedPrice = "Hasta $5"),
        )

        assertEquals(listOf(2), result.map { it.id })
    }

    @Test
    fun `applies search without filters`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(query = "promo"),
        )

        assertEquals(listOf(3), result.map { it.id })
    }

    @Test
    fun `empty search and cleared filters keep all cards`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(),
        )

        assertEquals(cards.map { it.id }, result.map { it.id })
    }

    @Test
    fun `sorts alphabetically for edge cases`() {
        val result = SearchFilterEngine.apply(
            cards = cards,
            criteria = SearchCriteria(selectedSort = "Nombre A-Z"),
        )

        assertEquals(listOf("Bulbasaur", "Charizard", "Charmander", "Squirtle"), result.map { it.name })
    }

    @Test
    fun `builds type options from loaded cards`() {
        val options = SearchFilterOptions.availableTypeOptions(cards)

        assertEquals(listOf(SearchFilterOptions.ALL_TYPES, "Fire", "Grass", "Water"), options)
    }

    @Test
    fun `builds rarity options from loaded cards`() {
        val options = SearchFilterOptions.availableRarityOptions(cards)

        assertEquals(listOf(SearchFilterOptions.ALL_RARITIES, "Common", "Promo", "Rare Holo"), options)
    }

    @Test
    fun `keeps selected type visible even when no longer in current list`() {
        val options = SearchFilterOptions.availableTypeOptions(cards.filter { "Water" in it.types }, selectedType = "Fire")

        assertEquals(listOf(SearchFilterOptions.ALL_TYPES, "Fire", "Water"), options)
    }

    @Test
    fun `price and sort options keep clear state available`() {
        assertTrue(SearchFilterOptions.priceOptions.first() == SearchFilterOptions.ANY_PRICE)
        assertTrue(SearchFilterOptions.sortOptions.first() == SearchFilterOptions.NO_SORT)
    }

    @Test
    fun `criteria reports active filters and can be cleared`() {
        val activeCriteria = SearchCriteria(
            selectedType = "Fire",
            selectedRarity = "Rare",
            selectedPrice = "Hasta $20",
            selectedSort = "Nombre A-Z",
        )

        assertTrue(activeCriteria.hasActiveLocalFilters())
        assertFalse(SearchCriteria().hasActiveLocalFilters())
    }

    private fun pokemonCard(
        id: Int,
        name: String,
        types: List<String>,
        rarity: String?,
        price: Double,
        setName: String? = null,
    ) = PokemonCard(
        id = id,
        externalId = "card-$id",
        name = name,
        imageUrl = "https://example.com/$id.png",
        rarity = rarity,
        price = price,
        description = "$name description",
        types = types,
        setName = setName,
        number = id.toString(),
        artist = "Artist $id",
        source = "test",
    )
}
