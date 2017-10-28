package com.oberkfell.cryptodiary.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.CryptoObject
import android.support.v4.os.CancellationSignal

import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.di.annotations.ActivityScope

import javax.inject.Inject

@ActivityScope
class FingerprintAuthPresenter @Inject constructor(private val fingerprintManager: FingerprintManagerCompat) {

    private var canceled = false
    private var cancellationSignal: CancellationSignal? = null
    private var fingerprintView: FingerprintView? = null

    fun attachView(view: FingerprintView) {
        this.fingerprintView = view
    }


     fun beginAuthentication() {
        cancellationSignal = CancellationSignal()

        val callback = object : FingerprintManagerCompat.AuthenticationCallback() {
            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                super.onAuthenticationHelp(errMsgId, errString)
                if (!canceled) {
                    fingerprintView?.onError(errString.toString(), true)
                }
            }

            override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                super.onAuthenticationHelp(helpMsgId, helpString)
                if (!canceled) {
                    fingerprintView?.onError(helpString.toString(), false)
                }
            }

            override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                fingerprintView?.onSuccess(result.cryptoObject)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                fingerprintView?.onError(R.string.fingerprint_not_recognized)
            }
        }

        fingerprintManager.authenticate(fingerprintView?.cryptoObject(), 0, cancellationSignal, callback, null)
    }

     fun cancel() {
        cancellationSignal?.cancel()
        this.canceled = true
    }


     interface FingerprintView {
        fun cryptoObject(): CryptoObject
        fun onSuccess(cryptoObject: CryptoObject)
        fun onError(errorString: String, isHardError: Boolean)
        fun onError(errorStringRes: Int)
    }
}