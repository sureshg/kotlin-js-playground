#!/usr/bin/env kotlin

// For compilation
//  #!/usr/bin/env -S kotlinc-jvm -nowarn -script

@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://jcenter.bintray.com/")
@file:DependsOn("io.rsocket.kotlin:rsocket-core:0.12.0")
@file:DependsOn("io.rsocket.kotlin:rsocket-core-jvm:0.12.0")
@file:DependsOn("io.rsocket.kotlin:rsocket-transport-ktor:0.12.0")
@file:DependsOn("io.rsocket.kotlin:rsocket-transport-ktor-jvm:0.12.0")
@file:DependsOn("io.rsocket.kotlin:rsocket-transport-ktor-client:0.12.0")
@file:DependsOn("io.rsocket.kotlin:rsocket-transport-ktor-client-jvm:0.12.0")
@file:DependsOn("io.ktor:ktor-client-okhttp:1.5.4")
@file:CompilerOptions("-jvm-target", "1.8", "-Xopt-in=kotlin.RequiresOptIn")
@file:OptIn(ExperimentalTime::class)

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.rsocket.kotlin.*
import io.rsocket.kotlin.core.*
import io.rsocket.kotlin.keepalive.*
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

runBlocking {
    val client = HttpClient(OkHttp) {
        install(WebSockets)
        install(RSocketSupport) {
            connector = RSocketConnector {
                connectionConfig {
                    // setupPayload(setupPayload)
                    keepAlive = KeepAlive(Duration.seconds(5))
                    payloadMimeType = PayloadMimeType("application/json", "application/json")
                }
            }
        }
    }

    // connect to some url
    val demoUrl = "wss://demo.rsocket.io/rsocket"
    println("Connecting to $demoUrl")
    val rSocket: RSocket = client.rSocket(demoUrl)

    // request stream
    val stream: Flow<Payload> = rSocket.requestStream(Payload.Empty)

    // take 5 values and print response
    stream.take(5).collect { payload: Payload ->
        println(payload.data.readText())
    }
}
