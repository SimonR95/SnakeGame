package com.example.simon.snakegame;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;

public class SnakeView extends SurfaceView implements Runnable{
    private Thread thread = null;
    private volatile boolean playing;
    private Canvas canvas;
    private SurfaceHolder holder;
    private Paint paint;
    private Paint mousePaint;
    private Paint snakePaint;
    private Paint directionLinePaint;
    private Context context;
    private SoundPool soundpool;
    private int getMouseSound = -1;
    private int deathSound = -1;

    public enum Direction{UP, RIGHT, DOWN, LEFT}
    private Direction direction = Direction.RIGHT;

    private int screenWidth;
    private int screenHeight;

    private long previousFrameMillis;
    private long FPS = 10;
    private final long millisInASecond = 1000;

    private int score;
    private String difficultyText;

    private int[] snakeXCoords;
    private int[] snakeYCoords;

    private int snakeLength;

    private int mouseXCoords;
    private int mouseYCoords;

    private int blockSize;

    private final int numBlocksWide = 40;
    private int numBlocksHigh;

    private Boolean touch = true;

    public SnakeView(Context acontext, Point size){
        super(acontext);
        context = acontext;
        screenWidth = size.x;
        screenHeight = size.y;
        blockSize = screenWidth / numBlocksWide;
        numBlocksHigh = screenHeight / blockSize;
        loadSound();
        holder = getHolder();
        paint = new Paint();
        snakePaint = new Paint();
        mousePaint = new Paint();
        directionLinePaint = new Paint();
        snakeXCoords = new int[200];
        snakeYCoords = new int[200];

        drawMenu();
    }

    //Thread Control Methods - START//

    //The default state, either playing the game or showing the game over screen
    public void run(){
        while(playing){
            if(checkForUpdate()){
                updateGame();
                drawGame();
            }
        }
        while (!playing){
            if(checkForUpdate()){
                drawMenu();
            }
        }
    }

    //Runs when focus is lost such as when the phone is locked
    public void pause(){
        playing = false;
        try {
            thread.join();
        }catch(InterruptedException e){
        }
    }

    //Runs when focus is resumed
    public void resume(){
        thread = new Thread(this);
        thread.start();
    }
    //Thread Control Methods - END//


    //Initialisation Methods - START//

    //Sets up game logic
    public void startGame(){
        snakeLength = 1;
        snakeXCoords[0] = numBlocksWide / 2;
        snakeYCoords[0] = numBlocksHigh;

        spawnMouse();
        score = 0;
        previousFrameMillis = System.currentTimeMillis();
    }

    //Sets up Death and Score sounds
    public void loadSound(){
        soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            getMouseSound = soundpool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            deathSound = soundpool.load(descriptor, 0);
        } catch (IOException e){
        }
    }
    //Initialisation Methods - END//


    //Game Update Methods - START//

    //Checks whether time has passed since the previous frame was rendered and renders a new frame + updates previousFrameMillis to the current time if true
    public boolean checkForUpdate(){
        if(previousFrameMillis <= System.currentTimeMillis()){
            previousFrameMillis = System.currentTimeMillis() + millisInASecond / FPS;
            return true;
        }
        return false;
    }

    //Called every frame, handles game logic
    public void updateGame(){
        if(snakeXCoords[0] == mouseXCoords && snakeYCoords[0] == mouseYCoords){
            eatMouse();
        }
        moveSnake();
        if(detectDeath()){
            soundpool.play(deathSound,1,1,0,0,1);
            touch = false;
            drawMenu();
            direction = direction.UP;
        }
    }
    //Game Update Methods - END//


    //Graphic Draw Methods - START//

    //Gameplay graphics
    public void drawGame(){
        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255,86,148,247));
            paint.setColor(Color.argb(255,255,255,255));
            mousePaint.setColor(Color.argb(255,84,86,66));
            snakePaint.setColor(Color.argb(255,224,239,88));
            directionLinePaint.setColor(Color.argb(255,133,175,242));
            directionLinePaint.setTextSize(40);
            directionLinePaint.setStrokeWidth(8);
            canvas.drawLine(screenWidth / 2, 0, screenWidth / 2, screenHeight, directionLinePaint );
            paint.setTextSize(40);
            canvas.drawText("SCORE: " + score + "        " + "DIFFICULTY: " + difficultyText,10,30,paint);
            canvas.drawText("TAP TO TURN LEFT", 100 , screenHeight - 50, directionLinePaint);
            canvas.drawText("TAP TO TURN RIGHT", screenWidth / 2 + 100, screenHeight - 50, directionLinePaint);
            increaseDifficulty(score);

            canvas.drawRoundRect(new RectF(snakeXCoords[0] * blockSize,
                            snakeYCoords[0] * blockSize,
                            (snakeXCoords[0] * blockSize) + blockSize,
                            (snakeYCoords[0] * blockSize) + blockSize),10,10,
                    snakePaint);

            for(int i = 1; i < snakeLength; i++) {
                canvas.drawRoundRect(new RectF(snakeXCoords[i] * blockSize,
                                snakeYCoords[i] * blockSize,
                                (snakeXCoords[i] * blockSize) + blockSize,
                                (snakeYCoords[i] * blockSize) + blockSize),5,5,
                        snakePaint);
            }
            canvas.drawRoundRect(new RectF(mouseXCoords * blockSize,
                            mouseYCoords * blockSize,
                            (mouseXCoords * blockSize) + blockSize,
                            (mouseYCoords * blockSize) + blockSize),6,6,
                    mousePaint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    //Game Over graphics//
    public void drawMenu(){
        playing = false;
        while (!playing) {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.argb(50, 0, 0, 0));
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(60);
                canvas.drawText("SCORE : " + score, 500, 200, paint);
                canvas.drawText("TOUCH TO RESTART" , 350, 400, paint);
                holder.unlockCanvasAndPost(canvas);

            }
            if (touch) {
                playing = true;
                startGame();
            }
        }
    }
    //Graphic Draw Methods - END//


    //Moves the mouse to a new random location
    public void spawnMouse(){
        Random random = new Random();
        mouseXCoords = random.nextInt(numBlocksWide - 1) + 1;
        mouseYCoords = random.nextInt(numBlocksHigh - 1) + 1;
    }

    //Updates the Snake length and score and calls the method above to reposition the mouse
    private void eatMouse(){
        snakeLength++;
        spawnMouse();
        score++;
        soundpool.play(getMouseSound, 1 ,1 ,0 ,0 ,1);
    }

    //Updates Snake position in accordance with current direction
    private void moveSnake(){
        for(int i = snakeLength; i > 0; i--){
            snakeXCoords[i] = snakeXCoords[i - 1];
            snakeYCoords[i] = snakeYCoords[i - 1];
        }
        switch(direction){
            case UP:
                snakeYCoords[0]--;
                break;
            case RIGHT:
                snakeXCoords[0]++;
                break;
            case DOWN:
                snakeYCoords[0]++;
                break;
            case LEFT:
                snakeXCoords[0]--;
                break;
        }
    }

    //Method that performs various checks against the Snake to see if it's alive
    private boolean detectDeath(){
        boolean dead = false;
        //Checks whether the Snake is out of bounds
        if(snakeXCoords[0] == -1) dead = true;
        if(snakeXCoords[0] >= numBlocksWide) dead = true;
        if(snakeYCoords[0] == -1) dead = true;
        if(snakeYCoords[0] >= numBlocksHigh) dead = true;
        //Checks whether the Snake's head is in the same spot as any of it's body's co-ordinates and has thus hit itself
        for(int i = snakeLength - 1; i > 0; i--){
            if((snakeXCoords[0] == snakeXCoords[i]) && (snakeYCoords[0] == snakeYCoords[i])){
                dead = true;
            }
        }
        return dead;
    }



    //Increases speed of Snake in response to score
    public void increaseDifficulty(int score){
        if (score < 5) {
            FPS = 6;
            difficultyText = "EASY";
        } else if (score < 8){
            FPS = 7;
            difficultyText = "AVERAGE";
        } else if (score < 16) {
            FPS = 8;
            difficultyText = "HARD";
        } else if (score < 22) {
            FPS = 10;
            difficultyText = "INSANE";
        } else if (score < 28){
            FPS = 12;
            difficultyText = "NO LIFE";
        } else if (score < 32) {
            FPS = 14;
            difficultyText = "LEGEND";
        } else if (score < 35){
            FPS = 16;
            difficultyText = "GOD LIKE";
        } else {
            FPS = 20;
            difficultyText = "CHEATER";
        }

    }

    //Determines what direction the Snake is moving in response to touch
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        touch = true;
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                //Tapping right side of screen for clockwise turns
                if(motionEvent.getX() >= screenWidth / 2){
                    switch(direction){
                        case UP:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.UP;
                            break;
                    }
                }
                //Tapping left side of screen for anti-clockwise turns
                else{
                    switch(direction){
                        case UP:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }
}
