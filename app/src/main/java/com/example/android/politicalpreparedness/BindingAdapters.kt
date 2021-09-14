package com.example.android.politicalpreparedness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("openLinkOnClick")
fun openLinkOnClick(textView: TextView, url: String?) {
    textView.setOnClickListener {
        if (url != null && url != "") {
            val context: Context = textView.context
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            context.startActivity(intent)
        }
    }
}