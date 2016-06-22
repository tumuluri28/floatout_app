package com.floatout.android.floatout_v01;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CameraCaptureActivity extends AppCompatActivity {
    ImageView im;
    TextView tv;
    String path;
    File f;
    private Size imageDimension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        setContentView(R.layout.activity_camera_capture);
        im = (ImageView) findViewById(R.id.image);
        //tv = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        Bitmap bm;
        try{
            f = new File(path, "image.jpg");
            bm = BitmapFactory.decodeStream(new FileInputStream(f));
            /*int width = im.getWidth();
            int height = im.getHeight();
            float scaledWidth = metrics.scaledDensity;
            float scaledHeight = metrics.scaledDensity;
            Matrix matrix = new Matrix();
            matrix.postScale(scaledWidth,scaledHeight);
            bm.setWidth(width);
            bm.setHeight(height);*/
            //Bitmap bp = Bitmap.createBitmap(bm,0,0,width,height,matrix,true);
            im.setImageBitmap(bm);

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }finally {
            f.delete();
        }
        //tv.setText(path);

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        //fullScreen();

        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

    private void fullScreen(){
        View decorView = getWindow().getDecorView();
        int uioptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uioptions);
    }
}
