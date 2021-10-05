package ru.zipper.godelivery.model.models

data class ContactsItem(
    var gate: String,
    var flat: String,
    var level: String,
    var code: String,
    var name: String,
    var phone: String,
    var comment: String,
    var photos: MutableList<String>
)