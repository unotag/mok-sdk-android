package com.unotag.mokone

import com.unotag.mokone.inAppMessage.ui.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

}


class MessageTypeTest {

    fun determineMessageType(
        hasRawHtml: Boolean,
        hasText: Boolean,
        hasImage: Boolean,
        hasWebSite: Boolean
    ): MessageType {
        return when {
            hasRawHtml -> MessageType.HTML
            hasText -> MessageType.TEXT
            hasImage -> MessageType.IMAGE
            hasWebSite -> MessageType.WEB
            else -> MessageType.UNKNOWN
        }
    }

    @Test
    fun testDetermineMessageType() {
        // Test case 1: HTML message
        val messageType1 = determineMessageType(hasRawHtml = true, hasText = false, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.HTML, messageType1)
        println("Type of message 1: $messageType1")

        // Test case 2: Text message
        val messageType2 = determineMessageType(hasRawHtml = false, hasText = true, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.TEXT, messageType2)
        println("Type of message 2: $messageType2")

        // Test case 3: Image message
        val messageType3 = determineMessageType(hasRawHtml = false, hasText = false, hasImage = true, hasWebSite = false)
        assertEquals(MessageType.IMAGE, messageType3)
        println("Type of message 3: $messageType3")

        // Test case 4: Web message
        val messageType4 = determineMessageType(hasRawHtml = false, hasText = false, hasImage = false, hasWebSite = true)
        assertEquals(MessageType.WEB, messageType4)
        println("Type of message 4: $messageType4")

        // Test case 5: Unknown message
        val messageType5 = determineMessageType(hasRawHtml = false, hasText = false, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.UNKNOWN, messageType5)
        println("Type of message 5: $messageType5")
    }
}
