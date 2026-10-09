package com.freighttiger.driverassistant.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EtaParserTest {
    private val parser = EtaParser()
    private fun parse(text: String) = parser.parse(HindiTextNormalizer.normalize(text))

    @Test fun `ek ghanta is 60 minutes`() = assertEquals(EtaEstimate(60, false), parse("Ek ghanta"))
    @Test fun `do ghante is 120 minutes`() = assertEquals(EtaEstimate(120, false), parse("Do ghante"))
    @Test fun `ek ghanta lagega`() = assertEquals(EtaEstimate(60, false), parse("Ek ghanta lagega"))
    @Test fun `aadha ghanta is 30`() = assertEquals(30, parse("aadha ghanta")?.minutes)
    @Test fun `dedh ghanta is 90`() = assertEquals(90, parse("डेढ़ घंटा")?.minutes)
    @Test fun `dhai ghante is 150`() = assertEquals(150, parse("dhai ghante")?.minutes)
    @Test fun `sava do ghante is 135`() = assertEquals(135, parse("sava do ghante")?.minutes)
    @Test fun `paune ghanta is 45`() = assertEquals(45, parse("paune ghanta")?.minutes)
    @Test fun `saadhe teen ghante is 210`() = assertEquals(210, parse("saadhe teen ghante")?.minutes)
    @Test fun `digits with minutes`() = assertEquals(EtaEstimate(45, false), parse("45 minute mein"))
    @Test fun `devanagari minutes`() = assertEquals(20, parse("बीस मिनट")?.minutes)
    @Test fun `hours and minutes add up`() = assertEquals(150, parse("2 ghante 30 minute")?.minutes)
    @Test fun `range takes upper bound and is approximate`() = assertEquals(EtaEstimate(180, true), parse("do teen ghante"))
    @Test fun `bare ghanta is approximately an hour`() = assertEquals(EtaEstimate(60, true), parse("ghanta lagega"))
    @Test fun `vague answers are not parsed`() {
        assertNull(parse("thodi der mein"))
        assertNull(parse("kal subah"))
        assertNull(parse("haan"))
        assertNull(parse(""))
    }
    @Test fun `absurd durations are rejected`() = assertNull(parse("5000 ghante"))
}
