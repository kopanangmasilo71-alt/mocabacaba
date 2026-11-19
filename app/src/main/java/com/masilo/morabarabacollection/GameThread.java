package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.os.Build;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;

    public GamePanel1 gamePanel;
    public SpiderPanel spiderPanel;
    public NormalPanel normalPanel;
    public RabbitPanel rabbitPanel;
    public AlquerquePanel alquerquePanel;
    public GamePanel2 gamePanel2;
    public RabbitPannel1 rabbitPannel1;
    public RabbitPanelMultiplayer rabbitPanelMultiplayer;

    private volatile boolean running;
    private final String name;
    private boolean dirty = true;

    // Target ~60 FPS
    private static final long FRAME_TIME = 16; 

    public GameThread(SurfaceHolder surfaceHolder, GamePanel1 gamePanel, String name) {
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, SpiderPanel spiderPanel, String name) {
        this.surfaceHolder = surfaceHolder;
        this.spiderPanel = spiderPanel;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, NormalPanel normalPanel, String name) {
        this.surfaceHolder = surfaceHolder;
        this.normalPanel = normalPanel;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, RabbitPanel rabbitPanel, String name) {
        this.surfaceHolder = surfaceHolder;
        this.rabbitPanel = rabbitPanel;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, AlquerquePanel alquerquePanel, String name) {
        this.surfaceHolder = surfaceHolder;
        this.alquerquePanel = alquerquePanel;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, GamePanel2 gamePanel2, String name) {
        this.surfaceHolder = surfaceHolder;
        this.gamePanel2 = gamePanel2;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, RabbitPanelMultiplayer rabbitPanelMultiplayer, String name) {
        this.surfaceHolder = surfaceHolder;
        this.rabbitPanelMultiplayer = rabbitPanelMultiplayer;
        this.name = name;
    }

    public GameThread(SurfaceHolder surfaceHolder, RabbitPannel1 rabbitPannel1, String name) {
        this.surfaceHolder = surfaceHolder;
        this.rabbitPannel1 = rabbitPannel1;
        this.name = name;
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (running) {
            setDirty();
        } else {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public void setDirty() {
        synchronized (this) {
            dirty = true;
            notifyAll();
        }
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();
            
            // We loop continuously if running to ensure smooth autonomous animations 
            // (like particles, floating pieces). We still check 'dirty' for minor 
            // optimizations if nothing is happening, but usually something is.
            
            Canvas canvas = null;
            try {
                // Restore hardware acceleration for smooth 60fps performance.
                // The native crashes were likely due to 0-size parameters in shaders,
                // which have been fixed in the drawing classes.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canvas = surfaceHolder.lockHardwareCanvas();
                } else {
                    canvas = surfaceHolder.lockCanvas();
                }

                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        switch (name) {
                            case "gp1": gamePanel.render(canvas); break;
                            case "sp": spiderPanel.render(canvas); break;
                            case "rp": rabbitPanel.render(canvas); break;
                            case "ap": alquerquePanel.render(canvas); break;
                            case "gp2": gamePanel2.render(canvas); break;
                            case "np": normalPanel.render(canvas); break;
                            case "rp1": rabbitPannel1.render(canvas); break;
                            case "rpm": rabbitPanelMultiplayer.render(canvas); break;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore rendering errors on single frames
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {}
                }
            }

            long endTime = System.currentTimeMillis();
            long sleepTime = FRAME_TIME - (endTime - startTime);
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // If we are lagging, yield a bit
                Thread.yield();
            }
        }
    }
}
