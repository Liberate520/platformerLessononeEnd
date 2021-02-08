package com.example.platformergame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private final Joystick joystick;
    private final Player player;
    protected SurfaceHolder holder;

    private Rect grow;

    public Game(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);

        joystick = new Joystick(200, 600, 70, 40);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player_run);
        int w = bitmap.getWidth()/6;
        int h = bitmap.getHeight();
        Rect rect = new Rect(0, 0, w, h);
        player = new Player(bitmap, joystick, 0, 0, rect);

        for (int i = 1; i < 6; i++){
            player.getFrames().add(new Rect(i*w, 0, i*w+w, h));
        }

        grow = new Rect(0, 500, 500, 550);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        MyThread thread = new MyThread();
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (joystick.getIsPressed()) {
                    player.jump();
                } else if (joystick.isPressed((double) event.getX(), (double) event.getY())) {
                    joystick.setIsPressed(true);
                } else {
                    // Joystick was not previously, and is not pressed in this event -> cast spell
                    player.jump();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.getIsPressed()) {
                    // Joystick was pressed previously and is now moved
                    joystick.setActuator((double) event.getX(), (double) event.getY());
                }
                return true;

            case MotionEvent.ACTION_UP:

                joystick.setIsPressed(false);
                joystick.resetActuator();

                return true;
        }
        return super.onTouchEvent(event);
    }

    class MyThread extends Thread{

        boolean work = true;

        @Override
        public void run() {
            Canvas canvas;

            int viewWidth = getHolder().getSurfaceFrame().right;
            int viewHeight = getHolder().getSurfaceFrame().bottom;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);

            while (work){
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.BLUE);
                canvas.drawRect(grow, paint);
                joystick.update();
                player.update(1);

                if (player.getBoundingBoxRect().intersect(grow)){
                    player.setCountJump(0);
                    player.setVelocityY(0);
                    player.setY(grow.top-player.getFrameHeight());
                }
                else {
                    player.setOnGrow(false);
                }

                player.draw(canvas);
                joystick.draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
