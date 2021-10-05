package ru.zipper.godelivery.view.activity.deliveryActivity.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.zipper.godelivery.R
import ru.zipper.godelivery.databinding.FragmentShopBinding
import ru.zipper.godelivery.model.models.DeliveryAddress
import ru.zipper.godelivery.utils.Constants
import ru.zipper.godelivery.view.adapter.DividerItemDecoration
import ru.zipper.godelivery.view.adapter.ImageClickListener
import ru.zipper.godelivery.view.adapter.ImagesAdapter
import ru.zipper.godelivery.view.adapter.ShopListAdapter
import ru.zipper.godelivery.view.adapter.ShopListDividerItemDecoration
import ru.zipper.godelivery.viewmodel.ContactsViewModel
import ru.zipper.godelivery.viewmodel.DeliveryOrderViewModel


class ShopFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    private var part = 0
    private lateinit var orderViewModel: DeliveryOrderViewModel
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var adapter: ShopListAdapter
    private lateinit var adapterImage: ImagesAdapter
    private lateinit var photos: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShopBinding.bind(
            inflater.inflate(
                R.layout.fragment_shop,
                container, false
            )
        )
        val viewModelProvider = ViewModelProvider(requireActivity())
        contactsViewModel = viewModelProvider[ContactsViewModel::class.java]
        orderViewModel = viewModelProvider[DeliveryOrderViewModel::class.java]
        photos = contactsViewModel.listPhotos2.value!!
        setAddressShop()
        setPriceYellowButton()
        setContactPartView()
        setOnClickListeners()
        getLiveDataShop()
        getLiveDataBottom()
        setGrayField()
        initRecyclerView()
        setAlcoCheckView()
        setRecyclerViewContact()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getPhoto(requestCode, resultCode, data)
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
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
        val layoutParams = bottomSheet!!.layoutParams

        val displayMetrics = Resources.getSystem().displayMetrics
        val windowHeight =
            (displayMetrics.heightPixels - resources.getDimension(R.dimen.expanded_offset)).toInt()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }


    private fun initRecyclerView() {
        val rvGoods = binding.shopPart.rvGoods
        rvGoods.addItemDecoration(ShopListDividerItemDecoration(requireActivity()))
        rvGoods.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        adapter = ShopListAdapter(orderViewModel.goods.value)
        rvGoods.adapter = adapter
    }

    private fun setRecyclerViewContact() {
        val recycler = binding.contactPart.rvImages
        recycler.layoutManager = GridLayoutManager(context, 4)
        adapterImage = ImagesAdapter(photos)
        adapterImage.imageListener = ImageClickListener {
            photos.removeAt(it)
            adapter.notifyItemRemoved(it)
        }
        recycler.adapter = adapterImage
    }

    private fun setContactPartView() {
        binding.contactPart.tvTakeParcel.text = getString(R.string.give_parcel)
        if (contactsViewModel.phone2.value == null) {
            binding.contactPart.editTextFromWhom.hint = activity?.getString(R.string.to_whom)
        }
    }

    private fun getLiveDataShop() {

        binding.shopPart.editTextShopName.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.shopName =
                MutableLiveData<String>(binding.shopPart.editTextShopName.text.toString())
        }
        binding.shopPart.editTextComment1.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.comment =
                MutableLiveData<String>(binding.shopPart.editTextComment1.text.toString())
        }
        binding.shopPart.editTextCost.doOnTextChanged { _, _, _, _ ->
            if (binding.shopPart.editTextCost.text.toString().toInt() > 5000) {
                Toast.makeText(context, "Не больше 5000 ₽", Toast.LENGTH_SHORT).show()
            } else {
                contactsViewModel.price =
                    MutableLiveData<Int>(binding.shopPart.editTextCost.text.toString().toInt())
            }
        }
        contactsViewModel.shopName.observe(this, Observer { it ->
            binding.shopPart.editTextShopName.setText(it)
        })
        contactsViewModel.comment.observe(this, Observer { it ->
            binding.shopPart.editTextComment1.setText(it)
        })
        contactsViewModel.price.observe(this, Observer { it ->
            binding.shopPart.editTextCost.setText(it)
        })
    }

    private fun getLiveDataBottom() {

        binding.contactPart.editTextGate.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.gate2 =
                MutableLiveData<String>(binding.contactPart.editTextGate.text.toString())
        }
        binding.contactPart.editTextFlat.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.flat2 =
                MutableLiveData<String>(binding.contactPart.editTextFlat.text.toString())
        }
        binding.contactPart.editTextLevel.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.level2 =
                MutableLiveData<String>(binding.contactPart.editTextLevel.text.toString())
        }
        binding.contactPart.editTextCode.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.code2 =
                MutableLiveData<String>(binding.contactPart.editTextCode.text.toString())
        }
        binding.contactPart.editTextComment.doOnTextChanged { _, _, _, _ ->
            contactsViewModel.comment2 =
                MutableLiveData<String>(binding.contactPart.editTextComment.text.toString())
        }

        contactsViewModel.gate2.observe(this, Observer { it ->
            binding.contactPart.editTextGate.setText(it)
        })
        contactsViewModel.flat2.observe(this, Observer { it ->
            binding.contactPart.editTextFlat.setText(it)
        })
        contactsViewModel.level2.observe(this, Observer { it ->
            binding.contactPart.editTextLevel.setText(it)
        })
        contactsViewModel.code2.observe(this, Observer { it ->
            binding.contactPart.editTextCode.setText(it)
        })
        contactsViewModel.comment2.observe(this, Observer { it ->
            binding.contactPart.editTextComment.setText(it)
        })
    }


    private fun setGrayField() {
        contactsViewModel.phone2.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.contactPart.layoutGreyField.visibility = View.INVISIBLE
                binding.contactPart.layoutGreyFieldContact.visibility = View.VISIBLE
                binding.contactPart.textViewPhone.text = it

                contactsViewModel.name2.observe(viewLifecycleOwner) { name ->
                    binding.contactPart.textViewName.text = name
                }
            }
        }
    }

    private fun setAlcoCheckView() {
        orderViewModel.isPassportVerified.observe(viewLifecycleOwner) {
            if (it) {
                binding.shopPart.textViewAlcohol.text = "Идентификация пройдена"
                binding.shopPart.textViewAlcohol.isClickable = false
            }
        }
    }

    private fun setPriceYellowButton() {
        val price: Int? = orderViewModel.price.value
        binding.yellowButtonPart.textViewSum.text = price.toString()
        orderViewModel.price.observe(this, Observer { it ->
            binding.yellowButtonPart.textViewSum.text = "$it ₽"
        })
    }

    private fun setOnClickListeners() {
        binding.shopPart.imageViewAddButton.setOnClickListener {
            orderViewModel.goods.value!!.add("")
            adapter.notifyItemInserted(orderViewModel.goods.value!!.size)
        }


            binding.shopPart.textViewAlcohol.setOnClickListener {
                NavHostFragment.findNavController(this)
                    .navigate(ShopFragmentDirections.actionShopFragmentToPassportFragment())
            }

        binding.contactPart.imageViewImage.setOnClickListener { _ ->
            if (checkStoragePermission()) {
                pickPhoto()
            } else {
                requestStoragePermission()
            }
        }

        binding.contactPart.layoutGreyField.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(
                    ShopFragmentDirections.actionShopFragmentToSelectPhoneFragment(2)
                )
        }

        binding.contactPart.layoutGreyFieldContact.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(
                    ShopFragmentDirections.actionShopFragmentToSelectPhoneFragment(2)
                )
        }

        binding.shopPart.textViewAddress1.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(
                    ShopFragmentDirections.actionShopFragmentToDeliveryAddressFragment(true)
                )
        }
        binding.contactPart.textViewAddress.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(
                    ShopFragmentDirections.actionShopFragmentToDeliveryAddressFragment(false)
                )
        }
        binding.yellowButtonPart.imageViewYellowButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(ShopFragmentDirections.actionShopFragmentToCourierSearchFragment())
        }
        binding.yellowButtonPart.imageViewDetails.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(ShopFragmentDirections.actionShopFragmentToOptionsFragment())
        }
    }

    private fun setAddressShop() {
        val viewModelProvider = ViewModelProvider(requireActivity())
        orderViewModel = viewModelProvider[DeliveryOrderViewModel::class.java]

        val startDeliveryAddress: DeliveryAddress? = orderViewModel.startDeliveryAddress.value
        val endDeliveryAddress: DeliveryAddress? = orderViewModel.endDeliveryAddress.value

        binding.shopPart.textViewAddress1.text = startDeliveryAddress?.deliveryAddress.toString()
        binding.contactPart.textViewAddress.text = endDeliveryAddress?.deliveryAddress.toString()

        orderViewModel.startDeliveryAddress.observe(this, Observer { it ->
            binding.shopPart.textViewAddress1.text = it.deliveryAddress
        })
        orderViewModel.endDeliveryAddress.observe(this, Observer { it ->
            binding.contactPart.textViewAddress.text = it.deliveryAddress
        })
    }


    private fun getPhoto(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                val cursor: Cursor
                val uri = data!!.data
                cursor = context?.contentResolver?.query(
                    uri!!,
                    arrayOf(MediaStore.Images.Media.DATA), null, null, null
                )!!
                if (cursor.moveToFirst()) {
                    val photoUri =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    photos.add(photoUri)
                    adapterImage.notifyItemInserted(photos.size)
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
