package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import com.bamboo.assur.partnerinsurers.sharedkernel.infrastructure.serialization.SerializationConfig
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ApiResponseSerializationTest {

    private val json = SerializationConfig().kotlinxJson()

    @Test
    fun `should serialize and deserialize successful response`() {
        val metadata = Meta(
            request = RequestMetadata(method = "GET", path = "/partners", query = "status=active"),
            response = ResponseMetadata(
                status = 200,
                reason = "OK",
                timestamp = Instant.parse("2024-01-01T00:00:00Z")
            )
        )

        val response = ApiResponse(
            success = true,
            meta = metadata,
            data = mapOf("key" to "value"),
            error = null
        )

        val serializer = ApiResponse.serializer(MapSerializer(String.serializer(), String.serializer()))

        val payload = json.encodeToString(serializer, response)
        val restored = json.decodeFromString(serializer, payload)

        assertEquals(response, restored)
    }

    @Test
    fun `should serialize and deserialize error response`() {
        val metadata = Meta(
            request = RequestMetadata(method = "POST", path = "/partners"),
            response = ResponseMetadata(
                status = 400,
                reason = "Bad Request",
                timestamp = Instant.parse("2024-02-02T10:15:30Z")
            )
        )

        val response: ApiResponse<Unit> = ApiResponse(
            success = false,
            meta = metadata,
            data = null,
            error = ErrorBody(
                message = "Validation failed",
                details = "{\"name\":\"must not be blank\"}"
            )
        )

        val serializer = ApiResponse.serializer(Unit.serializer())

        val payload = json.encodeToString(serializer, response)
        val restored = json.decodeFromString(serializer, payload)

        assertEquals(response.success, restored.success)
        assertEquals(response.meta, restored.meta)
        assertNull(restored.data)
        assertEquals(response.error, restored.error)
    }
}
