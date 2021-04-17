import kotlinx.browser.*
import kotlin.test.*

class TestClient {

  @Test
  fun testSayHello() {
    val container = document.createElement("div")
    container.sayHello()
    assertEquals("Hello Kotlin/JS!", container.textContent)
  }
}
