package com.eurekateam.fmradio.utils

import android.content.Context
import android.content.res.Configuration

class MaterialHelper (private val mContext : Context, private val mOnLight : Int, private val mOnDark : Int)
{
      private constructor(builder: Builder) : this(builder.context, builder.light, builder.dark)

      class Builder {
          lateinit var context: Context
          var light = 0
          var dark = 0

          fun build() = MaterialHelper(this)
      }

      companion object {
          inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
      }

      fun getValue() : Int =
        when (mContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> mContext.resources.getColor(mOnLight, mContext.theme)
            Configuration.UI_MODE_NIGHT_NO -> mContext.resources.getColor(mOnDark, mContext.theme)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mContext.resources.getColor(mOnLight, mContext.theme)
            else -> 0
        }
}
