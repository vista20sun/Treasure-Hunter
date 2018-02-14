package com.example.vista.treasurefinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Field;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        target=findViewById(R.id.beacon_spinner);
        flashing=findViewById(R.id.flash_switch);
        gradual=findViewById(R.id.gradual_switch);
        customeBg=findViewById(R.id.custome_switch);
        colors=findViewById(R.id.colorsPanel);

        c1=findViewById(R.id.color_1);
        c2=findViewById(R.id.color_2);
        c3=findViewById(R.id.color_3);
        c4=findViewById(R.id.color_4);
        colorViews=new ImageView[]{c1,c2,c3,c4};

        sp2=findViewById(R.id.sp2);
        sp3=findViewById(R.id.sp3);
        sp4=findViewById(R.id.sp4);

        e3=findViewById(R.id.enable_color_3);
        e4=findViewById(R.id.enable_color_4);

        Intent intent =getIntent();

        colorList = intent.getIntArrayExtra("colors");
        if(colorList==null)
            colorList = getResources().getIntArray(R.array.BgColors);
        for(int i=0;i<colorList.length;i++){
            colorViews[i].setBackgroundColor(colorList[i]);
        }

        splits = intent.getIntArrayExtra("splits");
        if(splits==null)
            splits=getResources().getIntArray(R.array.BgSplit);
        for(int i=1;i<splits.length;i++){
            switch (i){
                case 1:sp2.setText(String.valueOf(splits[i]));break;
                case 2:sp3.setText(String.valueOf(splits[i]));e3.setEnabled(true);e3.setChecked(true);sp2.setEnabled(true);break;
                case 3:sp4.setText(String.valueOf(splits[i]));e4.setEnabled(true);e4.setChecked(true);sp3.setEnabled(true);break;
            }
        }

        e3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp2.setEnabled(isChecked);
                sp3.setEnabled(isChecked&&e4.isChecked());
                e4.setEnabled(isChecked);

                if(isChecked){
                    sp3.setText(String.valueOf(100));
                }else{
                    e4.setChecked(false);
                    sp2.setText(String.valueOf(100));
                }
            }
        });
        e4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp3.setEnabled(isChecked);

                if(isChecked){
                    sp4.setText(String.valueOf(100));
                }else{
                    sp3.setText(String.valueOf(100));
                }
            }
        });

        for (int i=0;i<colorViews.length;i++){
            final int idx = i;
            colorViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    colorSelect(colorList[idx],idx);
                }
            });
        }

        customeBg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                colors.setVisibility(isChecked?View.VISIBLE:View.INVISIBLE);
            }
        });
        boolean custom = intent.getBooleanExtra("custom",false);
        customeBg.setChecked(custom);
        colors.setVisibility(custom?View.VISIBLE:View.INVISIBLE);
        target.setSelection(intent.getIntExtra("target",0));
        gradual.setChecked(intent.getBooleanExtra("gradual",true));
        flashing.setChecked(intent.getBooleanExtra("flash",true));
    }

    private void setResult(){
        Intent intent = getIntent();
        intent.putExtra("setFlash",flashing.isChecked());
        intent.putExtra("setGradual",gradual.isChecked());
        intent.putExtra("setCustomColor",customeBg.isChecked());
        if(customeBg.isChecked()){
            if(e4.isChecked()){
                intent.putExtra("setColors",colorList);
                intent.putExtra("setSplits",new int[]{0,Integer.parseInt(sp2.getText().toString()),Integer.parseInt(sp3.getText().toString()),100});
            }else if(e3.isChecked()){
                intent.putExtra("setColors",new int[]{colorList[0],colorList[1],colorList[2]});
                intent.putExtra("setSplits",new int[]{0,Integer.parseInt(sp2.getText().toString()),100});
            }else{
                intent.putExtra("setColors",new int[]{colorList[0],colorList[1]});
                intent.putExtra("setSplits",new int[]{0,100});
            }
        }
        intent.putExtra("setTarget",target.getSelectedItemPosition());
        setResult(RESULT_OK,intent);
        finish();

    }

    public void colorSelect(final int color,final int idx){
        AlertDialog.Builder edit_dia= new AlertDialog.Builder(Settings.this);
        final View dialogview= LayoutInflater.from(Settings.this).inflate(R.layout.color_picker,null);
        edit_dia.setTitle("Select color");
        edit_dia.setCancelable(false);
        edit_dia.setView(dialogview);
        final ImageView colorView = dialogview.findViewById(R.id.imageView);
        final SeekBar colorBars[]={
                dialogview.findViewById(R.id.redBar),
                dialogview.findViewById(R.id.greenBar),
                dialogview.findViewById(R.id.blueBar)};
        colorView.setBackgroundColor(color);
        colorBars[0].setProgress(ColoredBackground.getColorR(color));
        colorBars[1].setProgress(ColoredBackground.getColorG(color));
        colorBars[2].setProgress(ColoredBackground.getColorB(color));
        for(SeekBar bar:colorBars){
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    colorView.setBackgroundColor(ColoredBackground.getColor(
                            colorBars[0].getProgress(),
                            colorBars[1].getProgress(),
                            colorBars[2].getProgress()
                    ));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        edit_dia.setNeutralButton("cancel", null);
        edit_dia.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                colorList[idx] = ColoredBackground.getColor(
                        colorBars[0].getProgress(),
                        colorBars[1].getProgress(),
                        colorBars[2].getProgress()
                );
                colorViews[idx].setBackgroundColor(colorList[idx]);
            }
        });
        edit_dia.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        switch (id){
            case R.id.save:
                setResult();
                return true;
        }
        return false;
    }

    private Spinner target;
    private Switch flashing,gradual,customeBg;
    private View colors;
    private EditText sp2,sp3,sp4;
    private ImageView c1,c2,c3,c4,colorViews[];
    private CheckBox e3,e4;
    private int[] colorList,splits;
}
