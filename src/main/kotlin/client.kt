import kotlinx.browser.*
import kotlinx.html.*
import kotlinx.html.dom.*
import org.w3c.dom.*

fun main() {
    window.onload = { document.body?.sayHello() }
    document.write("Hello World", "1,2,3")
    List(10) {
        console.log("Hello $it")
    }
}

fun Node.sayHello() {
    append {
        div("dfd") {
            +"Hello from JS! "
        }
    }
}
