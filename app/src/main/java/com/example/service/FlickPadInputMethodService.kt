package com.example.service

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen

class FlickPadInputMethodService : InputMethodService() {

    companion object {
        var activeInstance: FlickPadInputMethodService? = null
            private set

        val isConnected: Boolean
            get() = activeInstance != null

        fun commitText(text: String): Boolean {
            val ic = activeInstance?.currentInputConnection ?: return false
            return ic.commitText(text, 1)
        }

        fun deleteBackspace(): Boolean {
            val ic = activeInstance?.currentInputConnection ?: return false
            val selected = ic.getSelectedText(0)
            return if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
        }

        fun sendEnter(): Boolean {
            val instance = activeInstance ?: return false
            return try {
                instance.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                true
            } catch (_: Exception) {
                false
            }
        }

        fun sendKey(keyCode: Int): Boolean {
            val instance = activeInstance ?: return false
            return try {
                instance.sendDownUpKeyEvents(keyCode)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        activeInstance = this
        // If overlay is enabled and user selects a text input field, summon Floating Overlay
        val vm = com.example.ui.FlickPadViewModel.activeInstance
        val overlayEnabled = vm?.overlayVisible?.value ?: true
        if (overlayEnabled) {
            try {
                if (android.provider.Settings.canDrawOverlays(this)) {
                    FlickPadOverlayService.start(this)
                }
                FlickPadOverlayService.showFloatingOverlayOnInput()
            } catch (_: Exception) {}
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    override fun onCreateInputView(): View {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(0xFF05080F.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 20, 32, 20)
        }

        val titleView = android.widget.TextView(this).apply {
            text = "⚡ FLICKPAD INPUT METHOD [FLOATING OVERLAY & GAMEPAD ACTIVE]"
            setTextColor(0xFF00FF66.toInt())
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            paint.isFakeBoldText = true
        }

        val subView = android.widget.TextView(this).apply {
            text = "Use left/right gamepad sticks to radial-type • Floating overlay visible"
            setTextColor(0xFFFFB300.toInt())
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, 6, 0, 0)
        }

        container.addView(titleView)
        container.addView(subView)
        return container
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (FlickPadOverlayService.handleExternalMotionEvent(event)) {
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (FlickPadOverlayService.handleExternalKeyEvent(keyCode, isDown = true)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (FlickPadOverlayService.handleExternalKeyEvent(keyCode, isDown = false)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
