package org.abiri.stompcli.controller

import jakarta.servlet.http.HttpServletRequest
import org.abiri.stompcli.service.StompCliService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("server || clientHttpInterface")
class StompCliController(
    private val stompHelperSrv: StompCliService
) {

    companion object {
        val DESTINATION = Regex("^/(?:(\\w+):)?(.*)$")
    }

    @PostMapping("/**")
    fun postMessage(request: HttpServletRequest, @RequestBody body: String): ResponseEntity<*> {
        val (endpointName, destination) = parseDestinationString(request)
        try {
            if (endpointName == null) {
                stompHelperSrv.postMessageToAll(destination, body)
            } else {
                stompHelperSrv.postMessageToEndpoint(endpointName, destination, body)
            }
            return ResponseEntity.ok().body("")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    private fun parseDestinationString(request: HttpServletRequest) =
        DESTINATION.matchEntire(request.requestURI)!!.run {
            Pair(groupValues[1].ifEmpty { null }, "/${groupValues[2]}")
        }

    @MessageMapping("/**")
    fun onMessageReceived(@Header destination: String, @Payload body: String) {
        stompHelperSrv.onMessageReceived(destination, body)
    }
}