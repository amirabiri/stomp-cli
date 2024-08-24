package org.abiri.stompcli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import org.abiri.stompcli.model.StompEndpoint
import org.abiri.stompcli.model.StompEndpointType

class StompCliCommand : CliktCommand(
    name = "stomp",
    help = "A simple STOMP command line development tool that can act as a server or client.",
    epilog = """
        At least one --server or --client option must be specified. STOMP endpoints are given in standard ws:// or
        wss:// notation, and can be optionally prefixed by a name using [name]:[endpoint] notation. For --server
        endpoints the scheme must be ws:// and the host must be localhost, 0.0.0.0 or 127.0.0.1.
        
        Client endpoints can be optionally followed by a comma separated list of subscription strings. If no
        subscriptions are specified, the client will subscribe to all destinations by subscribing to /**.
        
        STOMP messages can be sent using a simple HTTP interface - any POST request will be interpreted as a STOMP to
        send, with the request body as the payload and the URL path as the destination. --server endpoints automatically
        listen for such HTTP requests on the same port specified in their endpoint. For --client endpoints, the HTTP
        interface won't be created by default, the (-p|--http-port) option must be specified to enable it.
        
        The HTTP interface will be shared by all clients, i.e sending a POST HTTP request will result in sending STOMP
        messages to through all --client endpoints. To limit the message to a specific --client endpoint, the endpoint
        name should be used as a prefix in the URL path, e.g [name]:/topic/of/message/to/send.
        
        Examples:
        
        Start a STOMP server on ws://localhost:8080:
        
        stomp --server ws://localhost:8080
        
        Start two STOMP servers on ws://localhost:8080 and ws://localhost:8081, using names server1 and server2:
        
        stomp --server server1:ws://localhost:8080 --server server2:ws://localhost:8081
        
        Start a STOMP client and connect to server at ws://localhost:8080, subscribing to /topic/foo and /queue/bar:
        
        stomp --client ws://localhost:8080,/foo,/bar/\*\*        
        """.trimIndent(),
    printHelpOnEmptyArgs = true
) {

    val httpPort: Int? by option("--http-port", "-p", metavar = "http port").int().help("Http port to listen on")
    val endpoints by option("--server", "--client", metavar = "endpoint")
        .convert(metavar = "endpoint") { parseEndpoint(it) }
        .multiple()
        .help("STOMP endpoints")


    private fun OptionCallTransformContext.parseEndpoint(endpointStr: String) =
        StompEndpoint.parse(endpointType(name), endpointStr)
            ?: throw CliktError("'$endpointStr' is not a valid STOMP endpoint")

    private fun endpointType(name: String) = when (name) {
        "--server" -> StompEndpointType.SERVER
        "--client" -> StompEndpointType.CLIENT
        else -> throw CliktError("Unknown endpoint type")
    }

    override fun run() {
        if (endpoints.isEmpty()) {
            throw CliktError("Must specify at least one --server or --client STOMP endpoint")
        }
    }
}