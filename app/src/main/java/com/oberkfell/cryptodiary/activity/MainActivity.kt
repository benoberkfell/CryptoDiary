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
        val text = passwordEntry.text
        if (text.isNotEmpty()) {
            val password = CharArray(text.length)
            text.getChars(0, text.length, password, 0)
            presenter.attemptUnlockWithPassword(password)
            text.clear()
        } else {
            Toast.makeText(this, "You need to enter a password.", Toast.LENGTH_LONG).show()
        }
    }

    override fun launchFingerprintDialog(title: String, cipher: Cipher) : Observable<CryptoHelper> {
        val observable : Observable<CryptoHelper> = Observable.create { emitter ->
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

}
