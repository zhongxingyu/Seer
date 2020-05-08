 package com.calendar.reporter;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.calendar.reporter.database.task.TaskDataSource;
 import com.calendar.reporter.helper.DateListing;
 import com.calendar.reporter.helper.LocalDate;
 import com.calendar.reporter.helper.Session;
 import com.calendar.reporter.helper.TableAdapter;
 import android.widget.TableRow.LayoutParams;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class TasksGeneral extends Activity {
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.general);
 
         SharedPreferences settings = getSharedPreferences(Session.PREFS_NAME, 0);
         Session session = new Session(settings);
 
         LocalDate localDate = new LocalDate(session.getDate(Session.GENERAL));
 
         TextView lowerText = (TextView) findViewById(R.id.lowerTextGeneral);
         lowerText.setText(localDate.getDayName());
 
         TextView upperText = (TextView) findViewById(R.id.upperTextGeneral);
         upperText.setText(localDate.getMonthName());
 
         DateListing dateListing = new DateListing(this, session, Session.GENERAL);
         dateListing.upperRightBehavior(R.id.upperRightGeneral);
         dateListing.upperLeftBehavior(R.id.upperLeftGeneral);
         dateListing.lowerRightBehavior(R.id.lowerRightGeneral);
         dateListing.lowerLeftBehavior(R.id.lowerLeftGeneral);
 
         TableLayout tableLayout = (TableLayout) findViewById(R.id.tableGeneral);
 
 
         TaskDataSource taskDataSource = new TaskDataSource(this);
        HashMap<String, String> generalInfo = taskDataSource.getGeneralInfo(session.getProjectId());
         buidlTable(tableLayout, generalInfo);
 
     }
 
     private void buidlTable(TableLayout tableLayout, HashMap<String, String> generalInfo) {
         Iterator it = generalInfo.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry) it.next();
             TableAdapter tableAdapter = new TableAdapter(tableLayout, this);
             tableAdapter.createRow((String) pairs.getKey(), (String) pairs.getValue());
             it.remove(); // avoids a ConcurrentModificationException
         }
     }
 
 
 }
