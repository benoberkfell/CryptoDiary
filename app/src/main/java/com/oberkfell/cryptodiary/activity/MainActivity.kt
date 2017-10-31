package com.oberkfell.cryptodiary.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick
import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.fingerprint.FingerprintDialogFragment
import com.oberkfell.cryptodiary.helpers.CryptoHelper
import com.oberkfell.cryptodiary.modules.ActivityModule
import com.oberkfell.cryptodiary.presenter.MainPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.crypto.Cipher
import javax.inject.Inject


class MainActivity : AppCompatActivity(), MainPresenter.View {

    @Inject
    lateinit var presenter: MainPresenter

    @BindView(R.id.passwordEntry)
    lateinit var passwordEntry: EditText

    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.passwordButton)
    lateinit var passwordButton: Button

    @BindView(R.id.useFingerprintCheckbox)
    lateinit var useFingerprintCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        (application as CryptoDiaryApplication)
                .component
                .activityComponent(ActivityModule(this))
                .inject(this)

        presenter.attach(this)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @OnCheckedChanged(R.id.useFingerprintCheckbox)
    fun fingerprintCheckboxChanged() {
        presenter.fingerprintOptionChanged(useFingerprintCheckbox.isChecked)
    }

    @OnClick(R.id.passwordButton)
    fun onClickPasswordButton() {
        if (!passwordEntry.text.isNullOrEmpty()) {
            presenter.attemptUnlockWithPassword(passwordEntry.text.toString())
        } else {
            Toast.makeText(this, "You need to enter a password.", Toast.LENGTH_LONG).show()
        }
    }

    override fun launchFingerprintDialog(title: String, cipher: Cipher) : Observable<CryptoHelper> {
        var observable : Observable<CryptoHelper> = Observable.create { emitter ->
            val fragment = FingerprintDialogFragment()
            val args = Bundle()
            args.putString(FingerprintDialogFragment.DIALOG_TITLE_KEY, title)
            fragment.arguments = args
            fragment.setCipher(cipher)
            fragment.setCallback(object : FingerprintDialogFragment.Callback {
                override fun onHardError() {
                    emitter.onError(Exception("Hard error"))
                }

                override fun onAuthenticated(cryptoHelper: CryptoHelper) {
                    emitter.onNext(cryptoHelper)
                }
            })
            fragment.show(supportFragmentManager, "fingerprint_dialog")
        }

        return observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun proceedToDiary() {
        startActivity(Intent(this, DiaryListActivity::class.java))
        finish()
    }

    override fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }


    /*

        val useFingerprint = isFingerprintAvailable() && useFingerprintCheckbox.isChecked

        if (isFingerprintAvailable() && useFingerprintCheckbox.isChecked && prefsHelper.isKeyUnset()) {
            cryptoHelper.generateKey()

            val fragment = FingerprintDialogFragment()
            val args = Bundle()
            args.putString(FingerprintDialogFragment.DIALOG_TITLE_KEY, "Hello there")

            fragment.arguments = args
            fragment.setCipher(cryptoHelper.getCipher(true))
            fragment.setCallback(object : FingerprintDialogFragment.Callback {
                override fun onAuthenticated(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                    requestDbUnlockWithSavedPassword(cryptoObject)
                }
            })
            fragment.show(supportFragmentManager, "fingerprint_dialog")
        } else if (isFingerprintAvailable() && !prefsHelper.isKeyUnset()) {
            val fragment = FingerprintDialogFragment()
            val args = Bundle()
            args.putString(FingerprintDialogFragment.DIALOG_TITLE_KEY, "Hello there")

            fragment.arguments = args
            fragment.setCipher(cryptoHelper.getCipher(false))
            fragment.setCallback(object : FingerprintDialogFragment.Callback {
                override fun onAuthenticated(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                    cryptoHelper.unlockDbWithKey(cryptoObject)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                startActivity(Intent(this@MainActivity, DiaryListActivity::class.java))
                                finish()
                            },
                                    {

                                    })
                }
            })
            fragment.show(supportFragmentManager, "fingerprint_dialog")
        } else {
            requestDbUnlockWithSavedPassword(null)
        }

    }
    */

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun showFingerprintOption() {
        useFingerprintCheckbox.visibility = View.VISIBLE
    }

    override fun hideFingerprintOption() {
        useFingerprintCheckbox.visibility = View.GONE
    }

    override fun setFingerprintOptionState(checked: Boolean) {
        useFingerprintCheckbox.isChecked = checked
    }

    override fun getFingerprintOptionState(): Boolean {
        return useFingerprintCheckbox.isChecked
    }


    /*
    fun requestDbUnlockWithSavedPassword(cryptoObject: FingerprintManagerCompat.CryptoObject?) {
        passwordButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        cryptoHelper.unlockDbWithPassword(passwordEntry.text.toString().toCharArray(), cryptoObject)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    startActivity(Intent(this, DiaryListActivity::class.java))
                    finish()
                }, { throwable ->
                    Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_LONG).show()
                    passwordButton.isEnabled = true
                    progressBar.visibility = View.GONE
                })
    }
    */


}
