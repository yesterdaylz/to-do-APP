package com.example.todoapp.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
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
        val view = LayoutInflater.from(context)
            .inflate(R.layout.fragment_important_dialog, null)

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
        guideView.findViewById<TextView>(R.id.tv_guide_title).text =
            "vivo新系统在锁屏后会休眠App，这会导致待办、计时结束不提醒等问题。请按以下指引来设置权限。"
        guideView.findViewById<TextView>(R.id.tv_guide_description).text =
            "请放心，我们的后台耗电量很低，仅为了保持后台计时持续~\n点击前往设置-后台耗电管理-柠檬Todo-切换至【允许后台高耗电】，如图所示："
        guideView.findViewById<TextView>(R.id.tv_guide_steps).text =
            "1. 前往设置 → 电池 → 后台耗电管理\n2. 找到\"柠檬ToDo\"\n3. 选择\"允许后台高耗电\""
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
        guideView.findViewById<TextView>(R.id.tv_guide_title).text =
            "请开启自启动权限，确保应用能够正常启动定时提醒"
        guideView.findViewById<TextView>(R.id.tv_guide_description).text =
            "自启动权限允许应用在系统启动后自动运行，确保计时和提醒功能正常工作"
        guideView.findViewById<TextView>(R.id.tv_guide_steps).text =
            "1. 前往设置 → 应用 \n2. 点击权限\n3. 开启自启动权限"
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
        guideView.findViewById<TextView>(R.id.tv_guide_title).text =
            "请在多任务界面锁定应用，防止被一键清理"
        guideView.findViewById<TextView>(R.id.tv_guide_description).text =
            "锁定应用后，即使清理后台也不会关闭计时功能"
        guideView.findViewById<TextView>(R.id.tv_guide_steps).text =
            "1. 打开手机多任务界面）\n2. 找到\"柠檬Todo\"应用卡片\n3. 向下滑动或点击小箭头\n4. 点击锁形图标锁定应用"

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
            .setTitle("操作提示")
            .setMessage("请按照上述步骤在多任务界面锁定应用。\n\n打开多任务界面的方法：\n• 全面屏手机：从底部上滑并停顿\n• 虚拟键手机：点击方形多任务键\n• 其他方式：请参考手机使用说明")
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun openPowSetting() {
        try {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
            Toast.makeText(requireContext(), "点击后台耗电管理-柠檬Todo-切换至【允许后台高耗电】", Toast.LENGTH_LONG).show()
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            //当初和vivo大战试图打开优化电池界面放的，最后放弃了，直接打开电池界面吧，现在懒得删
            openAppSetting()
        }
    }

    private fun openAutoStartSetting() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            //intent.data = Uri.parse("package:${requireContext().packageName}")
            intent.data = "package:${requireContext().packageName}".toUri()
            Toast.makeText(requireContext(), "点击权限-打开【自启动】", Toast.LENGTH_LONG).show()
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