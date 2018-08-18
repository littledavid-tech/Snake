package top.littledavid.snake

import android.graphics.Color
import android.graphics.Paint

/**
 * Created by IT on 8/15/2018.
 */
object SnakeGamePaint {
    /**
     * 画蛇身体的画笔
     * */
    val snakeBodyPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    /**
     * 画蛇头部的画笔
     * */
    val snakeHeaderPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLUE
    }
    /**
     * 话食物的画笔
     * */
    val foodPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED
    }

    /**
     * 话墙壁的画笔
     * */
    val wallPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
    }
}