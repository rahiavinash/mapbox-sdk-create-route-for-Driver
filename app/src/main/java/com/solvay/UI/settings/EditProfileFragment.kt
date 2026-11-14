package com.solvay.UI.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.solvay.Base.BaseFragment
import com.solvay.BuildConfig
import com.solvay.HomesActivity
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.R
import com.solvay.UI.settings.bottomsheet.UploadImageBottomSheet
import com.solvay.UI.settings.dataModels.ProfileData
import com.solvay.UI.settings.dataModels.ProfileImageUpdation
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.DialogUtils
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.disableMultiTap
import com.solvay.Utils.Utils.loadImageMessage
import com.solvay.adapters.ClickListener
import com.solvay.adapters.LanguageRecyclerAdapter
import com.solvay.authentication.AuthenticationActivity
import com.solvay.authentication.models.LanguageDataModel
import com.solvay.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.InputStream
import java.lang.reflect.Type

class EditProfileFragment : BaseFragment(), ClickListener {
    private lateinit var mBinding: FragmentEditProfileBinding
    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var alertManager: AlertManager
    private var languageList =  ArrayList<LanguageDataModel>()
    var userName: String? = null
    var userLanguage: String? = null
    var selectedLanguage: String? = null
    var selectedLanguageCode: String? = null
    var selectedLanguageValue: String? = null
    var userPlateNumber: String? = null
    var userContractNumber: String? = null
    var userImage: String? = null
    var hasNameChanged: Boolean = false
    var hasPlateNumberChanged: Boolean = false
    var hasContractNumberChanged: Boolean = false
    var hasImageChanged: Boolean = false
    var hasLanguageChanged: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentEditProfileBinding.inflate(localizedInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            hideDropDown()
            if(hasNameChanged || hasPlateNumberChanged || hasLanguageChanged){
                moveBack()
            }else{
                Handler().post {
                    findNavController().navigateUp()
                }
            }
        }

        mBinding.llBack.setOnClickListener {
            hideDropDown()
            if(hasNameChanged || hasPlateNumberChanged || hasLanguageChanged){
                moveBack()
            }else{
                findNavController().navigateUp()
            }
        }

        mBinding.etAuftrag.setOnClickListener{
            hideKeyboard()
            alertManager.showError(getString(R.string.contract_number_edit_disabled))
        }
        mBinding.ivUserProfileImage.setOnClickListener{
            hideDropDown()
            selectCaptureImage()
        }
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        mBinding.btNext.isEnabled = false
        setupTouchListenerToHideKeyboard(view)
        mBinding.clLanguage.setOnClickListener {
            toggleDropDownVisibility()
        }
        mBinding.apply {
            etName.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etPlate.requestFocus()
                    true
                } else {
                    false
                }
            }
            btNext.setOnClickListener {
                hideDropDown()
                val name = etName.text.toString().trim()
                val contractNumber = etAuftrag.text.toString().trim()
                val plateNumber = etPlate.text.toString().trim()
                val langugage = tvLanguage1.text.toString().trim()
                if (validateInputs(firstName = name?:"", plateNumber = plateNumber?:"", aufTragNumber = contractNumber?:"", language = langugage?:"" )){
                    if(hasLanguageChanged){
                        changeLanguageDialog(firstName = name?:"", plateNumber = plateNumber?:"", aufTragNumber = contractNumber?:"", language = langugage?:"")
                    }else{
                        userName = name?.trim()
                        userPlateNumber = plateNumber?.trim()
                        userContractNumber = contractNumber?.trim()
                        userLanguage = langugage?.trim()
                        updateProfile()
                    }

                }
            }
            etPlate.setOnFocusChangeListener { view, b ->
                if (b){
                    hideDropDown()
                }
            }
            etName.setOnFocusChangeListener{view, b ->
                if (b){
                    hideDropDown()
                }
            }
            etAuftrag.setOnFocusChangeListener{view, b ->
                if (b){
                    hideDropDown()
                    hideKeyboard()
                    alertManager.showError(getString(R.string.contract_number_edit_disabled))
                }
            }
        }
        mBinding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (mBinding.llLanguageList.visibility == View.VISIBLE) {
                    val rvRect = Rect()
                    mBinding.llLanguageList.getGlobalVisibleRect(rvRect)

                    if (!rvRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        toggleDropDownVisibility()
                    }
                }
            }
            false
        }
        mBinding.clRoot.setOnClickListener {
            if (mBinding.llLanguageList.visibility == View.VISIBLE) {
                toggleDropDownVisibility()
            }
        }
        hideNavBottom()
        setupRecycler()
        getUserProfile()
        observer()
        textChangeListeners()
    }

    fun hideDropDown(){
        if (mBinding.llLanguageList.visibility == View.VISIBLE) {
            toggleDropDownVisibility()
        }
    }

    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
    }

    fun getUserProfile(){
        showProgress(true)
        viewModel.getProfile()
    }

    fun updateProfile(){
        val data = ProfileData(
            name = userName,
            plateNumber = userPlateNumber,
            contractNumber = userContractNumber,
            language = selectedLanguage
        )
        showProgress(true)
        viewModel.updateProfile(data, requireContext())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateImageApi(event: ProfileImageUpdation?) {
        Log.d("FragmentLifecycleCheck","$event")
        showProgress(true)
        viewModel.updateProfile(ProfileData(imageUri = event?.imageUri), requireContext())
    }

    fun observer(){
        viewModel.userProfile.observe(viewLifecycleOwner, EventObserver{
            showProgress(false)
            userProfileObserver(it)
        })

        viewModel.updateProfileResponse.observe(viewLifecycleOwner, EventObserver{
            showProgress(false)
            updateProfileObserver(it)
        })
    }

    private fun userProfileObserver(response: ProfileResponse){
        if (response.success == true){
            response.data?.apply {
                userName = name
                userLanguage = language
                userPlateNumber = plateNumber
                userContractNumber = contractNumber
                userImage = image
                updateUi()
            }
        }else{
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(response.message?:baseContext.getString(R.string.something_went_wrong))
        }
    }

    private fun updateProfileObserver(data: ProfileResponse){
        if (data.success == true){
            alertManager.showSuccess(data.message?:"")
                if (userImage != data.data?.image){
                    userImage = data.data?.image
                    CoroutineScope(Dispatchers.IO).launch {
                        mBinding.ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL+userImage)
                    }
                }
                if (userName != data.data?.name){
                    userName = data.data?.name
                    mBinding.tvName.text = userName
                }
                if (userPlateNumber != data.data?.plateNumber){
                    userPlateNumber = data.data?.plateNumber
                    mBinding.tvPlate.text = userPlateNumber
                }
                if (userContractNumber != data.data?.contractNumber){
                    userContractNumber = data.data?.contractNumber
                    mBinding.tvAuftrag.text = userContractNumber
                }
                if (hasLanguageChanged == true){
                    userLanguage = data.data?.language
                    mBinding.tvLanguage1.text = languageList.firstOrNull { it.value?.equals(userLanguage, ignoreCase = true) == true }?.name
                    selectedLanguageCode = languageList.firstOrNull {
                        it.name?.trim()?.lowercase() == userLanguage?.trim()?.lowercase()
                    }?.code
                    Prefs.init().appLanguageValue = userLanguage?:"german"
                    Prefs.init().appLanguageCode = selectedLanguageCode?:"de"
                    Prefs.init().appLanguage = mBinding.tvLanguage1.text.toString()
                    Handler( Looper.getMainLooper()).post {
                        MainApplication.get().applicationContext.startActivity(
                            Intent(
                                MainApplication.get().applicationContext,
                                HomesActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                }else{
                    Handler().postDelayed({
                        findNavController().navigateUp()
                    }, 500)
                }
            hasNameChanged = false
            hasLanguageChanged = false
            hasContractNumberChanged = false
            hasPlateNumberChanged = false
        }else{
            alertManager.showError(data.message?:"")
        }
    }
    private fun updateUi(){
        mBinding.apply {
            etName.setText(userName)
            tvLanguage1.text = languageList.firstOrNull { it.value?.equals(userLanguage, ignoreCase = true) == true }?.name?:userLanguage
            etPlate.setText(userPlateNumber)
            etAuftrag.setText(userContractNumber)
            CoroutineScope(Dispatchers.IO).launch {
                ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL+userImage)
            }
            if (!userLanguage.isNullOrEmpty()){
                mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
            }else{
                mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_secondary))
            }
        }
    }

    private fun textChangeListeners(){
        mBinding.apply {
            etName.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.toString().equals(userName)){
                        hasNameChanged = true
                        btNext.isEnabled = true
                    }else{
                        hasNameChanged = false
                        if (hasPlateNumberChanged || hasContractNumberChanged || hasLanguageChanged || hasImageChanged){
                            btNext.isEnabled = true
                        }else{
                            btNext.isEnabled = false
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })

            etPlate.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.toString().equals(userPlateNumber)){
                        hasPlateNumberChanged = true
                        btNext.isEnabled = true
                    }else{
                        hasPlateNumberChanged = false
                        if (hasNameChanged || hasContractNumberChanged || hasLanguageChanged || hasImageChanged){
                            btNext.isEnabled = true
                        }else{
                            btNext.isEnabled = false
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })

            etAuftrag.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.toString().equals(userContractNumber)){
                        hasContractNumberChanged = true
                        btNext.isEnabled = true
                    }else{
                        hasContractNumberChanged = false
                        if (hasNameChanged || hasPlateNumberChanged || hasLanguageChanged || hasImageChanged){
                            btNext.isEnabled = true
                        }else{
                            btNext.isEnabled = false
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
        }
    }

    private fun toggleDropDownVisibility() {
        val isVisible = mBinding.llLanguageList.visibility == View.VISIBLE
        mBinding.llLanguageList.visibility = if (isVisible) View.GONE else View.VISIBLE

        mBinding.imageView3.animate()
            .rotation(if (isVisible) 0f else 180f)
            .setDuration(200)
            .start()
    }
    private fun setupRecycler() {
        mBinding.apply {
            languageList =  ArrayList(getLanguages().sortedBy { it.name })
            rvLanguages.adapter =
                LanguageRecyclerAdapter(
                    list = languageList,
                    clickListener = this@EditProfileFragment
                )
        }
    }
    private fun readJSONFromAsset(path: String): String? {
        val json: String
        try {
            val inputStream: InputStream = requireContext().assets.open(path)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }
    private fun getLanguages(): List<LanguageDataModel> {
        val response = readJSONFromAsset("languages.json")
        val listType: Type = object : TypeToken<ArrayList<LanguageDataModel>>() {}.type
        return Gson().fromJson(response, listType)
    }
    override fun selectLanguage(view: View, language: LanguageDataModel) {
        view.disableMultiTap()
        selectedLanguage = language.name?.trim()
        selectedLanguageCode = language.code
        selectedLanguageValue = language.value?.lowercase()
        mBinding.tvLanguage1.text = selectedLanguage
        mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
        mBinding.tvLanguage1.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
        toggleDropDownVisibility()
        if (userLanguage != selectedLanguageValue){
            hasLanguageChanged = true
            mBinding.btNext.isEnabled = true
            userLanguage = selectedLanguageValue
        }else{
            hasLanguageChanged = false
            if (hasNameChanged || hasContractNumberChanged || hasPlateNumberChanged || hasImageChanged){
                mBinding.btNext.isEnabled = true
            }else{
                mBinding.btNext.isEnabled = false
            }
        }
    }

    private fun selectCaptureImage(){
        val bottomSheetFragment: BottomSheetDialogFragment =
            UploadImageBottomSheet()
        bottomSheetFragment.show(requireFragmentManager(), bottomSheetFragment.tag)
    }

    private fun validateInputs(
        firstName: String,
        plateNumber: String,
        aufTragNumber: String,
        language: String
    ): Boolean {
        if (firstName.isEmpty()) {
            Utils.showError(requireContext(), getString(R.string.please_enter_your_name))
            return false
        }

        if (plateNumber.isEmpty()) {
            Utils.showError(requireContext(), getString(R.string.please_enter_plate_number))
            return false
        }

        if (aufTragNumber.isEmpty()) {
            Utils.showError(requireContext(), getString(R.string.please_enter_auftrag_number))
            return false
        }

        if (language.isEmpty()) {
            Utils.showError(requireContext(), getString(R.string.select_language))
            return false
        }

        return true
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

    private fun changeLanguageDialog(
        firstName: String,
        plateNumber: String,
        aufTragNumber: String,
        language: String
    ){
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.change_langugage_alert_title),
            textMsg = getString(R.string.change_langugage_alert_text),
            imageIcon = R.drawable.ic_attention,
            singleButton = false,
            fullscreen = false,
            buttonTextLeft = getString(R.string.cancel),
            buttonTextRight = getString(R.string.proceed),
            buttonClickLeft = {
            },
            buttonClickRight = {
                userName = firstName.trim()
                userPlateNumber = plateNumber.trim()
                userContractNumber = aufTragNumber.trim()
                userLanguage = language.trim()
                updateProfile()
            }
        )
    }

    private fun moveBack(){
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.move_back_dialog_title),
            textMsg = getString(R.string.move_back_dialog_text),
            imageIcon = R.drawable.ic_attention,
            singleButton = false,
            fullscreen = false,
            buttonTextLeft = getString(R.string.stay),
            buttonTextRight = getString(R.string.discard),
            buttonClickLeft = {
            },
            buttonClickRight = {
                findNavController().navigateUp()
            }
        )
    }
    private fun setupTouchListenerToHideKeyboard(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                view.clearFocus()
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupTouchListenerToHideKeyboard(view.getChildAt(i))
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showProgress(visibility: Boolean){
        mBinding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }

}