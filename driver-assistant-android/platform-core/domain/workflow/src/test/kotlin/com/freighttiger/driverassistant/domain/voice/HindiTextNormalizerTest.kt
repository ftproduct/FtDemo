package com.freighttiger.driverassistant.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class HindiTextNormalizerTest {

    @Test
    fun `romanised spelling variants collapse to canonical tokens`() {
        assertEquals("haan ji", HindiTextNormalizer.normalize("Hanji!"))
        assertEquals("nahi", HindiTextNormalizer.normalize("Nahin."))
        assertEquals("nahi", HindiTextNormalizer.normalize("nhi"))
        assertEquals("main pahunch gaya", HindiTextNormalizer.normalize("Mai pahuch gayi"))
    }

    @Test
    fun `devanagari input maps to the same tokens as romanised input`() {
        assertEquals("haan", HindiTextNormalizer.normalize("हाँ"))
        assertEquals("haan", HindiTextNormalizer.normalize("हां।"))
        assertEquals("abhi nahi", HindiTextNormalizer.normalize("अभी नहीं"))
        assertEquals("ek ghanta lagega", HindiTextNormalizer.normalize("एक घंटा लगेगा"))
        assertEquals("main pahunch gaya", HindiTextNormalizer.normalize("मैं पहुँच गया"))
    }

    @Test
    fun `devanagari digits become ascii digits`() {
        assertEquals("45 minute", HindiTextNormalizer.normalize("४५ मिनट"))
    }

    @Test
    fun `negation is preserved and never dropped`() {
        assertEquals("main sahmat nahi hoon", HindiTextNormalizer.normalize("मैं सहमत नहीं हूँ"))
    }

    @Test
    fun `blank and punctuation only input gives empty text`() {
        assertEquals("", HindiTextNormalizer.normalize("   ...  "))
    }
}
