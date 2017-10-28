package com.oberkfell.cryptodiary.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.oberkfell.cryptodiary.R
import com.oberkfell.cryptodiary.models.DiaryEntry
import com.oberkfell.cryptodiary.modules.ActivityModule
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DiaryListActivity : AppCompatActivity() {

    @BindView(R.id.recyclerView)
    lateinit var recyclerView: RecyclerView

    private lateinit var realm: Realm

    @Inject
    lateinit var dateFormatterThreadLocal: ThreadLocal<SimpleDateFormat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_list)
        ButterKnife.bind(this)

        (application as CryptoDiaryApplication)
                .component
                .activityComponent(ActivityModule(this))
                .inject(this)

        realm = Realm.getDefaultInstance()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DiaryEntryAdapter(realm.where(DiaryEntry::class.java).findAll())
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    @OnClick(R.id.floatingActionButton)
    fun fabClicked() {
        startActivity(Intent(this, NewEntryActivity::class.java))
    }

    inner class DiaryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val entryTitleTextView : TextView = itemView.findViewById(R.id.entryTitleTextView)
        private val entryDateTextView : TextView = itemView.findViewById(R.id.entryDateTextView)

        fun applyId(id: String) {
            itemView.setOnClickListener {
                val intent = ViewEntryActivity.launchIntent(context = itemView.context, entryId = id)
                itemView.context.startActivity(intent)
            }
        }

        fun applyTitle(title: String) {
            entryTitleTextView.text = title
        }

        fun applyDate(date: Date) {
            entryDateTextView.text = dateFormatterThreadLocal.get().format(date)
        }
    }

    inner class DiaryEntryAdapter(list: OrderedRealmCollection<DiaryEntry>) :
            RealmRecyclerViewAdapter<DiaryEntry, DiaryItemViewHolder>(list, true) {

        override fun onBindViewHolder(holder: DiaryItemViewHolder, position: Int) {
            val item = getItem(position) as DiaryEntry
            holder.applyDate(item.date)
            holder.applyTitle(item.subject)
            holder.applyId(item.id)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.diary_entry_item_view, parent, false)
            return DiaryItemViewHolder(view)
        }
    }
}
