package com.proxerme.app.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */

val Context.inputMethodManager: InputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun TextView.measureAndGetHeight(totalMarginDp: Float): Int {
    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            Utils.getScreenWidth(context) - Utils.convertDpToPx(context, totalMarginDp),
            View.MeasureSpec.AT_MOST)
    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
            View.MeasureSpec.UNSPECIFIED)

    measure(widthMeasureSpec, heightMeasureSpec)

    return measuredHeight
}