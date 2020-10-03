package com.stangenchev.jokes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.stangenchev.jokes.R
import kotlinx.android.synthetic.main.fragment_bottomsheet.view.*

class BottomNavigationDrawerFragment: BottomSheetDialogFragment(),
    NavigationView.OnNavigationItemSelectedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bottomsheet, container, false)
        view.navigation_view.setNavigationItemSelectedListener(this)
        return view
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> {
                println("history")
                this.dismiss()
            }
            R.id.favorites -> {
                println("favorites")
                this.dismiss()
            }
            R.id.settings -> {
                println("settings")
                this.dismiss()
            }
        }
        return true
    }
}