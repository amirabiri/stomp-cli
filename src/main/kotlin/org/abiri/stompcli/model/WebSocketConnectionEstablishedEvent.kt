package org.abiri.stompcli.model

import org.springframework.context.ApplicationEvent
import org.springframework.web.socket.WebSocketSession

class WebSocketConnectionEstablishedEvent(source: Any, val session: WebSocketSession) : ApplicationEvent(source)
