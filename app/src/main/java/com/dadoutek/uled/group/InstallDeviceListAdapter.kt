package com.dadoutek.uled.group

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import com.dadoutek.uled.model.InstallDeviceModel
import com.dadoutek.uled.tellink.TelinkLightApplication

/**
 * Created by hejiajun on 2018/5/10.
 */

class InstallDeviceListAdapter(layoutResId: Int, data: List<InstallDeviceModel>?) : BaseQuickAdapter<InstallDeviceModel, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: InstallDeviceModel) {
       helper.setText(R.id.template_group_name_s,item.deviceType)
               .setText(R.id.device_describe,item.deviceDescribeTion)
        when (item.deviceType) {
            TelinkLightApplication.getApp().getString(R.string.normal_light) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_light_add_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.rgb_light) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_rgb_light_add_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.switch_title) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_switch_add_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.sensor) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_sensor_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.curtain) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_curtain_add_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.relay) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_acceptor_add_device)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
            TelinkLightApplication.getApp().getString(R.string.Gate_way) -> {
                helper.setImageResource(R.id.add_device_image,R.drawable.icon_gw_small)
                helper.setText(R.id.template_group_name_s,item.deviceType)
                        .setText(R.id.device_describe,item.deviceDescribeTion)
            }
        }
    }
}
