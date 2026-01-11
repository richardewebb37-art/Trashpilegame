package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for DeckBuilder
 * Tests deck creation, shuffling, and dealing
 */
class DeckBuilderTest {
    
    @Test
    fun `test create standard 52-card deck`() {
        val deck = DeckBuilder.createDeck()
        
        assertEquals(52, deck.size)
    }
    
    @Test
    fun `test deck has all suits`() {
        val deck = DeckBuilder.createDeck()
        
        val hearts = deck.filter { it.suit == Suit.HEARTS }
        val diamonds = deck.filter { it.suit == Suit.DIAMONDS }
        val clubs = deck.filter { it.suit == Suit.CLUBS }
        val spades = deck.filter { it.suit == Suit.SPADES }
        
        assertEquals(13, hearts.size)
        assertEquals(13, diamonds.size)
        assertEquals(13, clubs.size)
        assertEquals(13, spades.size)
    }
    
    @Test
    fun `test deck has all ranks`() {
        val deck = DeckBuilder.createDeck()
        
        val ranks = listOf(
            "ace", "two", "three", "four", "five", "six",
            "seven", "eight", "nine", "ten", "jack", "queen", "king"
        )
        
        ranks.forEach { rank ->
            val cardsOfRank = deck.filter { it.rank == rank }
            assertEquals("Should have 4 cards of rank $rank", 4, cardsOfRank.size)
        }
    }
    
    @Test
    fun `test all cards are face down initially`() {
        val deck = DeckBuilder.createDeck()
        
        assertTrue(deck.all { !it.isFaceUp })
    }
    
    @Test
    fun `test shuffle changes deck order`() {
        val deck1 = DeckBuilder.createDeck()
        val deck2 = DeckBuilder.shuffle(deck1)
        
        // Same size
        assertEquals(deck1.size, deck2.size)
        
        // Same cards (by ID), but likely different order
        val ids1 = deck1.map { it.id }.sorted()
        val ids2 = deck2.map { it.id }.sorted()
        assertEquals(ids1, ids2)
        
        // Order should be different (with very high probability)
        val order1 = deck1.map { it.id }
        val order2 = deck2.map { it.id }
        assertNotEquals(order1, order2)
    }
    
    @Test
    fun `test deal cards from deck`() {
        val deck = DeckBuilder.createDeck()
        val (dealtCards, remainingDeck) = DeckBuilder.deal(deck, 5)
        
        assertEquals(5, dealtCards.size)
        assertEquals(47, remainingDeck.size)
        
        // Dealt cards should be from top of deck
        assertEquals(deck.take(5), dealtCards)
    }
    
    @Test
    fun `test deal more cards than available`() {
        val deck = DeckBuilder.createDeck()
        val (dealtCards, remainingDeck) = DeckBuilder.deal(deck, 60)
        
        // Should only deal available cards
        assertEquals(52, dealtCards.size)
        assertTrue(remainingDeck.isEmpty())
    }
    
    @Test
    fun `test deal from empty deck`() {
        val emptyDeck = emptyList<CardState>()
        val (dealtCards, remainingDeck) = DeckBuilder.deal(emptyDeck, 5)
        
        assertTrue(dealtCards.isEmpty())
        assertTrue(remainingDeck.isEmpty())
    }
    
    @Test
    fun `test card IDs are unique`() {
        val deck = DeckBuilder.createDeck()
        val ids = deck.map { it.id }
        val uniqueIds = ids.toSet()
        
        assertEquals(52, uniqueIds.size)
    }
    
    @Test
    fun `test card ID format`() {
        val deck = DeckBuilder.createDeck()
        
        deck.forEach { card ->
            // ID should be in format: rank_of_suit
            assertTrue(card.id.contains("_of_"))
            assertTrue(card.id.startsWith(card.rank))
            assertTrue(card.id.endsWith(card.suit.name.lowercase()))
        }
    }
    
    @Test
    fun `test multiple shuffles produce different orders`() {
        val deck = DeckBuilder.createDeck()
        
        val shuffled1 = DeckBuilder.shuffle(deck)
        val shuffled2 = DeckBuilder.shuffle(deck)
        val shuffled3 = DeckBuilder.shuffle(deck)
        
        val order1 = shuffled1.map { it.id }
        val order2 = shuffled2.map { it.id }
        val order3 = shuffled3.map { it.id }
        
        // All should be different (with very high probability)
        assertNotEquals(order1, order2)
        assertNotEquals(order2, order3)
        assertNotEquals(order1, order3)
    }
    
    @Test
    fun `test deal multiple times`() {
        var deck = DeckBuilder.createDeck()
        
        // Deal 5 cards
        val (hand1, deck1) = DeckBuilder.deal(deck, 5)
        assertEquals(5, hand1.size)
        assertEquals(47, deck1.size)
        
        // Deal 5 more cards
        val (hand2, deck2) = DeckBuilder.deal(deck1, 5)
        assertEquals(5, hand2.size)
        assertEquals(42, deck2.size)
        
        // Hands should be different
        assertNotEquals(hand1, hand2)
    }
}