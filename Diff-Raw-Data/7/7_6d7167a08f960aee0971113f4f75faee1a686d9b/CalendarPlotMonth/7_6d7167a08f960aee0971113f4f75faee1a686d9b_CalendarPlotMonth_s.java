 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.redgeek.android.eventrend.calendar;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import net.redgeek.android.eventrend.Preferences;
 import net.redgeek.android.eventrend.calendar.CalendarPlot.PaintIndex;
 import net.redgeek.android.eventrend.primitives.Datapoint;
 import net.redgeek.android.eventrend.primitives.TimeSeries;
 import net.redgeek.android.eventrend.primitives.TimeSeriesCollector;
 import net.redgeek.android.eventrend.primitives.Tuple;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.Number;
 import net.redgeek.android.eventrend.util.DateUtil.Period;
 import net.redgeek.android.eventrend.util.Number.TrendState;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RectF;
 
 public class CalendarPlotMonth {
   // UI elements
   private ArrayList<Paint> mPaints;
 
   // Private data
   private Context mCtx;
   private TimeSeriesCollector mTSC;
   private DateUtil mDates;
   private float mSensitivity;
   private float mCellWidth;
   private float mCellHeight;
   private float mColorHeight;
 
   private Tuple mDimensions;
   private long mStartMS;
   private long mFocusStart;
   private long mFocusEnd;
 
   public CalendarPlotMonth(Context context, TimeSeriesCollector tsc,
       ArrayList<Paint> paints, Tuple dimensions) {
     mTSC = tsc;
     mCtx = context;
     mPaints = paints;
     setupData(dimensions);
   }
 
   private void setupData(Tuple dimensions) {
     mDimensions = new Tuple();
     setDimensions(dimensions);
     mDates = new DateUtil();
     mSensitivity = Preferences.getStdDevSensitivity(mCtx);
     setCellSizes();
   }
 
   public void setDimensions(Tuple dimensions) {
     mDimensions.set(dimensions);
     setCellSizes();
   }
 
   public void setStart(long startMs) {
     mStartMS = startMs;
   }
 
   private void setCellSizes() {
     mCellHeight = (mDimensions.y) / 6.0f;
     mCellWidth = (mDimensions.x) / 7.0f;
     mColorHeight = mCellHeight / mTSC.numSeries();
   }
 
   public synchronized void plot(Canvas canvas) {
     if (canvas == null)
       return;
 
     if (mDimensions.x <= 0 || mDimensions.y <= 0)
       return;
 
     drawMonth(canvas);
 
     return;
   }
 
   private int positionToRow(int position) {
     return position / 7;
   }
 
   private int positionToColumn(int position) {
     return position % 7;
   }
 
   private Tuple getCellTopLeft(int position) {
     Tuple t = new Tuple();
     t.x = (positionToColumn(position) * mCellWidth);
     t.y = (positionToRow(position) * mCellHeight);
     return t;
   }
 
   private Tuple getCellBottomRight(int position) {
     Tuple t = new Tuple();
     t.x = (positionToColumn(position) * mCellWidth) + mCellWidth;
     t.y = (positionToRow(position) * mCellHeight) + mCellHeight;
     return t;
   }
 
   private Paint mapTrendStateToPaint(TrendState state) {
     if (state == TrendState.UP_GOOD_BIG || state == TrendState.DOWN_GOOD_BIG)
       return mPaints.get(PaintIndex.DATUM_GOOD4.ordinal());
     else if (state == TrendState.UP_GOOD_SMALL
         || state == TrendState.DOWN_GOOD_SMALL)
       return mPaints.get(PaintIndex.DATUM_GOOD2.ordinal());
     else if (state == TrendState.DOWN_BAD_BIG
         || state == TrendState.DOWN_BAD_SMALL)
       return mPaints.get(PaintIndex.DATUM_BAD4.ordinal());
     else if (state == TrendState.UP_BAD_BIG || state == TrendState.UP_BAD_SMALL)
       return mPaints.get(PaintIndex.DATUM_BAD2.ordinal());
     else if (state == TrendState.UP_SMALL || state == TrendState.DOWN_SMALL
         || state == TrendState.EVEN)
       return mPaints.get(PaintIndex.DATUM_EVEN.ordinal());
     else if (state == TrendState.EVEN_GOAL)
       return mPaints.get(PaintIndex.DATUM_EVEN_GOAL.ordinal());
     return null;
   }
 
   private int setStartTime() {
     int focusMonth;
 
     Calendar cal = mDates.getCalendar();
     cal.setTimeInMillis(mStartMS);
     DateUtil.setToPeriodStart(cal, Period.MONTH);
     long ms = cal.getTimeInMillis();
     mDates.setBaseTime(ms);
 
     int month = mDates.get(Calendar.MONTH);
     int position = mDates.get(Calendar.DAY_OF_WEEK);
 
     focusMonth = month;
 
     if (position == cal.getFirstDayOfWeek()) {
       mDates.advance(Period.DAY, -7);
     } else {
       mDates.advance(Period.DAY, -position + 1);
     }
 
     return focusMonth;
   }
 
   private void drawMonthDayValue(Canvas canvas, int position, float prevValue,
       float thisValue, float goal, float stdDev) {
     Paint p;
     Tuple topLeft = getCellTopLeft(position);
     Tuple bottomRight = getCellBottomRight(position);
 
     float unit = stdDev * mSensitivity;
     float half = unit / 2;
     float quarter = half / 2;
 
     float delta = thisValue - prevValue;
     float absDelta = Math.abs(delta);
     if (absDelta > 0 && absDelta > quarter) {
       if ((delta > 0 && goal > thisValue) || (delta < 0 && goal < thisValue)) {
         if (delta > unit)
           p = mPaints.get(PaintIndex.DATUM_GOOD4.ordinal());
         else if (delta > half + quarter)
           p = mPaints.get(PaintIndex.DATUM_GOOD3.ordinal());
         else if (delta > half)
           p = mPaints.get(PaintIndex.DATUM_GOOD2.ordinal());
         else
           // if (delta > quarter)
           p = mPaints.get(PaintIndex.DATUM_GOOD1.ordinal());
       } else {
         if (delta > unit)
           p = mPaints.get(PaintIndex.DATUM_BAD4.ordinal());
         else if (delta > half + quarter)
           p = mPaints.get(PaintIndex.DATUM_BAD3.ordinal());
         else if (delta > half)
           p = mPaints.get(PaintIndex.DATUM_BAD2.ordinal());
         else
           // if (delta > quarter)
           p = mPaints.get(PaintIndex.DATUM_BAD1.ordinal());
       }
     } else {
       // even
       if (Math.abs(thisValue - goal) < half)
         p = mPaints.get(PaintIndex.DATUM_EVEN_GOAL.ordinal());
       else
         p = mPaints.get(PaintIndex.DATUM_EVEN.ordinal());
     }
 
     // Paint p = mPaints.get(PaintIndex.VALUE.ordinal());
     canvas.drawText("" + Number.Round(thisValue), topLeft.x + 3,
         bottomRight.y - 4, p);
   }
 
   private void drawMonthDayBackground(Canvas canvas, boolean focused,
       int position, int date, float lastTrend, float thisTrend, float goal,
       float stdDev) {
     Paint p = null;
 
     Tuple topLeft = getCellTopLeft(position);
     Tuple bottomRight = getCellBottomRight(position);
     // RectF cell = new RectF(topLeft.x, topLeft.y, bottomRight.x,
     // bottomRight.y);
     RectF cell = new RectF(topLeft.x, topLeft.y, topLeft.x + 20, topLeft.y + 18);
 
     float unit = stdDev * mSensitivity;
     float half = unit / 2;
     float quarter = half / 2;
 
     float delta = thisTrend - lastTrend;
     float absDelta = Math.abs(delta);
     if (absDelta > 0 && absDelta > quarter) {
       if ((delta > 0 && goal > thisTrend) || (delta < 0 && goal < thisTrend)) {
         if (delta > unit)
           p = mPaints.get(PaintIndex.DATUM_GOOD4.ordinal());
         else if (delta > half + quarter)
           p = mPaints.get(PaintIndex.DATUM_GOOD3.ordinal());
         else if (delta > half)
           p = mPaints.get(PaintIndex.DATUM_GOOD2.ordinal());
         else
           // if (delta > quarter)
           p = mPaints.get(PaintIndex.DATUM_GOOD1.ordinal());
       } else {
         if (delta > unit)
           p = mPaints.get(PaintIndex.DATUM_BAD4.ordinal());
         else if (delta > half + quarter)
           p = mPaints.get(PaintIndex.DATUM_BAD3.ordinal());
         else if (delta > half)
           p = mPaints.get(PaintIndex.DATUM_BAD2.ordinal());
         else
           // if (delta > quarter)
           p = mPaints.get(PaintIndex.DATUM_BAD1.ordinal());
       }
     } else {
       // even
       if (Math.abs(thisTrend - goal) < half)
         p = mPaints.get(PaintIndex.DATUM_EVEN_GOAL.ordinal());
       else
         p = null;
       // p = mPaints.get(PaintIndex.DATUM_EVEN.ordinal());
     }
 
     if (p != null) {
       canvas.drawRect(cell, p);
     }
   }
 
   private void drawMonthDayBorder(Canvas canvas, boolean focused, int position,
       int date) {
     Tuple topLeft = getCellTopLeft(position);
     Tuple bottomRight = getCellBottomRight(position);
 
     Paint p;
     if (focused == true) {
       p = mPaints.get(PaintIndex.BORDER_SECONDARY.ordinal());
       RectF cell = new RectF(topLeft.x, topLeft.y, topLeft.x + 20,
           topLeft.y + 18);
       canvas.drawRect(cell, p);
 
       cell = new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
       canvas.drawRect(cell, p);
 
       p = mPaints.get(PaintIndex.LABEL.ordinal());
       canvas.drawText("" + date, topLeft.x + 3, topLeft.y
           + CalendarView.TEXT_HEIGHT + 3, p);
 
     } else {
       p = mPaints.get(PaintIndex.BORDER_SECONDARY.ordinal());
       Path border = new Path();
 
       border.moveTo(topLeft.x, topLeft.y + 18);
       border.lineTo(topLeft.x + 20, topLeft.y + 18);
       border.lineTo(topLeft.x + 20, topLeft.y);
 
       if (position < 14) {
         border.moveTo(topLeft.x, bottomRight.y);
         border.lineTo(topLeft.x, topLeft.y);
         border.lineTo(bottomRight.x, topLeft.y);
         border.lineTo(bottomRight.x, bottomRight.y);
 
       } else {
         border.moveTo(topLeft.x, topLeft.y);
         border.lineTo(topLeft.x, bottomRight.y);
         border.lineTo(bottomRight.x, bottomRight.y);
         border.lineTo(bottomRight.x, topLeft.y);
       }
       canvas.drawPath(border, p);
       p = mPaints.get(PaintIndex.LABEL.ordinal());
       canvas.drawText("" + date, topLeft.x + 3, topLeft.y
           + CalendarView.TEXT_HEIGHT + 3, p);
 
     }
   }
 
   private void drawFocusBorder(Canvas canvas, int firstPosition,
       int lastPosition) {
     Tuple start = new Tuple();
     Tuple corner1, corner2;
     int firstRow = positionToRow(firstPosition);
     int firstCol = positionToColumn(firstPosition);
     int lastRow = positionToRow(lastPosition);
     int lastCol = positionToColumn(lastPosition);
 
     Path border = new Path();
 
     corner1 = getCellTopLeft(firstPosition);
     corner2 = getCellBottomRight(firstPosition);
     start.x = corner1.x;
     start.y = corner2.y;
 
     border.moveTo(start.x, start.y);
     border.lineTo(corner1.x, corner1.y);
 
     corner2 = getCellBottomRight(firstPosition - 1 + (7 - (firstPosition % 7)));
     border.lineTo(corner2.x, corner1.y);
 
     corner1 = getCellBottomRight(lastPosition - ((lastPosition + 1) % 7));
     border.lineTo(corner2.x, corner1.y);
 
     corner2 = getCellTopLeft(lastPosition - 6);
     border.lineTo(corner2.x, corner1.y);
 
     corner1 = getCellBottomRight(lastPosition);
     border.lineTo(corner2.x, corner1.y);
 
     corner2 = getCellTopLeft(lastPosition - (lastPosition % 7));
     border.lineTo(corner2.x, corner1.y);
 
     corner1 = getCellTopLeft(firstPosition + (7 - ((firstPosition - 1) % 7)));
     border.lineTo(corner2.x, corner1.y);
 
     border.lineTo(start.x, start.y);
 
     Paint p = mPaints.get(PaintIndex.BORDER_PRIMARY.ordinal());
     canvas.drawPath(border, p);
     p = mPaints.get(PaintIndex.BORDER_HIGHLIGHT.ordinal());
     canvas.drawPath(border, p);
   }
 
   private void drawMonth(Canvas canvas) {
     Calendar tmp = Calendar.getInstance();
     TimeSeries ts;
     Datapoint prev, current;
     int focusMonth;
     int firstPosition = 0;
     int lastPosition = 0;
     int i = 42; // as usual
 
     focusMonth = setStartTime();
 
     Calendar cal = mDates.getCalendar();
     long ms = cal.getTimeInMillis();
     int position = mDates.get(Calendar.DAY_OF_WEEK);
    int day = mDates.get(Calendar.DAY_OF_MONTH);
 
     position--;
     while (i-- > 0) {
       day = mDates.get(Calendar.DAY_OF_MONTH);
 
       ms = mDates.getCalendar().getTimeInMillis();
       for (int s = 0; s < mTSC.numSeries(); s++) {
         ts = (TimeSeries) mTSC.getSeries(s);
         if (ts == null || mTSC.isSeriesEnabled(ts.getDbRow().getId()) == false)
           continue;
 
         current = ts.findPostNeighbor(ms);
         prev = ts.findPreNeighbor(ms - 1);
 
         if (current != null && prev != null) {
           if (focusMonth == mDates.get(Calendar.MONTH)) {
             drawMonthDayBackground(canvas, true, position, day, prev.mTrend.y,
                 current.mTrend.y, ts.getDbRow().getGoal(), current.mStdDev);
           } else {
             drawMonthDayBackground(canvas, false, position, day, prev.mTrend.y,
                 current.mTrend.y, ts.getDbRow().getGoal(), current.mStdDev);
           }
         }
 
         if (current != null) {
           float oldVal = current.mValue.y;
           if (prev != null)
             oldVal = prev.mValue.y;
 
           tmp.setTimeInMillis(current.mMillis);
          if (day == tmp.get(Calendar.DAY_OF_MONTH)) {
             drawMonthDayValue(canvas, position, oldVal, current.mValue.y, ts
                 .getDbRow().getGoal(), current.mStdDev);
           }
         }
       }
 
       if (focusMonth == mDates.get(Calendar.MONTH)) {
         if (day == 1) {
           firstPosition = position;
           mFocusStart = ms;
         }
         if (day == mDates.getCalendar().getActualMaximum(Calendar.DAY_OF_MONTH)) {
           lastPosition = position;
           mFocusEnd = ms + DateUtil.DAY_MS - 1;
         }
 
         drawMonthDayBorder(canvas, true, position, day);
       } else {
         drawMonthDayBorder(canvas, false, position, day);
       }
       mDates.advance(Period.DAY, 1);
       position++;
     }
 
     drawFocusBorder(canvas, firstPosition, lastPosition);
   }
 
   public long getFocusStart() {
     return mFocusStart;
   }
 
   public long getFocusEnd() {
     return mFocusEnd;
   }
 }
