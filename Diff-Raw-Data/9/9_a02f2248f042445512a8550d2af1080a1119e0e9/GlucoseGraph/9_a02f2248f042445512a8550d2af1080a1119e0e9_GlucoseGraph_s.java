 package se.huffyreek.glucosejournal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class GlucoseGraph extends View {
     private static final String TAG = "GlucoseJournal.GlucoseGraph";
     Paint redPaint = new Paint();
     Paint glucosePoint = new Paint();
     Paint glucosePointFill = new Paint();
     Paint glucosePointGood = new Paint();
     Paint glucosePointFillGood = new Paint();
     Paint lines = new Paint();
     public List<JournalEntry> journalEntries;
     public int entrySecondsPerPixel = 120;
     public long endTime;
     private int w;
     private int h;
     private int capTop = 20;
     private int capBottom = 1;
     private float valueLow = 3.6f;
     private float valueGood = 10.0f;
     private float valueHigh = 15.0f;
     public int futureRange = 120*60;
 
 
     public GlucoseGraph(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         redPaint.setColor(Color.RED);
         glucosePoint.setColor(Color.rgb(160, 160, 255));
         glucosePoint.setStyle(Style.STROKE);
         glucosePointFill.setColor(Color.rgb(100, 100, 255));
         glucosePointFill.setStyle(Style.FILL);
 
         glucosePointGood.setColor(Color.rgb(160, 255, 160));
         glucosePointGood.setStyle(Style.STROKE);
         glucosePointFillGood.setColor(Color.rgb(100, 255, 100));
         glucosePointFillGood.setStyle(Style.FILL);
 
         lines.setColor(Color.GRAY);
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
         this.w = w;
         this.h = h;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         JournalEntry lastEntry = null;
         super.onDraw(canvas);
 
         canvas.drawLine(0, futureRange/entrySecondsPerPixel, w, futureRange/entrySecondsPerPixel, lines);
 
         canvas.drawLine(glucoseToX(valueLow), 0, glucoseToX(valueLow), h, lines);
         canvas.drawLine(glucoseToX(valueGood), 0, glucoseToX(valueGood), h, lines);
         canvas.drawLine(glucoseToX(valueHigh), 0, glucoseToX(valueHigh), h, lines);
 
         if (journalEntries != null) {
 
             for (JournalEntry entry : journalEntries) {
                if (lastEntry != null) {
                    canvas.drawLine(calculateX(lastEntry), calculateY(lastEntry), calculateX(entry), calculateY(entry), glucosePointFill);
                 }
                lastEntry = entry;
             }
 
             for (JournalEntry entry : journalEntries) {
                 drawGlucosePoint(canvas, entry);
             }
         }
     }
 
     private float[] calculateAllLocations() {
         float[] values = new float[journalEntries.size()*2];
         int i = 0;
         for (JournalEntry entry : journalEntries) {
             values[i*2] = calculateX(entry);
             values[i*2+1] = calculateY(entry);
             i++;
         }
         return values;
     }
 
     private Integer glucoseToX(Float glucose) {
         return (int)(w-((glucose-capBottom)*(w/(capTop-capBottom))));
     }
 
     private Integer calculateX(JournalEntry entry) {
         return glucoseToX(MainActivity.isFloat(entry.glucose) ? Float.parseFloat(entry.glucose) : 0);
     }
 
     private Integer calculateY(JournalEntry entry) {
         return (int)((endTime-entry.at.toMillis(false))/(entrySecondsPerPixel*1000));
     }
 
     private void drawGlucosePoint(Canvas canvas, JournalEntry entry) {
         int x = calculateX(entry);
         int y = calculateY(entry);
         if (MainActivity.isFloat(entry.glucose) && Float.parseFloat(entry.glucose) >= valueLow && Float.parseFloat(entry.glucose) <= valueGood) {
             canvas.drawCircle(x, y, 6, glucosePointGood);
             canvas.drawCircle(x, y, 5, glucosePointFillGood);
         } else {
             canvas.drawCircle(x, y, 6, glucosePoint);
             canvas.drawCircle(x, y, 5, glucosePointFill);
         }
     }
 }
