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
    private Thread m_Thread = null;
    private volatile boolean m_Playing;
    private Canvas m_Canvas;
    private SurfaceHolder m_Holder;
    private Paint m_Paint;
    private Paint mouse_m_Paint;
    private Paint snake_m_Paint;
    private Paint direction_line_Paint;
    private Context m_context;
    private SoundPool m_SoundPool;
    private int m_get_mouse_sound = -1;
    private int m_dead_sound = -1;

    public enum Direction{UP, RIGHT, DOWN, LEFT}
    private Direction m_Direction = Direction.RIGHT;

    private int m_ScreenWidth;
    private int m_ScreenHeight;

    private long m_NextFrameTime;
    private long FPS = 10;
    private final long MILLIS_IN_A_SECOND = 1000;

    private int m_Score;
    private String Difficulty_text;

    private int[] m_SnakeXs;
    private int[] m_SnakeYs;

    private int m_SnakeLength;

    private int m_MouseX;
    private int m_MouseY;

    private int m_BlockSize;

    private final int NUM_BLOCKS_WIDE = 40;
    private int NUM_BLOCKS_HIGH;

    private Boolean touch = true;

    public SnakeView(Context context, Point size){
        super(context);
        m_context = context;
        m_ScreenWidth = size.x;
        m_ScreenHeight = size.y;
        m_BlockSize = m_ScreenWidth / NUM_BLOCKS_WIDE;
        NUM_BLOCKS_HIGH = m_ScreenHeight / m_BlockSize;
        loadSound();
        m_Holder = getHolder();
        m_Paint = new Paint();
        snake_m_Paint = new Paint();
        mouse_m_Paint = new Paint();
        direction_line_Paint = new Paint();
        m_SnakeXs = new int[200];
        m_SnakeYs = new int[200];

        drawMenu();
    }

    //Thread Control Methods - START
    public void run(){
        while(m_Playing){
            if(checkForUpdate()){
                updateGame();
                drawGame();
            }
        }
        while (!m_Playing){
            if(checkForUpdate()){
                drawMenu();
            }
        }
    }

    public void pause(){
        m_Playing = false;
        try {
            m_Thread.join();
        }catch(InterruptedException e){
        }
    }

    public void resume(){
        //m_Playing = true;
        m_Thread = new Thread(this);
        m_Thread.start();
    }
    //Thread Control Methods - END

    public void startGame(){
        m_SnakeLength = 1;
        m_SnakeXs[0] = NUM_BLOCKS_WIDE / 2;
        m_SnakeYs[0] = NUM_BLOCKS_HIGH;

        spawnMouse();
        m_Score = 0;
        m_NextFrameTime = System.currentTimeMillis();
    }

    public void loadSound(){
        m_SoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = m_context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            m_get_mouse_sound = m_SoundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            m_dead_sound = m_SoundPool.load(descriptor, 0);
        } catch (IOException e){
        }
    }

    public void spawnMouse(){
        Random random = new Random();
        m_MouseX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        m_MouseY = random.nextInt(NUM_BLOCKS_HIGH - 1) + 1;
    }

    private void eatMouse(){
        m_SnakeLength++;
        spawnMouse();
        m_Score++;
        m_SoundPool.play(m_get_mouse_sound, 1 ,1 ,0 ,0 ,1);
    }

    private void moveSnake(){
        for(int i = m_SnakeLength; i > 0; i--){
            m_SnakeXs[i] = m_SnakeXs[i - 1];
            m_SnakeYs[i] = m_SnakeYs[i - 1];
        }
        switch(m_Direction){
            case UP:
                m_SnakeYs[0]--;
                break;
            case RIGHT:
                m_SnakeXs[0]++;
                break;
            case DOWN:
                m_SnakeYs[0]++;
                break;
            case LEFT:
                m_SnakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath(){
        boolean dead = false;
        if(m_SnakeXs[0] == -1) dead = true;
        if(m_SnakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if(m_SnakeYs[0] == -1) dead = true;
        if(m_SnakeYs[0] >= NUM_BLOCKS_HIGH) dead = true;
        for(int i = m_SnakeLength - 1; i > 0; i--){
            if((i > 4) && (m_SnakeXs[0] == m_SnakeXs[i]) && (m_SnakeYs[0] == m_SnakeYs[i])){
                dead = true;
            }
        }
        return dead;
    }

    public void updateGame(){
        if(m_SnakeXs[0] == m_MouseX && m_SnakeYs[0] == m_MouseY){
            eatMouse();
        }
        moveSnake();
        if(detectDeath()){
            m_SoundPool.play(m_dead_sound,1,1,0,0,1);
            touch = false;
            drawMenu();
            m_Direction = m_Direction.UP;
        }
    }
    public void increaseDifficulty(int m_score){
        if (m_score < 5) {
            FPS = 6;
            Difficulty_text = "EASY";
        } else if (m_score < 8){
            FPS = 7;
            Difficulty_text = "AVERAGE";
        } else if (m_score < 16) {
            FPS = 8;
            Difficulty_text = "HARD";
        } else if (m_score < 22) {
            FPS = 10;
            Difficulty_text = "INSANE";
        } else if (m_score < 28){
            FPS = 12;
            Difficulty_text = "NO LIFE";
        } else if (m_score < 32) {
            FPS = 14;
            Difficulty_text = "LEGEND";
        } else if (m_score < 35){
            FPS = 16;
            Difficulty_text = "GOD LIKE";
        } else {
            FPS = 20;
            Difficulty_text = "CHEATER";
        }

    }
    RectF RectF = new RectF(
            m_BlockSize, // left
            m_BlockSize, // top
            m_ScreenWidth - m_BlockSize, // right
            m_ScreenHeight - m_BlockSize // bottom
    );

    public void drawGame(){
        if(m_Holder.getSurface().isValid()){
            m_Canvas = m_Holder.lockCanvas();
            m_Canvas.drawColor(Color.argb(255,86,148,247));
            m_Paint.setColor(Color.argb(255,255,255,255));
            mouse_m_Paint.setColor(Color.argb(255,84,86,66));
            snake_m_Paint.setColor(Color.argb(255,224,239,88));
            direction_line_Paint.setColor(Color.argb(255,133,175,242));
            direction_line_Paint.setTextSize(40);
            direction_line_Paint.setStrokeWidth(8);
            m_Canvas.drawLine(m_ScreenWidth / 2, 0, m_ScreenWidth / 2, m_ScreenHeight, direction_line_Paint );
            m_Paint.setTextSize(40);
            m_Canvas.drawText("SCORE: " + m_Score + "        " + "DIFFICULTY: " + Difficulty_text,10,30,m_Paint);
            m_Canvas.drawText("TAP TO TURN LEFT", m_ScreenWidth - m_ScreenWidth + 100 , m_ScreenHeight - 50, direction_line_Paint);
            m_Canvas.drawText("TAP TO TURN RIGHT", m_ScreenWidth / 2 + 100, m_ScreenHeight - 50, direction_line_Paint);
            increaseDifficulty(m_Score);

            m_Canvas.drawRoundRect(new RectF(m_SnakeXs[0] * m_BlockSize,
                            m_SnakeYs[0] * m_BlockSize,
                            (m_SnakeXs[0] * m_BlockSize) + m_BlockSize,
                            (m_SnakeYs[0] * m_BlockSize) + m_BlockSize),10,10,
                    snake_m_Paint);

            for(int i = 1; i < m_SnakeLength; i++) {
                m_Canvas.drawRoundRect(new RectF(m_SnakeXs[i] * m_BlockSize,
                        m_SnakeYs[i] * m_BlockSize,
                        (m_SnakeXs[i] * m_BlockSize) + m_BlockSize,
                        (m_SnakeYs[i] * m_BlockSize) + m_BlockSize),5,5,
                        snake_m_Paint);
            }
            m_Canvas.drawRoundRect(new RectF(m_MouseX * m_BlockSize,
                    m_MouseY * m_BlockSize,
                    (m_MouseX * m_BlockSize) + m_BlockSize,
                    (m_MouseY * m_BlockSize) + m_BlockSize),6,6,
                    mouse_m_Paint);

            m_Holder.unlockCanvasAndPost(m_Canvas);
        }
    }
    public void drawMenu(){
        m_Playing = false;
        while (!m_Playing) {
            if (m_Holder.getSurface().isValid()) {
                m_Canvas = m_Holder.lockCanvas();
                m_Canvas.drawColor(Color.argb(50, 0, 0, 0));
                m_Paint.setColor(Color.argb(255, 255, 255, 255));
                m_Paint.setTextSize(60);
                m_Canvas.drawText("SCORE : " + m_Score, 500, 200, m_Paint);
                m_Canvas.drawText("TOUCH TO RESTART" , 350, 400, m_Paint);
                m_Holder.unlockCanvasAndPost(m_Canvas);

            }
            if (touch) {
                m_Playing = true;
                startGame();
            }
        }
    }

    public boolean checkForUpdate(){
        if(m_NextFrameTime <= System.currentTimeMillis()){
            m_NextFrameTime = System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        touch = true;
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                if(motionEvent.getX() >= m_ScreenWidth / 2){
                    switch(m_Direction){
                        case UP:
                            m_Direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_Direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_Direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_Direction = Direction.UP;
                            break;
                    }
                }
                else{
                    switch(m_Direction){
                        case UP:
                            m_Direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_Direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_Direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_Direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }
}
