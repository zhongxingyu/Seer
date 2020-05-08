 package de.micmun.android.miwotreff.utils;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.provider.CalendarContract;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ListView;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import de.micmun.android.miwotreff.R;
 
 /**
  * Class for handling the contextual action bar.
  *
  * @author: Michael Munzert
  * @version: 1.0, 20.10.13.
  */
 public class ContextActionMode implements ListView
       .MultiChoiceModeListener, UndoBarController.UndoListener {
    private final Activity mActivity;
    private ListView lv;
    private Menu menu;
    private UndoBarController mUndoBarController;
    private String selMsgFormat;
 
    /**
     * Creates a new ContextActionMode.
     *
     * @param context MainActivity.
     * @param lv      ListView.
     */
    public ContextActionMode(Activity context, ListView lv) {
       this.lv = lv;
       mActivity = context;
       mUndoBarController = new UndoBarController(mActivity.findViewById(R.id
             .undobar), this);
       lv.setOnTouchListener(new View.OnTouchListener() {
          /**
           * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
           */
          @Override
          public boolean onTouch(View v, MotionEvent event) {
             mUndoBarController.hideUndoBar(true);
             return false;
          }
       });
       selMsgFormat = mActivity.getResources().getString(R.string
             .title_count_selected);
    }
 
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
       this.menu = menu;
       // Inflate a menu resource providing context menu items
       MenuInflater inflater = mode.getMenuInflater();
       inflater.inflate(R.menu.context_menu, menu);
       return true;
    }
 
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
       int count = lv.getCheckedItemCount();
       String selMsg = String.format(selMsgFormat, String.valueOf(count));
 
       if (count == 0) {
          // nothing selected -> hide action mode
          mode.finish();
          return true;
       } else if (count == 1) { // show delete and add2Cal
          menu.findItem(R.id.addToCal).setVisible(true);
          menu.findItem(R.id.share).setVisible(true);
          mode.setTitle(selMsg);
          return true;
       } else if (count > 1) { // show only delete
          menu.findItem(R.id.addToCal).setVisible(false);
          menu.findItem(R.id.share).setVisible(false);
          mode.setTitle(selMsg);
          return true;
       }
 
       return false;
    }
 
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
       long[] itemIds = lv.getCheckedItemIds();
 
       switch (item.getItemId()) {
          case R.id.addToCal:
             add2Cal(itemIds[0]);
             mode.finish();
             return true;
          case R.id.delItem:
             // delete items
             delItem(itemIds);
             return true;
          case R.id.share:
             showShareIntent(itemIds[0]);
             return true;
       }
       return false;
    }
 
    @Override
    public void onDestroyActionMode(ActionMode mode) {
 
    }
 
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
       onPrepareActionMode(mode, menu);
    }
 
    @Override
    public void onUndo(Parcelable token) {
       List<SingleParcelEntry> list = ((ParcelList) token).getArrList();
       ContentValues values;
 
       for (SingleParcelEntry entry : list) {
          values = new ContentValues();
          values.put(DBConstants.KEY_DATUM, DBDateUtility.getDateFromString
                (entry.date).getTime());
          values.put(DBConstants.KEY_THEMA, entry.topic);
          values.put(DBConstants.KEY_PERSON, entry.person);
          values.put(DBConstants.KEY_EDIT, Integer.valueOf(entry.edit));
 
             /* insert */
          mActivity.getContentResolver().insert(DBConstants.TABLE_CONTENT_URI,
                values);
       }
    }
 
    /**
     * Opens a standard share intent.
     *
     * @param id the ID of the entry to share.
     */
    private void showShareIntent(long id) {
       /* get data */
       Cursor c = mActivity.getContentResolver().query(
             Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI,
                   String.valueOf(id)), null, null, null, null);
       if (c == null || c.getCount() <= 0) {
          return;
       }
       c.moveToFirst();
 
       /* date */
       long timestamp = c.getLong(c.getColumnIndex(DBConstants.KEY_DATUM));
       Calendar then = GregorianCalendar.getInstance();
       then.setTimeInMillis(timestamp);
       Calendar now = GregorianCalendar.getInstance();
 
       /* topic */
       String topic = c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
 
       String text;
       /* today */
       if (then.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
             && then.get(Calendar.MONTH) == now.get(Calendar.MONTH)
             && then.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
          text = mActivity.getString(R.string.shareText_today, topic);
       } else { /* other day than today */
          SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
          if (then.compareTo(now) == -1) { /* other day in the past */
             text = mActivity.getString(
                   R.string.shareText_past, topic, df.format(then.getTime()));
          } else { /* other day in the future */
             text = mActivity.getString(
                   R.string.shareText_future, topic, df.format(then.getTime()));
          }
       }
 
       Intent shareIntent = new Intent();
       shareIntent.setAction(Intent.ACTION_SEND);
       shareIntent.putExtra(Intent.EXTRA_TEXT, text);
       shareIntent.setType("text/plain");
       mActivity.startActivity(Intent.createChooser(
             shareIntent, mActivity.getResources().getText(R.string.shareWith)));
    }
 
    /**
     * Adds the entry to the calendar.
     *
     * @param id ID of the entry.
     */
    private void add2Cal(long id) {
       Cursor c = mActivity.getContentResolver().query(Uri.withAppendedPath
             (DBConstants
                   .TABLE_CONTENT_URI, String.valueOf(id)), null, null, null,
             null);
       if (c == null || c.getCount() <= 0)
          return;
       c.moveToFirst();
 
       // Date and time of the calendar entry
       long d = c.getLong(c.getColumnIndex(DBConstants.KEY_DATUM));
       GregorianCalendar start = new GregorianCalendar();
       GregorianCalendar end = new GregorianCalendar();
       start.setTimeInMillis(d);
       start.set(GregorianCalendar.HOUR_OF_DAY, 19);
       start.set(GregorianCalendar.MINUTE, 30);
       end.setTimeInMillis(d);
       end.set(GregorianCalendar.HOUR_OF_DAY, 21);
 
       // title
       String title = mActivity.getResources().getString(R.string.cal_prefix)
             + " "
             + c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
 
       // location
       String loc = mActivity.getResources().getString(R.string.cal_loc);
 
       // Calendar: Insert per Intent
       Intent intent = new Intent(Intent.ACTION_INSERT)
             .setData(CalendarContract.Events.CONTENT_URI)
             .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                   start.getTimeInMillis())
             .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                   end.getTimeInMillis())
             .putExtra(CalendarContract.Events.TITLE, title)
             .putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
             .putExtra(CalendarContract.Events.AVAILABILITY,
                   CalendarContract.Events.AVAILABILITY_BUSY);
       mActivity.startActivity(intent);
    }
 
    /**
     * Deletes all selected items.
     *
     * @param itemIds ids of the items to delete.
     */
    private void delItem(long[] itemIds) {
       List<SingleParcelEntry> list = new ArrayList<SingleParcelEntry>(itemIds
             .length);
       int deletions = 0;
 
       for (long i : itemIds) {
          // first backup ...
          Uri readUri = Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI,
                String.valueOf(i));
          Cursor c = mActivity.getContentResolver().query(readUri, null, null,
                null, null);
          c.moveToFirst();
          String d = DBDateUtility.getDateString(c.getLong(c.getColumnIndex
                (DBConstants.KEY_DATUM)));
          String t = c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
          String p = c.getString(c.getColumnIndex(DBConstants.KEY_PERSON));
          String e = c.getString(c.getColumnIndex(DBConstants.KEY_EDIT));
          list.add(new SingleParcelEntry(d, t, p, e));
 
          // ... then delete
          Uri delUri = Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI,
                String.valueOf(i));
          deletions += mActivity.getContentResolver().delete(delUri, null,
                null);
       }
       // Safe backup into parcel
       ParcelList undoToken = new ParcelList();
       undoToken.setArrList(list);
 
       // show UndoBar
       String msg = String.format(mActivity.getResources().getString(R.string
             .undobar_entry_deleted), String.valueOf(deletions));
       mUndoBarController.showUndoBar(false, msg, undoToken);
    }
 
    /**
     * Saves the state of the instance.
     *
     * @param outState state bundle.
     */
    public void onSaveInstanceState(Bundle outState) {
       mUndoBarController.onSaveInstanceState(outState);
    }
 
    /**
     * Restore the state of the instance.
     *
     * @param savedInstanceState state bundle.
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
       mUndoBarController.onRestoreInstanceState(savedInstanceState);
    }
 
    /**
     * Single entry for undo.
     *
     * @author Michael Munzert
     * @version 1.0, 20.10.2013
     */
    private static class SingleParcelEntry implements Parcelable {
 
       public static final Parcelable.Creator<SingleParcelEntry> CREATOR = new Parcelable.Creator<SingleParcelEntry>() {
          @Override
          public SingleParcelEntry createFromParcel(Parcel in) {
             return new SingleParcelEntry(in);
          }
 
          @Override
          public SingleParcelEntry[] newArray(int size) {
             return new SingleParcelEntry[size];
          }
       };
       private String date;
       private String topic;
       private String person;
       private String edit;
 
       public SingleParcelEntry(String date, String topic,
                                String person, String edit) {
          this.date = date;
          this.topic = topic;
          this.person = person;
          this.edit = edit;
       }
 
       public SingleParcelEntry(Parcel in) {
          String[] data = new String[4];
 
          in.readStringArray(data);
          this.date = data[0];
          this.topic = data[1];
          this.person = data[2];
          this.edit = data[3];
       }
 
       @Override
       public int describeContents() {
          return 0;
       }
 
       @Override
       public void writeToParcel(Parcel dest, int flags) {
          dest.writeStringArray(new String[]{this.date,
                this.topic, this.person, this.edit});
       }
    }
 
    /**
     * List of parcel entries for undo.
     *
     * @author Michael Munzert
     * @version 1.0, 20.10.2013
     */
    private static class ParcelList implements Parcelable {
 
       @SuppressWarnings("unused")
       public static final Parcelable.Creator<ParcelList> CREATOR = new Parcelable.Creator<ParcelList>() {
          @Override
          public ParcelList createFromParcel(Parcel in) {
             return new ParcelList(in);
          }
 
          @Override
          public ParcelList[] newArray(int size) {
             return new ParcelList[size];
          }
       };
       private List<SingleParcelEntry> arrList = new ArrayList<SingleParcelEntry>();
 
       ParcelList() { // initialization
          arrList = new ArrayList<SingleParcelEntry>();
       }
 
       public ParcelList(Parcel in) {
          in.readTypedList(arrList, SingleParcelEntry.CREATOR);
       }
 
       public List<SingleParcelEntry> getArrList() {
          return arrList;
       }
 
       public void setArrList(List<SingleParcelEntry> arrList) {
          this.arrList = arrList;
       }
 
       @Override
       public int describeContents() {
          return 0;
       }
 
       @Override
       public void writeToParcel(Parcel dest, int flags) {
          dest.writeTypedList(arrList);
       }
    }
 }
