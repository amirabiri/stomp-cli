package org.abiri.stompcli.service

import org.abiri.stompcli.model.StompEndpoint
import org.abiri.stompcli.model.WebSocketConnectionClosedEvent
import org.abiri.stompcli.model.WebSocketConnectionEstablishedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate

class StompCliServiceServerImpl(
    private val endpoint: StompEndpoint,
    private val simpMsgTpl: SimpMessagingTemplate,
    private val reporter: StompCliReporter
) : StompCliService {

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        reporter.connectionStatus(endpoint, "Listening")
    }

    @EventListener
    fun onConnectionEstablished(evt: WebSocketConnectionEstablishedEvent) {
        reporter.connectionStatus(endpoint, "Client connected: ${evt.session.remoteAddress.toString().trimStart('/')}")
    }

    @EventListener
    fun onConnectionClosed(evt: WebSocketConnectionClosedEvent) {
        reporter.connectionStatus(endpoint, "Client disconnected: ${evt.session.remoteAddress.toString().trimStart('/')}")
    }

    override fun onMessageReceived(destination: String, body: String) {
        reporter.messageReceived(endpoint, destination, body)
    }

    override fun postMessageToAll(destination: String, body: String) {
        reporter.messageSent(endpoint, destination, body)
        simpMsgTpl.convertAndSend(destination, body)
    }

    override fun postMessageToEndpoint(endpointName: String, destination: String, body: String) {
        throw IllegalArgumentException("Sending message to endpoint by name is not supported in server mode")
    }
}