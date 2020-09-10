package com.dadoutek.uled.model;

import java.io.Serializable;

public class DadouDeviceInfo implements Serializable {

    public String macAddress;
    public String sixByteMacAddress;
    public String deviceName;

    public String meshName;
    public int meshAddress;
    public int meshUUID;
    public int productUUID;
    public int status;
    public byte[] longTermKey = new byte[16];
    public String firmwareRevision;

    public boolean selected;
}
