package com.oberkfell.cryptodiary.modules

import com.oberkfell.cryptodiary.activity.CryptoDiaryApplication
import com.oberkfell.cryptodiary.di.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@ApplicationScope
class AppModule(private val application: CryptoDiaryApplication) {

    @Provides
    @ApplicationScope
    fun provideApp() = application
}