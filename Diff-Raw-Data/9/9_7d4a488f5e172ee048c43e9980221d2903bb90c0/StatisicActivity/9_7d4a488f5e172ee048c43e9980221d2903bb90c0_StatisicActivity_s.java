 package com.example.tomatroid;
 
 import java.util.ArrayList;
 
 import com.example.tomatroid.digram.PieChart;
 import com.example.tomatroid.sql.SQHelper;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.Menu;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class StatisicActivity extends Activity {
 
 	LinearLayout overview;
 	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
 			LayoutParams.WRAP_CONTENT);
 	LayoutParams barParams = new TableLayout.LayoutParams(
 			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 	SQHelper sqHelper = new SQHelper(this);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statisic);
 
 		overview = (LinearLayout) findViewById(R.id.statistic_overview);
 
 		// Pomodoro Overview
 		int[] pomodoroInfoArray = sqHelper.getTotalCountTimeDays(
 				new int[] { SQHelper.TYPE_POMODORO }, 
 				new int[] {});
 		TextView pomodoroCount = (TextView) findViewById(R.id.statistic_pomodoroCount);
 		pomodoroCount.setText("" + pomodoroInfoArray[0]);
 		TextView pomodoroInfo = (TextView) findViewById(R.id.statistic_pomodoroInfo);
 		pomodoroInfo.setText(prepareInfoText(pomodoroInfoArray));
 
 		// Long Break Overview
 		int[] breakInfoArray = sqHelper.getTotalCountTimeDays(
 				new int[] { SQHelper.TYPE_LONGBREAK, SQHelper.TYPE_SHORTBREAK },
 				new int[] {});
 		TextView breakCount = (TextView) findViewById(R.id.statistic_breakCount);
 		breakCount.setText("" + breakInfoArray[0]);
 		TextView breakInfo = (TextView) findViewById(R.id.statistic_breakInfo);
 		breakInfo.setText(prepareInfoText(breakInfoArray));
 
 		// Sleep Overview
 		int[] sleepInfoArray = sqHelper.getTotalCountTimeDays(
 				new int[] {},
 				new int[] {});
 		TextView sleepCount = (TextView) findViewById(R.id.statistic_sleepCount);
 		sleepCount.setText("" + sleepInfoArray[0]);
 		TextView sleepInfo = (TextView) findViewById(R.id.statistic_sleepInfo);
 		sleepInfo.setText(prepareInfoText(sleepInfoArray));
 
 		Log.e("Statistic", "before List");
 
 		// Theme List Overview
 		ArrayList<String> valueList = new ArrayList<String>();
 		Cursor c = sqHelper.getThemeCursor();
 		if (c.moveToFirst()) {
 			Log.e("Statistic", "before loop " + c.getCount());
 			do {
 				int[] themeInfo = sqHelper.getTotalCountTimeDays(
 						new int[] { SQHelper.TYPE_POMODORO, SQHelper.TYPE_TRACKING },
 						new int[] { c.getInt(0) });
 				valueList.add(c.getString(1) + "\n"
 						+ prepareInfoText(themeInfo));
 			} while (c.moveToNext());
 		}
 		c.close();
 		ListView themelist = (ListView) findViewById(R.id.statistic_themelist);
 		themelist.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
 				R.layout.theme_list_row, valueList));
 
 		// float values[] = { 3456, 5734, 5735, 5477, 9345, 3477 };
 		// PieChart pie = new PieChart(this, values);
 		// overview.addView(pie, 0, barParams);
 
 		// themelist.addView(pie);
 		//
 
 		// TextView t1 = new TextView(this);
 		// t1.setText("Total Pomodoro");
 		// overview.addView(t1, params);
 
 	}
 
 	public String prepareInfoText(int[] array) {
 		if (array[2] != 0) {
 			return array[1] + " mins \n" + array[0] / array[2]
 					+ " per Day \n (over " + array[2] + " days)";
 		} else {
 			return "Keine Informationen";
 		}
 	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_statictic, menu);
		return true;
	}
 }
