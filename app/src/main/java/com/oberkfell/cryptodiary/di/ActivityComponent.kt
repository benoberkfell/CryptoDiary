package com.oberkfell.cryptodiary.di

import com.oberkfell.cryptodiary.activity.DiaryListActivity
import com.oberkfell.cryptodiary.activity.MainActivity
import com.oberkfell.cryptodiary.activity.ViewEntryActivity
import com.oberkfell.cryptodiary.modules.ActivityModule
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun inject(activity: MainActivity)
    fun inject(activity: DiaryListActivity)
    fun inject(activity: ViewEntryActivity)
}