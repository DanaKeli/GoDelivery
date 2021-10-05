package ru.zipper.godelivery.view.activity.deliveryActivity.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.zipper.godelivery.R
import ru.zipper.godelivery.databinding.FragmentSelectPhoneBinding
import ru.zipper.godelivery.utils.Constants
import ru.zipper.godelivery.viewmodel.ContactsViewModel

class SelectPhoneFragment : BottomSheetDialogFragment() {

    private val args: SelectPhoneFragmentArgs by navArgs()
    private var part: Int = 0

    private var _binding: FragmentSelectPhoneBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactsViewModel: ContactsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        part = args.part
        _binding = FragmentSelectPhoneBinding.bind(
            inflater.inflate(
                R.layout.fragment_select_phone,
                container, false
            )
        )
        val viewModelProvider = ViewModelProvider(requireActivity())
        contactsViewModel = viewModelProvider[ContactsViewModel::class.java]

        setOnClickListeners()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val dialog = requireDialog() as BottomSheetDialog
        dialog.dismissWithAnimation = true
        val behavior = dialog.behavior
        val bottomSheet = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        val layoutParams = bottomSheet!!.layoutParams
        if (layoutParams != null) {
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    override fun getTheme(): Int {
        return R.style.ModalBottomSheetDialogWhite
    }


    var contactActivityLauncher = registerForActivityResult(StartActivityForResult()) {
        result: ActivityResult ->
        when (part) {
            1 -> addContactTop(CONTACT_PICK_CODE, result.resultCode, result.data)
            2 -> addContactBottom(CONTACT_PICK_CODE, result.resultCode, result.data)
        }
        NavHostFragment.findNavController(this).popBackStack()
    }

    private fun setOnClickListeners() {

        binding.editTextNameOrPhone.addTextChangedListener {
            binding.textViewContacts.visibility = View.INVISIBLE
            binding.namePhoneContainer.visibility = View.INVISIBLE
            binding.imageViewVerticalLine3.visibility = View.INVISIBLE
            binding.imageViewClose2.visibility = View.VISIBLE
            binding.yellowButtonContainer.visibility = View.VISIBLE
        }
        binding.editTextNameOrPhone.setOnClickListener {
            binding.textViewContacts.visibility = View.INVISIBLE
            binding.namePhoneContainer.visibility = View.INVISIBLE
            binding.imageViewVerticalLine3.visibility = View.INVISIBLE
            binding.imageViewClose2.visibility = View.VISIBLE
            binding.yellowButtonContainer.visibility = View.VISIBLE
        }

        binding.imageViewClose.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        binding.namePhoneContainer.setOnClickListener {
            when (part) {
                1 -> {
                    contactsViewModel.phone.value = binding.textViewMyPhone.text.toString()
                    contactsViewModel.name.value = binding.textViewMe.text.toString()
                }
                2 -> {
                    contactsViewModel.phone2.value = binding.textViewMyPhone.text.toString()
                    contactsViewModel.name2.value = binding.textViewMe.text.toString()
                }
            }
            NavHostFragment.findNavController(this).popBackStack()
        }
        binding.imageViewClose2.setOnClickListener {
            binding.editTextNameOrPhone.text.clear()
            binding.textViewContacts.visibility = View.VISIBLE
            binding.namePhoneContainer.visibility = View.VISIBLE
            binding.imageViewVerticalLine3.visibility = View.VISIBLE
            binding.imageViewClose2.visibility = View.INVISIBLE
            binding.yellowButtonContainer.visibility = View.INVISIBLE
        }
        binding.yellowButtonContainer.setOnClickListener {
            val name: String = binding.editTextNameOrPhone.text.filter { !it.isDigit() }.toString()
            val phone: String = binding.editTextNameOrPhone.text.filter { it.isDigit() }.toString()
            when (part) {
                1 -> {
                    contactsViewModel.phone.value = phone
                    contactsViewModel.name.value = name
                }
                2 -> {
                    contactsViewModel.phone2.value = phone
                    contactsViewModel.name2.value = name
                }
            }
            NavHostFragment.findNavController(this).popBackStack()
        }

        binding.textViewContacts.setOnClickListener {
            if (checkContactPermission()) {
                sendContact()
            } else {
                requestContactPermission()
            }
        }
    }

    private fun addContactTop(requestCode: Int, resultCode: Int, data: Intent?) {
        var requestCode: Int = requestCode
        var resultCode = resultCode
        var data = data

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CONTACT_PICK_CODE) {
                val cursor1: Cursor
                val cursor2: Cursor?
                val uri = data!!.data
                cursor1 = context?.contentResolver?.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    val contactId =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val idResults =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()
                    contactsViewModel.name.value = contactName
                    if (idResultHold == 1) {
                        cursor2 = context?.contentResolver?.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null
                        )
                        while (cursor2!!.moveToNext()) {
                            val contactNumber = cursor2.getString(
                                cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            contactsViewModel.phone.value = contactNumber

                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }
    }

    private fun addContactBottom(requestCode: Int, resultCode: Int, data: Intent?) {
        var requestCode: Int = requestCode
        var resultCode = resultCode
        var data = data

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CONTACT_PICK_CODE) {
                val cursor1: Cursor
                val cursor2: Cursor?
                val uri = data!!.data
                cursor1 = context?.contentResolver?.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    val contactId =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val idResults =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()
                    contactsViewModel.name2.value = contactName
                    if (idResultHold == 1) {
                        cursor2 = context?.contentResolver?.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null
                        )
                        while (cursor2!!.moveToNext()) {
                            val contactNumber = cursor2.getString(
                                cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            contactsViewModel.phone2.value = contactNumber

                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }
    }

    private fun checkContactPermission(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(
                it.applicationContext,
                Manifest.permission.READ_CONTACTS
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermission() {
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(
            activity as Activity, permission,
            CONTACT_PERMISSION_CODE
        )
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NavHostFragment.findNavController(this).popBackStack()
            }
        }
    }

    private fun sendContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        contactActivityLauncher.launch(intent)
    }


    companion object {
        const val CONTACT_PERMISSION_CODE = 1
        const val CONTACT_PICK_CODE = 2
    }
}