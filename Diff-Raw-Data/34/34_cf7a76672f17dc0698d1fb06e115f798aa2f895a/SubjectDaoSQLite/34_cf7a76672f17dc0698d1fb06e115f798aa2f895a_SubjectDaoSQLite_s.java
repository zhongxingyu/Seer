 /*
 * $HeadURL$
 * $Date$
 * $Revision$
 *
 * All Rights Reserved.
 */
 package com.itborci.layers;
 
 import android.content.Context;
 import android.database.Cursor;
import android.util.Log;

 import com.itborci.POJO.Subject;
 
 /**
 * Data access layer for object {@link com.itborci.POJO.Subject}
 *
 * @author <a href="mailto:petr.ujezdsky@gmail.com">Petr Újezdský</a>
 * @version $Id$
 */
 class SubjectDaoSQLite implements SubjectDao {
 
     private interface AdapterFunc<OUT> {
         OUT call(DBAdapter dbAdapter);
     }
 
     @Override
     public void saveSubject(final Subject subject, Context context) {
         if (subject.getId() == null) {
             insertSubject(subject, context);
         } else {
             updateSubject(subject, context);
         }
     }
 
     private void insertSubject(final Subject subject, Context context) {
         runOnAdapter(context, new AdapterFunc<Void>() {
             @Override
             public Void call(DBAdapter dbAdapter) {
                 long id = dbAdapter.insertSubject(
                         subject.getName(),
                         subject.getTeacher(),
                         subject.getRoom(),
                         subject.getDay(),
                         subject.getWeek(),
                         subject.getHour(),
                         subject.getColor(),
                         subject.getBellAsInt()
                 );
                 subject.setId(id);
                 return null;
             }
         });
     }
 
     private void updateSubject(final Subject subject, Context context) {
         runOnAdapter(context, new AdapterFunc<Void>() {
             @Override
             public Void call(DBAdapter dbAdapter) {
                 dbAdapter.updateSubjectById(
                         subject.getId(),
                         subject.getName(),
                         subject.getTeacher(),
                         subject.getRoom(),
                         subject.getDay(),
                         subject.getWeek(),
                         subject.getHour(),
                         subject.getColor(),
                         subject.getBellAsInt()
                 );
                 return null;
             }
         });
     }
 
     @Override
     public void deleteSubject(final Subject subject, Context context) {
         runOnAdapter(context, new AdapterFunc<Void>() {
             @Override
             public Void call(DBAdapter dbAdapter) {
                 dbAdapter.deleteSubjectById(subject.getId());
                 return null;
             }
         });
     }
 
     @Override
     public Subject getSubjectByWeekDayHour(final int week, final int day, final int hour, Context context) {
         return runOnAdapter(context, new AdapterFunc<Subject>() {
             @Override
             public Subject call(DBAdapter dbAdapter) {
                 Cursor cursor = dbAdapter.getSubjectByWeekDayHour(week, day, hour);
                 return cursorToSubject(cursor);
             }
         });
     }
 
     private <T> T runOnAdapter(Context context, AdapterFunc<T> func) {
         // create DB adapter based on given context
         DBAdapter dbAdapter = new DBAdapter(context);
         // open connection
         dbAdapter.open();
 
         // call func on it
         T result = null;
         try {
             result = func.call(dbAdapter);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         finally {
             // close DB adapter connection at any circumstances
             dbAdapter.close();
         }
 
         return result;
     }
 
     private Subject cursorToSubject(Cursor cursor) {
    	Log.i("TEST", "sem to jeste dojede");
        Subject subject = Subject.create(
                cursor.getLong(cursor.getColumnIndex(DBAdapter.KEY_ID)),
                cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_SUBJECT)),
                cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_ROOM)),
                cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_TEACHER)),
                cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_WEEK)),
                cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_DAY)),
                cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_HOUR)),
                cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_COLOR)),
                cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_BELL)));
        Log.i("TEST", "sem uz ne :/");
         return subject;
     }
 }
