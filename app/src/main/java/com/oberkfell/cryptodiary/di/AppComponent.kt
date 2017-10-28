package com.oberkfell.cryptodiary.di

import com.oberkfell.cryptodiary.activity.CryptoDiaryApplication
import com.oberkfell.cryptodiary.di.annotations.ApplicationScope
import com.oberkfell.cryptodiary.modules.ActivityModule
import com.oberkfell.cryptodiary.modules.AppModule
import dagger.Component

@ApplicationScope
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(app: CryptoDiaryApplication)

    fun activityComponent(module: ActivityModule) : ActivityComponent

}