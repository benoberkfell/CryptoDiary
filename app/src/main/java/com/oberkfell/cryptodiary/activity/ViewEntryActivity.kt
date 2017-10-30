package com.oberkfell.cryptodiary.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.models.DiaryEntry
import com.oberkfell.cryptodiary.modules.ActivityModule
import io.realm.Realm
import java.text.SimpleDateFormat
import javax.inject.Inject

class ViewEntryActivity : AppCompatActivity() {

    lateinit var realm: Realm

    @BindView(R.id.entryTitleTextView)
    lateinit var entryTitleTextView: TextView

    @BindView(R.id.entryDateTextView)
    lateinit var entryDateTextView: TextView

    @BindView(R.id.entryTextView)
    lateinit var entryTextView: TextView

    @Inject
    lateinit var dateFormatterThreadLocal: ThreadLocal<SimpleDateFormat>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_entry)
        ButterKnife.bind(this)

        (application as CryptoDiaryApplication)
                .component
                .activityComponent(ActivityModule(this))
                .inject(this)

        realm = Realm.getDefaultInstance()
    }

    override fun onResume() {
        super.onResume()
        val itemId = intent.getStringExtra(EXTRA_ENTRY_ID)

        val entry = realm.where(DiaryEntry::class.java).equalTo("id", itemId).findFirst() as DiaryEntry

        entryTitleTextView.text = entry.subject
        entryDateTextView.text = dateFormatterThreadLocal.get().format(entry.date)
        entryTextView.text = entry.text
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    companion object {
        private val EXTRA_ENTRY_ID = "EXTRA_ENTRY_ID"

        fun launchIntent(context: Context, entryId: String) : Intent {
            val intent = Intent(context, ViewEntryActivity::class.java)
            intent.putExtra(EXTRA_ENTRY_ID, entryId)
            return intent
        }
    }
}
