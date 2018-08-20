package top.littledavid.snake

import android.graphics.Canvas
import android.graphics.Paint


/**
 * 因为蛇的组成是块状的，此对象就是组成蛇的块
 * */
class SnakeBlock(row: Int, column: Int, var isHead: Boolean) : GameObject(row, column) {

}