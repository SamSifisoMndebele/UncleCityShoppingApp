package com.clothing.unclecity.ui.storeinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.clothing.unclecity.databinding.FragmentStoreInfoBinding
import com.clothing.unclecity.utils.Extensions.to2DecimalString

class StoreInfoFragment : Fragment() {

    private var _binding: FragmentStoreInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStoreInfoBinding.inflate(inflater, container, false)

        val storeInfoViewModel = ViewModelProvider(this)[StoreInfoViewModel::class.java]

        storeInfoViewModel.businessEmail.observe(viewLifecycleOwner) {
            binding.email.editText!!.setText(it)
            binding.email.editText!!.doOnTextChanged { text, _, _, _ ->
                storeInfoViewModel.setBusinessEmail(text.toString())
            }
        }
        storeInfoViewModel.queryFormLink.observe(viewLifecycleOwner) {
            binding.queryLink.editText!!.setText(it)
            binding.queryLink.editText!!.doOnTextChanged { text, _, _, _ ->
                storeInfoViewModel.setQueryFormLink(text.toString())
            }
        }
        storeInfoViewModel.businessNumber.observe(viewLifecycleOwner) {
            binding.number.editText!!.setText(it)
            binding.number.editText!!.doOnTextChanged { text, _, _, _ ->
                if (text.toString().startsWith("0")){
                    storeInfoViewModel.setBusinessNumber(text.toString().replaceFirst("0", "+27"))
                } else {
                    storeInfoViewModel.setBusinessNumber(text.toString())
                }
            }
        }
        storeInfoViewModel.verificationCode.observe(viewLifecycleOwner) {
            binding.verificationCode.editText!!.setText(it)
            binding.verificationCode.editText!!.doOnTextChanged { text, _, _, _ ->
                storeInfoViewModel.setVerificationCode(text.toString())
            }
        }
        storeInfoViewModel.bankDetails.observe(viewLifecycleOwner) {
            binding.bankDetails.editText!!.setText(it)
            binding.bankDetails.editText!!.doOnTextChanged { text, _, _, _ ->
                storeInfoViewModel.setBankDetails(text.toString())
            }
        }
        storeInfoViewModel.shippingFee.observe(viewLifecycleOwner) {
            binding.shippingFee.editText!!.setText((it ?: 0f).to2DecimalString())
            binding.shippingFee.editText!!.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()){
                    storeInfoViewModel.setShippingFee(text.toString().toFloat().to2DecimalString().toFloat())
                }
            }
        }

        return  binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}