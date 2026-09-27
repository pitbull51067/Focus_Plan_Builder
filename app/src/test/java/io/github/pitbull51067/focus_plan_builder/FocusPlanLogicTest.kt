package io.github.pitbull51067.focus_plan_builder

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the two pure calculation functions used by the Focus Plan
 * Builder screen. These run on the JVM (no emulator needed) via
 * Run > FocusPlanLogicTest, and cover the required test-case table from the
 * assignment for the parts that don't require the UI.
 */
class FocusPlanLogicTest {

    // --- durationCategory --------------------------------------------------

    @Test
    fun durationCategory_belowTen_isInvalid() {
        assertEquals("Invalid", durationCategory(9))
    }

    @Test
    fun durationCategory_tenToTwentyNine_isQuickReview() {
        assertEquals("Quick review", durationCategory(10))
        assertEquals("Quick review", durationCategory(20))
        assertEquals("Quick review", durationCategory(29))
    }

    @Test
    fun durationCategory_thirtyToSixty_isFocusedSession() {
        assertEquals("Focused session", durationCategory(30))
        assertEquals("Focused session", durationCategory(45))
        assertEquals("Focused session", durationCategory(60))
    }

    @Test
    fun durationCategory_aboveSixty_isExtendedSession() {
        assertEquals("Extended session", durationCategory(61))
        assertEquals("Extended session", durationCategory(90))
        assertEquals("Extended session", durationCategory(180))
    }

    // --- recommendedBreak ---------------------------------------------------

    @Test
    fun recommendedBreak_tenToTwentyNine_isFive() {
        assertEquals(5, recommendedBreak(10))
        assertEquals(5, recommendedBreak(29))
    }

    @Test
    fun recommendedBreak_thirtyToSixty_isTen() {
        assertEquals(10, recommendedBreak(30))
        assertEquals(10, recommendedBreak(60))
    }

    @Test
    fun recommendedBreak_aboveSixty_isFifteen() {
        assertEquals(15, recommendedBreak(61))
        assertEquals(15, recommendedBreak(180))
    }

    // --- toIntOrNull safety (mirrors what the UI relies on) -----------------

    @Test
    fun toIntOrNull_onNonNumericInput_returnsNullInsteadOfCrashing() {
        assertEquals(null, "abc".toIntOrNull())
        assertEquals(null, "".toIntOrNull())
    }
}
