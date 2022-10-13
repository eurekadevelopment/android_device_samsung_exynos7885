package com.eurekateam.fmradio

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PebbleTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {
    constructor (context: Context) : this(context, null)
    constructor (context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    var mText: String = "102.7"
    var mColor: Int = Color.GREEN

    private val mPaint = Paint()
    private val mRectf = RectF(25F, 25F, 350F, 270F)
    private val mRectfAnother = RectF(10F, 25F, 250F, 235F)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.color = mColor
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(175F, 175F, 140F, mPaint)
        canvas.drawArc(mRectf, -120F, 180F, true, mPaint)
        canvas.drawArc(mRectfAnother, -240F, 180F, true, mPaint)
        mPaint.color = Color.BLACK
        setTextSizeForWidth(mPaint, 200F, mText)
        canvas.drawText(mText, 75F, 175F, mPaint)
        setTextSizeForWidth(mPaint, 75F, "Mhz")
        canvas.drawText("Mhz", 155F, 235F, mPaint)
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     * the Paint to set the text size for
     * @param desiredWidth
     * the desired width
     * @param text
     * the text that should be that width
     */
    private fun setTextSizeForWidth(
        paint: Paint,
        desiredWidth: Float,
        text: String
    ) {
        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        val testTextSize = 48f

        // Get the bounds of the text, using our testTextSize.
        paint.textSize = testTextSize
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // Calculate the desired size as a proportion of our testTextSize.
        val desiredTextSize: Float = testTextSize * desiredWidth / bounds.width()

        // Set the paint for that size.
        paint.textSize = desiredTextSize
    }
}
