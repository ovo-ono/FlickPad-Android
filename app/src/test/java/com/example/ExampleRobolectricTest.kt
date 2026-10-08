package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.RadialEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FlickPad", appName)
  }

  @Test
  fun `radial engine neutral zone check`() {
    // Within deadzone
    val zone = RadialEngine.getZoneFromStick(0.05f, 0.05f, 0.22f)
    assertEquals(0, zone) // Neutral Zone
    val sector = RadialEngine.getSectorFromStick(0.05f, 0.05f, 0.22f)
    assertNull(sector)
  }

  @Test
  fun `radial engine directional zones check`() {
    // Up / North (y = -1.0)
    val upZone = RadialEngine.getZoneFromStick(0f, -0.8f, 0.22f)
    assertEquals(1, upZone) // UP

    // Right / East (x = +1.0)
    val rightZone = RadialEngine.getZoneFromStick(0.8f, 0f, 0.22f)
    assertEquals(3, rightZone) // RIGHT

    // Down / South (y = +1.0)
    val downZone = RadialEngine.getZoneFromStick(0f, 0.8f, 0.22f)
    assertEquals(5, downZone) // DOWN

    // Left / West (x = -1.0)
    val leftZone = RadialEngine.getZoneFromStick(-0.8f, 0f, 0.22f)
    assertEquals(7, leftZone) // LEFT
  }
}
