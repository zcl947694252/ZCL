package com.dadoutek.uled.othersview

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import android.view.ViewGroup

class ViewPagerAdapter(private val fm: FragmentManager?, val fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

//    override fun destroyItem(container: View, position: Int, `object`: Any) {
////        super.destroyItem(container, position, object);
//    }

//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val fragment = fragments[position]
//        //判断当前的fragment是否已经被添加进入Fragmentanager管理器中
//        if (!fragment.isAdded) {
//            val transaction = fm!!.beginTransaction()
//            transaction.add(fragment, fragment::class.simpleName)
//            //不保存系统参数，自己控制加载的参数
//            transaction.commitAllowingStateLoss()
//            //手动调用,立刻加载Fragment片段
//            fm.executePendingTransactions()
//        }
//        if (fragment.view!!.parent == null) {
//            //添加布局
//            container.addView(fragment.view)
//        }
//        return fragment.view!!
//    }


//    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        //移除布局
//        container.removeView(fragments[position].view)
//    }
}