package com.chen.mynewaudiodemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 * 滑动线
 */
class MySlideLineView : LinearLayout {

    private var mContext: Context? = null

    //线的画笔
    private var linePaint: Paint? = null
    //滑过的部分的线的画笔
    private var passLinePaint: Paint? = null
    //点的画笔
    private var pointPaint: Paint? = null
    //线的颜色
    private var lineColor: Int = Color.GRAY
    //滑过的部分的线的颜色
    private var passLineColor: Int = Color.BLUE
    //点的颜色
    private var pointColor: Int = Color.BLUE

    //控件的宽
    private var viewWidth: Float = 0f
    //控件的高
    private var viewHeight: Float = 0f

    //线在控件中开始的位置
    private var lineStartX: Float = 0f
    //线在控件中结束的位置
    private var lineEndX: Float = 0f
    //线的长度。不是控件的宽度
    private var lineLength: Float = 0f
    //线在Y轴方向的中心位置
    private var centerY: Float = 0f

    //X轴方向，当前手指移动到的位置换算成圆点的位置
    private var drawX: Float = 0f

    private var step: Float = 0f

    private var maxValue: Float = 0f

    //是否正在滑动
    private var isSlide: Boolean = false

    private var isCanSlide: Boolean = true

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context

        setBackgroundColor(Color.TRANSPARENT)

        init()
    }

    //先于 onMeasure 执行
    private fun init() {

        //设置线的画笔属性
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        linePaint?.color = lineColor

        //设置画笔样式为：填充
        linePaint?.style = Paint.Style.FILL

        //设置画笔宽度
        linePaint?.strokeWidth = dp2px(mContext, 3f).toFloat()

        /**
         * Paint.Cap.BUTT：默认类型
         *
         * Paint.Cap.SQUARE：以线条宽度为大小，在开头和结尾分别添加半个正方形
         *
         * Paint.Cap.ROUND：以线条宽度为直径，在开头和结尾分别添加一个半圆
         */
        linePaint?.strokeCap = Paint.Cap.ROUND

        //设置 滑过的线的画笔的属性
        passLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        passLinePaint?.color = passLineColor
        passLinePaint?.style = Paint.Style.FILL
        passLinePaint?.strokeWidth = dp2px(mContext, 3f).toFloat()
        passLinePaint?.strokeCap = Paint.Cap.ROUND


        //设置 滑过的线的画笔的属性
        pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pointPaint?.color = pointColor
        pointPaint?.style = Paint.Style.FILL
        pointPaint?.strokeWidth = dp2px(mContext, 10f).toFloat()
        pointPaint?.strokeCap = Paint.Cap.ROUND

    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        super.onLayout(p0, p1, p2, p3, p4)

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        ev ?: return super.onInterceptTouchEvent(ev)

        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            // 将父控件的滚动事件拦截
            requestDisallowInterceptTouchEvent(true)
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 把滚动事件恢复给父控件
            requestDisallowInterceptTouchEvent(false)
        }

        return super.onInterceptTouchEvent(ev)
    }

    //先于 onSizeChanged 执行
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            widthMeasureSpec,
            dp2px(mContext, 30f)
        )

    }

    //先于 onDraw 执行
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewWidth = w.toFloat()

        viewHeight = h.toFloat()

        lineStartX = dp2px(mContext, 15f).toFloat()

        lineEndX = viewWidth - dp2px(mContext, 15f).toFloat()

        lineLength = lineEndX - lineStartX

        drawX = lineStartX

        centerY = viewHeight / 2

        step = lineLength / maxValue

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawLine(lineStartX, centerY, lineEndX, centerY, linePaint!!)

        canvas?.drawLine(
            lineStartX,
            centerY,
            drawX,
            centerY,
            passLinePaint!!
        )

        canvas?.drawPoint(drawX, viewHeight / 2, pointPaint!!)

    }

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var moveX: Float = 0f
    private var moveY: Float = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return super.onTouchEvent(event)

        if (!isCanSlide) {
            return true
        }

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                downX = event.x
                downY = event.y

                isSlide = true

                mySlideLinePercentageListener?.audioStart()

            }

            MotionEvent.ACTION_MOVE -> {

                moveX = event.x
                moveY = event.y

                isSlide = true

                if (moveX <= lineStartX) {
                    drawX = lineStartX
                } else if (moveX >= lineEndX) {
                    drawX = lineEndX
                } else {
                    drawX = moveX
                }

                postInvalidate()

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                moveX = event.x

                isSlide = false

                mySlideLinePercentageListener?.slidePercentageResult(
                    (drawX - lineStartX) / lineLength,
                    0f,
                    1f
                )

            }

        }

        return true
    }


    fun setMaxValue(maxV: Float) {
        maxValue = maxV
    }

    /**
     * 设置步子添加，即 小圆点正向移动
     *
     * isAllowMove：是否允许移动（添加或减少）
     * isAdd：是否是添加（正向移动）
     * stepNum：一下走几步（几个单位的步子）
     */
    fun setStepMove(isAllowMove: Boolean, isAdd: Boolean, stepNum: Int = 1) {

        if (isAllowMove) {

            if (isAdd) {
                drawX += (step * stepNum)
            } else {
                drawX -= (step * stepNum)
            }

            if (drawX <= lineStartX) {
                drawX = lineStartX
            } else if (drawX >= lineEndX) {
                drawX = lineEndX
            }

            postInvalidate()

            mySlideLinePercentageListener?.moveResult(
                nowPercentage = (drawX - lineStartX) / lineLength,
                isStart = drawX <= lineStartX,
                isEnd = drawX >= lineEndX,
                isAdd = isAdd,
                stepNum = stepNum

            )
        }

    }

    fun getIsNowSlide(): Boolean = isSlide

    fun setCanSlide(b: Boolean) {
        isCanSlide = b
    }

    //重置控件状态
    fun resetViewStatus() {

        drawX = lineStartX
        postInvalidate()

    }

    //百分比
    interface MySlideLinePercentageListener {
        fun slidePercentageResult(nowPercentage: Float, min: Float, max: Float)
        fun moveResult(
            nowPercentage: Float,
            isStart: Boolean,
            isEnd: Boolean,
            isAdd: Boolean,
            stepNum: Int
        )

        fun audioStart()
        fun audioPause()
    }

    private var mySlideLinePercentageListener: MySlideLinePercentageListener? = null

    fun setMySlideLinePercentageListener(listener: MySlideLinePercentageListener) {
        mySlideLinePercentageListener = listener
    }

    private fun dp2px(context: Context?, dpValue: Float): Int {

        if (context != null) {
            val scale: Float = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        } else {
            return 0
        }

    }

}