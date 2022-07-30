package com.realworld.android.petsave.common.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.utils.dpToPx
import com.realworld.android.petsave.common.utils.getTextWidth

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.progressButtonStyle,
    defStyleRes: Int = R.style.ProgressButtonStyle
) : View(context, attrs, defStyleAttr, defStyleRes) {

  private var buttonText = ""

  private val textPaint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
    textSize = context.dpToPx(16f)
  }

  private val backgroundPaint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
  }

  private val progressPaint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
    strokeWidth = context.dpToPx(2f)
  }

  private val buttonRect = RectF()
  private val progressRect = RectF()

  private var buttonRadius = context.dpToPx(16f)

  private var offset: Float = 0f

  private var widthAnimator: ValueAnimator? = null
  private var loading = false
  private var startAngle = 0f

  private var rotationAnimator: ValueAnimator? = null
  private var drawTick = false

  init {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyleAttr, defStyleRes)

    buttonText = typedArray.getString(R.styleable.ProgressButton_progressButton_text) ?: ""

    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    val defaultBackgroundColor = typedValue.data

    val defaultTextColor = Color.WHITE
    val defaultProgressColor = Color.WHITE

    val backgroundColor = typedArray.getColor(R.styleable.ProgressButton_progressButton_backgroundColor, defaultBackgroundColor)
    backgroundPaint.color = backgroundColor

    val textColor = typedArray.getColor(R.styleable.ProgressButton_progressButton_textColor, defaultTextColor)
    textPaint.color = textColor

    val progressColor = typedArray.getColor(R.styleable.ProgressButton_progressButton_progressColor, defaultProgressColor)
    progressPaint.color = progressColor
    typedArray.recycle()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    buttonRadius = measuredHeight / 2f
    buttonRect.apply {
      top = 0f
      left = 0f + offset
      right = measuredWidth.toFloat() - offset
      bottom = measuredHeight.toFloat()
    }
    canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius, backgroundPaint)

    if (offset < (measuredWidth - measuredHeight) / 2f) {
      val textX = measuredWidth / 2.0f - textPaint.getTextWidth(buttonText) / 2.0f
      val textY = measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
      canvas.drawText(buttonText, textX,
          textY,
          textPaint)
    }

    if (loading && offset == (measuredWidth - measuredHeight) / 2f) {
      progressRect.left = measuredWidth / 2.0f - buttonRect.width() / 4
      progressRect.top = measuredHeight / 2.0f - buttonRect.width() / 4
      progressRect.right = measuredWidth / 2.0f + buttonRect.width() / 4
      progressRect.bottom = measuredHeight / 2.0f + buttonRect.width() / 4
      canvas.drawArc(progressRect, startAngle, 140f, false, progressPaint)
    }

    if (drawTick) {
      canvas.save()
      canvas.rotate(45f, measuredWidth / 2f, measuredHeight / 2f)
      val x1 = measuredWidth / 2f - buttonRect.width() / 8
      val y1 = measuredHeight / 2f + buttonRect.width() / 4
      val x2 = measuredWidth / 2f + buttonRect.width() / 8
      val y2 = measuredHeight / 2f + buttonRect.width() / 4
      val x3 = measuredWidth / 2f + buttonRect.width() / 8
      val y3 = measuredHeight / 2f - buttonRect.width() / 4
      canvas.drawLine(x1, y1, x2, y2, progressPaint)
      canvas.drawLine(x2, y2, x3, y3, progressPaint)
      canvas.restore()
    }
  }

  fun startLoading() {
    loading = true
    isClickable = false
    widthAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
      addUpdateListener {
        offset = (measuredWidth - measuredHeight) / 2f * it.animatedValue as Float
        invalidate()
      }
      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          super.onAnimationEnd(animation)
          // TODO: call startProgressAnimation()
          startProgressAnimation()
        }
      })
      duration = 200
    }
    widthAnimator?.start()
  }

  private fun startProgressAnimation() {
    rotationAnimator = ValueAnimator.ofFloat(0f, 340f).apply {
      addUpdateListener {
        startAngle = it.animatedValue as Float
        invalidate()
      }
      duration = 600
      interpolator = LinearInterpolator()
      repeatCount = Animation.INFINITE
      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          super.onAnimationEnd(animation)
          loading = false
          drawTick = true
          invalidate()
        }
      })
    }
    rotationAnimator?.start()
  }

  fun done() {
    loading = false
    drawTick = true
    rotationAnimator?.cancel()
    invalidate()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    widthAnimator?.cancel()
    rotationAnimator?.cancel()
  }
}
