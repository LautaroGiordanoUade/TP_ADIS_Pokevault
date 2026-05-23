package com.pokevault.mobile.data.repository

import com.pokevault.mobile.domain.model.PokemonCard

object DemoCards {
    val cards = listOf(
        PokemonCard("charizard-vmax", "Charizard VMAX", "", "VMAX", 349.99, "Carta premium certificada.", listOf("Fire"), "Champion's Path", "020", "EliteTCG_Pro", "demo", true),
        PokemonCard("blastoise-base-holo", "Blastoise Base Holo", "", "Stage 2", 420.00, "Base holo PSA.", listOf("Water"), "Base Set", "002", "Kanto_Vintage", "demo", true),
        PokemonCard("gengar-vmax-alt", "Gengar VMAX Alt Art", "", "VMAX", 280.00, "Arte alternativo.", listOf("Ghost"), "Fusion Strike", "271", "ShadowLeague_TCG", "demo", true),
        PokemonCard("venusaur-base", "Venusaur Base Holo", "", "Stage 2", 390.00, "Coleccionable clásico.", listOf("Grass"), "Base Set", "015", "Kanto_Vintage", "demo"),
        PokemonCard("pikachu-illustrator", "Pikachu Illustrator", "", "Promo", 4999.00, "Promo de alto valor.", listOf("Electric"), "Promo", "001", "Pallet_Org", "demo"),
        PokemonCard("umbreon-vmax", "Umbreon VMAX Alt", "", "VMAX", 649.00, "Moonbreon premium.", listOf("Dark"), "Evolving Skies", "215", "NeoVault", "demo"),
        PokemonCard("rayquaza-vmax", "Rayquaza VMAX Alt", "", "VMAX", 510.00, "Dragón certificado.", listOf("Dragon"), "Evolving Skies", "218", "HoennCards", "demo"),
        PokemonCard("mewtwo-ex-full-art", "Mewtwo EX Full Art", "", "EX", 270.00, "Full art impecable.", listOf("Psychic"), "Next Destinies", "098", "Pallet_Org", "demo"),
    )
}
