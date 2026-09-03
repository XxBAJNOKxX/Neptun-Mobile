package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.University
import com.example.presentation.ui.screens.LoginScreen
import com.example.presentation.viewmodel.AuthUiState
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen(
          uiState = AuthUiState(
            selectedUniversity = University(
              id = "bme",
              name = "Budapesti Műszaki és Gazdaságtudományi Egyetem",
              shortName = "BME",
              city = "Budapest",
              neptunUrl = "neptun.bme.hu"
            ),
            neptunCode = "NEP123"
          ),
          onSearchQueryChange = {},
          onSelectUniversity = {},
          onNeptunCodeChange = {},
          onPasswordChange = {},
          onLoginClick = {},
          onQuickDemoFill = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

