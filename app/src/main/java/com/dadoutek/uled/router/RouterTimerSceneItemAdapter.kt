package com.dadoutek.uled.router

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import com.dadoutek.uled.gateway.bean.WeekBean
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.dbModel.DBUtils
import com.dadoutek.uled.network.RouterTimerSceneBean
import org.greenrobot.greendao.DbUtils


/**
 * 创建者     ZCL
 * 创建时间   2020/10/28 9:51
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class RouterTimerSceneItemAdapter(resId: Int, data: MutableList<RouterTimerSceneBean>) : BaseQuickAdapter<RouterTimerSceneBean, BaseViewHolder>(resId, data) {
    override fun convert(helper: BaseViewHolder?, item: RouterTimerSceneBean?) {
        val min = item?.min ?: 0
        val hour = item?.hour ?: 0
        var minStr = if (min < 10) "0$min" else min.toString()
        var hourStr = if (hour < 10) "0$hour" else hour.toString()

        val value = "${hourStr}:${minStr}"
        helper?.addOnClickListener(R.id.item_event_ly)
                ?.addOnClickListener(R.id.item_event_switch)
                ?.setText(R.id.item_event_title, value)
                ?.setText(R.id.item_event_week, getWeekStr(item?.week))
                ?.setText(R.id.item_event_name, DBUtils.getSceneByID(item!!.sid.toLong())?.name)
                ?.setVisible(R.id.item_event_name, true)
                ?.setChecked(R.id.item_event_switch, item?.status == 1)
    }

    open fun getWeekStr(week: Int?): String {
        var tmpWeek = week ?: 0
        val sb = StringBuilder()
        var str  =  when (tmpWeek) {
            0b01111111,0b10000000-> sb.append(mContext.getString(R.string.every_day)).toString()
            0b00000000 -> sb.append(mContext.getString(R.string.only_one)).toString()
            else -> {
                var list = mutableListOf(
                        WeekBean(mContext.getString(R.string.monday), 1, (tmpWeek and Constant.MONDAY) != 0),
                        WeekBean(mContext.getString(R.string.tuesday), 2, (tmpWeek and Constant.TUESDAY) != 0),
                        WeekBean(mContext.getString(R.string.wednesday), 3, (tmpWeek and Constant.WEDNESDAY) != 0),
                        WeekBean(mContext.getString(R.string.thursday), 4, (tmpWeek and Constant.THURSDAY) != 0),
                        WeekBean(mContext.getString(R.string.friday), 5, (tmpWeek and Constant.FRIDAY) != 0),
                        WeekBean(mContext.getString(R.string.saturday), 6, (tmpWeek and Constant.SATURDAY) != 0),
                        WeekBean(mContext.getString(R.string.sunday), 7, (tmpWeek and Constant.SUNDAY) != 0))
                for (i in 0 until list.size) {
                    if (list[i].selected)
                        sb.append(list[i].week).append(",")
                }
                sb.toString().substring(0,sb.length-1)
            }
        }
        return str
    }


}