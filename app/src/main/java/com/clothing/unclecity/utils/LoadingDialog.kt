package com.clothing.unclecity.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.clothing.unclecity.R

class LoadingDialog(context: Context) {
    private val dialog = Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_loading_dialog)
        setCancelable(false)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)
    }


    private val loadingAnimationView :LottieAnimationView = dialog.findViewById(R.id.loading_anim)
    private val loadingView :LinearLayout = dialog.findViewById(R.id.loading_view)
    private val loadingText: TextView = dialog.findViewById(R.id.progress_text)

    private val doneAnimationView :LottieAnimationView = dialog.findViewById(R.id.done_anim)
    private val doneView :LinearLayout = dialog.findViewById(R.id.done_view)
    private val doneText: TextView = dialog.findViewById(R.id.done_text)

    private val errorAnimationView :LottieAnimationView = dialog.findViewById(R.id.error_anim)
    private val errorView: LinearLayout = dialog.findViewById(R.id.error_view)
    private val errorText: TextView = dialog.findViewById(R.id.error_text)

    fun show(text: String ) {
        loadingAnimationView.playAnimation()
        if (!dialog.isShowing) {
            dialog.show()
        }
        loadingText.text = text
    }

    fun show() {
        loadingAnimationView.playAnimation()
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun isDone(successMessage: String = "", time: Long = 2000, func : (dialog: Dialog) -> Unit) {
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        doneView.visibility = View.VISIBLE
        doneAnimationView.playAnimation()
        doneText.text = successMessage
        if (!dialog.isShowing) {
            dialog.show()
        }
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            func(dialog)
        },time)
    }

    fun isError(errorMessage: String?) {
        loadingView.visibility = View.GONE
        doneView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        errorAnimationView.playAnimation()
        errorText.text = errorMessage
        if (!dialog.isShowing) {
            dialog.show()
        }
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        },4000)
    }

    fun setText(text: String) {
        loadingAnimationView.playAnimation()
        doneAnimationView.playAnimation()
        loadingText.text = text
    }

    fun dismiss() {
        dialog.dismiss()
    }
}