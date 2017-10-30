package com.oberkfell.cryptodiary.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.helpers.CryptoHelper
import com.oberkfell.cryptodiary.modules.ActivityModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmConfiguration
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var cryptoHelper: CryptoHelper

    @BindView(R.id.passwordEntry)
    lateinit var passwordEntry: EditText

    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.passwordButton)
    lateinit var passwordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        (application as CryptoDiaryApplication)
                .component
                .activityComponent(ActivityModule(this))
                .inject(this)

    }

    @OnClick(R.id.passwordButton)
    fun onClickPasswordButton() {
        if (!passwordEntry.text.isNullOrEmpty()) {
            passwordButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            cryptoHelper.unlockDbWithPassword(passwordEntry.text.toString().toCharArray())
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
        passwordEntry.setText("")
    }

}
