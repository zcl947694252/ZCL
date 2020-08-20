package com.dadoutek.uled.group

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import kotlinx.android.synthetic.main.item_group.view.*


/**
 * 创建者     ZCL
 * 创建时间   2020/8/20 14:41
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class OnlyNameAdapter(resId: Int, data: MutableList<String>) :BaseQuickAdapter<String,BaseViewHolder>(resId,data) {
    override fun convert(helper: BaseViewHolder?, item: String?) {
        helper?.setText(R.id.template_group_name_s,item)
    }
}