package com.dadoutek.uled.model;

import com.dadoutek.uled.R;

import java.io.Serializable;

/**
 * Created by hejiajun on 2018/5/5.
 */

public class ItemGroup implements Serializable {
    public int groupAddress = 0;
    public int brightness = 50;
    public int temperature = 50;
    public int color = 855638015;
    public String gpName = "";
    public boolean enableCheck=false;
    public boolean checked=false;
    public int deviceType;
    public boolean isOn =true;
    public boolean isEnableWhiteBright = true;
    public boolean isEnableBright = true;

    public long sceneId= 1000000;
    public int rgbType = 0   ;   //rgb 类型 0:颜色模式 1：渐变模式
    public int gradientType = 0 ;//渐变类型 1：自定义渐变  2：内置渐变
    public int gradientId = 1  ; //渐变id
    public int gradientSpeed = 0 ;//渐变速度
    public String gradientName  ;//变速度
    public int icon = R.drawable.icon_group ;//图标

    @Override
    public String toString() {
        return "ItemGroup{" +
                "groupAddress=" + groupAddress +
                ", brightness=" + brightness +
                ", temperature=" + temperature +
                ", color=" + color +
                ", gpName='" + gpName + '\'' +
                ", enableCheck=" + enableCheck +
                ", checked=" + checked +
                ", deviceType=" + deviceType +
                ", isOn=" + isOn +
                ", isEnableWhiteBright=" + isEnableWhiteBright +
                ", isEnableBright=" + isEnableBright +
                ", rgbType=" + rgbType +
                ", gradientType=" + gradientType +
                ", gradientId=" + gradientId +
                ", gradientSpeed=" + gradientSpeed +
                ", gradientName='" + gradientName + '\'' +
                '}';
    }
}
