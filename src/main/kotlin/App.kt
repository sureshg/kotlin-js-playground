import com.russhwolf.settings.*
import hljs.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.rsocket.kotlin.*
import io.rsocket.kotlin.core.*
import io.rsocket.kotlin.keepalive.*
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.client.*
import jetbrains.letsPlot.*
import jetbrains.letsPlot.frontend.*
import jetbrains.letsPlot.geom.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
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
import space.kscience.plotly.*
import space.kscience.plotly.models.*
import xterm.*
import kotlin.collections.set
import kotlin.math.*
import kotlin.random.*
import kotlin.time.*

@OptIn(DelicateCoroutinesApi::class)
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
                        connector = RSocketConnector {
                            // configure rSocket connector
                            connectionConfig {
                                keepAlive =
                                    KeepAlive(
                                        interval = Duration.seconds(30),
                                        maxLifetime = Duration.minutes(2)
                                    )
                                payloadMimeType = PayloadMimeType(
                                    data = "application/json",
                                    metadata = "application/json"
                                )
                                // payload for setup frame
                                setupPayload { buildPayload { data("Hello Kotlin!") } }
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

                            reconnectable { t, attempt ->
                                println("Reconnecting $attempt time due to ${t.message}")
                                true
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

                    pre {
                        attributes["max-height"] = "25em"
                        attributes["overflow"] = "auto"
                        attributes["height"] = "auto"

                        code {
                            classes = setOf("language-kotlin", "hljs")
                            attributes["font-family"] = "'JetBrains Mono', monospace"
                            attributes["tab-size"] = "2"
                            attributes["font-size"] = "10pt"
                            attributes["font-family"] = "'JetBrains Mono', monospace"
                            +"""
                             fun main() {
                                val langs = listOf("Java","Kotlin","Scala","Clojure","Groovy")
                                langs.forEach {
                                    println(it)
                                } 
                             }            
                            """.trimIndent()
                        }
                    }
                }

                println("Enabling Kotlin Playground!")
                KotlinPlayground(".kotlin-code")

                codeHighlight()
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
                        Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .toString()
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

            letsPlott()
            plotly()

            val epoch = js("Date.now()") as Double
            println("Epoch using JS Date: $epoch")
            List(5) { println("Kotlin/JS-$it: ${Clock.System.now()}") }

            // jsTypeOf()
        }
}


fun plotly() {
    val contentDiv = document.getElementById("plotly") as HTMLElement
    contentDiv.append {
        div {
            style = "height:50%; width=100%;"
            h1 { +"Histogram demo" }
            plot {
                val rnd = Random(222)
                histogram {
                    name = "Random data"
                    GlobalScope.launch {
                        while (isActive) {
                            x.numbers = List(500) { rnd.nextDouble() }
                            delay(300)
                        }
                    }
                }

                layout {
                    bargap = 0.1
                    title {
                        text = "Basic Histogram"
                        font {
                            size = 20
                            color("black")
                        }
                    }
                    xaxis {
                        title {
                            text = "Value"
                            font {
                                size = 16
                            }
                        }
                    }
                    yaxis {
                        title {
                            text = "Count"
                            font {
                                size = 16
                            }
                        }
                    }
                }
            }
        }

        div {
            style = "height:50%; width=100%;"
            h1 { +"Dynamic trace demo" }
            plot {
                scatter {
                    x(1, 2, 3, 4)
                    y(10, 15, 13, 17)
                    mode = ScatterMode.markers
                    type = TraceType.scatter
                }
                scatter {
                    x(2, 3, 4, 5)
                    y(10, 15, 13, 17)
                    mode = ScatterMode.lines
                    type = TraceType.scatter

                    GlobalScope.launch {
                        while (isActive) {
                            delay(500)
                            marker {
                                if (Random.nextBoolean()) {
                                    color("magenta")
                                } else {
                                    color("blue")
                                }
                            }
                        }
                    }
                }
                scatter {
                    x(1, 2, 3, 4)
                    y(12, 5, 2, 12)
                    mode = ScatterMode.`lines+markers`
                    type = TraceType.scatter
                    marker {
                        color("red")
                    }
                }
                layout {
                    title = "Line and Scatter Plot"
                }
            }

        }
    }
}

fun letsPlott() {
    val contentDiv = document.getElementById("lets-plot")
    val n = 100
    val data = mapOf(
        "x" to List(n) { nextGaussian() }
    )

    val p = letsPlot(data) + geomDensity(
        color = "dark-green",
        fill = "green",
        alpha = .3,
        size = 2.0
    ) { x = "x" }

    contentDiv?.appendChild(JsFrontendUtil.createPlotDiv(p))
}

fun nextGaussian(): Double {
    var u = 0.0
    var v = 0.0
    while (u < 1.0e-7) u = Random.nextDouble()
    while (v < 1.0e-7) v = Random.nextDouble()
    return sqrt(-2.0 * ln(u)) * cos(2.0 * PI * v)
}

fun log(text: Any) {
    val log = document.getElementById("log") as? HTMLTextAreaElement
    log?.appendText(text.toString())
}

@OptIn(InternalCoroutinesApi::class)
suspend fun rSocketTest(client: HttpClient) {
    try {
        log("\n\nRSocket Test\n")
        log("------------\n")
        val demoUrl = "wss://demo.rsocket.io/rsocket"
        println("Connecting to RSocket server at $demoUrl")
        val rSocket = client.rSocket(urlString = demoUrl)
        val stream = rSocket.requestStream(Payload.Empty)
        stream.take(11).collect { log("${it.data.readText()} ") }
    } catch (t: Throwable) {
        t.printStackTrace()
        log("Error : ${t.message}")
    }
}

fun HTMLElement.highlightCode(code: String) {
    innerText = code
    HighlightJs.highlightElement(this)
}

fun codeHighlight() {
    println("Enabling code Highlighting: ${HighlightJs.versionString} ")
    HighlightJs.highlightAll()
}

fun yamlTest() {
    log("\nYaml Test\n")
    log("---------\n")
    val model = Yaml.Default.decodeFromString<Model>(
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
    val list: List<String>,
    @Redacted
    val password: String = "password"
) {
    @Serializable
    data class Nested(val numberCast: Int)
}
