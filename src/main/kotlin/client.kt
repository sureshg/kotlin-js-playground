import com.russhwolf.settings.*
import io.ktor.client.*
import io.ktor.client.request.*
import kotlin.time.*
import kotlinx.browser.*
import kotlinx.coroutines.*
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
          val mark = TimeSource.Monotonic.markNow()
          val client = HttpClient()
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
