package ru.zipper.godelivery.view.activity.deliveryActivity.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Activity.TELEPHONY_SERVICE
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.zipper.godelivery.R
import ru.zipper.godelivery.databinding.FragmentContactsBinding
import ru.zipper.godelivery.model.models.DeliveryAddress
import ru.zipper.godelivery.utils.Constants
import ru.zipper.godelivery.view.activity.deliveryActivity.fragment.SelectPhoneFragment.Companion.CONTACT_PICK_CODE
import ru.zipper.godelivery.view.adapter.ImageClickListener
import ru.zipper.godelivery.view.adapter.ImagesAdapter
import ru.zipper.godelivery.viewmodel.ContactsViewModel
import ru.zipper.godelivery.viewmodel.DeliveryOrderViewModel
import java.io.File

class ContactFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private var part = 0
    private var image = 0
    private lateinit var orderViewModel: DeliveryOrderViewModel
    private lateinit var contactsViewModel: ContactsViewModel

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ImagesAdapter
    private lateinit var adapter2: ImagesAdapter
    private lateinit var navController: NavController
    private lateinit var photos: MutableList<String>
    private lateinit var photos2: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsBinding.bind(
            inflater.inflate(
                R.layout.fragment_contacts,
                container, false
            )
        )
        val viewModelProvider = ViewModelProvider(requireActivity())
        contactsViewModel = viewModelProvider[ContactsViewModel::class.java]
        orderViewModel = viewModelProvider[DeliveryOrderViewModel::class.java]
        navController = NavHostFragment.findNavController(this)

        photos = contactsViewModel.listPhotos.value!!
        photos2 = contactsViewModel.listPhotos2.value!!
        requestStoragePermission()
        setAddress()
        setBottomPartView()
        getLiveDataBottom()
        getLiveDataTop()
        setOnClickListeners()
        setPriceYellowButton()
        setGrayField()
        setRecyclerView()
        setRecyclerViewBottom()

        return binding.root
    }


    override fun onStart() {
        super.onStart()

        val dialog = requireDialog() as BottomSheetDialog
        dialog.dismissWithAnimation = true

        val behavior = dialog.behavior
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
        val layoutParams = bottomSheet!!.layoutParams

        val displayMetrics = Resources.getSystem().displayMetrics
        val windowHeight =
            (displayMetrics.heightPixels - resources.getDimension(R.dimen.expanded_offset)).toInt()
        if (layoutParams != null) {
            layoutParams.height = windowHeight //WindowManager.LayoutParams.MATCH_PARENT;
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getPhoto(requestCode, resultCode, data, part)
    }

    private fun setGrayField() {
        contactsViewModel.phone.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.topPart.layoutGreyField.visibility = View.INVISIBLE
                binding.topPart.layoutGreyFieldContact.visibility = View.VISIBLE
//                binding.topPart.textViewName.text = contactsViewModel.name.value
                binding.topPart.textViewPhone.text = it

                contactsViewModel.name.observe(viewLifecycleOwner) { name ->
                    binding.topPart.textViewName.text = name
                }
            }
        }
        contactsViewModel.phone2.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.bottomPart.layoutGreyField.visibility = View.INVISIBLE
                binding.bottomPart.layoutGreyFieldContact.visibility = View.VISIBLE
//                binding.bottomPart.textViewName.text = contactsViewModel.name2.value
                binding.bottomPart.textViewPhone.text = it

                contactsViewModel.name2.observe(viewLifecycleOwner) { name ->
                    binding.bottomPart.textViewName.text = name
                }
            }
        }
    }

    private fun setRecyclerView() {
        recycler = binding.topPart.rvImages
        recycler.layoutManager = GridLayoutManager(context, 4)
        adapter = ImagesAdapter(photos)
        adapter.imageListener = ImageClickListener {
            photos.removeAt(it)
            adapter.notifyItemRemoved(it)
        }
        recycler.adapter = adapter
    }

    private fun setRecyclerViewBottom() {
        recycler = binding.bottomPart.rvImages
        recycler.layoutManager = GridLayoutManager(context, 4)
        adapter2 = ImagesAdapter(photos2)
        adapter2.imageListener = ImageClickListener {
            photos2.removeAt(it)
            adapter2.notifyItemRemoved(it)
        }
        recycler.adapter = adapter2
    }

    private fun setPriceYellowButton() {
        val price: Int? = orderViewModel.price.value
        binding.yellowButtonPart.textViewSum.text = price.toString()
        orderViewModel.price.observe(this) {
            binding.yellowButtonPart.textViewSum.text = getString(R.string.rub, it)
        }
    }

    private fun setBottomPartView() {
        binding.bottomPart.tvTakeParcel.text = getString(R.string.give_parcel)
        if (contactsViewModel.phone2.value == null) {
            binding.bottomPart.editTextFromWhom.hint = activity?.getString(R.string.to_whom)
        }
    }

    private fun setOnClickListeners() {
        binding.topPart.imageViewImage.setOnClickListener { _ ->
            if (checkStoragePermission()) {
                pickPhoto()
                part = 1
            } else {
                requestStoragePermission()
            }
        }

        binding.bottomPart.imageViewImage.setOnClickListener { _ ->
            if (checkStoragePermission()) {
                pickPhoto()
                part = 2
            } else {
                requestStoragePermission()
            }
        }

        binding.topPart.layoutGreyField.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToSelectPhoneFragment(1)
                )
        }

        binding.bottomPart.layoutGreyField.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToSelectPhoneFragment(2)
                )
        }

        binding.topPart.layoutGreyFieldContact.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToSelectPhoneFragment(1)
                )
        }

        binding.bottomPart.layoutGreyFieldContact.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToSelectPhoneFragment(2)
                )
        }

        binding.topPart.textViewAddress.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToDeliveryAddressFragment(true)
                )
        }
        binding.bottomPart.textViewAddress.setOnClickListener {
            navController
                .navigate(
                    ContactFragmentDirections.actionContactFragmentToDeliveryAddressFragment(false)
                )
        }

        binding.yellowButtonPart.imageViewYellowButton.setOnClickListener {
            navController
                .navigate(ContactFragmentDirections.actionContactFragmentToCourierSearchFragment())
        }
        binding.yellowButtonPart.imageViewDetails.setOnClickListener {
            navController
                .navigate(ContactFragmentDirections.actionContactFragmentToOptionsFragment())
        }
    }

    private fun getLiveDataTop() {
        binding.topPart.editTextGate.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.gate =
                MutableLiveData<String>(binding.topPart.editTextGate.text.toString())
        }

        binding.topPart.editTextFlat.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.flat =
                MutableLiveData<String>(binding.topPart.editTextFlat.text.toString())
        }
        binding.topPart.editTextLevel.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.level =
                MutableLiveData<String>(binding.topPart.editTextLevel.text.toString())
        }
        binding.topPart.editTextCode.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.code =
                MutableLiveData<String>(binding.topPart.editTextCode.text.toString())
        }
        binding.topPart.editTextComment.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.comment =
                MutableLiveData<String>(binding.topPart.editTextComment.text.toString())
        }
        contactsViewModel.gate.observe(this, Observer { it ->
            binding.topPart.editTextGate.setText(it)
        })
        contactsViewModel.flat.observe(this, Observer { it ->
            binding.topPart.editTextFlat.setText(it)
        })
        contactsViewModel.level.observe(this, Observer { it ->
            binding.topPart.editTextLevel.setText(it)
        })
        contactsViewModel.code.observe(this, Observer { it ->
            binding.topPart.editTextCode.setText(it)
        })
        contactsViewModel.comment.observe(this, Observer { it ->
            binding.topPart.editTextComment.setText(it)
        })
    }

    private fun getLiveDataBottom() {
        binding.bottomPart.editTextGate.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.gate2 =
                MutableLiveData<String>(binding.bottomPart.editTextGate.text.toString())
        }
        binding.bottomPart.editTextFlat.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.flat2 =
                MutableLiveData<String>(binding.bottomPart.editTextFlat.text.toString())
        }
        binding.bottomPart.editTextLevel.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.level2 =
                MutableLiveData<String>(binding.bottomPart.editTextLevel.text.toString())
        }
        binding.bottomPart.editTextCode.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.code2 =
                MutableLiveData<String>(binding.bottomPart.editTextCode.text.toString())
        }
        binding.bottomPart.editTextComment.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.comment2 =
                MutableLiveData<String>(binding.bottomPart.editTextComment.text.toString())
        }

        contactsViewModel.gate2.observe(this, Observer { it ->
            binding.bottomPart.editTextGate.setText(it)
        })
        contactsViewModel.flat2.observe(this, Observer { it ->
            binding.bottomPart.editTextFlat.setText(it)
        })
        contactsViewModel.level2.observe(this, Observer { it ->
            binding.bottomPart.editTextLevel.setText(it)
        })
        contactsViewModel.code2.observe(this, Observer { it ->
            binding.bottomPart.editTextCode.setText(it)
        })
        contactsViewModel.comment2.observe(this, Observer { it ->
            binding.bottomPart.editTextComment.setText(it)
        })
    }

    fun View.hideKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setAddress() {
        val startDeliveryAddress: DeliveryAddress? = orderViewModel.startDeliveryAddress.value
        val endDeliveryAddress: DeliveryAddress? = orderViewModel.endDeliveryAddress.value

        binding.topPart.textViewAddress.text = startDeliveryAddress?.deliveryAddress.toString()

        binding.bottomPart.textViewAddress.text = endDeliveryAddress?.deliveryAddress.toString()

            orderViewModel.startDeliveryAddress.observe(this, Observer { it ->
                binding.topPart.textViewAddress.text = it.deliveryAddress
            })
            orderViewModel.endDeliveryAddress.observe(this, Observer { it ->
                binding.bottomPart.textViewAddress.text = it.deliveryAddress
            })
        }

    private fun getPhoto(requestCode: Int, resultCode: Int, data: Intent?, part: Int) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                binding.topPart.rvImages.visibility = View.VISIBLE

                val cursor: Cursor
                val uri = data!!.data
                cursor = context?.contentResolver?.query(
                    uri!!,
                    arrayOf(MediaStore.Images.Media.DATA), null, null, null
                )!!
                if (cursor.moveToFirst()) {
                    val photoUri =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    when (part) {
                        1 -> {
                            photos.add(photoUri)
                            adapter.notifyItemInserted(photos.size)
                        }
                        2 -> {
                            photos2.add(photoUri)
                            adapter2.notifyItemInserted(photos2.size)
                        }
                    }
                }
                cursor.close()
            }
        }
    }


    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return true
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data =
                        Uri.parse(
                            String.format(
                                "package:%s",
                                requireActivity().applicationContext.packageName
                            )
                        )
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivity(intent)
                }
            }
        } else {
            val grant = ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (grant != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.PERMISSION_REQUEST_CODE_READ_STORAGE
                )
            }
        }
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickPhoto()
            }
        }
    }

    companion object {
        private const val PICK_IMAGE = 1
        private const val STORAGE_PERMISSION_CODE = 101
    }
}