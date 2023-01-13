package siarhei.luskanau.managed.virtual.device

import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import org.junit.Assert.assertEquals

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleFailedInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("siarhei.luskanau.managed.virtual.device.failed", appContext.packageName)
    }
}
