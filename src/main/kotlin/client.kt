import com.russhwolf.settings.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.client.*
import kotlin.collections.set
import kotlin.time.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.internal.JSJoda.*
import kotlinx.html.*
import kotlinx.html.dom.*
import kotlinx.serialization.json.*
import org.w3c.dom.*

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
            install(RSocketSupport)
          }

          val mark = TimeSource.Monotonic.markNow()
          val resp = client.get<String>("https://httpbin.org/get")
          val took = mark.elapsedNow()

          root?.append {
            div {
              style = "white-space: pre-line; background: #DDDDDD"

              +"""
               $resp
               ${settings.getString("name","n/a")}
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
          }
          println("Enabling Kotlin Playground!")
          KotlinPlayground(".kotlin-code")

          val rSocket = client.rSocket(urlString = "wss://rsocket-demo.herokuapp.com/rsocket")
          val stream = rSocket.requestStream(buildPayload { data("Hello") })
          stream.take(10).collect { println(it.data.readText()) }
        }

        // Schedule on window.asCoroutineDispatcher()
        CoroutineScope(Dispatchers.Default).launch {
          while (isActive) {
            val time = document.getElementById("time")
            time?.textContent = ZonedDateTime.now().toLocalDateTime().toString()
            delay(1000)
          }
        }

        // jsTypeOf()
        // js()

        val epoch = js("Date.now()") as Double
        println("Epoch using JS Date: $epoch")
        List(5) { println("Kotlin/JS-$it: ${Clock.System.now()}") }
      }
}

fun Node.sayHello() {
  append { div { +"Hello Kotlin/JS!" } }
}

external fun KotlinPlayground(message: String)
