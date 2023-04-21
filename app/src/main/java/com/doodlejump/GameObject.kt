package com.doodlejump

import android.graphics.*

abstract class GameObject(val size: Vector, var pos: Vector, var sprite: Int) {

    private var ressource: Bitmap? = null
    var hitbox = RectF(0F, 0F, 0F, 0F)

    init { move(Vector(0f, 0f)) }
    abstract fun whenHit(player: Player)
    open fun isHit(box: RectF): Boolean { return box.intersect(hitbox) }

    open fun draw(game: GameManager) {
        var wd = game.width / GameManager.WIDTH
        var hd = game.height / GameManager.HEIGHT
        if(ressource == null) ressource = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(game.context.resources, sprite), (size.x * wd).toInt(), (size.y * hd).toInt(), false)
        ressource?.let { game.canvas.drawBitmap(it, pos.x * wd, (GameManager.HEIGHT - pos.y - size.y) * hd, Paint()) }
    }

    fun move(inc: Vector) {
        this.pos += inc
        hitbox.left = pos.x
        hitbox.top = pos.y - size.y
        hitbox.right = pos.x + size.x
        hitbox.bottom = pos.y
    }
}