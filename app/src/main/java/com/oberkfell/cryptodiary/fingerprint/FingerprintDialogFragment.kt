package com.oberkfell.cryptodiary.fingerprint

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.activity.CryptoDiaryApplication
import com.oberkfell.cryptodiary.modules.ActivityModule

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import android.os.Build.VERSION_CODES.M
import com.oberkfell.cryptodiary.helpers.CryptoHelper
import com.oberkfell.cryptodiary.presenter.FingerprintAuthPresenter
import javax.crypto.Cipher

class FingerprintDialogFragment : DialogFragment(), FingerprintAuthPresenter.FingerprintView {

    @Inject
    lateinit var helper: FingerprintAuthPresenter

    @BindView(R.id.fingerprint_icon)
    lateinit var fingerprintIconImageView: ImageView

    @BindView(R.id.fingerprint_status)
    lateinit var fingerprintStatusTextView: TextView

    private var cipher: Cipher? = null
    private var callback: Callback? = null
    private var title: String? = null

    private var resetFingerprintHint: Runnable = Runnable {
        if (this@FingerprintDialogFragment.isVisible) {
            fingerprintStatusTextView.setText(R.string.fingerprint_hint)
            fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_fp_40px))
            fingerprintStatusTextView.setTextColor(ContextCompat.getColor(activity!!, R.color.hint_color))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)

        val args = arguments
        title = args!!.getString(DIALOG_TITLE_KEY)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as CryptoDiaryApplication)
                .component
                .activityComponent(ActivityModule(activity as Activity))
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View? {
        super.onCreateView(inflater, viewGroup, bundle)

        val view = inflater.inflate(R.layout.fragment_fingerprint_dialog, viewGroup)
        ButterKnife.bind(this, view)

        helper.attachView(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog.setTitle(title)
        helper.beginAuthentication()
    }

    override fun onPause() {
        helper.cancel()
        super.onPause()
    }

    @OnClick(R.id.cancel_button)
    fun onCancelClicked() {
        helper.cancel()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint)
    }

    override fun cryptoObject(): FingerprintManagerCompat.CryptoObject {
        return FingerprintManagerCompat.CryptoObject(cipher!!)
    }

    @RequiresApi(M)
    override fun onSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint)

        fingerprintStatusTextView.text = getString(R.string.fingerprint_success)
        fingerprintStatusTextView.setTextColor(ContextCompat.getColor(activity!!, R.color.success_color))
        fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_fingerprint_success))
        fingerprintStatusTextView.postDelayed({
            dismiss()
            callback?.onAuthenticated(CryptoHelper(cipher!!))
        }, SUCCESS_TIMEOUT_MS)
    }

    override fun onError(errorString: String, isHardError: Boolean) {
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint)

        fingerprintStatusTextView.setTextColor(ContextCompat.getColor(activity!!, R.color.warning_color))
        fingerprintStatusTextView.text = errorString
        fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_fingerprint_error))

        if (!isHardError) {
            fingerprintIconImageView.postDelayed(resetFingerprintHint, ERROR_TIMEOUT_MS)
        } else {
            dismiss()
            callback?.onHardError()
        }
    }

    override fun onError(errorStringRes: Int) {
        onError(getString(errorStringRes), false)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setCipher(cipher: Cipher) {
        this.cipher = cipher
    }

    interface Callback {
        fun onAuthenticated(cryptoHelper: CryptoHelper)
        fun onHardError()
    }

    companion object {

        val DIALOG_TITLE_KEY = "DialogTitle"

        private val SUCCESS_TIMEOUT_MS: Long = 1000
        private val ERROR_TIMEOUT_MS: Long = 1200
    }
}