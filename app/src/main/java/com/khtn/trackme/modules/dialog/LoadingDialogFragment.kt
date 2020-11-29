package com.khtn.trackme.modules.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.khtn.trackme.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = activity?.layoutInflater?.inflate(R.layout.fragment_loading, null)

        isCancelable = false

        val dialog = AlertDialog.Builder(activity)
            .setView(v)
            .create()

        dialog.window?.apply {
            // set background transparent
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // set full screen
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }

        return dialog
    }
}