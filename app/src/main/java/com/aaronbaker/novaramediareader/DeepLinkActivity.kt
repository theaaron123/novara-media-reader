package com.aaronbaker.novaramediareader

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction


class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val inLink = intent
        val data: Uri? = inLink.data
        val bundle = Bundle()
        bundle.putString(ArticleListFragment.PERMALINK, data.toString())
        val tx: FragmentTransaction = supportFragmentManager.beginTransaction()
        tx.setCustomAnimations(0, 0)
        val deepLinkFragment = ArticleFullscreenFragment()
        deepLinkFragment.arguments = bundle
        tx.add(R.id.fragment_container, deepLinkFragment).attach(deepLinkFragment).commit()
    }
}