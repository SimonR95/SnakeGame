package com.example.simon.snakegame;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Point;
import android.view.Display;

public class SnakeActivity extends Activity {
    SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        snakeView = new SnakeView(this, size);
        setContentView(snakeView);
    }

    @Override
    protected void onResume(){
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        snakeView.pause();
    }
}
