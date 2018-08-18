package top.littledavid.snake

import android.graphics.Canvas
import android.graphics.Paint


/**
 * 贪吃蛇吃的食物
 * */
class Food(row: Int, column: Int) : GameObject(row, column) {
    override fun draw(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        val gw = SnakeGameConfiguration.GRID_WIDTH
        val gh = SnakeGameConfiguration.GRID_HEIGHT
        canvas.drawCircle(x + (gw / 2), y + (gh / 2), gw / 2, paint)
    }
}