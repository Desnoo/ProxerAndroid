package com.proxerme.app.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ImageView
import com.proxerme.app.R

/**
 * An ImageView, which calculates it's height by the given width and an aspect.

 * @author Ruben Gees
 */
class WidthAspectImageView : ImageView {

    companion object {
        private val DEFAULT_ASPECT = 1f
    }

    var aspect: Float

    constructor(context: Context?) : super(context) {
        aspect = DEFAULT_ASPECT
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WidthAspectImageView)

        aspect = attributes.getFloat(R.styleable.WidthAspectImageView_aspect,
                DEFAULT_ASPECT)

        attributes.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
    super(context, attrs, defStyleAttr) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WidthAspectImageView)

        aspect = attributes.getFloat(R.styleable.WidthAspectImageView_aspect,
                DEFAULT_ASPECT)

        attributes.recycle()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WidthAspectImageView)

        aspect = attributes.getFloat(R.styleable.WidthAspectImageView_aspect,
                DEFAULT_ASPECT)

        attributes.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(measuredWidth, (measuredWidth * aspect).toInt())
    }
}
