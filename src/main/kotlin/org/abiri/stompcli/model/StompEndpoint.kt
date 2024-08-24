package org.abiri.stompcli.model

import java.net.URI

sealed interface StompEndpoint {
    companion object {
        private val PREFIX_REGEX = Regex("^(?:([-\\w]+):)?(wss?://[^,]*)(?:,(.*))?$")

        fun parse(type: StompEndpointType, endpointStr: String) =
            PREFIX_REGEX.matchEntire(endpointStr)?.run {
                val subscriptions = groupValues[3].splitOrNull()
                when {
                    groupValues[1].isEmpty() -> UnnamedStompEndpoint(type, URI(groupValues[2]), subscriptions)
                    else                     -> NamedStompEndpoint(type, groupValues[1], URI(groupValues[2]), subscriptions)
                }
            }

        private fun String.splitOrNull() = when {
            isEmpty() -> null
            else      -> split(",")
        }
    }

    val type: StompEndpointType
    val url: URI
    val subscriptions: List<String>?
    val displayName: String

    fun isServer() =
        type == StompEndpointType.SERVER
}

data class UnnamedStompEndpoint(
    override val type: StompEndpointType,
    override val url: URI,
    override val subscriptions: List<String>?
) : StompEndpoint {
    override val displayName = url.toString()
}

data class NamedStompEndpoint(
    override val type: StompEndpointType,
    val name: String,
    override val url: URI,
    override val subscriptions: List<String>?
) : StompEndpoint {
    override val displayName
        get() = name
}

fun Collection<StompEndpoint>.noServers() = none { it.type == StompEndpointType.SERVER }
fun Collection<StompEndpoint>.anyClients() = any { it.type == StompEndpointType.CLIENT }
fun Collection<StompEndpoint>.clients() = filter { it.type == StompEndpointType.CLIENT }
