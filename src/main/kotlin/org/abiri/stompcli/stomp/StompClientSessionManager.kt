package org.abiri.stompcli.stomp

import jakarta.websocket.DeploymentException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.io.IOException
import java.net.URI
import java.nio.channels.UnresolvedAddressException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeoutException

class StompClientSessionManager(
    private val url: URI,
    private val subscriptions: List<String>,
    private val client: WebSocketStompClient,
    private val taskScheduler: TaskScheduler,
    private val handler: StompClientSessionHandler
) {

    private var currentSession: StompSession? = null

    fun connect() {
        client.connectAsync(url, null, null, stompSessionHandler)
    }

    private val stompSessionHandler = object : StompSessionHandlerAdapter() {

        private var connected = false
        private var lastConnectError: String? = null

        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
            connected = true
            lastConnectError = null
            currentSession = session
            handler.onConnectionStatusChanged("Connected")
            subscriptions.forEach { session.subscribe(it, this) }
        }

        override fun handleFrame(headers: StompHeaders, payload: Any?) {
            handler.onMessageReceived(headers, payload)
        }

        override fun handleException(session: StompSession, command: StompCommand?, headers: StompHeaders, payload: ByteArray, exception: Throwable) {
            handler.onException(exception)
        }

        override fun handleTransportError(session: StompSession, exception: Throwable) {
            if (connected) {
                handler.onConnectionStatusChanged("Disconnected")
            }
            else {
                val errorMessage = determineErrorMessage(exception)
                if (lastConnectError == null || lastConnectError != errorMessage) {
                    lastConnectError = errorMessage
                    handler.onConnectionStatusChanged("Connection failed: $errorMessage (retrying every 2s)")
                }
            }
            connected = false
            currentSession = null
            taskScheduler.schedule(this@StompClientSessionManager::connect, Instant.now() + Duration.ofSeconds(1))
        }
    }

    private fun determineErrorMessage(exception: Throwable) =
        when (val rootCause = exception.rootCause()) {
            is IOException                -> rootCause.message!!
            is DeploymentException        -> rootCause.message!!
            is UnresolvedAddressException -> "Unresolved address"
            is TimeoutException           -> "Timed out"
            else                          -> exception.message!!
        }

    private fun Throwable.rootCause(): Throwable {
        var rootCause = this
        while (rootCause.cause != null && rootCause.cause != rootCause) {
            rootCause = rootCause.cause!!
        }
        return rootCause
    }

    fun sendMessage(destination: String, payload: String) {
        currentSession?.send(destination, payload)
    }
}
