import com.russhwolf.settings.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.rsocket.kotlin.*
import io.rsocket.kotlin.core.*
import io.rsocket.kotlin.keepalive.*
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.client.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.internal.JSJoda.*
import kotlinx.dom.*
import kotlinx.html.*
import kotlinx.html.dom.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.mamoe.yamlkt.*
import org.intellij.markdown.flavours.gfm.*
import org.intellij.markdown.html.*
import org.intellij.markdown.parser.*
import org.w3c.dom.*
import xterm.*
import kotlin.collections.set
import kotlin.time.*
import kotlin.time.Duration

fun main() {
    val format = Json {
        prettyPrint = true
        isLenient = true
    }

    window.onload =
        {
            val root = document.getElementById("root") as? HTMLDivElement
            root?.sayHello()

            val settings = JsSettings()
            if (settings.getStringOrNull("name").isNullOrBlank()) {
                settings["name"] = window.navigator.userAgent
            }

            GlobalScope.launch {
                val client = HttpClient {
                    install(WebSockets)
                    install(RSocketSupport) {
                        connector =
                            RSocketConnector {
                                // configure rSocket connector
                                connectionConfig {
                                    keepAlive =
                                        KeepAlive(
                                            interval = Duration.seconds(30),
                                            maxLifetime = Duration.minutes(2)
                                        )
                                    payloadMimeType =
                                        PayloadMimeType(
                                            data = "application/json",
                                            metadata = "application/json"
                                        )
                                }

                                // Interceptors
                                interceptors {
                                    forConnection { conn ->
                                        println("Accepting new RSocket connection!")
                                        conn
                                    }
                                }

                                // optional acceptor for server requests
                                acceptor {
                                    RSocketRequestHandler {
                                        requestResponse { it } // echo request payload
                                    }
                                }
                            }
                    }
                }

                val mark = TimeSource.Monotonic.markNow()
                val resp = client.get<String>("https://httpbin.org/get")
                val took = mark.elapsedNow()

                root?.append {
                    div {
                        style = "white-space: pre-line; background: #DDDDDD"

                        +"""
               $resp
               ${settings.getString("name", "n/a")}
               ${took.toDouble(DurationUnit.MILLISECONDS)} ms
                        """.trimIndent()
                    }

                    div {
                        id = "time"
                        style = "background: cyan"
                    }

                    div {
                        classes = setOf("kotlin-code")
                        attributes["theme"] = "darcula"
                        attributes["folded-button"] = "true"

                        +"""
                           fun main() {
                             val langs = listOf("Java","Kotlin","Scala","Clojure","Groovy")
                             langs.forEach {
                                println(it)
                             } 
                            }            
                        """.trimIndent()
                    }

                    textArea {
                        id = "log"
                        rows = "20"
                        cols = "70"
                    }
                }

                println("Enabling Kotlin Playground!")
                KotlinPlayground(".kotlin-code")

                yamlTest()
                markdownTest()
                parserTest()
                rSocketTest(client)
            }

            // Schedule on window.asCoroutineDispatcher()
            CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    val time = document.getElementById("time")
                    time?.textContent =
                        ZonedDateTime.now(ZoneId.SYSTEM).toLocalDateTime().toString()
                    delay(1000)
                }
            }
            val term = Terminal(object : ITerminalOptions {
                init {
                    cursorBlink = true
                    cols = 100
                    rows = 100
                }
            })

            with(term) {
                open(document.getElementById("terminal") as HTMLDivElement)
                write("Hello from \u001B[1;3;31mKotlin/Js\u001B[0m \r\n\$ ")
                focus()
                onKey { kbEvt ->
                    val evt = kbEvt.domEvent
                    val printable = !evt.altKey && !evt.ctrlKey && !evt.metaKey
                    when {
                        evt.keyCode == 13 -> term.write("\r\n$ ")
                        // Do not delete the prompt
                        evt.keyCode == 8 -> if (term.buffer.active.cursorX.toInt() > 2) term.write("\b \b")
                        printable -> term.write(kbEvt.key)
                    }
                }
            }

            val epoch = js("Date.now()") as Double
            println("Epoch using JS Date: $epoch")
            List(5) { println("Kotlin/JS-$it: ${Clock.System.now()}") }

            // jsTypeOf()
            // js()
        }
}

fun log(text: Any) {
    val log = document.getElementById("log") as? HTMLTextAreaElement
    log?.appendText(text.toString())
}

suspend fun rSocketTest(client: HttpClient) {
    try {
        log("\nRSocket Test\n")
        log("------------\n")
        val demoUrl = "wss://demo.rsocket.io/rsocket"
        println("Connecting to RSocket server at $demoUrl")
        val rSocket = client.rSocket(urlString = demoUrl)
        val stream = rSocket.requestStream(Payload.Empty)
        stream.take(10).collect { log(it.data.readText()) }
    } catch (t: Throwable) {
        t.printStackTrace()
        log("Error : ${t.message}")
    }
}

fun yamlTest() {
    log("\nYaml Test\n")
    log("---------\n")
    val model =
        Yaml.Default.decodeFromString(
            Model.serializer(),
            """
            test: testString
            nest: 
              numberCast: 0xFE
            list: [str, "str2"]
            """
        )
    log("$model\n")
}

fun markdownTest() {
    try {
        log("\nMarkdown Test\n")
        log("-------------\n")
        val src = "Some *markdown*"
        val flavour = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val html = HtmlGenerator(src, parsedTree, flavour).generateHtml()
        log("Markdown : $html")
    } catch (t: Throwable) {
        t.printStackTrace()
        log("Error : ${t.message}\n")
    }
}

fun parserTest() {}

fun Node.sayHello() {
    append { div { +"Hello Kotlin/JS!" } }
}

@Serializable
data class Model(
    val test: String,
    val optional: String = "optional", // Having default value means optional
    val nest: Nested,
    val list: List<String>
) {
    @Serializable
    data class Nested(val numberCast: Int)
}
