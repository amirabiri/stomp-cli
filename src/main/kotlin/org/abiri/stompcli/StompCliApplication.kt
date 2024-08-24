package org.abiri.stompcli

import org.abiri.stompcli.command.StompCliCommand
import org.abiri.stompcli.converter.SimpleStringMessageConverter
import org.abiri.stompcli.model.anyClients
import org.abiri.stompcli.model.clients
import org.abiri.stompcli.model.noServers
import org.abiri.stompcli.service.StompCliReporter
import org.abiri.stompcli.service.StompCliServiceClientsImpl
import org.abiri.stompcli.service.StompCliServiceServerImpl
import org.abiri.stompcli.stomp.ConnectionEventsWebSocketHandlerDecorator
import org.abiri.stompcli.stomp.SuppressRelayFromClientsChannelInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner.Mode
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.CountDownLatch

val cliktCmd = StompCliCommand()
fun main(args: Array<String>) {
    with(cliktCmd) {
        main(args)
        val rootCtxt = StompCliRootApp.run()
        for ((idx, endpoint) in endpoints.withIndex()) {
            if (endpoint.isServer()) {
                StompCliServerContext.run(rootCtxt, idx)
            }
        }
        if (endpoints.anyClients()) {
            StompCliClientsContext.run(rootCtxt, httpPort)
        }
        if (endpoints.noServers()) {
            CountDownLatch(1).await()
        }
    }
}

@SpringBootApplication
@Profile("!server && !clients")
class StompCliRootApp {

    companion object {
        fun run(): ConfigurableApplicationContext = SpringApplicationBuilder()
            .sources(StompCliRootApp::class.java)
            .bannerMode(Mode.OFF)
            .web(WebApplicationType.NONE)
            .run()
    }

    @Bean
    fun stompCliReporter() = with(cliktCmd) {
        StompCliReporter(endpoints.size > 1)
    }
}

@SpringBootApplication
@EnableWebSocketMessageBroker
@Profile("server")
class StompCliServerContext(
    private val appEventsPublisher: ApplicationEventPublisher,
) : WebSocketMessageBrokerConfigurer {

    companion object {
        fun run(parent: ConfigurableApplicationContext, endpointIdx: Int): Unit = with(cliktCmd) {
            endpoints[endpointIdx].let { endpoint -> SpringApplicationBuilder()
                .parent(parent)
                .sources(StompCliServerContext::class.java)
                .profiles("server")
                .bannerMode(Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .properties(
                    mapOf(
                        "endpointIdx" to endpointIdx,
                        "server.address" to endpoint.url.host,
                        "server.port" to endpoint.url.port
                    )
                )
                .run()
            }
        }
    }

    @Bean
    fun stompCliService(
        @Value("\${endpointIdx}") endpointIdx: Int,
        simpMsgTpl: SimpMessagingTemplate,
        stompCliReporter: StompCliReporter,
    ) =
        with(cliktCmd) {
            StompCliServiceServerImpl(endpoints[endpointIdx], simpMsgTpl, stompCliReporter)
        }

    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
        }
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry.addDecoratorFactory {
            ConnectionEventsWebSocketHandlerDecorator(it, appEventsPublisher)
        }
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.setOrder(-1)
        registry.addEndpoint("/")
            .setAllowedOrigins("*")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(SuppressRelayFromClientsChannelInterceptor())
    }
}

@SpringBootApplication
@EnableScheduling
@Profile("clients")
class StompCliClientsContext {

    companion object {
        fun run(parent: ConfigurableApplicationContext, httpPort: Int?) = SpringApplicationBuilder()
            .parent(parent)
            .sources(StompCliClientsContext::class.java)
            .profiles("clients")
            .bannerMode(Mode.OFF)
            .apply {
                if (httpPort != null) {
                    web(WebApplicationType.SERVLET)
                    profiles("clientHttpInterface")
                    properties(mapOf("server.port" to httpPort))
                }
                else {
                    web(WebApplicationType.NONE)
                }
            }
            .run()
    }

    @Bean
    fun stompCliService(stompClient: WebSocketStompClient, reporter: StompCliReporter, taskScheduler: TaskScheduler) = with(cliktCmd) {
        StompCliServiceClientsImpl(endpoints.clients(), stompClient, taskScheduler, reporter)
    }

    @Bean
    fun webSocketStompClient(taskScheduler: TaskScheduler) =
        WebSocketStompClient(StandardWebSocketClient()).apply {
            messageConverter = SimpleStringMessageConverter()
            this.taskScheduler = taskScheduler
        }
}
