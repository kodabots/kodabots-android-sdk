package ai.koda.mobile.sdk.sample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout

class SendBlockWithParamsDialog(context: Context) {
    var mContext: Context? = null
    var outerCallback: (String, String, String) -> Unit = { _, _, _ -> }
    var mDialog: AlertDialog? = null
    var view: View? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        view = LayoutInflater.from(context).inflate(R.layout.dialog_three_edit_text, null, false)
        view?.findViewById<View>(R.id.dialog_edittext_ok)?.setOnClickListener {
            outerCallback.invoke(
                view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input)?.editText?.text.toString(),
                view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input2)?.editText?.text.toString(),
                view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input3)?.editText?.text.toString(),
            )
            mDialog?.dismiss()
        }
        view?.findViewById<View>(R.id.dialog_edittext_cancel)?.setOnClickListener {
            mDialog?.dismiss()
        }
        setText()
    }

    private fun setText() {
        view?.findViewById<TextView>(R.id.dialog_edittext_title)?.text =
            mContext?.getString(R.string.activity_main_controls_send_block).orEmpty()
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input)?.hint = "Block ID"
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input2)?.hint = "Parameter Key"
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input3)?.hint = "Parameter Value"
    }


    fun createDialog(callback: (String, String, String) -> Unit): AlertDialog {
        outerCallback = callback
        val builder = AlertDialog.Builder(mContext!!)
        builder.setView(view)
        mDialog = builder.create()
        mDialog?.show()
        return mDialog!!
    }
}