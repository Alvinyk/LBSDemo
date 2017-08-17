package com.example.alvin.lbsdemo;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by alvin on 2017/8/15.
 */
public class MonitorDef implements Serializable {
    private int iMonitorID;
    private String szMonitorName;
    private String szAddress;
    private float fLongitude;
    private float fLatitude;
    private int iState;


    public MonitorDef(int ID) {
        iMonitorID = ID;
        szMonitorName = "ESC" + ID;
        Random rand = new Random();
        fLongitude = (float) 120.0 + rand.nextFloat() - rand.nextInt(5);
        fLatitude = (float) 30.0 + rand.nextFloat() + rand.nextInt(5);
        szAddress = "";
    }

    public int getMonitorID() {
        return iMonitorID;
    }

    public String getMonitorName() {
        return szMonitorName;
    }

    public String getAddress() {
        return szAddress;
    }

    public float getLongitude() {
        return fLongitude;
    }

    public float getLatitude() {
        return fLatitude;
    }

    public int getState() {return  iState;}
}
