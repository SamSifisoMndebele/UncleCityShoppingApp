package com.clothing.unclecity.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.clothing.unclecity.activities.MainActivity
import com.clothing.unclecity.R
import com.clothing.unclecity.activities.LoginActivity
import com.clothing.unclecity.databinding.FragmentProfileBinding
import com.clothing.unclecity.utils.OpenPicturesContract
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var profileViewModel : ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = (activity as MainActivity).profileViewModel
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel.email.observe(viewLifecycleOwner) {
            binding.email.editText!!.setText(it)
        }
        profileViewModel.name.observe(viewLifecycleOwner) {
            binding.names.editText!!.setText(it)
            binding.names.editText!!.doOnTextChanged { text, _, _, _ ->
                profileViewModel.setName(text.toString())
            }
        }
        profileViewModel.phone.observe(viewLifecycleOwner) {
            binding.number.editText!!.setText(it)
            binding.number.editText!!.doOnTextChanged { text, _, _, _ ->
                profileViewModel.setPhone(text.toString())
            }
        }
        profileViewModel.imageUrl.observe(viewLifecycleOwner) {
            binding.pictureProgress.visibility = View.GONE
            if (!it.isNullOrEmpty()){
                imageUri = Uri.parse(it)
            }
            Glide.with(binding.profileImage)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.profileImage)
        }

        binding.changeImageButton.setOnClickListener {
            selectImage()
        }
        binding.profileImage.setOnClickListener {
            selectImage()
        }

        binding.accountSignOutTv.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Sign Out?")
                setPositiveButton("Continue"){_,_->
                    Firebase.auth.signOut()
                    startActivity(Intent(context, LoginActivity::class.java))
                    activity?.finishAffinity()
                }
                show()
            }
        }

        return binding.root
    }

    private var imageUri : Uri? = null
    private val selectImageResult = registerForActivityResult(OpenPicturesContract()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.pictureProgress.visibility = View.VISIBLE
            Glide.with(this.requireContext())
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.profileImage)
            profileViewModel.setImage(it)
        }
    }

    private fun selectImage() {
        if (imageUri == null) {
            selectImageResult.launch(arrayOf("image/*"))
        } else {
            val choice = arrayOf<CharSequence>("Change Image","Remove Image")
            AlertDialog.Builder(context).apply {
                setItems(choice) { _, item ->
                    when {
                        choice[item] == "Change Image" -> {
                            selectImageResult.launch(arrayOf("image/*"))
                        }
                        choice[item] == "Remove Image" -> {
                            binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                            imageUri = null
                            profileViewModel.setImage(null)
                        }
                    }
                }
                show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}