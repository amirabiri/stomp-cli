package org.abiri.stompcli.stomp

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler
import org.springframework.messaging.support.ExecutorChannelInterceptor

class SuppressRelayFromClientsChannelInterceptor : ExecutorChannelInterceptor {

    override fun beforeHandle(message: Message<*>, channel: MessageChannel, handler: MessageHandler) = when {
        handler is SimpleBrokerMessageHandler && message.isMessageType() -> null
        else -> message
    }

    private fun Message<*>.isMessageType() =
        SimpMessageHeaderAccessor.getMessageType(headers) == SimpMessageType.MESSAGE
}