package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ZoneData

class FlickPadOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingBadgeView: View? = null
    private var floatingCursorView: View? = null
    private var cursorPointerView: CursorPointerView? = null
    private var floatingHudView: View? = null

    private var isHudExpanded = false
    private var isCursorMode = false

    private val zoneCardContainers = mutableMapOf<Int, FrameLayout>()
    private val zoneKeyLabels = mutableMapOf<Pair<Int, Int>, TextView>()
    private var hudTitleView: TextView? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        var isRunning = false
            private set

        private var activeInstance: FlickPadOverlayService? = null

        fun start(context: Context) {
            try {
                if (!Settings.canDrawOverlays(context)) return
                val intent = Intent(context, FlickPadOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FlickPadOverlayService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }

        fun updateCursorPosition(x: Float, y: Float) {
            activeInstance?.mainHandler?.post {
                activeInstance?.setCursorPosition(x, y)
            }
        }

        fun updateCursorClickState(isLeftDown: Boolean, isRightDown: Boolean, isDragLocked: Boolean) {
            activeInstance?.mainHandler?.post {
                activeInstance?.setCursorClickVisual(isLeftDown, isRightDown, isDragLocked)
            }
        }

        fun setCursorVisible(visible: Boolean) {
            activeInstance?.mainHandler?.post {
                activeInstance?.setCursorVisibility(visible)
            }
        }

        fun setCursorVisibility(visible: Boolean) {
            setCursorVisible(visible)
        }

        fun setOverlayHudVisible(visible: Boolean) {
            activeInstance?.mainHandler?.post {
                activeInstance?.setHudVisibility(visible)
            }
        }

        fun setHudVisibility(visible: Boolean) {
            setOverlayHudVisible(visible)
        }

        fun updateHudData(zones: List<ZoneData>, activeZone: Int, armedSector: Int?, layoutName: String) {
            activeInstance?.mainHandler?.post {
                activeInstance?.updateHudContent(zones, activeZone, armedSector, layoutName)
            }
        }

        fun injectText(context: Context, text: String) {
            FlickPadInputMethodService.commitText(text)
        }

        fun injectBackspace() {
            FlickPadInputMethodService.deleteBackspace()
        }

        fun injectEnter() {
            FlickPadInputMethodService.sendEnter()
        }

        fun handleExternalMotionEvent(event: MotionEvent): Boolean {
            return com.example.ui.FlickPadViewModel.activeInstance?.onGenericMotionEventExternal(event) ?: false
        }

        fun handleExternalKeyEvent(keyCode: Int, isDown: Boolean): Boolean {
            val vm = com.example.ui.FlickPadViewModel.activeInstance ?: return false
            return if (isDown) vm.onGamepadKeyDown(keyCode) else vm.onGamepadKeyUp(keyCode)
        }

        fun showFloatingOverlayOnInput() {
            activeInstance?.mainHandler?.post {
                val service = activeInstance
                service?.setHudVisibility(true)
                val vm = com.example.ui.FlickPadViewModel.activeInstance
                if (vm != null) {
                    vm.setMode("TEXT_INPUT")
                    vm.setOverlayVisible(true)
                }
            }
        }

        fun showModeToast(modeText: String) {
            activeInstance?.mainHandler?.post {
                activeInstance?.displayBriefModeToast(modeText)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        activeInstance = this
        try {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    1001,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(1001, notification)
            }
        } catch (_: Exception) {}

        try {
            if (Settings.canDrawOverlays(this)) {
                windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                initFloatingBadge()
                initFloatingCursor()
                initFloatingHud()
                val currentMode = com.example.ui.FlickPadViewModel.activeInstance?.activeMode?.value
                if (currentMode == "TEXT_INPUT") {
                    setHudVisibility(true)
                } else if (currentMode == "CURSOR_CONTROL") {
                    setCursorVisibility(true)
                }
            }
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "flickpad_channel",
                "FlickPad Background Gaming Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Gamepad radial keyboard, in-game text injection, and full-screen mouse cursor"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "flickpad_channel")
            .setContentTitle("FlickPad In-Game System Active")
            .setContentText("Radial keyboard & cursor ready for games. Tap to open.")
            .setSmallIcon(R.drawable.ic_flickpad_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun getOverlayLayoutParams(width: Int, height: Int, xPos: Int, yPos: Int, flags: Int): WindowManager.LayoutParams {
        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            width,
            height,
            layoutParamsType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xPos
            y = yPos
        }
    }

    private fun initFloatingBadge() {
        val wm = windowManager ?: return

        val params = getOverlayLayoutParams(
            width = WindowManager.LayoutParams.WRAP_CONTENT,
            height = WindowManager.LayoutParams.WRAP_CONTENT,
            xPos = 40,
            yPos = 140,
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        val frame = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
        }

        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.ic_flickpad_logo)
            layoutParams = FrameLayout.LayoutParams(110, 110).apply {
                gravity = Gravity.CENTER
            }
        }
        frame.addView(logoImage)

        frame.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            wm.updateViewLayout(frame, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            toggleFloatingHud()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingBadgeView = frame
        try {
            wm.addView(floatingBadgeView, params)
        } catch (_: Exception) {}
    }

    private var modeToastView: View? = null
    private val hideToastRunnable = Runnable {
        modeToastView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
            modeToastView = null
        }
    }

    fun displayBriefModeToast(modeText: String) {
        val wm = windowManager ?: (getSystemService(Context.WINDOW_SERVICE) as? WindowManager) ?: return
        mainHandler.removeCallbacks(hideToastRunnable)
        modeToastView?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            modeToastView = null
        }

        val toastParams = getOverlayLayoutParams(
            width = WindowManager.LayoutParams.WRAP_CONTENT,
            height = WindowManager.LayoutParams.WRAP_CONTENT,
            xPos = 0,
            yPos = 120,
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val textView = TextView(this).apply {
            text = modeText
            setTextColor(0xFF00FF66.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            paint.isFakeBoldText = true
            setPadding(28, 12, 28, 12)
            background = GradientDrawable().apply {
                setColor(0xFF000000.toInt())
                setStroke(3, 0xFF00FF66.toInt())
                cornerRadius = 16f
            }
        }

        modeToastView = textView
        try {
            wm.addView(textView, toastParams)
            mainHandler.postDelayed(hideToastRunnable, 1200)
        } catch (_: Exception) {}
    }

    /**
     * Precision Arrow Cursor View for Full-Screen System Cursor Mode.
     * Changes color on face button hold (Left Mouse Button = Cyan, Right Mouse Button = Amber, Drag Lock = Green).
     */
    class CursorPointerView(context: Context) : View(context) {
        private val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, 32f)
            lineTo(9f, 23f)
            lineTo(16f, 35f)
            lineTo(22f, 32f)
            lineTo(15f, 20f)
            lineTo(25f, 19f)
            close()
        }

        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            color = Color.BLACK
        }

        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        fun setCursorState(isLeftDown: Boolean, isRightDown: Boolean, isDragLocked: Boolean) {
            fillPaint.color = when {
                isLeftDown -> 0xFF00E5FF.toInt() // Cyan for Left Click / Hold
                isRightDown -> 0xFFFFB300.toInt() // Amber for Right Click
                isDragLocked -> 0xFF00FF66.toInt() // Neon Green for Drag Lock
                else -> Color.WHITE
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawPath(path, strokePaint)
            canvas.drawPath(path, fillPaint)
        }
    }

    private fun initFloatingCursor() {
        val wm = windowManager ?: return

        val cursorSize = 42
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        val params = getOverlayLayoutParams(
            width = cursorSize,
            height = cursorSize,
            xPos = 300,
            yPos = 500,
            flags = flags
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val pointer = CursorPointerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(cursorSize, cursorSize)
        }
        cursorPointerView = pointer
        floatingCursorView = pointer
        pointer.visibility = View.GONE

        try {
            wm.addView(floatingCursorView, params)
        } catch (_: Exception) {}
    }

    fun setCursorPosition(x: Float, y: Float) {
        val view = floatingCursorView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        params.x = x.toInt()
        params.y = y.toInt()
        try {
            windowManager?.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    fun setCursorClickVisual(isLeftDown: Boolean, isRightDown: Boolean, isDragLocked: Boolean) {
        cursorPointerView?.setCursorState(isLeftDown, isRightDown, isDragLocked)
    }

    fun setCursorVisibility(visible: Boolean) {
        isCursorMode = visible
        floatingCursorView?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Floating Overlay HUD: High-density 3x3 FlickBoard Grid Window.
     * Sized near screen width in portrait, and matching height in landscape.
     */
    private fun initFloatingHud() {
        val wm = windowManager ?: return
        if (floatingHudView != null) return

        val displayMetrics = resources.displayMetrics
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val hudWidth = if (isLandscape) {
            (displayMetrics.heightPixels * 0.88f).toInt().coerceAtLeast(320)
        } else {
            (displayMetrics.widthPixels * 0.96f).toInt().coerceAtLeast(320)
        }

        val hudParams = getOverlayLayoutParams(
            width = hudWidth,
            height = WindowManager.LayoutParams.WRAP_CONTENT,
            xPos = ((displayMetrics.widthPixels - hudWidth) / 2).coerceAtLeast(0),
            yPos = if (isLandscape) 40 else (displayMetrics.heightPixels - 720).coerceAtLeast(80),
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF000000.toInt())
            setPadding(6, 6, 6, 6)
            background = GradientDrawable().apply {
                setColor(0xFF000000.toInt())
                setStroke(2, 0xFFFFB300.toInt())
                cornerRadius = 16f
            }
        }

        // Header Bar (Draggable)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(10, 6, 10, 6)
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "⚡ FLICKPAD [FLOATING OVERLAY]"
            setTextColor(0xFF00FF66.toInt())
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            paint.isFakeBoldText = true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        hudTitleView = title
        header.addView(title)

        val closeBtn = TextView(this).apply {
            text = "[✕ MINIMIZE]"
            setTextColor(0xFFFFB300.toInt())
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(10, 4, 10, 4)
            setOnClickListener {
                setHudVisibility(false)
            }
        }
        header.addView(closeBtn)

        header.setOnTouchListener(object : View.OnTouchListener {
            private var initX = 0
            private var initY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = hudParams.x
                        initY = hudParams.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()
                        hudParams.x = initX + dx
                        hudParams.y = initY + dy
                        try {
                            wm.updateViewLayout(root, hudParams)
                        } catch (_: Exception) {}
                        return true
                    }
                }
                return false
            }
        })
        root.addView(header)

        // 3x3 Zones Grid:
        // Row 0: Up-Left(8), Up(1), Up-Right(2)
        // Row 1: Left(7), Center(0), Right(3)
        // Row 2: Down-Left(6), Down(5), Down-Right(4)
        val gridOrder = listOf(
            listOf(8, 1, 2),
            listOf(7, 0, 3),
            listOf(6, 5, 4)
        )

        val gridContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(2, 2, 2, 2)
        }

        for (rowZones in gridOrder) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            for (zoneIdx in rowZones) {
                val zoneCard = createMiniZoneCardView(zoneIdx)
                zoneCardContainers[zoneIdx] = zoneCard
                val cardParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(2, 2, 2, 2)
                }
                rowLayout.addView(zoneCard, cardParams)
            }
            gridContainer.addView(rowLayout)
        }
        root.addView(gridContainer)

        floatingHudView = root
        root.visibility = View.GONE

        try {
            wm.addView(root, hudParams)
            com.example.ui.FlickPadViewModel.activeInstance?.requestOverlayHudSync()
        } catch (_: Exception) {}
    }

    private fun createMiniZoneCardView(zoneIdx: Int): FrameLayout {
        val card = FrameLayout(this).apply {
            setPadding(2, 2, 2, 2)
            background = GradientDrawable().apply {
                setColor(0xFF05080F.toInt())
                setStroke(1, 0xFF443300.toInt())
                cornerRadius = 6f
            }
        }

        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Inner 3x3 symbol squares for sector 0..7
        // Row 0: 0, 1, 2
        // Row 1: 3, (center empty), 4
        // Row 2: 5, 6, 7
        val sectorRows = listOf(
            listOf(0, 1, 2),
            listOf(3, -1, 4),
            listOf(5, 6, 7)
        )

        for (rowSectors in sectorRows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            for (sectorIdx in rowSectors) {
                val tv = TextView(this).apply {
                    textSize = 10f
                    gravity = Gravity.CENTER
                    typeface = android.graphics.Typeface.MONOSPACE
                    setPadding(1, 2, 1, 2)
                    setTextColor(0xFFFFB300.toInt())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(1, 1, 1, 1)
                    }
                }
                if (sectorIdx >= 0) {
                    zoneKeyLabels[Pair(zoneIdx, sectorIdx)] = tv
                } else {
                    tv.text = ""
                }
                rowLayout.addView(tv)
            }
            innerLayout.addView(rowLayout)
        }

        card.addView(innerLayout)
        return card
    }

    fun updateHudContent(
        zones: List<ZoneData>,
        activeZone: Int,
        armedSector: Int?,
        layoutName: String
    ) {
        hudTitleView?.text = "⚡ FLICKPAD [$layoutName]"
        for (z in 0..8) {
            val card = zoneCardContainers[z] ?: continue
            val isActive = (z == activeZone)
            card.background = GradientDrawable().apply {
                setColor(if (isActive) 0x3300FF66.toInt() else 0xFF05080F.toInt())
                setStroke(if (isActive) 3 else 1, if (isActive) 0xFF00FF66.toInt() else 0xFF443300.toInt())
                cornerRadius = 6f
            }
        }

        for (zone in zones) {
            val zIdx = zone.index
            for (sIdx in 0..7) {
                val tv = zoneKeyLabels[Pair(zIdx, sIdx)] ?: continue
                val sym = zone.symbols.getOrNull(sIdx) ?: ""
                tv.text = sym
                val isArmed = (zIdx == activeZone && armedSector == sIdx)
                if (isArmed) {
                    tv.setTextColor(0xFF00FF66.toInt())
                    tv.paint.isFakeBoldText = true
                    tv.setBackgroundColor(0x5500FF66.toInt())
                } else {
                    tv.setTextColor(0xFFFFB300.toInt())
                    tv.paint.isFakeBoldText = false
                    tv.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    fun setHudVisibility(visible: Boolean) {
        isHudExpanded = visible
        if (visible && floatingHudView == null) {
            initFloatingHud()
        }
        floatingHudView?.visibility = if (visible) View.VISIBLE else View.GONE
        floatingBadgeView?.visibility = if (visible) View.GONE else View.VISIBLE
    }

    private fun toggleFloatingHud() {
        val hud = floatingHudView
        if (hud == null) {
            initFloatingHud()
            floatingHudView?.visibility = View.VISIBLE
            isHudExpanded = true
            floatingBadgeView?.visibility = View.GONE
        } else {
            val willBeVisible = (hud.visibility != View.VISIBLE)
            setHudVisibility(willBeVisible)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (activeInstance == this) {
            activeInstance = null
        }
        mainHandler.removeCallbacks(hideToastRunnable)
        modeToastView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        floatingBadgeView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        floatingCursorView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        floatingHudView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
    }
}
