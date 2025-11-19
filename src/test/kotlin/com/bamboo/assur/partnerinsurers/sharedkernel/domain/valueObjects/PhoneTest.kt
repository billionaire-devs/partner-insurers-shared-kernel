package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PhoneTest {

    @Test
    fun `valid e164 like phone numbers are accepted`() {
        val phoneWithPlus = Phone("+33123456789")
        val phoneWithoutPlus = Phone("0123456789")

        assertEquals("+33123456789", phoneWithPlus.value)
        assertEquals("0123456789", phoneWithoutPlus.value)
    }

    @Test
    fun `blank phone is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Phone("   ")
        }
    }

    @Test
    fun `invalid phone formats are rejected`() {
        listOf(
            "12345",          // too short
            "+12-34-56",      // contains non-digit characters
            "+abc1234567",    // letters are not allowed
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>("Expected '$value' to be rejected") {
                Phone(value)
            }
        }
    }
}

