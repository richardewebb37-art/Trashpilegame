package com.trashpiles.game.utils

/**
 * Asset Paths - Centralized asset path management
 * 
 * All image and audio paths in ONE place.
 * Makes it easy to update paths without touching component code.
 * 
 * Note: In Android, assets are typically placed in:
 * - Images: app/src/main/assets/images/
 * - Audio: app/src/main/assets/audio/
 */
object AssetPaths {
    
    // ========================================================================
    // CARD IMAGES (52 cards + back)
    // ========================================================================
    
    private const val CARD_BASE_PATH = "images/cards/"
    
    /**
     * Get path for a specific card
     * Example: getCardImage("ace", "spades") → "images/cards/ace_of_spades.png"
     */
    fun getCardImage(rank: String, suit: String): String {
        return "${CARD_BASE_PATH}${rank}_of_$suit.png"
    }
    
    /**
     * Card back image
     */
    const val CARD_BACK_IMAGE = "${CARD_BASE_PATH}card_back.png"
    
    /**
     * All 52 cards explicitly listed (for validation)
     */
    val ALL_CARDS = mapOf(
        "spades" to listOf(
            "ace_of_spades.png",
            "two_of_spades.png",
            "three_of_spades.png",
            "four_of_spades.png",
            "five_of_spades.png",
            "six_of_spades.png",
            "seven_of_spades.png",
            "eight_of_spades.png",
            "nine_of_spades.png",
            "ten_of_spades.png",
            "jack_of_spades.png",
            "queen_of_spades.png",
            "king_of_spades.png"
        ),
        "hearts" to listOf(
            "ace_of_hearts.png",
            "two_of_hearts.png",
            "three_of_hearts.png",
            "four_of_hearts.png",
            "five_of_hearts.png",
            "six_of_hearts.png",
            "seven_of_hearts.png",
            "eight_of_hearts.png",
            "nine_of_hearts.png",
            "ten_of_hearts.png",
            "jack_of_hearts.png",
            "queen_of_hearts.png",
            "king_of_hearts.png"
        ),
        "clubs" to listOf(
            "ace_of_clubs.png",
            "two_of_clubs.png",
            "three_of_clubs.png",
            "four_of_clubs.png",
            "five_of_clubs.png",
            "six_of_clubs.png",
            "seven_of_clubs.png",
            "eight_of_clubs.png",
            "nine_of_clubs.png",
            "ten_of_clubs.png",
            "jack_of_clubs.png",
            "queen_of_clubs.png",
            "king_of_clubs.png"
        ),
        "diamonds" to listOf(
            "ace_of_diamonds.png",
            "two_of_diamonds.png",
            "three_of_diamonds.png",
            "four_of_diamonds.png",
            "five_of_diamonds.png",
            "six_of_diamonds.png",
            "seven_of_diamonds.png",
            "eight_of_diamonds.png",
            "nine_of_diamonds.png",
            "ten_of_diamonds.png",
            "jack_of_diamonds.png",
            "queen_of_diamonds.png",
            "king_of_diamonds.png"
        )
    )
    
    // ========================================================================
    // BUTTON IMAGES
    // ========================================================================
    
    private const val BUTTON_BASE_PATH = "images/buttons/"
    
    const val DEAL_BUTTON = "${BUTTON_BASE_PATH}deal_button.png"
    const val DRAW_BUTTON = "${BUTTON_BASE_PATH}draw_button.png"
    const val PLAY_BUTTON = "${BUTTON_BASE_PATH}play_button.png"
    const val MENU_BUTTON = "${BUTTON_BASE_PATH}menu_button.png"
    
    // ========================================================================
    // BACKGROUND IMAGES
    // ========================================================================
    
    private const val BACKGROUND_BASE_PATH = "images/backgrounds/"
    
    const val GAME_TABLE = "${BACKGROUND_BASE_PATH}game_table.png"
    const val MENU_BACKGROUND = "${BACKGROUND_BASE_PATH}menu_background.png"
    
    // ========================================================================
    // UI IMAGES
    // ========================================================================
    
    private const val UI_BASE_PATH = "images/ui/"
    
    const val CHIP_INDICATOR = "${UI_BASE_PATH}chip_indicator.png"
    const val CARD_SLOT = "${UI_BASE_PATH}card_slot.png"
    const val CARD_PLACEHOLDER = "${UI_BASE_PATH}card_placeholder.png"
    
    // ========================================================================
    // PARTICLE IMAGES (optional)
    // ========================================================================
    
    private const val PARTICLE_BASE_PATH = "images/particles/"
    
    const val STAR_PARTICLE = "${PARTICLE_BASE_PATH}star.png"
    const val SPARKLE_PARTICLE = "${PARTICLE_BASE_PATH}sparkle.png"
    
    // ========================================================================
    // AUDIO FILES
    // ========================================================================
    
    private const val AUDIO_SFX_PATH = "audio/sfx/"
    private const val AUDIO_MUSIC_PATH = "audio/music/"
    
    // Sound effects
    const val CARD_FLIP_SOUND = "${AUDIO_SFX_PATH}card_flip.mp3"
    const val CARD_DEAL_SOUND = "${AUDIO_SFX_PATH}card_deal.mp3"
    const val CARD_PLACE_SOUND = "${AUDIO_SFX_PATH}card_place.mp3"
    const val BUTTON_CLICK_SOUND = "${AUDIO_SFX_PATH}button_click.mp3"
    const val VICTORY_SOUND = "${AUDIO_SFX_PATH}victory.mp3"
    
    // Background music
    const val BACKGROUND_MUSIC = "${AUDIO_MUSIC_PATH}background_music.mp3"
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * Validate that all card assets exist (for debugging)
     */
    fun getAllCardPaths(): List<String> {
        val paths = mutableListOf<String>()
        for ((suit, cards) in ALL_CARDS) {
            paths.addAll(cards.map { card -> "$CARD_BASE_PATH$card" })
        }
        paths.add(CARD_BACK_IMAGE)
        return paths
    }
    
    /**
     * Get all asset paths for preloading
     */
    fun getAllAssetPaths(): List<String> {
        return buildList {
            addAll(getAllCardPaths())
            add(DEAL_BUTTON)
            add(DRAW_BUTTON)
            add(PLAY_BUTTON)
            add(MENU_BUTTON)
            add(GAME_TABLE)
            add(MENU_BACKGROUND)
            add(CHIP_INDICATOR)
            add(CARD_SLOT)
            add(CARD_PLACEHOLDER)
            add(STAR_PARTICLE)
            add(SPARKLE_PARTICLE)
        }
    }
    
    /**
     * Get all audio paths for preloading
     */
    fun getAllAudioPaths(): List<String> {
        return listOf(
            CARD_FLIP_SOUND,
            CARD_DEAL_SOUND,
            CARD_PLACE_SOUND,
            BUTTON_CLICK_SOUND,
            VICTORY_SOUND,
            BACKGROUND_MUSIC
        )
    }
    
    /**
     * Validate a card path exists in the defined cards
     */
    fun isValidCardPath(rank: String, suit: String): Boolean {
        val cards = ALL_CARDS[suit] ?: return false
        val expectedCard = "${rank}_of_$suit.png"
        return cards.contains(expectedCard)
    }
    
    /**
     * Get all suits
     */
    fun getAllSuits(): List<String> = ALL_CARDS.keys.toList()
    
    /**
     * Get all ranks for a suit
     */
    fun getRanksForSuit(suit: String): List<String> {
        return ALL_CARDS[suit]?.map { card ->
            // Extract rank from "rank_of_suit.png"
            card.substringBefore("_of_")
        } ?: emptyList()
    }
}