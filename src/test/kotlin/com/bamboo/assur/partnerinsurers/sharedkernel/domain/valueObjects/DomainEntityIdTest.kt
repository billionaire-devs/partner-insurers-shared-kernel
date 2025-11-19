package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import java.util.UUID

class DomainEntityIdTest {

    @Test
    fun `random should generate different values`() {
        val first = DomainEntityId.random()
        val second = DomainEntityId.random()

        assertNotEquals(first, second, "Two random identifiers should not be equal in practice")
    }

    @Test
    fun `fromString should parse a valid uuid string`() {
        val uuid = UUID.randomUUID()
        val domainId = DomainEntityId.fromString(uuid.toString())

        assertEquals(uuid, domainId.value)
    }

    @Test
    fun `fromString should reject blank string`() {
        assertFailsWith<IllegalArgumentException> {
            DomainEntityId.fromString("")
        }
    }

    @Test
    fun `fromString should reject malformed uuid`() {
        assertFailsWith<IllegalArgumentException> {
            DomainEntityId.fromString("not-a-uuid")
        }
    }
}

