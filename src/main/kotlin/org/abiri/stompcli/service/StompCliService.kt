package org.abiri.stompcli.service

interface StompCliService {
    fun postMessageToAll(destination: String, body: String)
    fun postMessageToEndpoint(endpointName: String, destination: String, body: String)
    fun onMessageReceived(destination: String, body: String)
}
