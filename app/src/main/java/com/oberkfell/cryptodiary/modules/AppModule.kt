package com.oberkfell.cryptodiary.modules

import com.oberkfell.cryptodiary.activity.CryptoDiaryApplication
import com.oberkfell.cryptodiary.di.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import android.content.Context.KEYGUARD_SERVICE
import android.app.KeyguardManager
import android.content.Context
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat



@Module
@ApplicationScope
class AppModule(private val application: CryptoDiaryApplication) {

    @Provides
    @ApplicationScope
    fun provideApp() = application

    @Provides
    fun providesFingerprintManager(app: CryptoDiaryApplication): FingerprintManagerCompat {
        return FingerprintManagerCompat.from(app)
    }

    @Provides
    fun providesKeyguardManager(app: CryptoDiaryApplication): KeyguardManager {
        return app.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }
}