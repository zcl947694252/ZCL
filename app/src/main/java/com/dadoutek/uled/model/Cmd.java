package com.dadoutek.uled.model;

import java.io.Serializable;

/**
 * Created by hejiajun on 2018/3/27.
 */

public class Cmd implements Serializable {
    public static final int SCANCOMPLET = 0X01;
    public static final int SCANSUCCESS = 0X02;
    public static final int SCANFAIL = 0X03;
    public static final int UPDATEDATA = 0X04;
    public static final int BLEOPEN = 0X05;
    public static final int GETACCOUNT = 0X06;
    public static final int GETSALT = 0X07;
    public static final int STLOGIN = 0X08;
}
