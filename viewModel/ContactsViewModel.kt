package ru.zipper.godelivery.viewmodel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.zipper.godelivery.model.models.ContactsItem
import ru.zipper.godelivery.view.activity.deliveryActivity.fragment.ContactFragment
import javax.inject.Inject


class ContactsViewModel : ViewModel() {

    var gate: MutableLiveData<String> = MutableLiveData()
    var flat: MutableLiveData<String> = MutableLiveData()
    var level: MutableLiveData<String> = MutableLiveData()
    var code: MutableLiveData<String> = MutableLiveData()
    var comment: MutableLiveData<String> = MutableLiveData()
    var name: MutableLiveData<String> = MutableLiveData()
    var phone: MutableLiveData<String> = MutableLiveData()
    var listPhotos: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    var shopName: MutableLiveData<String> = MutableLiveData()
    var price: MutableLiveData<Int> = MutableLiveData()

    var gate2: MutableLiveData<String> = MutableLiveData()
    var flat2: MutableLiveData<String> = MutableLiveData()
    var level2: MutableLiveData<String> = MutableLiveData()
    var code2: MutableLiveData<String> = MutableLiveData()
    var comment2: MutableLiveData<String> = MutableLiveData()
    var name2: MutableLiveData<String> = MutableLiveData()
    var phone2: MutableLiveData<String> = MutableLiveData()
    var listPhotos2: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
}