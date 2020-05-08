 package uk.ac.ic.doc.protein_factory;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.util.Log;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Stack;
 
 public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback
 {
     private MainThread mainThread;
     private ShiftThread shiftThread;
     protected Random gen = new Random();
     private List<RNANucleotide> rnaNucleotides = new LinkedList<RNANucleotide>();
     private Stack<RNANucleotide> unusedNucleotides = new Stack<RNANucleotide>();
     private List<DNANucleotide> backbone = new LinkedList<DNANucleotide>();
     private static final String TAG = MainGamePanel.class.getSimpleName();
     private static final int rnaCount = 20;
 
     public MainGamePanel(Context c)
     {
         super(c);
 
         DisplayMetrics displaymetrics = c.getResources().getDisplayMetrics();
 
         for (int i = 0; i < rnaCount; i++)
             rnaNucleotides.add(new RNANucleotide(c, gen));
         
         for (int i = 0; i < displaymetrics.widthPixels / 50; i++)
             backbone.add(new DNANucleotide(c,gen,i));
 
         getHolder().addCallback(this);
         mainThread = new MainThread(getHolder(),this);
         shiftThread = new ShiftThread(getHolder(),this);
         setFocusable(true);
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder h, int format, int width, int height)
     {
 
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder h)
     {
         mainThread.setRunning(true);
         mainThread.start();
         shiftThread.setRunning(true);
         shiftThread.start();

     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder h)
     {
         Log.d(TAG,"Surface being destroyed");
 
         mainThread.setRunning(false);
         shiftThread.setRunning(false);
         ((Activity)getContext()).finish();
 
         // Not Reached?
         boolean retry = true;
         while (retry)
         {
             try
             {
                 mainThread.join();
                 retry = false;
             }
             catch (InterruptedException e)
             {
                 // try shutting the thread down again
             }
         }
         retry = true;
         while (retry)
         {
             try
             {
                 shiftThread.join();
                 retry = false;
             }
             catch (InterruptedException e)
             {
                 // try shutting the thread down again
             }
         }
 
         Log.d(TAG,"Thread has been shut down cleanly");
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent e)
     {
         if (e.getAction() == MotionEvent.ACTION_DOWN)
         {
             // If the action is a touch, this will get the "closest" rna icon
 
             int distX,distY;
             RNANucleotide closest = rnaNucleotides.get(0);
 
             // Taking absolute value expensive - just leave it like that, squaring will fix it
             distX = closest.getX() - (int)e.getX();
             distY = closest.getY() - (int)e.getY();
 
             // Square routing is expensive - just use distance squared
             int distance_squared = (distX * distX) + (distY * distY);
             int current_distance_squared;
 
             for (RNANucleotide rna : rnaNucleotides)
             {
                 distX = rna.getX() - (int)e.getX();
                 distY = rna.getY() - (int)e.getY();
                 current_distance_squared = (distX * distX) + (distY * distY);
                 if (current_distance_squared < distance_squared)
                 {
                     distance_squared = current_distance_squared;
                     closest = rna;
                 }
             }
 
             closest.actionDown((int)e.getX(),(int)e.getY());
 
             Log.d(TAG,"Coords: x=" + e.getX() + ",y=" + e.getY());
         }
         if (e.getAction() == MotionEvent.ACTION_MOVE)
         {
             for (RNANucleotide rna : rnaNucleotides)
             {
                 if (rna.isTouched())
                 {
                     rna.setX((int)e.getX() + gen.nextInt(3) - 1);
                     rna.setY((int)e.getY() + gen.nextInt(3) - 1);
                 }
             }
         }
         if (e.getAction() == MotionEvent.ACTION_UP)
         {
             for (RNANucleotide rna : rnaNucleotides)
                 rna.setTouched(false);
         }
         return true;
     }
 
     @Override
     protected void onDraw(Canvas canvas)
     {
        renderBackBone(canvas);
         canvas.drawColor(Color.GREEN);
         for (RNANucleotide rna : rnaNucleotides)
         {
             if (!rna.isTouched())
             {
                 rna.setX(rna.getX() + gen.nextInt(3) - 1);
                 rna.setY(rna.getY() + gen.nextInt(3) - 1);
                 rna.draw(canvas);
             }
             else
             {
                 rna.draw(canvas);
             }
         }
     }
 
     protected void onShift(Canvas canvas)
     {
         canvas.drawColor(Color.GREEN);
         renderBackBone(canvas);
         boolean updated = false;
 
         for (RNANucleotide rna : rnaNucleotides)
         {
             if (!rna.isTouched())
             {
                 rna.setX(rna.getX() + 5);
                 if (rna.getX() > getWidth())
                 {
                     unusedNucleotides.push(rna);
                     updated = true;
                     Log.d(TAG,"Put a nucleotide on the stack");
                 }
             }
             rna.draw(canvas);
         }
 
         if (updated)
         {
             for (RNANucleotide rna : unusedNucleotides)
             {
                 rnaNucleotides.remove(rna);
             }
         }
     }
 
     protected void renderBackBone(Canvas canvas)
     {
         // Every thread needs to call this if they fuck with the canvas, as far as i can tell
         for (DNANucleotide dna : backbone)
             dna.draw(canvas);
 
     }
 }
