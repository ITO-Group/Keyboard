package com.example.zhoujianyu.keyboard;

import android.graphics.Point;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    String alpha_map[]=new String[]{""," ","q","a","z","w","s","x","e","d","c","r","f","v","t","g","b","y","h","n","u","j","m","i","k","del","o","l","p","enter","",""};
    TextView textView;
    public static final int ROW_NUM = 30;
    public static final int COL_NUM = 16;
    public static final int PRESS_THR =100;
    public static final int PRESS_INTERVAL = 200;
    public int screenWidth;
    public int screenHeight;
    public int capaWidth;
    public int capaHeight;
    public int capacity_data1[][] = new int[ROW_NUM][COL_NUM];
    public int capacity_data2[][] = new int[ROW_NUM][COL_NUM];
    public int diff_data[][] = new int[ROW_NUM][COL_NUM];
    boolean first = true;
    long last_time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.sample_text);
        init();
        readDiffStart();
    }

    public void init(){
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        capaWidth = screenWidth/COL_NUM;
        capaHeight = screenHeight/ROW_NUM;
    }
    public void updateCapacity(short[] data){
        for(int i = 0;i<ROW_NUM;i++) {
            for (int j = 0; j < COL_NUM; j++) {
                capacity_data1[i][j] = capacity_data2[i][j];
                capacity_data2[i][j] = data[i * COL_NUM + j];
                if (!first) {
                    diff_data[i][j] = capacity_data2[i][j] - capacity_data1[i][j];
                }
                else first = false;
            }
        }
    }
    public int keyboardAnalyze(){
        if(!first){
            // find most significant up
            int max = -1; int max_row = -1;
            for(int i = 0;i<ROW_NUM;i++){
                if(max<diff_data[i][0]) {
                    max = diff_data[i][0];
                    max_row = i;
                }
            }
            Log.e("bug",Integer.toString(max)+","+Integer.toString(max_row));
             if(max>PRESS_THR && max_row<30 && max_row>=0) {
                return max_row;
            }
            else return -1;
        }
        return -1;
    }

    /**
     * callback method after everytime native_lib.cpp read an image of capacity data
     * The function first convert
     * @param data: 32*16 short array
     */
    public void processDiff(short[] data) throws InterruptedException{
        updateCapacity(data);
        final int key = keyboardAnalyze();
        long time_interval = System.currentTimeMillis()-last_time;
        if(key!=-1&&time_interval>PRESS_INTERVAL){
            last_time = System.currentTimeMillis();
            Log.e("bug",alpha_map[key]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String text = textView.getText().toString();
                    if(alpha_map[key].equals("del")){
                        if(text.length()>0) text = text.substring(0,text.length()-1);
                        text =  "";
                    }
                    else{
//                        if(!alpha_map[key].equals("x")){
//                        }
                        text += alpha_map[key];
                    }
                    textView.setTextSize(70);
                    textView.setText(text);
                }
            });
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void readDiffStart ();
    public native void readDiffStop ();
}
