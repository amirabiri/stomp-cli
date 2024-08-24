package org.abiri.stompcli.converter

import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.AbstractMessageConverter

class SimpleStringMessageConverter : AbstractMessageConverter() {
    override fun supports(clazz: Class<*>) =
        true

    override fun convertFromInternal(message: Message<*>, targetClass: Class<*>, conversionHint: Any?) =
        (message.payload as ByteArray).toString(Charsets.UTF_8)

    override fun convertToInternal(payload: Any, headers: MessageHeaders?, conversionHint: Any?) =
        (payload as String).toByteArray()
}