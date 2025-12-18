package com.example.todoapp.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.example.todoapp.R


class ImportantDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val view = layoutInflater.inflate(R.layout.fragment_important_dialog, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        //窗口背景透明，不知道为什么不这样做我的对话框方圆交加😶
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        view.findViewById<ImageView>(R.id.close_btn).setOnClickListener{
            dismiss()
        }
        view.findViewById<View>(R.id.btn_background).setOnClickListener {
            showGide1()
        }
        view.findViewById<View>(R.id.btn_autostart).setOnClickListener {
            showGide2()
        }
        view.findViewById<View>(R.id.btn_lock).setOnClickListener {
            showGide3()
        }
        return dialog
    }
    private fun showGide1() {
        val guideView = layoutInflater.inflate(R.layout.dialog_pemission, null)
        guideView.findViewById<TextView>(R.id.tv_guide_title).setText(R.string.guide1_title)
        guideView.findViewById<TextView>(R.id.tv_guide_description).setText(R.string.guide1_description)
        guideView.findViewById<TextView>(R.id.tv_guide_steps).setText(R.string.guide1_steps)
        guideView.findViewById<Button>(R.id.btn_go_settings).setOnClickListener {
            openPowSetting()
        }
        val guideDialog = AlertDialog.Builder(requireContext())
            .setView(guideView)
            .create()
        guideDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        guideDialog.show()
    }
    private fun showGide2() {
        val guideView = layoutInflater.inflate(R.layout.dialog_pemission, null)
        guideView.findViewById<TextView>(R.id.tv_guide_title).setText(R.string.guide2_title)
        guideView.findViewById<TextView>(R.id.tv_guide_description).setText(R.string.guide2_description)
        guideView.findViewById<TextView>(R.id.tv_guide_steps).setText(R.string.guide2_steps)
        // ...
        guideView.findViewById<Button>(R.id.btn_go_settings).setOnClickListener {
            openAutoStartSetting()
        }

        val guideDialog = AlertDialog.Builder(requireContext())
            .setView(guideView)
            .create()

        guideDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        guideDialog.show()
    }
    private fun showGide3() {
        val guideView = layoutInflater.inflate(R.layout.dialog_pemission, null)
        guideView.findViewById<TextView>(R.id.tv_guide_title).setText(R.string.guide3_title)
        guideView.findViewById<TextView>(R.id.tv_guide_description).setText(R.string.guide3_description)
        guideView.findViewById<TextView>(R.id.tv_guide_steps).setText(R.string.guide3_steps)
        // ...

        guideView.findViewById<Button>(R.id.btn_go_settings).setOnClickListener {
            showHint()
        }

        val guideDialog = AlertDialog.Builder(requireContext())
            .setView(guideView)
            .create()

        guideDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        guideDialog.show()
    }
    private fun showHint() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.hint_title)
            .setMessage(R.string.hint_message)
            .setPositiveButton(R.string.hint_confirm, null)
            .show()
    }

    private fun openPowSetting() {
        try {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
            Toast.makeText(requireContext(), R.string.toast_power_setting, Toast.LENGTH_LONG).show()
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            openAppSetting()
        }
    }

    private fun openAutoStartSetting() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = "package:${requireContext().packageName}".toUri()
            Toast.makeText(requireContext(), R.string.toast_auto_start, Toast.LENGTH_LONG).show()
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun openAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = "package:${requireContext().packageName}".toUri()
        startActivity(intent)
    }
}