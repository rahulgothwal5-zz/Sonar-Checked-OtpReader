package com.developer.otpreaderkotlin.otpReader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.developer.otpreaderkotlin.R

class OtpEditText : AppCompatEditText {
    private var defStyleAttr = 0

    private var mSpace = 8f //24 dp by default, space between the lines
    private var mCharSize: Float = 0.toFloat()
    private var mNumChars = 6f
    private var mLineSpacing = 10f //8dp by default, height of the text from our lines
    private var mMaxLength = 6

    private var mClickListener: OnClickListener? = null

    private var mLineStroke = 1f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null

    private var mMainColor: Int = 0
    private var mSecondaryColor: Int = 0
    private var mTextColor: Int = 0

    private var mStrokePaint: Paint? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.defStyleAttr = defStyleAttr
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {

        getAttrsFromTypedArray(attrs)

        val multi = context.resources.displayMetrics.density
        mLineStroke = multi * mLineStroke
        mLineStrokeSelected = multi * mLineStrokeSelected
        mLinesPaint = Paint(getPaint())
        mStrokePaint = Paint(getPaint())
        mStrokePaint!!.strokeWidth = 4f
        mStrokePaint!!.style = Paint.Style.STROKE
        mLinesPaint!!.strokeWidth = mLineStroke
        setBackgroundResource(0)
        mSpace = multi * mSpace //convert to pixels for our density
        mNumChars = mMaxLength.toFloat()

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.

        super.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View) {
                setSelection(getText()!!.length)
                if (mClickListener != null) {
                    mClickListener!!.onClick(v)
                }
            }
        })

    }

    private fun getAttrsFromTypedArray(attributeSet: AttributeSet) {
        val a = getContext().obtainStyledAttributes(
            attributeSet,
            R.styleable.OtpEditText,
            defStyleAttr,
            0
        )

        mMaxLength = attributeSet.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
        mMainColor = a.getColor(
            R.styleable.OtpEditText_oev_primary_color,
            getResources().getColor(android.R.color.holo_red_dark)
        )
        mSecondaryColor = a.getColor(
            R.styleable.OtpEditText_oev_secondary_color,
            getResources().getColor(R.color.dark_grey)
        )
        mTextColor = a.getColor(
            R.styleable.OtpEditText_oev_text_color,
            getResources().getColor(android.R.color.black)
        )

        a.recycle()
    }

//    fun setOnClickListener(l: OnClickListener) {
//        mClickListener = l
//    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    protected override fun onDraw(canvas: Canvas) {
        val availableWidth = getWidth() - getPaddingRight() - getPaddingLeft()
        if (mSpace < 0) {
            mCharSize = availableWidth / (mNumChars * 2 - 1)
        } else {
            mCharSize = (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        mLineSpacing = (getHeight() * .6).toFloat()

        var startX = getPaddingLeft()
        val bottom = getHeight() - getPaddingBottom()
        val top = getPaddingTop()

        //Text Width
        val text = getText()
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        getPaint().getTextWidths(getText(), 0, textLength, textWidths)

        var i = 0
        while (i < mNumChars) {
            updateColorForLines(
                i <= textLength,
                i == textLength,
                getText()!!.length,
                mNumChars.toInt()
            )
            canvas.drawLine(
                startX.toFloat(),
                bottom.toFloat(),
                startX + mCharSize,
                bottom.toFloat(),
                mLinesPaint!!
            )

            try {
                canvas.drawRoundRect(
                    startX.toFloat(),
                    top.toFloat(),
                    startX + mCharSize,
                    bottom.toFloat(),
                    8f,
                    8f,
                    mLinesPaint!!
                )
                canvas.drawRoundRect(
                    startX.toFloat(),
                    top.toFloat(),
                    startX + mCharSize,
                    bottom.toFloat(),
                    8f,
                    8f,
                    mStrokePaint!!
                )
            } catch (err: NoSuchMethodError) {
                canvas.drawRect(
                    startX.toFloat(),
                    top.toFloat(),
                    startX + mCharSize,
                    bottom.toFloat(),
                    mLinesPaint!!
                )
                canvas.drawRect(
                    startX.toFloat(),
                    top.toFloat(),
                    startX + mCharSize,
                    bottom.toFloat(),
                    mStrokePaint!!
                )
            }

            if (getText()!!.length > i) {
                val middle = startX + mCharSize / 2
                canvas.drawText(
                    text.toString(),
                    i,
                    i + 1,
                    middle - textWidths[0] / 2,
                    mLineSpacing,
                    getPaint()
                )
            }

            if (mSpace < 0) {
                startX += (mCharSize * 2).toInt()
            } else {
                startX += (mCharSize + mSpace).toInt()
            }
            i++
        }
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(
        next: Boolean,
        current: Boolean,
        textSize: Int,
        totalSize: Int
    ) {
        if (next) {
            mStrokePaint!!.color = mSecondaryColor
            mLinesPaint!!.color = mSecondaryColor
        } else {
            mStrokePaint!!.color = mSecondaryColor
            mLinesPaint!!.color = ContextCompat.getColor(getContext(), android.R.color.white)
        }
        if (current) {
            mLinesPaint!!.color = ContextCompat.getColor(getContext(), android.R.color.white)
            mStrokePaint!!.color = mMainColor
        }
    }

    companion object {
        val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }
}