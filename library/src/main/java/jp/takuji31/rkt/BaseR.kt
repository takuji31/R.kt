package jp.takuji31.rkt

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat

open class BaseR(val context: Context) {
    open class Drawables(internal val context: Context) {

    }
    open class Image(private val context: Context, val id: Int) {
        fun asDrawable() : Drawable = ContextCompat.getDrawable(context, id)
    }
}