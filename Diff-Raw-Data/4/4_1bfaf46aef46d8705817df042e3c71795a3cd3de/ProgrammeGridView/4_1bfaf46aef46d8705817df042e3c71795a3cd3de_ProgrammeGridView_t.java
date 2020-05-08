 /*
  * Copyright 2013- Yan Bonnel
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.ybo.ybotv.android.grid;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Parcelable;
 import android.text.TextPaint;
 import android.text.TextUtils;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.activity.ProgrammeActivity;
 import fr.ybo.ybotv.android.modele.Programme;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 public class ProgrammeGridView extends View implements View.OnTouchListener {
 
     private List<Programme> programmes = new ArrayList<Programme>();
     private Date start;
     private Date currentDate;
 
     public ProgrammeGridView(Context context, AttributeSet attrs) {
         super(context, attrs);
         setOnTouchListener(this);
     }
 
     public void setProgrammes(List<Programme> programmes, Date start, Date currentDate) {
         this.programmes = new ArrayList<Programme>(programmes);
         Collections.sort(this.programmes, new Comparator<Programme>() {
             @Override
             public int compare(Programme o1, Programme o2) {
                 return o1.getStart().compareTo(o2.getStart());
             }
         });
         this.start = start;
         this.currentDate = currentDate;
     }
 
 
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         int maxOffset = 0;
         try {
            if (programmes.size() > 0) {
                maxOffset = (int) ((sdf.parse(programmes.get(programmes.size() -1).getStop()).getTime() - start.getTime()) / 1000 / 60 * sizeofminute);
            }
         } catch (ParseException ignore) {
         }
         setMeasuredDimension(maxOffset|MeasureSpec.EXACTLY, getLayoutParams().height|MeasureSpec.EXACTLY);
 
     }
 
     public static final int sizeofminute = 10;
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         if (programmes.isEmpty()) {
             return;
         }
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         SimpleDateFormat sdfForTime = new SimpleDateFormat("HH:mm");
 
         int top = 0;
         int bottom = getMeasuredHeight();
 
         Paint border = new Paint();
         border.setColor(Color.CYAN);
         border.setStrokeWidth(3);
         border.setStyle(Paint.Style.STROKE);
         TextPaint textPaint = new TextPaint();
         textPaint.setColor(Color.WHITE);
         int textSize = 20;
         textPaint.setTextSize(textSize);
 
 
         for (Programme programme : programmes) {
 
             int startOffset = 0;
             int endOffset = 0;
             String time = "";
             try {
                 Date startTime = sdf.parse(programme.getStart());
                 time = sdfForTime.format(startTime);
                 startOffset = (int) ((startTime.getTime() - start.getTime()) / 1000 / 60 * sizeofminute);
                 if (startOffset < 0) {
                     startOffset = 0;
                 }
                 endOffset = (int) ((sdf.parse(programme.getStop()).getTime() - start.getTime()) / 1000 / 60 * sizeofminute);
             } catch (ParseException ignore) {
             }
 
             int left = startOffset;
             int right = endOffset;
 
 
             Rect rect = new Rect(left, top, right, bottom);
 
             canvas.drawRect(rect, border);
 
             canvas.drawText(TextUtils.ellipsize(
                     time,
                     textPaint,
                     right - left - 4, TextUtils.TruncateAt.END).toString(),
                     left + 2, top + 2 + textSize, textPaint);
 
             canvas.drawText(TextUtils.ellipsize(
                     programme.getTitle(),
                     textPaint,
                     right - left - 4, TextUtils.TruncateAt.END).toString(),
                     left + 2, top + 6 + (textSize << 1), textPaint);
         }
 
         Paint lineNow = new Paint();
         lineNow.setColor(Color.RED);
         lineNow.setStrokeWidth(3);
 
         int lineOffset = (int) ((currentDate.getTime() - start.getTime()) / 1000 / 60 * sizeofminute);
         canvas.drawLine(lineOffset, top, lineOffset, bottom, lineNow);
 
     }
 
     private long timeDown = System.currentTimeMillis();
 
     @Override
     public boolean onTouch(View v, MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 timeDown = System.currentTimeMillis();
                 return true;
             case MotionEvent.ACTION_UP:
                 if (System.currentTimeMillis() - timeDown < 1000) {
                     float newX = event.getX();
                     Programme programme = findProgramme(newX);
                     if (programme != null) {
                         Intent intent = new Intent(getContext(), ProgrammeActivity.class);
                         intent.putExtra("programme", (Parcelable) programme);
                         getContext().startActivity(intent);
                     }
                 }
                 break;
         }
         return false;
     }
 
     private Programme findProgramme(float newX) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         for (Programme programme : programmes) {
 
             try {
                 Date startTime = sdf.parse(programme.getStart());
                 float startOffset = (startTime.getTime() - start.getTime()) / 1000 / 60 * sizeofminute;
                 float endOffset = (sdf.parse(programme.getStop()).getTime() - start.getTime()) / 1000 / 60 * sizeofminute;
                 if (newX >= startOffset && newX <= endOffset) {
                     return programme;
                 }
             } catch (ParseException ignore) {
             }
         }
         return null;
     }
 
 }
