package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddressTest {

    @Test
    fun `valid address is accepted`() {
        val address = Address(
            street = "221B Baker Street",
            city = "London",
            country = "UK",
            zipCode = "NW1",
        )

        assertEquals("221B Baker Street", address.street)
        assertEquals("London", address.city)
        assertEquals("UK", address.country)
        assertEquals("NW1", address.zipCode)
    }

    @Test
    fun `zip code can be null`() {
        val address = Address(
            street = "221B Baker Street",
            city = "London",
            country = "UK",
            zipCode = null,
        )

        assertEquals(null, address.zipCode)
    }

    @Test
    fun `blank street is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Address(
                street = "",
                city = "City",
                country = "Country",
                zipCode = null,
            )
        }
    }

    @Test
    fun `blank city is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Address(
                street = "Street",
                city = "",
                country = "Country",
                zipCode = null,
            )
        }
    }

    @Test
    fun `blank country is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Address(
                street = "Street",
                city = "City",
                country = "",
                zipCode = null,
            )
        }
    }
}

