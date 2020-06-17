package com.dadoutek.uled.switches

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import com.dadoutek.uled.model.DbModel.DbGroup


/**
 * 创建者     ZCL
 * 创建时间   2020/1/13 10:47
 * 描述  组列表单选
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class GroupItemAdapter(resId: Int, data: MutableList<DbGroup>):BaseQuickAdapter<DbGroup,BaseViewHolder>(resId,data){
    override fun convert(helper: BaseViewHolder?, item: DbGroup?) {
        helper?.setText(R.id.template_group_name_s,item?.name)
                ?.setVisible(R.id.template_device_batch_selected,false)
    }

}