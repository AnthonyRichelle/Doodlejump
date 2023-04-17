package com.doodlejump

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.doodlejump.plateforms.BasePlatform
import com.doodlejump.plateforms.MovingPlateform
import com.doodlejump.plateforms.Platform
import kotlin.random.Random

class GameManager @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0): SurfaceView(context, attributes,defStyleAttr),
    SurfaceHolder.Callback, Runnable  {

    private var objects = arrayListOf<GameObject>()
    private var removeStack = arrayListOf<GameObject>()
    private var addStack = arrayListOf<GameObject>()
    private var drawing = true;
    private var totalElapsedTime = 0.0
    private var backgroundPaint = Paint()
    private var player = Player(Vector(520F, 0F))
    private var score = 0F
    private var scorePaint = Paint()
    private var density = 0.5F
    private val step = Platform.size.y + 100F
    private lateinit var canvas: Canvas
    private lateinit var thread: Thread

    companion object {
        const val TIME_CONSTANT = 0.5F
    }

    init {
        scorePaint.color = Color.RED
        scorePaint.textSize = 100F
        objects.add(player)
        objects.add(BasePlatform(Vector(500F, 300F)))
        objects.add(MovingPlateform(Vector(500F, 1000F)))
        backgroundPaint.color = Color.WHITE
    }

    private fun gameLoop() {
        objects.forEach { if(it is IUpdate) it.update(this) }
        removeStack.forEach { objects.remove(it) }
        addStack.forEach { objects.add(it) }
        player.checkCollisions(objects)
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawColor( 0, PorterDuff.Mode.CLEAR );
            canvas.drawText("${score.toInt()}", 100F, 100F, scorePaint)
            objects.forEach { it.draw(canvas, context) }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun run() {
        var previousFrameTime = System.currentTimeMillis()
        while (drawing) {
            val currentTime = System.currentTimeMillis()
            var elapsedTimeMS:Double=(currentTime-previousFrameTime).toDouble()
            gameLoop()
            totalElapsedTime += elapsedTimeMS / 1000.0
            previousFrameTime = currentTime
        }
    }

    fun onPause() {
        drawing = false
        thread.join()
    }

    fun onResume() {
        drawing = true
        thread = Thread(this)
        thread.start()
    }

    fun setXOrientation(deg: Float) {
        player.speed.x = deg / 10
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

    }

    fun moveObjects(amount: Float) {
        score += amount / 10
        objects.forEach {
            it.pos.y -= amount
            if(it.pos.y < 0) removeStack.add(it)
        }
        // Generation of the new plateforms
        for (i in 1..(density * amount).toInt()) addStack.add(BasePlatform(Vector(Random.nextFloat() * width, amount / i + height)))
    }



}