package com.unotag.mokone

import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
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
        val a = InAppMessageBaseActivity()

    @Test
    fun `determineMessageType returns HTML when hasRawHtml is true`() {
        val result = a.determineMessageType(hasRawHtml = true, hasText = false, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.HTML, result)
    }

    @Test
    fun `determineMessageType returns TEXT when hasText is true`() {
        val result = a.determineMessageType(hasRawHtml = false, hasText = true, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.TEXT, result)
    }

    @Test
    fun `determineMessageType returns IMAGE when hasImage is true`() {
        val result = a.determineMessageType(hasRawHtml = false, hasText = false, hasImage = true, hasWebSite = false)
        assertEquals(MessageType.IMAGE, result)
    }

    @Test
    fun `determineMessageType returns WEB when hasWebSite is true`() {
        val result = a.determineMessageType(hasRawHtml = false, hasText = false, hasImage = false, hasWebSite = true)
        assertEquals(MessageType.WEB, result)
    }

    @Test
    fun `determineMessageType returns UNKNOWN when all flags are false`() {
        val result = a.determineMessageType(hasRawHtml = false, hasText = false, hasImage = false, hasWebSite = false)
        assertEquals(MessageType.UNKNOWN, result)
    }
}
