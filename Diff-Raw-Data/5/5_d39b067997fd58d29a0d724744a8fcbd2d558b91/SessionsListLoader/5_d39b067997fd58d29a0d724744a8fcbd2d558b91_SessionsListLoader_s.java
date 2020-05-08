 package com.hasgeek.funnel.misc;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.support.v4.content.AsyncTaskLoader;
 
 import com.hasgeek.funnel.fragment.SessionsListFragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class SessionsListLoader extends AsyncTaskLoader<List<EventSessionRow>> {
 
     private SQLiteDatabase mDatabase;
     private List<EventSessionRow> mData;
     private int mMode;
 
 
     public SessionsListLoader(Context context, int mode) {
         super(context);
         mDatabase = DBManager.getInstance(context).getWritableDatabase();
         mMode = mode;
     }
 
 
     @Override
     public List<EventSessionRow> loadInBackground() {
         List<EventSession> esList = new ArrayList<EventSession>();
         Cursor sessions;
         if (mMode == SessionsListFragment.All_SESSIONS) {
             sessions = mDatabase.rawQuery(
                     "SELECT s.id, s.title, s.speaker, s.section, s.level, s.description, s.url, s.bookmarked, s.date_ist, s.slot_ist, " +
                             "r.title as roomtitle, r.bgcolor " +
                             "FROM sessions s " +
                             "LEFT JOIN rooms r on s.room = r.name " +
                            "ORDER BY s.date_ist ASC",
                     null);
 
         } else {
             sessions = mDatabase.rawQuery(
                     "SELECT s.id, s.title, s.speaker, s.section, s.level, s.description, s.url, s.bookmarked, s.date_ist, s.slot_ist, " +
                             "r.title as roomtitle, r.bgcolor " +
                             "FROM sessions s " +
                             "LEFT JOIN rooms r on s.room = r.name " +
                             "WHERE s.bookmarked = ? " +
                            "ORDER BY s.date_ist ASC",
                     new String[] { "true" });
         }
 
         if (sessions.moveToFirst()) {
             do {
                 boolean bookmarkState = false;
                 if (!sessions.isNull(sessions.getColumnIndex("bookmarked"))) {
                     bookmarkState = sessions.getString(sessions.getColumnIndex("bookmarked")).equals("true");
                 }
                 EventSession es = new EventSession(
                         sessions.getString(sessions.getColumnIndex("id")),
                         sessions.getString(sessions.getColumnIndex("title")),
                         sessions.getString(sessions.getColumnIndex("speaker")),
                         sessions.getString(sessions.getColumnIndex("section")),
                         sessions.getString(sessions.getColumnIndex("level")),
                         sessions.getString(sessions.getColumnIndex("description")),
                         sessions.getString(sessions.getColumnIndex("url")),
                         sessions.getString(sessions.getColumnIndex("date_ist")),
                         sessions.getString(sessions.getColumnIndex("slot_ist")),
                         sessions.getString(sessions.getColumnIndex("roomtitle")),
                         sessions.getString(sessions.getColumnIndex("bgcolor")),
                         bookmarkState
                 );
                 esList.add(es);
 
             } while (sessions.moveToNext());
         }
         sessions.close();
 
         List<EventSessionRow> list = new ArrayList<EventSessionRow>();
         String oldDate = esList.get(0).getDateInIst();
         String oldSlotTime = esList.get(0).getSlotInIst24Hrs();
         List<EventSession> temp = new ArrayList<EventSession>();
         for (EventSession e : esList) {
             if (e.getDateInIst().equals(oldDate) && e.getSlotInIst24Hrs().equals(oldSlotTime)) {
                 temp.add(e);
             } else {
                 // First, save current row slot
                 EventSessionRow row = new EventSessionRow(oldDate, oldSlotTime, temp);
                 list.add(row);
                 temp = new ArrayList<EventSession>();
                 // Then, start a new row
                 oldDate = e.getDateInIst();
                 oldSlotTime = e.getSlotInIst24Hrs();
                 temp.add(e);
             }
         }
 
         return list;
     }
 
 
     @Override
     public void deliverResult(List<EventSessionRow> data) {
         if (isReset()) {
             releaseResources(data);
             return;
         }
 
         // Hold a reference to the old data so it doesn't get garbage collected.
         // The old data may still be in use (i.e. bound to an adapter, etc.), so
         // we must protect it until the new data has been delivered.
         List<EventSessionRow> oldData = mData;
         mData = data;
 
         if (isStarted()) {
             super.deliverResult(data);
         }
 
         // Invalidate the old data as we don't need it any more.
         if (oldData != null && oldData != data) {
             releaseResources(oldData);
         }
     }
 
 
     @Override
     protected void onStartLoading() {
         if (mData != null) {
             deliverResult(mData);
         }
 
         if (takeContentChanged() || mData == null) {
             forceLoad();
         }
     }
 
 
     @Override
     protected void onStopLoading() {
         // The Loader is in a stopped state, so we should attempt to cancel the
         // current load (if there is one).
         cancelLoad();
 
         // Note that we leave the observer as is; Loaders in a stopped state
         // should still monitor the data source for changes so that the Loader
         // will know to force a new load if it is ever started again.
     }
 
 
     @Override
     protected void onReset() {
         // Ensure the loader has been stopped.
         onStopLoading();
 
         // At this point we can release the resources associated with 'mData'.
         if (mData != null) {
             releaseResources(mData);
             mData = null;
         }
     }
 
 
     @Override
     public void onCanceled(List<EventSessionRow> data) {
         super.onCanceled(data);
         releaseResources(data);
     }
 
 
     private void releaseResources(List<EventSessionRow> data) {
         // For a simple List, there is nothing to do. For something like a Cursor, we
         // would close it in this method. All resources associated with the Loader
         // should be released here.
     }
 }
