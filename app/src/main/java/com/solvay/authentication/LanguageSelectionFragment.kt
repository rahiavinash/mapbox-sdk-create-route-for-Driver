package com.solvay.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils.disableMultiTap
import com.solvay.adapters.ClickListener
import com.solvay.adapters.LanguageRecyclerAdapter
import com.solvay.authentication.models.LanguageDataModel
import com.solvay.databinding.FragmentLanguageSelectionBinding
import java.io.InputStream
import java.lang.reflect.Type

class LanguageSelectionFragment : Fragment(), ClickListener {
    private lateinit var mBinding: FragmentLanguageSelectionBinding
    private var selectedLanguage: String? = null
    private var selectedLanguageCode: String? = "de"
    private var selectedLanguageValue: String? = "german"
    private var languageList =  ArrayList<LanguageDataModel>()
    private lateinit var alertManager: AlertManager
    val bundle = Bundle()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "de")

        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentLanguageSelectionBinding.inflate(localizedInflater, container, false)
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(::mBinding.isInitialized){
            if(Prefs.init().isRouteCompleted){
                bundle.putString(getString(R.string.language),Prefs.init().appLanguage)
                bundle.putString(getString(R.string.selected_language_code),Prefs.init().appLanguageCode)
                bundle.putString(getString(R.string.selected_language_value), selectedLanguageValue)
                findNavController().navigate(R.id.registerFragment,bundle)
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                requireActivity().finishAffinity()
            }


            alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
            if (Prefs.init().appLanguage.isNotEmpty()){
                selectedLanguage = Prefs.init().appLanguage
                selectedLanguageCode = Prefs.init().appLanguageCode
                selectedLanguageValue = Prefs.init().appLanguageValue
                mBinding.tvLanguage.text = selectedLanguage
                mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
            }else{
                mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
            }
            setupRecycler()
            mBinding.apply {
                clLanguage.setOnClickListener {
                    toggleDropDownVisibility()
                }
                btRegister.setOnClickListener {
                    registerLanguage(selectedLanguage?:"", selectedLanguageCode?:"de")
                }
                root.setOnTouchListener { _, event ->
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
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
 //       alertManager.cleanup()
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
            if (selectedLanguage == null && Prefs.init().appLanguage == ""){
                selectedLanguage = "German (Deutsch)"
                tvLanguage.text = selectedLanguage
            }
            rvLanguages.adapter =
                LanguageRecyclerAdapter(
                    list = languageList,
                    clickListener = this@LanguageSelectionFragment
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
        mBinding.tvLanguage.text = selectedLanguage
        mBinding.tvLanguage.setTextColor(ContextCompat.getColor(requireContext(), R.color.editext_hint))
        toggleDropDownVisibility()
    }
    private fun registerLanguage(selectedLanguage: String, selectedLanguageCode: String){
        if (languageValidation(selectedLanguage)){
            LocaleHelper.setLocale(requireContext(), selectedLanguageCode ?: "en")
            bundle.putString(getString(R.string.language), selectedLanguage)
            bundle.putString(getString(R.string.selected_language_code), selectedLanguageCode)
            bundle.putString(getString(R.string.selected_language_value), selectedLanguageValue)
            Prefs.init().appLanguageCodeTemp = selectedLanguageCode
            findNavController().navigate(R.id.registerFragment, bundle)
        }else{
            if (selectedLanguage == ""){
                val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                alertManager.showError(baseContext.getString(R.string.select_a_language))
            }else{
                val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                alertManager.showError(baseContext.getString(R.string.select_a_valid_language))
            }
        }
    }
    private fun languageValidation(selectedLanguage: String): Boolean {
        return languageList.any { it.name == selectedLanguage }
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }



}