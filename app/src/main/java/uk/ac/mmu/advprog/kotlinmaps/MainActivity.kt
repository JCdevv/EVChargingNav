package uk.ac.mmu.advprog.kotlinmaps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity() {

    var fragmentManager : FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager = supportFragmentManager

        var fragTransation = fragmentManager!!.beginTransaction()
        fragTransation.replace(android.R.id.content,LoadingFragment())
        fragTransation.commit()

    }

}
