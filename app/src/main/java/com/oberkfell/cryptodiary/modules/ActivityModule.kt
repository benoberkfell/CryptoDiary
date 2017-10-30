package com.oberkfell.cryptodiary.modules

import android.app.Activity
import dagger.Module
import dagger.Provides
import java.text.SimpleDateFormat
import java.util.*

@Module
class ActivityModule constructor(private val activity: Activity) {


    @Provides
    fun provideDateFormatterThreadLocal() : ThreadLocal<SimpleDateFormat> {
        return object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            }
        }
    }

}