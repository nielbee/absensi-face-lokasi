package com.example.absensi.ui.login

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.absensi.R
import com.example.absensi.remote.VerifikasiResponse


class VerifikasiDialog(
    context: Context,
    private val data: VerifikasiResponse,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) {

    private val dialog: Dialog = Dialog(context)

    fun show() {
        val view = LayoutInflater.from(dialog.context)
            .inflate(R.layout.dialog_verifikasi, null)

        dialog.setContentView(view)
        dialog.setCancelable(false)

        view.findViewById<TextView>(R.id.txtNama).text =
            "Nama : ${data.nama}"

        view.findViewById<TextView>(R.id.txtNisn).text =
            "ID GTK : ${data.id_guru}"



        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        view.findViewById<Button>(R.id.btnBatal).setOnClickListener {
            dialog.dismiss()
            onCancel()
        }

        dialog.show()
    }
}
