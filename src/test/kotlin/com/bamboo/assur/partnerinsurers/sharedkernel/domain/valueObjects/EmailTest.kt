package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmailTest {

    @Test
    fun `constructing with valid email succeeds`() {
        val email = Email("user@example.com")

        assertEquals("user@example.com", email.value)
    }

    @Test
    fun `blank email is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Email("   ")
        }
    }

    @Test
    fun `invalid email format is rejected`() {
        listOf(
            "user@",
            "@example.com",
            "userexample.com",
            "user@ex",
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>("Expected '$value' to be rejected") {
                Email(value)
            }
        }
    }
}

