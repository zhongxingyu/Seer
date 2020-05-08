 /*
  * Copyright (c) 2013. Zachary Dremann
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.zdremann.wc.ui;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.TypefaceSpan;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 import net.zdremann.wc.ForActivity;
 import net.zdremann.wc.R;
 import net.zdremann.wc.io.rooms.TmpRoomLoader;
 import net.zdremann.wc.model.Machine;
 import net.zdremann.wc.provider.MachinesContract.Machines;
 import net.zdremann.wc.provider.NotificationsContract.Notifications;
 import net.zdremann.wc.service.NotificationService;
 import net.zdremann.wc.ui.widget.SimpleSectionedListAdapter;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import static java.util.concurrent.TimeUnit.*;
 
 public class RoomViewFragment extends InjectingListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
     public static final String ARG_ROOM_ID = "room_id";
     public static final String ARG_SELECT_MODE = "select_mode";
     private final Set<Integer> mSelectedIndices = new HashSet<Integer>();
     private long mRoomId;
     private boolean mSelectMode;
     private MenuItem mRefreshItem;
 
     MyRoomViewAdapter mRoomViewAdapter;
 
     SimpleSectionedListAdapter mAdapter;
 
     private Handler mHandler = new Handler();
 
     @Inject
     @ForActivity
     Context mActivityContext;
     private final Runnable mRefreshRunnable = new Runnable() {
         @Override
         public void run() {
             setIsLoading(true);
             mActivityContext.getContentResolver().notifyChange(Machines.buildRoomUri(mRoomId), null);
         }
     };
 
     protected void setIsLoading(final boolean isLoading) {
         if (mRefreshItem != null) {
             if (isLoading) {
                 mRefreshItem.setActionView(R.layout.actionbar_indeterminite_progress);
             } else {
                 mRefreshItem.setActionView(null);
             }
         }
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
         return new TmpRoomLoader(mActivityContext, mRoomId);
     }
 
     @Override
     public void onLoadFinished(final Loader<Cursor> loader, @NotNull final Cursor cursor) {
         if (getActivity() == null)
             return;
 
         setEmptyText(getText(R.string.error_room_download));
         //setListShown(data != null);
 
 
         final ArrayList<SimpleSectionedListAdapter.Section> sections =
                 new ArrayList<SimpleSectionedListAdapter.Section>();
 
         cursor.moveToFirst();
         Machine.Type lastType = null;
 
         int idx_type = cursor.getColumnIndex(Machines.TYPE);
         while (!cursor.isAfterLast()) {
             Machine.Type currentType = Machine.Type.fromInt(cursor.getInt(idx_type));
             if (currentType != lastType)
                 sections.add(
                         new SimpleSectionedListAdapter.Section(
                                 cursor.getPosition(),
                                 currentType.toString(mActivityContext)
                         ));
 
             lastType = currentType;
             cursor.moveToNext();
         }
 
         mRoomViewAdapter.changeCursor(cursor);
         SimpleSectionedListAdapter.Section[] dummy =
                 new SimpleSectionedListAdapter.Section[sections.size()];
         mAdapter.setSections(sections.toArray(dummy));
 
         setIsLoading(false);
 
        mHandler.postDelayed(mRefreshRunnable, MILLISECONDS.convert(5, MINUTES));
     }
 
     @Override
     public void onLoaderReset(final Loader<Cursor> loader) {
         mRoomViewAdapter.swapCursor(null);
        setIsLoading(false);
     }
 
     @Override
     public String toString() {
         return "RoomViewFragment{" +
                 "mSelectMode=" + mSelectMode +
                 ", mRoomId=" + mRoomId +
                 '}';
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onActivityCreated(final Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mRoomViewAdapter = new MyRoomViewAdapter(mActivityContext);
         mAdapter = new SimpleSectionedListAdapter(
                 mActivityContext, R.layout.item_machine_header, mRoomViewAdapter);
 
         setEmptyText(getText(R.string.room_empty));
 
         final Bundle arguments = getArguments();
 
         setListAdapter(mAdapter);
 
         if (arguments == null) {
             mRoomId = 0;
             mSelectMode = false;
         } else {
             mRoomId = arguments.getLong(ARG_ROOM_ID);
             mSelectMode = arguments.getBoolean(ARG_SELECT_MODE, false);
             getLoaderManager().initLoader(0, null, this);
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
         registerForContextMenu(getListView());
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         AdapterView.AdapterContextMenuInfo adapterMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
         final Cursor item = (Cursor) mAdapter.getItem(adapterMenuInfo.position);
         final int idx_status = item.getColumnIndex(Machines.STATUS);
 
         final int status = item.getInt(idx_status);
 
         if (status <= Notifications.STATUS_VALUE_AVAILABLE) {
             return;
         }
 
         final MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.room_view_contextual, menu);
         final MenuItem completeBtn = menu.findItem(R.id.action_notify_cycle_complete);
 
         assert completeBtn != null;
         completeBtn.setVisible(status > Notifications.STATUS_VALUE_CYCLE_COMPLETE);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mHandler.removeCallbacks(mRefreshRunnable);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         assert menuInfo != null;
         final int index = menuInfo.position;
         final Cursor cursor = (Cursor) mAdapter.getItem(index);
 
         final ContentResolver contentResolver = getActivity().getContentResolver();
 
         final ContentValues cv = new ContentValues();
         cv.put(Notifications.ROOM_ID, mRoomId);
         cv.put(Notifications.NUMBER, cursor.getInt(cursor.getColumnIndex(Machines.NUMBER)));
         cv.put(Notifications.DATE, System.currentTimeMillis());
         cv.put(Notifications.TYPE, cursor.getInt(cursor.getColumnIndex(Machines.TYPE)));
 
         switch (item.getItemId()) {
             case R.id.action_notify_available:
                 cv.put(Notifications.STATUS, Notifications.STATUS_VALUE_AVAILABLE);
                 break;
             case R.id.action_notify_cycle_complete:
                 cv.put(Notifications.STATUS, Notifications.STATUS_VALUE_CYCLE_COMPLETE);
                 break;
             default:
                 return super.onContextItemSelected(item);
         }
 
         contentResolver.insert(Notifications.CONTENT_URI, cv);
         Intent intent = new Intent(getActivity(), NotificationService.class);
         getActivity().startService(intent);
         return true;
     }
 
     @Override
     public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
         inflater.inflate(R.menu.fragment_room_view, menu);
         mRefreshItem = menu.findItem(R.id.action_refresh);
     }
 
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_refresh:
                 setIsLoading(true);
                 getLoaderManager().restartLoader(0, null, this);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     static class MyRoomViewAdapter extends CursorAdapter {
 
         private int idx_id;
         private int idx_room_id;
         private int idx_machine_id;
         private int idx_number;
         private int idx_type;
         private int idx_status;
         private int idx_time_remaining;
 
         private final LayoutInflater mLayoutInflater;
 
         private MyRoomViewAdapter(@NotNull Context context) {
             super(context, null, false);
             mLayoutInflater = (LayoutInflater.from(context));
         }
 
         private final TypefaceSpan mPostfixSpan = new TypefaceSpan("sans-serif-light");
 
         @Nullable
         @Override
         public Cursor swapCursor(@Nullable Cursor newCursor) {
             Cursor cursor = super.swapCursor(newCursor);
 
             if (newCursor != null) {
                 idx_id = newCursor.getColumnIndex(Machines._ID);
                 idx_room_id = newCursor.getColumnIndex(Machines.ROOM_ID);
                 idx_machine_id = newCursor.getColumnIndex(Machines.MACHINE_ID);
                 idx_number = newCursor.getColumnIndex(Machines.NUMBER);
                 idx_type = newCursor.getColumnIndex(Machines.TYPE);
                 idx_status = newCursor.getColumnIndex(Machines.STATUS);
                 idx_time_remaining = newCursor.getColumnIndex(Machines.TIME_REMAINING);
             }
             return cursor;
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             View view = mLayoutInflater.inflate(R.layout.item_machine_row, parent, false);
             assert view != null;
             view.setTag(ViewHolder.from(view));
             return view;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             ViewHolder vh = (ViewHolder) view.getTag();
             Resources res = context.getResources();
 
             int number = cursor.getInt(idx_number);
             Machine.Status status = Machine.Status.fromInt(cursor.getInt(idx_status));
             long timeRemaining = cursor.getLong(idx_time_remaining);
 
             vh.number.setText(String.valueOf(number));
             vh.status.setText(status.toString(context));
             vh.status.setTextColor(status.getColor(context));
 
             if (timeRemaining >= 0) {
                 String timeText = String.format("%.0f", (double) timeRemaining / MINUTES.toMillis(1));
                 String timePostfix = res.getString(R.string.minutes_remaining_postfix);
 
                 Spannable spanRange = new SpannableString(timeText + timePostfix);
                 spanRange.setSpan(mPostfixSpan, timeText.length(), spanRange.length(), 0);
                 vh.time.setText(spanRange, TextView.BufferType.SPANNABLE);
 
                 vh.time.setVisibility(View.VISIBLE);
             } else {
                 vh.time.setVisibility(View.GONE);
             }
         }
 
         private static class ViewHolder {
             public TextView number;
             public TextView status;
             public TextView time;
 
             @NotNull
             public static ViewHolder from(@NotNull View v) {
                 final ViewHolder vh = new ViewHolder();
                 vh.number = (TextView) v.findViewById(R.id.number);
                 vh.status = (TextView) v.findViewById(R.id.status);
                 vh.time = (TextView) v.findViewById(R.id.time_remaining);
 
                 return vh;
             }
         }
     }
 
 }
