package com.doodlejump

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.doodlejump.plateforms.*
import kotlin.math.floor
import kotlin.random.Random

class GameManager @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0): SurfaceView(context, attributes,defStyleAttr),
    SurfaceHolder.Callback, Runnable  {

    private var objects = arrayListOf<GameObject>()
    private var addStack = arrayListOf<GameObject>()
    private var drawing = true;
    private var totalElapsedTime = 0.0
    private var backgroundPaint = Paint()
    private var player = Player(Vector(400F, 10F))
    private var scorePaint = Paint()
    private var genStep = 2 * Platform.size.y
    private var genBuffer = 0F
    private lateinit var thread: Thread

    private var timeObservables = arrayListOf<TimeObservable>()

    var score = 0F
    lateinit var canvas: Canvas

    companion object {
        const val TIME_CONSTANT = 0.5F
        const val DENSITY = 0.5F
        const val SCORE_MULTIPLIER = 0.1F
        const val WIDTH = 1074f
        const val HEIGHT = 1584f
    }

    init {
        scorePaint.color = Color.BLACK
        scorePaint.textSize = 100F
        objects.add(BasePlatform(Vector(500F, 300F)))
        objects.add(OneUsePlatform(Vector(500F, 1100F)))
        objects.add(BasePlatform(Vector(500F, 1500F)))
        objects.add(FalsePlatform(Vector(200F, 1500F)))
        objects.add(MovingPlatform(Vector(500F, 800F)))
        backgroundPaint.color = Color.WHITE
        Log.d("", "${Player.JUMP_HEIGHT}")
    }

    private fun gameLoop() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawColor( 0, PorterDuff.Mode.CLEAR );
            canvas.drawText("${score.toInt()}", 100F, 100F, scorePaint)

            objects.forEach {
                if(it is IUpdate) it.update(this)
                it.draw(this)
            }
            objects.removeAll{ it.removed }
            player.update(this)
            player.draw(this)
            player.checkCollisions(objects)

            addStack.forEach { objects.add(it) }; addStack.clear()
            timeObservables.forEach { it.update() }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun registerTimeObservable(obs: TimeObservable) {
        timeObservables.add(obs)
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
        if(player.alive) player.speed.x = deg / 10
    }

    fun changeScore(amount: Float) {
        if(player.alive) score += amount * SCORE_MULTIPLIER
    }

    fun moveObjects(amount: Float) {
        // Generation of the new plateforms
        genBuffer += amount
        for (i in 1..floor(genBuffer * DENSITY / (genStep)).toInt()) {
            addStack.add(BasePlatform(Vector(Random.nextFloat() * WIDTH, genBuffer / i + HEIGHT)))
            genBuffer -= genStep
        }

        // Moving the player up
        changeScore(amount)
        objects.forEach {
            it.move(Vector(0F, -amount))
            if (it.pos.y < 0) it.removed = true
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

    }
}