package com.solvay.UI.settings

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.navigateUp
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.UI.settings.dataModels.ContactUsRequest
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.databinding.FragmentContactUsBinding


class ContactUsFragment : BaseFragment() {
private lateinit var mBinding :FragmentContactUsBinding
    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var alertManager: AlertManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentContactUsBinding.inflate(localizedInflater , container , false)
        mBinding.apply {
            llBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        return  mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        observer()
        setupTouchListenerToHideKeyboard(view)
        mBinding.apply {
            btNext.setOnClickListener {
                if (validateEmailAndMessage(etName, etPlate)){
                    val email = etName.text.toString().trim()
                    val message = etPlate.text.toString().trim()
                    callApi(ContactUsRequest(message, email))
                }
            }
        }
    }

    fun callApi(request: ContactUsRequest){
        viewModel.contactUs(request)
    }

    fun observer(){
        viewModel.contactusResponse.observe(viewLifecycleOwner, EventObserver{
            contactUsObserver(it)
        })
    }

    private fun contactUsObserver(response: ContactusResponse){
        if (response.success == true){
            alertManager.showSuccess(response.message?:"")
            mBinding.apply {
                etPlate.clearFocus()
                etPlate.setText("")
                etName.clearFocus()
                etName.setText("")
            }
        }else{
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(response.message?:baseContext.getString(R.string.something_went_wrong))
        }
    }

    fun validateEmailAndMessage(
        emailEditText: EditText,
        messageEditText: EditText,
    ): Boolean {
        val email = emailEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.enter_email_address))
            emailEditText.requestFocus()
            isValid = false
        } else if (!isValidEmail(email)) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.enter_valid_email))
            emailEditText.requestFocus()
            isValid = false
        }

        if (message.isEmpty()) {
            if (isValid) {
                messageEditText.requestFocus()
            }
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
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
}