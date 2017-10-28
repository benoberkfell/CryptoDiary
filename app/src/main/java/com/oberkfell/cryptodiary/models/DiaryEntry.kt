package com.oberkfell.cryptodiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class DiaryEntry: RealmObject() {

    @PrimaryKey
    var id: String = ""

    var date: Date = Date()

    var subject: String = ""

    var text: String = ""

    companion object {
        fun getPrimaryKey() : String {
            return UUID.randomUUID().toString()
        }
    }

}