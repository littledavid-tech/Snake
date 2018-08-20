package top.littledavid.snake

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.preference.PreferenceActivity
import android.util.AttributeSet
import android.view.View
import top.littledavid.logerlibrary.e
import top.littledavid.snake.callbacks.OnCrashListener
import top.littledavid.snake.callbacks.OnEatenFoodListener
import java.util.*
import kotlin.concurrent.thread

/**
 * Created by IT on 8/15/2018.
 */
class SnakeGameView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    /**
     * 组成贪吃蛇的小方块的列表
     * */
    private val snake = mutableListOf<SnakeBlock>()

    /**
     * 贪吃蛇的食物
     * */
    private lateinit var food: Food

    /**
     * 单元格集合
     * */
    private lateinit var gridList: MutableList<MutableList<PointF>>

    /**
     * 当贪吃撞击到障碍物时时的回调
     * */
    var crashListener: OnCrashListener? = null

    /**
     * 当贪吃蛇吃到食物的时候的回调
     * */
    var eatenListener: OnEatenFoodListener? = null

    /**
     * 获取或设置贪吃蛇的移动方向
     * */
    var direction = DIRECTION.DIRECTION_RIGHT
        set(value) {//重写属性的Set方法，来避免错误的移动
            when (value) {
                DIRECTION.DIRECTION_UP -> {
                    if (field != DIRECTION.DIRECTION_DOWN) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_DOWN -> {
                    if (field != DIRECTION.DIRECTION_UP) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_LEFT -> {
                    if (field != DIRECTION.DIRECTION_RIGHT) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_RIGHT -> {
                    if (field != DIRECTION.DIRECTION_LEFT) {
                        field = value
                    }
                }
            }
        }

    private var frequency: Long = 800//贪吃蛇移动的速率

    /**
     * 获取游戏是否已经开始
     * */
    var isStarted = false
        private set

    private val random = Random()

    var isRunning = true

    /**
     * 绘制游戏对象
     * */
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (!isStarted)
            return

        drawSnake(canvas!!)
        drawFood(canvas)
    }

    /**
     * 绘制贪吃蛇
     * */
    private fun drawSnake(canvas: Canvas) {
        snake.forEach {
            val pointF = this.gridList[it.row][it.column]
            if (it.isHead) {
                it.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.snakeHeaderPaint)
            } else {
                it.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.snakeBodyPaint)
            }
        }
    }

    /**
     * 绘制食物
     * */
    private fun drawFood(canvas: Canvas) {
        val pointF = this.gridList[food.row][food.column]

        food.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.foodPaint)
    }

    /**
     * 移动贪吃蛇
     * */
    private fun moveTo() {
        //预先计算好蛇头将要到达的位置
        var newHeadRow = snake[0].row
        var newHeadColumn = snake[0].column
        when (this.direction) {
            DIRECTION.DIRECTION_UP -> {
                newHeadRow -= 1
            }
            DIRECTION.DIRECTION_DOWN -> {
                newHeadRow += 1
            }
            DIRECTION.DIRECTION_LEFT -> {
                newHeadColumn -= 1
            }
            DIRECTION.DIRECTION_RIGHT -> {
                newHeadColumn += 1
            }
        }

        //检测是否吃到食物
        if (food.row == newHeadRow && food.column == newHeadColumn) {
            //如果吃到了食物，则不移动贪吃蛇，将食物的位置变为贪吃蛇的脑袋
            snake[0].isHead = false

            val newHead = SnakeBlock(newHeadRow, newHeadColumn, true)
            snake.add(0, newHead)

            if (this.eatenListener != null) {
                this.eatenListener!!.onEaten()
            }
            //加速贪吃蛇的移动速度
            if (frequency > 500) {
                frequency -= 50
            }
            //重新生成食物
            generateFoodInRandom()
        } else {
            //碰撞检测开始
            //想蛇头方向移动贪吃蛇的身子
            for (i in this.snake.size - 1 downTo 1) {
                val previous = this.snake[i - 1]
                val current = this.snake[i]
                current.row = previous.row
                current.column = previous.column

            }
            //移动蛇头
            val head = snake[0]
            head.row = newHeadRow
            head.column = newHeadColumn

            //判断超出边界
            if (head.row < 0
                    || head.row > SnakeGameConfiguration.GAME_ROW_COUNT - 1
                    || head.column < 0
                    || head.column > SnakeGameConfiguration.GAME_COLUMN_COUNT - 1
                    ) {
                isStarted = false
                if (this.crashListener != null) {
                    "Out of the border".e()
                    "head row ${head.row}".e()
                    "head column ${head.column}".e()
                    crashListener!!.onCrash()
                }
            }
            //和自己碰撞的检测
            else if (snake.firstOrNull { it.isHead == false && it.row == head.row && it.column == head.column } != null) {
                isStarted = false
                if (this.crashListener != null) {
                    "Catch itself".e()
                    "head row ${head.row}".e()
                    "head column ${head.column}".e()
                    crashListener!!.onCrash()
                }
            }
            //碰撞检测结束
        }

        //重绘
        this.invalidate()
    }

    /**
     * 测量地图获取地图的基本参数
     * */
    private fun measureGameMap() {
        val w = this.width
        val h = this.height
        SnakeGameConfiguration.GRID_HEIGHT = (h / SnakeGameConfiguration.GAME_ROW_COUNT).toFloat()
        SnakeGameConfiguration.GRID_WIDTH = (w / SnakeGameConfiguration.GAME_COLUMN_COUNT).toFloat()
    }

    /**
     * 生成游戏的单元格
     * */
    private fun generateGird() {
        this.gridList = mutableListOf()

        for (i in 0 until SnakeGameConfiguration.GAME_ROW_COUNT) {
            val tempList = mutableListOf<PointF>()
            for (j in 0 until SnakeGameConfiguration.GAME_COLUMN_COUNT) {
                val point = PointF(j * SnakeGameConfiguration.GRID_WIDTH, i * SnakeGameConfiguration.GRID_HEIGHT)
                tempList.add(point)
            }
            this.gridList.add(tempList)
        }
    }

    /**
     * 随机生成食物
     * */
    private fun generateFoodInRandom() {
        var row = this.random.nextInt(SnakeGameConfiguration.GAME_ROW_COUNT)
        var column = this.random.nextInt(SnakeGameConfiguration.GAME_COLUMN_COUNT)
        while (true) {
            //避免生成的食物和贪吃蛇的位置重叠
            if (this.snake.firstOrNull { it.row == row && it.column == column } == null)
                break
            row = this.random.nextInt(SnakeGameConfiguration.GAME_ROW_COUNT)
            column = this.random.nextInt(SnakeGameConfiguration.GAME_COLUMN_COUNT)
        }
        this.food = Food(row, column)
    }

    /**
     * 生成最初的贪吃蛇
     * */
    private fun generateSnake() {
        this.snake.clear()
        this.snake.add(SnakeBlock(0, 2, true))
        this.snake.add(SnakeBlock(0, 1, false))
        this.snake.add(SnakeBlock(0, 0, false))
    }

    /**
     * 开始游戏
     * */
    fun start() {
        //初始化地图
        //1. 计算地图的布局
        this.measureGameMap()

        this.generateGird()
        this.generateFoodInRandom()
        this.generateSnake()

        isStarted = true
        this.invalidate()
        //开始线程移动贪吃蛇
        thread {
            while (isRunning) {
                if (isStarted) {
                    //通过线程的睡眠，来控制贪吃蛇的移动速度
                    this.post {
                        moveTo()
                    }
                    SystemClock.sleep(this.frequency)
                }
            }
        }
    }

    /**
     * 重新开始游戏
     * */
    fun restart() {
        this.generateGird()
        this.generateFoodInRandom()
        this.generateSnake()
        this.direction = DIRECTION.DIRECTION_RIGHT
        isStarted = true
        invalidate()
    }

    /**
     * 贪吃蛇移动的方向的常量类
     * */
    object DIRECTION {
        val DIRECTION_UP = 0
        val DIRECTION_DOWN = 1
        val DIRECTION_LEFT = 2
        val DIRECTION_RIGHT = 3
    }
}