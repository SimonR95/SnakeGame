package com.example.simon.snakegame;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Point;
import android.view.Display;

public class SnakeActivity extends Activity {
    SnakeView snakeView;

    //Gets the current phone screen size and creates SnakeView with this size as a parameter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        snakeView = new SnakeView(this, size);
        setContentView(snakeView);
    }

    //Called when the app focus is resumed
    @Override
    protected void onResume(){
        super.onResume();
        snakeView.resume();
    }

    //Called when the app loses focus such as when locking the phone
    @Override
    protected void onPause(){
        super.onPause();
        snakeView.pause();
    }

}
