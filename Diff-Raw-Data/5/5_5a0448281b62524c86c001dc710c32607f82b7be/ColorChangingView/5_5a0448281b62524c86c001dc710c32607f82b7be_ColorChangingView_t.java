 package com.github.colorishi;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.speech.tts.TextToSpeech;
 import android.view.*;
 
 public class ColorChangingView extends SurfaceView implements SurfaceHolder.Callback {
 
     private static final String WELCOME_MESSAGE = "Welcome to Colorishi! Touch the screen to start.";
     private static final int TWO_SECONDS = 2 * 1000;
     private long lastTrigger = 0;
 
     public static final class C {
         final int color;
         final String name;
 
         public C(int color, String name) {
             this.color = color;
             this.name = name;
         }
 
         public void sayIt(TextToSpeech tts) {
             if (tts != null) {
                tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
             }
         }
     }
 
     private static C[] COLORS = new C[]{
             new C(Color.RED, "Red"),
             new C(Color.GREEN, "Green"),
             new C(Color.BLUE, "Blue"),
             new C(Color.CYAN, "Cyan"),
             new C(Color.MAGENTA, "Magenta"),
             new C(Color.YELLOW, "Yellow"),
             new C(Color.rgb(0xdc, 0x14, 0x3c), "Crimson"),
             new C(Color.GRAY, "Grey"),
             new C(Color.rgb(0x4b, 0x00, 0x82), "Indigo"),
             new C(Color.WHITE, "White"),
             new C(Color.rgb(0xff, 0x9a, 0xcc), "Pink"),
             new C(Color.BLACK, "Black"),
             new C(Color.rgb(0xff, 0xa5, 0x00), "Orange"),
             new C(Color.rgb(0x3e, 0x04, 0x5b), "Purple"),
             new C(Color.rgb(0x8f, 0xbc, 0x8f), "Dark Sea Green"),
             new C(Color.rgb(0xb2, 0x22, 0x22), "Fire Brick"),
             new C(Color.rgb(0xad, 0xff, 0x2f), "Green Yellow"),
             new C(Color.rgb(0x40, 0xe0, 0xd0), "Turquoise"),
             new C(Color.rgb(0xff, 0xa0, 0x7a), "Light Salmon"),
             new C(Color.rgb(0xa0, 0x52, 0x2d), "Sienna"),
             new C(Color.rgb(0x00, 0x00, 0x80), "Navy"),
             new C(Color.rgb(0xdd, 0xa0, 0xdd), "Plum"),
             new C(Color.rgb(0x80, 0x00, 0x00), "Maroon"),
             new C(Color.rgb(0x80, 0x80, 0x00), "Olive"),
             new C(Color.rgb(0xa5, 0x2a, 0x2a), "Brown"),
             new C(Color.rgb(0x7f, 0xff, 0xd4), "Aqua"),
             new C(Color.rgb(0xde, 0xb8, 0x87), "Burly Wood"),
             new C(Color.rgb(0x00, 0xff, 0x7f), "Spring Green")
     };
 
     private int currentColorIdx;
     private volatile TextToSpeech mTts;
 
     public ColorChangingView(Context context) {
         super(context);
         currentColorIdx = 0;
         getHolder().addCallback(this);
     }
 
     public void surfaceCreated(SurfaceHolder surfaceHolder) {
         refresh(surfaceHolder);
     }
 
     public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        
     }
 
     private void refresh(SurfaceHolder surfaceHolder) {
         Canvas canvas = surfaceHolder.lockCanvas();
         C c = COLORS[currentColorIdx];
         canvas.drawColor(c.color);
         c.sayIt(mTts);
         surfaceHolder.unlockCanvasAndPost(canvas);
     }
 
     public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
 
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (isChangeDue()) {
             currentColorIdx++;
             currentColorIdx %= COLORS.length;
             refresh(getHolder());
         }
         return true;
     }
 
     private boolean isChangeDue() {
         long current = System.currentTimeMillis();
         if (current > lastTrigger + TWO_SECONDS) {
             markChanged(current);
             return true;
         }
         return false;
     }
 
     private void markChanged(long current) {
         lastTrigger = current;
     }
 
     public void setTts(TextToSpeech mTts) {
         this.mTts = mTts;
         mTts.speak(WELCOME_MESSAGE, TextToSpeech.QUEUE_FLUSH, null);
     }
 }
