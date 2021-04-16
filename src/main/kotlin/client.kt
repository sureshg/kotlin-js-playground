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

  window.onload = { document.body?.sayHello() }
  GlobalScope.launch {
    val mark = TimeSource.Monotonic.markNow()
    val client = HttpClient()
    val resp = client.get<String>("https://httpbin.org/get")
    val took = mark.elapsedNow()

    document.body?.append {
      div {
        style = "white-space: pre-line; background: #DDDDDD"
        +"""
         $resp
         ${took.toDouble(DurationUnit.MILLISECONDS)} ms
         """.trimIndent()
      }
    }
  }

  List(10) { console.log("Hello KotlinJS-$it: ${Clock.System.now()}") }
}

fun Node.sayHello() {
  append { div { +"Hello KotlinJS!" } }
}
