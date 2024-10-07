package ai.koda.mobile.sdk.sample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout

class ThreeEditTextDialog(context: Context) {
    var mContext: Context? = null
    var outerCallback: (String, String, String) -> Unit = {_,_,_ ->}
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
    }

    fun setText(title: String, message: String?, hint1: String?, hint2: String, hint3: String?) {
        view?.findViewById<TextView>(R.id.dialog_edittext_message)?.text = message
        view?.findViewById<TextView>(R.id.dialog_edittext_title)?.text = title
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input)?.hint = hint1
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input2)?.hint = hint2
        view?.findViewById<TextInputLayout>(R.id.dialog_edittext_input3)?.hint = hint3
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
