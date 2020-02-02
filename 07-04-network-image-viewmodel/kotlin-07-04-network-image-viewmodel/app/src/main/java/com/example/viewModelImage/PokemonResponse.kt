package com.example.viewModelImage

data class PokemonResponse(
        var species: Species,
        var sprites: Sprites
) {
    data class Species(var name: String)
    data class Sprites(var frontDefault: String)
}