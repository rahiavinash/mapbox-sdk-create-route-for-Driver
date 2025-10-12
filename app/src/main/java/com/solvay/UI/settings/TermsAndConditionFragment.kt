package com.solvay.UI.settings

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.Utils.Prefs
import com.solvay.databinding.FragmentAboutUsBinding
import com.solvay.databinding.FragmentContactUsBinding
import com.solvay.databinding.FragmentTermsAndConditionBinding

class TermsAndConditionFragment : BaseFragment() {
    private lateinit var mBinding : FragmentTermsAndConditionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentTermsAndConditionBinding.inflate(localizedInflater , container , false)
        mBinding.apply {
            llBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        hideNavBottom()
        return  mBinding.root
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }
}