import io.ktor.client.*
import io.ktor.client.request.*
import kotlin.time.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
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
               ${took.toDouble(DurationUnit.MILLISECONDS)} ms
               """.trimIndent()
            }

            // iframe { src = "https://pl.kotl.in/yIx7pHtRa?theme=darcula" }

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

        val epoch = js("Date.now()") as Double
        println("Epoch: $epoch")
        List(5) { println("Kotlin/JS-$it: ${Clock.System.now()}") }
      }
}

fun Node.sayHello() {
  append { div { +"Hello Kotlin/JS!" } }
}

external fun KotlinPlayground(message: String)
