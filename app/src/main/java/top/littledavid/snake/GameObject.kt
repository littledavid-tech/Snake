package top.littledavid.snake

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.ViewDebug
import top.littledavid.logerlibrary.e

/**
 * 贪吃蛇游戏中所有游戏的父类,所有的游戏对象都将会从此类继承
 * */
open class GameObject(var row: Int, var column: Int) {

    /**
     * 绘制游戏对象
     * @param canvas 画布对象
     * @param paint 画笔对象
     * */
    open fun draw(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        canvas.drawRect(x, y, x + SnakeGameConfiguration.GRID_WIDTH, y + SnakeGameConfiguration.GRID_HEIGHT, paint)
    }
}