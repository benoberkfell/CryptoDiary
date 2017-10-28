package com.oberkfell.cryptodiary.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.models.DiaryEntry
import io.realm.Realm

class NewEntryActivity : AppCompatActivity() {

    @BindView(R.id.titleInputText)
    lateinit var titleInput: EditText

    @BindView(R.id.entryInputText)
    lateinit var entryInput: EditText

    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)
        ButterKnife.bind(this)

        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_entry, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.saveItem) {
            saveItem()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun saveItem() {
        val titleText = titleInput.text
        val entryText = entryInput.text

        if (titleText.isNullOrBlank() || entryText.isNullOrBlank()) {
            AlertDialog.Builder(this)
                    .setMessage("I can't save this diary entry without both a title and an entry")
                    .setTitle("Incomplete entry")
                    .setPositiveButton("Oops!", { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }).show()
        } else {
            realm.executeTransaction {
                val entry = realm.createObject(DiaryEntry::class.java, DiaryEntry.getPrimaryKey())
                entry.subject = titleText.toString()
                entry.text = entryText.toString()
            }
            finish()
        }
    }
}
