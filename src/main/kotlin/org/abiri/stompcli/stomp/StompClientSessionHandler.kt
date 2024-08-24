package org.abiri.stompcli.stomp

import org.springframework.messaging.simp.stomp.StompHeaders

interface StompClientSessionHandler {

    fun onConnectionStatusChanged(status: String)
    fun onMessageReceived(headers: StompHeaders, payload: Any?)
    fun onException(exception: Throwable)

}