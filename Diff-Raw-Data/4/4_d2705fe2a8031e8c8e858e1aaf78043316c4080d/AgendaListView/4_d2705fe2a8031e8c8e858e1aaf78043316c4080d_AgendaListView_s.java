 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package com.android.calendar.agenda;
 
 import com.android.calendar.CalendarController;
 import com.android.calendar.CalendarController.EventType;
 import com.android.calendar.DeleteEventHelper;
 import com.android.calendar.R;
 import com.android.calendar.Utils;
 import com.android.calendar.agenda.AgendaAdapter.ViewHolder;
 import com.android.calendar.agenda.AgendaWindowAdapter.DayAdapterInfo;
 import com.android.calendar.agenda.AgendaWindowAdapter.EventInfo;
 
 import android.content.Context;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.text.format.Time;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class AgendaListView extends ListView implements OnItemClickListener {
 
     private static final String TAG = "AgendaListView";
     private static final boolean DEBUG = false;
 
     private AgendaWindowAdapter mWindowAdapter;
     private DeleteEventHelper mDeleteEventHelper;
     private Context mContext;
     private String mTimeZone;
     private boolean mShowEventDetailsWithAgenda;
     // Used to update the past/present separator at midnight
     private Handler mMidnightUpdate = null;;
 
     private Runnable mTZUpdater = new Runnable() {
         @Override
         public void run() {
             mTimeZone = Utils.getTimeZone(mContext, this);
         }
     };
 
     private Runnable mMidnightUpdater = new Runnable() {
         @Override
         public void run() {
             refresh(true);
             setMidnightUpdater();
         }
     };
 
 
     public AgendaListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initView(context);
     }
 
     private void initView(Context context) {
         mContext = context;
         mTimeZone = Utils.getTimeZone(context, mTZUpdater);
         setOnItemClickListener(this);
         setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         setVerticalScrollBarEnabled(false);
         mWindowAdapter = new AgendaWindowAdapter(context, this,
                 Utils.getConfigBool(context, R.bool.show_event_details_with_agenda));
         mWindowAdapter.setSelectedInstanceId(-1/* TODO:instanceId */);
         setAdapter(mWindowAdapter);
         setCacheColorHint(context.getResources().getColor(R.color.agenda_item_not_selected));
         mDeleteEventHelper =
                 new DeleteEventHelper(context, null, false /* don't exit when done */);
         mShowEventDetailsWithAgenda = Utils.getConfigBool(mContext,
                 R.bool.show_event_details_with_agenda);
         setMidnightUpdater();
     }
 
 
     // Sets a thread to run one second after midnight and refresh the list view
     // causing the separator between past/present to be updated.
     private void setMidnightUpdater() {
 
         // Create the handler or clear the existing one.
         if (mMidnightUpdate == null) {
             mMidnightUpdate = new Handler();
         } else {
             mMidnightUpdate.removeCallbacks(mMidnightUpdater);
         }
 
         // Calculate the time until midnight + 1 second and set the handler to
         // do a refresh
         // at that time.
 
         long now = System.currentTimeMillis();
         Time time = new Time(mTimeZone);
         time.set(now);
         long runInMillis = (24 * 3600 - time.hour * 3600 - time.minute * 60 -
                 time.second + 1) * 1000;
         mMidnightUpdate.postDelayed(mMidnightUpdater, runInMillis);
     }
 
     // Stop the midnight update thread
     private void resetMidnightUpdater() {
         if (mMidnightUpdate != null) {
             mMidnightUpdate.removeCallbacks(mMidnightUpdater);
         }
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         mWindowAdapter.close();
     }
 
     // Implementation of the interface OnItemClickListener
     @Override
     public void onItemClick(AdapterView<?> a, View v, int position, long id) {
         if (id != -1) {
             // Switch to the EventInfo view
             EventInfo event = mWindowAdapter.getEventByPosition(position);
             long oldInstanceId = mWindowAdapter.getSelectedInstanceId();
             mWindowAdapter.setSelectedView(v);
 
             // If events are shown to the side of the agenda list , do nothing
             // when the same
             // event is selected , otherwise show the selected event.
 
             if (event != null && (oldInstanceId != mWindowAdapter.getSelectedInstanceId() ||
                     !mShowEventDetailsWithAgenda)) {
                 CalendarController.getInstance(mContext).sendEventRelatedEvent(this,
                         EventType.VIEW_EVENT, event.id, event.begin, event.end, 0, 0, -1);
             }
         }
     }
 
     public void goTo(Time time, long id, String searchQuery, boolean forced) {
         if (time == null) {
             time = new Time(mTimeZone);
             long goToTime = getFirstVisibleTime();
             if (goToTime <= 0) {
                 goToTime = System.currentTimeMillis();
             }
             time.set(goToTime);
         }
         mWindowAdapter.refresh(time, id, searchQuery, forced);
     }
 
     public void refresh(boolean forced) {
         Time time = new Time(Utils.getTimeZone(mContext, null));
         long goToTime = getFirstVisibleTime();
         if (goToTime <= 0) {
             goToTime = System.currentTimeMillis();
         }
         time.set(goToTime);
         mWindowAdapter.refresh(time, -1, null, forced);
     }
 
     public void deleteSelectedEvent() {
         int position = getSelectedItemPosition();
         EventInfo event = mWindowAdapter.getEventByPosition(position);
         if (event != null) {
             mDeleteEventHelper.delete(event.begin, event.end, event.id, -1);
         }
     }
 
     @Override
     public int getFirstVisiblePosition() {
         // TODO File bug!
         // getFirstVisiblePosition doesn't always return the first visible
         // item. Sometimes, it is above the visible one.
         // instead. I loop through the viewgroup children and find the first
         // visible one. BTW, getFirstVisiblePosition() == getChildAt(0). I
         // am not looping through the entire list.
         View v = getFirstVisibleView();
         if (v != null) {
             if (DEBUG) {
                 Log.v(TAG, "getFirstVisiblePosition: " + AgendaWindowAdapter.getViewTitle(v));
             }
             return getPositionForView(v);
         }
         return -1;
     }
 
     public View getFirstVisibleView() {
         Rect r = new Rect();
         int childCount = getChildCount();
         for (int i = 0; i < childCount; ++i) {
             View listItem = getChildAt(i);
             listItem.getLocalVisibleRect(r);
             if (r.top >= 0) { // if visible
                 return listItem;
             }
         }
         return null;
     }
 
     public long getSelectedTime() {
         int position = getSelectedItemPosition();
         if (position >= 0) {
             EventInfo event = mWindowAdapter.getEventByPosition(position);
             if (event != null) {
                 return event.begin;
             }
         }
         return getFirstVisibleTime();
     }
 
     public long getFirstVisibleTime() {
         int position = getFirstVisiblePosition();
         if (DEBUG) {
             Log.v(TAG, "getFirstVisiblePosition = " + position);
         }
 
         EventInfo event = mWindowAdapter.getEventByPosition(position);
         if (event != null) {
             Time t = new Time(mTimeZone);
             t.set(event.begin);
             t.setJulianDay(event.startDay);
             return t.normalize(false);
         }
         return 0;
     }
 
     public int getJulianDayFromPosition(int position) {
         DayAdapterInfo info = mWindowAdapter.getAdapterInfoByPosition(position);
         if (info != null) {
             return info.dayAdapter.findJulianDayFromPosition(position - info.offset);
         }
         return 0;
     }
 
     // Finds is a specific event (defined by start time and id) is visible
     public boolean isEventVisible(Time startTime, long id) {
 
         if (id == -1 || startTime == null) {
             return false;
         }
 
         View child = getChildAt(0);
         // View not set yet, so not child - return
         if (child == null) {
             return false;
         }
         int start = getPositionForView(child);
         long milliTime = startTime.toMillis(true);
         int childCount = getChildCount();
         int eventsInAdapter = mWindowAdapter.getCount();
 
         for (int i = 0; i < childCount; i++) {
             if (i + start >= eventsInAdapter) {
                 break;
             }
             EventInfo event = mWindowAdapter.getEventByPosition(i + start);
             if (event == null) {
                 continue;
             }
             if (event.id == id && event.begin == milliTime) {
                 View listItem = getChildAt(i);
                 if (listItem.getBottom() <= getHeight() &&
                         listItem.getTop() >= 0) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public long getSelectedInstanceId() {
         return mWindowAdapter.getSelectedInstanceId();
     }
 
     // Move the currently selected or visible focus down by offset amount.
     // offset could be negative.
     public void shiftSelection(int offset) {
         shiftPosition(offset);
         int position = getSelectedItemPosition();
         if (position != INVALID_POSITION) {
             setSelectionFromTop(position + offset, 0);
         }
     }
 
     private void shiftPosition(int offset) {
         if (DEBUG) {
             Log.v(TAG, "Shifting position " + offset);
         }
 
         View firstVisibleItem = getFirstVisibleView();
 
         if (firstVisibleItem != null) {
             Rect r = new Rect();
             firstVisibleItem.getLocalVisibleRect(r);
             // if r.top is < 0, getChildAt(0) and getFirstVisiblePosition() is
             // returning an item above the first visible item.
             int position = getPositionForView(firstVisibleItem);
             setSelectionFromTop(position + offset, r.top > 0 ? -r.top : r.top);
             if (DEBUG) {
                 if (firstVisibleItem.getTag() instanceof AgendaAdapter.ViewHolder) {
                     ViewHolder viewHolder = (AgendaAdapter.ViewHolder) firstVisibleItem.getTag();
                     Log.v(TAG, "Shifting from " + position + " by " + offset + ". Title "
                             + viewHolder.title.getText());
                 } else if (firstVisibleItem.getTag() instanceof AgendaByDayAdapter.ViewHolder) {
                     AgendaByDayAdapter.ViewHolder viewHolder =
                             (AgendaByDayAdapter.ViewHolder) firstVisibleItem.getTag();
                     Log.v(TAG, "Shifting from " + position + " by " + offset + ". Date  "
                             + viewHolder.dateView.getText());
                 } else if (firstVisibleItem instanceof TextView) {
                     Log.v(TAG, "Shifting: Looking at header here. " + getSelectedItemPosition());
                 }
             }
         } else if (getSelectedItemPosition() >= 0) {
             if (DEBUG) {
                 Log.v(TAG, "Shifting selection from " + getSelectedItemPosition() +
                         " by " + offset);
             }
             setSelection(getSelectedItemPosition() + offset);
         }
     }
 
     public void setHideDeclinedEvents(boolean hideDeclined) {
         mWindowAdapter.setHideDeclinedEvents(hideDeclined);
     }
 
     public void onResume() {
         mTZUpdater.run();
         setMidnightUpdater();
         mWindowAdapter.onResume();
     }
 
     public void onPause() {
         resetMidnightUpdater();
         mWindowAdapter.notifyDataSetInvalidated();
     }
 }
