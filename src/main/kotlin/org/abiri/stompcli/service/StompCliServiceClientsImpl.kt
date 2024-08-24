package org.abiri.stompcli.service

import org.abiri.stompcli.model.NamedStompEndpoint
import org.abiri.stompcli.model.StompEndpoint
import org.abiri.stompcli.stomp.StompClientSessionManager
import org.abiri.stompcli.stomp.StompClientSessionHandler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.socket.messaging.WebSocketStompClient

class StompCliServiceClientsImpl(
    private val endpoints: List<StompEndpoint>,
    private val stompClient: WebSocketStompClient,
    private val taskScheduler: TaskScheduler,
    private val reporter: StompCliReporter
) : StompCliService {

    private val allStompSessions = ArrayList<Pair<StompEndpoint, StompClientSessionManager>>()
    private val namedStompSessions = HashMap<String, Pair<StompEndpoint, StompClientSessionManager>>()

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        for (endpoint in endpoints) {
            createStompSession(endpoint)
        }
    }

    private fun createStompSession(endpoint: StompEndpoint) {
        val subscriptions = endpoint.subscriptions ?: listOf("/**")

        val session = StompClientSessionManager(endpoint.url, subscriptions, stompClient, taskScheduler, object :
            StompClientSessionHandler {

            override fun onConnectionStatusChanged(status: String) =
                reporter.connectionStatus(endpoint, status)

            override fun onMessageReceived(headers: StompHeaders, payload: Any?) =
                reporter.messageReceived(endpoint, headers.destination!!, payload as String)

            override fun onException(exception: Throwable) {
                exception.printStackTrace()
            }
        })

        session.connect()

        allStompSessions += Pair(endpoint, session)
        if (endpoint is NamedStompEndpoint) {
            namedStompSessions[endpoint.name] = Pair(endpoint, session)
        }
    }

    override fun postMessageToAll(destination: String, body: String) {
        reporter.messageSentToAll(destination, body)
        for ((_, session) in allStompSessions) {
            session.sendMessage(destination, body)
        }
    }

    override fun postMessageToEndpoint(endpointName: String, destination: String, body: String) {
        val (endpoint, session) = namedStompSessions[endpointName]
            ?: throw IllegalArgumentException("Unknown endpoint: $endpointName")
        reporter.messageSent(endpoint, destination, body)
        session.sendMessage(destination, body)
    }

    override fun onMessageReceived(destination: String, body: String) {
        throw RuntimeException("Client is not supposed to invoke message received through @MessageMapping")
    }
}