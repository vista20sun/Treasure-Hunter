package com.example.vista.treasurefinder;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        backGround = findViewById(R.id.colorbg);
        dropCount=0;
        lostCount=0;
        current = null;
        loadSound();
        handler = new Handler();
        working = false;
        hints=getResources().getStringArray(R.array.hints);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"BlueTooth not supported",Toast.LENGTH_SHORT).show();
            SeekBar bar = findViewById(R.id.testBar);
            bar.setVisibility(View.VISIBLE);
            bar.setMax(100);
            bar.setProgress(33);
            //backGround.setStrokeWidth(0,100,33);
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    backGround.setRatio(0,100,seekBar.getProgress());
                }
            });
            return;
            //if bluetooth not available in device, do not init functions for other view component but show the seekBar for debug UI
        }
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));// support iBeacon
        beaconManager.setForegroundScanPeriod(300L);

        switchButton = findViewById(R.id.SearchSwitch);
        refresh = findViewById(R.id.Refresh);
        refresh.setEnabled(false);
        switchButton.setChecked(false);
        switchButton.setOnClickListener(new ToggleButton.OnClickListener() {
            public void onClick(View v){
                boolean checked = switchButton.isChecked();
                refresh.setEnabled(checked);
                backGround.setShowUp(checked);
                if(checked){
                    if(!checkLocationPermission())
                        return;
                    BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(!bleAdapter.isEnabled()){
                        openBluetooth();
                        return;
                    }
                    startSearching();
                }else{
                    stopSearching();
                }
            }
        });
        switchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(switchButton.isChecked())
                    return false;
                setting();
                return true;
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconManager.unbind(MainActivity.this);
                beaconManager.bind(MainActivity.this);
            }
        });
        updateUI();
    }

    private void startFlash(final float rate){
        if(!flashing)
            return;
        int time = (int) (500/rate);
        if(animation == null) {
            animation = new AlphaAnimation(1, 0.1f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setDuration(time);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                backGround.startAnimation(animation);
            }
        });
    }
    private void setFlashRate(final float rate){
        if(!flashing||animation==null)
            return;
        int time = (int) (500/rate);
        animation.setDuration(time);
    }

    private void stopFlash(){
        if(!flashing)
            return;
        animation=null;
        handler.post(new Runnable() {
            @Override
            public void run() {
                backGround.clearAnimation();
            }
        });
    }


    private void setting(){
        Intent intent = new Intent(MainActivity.this,Settings.class);
        intent.putExtra("colors",backGround.getColors());
        intent.putExtra("splits",backGround.getSplits());
        intent.putExtra("target",targetBeaconID);
        intent.putExtra("gradual",backGround.isGradation());
        intent.putExtra("flash",flashing);
        intent.putExtra("custom",customeColor);
        startActivityForResult(intent,REQUEST_SETTINGS);
    }

    /**
     * when start searching, bind the beacon searching service to beaconManager and play sound
     * when stop, unbind and stop play sound.
     * */
    private void startSearching(){
        working = true;
        beaconManager.bind(MainActivity.this);
        streamID = sound.play(soundID,1,1,1,-1,(float) 0.5);
        updateUI();
        startFlash(0.5f);
    }
    private void stopSearching(){
        working = false;
        beaconManager.unbind(MainActivity.this);
        sound.stop(streamID);
        stopFlash();
        streamID=-1;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUI();
                stopFlash();
            }
        },800);
    }



    private void openBluetooth(){
        Intent openBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(openBluetooth,REQUEST_ENABLE_BLE);
    }

    /**
     * check this does this application has permission to access location service
     * */
    private boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            return true;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_BLUETOOTH_PERMISSION);
        return false;
    }


    /**
     * load /res/raw/bee.mp3 sound file
     * notice:
     * soundID is the id for the sound resource for "bee.mp3";
     * StreamID is the task ID for current task which playing the "bee.mp3"
     * */
    private void loadSound(){
        if(Build.VERSION.SDK_INT>=21){
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(2);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            sound =  builder.build();
        }else
            sound = new SoundPool(2,AudioManager.STREAM_SYSTEM,5);
        soundID = sound.load(this,R.raw.bee,1);
    }

    //update the information that showed on screen with the closest beacon.
    private void updateUI(final BeaconRecord record){
        backGround.setRatio(BeaconRecord.referenceMinRSSI, BeaconRecord.referenceMaxRSSI,record.getmRSSI());
        float rate = (float) ((2.5 * (float)(record.getmRSSI()- BeaconRecord.referenceMinRSSI))/(float)(BeaconRecord.referenceMaxRSSI- BeaconRecord.referenceMinRSSI) + 0.5);
        setFlashRate(rate);
        sound.setRate(streamID,rate);
    }
    //update the information that showed on screen while no beacon founded
    private void updateUI(){
        backGround.setRatio(BeaconRecord.referenceMinRSSI, BeaconRecord.referenceMaxRSSI, BeaconRecord.referenceMinRSSI);
        sound.setRate(streamID,0.5f);
    }

    /**
     * recall method, called when this application get/lose focus.
     * will pause/resume play the sound and search the beacons.
     * */
    @Override
    public void onPause(){
        super.onPause();
        if(!working)
            return;
        sound.pause(streamID);
        beaconManager.unbind(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!working)
            return;
        sound.resume(streamID);
        beaconManager.bind(this);
    }

    /***
     * recall method, using when need ask user to open the bluetooth
     * if user open the bluetooth, then start search
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_ENABLE_BLE){
            switch (resultCode){
                case RESULT_OK:
                    startSearching();
                    break;
                default:
                    Toast.makeText(this, "Please Open Bluetooth", Toast.LENGTH_SHORT).show();
                    switchButton.setChecked(false);
            }
        }else if(requestCode == REQUEST_SETTINGS){
            switch(resultCode){
                case RESULT_OK:
                    flashing = data.getBooleanExtra("setFlash",false);
                    backGround.setGradation(data.getBooleanExtra("setGradual",true));
                    customeColor = data.getBooleanExtra("setCustomColor",false);
                    targetBeaconID = data.getIntExtra("setTarget",0);
                    if(customeColor)
                        backGround.setColors(data.getIntArrayExtra("setColors"),data.getIntArrayExtra("setSplits"));
                    else
                        backGround.setColors(getResources().getIntArray(R.array.BgColors),getResources().getIntArray(R.array.BgSplit));
            }
        }
    }
    /**
     * recall method, using when need ask for permission to access the location service.
     * if get the required permission, then check does bluetooth open.
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode){
            case REQUEST_BLUETOOTH_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(!bleAdapter.isEnabled()){
                        openBluetooth();
                        return;
                    }
                    startSearching();
                } else {
                    Toast.makeText(this,"This application needs This Permission",Toast.LENGTH_SHORT).show();
                    switchButton.setChecked(false);
                }

        }
    }


    /**
     * recall method for bind beacon service
     * each time found some beacons in a search period, use the one with the highest rssi to update the showing information.
     * if the rssi changes is great, increase dropCount, and ignore this change while the counter didn't reach the threshold
     * */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons.size()>0){
                    lostCount = 0 ;
                    PriorityQueue<BeaconRecord> heap = new PriorityQueue<>();
                    for(Beacon x:beacons){
                        Log.d(debugTag,String.format("[%d]uuid:%s\tmajor:%s\\manner:%s\\distance:%.2f<rssi:%d>\n",beacons.size(),x.getId1(),x.getId2(),x.getId3(),x.getDistance(),x.getRssi()));
                        heap.offer(new BeaconRecord(x));
                    }
                    BeaconRecord temp = heap.peek();
                    Log.d(debugTag,String.format("[000]:rssi:%d/distance%.2f\n",temp.getmRSSI(),temp.getmDistance()));
                    if(current == null) {
                        current = temp;
                        dropCount = 0;
                    }else{
                        if(temp.getmRSSI()-current.getmRSSI()<RSSIChangeThreshold){
                            dropCount++;
                            if(dropCount>=DropThreshold){
                                current = temp;
                                dropCount=0;
                                return;
                            }
                        }else{
                            current = temp;
                            dropCount=0;
                        }
                    }
                    updateUI(current);
                }else{
                    Log.d(debugTag,"-----no detected-----");
                    lostCount++;
                    if(lostCount>=LostThreshold){
                        dropCount=0;
                        current = null;
                        updateUI();
                    }
                }
            }
        });

        try{
            if(targetBeaconID==0)
                beaconManager.startRangingBeaconsInRegion(new Region(targetUUID,null,null,null));
            else{
                String info[] = getResources().getStringArray(R.array.beacons)[targetBeaconID].split(":");
                beaconManager.startRangingBeaconsInRegion(new Region(targetUUID,null,
                        Identifier.fromInt(Integer.parseInt(info[0])),
                        Identifier.fromInt(Integer.parseInt(info[1]))));
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    private final static int DropThreshold = 10,LostThreshold=15,RSSIChangeThreshold=5;
    private ColoredBackground backGround;
    private boolean working;
    private String hints[];
    private final static int REQUEST_ENABLE_BLE = 0xa01,REQUEST_BLUETOOTH_PERMISSION=0xa93,REQUEST_SETTINGS=0xaaa;
    private SoundPool sound;
    private int soundID, streamID;
    private int dropCount,lostCount,flashRate;
    private Animation animation;
    private final static String debugTag="TreasureFinder:";
    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private final static String targetUUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825";
    private Handler handler;
    private BeaconRecord current;
    private BeaconManager beaconManager;
    private Button refresh;
    private ToggleButton switchButton;
    private boolean customeColor = false,flashing = false;
    private int targetBeaconID = 0;


}
