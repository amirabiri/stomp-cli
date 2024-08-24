package org.abiri.stompcli.service

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import org.abiri.stompcli.model.StompEndpoint

class StompCliReporter(
    private val showEndpointNames: Boolean
) {

    private val t = Terminal()

    fun connectionStatus(endpoint: StompEndpoint, status: String) {
        t.println(gray(formatLine(endpoint.displayName, "CONN", status)))
    }

    fun messageReceived(endpoint: StompEndpoint, destination: String, payload: String) {
        t.println(formatLine(blue(endpoint.displayName), brightGreen("RECV"), "${brightYellow(destination)} : ${payload.replaceSpecialChars()}"))
    }

    fun messageSent(endpoint: StompEndpoint, destination: String, body: String) {
        t.println(formatLine(blue(endpoint.displayName), brightGreen("SEND"), "${brightYellow(destination)} : ${body.replaceSpecialChars()}"))
    }

    fun messageSentToAll(destination: String, body: String) {
        t.println(formatLine(blue("*"), brightGreen("SEND"), "${brightYellow(destination)} : ${body.replaceSpecialChars()}"))
    }

    private fun String.replaceSpecialChars() = buildString {
        for (c in this@replaceSpecialChars) {
            when {
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c.isISOControl() -> append("\\u%04x".format(c.code))
                else -> append(c)
            }
        }
    }

    private fun formatLine(endpointName: String, prefix: String, message: String) = buildString {
        append(prefix)
        if (showEndpointNames) {
            append(" ["); append(endpointName); append("]")
        }
        append(" - ")
        append(message)
    }
}