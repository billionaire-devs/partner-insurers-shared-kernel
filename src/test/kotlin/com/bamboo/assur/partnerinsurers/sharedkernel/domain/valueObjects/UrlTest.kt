package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UrlTest {

    @Test
    fun `valid urls are accepted`() {
        val url = Url("https://example.com/path")

        assertEquals("https://example.com/path", url.value)
    }

    @Test
    fun `blank url is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Url("   ")
        }
    }

    @Test
    fun `malformed url is rejected`() {
        listOf(
            "not-a-url",
            "htp://bad-scheme",
            "://missing-scheme",
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>("Expected '$value' to be rejected") {
                Url(value)
            }
        }
    }
}

