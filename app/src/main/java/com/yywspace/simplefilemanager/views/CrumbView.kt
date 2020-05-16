package com.yywspace.simplefilemanager.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.yywspace.simplefilemanager.R
import kotlinx.android.synthetic.main.crumb_item_layout.view.*
import kotlinx.android.synthetic.main.crumb_layout.view.*

class CrumbView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var container: LinearLayout
    private lateinit var firstFixedCrumb: LinearLayout
    private lateinit var scrollView: HorizontalScrollView
    private var selectColor: Int = Color.BLACK
    private var defaultColor: Int = Color.GRAY
    private var isFirstFragment = true
    private var fixedBackCount = 1

    init {
        initAttribute(context, attrs)
        initView(context)
    }

    private fun initAttribute(context: Context?, attrs: AttributeSet?) {
        val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.CrumbView)
        selectColor = typedArray.getColor(R.styleable.CrumbView_select_color, Color.BLACK)
        defaultColor = typedArray.getColor(R.styleable.CrumbView_default_color, Color.GRAY)
        typedArray.recycle()
    }

    private fun initView(context: Context?) {
        val view = LayoutInflater.from(context).inflate(R.layout.crumb_layout, this, true)
        container = view.crumb_container
        firstFixedCrumb = view.crumb_item_view
        scrollView = view.scroll_view
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.isHorizontalScrollBarEnabled = false
    }

    fun setFragmentTransaction(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        fragmentManager.addOnBackStackChangedListener {
            val childCount = container.childCount
            for (i in childCount - 1 downTo fragmentManager.backStackEntryCount + fixedBackCount)
                container.removeViewAt(i)
            if (container.childCount >= 1)
                container.getChildAt(container.childCount - 1)
                    ?.findViewById<TextView>(R.id.crumb_name)
                    ?.setTextColor(selectColor)
        }
    }

    fun addFirstFixedCrumbItem(fragmentContainerId: Int, title: String, fragment: Fragment) {
        if (!isFirstFragment)
            return
        isFirstFragment = false
        fixedBackCount = 0
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(fragmentContainerId, fragment)
        fragmentTransaction.commit()
        firstFixedCrumb.crumb_name.text = title
        firstFixedCrumb.crumb_name.setTextColor(selectColor)
        firstFixedCrumb.setOnClickListener {
            firstFixedCrumb.crumb_name.setTextColor(selectColor)
            if (fragmentManager.backStackEntryCount == 0)
                return@setOnClickListener
            fragmentManager.popBackStack(
                fragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    fun addCrumbItem(fragmentContainerId: Int, title: String, fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(fragmentContainerId, fragment)
        if (isFirstFragment) {
            crumb_item_view.visibility = View.GONE
            isFirstFragment = false
        } else
            fragmentTransaction.addToBackStack(null)
        val id = fragmentTransaction.commit()
        with(
            LayoutInflater.from(context).inflate(R.layout.crumb_item_layout, container, false)
        ) {
            crumb_name.setTextColor(selectColor)
            crumb_name.text = title
            crumb_name.setOnClickListener {
                if (id < 0) {
                    (it as TextView).setTextColor(selectColor)
                    if (fragmentManager.backStackEntryCount == 0)
                        return@setOnClickListener
                    // 得到第一个id并将其和其后的所有弹出
                    fragmentManager.popBackStack(
                        fragmentManager.getBackStackEntryAt(0).id,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return@setOnClickListener
                }
                fragmentManager.popBackStack(id, 0)
            }
            firstFixedCrumb.crumb_name.setTextColor(defaultColor)
            if (container.childCount >= 1)
                container.getChildAt(container.childCount - 1)
                    ?.findViewById<TextView>(R.id.crumb_name)
                    ?.setTextColor(defaultColor)
            container.addView(this)
            post {
                scrollView.fullScroll(View.FOCUS_RIGHT)
            }
        }
    }
}