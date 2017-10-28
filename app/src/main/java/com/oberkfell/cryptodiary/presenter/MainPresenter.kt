package com.oberkfell.cryptodiary.presenter

import android.app.KeyguardManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.oberkfell.cryptodiary.BuildConfig
import com.oberkfell.cryptodiary.di.annotations.ActivityScope
import com.oberkfell.cryptodiary.helpers.CryptoHelper
import com.oberkfell.cryptodiary.helpers.PreferencesHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

@ActivityScope
class MainPresenter @Inject constructor(private var fingerprintManager: FingerprintManagerCompat,
                                        private var keyguardManager: KeyguardManager,
                                        private var preferencesHelper: PreferencesHelper) {

    lateinit var view: View

    private val KEY_NAME = "db_key"
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

    private var compositeDisposable = CompositeDisposable()

    fun attach(view: View) {
        this.view = view

        if (isFingerprintAvailable()) {
            view.showFingerprintOption()
            view.setFingerprintOptionState(preferencesHelper.isKeySet())
        } else {
            view.hideFingerprintOption()
        }

        if (preferencesHelper.isKeySet()) {
            attemptUnlockFromFingerprint()
        }
    }

    fun destroy() {
        compositeDisposable.dispose()
    }

    fun fingerprintOptionChanged(checked: Boolean) {
        if (!checked && preferencesHelper.isKeySet()) {
            preferencesHelper.clearKey()
        }
    }



    fun attemptUnlockWithPassword(password: CharArray) {
        if (view.getFingerprintOptionState() && !preferencesHelper.isKeySet()) {
            enrollFingerprintAndUnlockDatabase(password)
        } else {
            justUnlockDatabase(password)
        }
    }

    private fun justUnlockDatabase(password: CharArray) {
        compositeDisposable.add(passwordUnlockObservable(password, { })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.proceedToDiary()
                }, {
                    view.showError("An error occurred unlocking, check your password")
                }))
    }

    private fun enrollFingerprintAndUnlockDatabase(password: CharArray) {
        val cipher: Cipher
        generateKey()
        try {
            cipher = getCipher(true)
        } catch (exception: KeyPermanentlyInvalidatedException) {
            reportInvalidKey()
            return
        }

        compositeDisposable.add(view.launchFingerprintDialog("Enroll Fingerprint", cipher)
                .doOnError {
                    view.showError("Could not authenticate fingerprint.")
                }
                .flatMap ({ cryptoHelper ->
                    passwordUnlockObservable(password, { derivedKey ->
                        // If we've unlocked the key, use it to encrypt the generated passphrase
                        val unlockedCipher = cryptoHelper.cipher
                        val bytes = unlockedCipher.doFinal(derivedKey)
                        preferencesHelper.encryptedRealmKey = bytes
                    })
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.proceedToDiary()
                }, {
                    Arrays.fill(password, '0') // clear the password just to be sure
                    view.showError("An error occurred unlocking, check your password")
                }))
    }

    private fun attemptUnlockFromFingerprint() {
        val cipher: Cipher
        try {
            cipher = getCipher(false)
        } catch (exception: KeyPermanentlyInvalidatedException) {
            reportInvalidKey()
            return
        }

        compositeDisposable.add(view.launchFingerprintDialog("Unlock Diary", cipher)
                .doOnError { view.showError("Could not authenticate fingerprint.") }
                .flatMap { cryptoHelper -> keyUnlockObservable(cryptoHelper.cipher) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.proceedToDiary()
                    }, {
                        view.showError("Could not unlock the diary with the secret key.")
                    }))
    }

    private fun keyUnlockObservable(cipher: Cipher) : Observable<Boolean> {
        return Observable.create { emitter ->
            try {
                val decrypted = cipher.doFinal(preferencesHelper.encryptedRealmKey)
                val config = RealmConfiguration.Builder()
                        .encryptionKey(decrypted)
                        .build()
                Arrays.fill(decrypted, 0)
                Realm.setDefaultConfiguration(config)
                val realm = Realm.getDefaultInstance()
                realm.close()
                emitter.onNext(true)
                emitter.onComplete()
            } catch (throwable: Throwable) {
                emitter.onError(throwable)
            }
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray) : ByteArray {
        val iterations = 10000
        val outputKeyLength = 512

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keySpec = PBEKeySpec(password, salt, iterations, outputKeyLength)
        val key = secretKeyFactory.generateSecret(keySpec)

        Arrays.fill(password, '0')
        return key.encoded
    }

    private fun passwordUnlockObservable(password: CharArray, postUnlockCallback: (key: ByteArray) -> Unit) : Observable<Boolean> {
        return Observable.create { emitter ->

            val salt = getSalt()
            val key = deriveKey(password, salt)

            try {
                val config = RealmConfiguration.Builder()
                        .encryptionKey(key)
                        .build()

                Realm.setDefaultConfiguration(config)
                val realm = Realm.getDefaultInstance()
                realm.close()

                // now save the salt after a successful open
                preferencesHelper.salt = salt

                postUnlockCallback.invoke(key)
                Arrays.fill(key, 0)
                emitter.onNext(true)
            } catch (throwable: Throwable) {
                emitter.onError(throwable)
            }
        }
    }

    private fun getCipher(encrypt: Boolean) : Cipher {
        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        keyStore.load(null)

        //key still cannot be used yet until authentication!
        val secretKey = keyStore.getKey(KEY_NAME, null)

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            // save the IV to shared prefs so that it can be used later
            preferencesHelper.iv = cipher.iv
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(preferencesHelper.iv))
        }

        return cipher
    }

    private fun generateKey() {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSalt() : ByteArray {
        return if (preferencesHelper.isSaltSet()) {
            preferencesHelper.salt
        } else {
            getSomeRandomBytes(20)
        }
    }

    private fun getSomeRandomBytes(size: Int) : ByteArray {
        val secRand = SecureRandom()
        val bytes = ByteArray(size)
        secRand.nextBytes(bytes)
        return bytes
    }


    private fun isFingerprintAvailable() : Boolean {
        return fingerprintManager.isHardwareDetected() &&
                fingerprintManager.hasEnrolledFingerprints()
    }

    private fun reportInvalidKey() {
        preferencesHelper.clearKey()
        view.setFingerprintOptionState(false)
        view.showError("Key was invalidated.")
    }

    interface View {
        fun showProgress()
        fun hideProgress()

        fun showFingerprintOption()
        fun hideFingerprintOption()

        fun setFingerprintOptionState(checked: Boolean)
        fun getFingerprintOptionState() : Boolean

        fun proceedToDiary()
        fun showError(error: String)

        fun launchFingerprintDialog(title: String, cipher: Cipher) : Observable<CryptoHelper>
    }


}