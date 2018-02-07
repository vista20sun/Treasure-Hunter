package com.example.vista.treasurefinder;


import org.altbeacon.beacon.Beacon;

/**
 * Created by Yuyang Luo on 2018/1/30.
 * records Beacon's information & comparable
 */

public class BeaconRecord implements Comparable<BeaconRecord>{
    private String mId;
    private double mDistance;
    private int mRSSI;

    public final static int referenceMaxRSSI = -50,referenceMinRSSI=-100;

    public BeaconRecord(Beacon beacon){
        mId = beacon.getId2().toString()+beacon.getId3().toString();
        int rssi = beacon.getRssi();
        mDistance = beacon.getDistance();
        //mDistance = RSSI2Distance(rssi);
        if(rssi > referenceMaxRSSI)
            mRSSI = referenceMaxRSSI;
        else if(rssi<referenceMinRSSI)
            mRSSI = referenceMinRSSI;
        else
            mRSSI = rssi;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public double getmDistance() {
        return mDistance;
    }

    public void setmDistance(double mDistance) {
        this.mDistance = mDistance;
    }

    public int getmRSSI() {
        return mRSSI;
    }

    public void setmRSSI(int mRSSI) {
        this.mRSSI = mRSSI;
    }

    public static double RSSI2Distance(int rssi){
        return Math.pow((Math.abs(rssi)-59)/(10*2.0),10);
    }

    @Override
    public int compareTo(BeaconRecord beaconRecord) {
        return beaconRecord.mRSSI - this.mRSSI;
    }
}
