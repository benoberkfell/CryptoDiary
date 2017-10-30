package com.oberkfell.cryptodiary.helpers

import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmConfiguration
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class CryptoHelper @Inject constructor(val preferencesHelper: PreferencesHelper) {

    fun unlockDbWithPassword(passphraseOrPin: CharArray): Observable<Boolean> {

        return Observable.create { emitter ->

            val salt = getSalt()

            val iterations = 10000
            val outputKeyLength = 512

            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keySpec = PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength)

            val key = secretKeyFactory.generateSecret(keySpec)
            Arrays.fill(passphraseOrPin, '0')

            try {
                val config = RealmConfiguration.Builder()
                        .encryptionKey(key.encoded)
                        .build()

                Realm.setDefaultConfiguration(config)
                val realm = Realm.getDefaultInstance()
                realm.close()

                // now save the salt after a successful open
                preferencesHelper.salt = salt
                emitter.onNext(true)
            } catch (throwable: Throwable) {
                emitter.onError(throwable)
            }
        }
    }

    private fun getSalt() : ByteArray {
        return if (preferencesHelper.isSaltUnset()) {
            val secRand = SecureRandom()
            val salt = ByteArray(20)
            secRand.nextBytes(salt)
            salt
        } else {
            preferencesHelper.salt
        }
    }

}