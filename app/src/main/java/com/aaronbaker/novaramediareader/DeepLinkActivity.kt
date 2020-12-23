package com.aaronbaker.novaramediareader

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val inLink = intent
        val data: Uri? = inLink.data
        println("LINK   :- $data")
    }
}