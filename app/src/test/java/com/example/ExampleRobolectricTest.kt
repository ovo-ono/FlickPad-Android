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

  @Test
  fun `radial engine right stick 8-direction sector mapping check`() {
    val deadzone = 0.20f

    // Sector 0: Up-Left (NW)
    assertEquals(0, RadialEngine.getSectorFromStick(-0.7f, -0.7f, deadzone))
    assertEquals("NW", RadialEngine.getSectorDirectionLabel(0))

    // Sector 1: Up (N)
    assertEquals(1, RadialEngine.getSectorFromStick(0f, -0.8f, deadzone))
    assertEquals("N", RadialEngine.getSectorDirectionLabel(1))

    // Sector 2: Up-Right (NE)
    assertEquals(2, RadialEngine.getSectorFromStick(0.7f, -0.7f, deadzone))
    assertEquals("NE", RadialEngine.getSectorDirectionLabel(2))

    // Sector 3: Left (W)
    assertEquals(3, RadialEngine.getSectorFromStick(-0.8f, 0f, deadzone))
    assertEquals("W", RadialEngine.getSectorDirectionLabel(3))

    // Sector 4: Right (E)
    assertEquals(4, RadialEngine.getSectorFromStick(0.8f, 0f, deadzone))
    assertEquals("E", RadialEngine.getSectorDirectionLabel(4))

    // Sector 5: Down-Left (SW)
    assertEquals(5, RadialEngine.getSectorFromStick(-0.7f, 0.7f, deadzone))
    assertEquals("SW", RadialEngine.getSectorDirectionLabel(5))

    // Sector 6: Down (S)
    assertEquals(6, RadialEngine.getSectorFromStick(0f, 0.8f, deadzone))
    assertEquals("S", RadialEngine.getSectorDirectionLabel(6))

    // Sector 7: Down-Right (SE)
    assertEquals(7, RadialEngine.getSectorFromStick(0.7f, 0.7f, deadzone))
    assertEquals("SE", RadialEngine.getSectorDirectionLabel(7))
  }

  @Test
  fun `custom default profile is default and cannot be deleted`() {
    val initialProfiles = com.example.data.DefaultProfiles.createInitialProfiles()
    val defaultProfile = initialProfiles.firstOrNull { it.isDefault }
    org.junit.Assert.assertNotNull(defaultProfile)
    assertEquals("Custom Default", defaultProfile?.name)
  }

  @Test
  fun `profile json serialization preserves layouts and stick configurations`() {
    val initialProfiles = com.example.data.DefaultProfiles.createInitialProfiles()
    val customDefault = initialProfiles.first { it.name == "Custom Default" }
    
    val repository = com.example.data.ProfileRepository(object : com.example.data.ProfileDao {
      override fun getAllProfiles(): kotlinx.coroutines.flow.Flow<List<com.example.data.ProfileEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
      override suspend fun getProfileById(id: Long) = null
      override suspend fun getDefaultProfile() = null
      override suspend fun insert(profile: com.example.data.ProfileEntity) = 1L
      override suspend fun update(profile: com.example.data.ProfileEntity) {}
      override suspend fun delete(profile: com.example.data.ProfileEntity) {}
      override suspend fun clearDefaultFlags() {}
      override suspend fun setDefaultProfile(id: Long) {}
    })

    val exportedJson = repository.exportProfileToJson(customDefault)
    org.junit.Assert.assertTrue(exportedJson.contains("Custom Default"))
    org.junit.Assert.assertTrue(exportedJson.contains("layouts"))
    org.junit.Assert.assertTrue(exportedJson.contains("settings"))

    val imported = repository.importProfileFromJson(exportedJson)
    assertEquals("Custom Default", imported.name)
    assertEquals(customDefault.leftDeadzone, imported.leftDeadzone, 0.001f)
    assertEquals(customDefault.mouseSensitivity, imported.mouseSensitivity, 0.001f)
  }

  @Test
  fun `mode switch toast tokens format correctly`() {
    val modes = listOf("OFF", "TEXT_INPUT", "CURSOR_CONTROL")
    val expectedTokens = listOf("[off]", "[text_input]", "[cursor]")
    val actualTokens = modes.map { mode ->
      when (mode) {
        "OFF" -> "[off]"
        "TEXT_INPUT" -> "[text_input]"
        "CURSOR_CONTROL" -> "[cursor]"
        else -> "[${mode.lowercase()}]"
      }
    }
    assertEquals(expectedTokens, actualTokens)
  }

  @Test
  fun `cursor mappings map face buttons to mouse click actions`() {
    val cursorJson = com.example.data.DefaultProfiles.getDefaultCursorMappingsJson()
    val mappings = com.example.data.DefaultProfiles.jsonToMappings(cursorJson)
    
    // Face button A acts as Left Click
    assertEquals("MOUSE_LEFT_CLICK", mappings["KEYCODE_BUTTON_A"])
    // Face button B acts as Right Click
    assertEquals("MOUSE_RIGHT_CLICK", mappings["KEYCODE_BUTTON_B"])
    // Face button X acts as Middle Click
    assertEquals("MOUSE_MIDDLE_CLICK", mappings["KEYCODE_BUTTON_X"])
    // Face button Y acts as Double Click
    assertEquals("MOUSE_DOUBLE_CLICK", mappings["KEYCODE_BUTTON_Y"])
    // Trigger L2 acts as Drag Lock
    assertEquals("MOUSE_DRAG_LOCK", mappings["KEYCODE_BUTTON_L2"])
  }
}

