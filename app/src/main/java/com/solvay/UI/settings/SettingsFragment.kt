package com.solvay.UI.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.DialogUtils
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.authentication.AuthenticationActivity
import com.solvay.databinding.FragmentSettingsBinding


class SettingsFragment : BaseFragment() {
    private lateinit var mBinding :FragmentSettingsBinding
    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var alertManager: AlertManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
       mBinding=  FragmentSettingsBinding.inflate(localizedInflater , container , false)
        mBinding.constEdit.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }
        mBinding.constAboutUs.setOnClickListener {
            findNavController().navigate(R.id.aboutUsFragment)
        }
        mBinding.constTermsConditions.setOnClickListener {
            findNavController().navigate(R.id.termsAndConditionFragment)
        }
        mBinding.constContactUs.setOnClickListener {
            findNavController().navigate(R.id.contactUsFragment)
        }
        mBinding.constLogout.setOnClickListener {
            logOutDialog()
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        if (::mBinding.isInitialized){
            showNavBottom()
        }
        observer()
    }
    private fun logOutDialog(){
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.logout),
            textMsg = getString(R.string.are_you_sure_logout),
            imageIcon = null,
            singleButton = false,
            fullscreen = false,
            buttonTextLeft = getString(R.string.cancel),
            buttonTextRight = getString(R.string.logout),
            buttonClickLeft = {
            },
            buttonClickRight = {
                Prefs.init().isRouteCompleted = true
                if (isInternetAvailable(requireContext())){
                    viewModel.logout()
                }else{
                    val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                    alertManager.showError(baseContext.getString(R.string.no_internet_message))
                }


            }
        )
    }

    fun observer(){
        viewModel.contactusResponse.observe(viewLifecycleOwner, EventObserver{
            logoutObserver(it)
        })

        viewModel.logoutResponse.observe(viewLifecycleOwner, EventObserver{
            Log.e("hjfgdsjf", "jfhjhfjhj")
            if (it.success==true){
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }else{
                it.message?.let { it1 -> alertManager.showError(it1) }
            }

        })


    }

    private fun logoutObserver(response: ContactusResponse){
        if (response.success == true){
            alertManager.showSuccess(response.message?:"")

        }else{
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(response.message?:baseContext.getString(R.string.something_went_wrong))
        }
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

}