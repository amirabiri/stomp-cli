package org.abiri.stompcli.stomp

import org.abiri.stompcli.model.WebSocketConnectionClosedEvent
import org.abiri.stompcli.model.WebSocketConnectionEstablishedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.WebSocketHandlerDecorator

class ConnectionEventsWebSocketHandlerDecorator(
    delegate: WebSocketHandler,
    private val appEventsPublisher: ApplicationEventPublisher
) : WebSocketHandlerDecorator(delegate) {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        appEventsPublisher.publishEvent(WebSocketConnectionEstablishedEvent(delegate, session))
        super.afterConnectionEstablished(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        appEventsPublisher.publishEvent(WebSocketConnectionClosedEvent(delegate, session))
        super.afterConnectionClosed(session, closeStatus)
    }

}