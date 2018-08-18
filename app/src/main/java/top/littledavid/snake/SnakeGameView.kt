package top.littledavid.snake

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
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
        /**
         * 移动贪吃蛇
         * 实现思路
         *  1. 首先从蛇的尾部开始，向前一个组成蛇的块的对象的位置移动
         *  2. 然后倒数第二个想倒数第三个的位置移动，依次类图
         *  3. 到蛇头的位置以后，那么根据移动的方向移动蛇头
         * */
        for (i in this.snake.size - 1 downTo 0) {
            val value = this.snake[i]
            //根据移动的方向设置贪吃蛇的坐标
            if (i == 0) {
                when (this.direction) {
                    DIRECTION.DIRECTION_UP -> {
                        value.row -= 1
                    }
                    DIRECTION.DIRECTION_DOWN -> {
                        value.row += 1
                    }
                    DIRECTION.DIRECTION_LEFT -> {
                        value.column -= 1
                    }
                    DIRECTION.DIRECTION_RIGHT -> {
                        value.column += 1
                    }
                }
            } else {
                val previous = this.snake[i - 1]
                val current = this.snake[i]
                current.row = previous.row
                current.column = previous.column
            }
        }
        //碰撞检测开始
        val head = snake[0]
        //判断超出边界
        if (head.row < 0
                || head.row > SnakeGameConfiguration.GAME_ROW_COUNT - 1
                || head.column == -1
                || head.column > SnakeGameConfiguration.GAME_COLUMN_COUNT - 1
                ) {
            isStarted = false
            if (this.crashListener != null) {
                crashListener!!.onCrash()
            }
        }
        //和自己碰撞的检测
        if (snake.firstOrNull { it != head && it.row == head.row && it.column == head.column } != null) {
            isStarted = false
            if (this.crashListener != null) {
                crashListener!!.onCrash()
            }
        }
        //检测是否吃到食物
        if (food.row == head.row && food.column == head.column) {
            appendBlockToTail()
            if (this.eatenListener != null) {
                this.eatenListener!!.onEaten()
            }
            //加速贪吃蛇的移动速度
            if (frequency > 500) {
                frequency -= 50
            }
            generateFoodInRandom()
        }
        //碰撞检测结束
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
            while (isStarted) {
                //通过线程的睡眠，来控制贪吃蛇的移动速度
                SystemClock.sleep(this.frequency)
                this.post {
                    moveTo()
                }
            }
        }
    }

    /**
     * 当贪吃蛇吃到食物后，向尾巴添加一块，实现贪吃蛇长度的增长
     * */
    private fun appendBlockToTail() {
        //向后面追加长度
        val last = snake[snake.size - 1]
        val lastBefore = snake[snake.size - 2]
        var row = last.row
        var column = last.column

        if (last.row == lastBefore.row) {
            if (last.column == 0 || last.column == SnakeGameConfiguration.GAME_COLUMN_COUNT - 1) {
                row = if (last.row == SnakeGameConfiguration.GAME_ROW_COUNT - 1) last.row - 1 else last.row + 1
            } else {
                column = if (last.column < lastBefore.column) last.column - 1 else last.column + 1
            }
        }
        if (last.column == lastBefore.column) {
            if (last.row == 0 || last.row == SnakeGameConfiguration.GAME_ROW_COUNT - 1) {
                column = if (last.column == SnakeGameConfiguration.GAME_COLUMN_COUNT - 1) last.column - 1 else last.column + 1
            } else {
                row = if (last.row < lastBefore.column) last.row - 1 else last.row + 1
            }
        }
        //向组成贪吃蛇的块的集合中添加一块
        val block = SnakeBlock(row, column, false)
        this.snake.add(block)
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