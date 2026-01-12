package com.trashpiles.presentation.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.*

/**
 * Dynamic Smiley Face View
 * Changes expressions and colors based on game state
 * Provides visual feedback and personality to the game
 */
class SmileyFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // State management
    enum class State {
        NORMAL,           // Regular gameplay
        HAPPY,            // Good move
        CONFUSED,         // Wrong move
        THINKING,         // Player's turn
        CELEBRATING,      // Won round/game
        DISAPPOINTED,     // Lost round/game
        TRANSITIONING     // Between players
    }

    enum class Expression {
        NEUTRAL,          // 😐
        HAPPY,            // 😊
        BIG_SMILE,        // 😄
        FROWN,            // 😢
        CONFUSED,         // 😕
        THINKING,         // 🤔
        CELEBRATING,      // 🎉
        DISAPPOINTED      // 😞
    }

    // Current state
    private var currentState = State.NORMAL
    private var currentExpression = Expression.NEUTRAL
    private var currentColor = Color.parseColor("#FF6F00") // Default orange

    // Animation state
    private var animationProgress = 0f
    private var isAnimating = false
    private var shakeIntensity = 0f
    private var bounceHeight = 0f

    // Paint objects
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Dimensions
    private var faceRadius = 0f
    private var eyeRadius = 0f
    private var eyeOffset = 0f
    private var mouthWidth = 0f

    // Animation references
    private var currentAnimator: AnimatorSet? = null

    init {
        setupPaints()
    }

    private fun setupPaints() {
        facePaint.color = currentColor
        facePaint.style = Paint.Style.FILL
        
        eyePaint.color = Color.WHITE
        eyePaint.style = Paint.Style.FILL
        
        mouthPaint.color = Color.BLACK
        mouthPaint.style = Paint.Style.STROKE
        mouthPaint.strokeWidth = 4f
        mouthPaint.strokeCap = Paint.Cap.ROUND
        
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = 100
        highlightPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val size = min(w, h).toFloat()
        faceRadius = size * 0.4f
        eyeRadius = size * 0.06f
        eyeOffset = size * 0.15f
        mouthWidth = size * 0.25f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Apply shake effect
        canvas.save()
        if (shakeIntensity > 0f) {
            val shakeX = sin(System.currentTimeMillis() * 0.05f) * shakeIntensity * 10f
            val shakeY = cos(System.currentTimeMillis() * 0.05f) * shakeIntensity * 10f
            canvas.translate(shakeX, shakeY)
        }
        
        // Apply bounce effect
        val bounceY = bounceHeight * sin(animationProgress * PI).toFloat()
        canvas.translate(0f, -bounceY)
        
        // Draw face
        drawFace(canvas, centerX, centerY)
        
        // Draw eyes
        drawEyes(canvas, centerX, centerY)
        
        // Draw mouth
        drawMouth(canvas, centerX, centerY)
        
        // Draw expression-specific details
        drawExpressionDetails(canvas, centerX, centerY)
        
        canvas.restore()
    }

    private fun drawFace(canvas: Canvas, centerX: Float, centerY: Float) {
        // Main face circle
        facePaint.color = currentColor
        canvas.drawCircle(centerX, centerY, faceRadius, facePaint)
        
        // Highlight/shine effect
        highlightPaint.alpha = 80
        canvas.drawCircle(
            centerX - faceRadius * 0.3f,
            centerY - faceRadius * 0.3f,
            faceRadius * 0.2f,
            highlightPaint
        )
    }

    private fun drawEyes(canvas: Canvas, centerX: Float, centerY: Float) {
        val leftEyeX = centerX - eyeOffset
        val rightEyeX = centerX + eyeOffset
        val eyeY = centerY - faceRadius * 0.2f
        
        when (currentExpression) {
            Expression.THINKING -> {
                // Eyes looking left and right
                val lookOffset = sin(System.currentTimeMillis() * 0.003f) * 5f
                drawEye(canvas, leftEyeX + lookOffset, eyeY)
                drawEye(canvas, rightEyeX + lookOffset, eyeY)
            }
            Expression.CELEBRATING -> {
                // Star/sparkle eyes
                drawStarEyes(canvas, leftEyeX, eyeY)
                drawStarEyes(canvas, rightEyeX, eyeY)
            }
            Expression.DISAPPOINTED -> {
                // Closed eyes
                drawClosedEye(canvas, leftEyeX, eyeY)
                drawClosedEye(canvas, rightEyeX, eyeY)
            }
            else -> {
                // Normal open eyes
                drawEye(canvas, leftEyeX, eyeY)
                drawEye(canvas, rightEyeX, eyeY)
            }
        }
    }

    private fun drawEye(canvas: Canvas, x: Float, y: Float) {
        eyePaint.color = Color.WHITE
        canvas.drawCircle(x, y, eyeRadius, eyePaint)
        
        // Pupil
        eyePaint.color = Color.BLACK
        canvas.drawCircle(x, y, eyeRadius * 0.4f, eyePaint)
    }

    private fun drawStarEyes(canvas: Canvas, centerX: Float, centerY: Float) {
        eyePaint.color = Color.YELLOW
        val innerRadius = eyeRadius * 0.5f
        val outerRadius = eyeRadius * 1.2f
        val points = 5
        val rotation = System.currentTimeMillis() * 0.002f
        
        val starPath = createStarPath(centerX, centerY, innerRadius, outerRadius, points, rotation)
        canvas.drawPath(starPath, eyePaint)
    }

    private fun drawClosedEye(canvas: Canvas, x: Float, y: Float) {
        eyePaint.color = Color.BLACK
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = 3f
        
        canvas.drawLine(
            x - eyeRadius,
            y,
            x + eyeRadius,
            y,
            eyePaint
        )
        
        eyePaint.style = Paint.Style.FILL
    }

    private fun drawMouth(canvas: Canvas, centerX: Float, centerY: Float) {
        val mouthY = centerY + faceRadius * 0.3f
        
        when (currentExpression) {
            Expression.HAPPY -> drawSmile(canvas, centerX, mouthY, 0.5f)
            Expression.BIG_SMILE -> drawSmile(canvas, centerX, mouthY, 0.8f)
            Expression.FROWN, Expression.DISAPPOINTED -> drawFrown(canvas, centerX, mouthY, 0.4f)
            Expression.CONFUSED -> drawConfusedMouth(canvas, centerX, mouthY)
            Expression.THINKING -> drawNeutralMouth(canvas, centerX, mouthY)
            Expression.CELEBRATING -> drawSmile(canvas, centerX, mouthY, 0.9f)
            Expression.NEUTRAL -> drawNeutralMouth(canvas, centerX, mouthY)
        }
    }

    private fun drawSmile(canvas: Canvas, centerX: Float, y: Float, intensity: Float) {
        val path = Path()
        val smileRadius = mouthWidth * intensity
        
        path.moveTo(centerX - smileRadius, y)
        path.quadTo(
            centerX,
            y + smileRadius * 1.5f,
            centerX + smileRadius,
            y
        )
        
        canvas.drawPath(path, mouthPaint)
    }

    private fun drawFrown(canvas: Canvas, centerX: Float, y: Float, intensity: Float) {
        val path = Path()
        val frownRadius = mouthWidth * intensity
        
        path.moveTo(centerX - frownRadius, y + frownRadius * 0.5f)
        path.quadTo(
            centerX,
            y - frownRadius * 0.5f,
            centerX + frownRadius,
            y + frownRadius * 0.5f
        )
        
        canvas.drawPath(path, mouthPaint)
    }

    private fun drawConfusedMouth(canvas: Canvas, centerX: Float, y: Float) {
        // Slightly tilted line
        val path = Path()
        path.moveTo(centerX - mouthWidth * 0.3f, y - 5f)
        path.lineTo(centerX + mouthWidth * 0.3f, y + 5f)
        canvas.drawPath(path, mouthPaint)
    }

    private fun drawNeutralMouth(canvas: Canvas, centerX: Float, y: Float) {
        canvas.drawLine(
            centerX - mouthWidth * 0.3f,
            y,
            centerX + mouthWidth * 0.3f,
            y,
            mouthPaint
        )
    }

    private fun drawExpressionDetails(canvas: Canvas, centerX: Float, centerY: Float) {
        when (currentExpression) {
            Expression.CONFUSED -> {
                // Raised eyebrow
                mouthPaint.strokeWidth = 2f
                canvas.drawLine(
                    centerX - eyeOffset - 10f,
                    centerY - faceRadius * 0.4f - 5f,
                    centerX - eyeOffset + 10f,
                    centerY - faceRadius * 0.4f + 5f,
                    mouthPaint
                )
                mouthPaint.strokeWidth = 4f
            }
            Expression.CELEBRATING -> {
                // Draw sparkles around face
                drawSparkles(canvas, centerX, centerY)
            }
            else -> {}
        }
    }

    private fun drawSparkles(canvas: Canvas, centerX: Float, centerY: Float) {
        val sparkleCount = 6
        val sparkleRadius = faceRadius * 0.8f
        
        for (i in 0 until sparkleCount) {
            val angle = (i * (360f / sparkleCount) + System.currentTimeMillis() * 0.1f) * (PI / 180f)
            val sparkleX = centerX + cos(angle).toFloat() * sparkleRadius
            val sparkleY = centerY + sin(angle).toFloat() * sparkleRadius
            
            drawSparkle(canvas, sparkleX, sparkleY)
        }
    }

    private fun drawSparkle(canvas: Canvas, x: Float, y: Float) {
        highlightPaint.alpha = 255
        canvas.drawCircle(x, y, 3f, highlightPaint)
        
        // Draw sparkle lines
        mouthPaint.strokeWidth = 2f
        canvas.drawLine(x - 5f, y, x + 5f, y, mouthPaint)
        canvas.drawLine(x, y - 5f, x, y + 5f, mouthPaint)
        mouthPaint.strokeWidth = 4f
    }

    private fun createStarPath(
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        points: Int,
        rotation: Double
    ): Path {
        val path = Path()
        val angleStep = (2 * PI) / points
        
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * angleStep / 2 + rotation
            val x = centerX + cos(angle).toFloat() * radius
            val y = centerY + sin(angle).toFloat() * radius
            
            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        
        path.close()
        return path
    }

    // Public API

    /**
     * Set the smiley face state
     */
    fun setState(state: State, animate: Boolean = true) {
        currentState = state
        currentExpression = getExpressionForState(state)
        
        if (animate) {
            animateStateChange()
        } else {
            invalidate()
        }
    }

    /**
     * Set the smiley face color (matches player color)
     */
    fun setColor(color: Int, animate: Boolean = true) {
        if (animate) {
            animateColorChange(currentColor, color)
        } else {
            currentColor = color
            invalidate()
        }
    }

    /**
     * Trigger shake effect (for invalid moves)
     */
    fun shake() {
        shakeIntensity = 1f
        
        object : Thread() {
            override fun run() {
                repeat(10) {
                    Thread.sleep(50)
                    postInvalidate()
                }
                shakeIntensity = 0f
                postInvalidate()
            }
        }.start()
    }

    /**
     * Trigger bounce effect (for celebrations)
     */
    fun bounce() {
        currentAnimator?.cancel()
        
        val bounceAnim = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f)
        bounceAnim.duration = 300
        bounceAnim.interpolator = AccelerateDecelerateInterpolator()
        
        bounceAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bounceHeight = 0f
                invalidate()
            }
        })
        
        bounceAnim.addUpdateListener { 
            animationProgress = it.animatedValue as Float
            bounceHeight = 30f * sin(animationProgress * PI).toFloat()
            invalidate()
        }
        
        val animatorSet = AnimatorSet()
        animatorSet.play(bounceAnim)
        animatorSet.start()
        
        currentAnimator = animatorSet
    }

    private fun getExpressionForState(state: State): Expression {
        return when (state) {
            State.NORMAL -> Expression.NEUTRAL
            State.HAPPY -> Expression.HAPPY
            State.CONFUSED -> Expression.CONFUSED
            State.THINKING -> Expression.THINKING
            State.CELEBRATING -> Expression.CELEBRATING
            State.DISAPPOINTED -> Expression.DISAPPOINTED
            State.TRANSITIONING -> Expression.NEUTRAL
        }
    }

    private fun animateStateChange() {
        invalidate()
    }

    private fun animateColorChange(fromColor: Int, toColor: Int) {
        val colorAnim = ObjectAnimator.ofArgb(this, "currentColor", fromColor, toColor)
        colorAnim.duration = 200
        
        colorAnim.addUpdateListener {
            currentColor = it.animatedValue as Int
            invalidate()
        }
        
        colorAnim.start()
    }

    // Setters for animation
    fun setCurrentColor(color: Int) {
        this.currentColor = color
    }
}