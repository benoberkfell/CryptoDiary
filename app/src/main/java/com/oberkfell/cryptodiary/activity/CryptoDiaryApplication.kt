package com.oberkfell.cryptodiary.activity

import android.app.Application
import com.oberkfell.cryptodiary.di.AppComponent
import com.oberkfell.cryptodiary.di.DaggerAppComponent
import com.oberkfell.cryptodiary.modules.AppModule
import io.realm.Realm

class CryptoDiaryApplication : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        component.inject(this)

        Realm.init(this)
    }


}