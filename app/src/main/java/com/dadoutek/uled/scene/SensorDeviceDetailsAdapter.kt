package com.dadoutek.uled.scene

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import com.dadoutek.uled.model.DbModel.DbSensor

class SensorDeviceDetailsAdapter(layoutResId: Int, data: List<DbSensor>?) : BaseQuickAdapter<DbSensor, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, scene: DbSensor) {
        if (scene != null) {
            helper.setText(R.id.name, scene.name)
            helper.setImageResource(R.id.img_icon, R.drawable.icon_light_on)
        }
    }
}