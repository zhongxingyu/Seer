 package ie.ucd.asteroid;
 
 import java.util.Calendar;
 import java.util.Scanner;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LvAsteroidAdapter extends ArrayAdapter<Asteroids>{
 
     Context context; 
     int layoutResourceId;    
     Asteroids data[] = null;
     DBAdapter db;
     private boolean[] checkboxState;
     private int[] idList;
     private static final String TAG = "LvAsteroidAdapter";     
     
     public LvAsteroidAdapter(Context context, int layoutResourceId, Asteroids[] data) {
         super(context, layoutResourceId, data);
         this.layoutResourceId = layoutResourceId;
         this.context = context;
         this.data = data;
         
         db = new DBAdapter(context);
         checkboxState = new boolean[data.length];
         idList = new int[data.length];
     }
 
 	@Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         View row = convertView;
         AsteroidsHolder holder = null;
         
         if (isNotified(data[position].getId()))
         {
         	checkboxState[position] = true;
         }
         
         if(row == null)
         {
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, parent, false);
             
             holder = new AsteroidsHolder();
             holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
             holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
             holder.imgNotify = (CheckBox)row.findViewById(R.id.imgNotify);
             
             row.setTag(holder);
         }
         else
         {
             holder = (AsteroidsHolder)row.getTag();
         }
         
         Asteroids ast = data[position];
         holder.txtTitle.setText(ast.title);
         //set typeface for listview row text
         Typeface notifyFace=Typeface.createFromAsset(this.getContext().getAssets(),"fonts/Roboto-CondensedItalic.ttf");
         
         holder.txtTitle.setTypeface(notifyFace);
         holder.imgIcon.setImageResource(ast.icon);
 		holder.imgNotify.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton checkboxView, boolean isChecked)
 			{
 				int id = data[position].getId();
 				
 				if (isChecked)
 				{
 					if (!isNotified(id))
 					{
 						db.openDB();
 						String name = db.getName(id);
 						String date = db.getApproachDate(id);
 						String time = db.getTime(id);
 						db.setNotificationState(id, "on");
						Toast.makeText(getContext(), getContext().getString(R.string.notification_set) + " " + db.getApproachDateDisplay(id) +
								" " + getContext().getString(R.string.notification_at) + " " + time, Toast.LENGTH_SHORT).show();
 						db.closeDB();
 						setNotification(id, name, date, time);
 					}
 				}
 				else
 				{
 					if (isNotified(id))
 					{
 						db.openDB();
 						db.setNotificationState(id, "off");
 						db.closeDB();
 						stopNotification(id);
 					}
 				}
 			}
 		});
         holder.imgNotify.setChecked(checkboxState[position]);
         // Fixes checkbox bug that unticked checkbox when scrolling
         holder.imgNotify.setOnClickListener(new OnClickListener() {
         	public void onClick(View v)
         	{
         		if (((CheckBox)v).isChecked())
         		{
         			checkboxState[position] = true;
         		}
         		else
         		{
         			checkboxState[position] = false;
         		}
         	}
         });
 
         return row;
     }
     
     static class AsteroidsHolder
     {
         ImageView imgIcon;
         TextView txtTitle;
         CheckBox imgNotify;
     }
     
     // Sets an alarm that calls a notification
 	private void setNotification(int id, String name, String date, String time)
 	{		
 		Log.d(TAG, "Creating notification using id " + id);
 		
 		Intent intent = new Intent(context, NotificationSettings.class);
 		intent.putExtra("id", id);
 		intent.putExtra("name", name);
 		
 		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 	
 		// id ensures that each asteroid has its own alarm set
 		PendingIntent pendingIntent = PendingIntent.getService(context, id, intent, 0);
 		
 		// Use delimiter to separate date and time
 		Scanner sc = new Scanner(date).useDelimiter("-");
 		int day = Integer.parseInt(sc.next());
 		int month = Integer.parseInt(sc.next()) - 1; // 0 is January
 		int year = Integer.parseInt(sc.next());
 		
 		sc = new Scanner(time).useDelimiter(":");
 		int hour = Integer.parseInt(sc.next());
 		int min = Integer.parseInt(sc.next());
 		
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(year, month, day, hour, min, 0);
 		
 		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 		Log.d(TAG, "Alarm set for " + id + " on "+ calendar.getTime());
 	}
 	
 	// Set notification for an array of ids
 	// Fix for select all checkbox bug in which only visible check box notifications are set
 	private void setNotification(int[] ids)
 	{
 		String name;
 		String date;
 		String time;
 		db.openDB();
 		for(int i : ids)
 		{
 			if (!isNotified(i))
 			{
 				name = db.getName(i);
 				date = db.getApproachDate(i);
 				time = db.getTime(i);
 				db.setNotificationState(i, "on");
 				setNotification(i, name, date, time);
 			}
 		}
 		db.closeDB();
 	}
 	
 	// Stops a notification by canceling its AlarmManager
 	private void stopNotification(int id)
 	{		
 		Intent intent = new Intent(context, NotificationSettings.class);
 		PendingIntent pendingIntent = PendingIntent.getService(context, id, intent, 0);
 		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		alarmManager.cancel(pendingIntent);
 		Log.d(TAG, "Notification cancelled for " + id);
 	}
 	
 	// Stop notification for an array of ids
 	// Fix for select all checkbox bug in which only visible check box notifications are stopped
 	private void stopNotification(int[] ids)
 	{
 		db.openDB();
 		for(int i : ids)
 		{
 			if (isNotified(i))
 			{
 				db.setNotificationState(i, "off");
 				stopNotification(i);
 			}
 		}
 		db.closeDB();
 	}
 	
 	// Changes state of all check boxes
 	public void setAllCheckbox(boolean state)
 	{		
 		if (state)
 		{
 			setNotification(idList);
 			Toast.makeText(getContext(), R.string.notification_setAll, Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			stopNotification(idList);
 		}
 		for(int i = 0; i < checkboxState.length; i++)
 		{
 			checkboxState[i] = state;
 		}
 		
 	}
 	
 	// Returns the id of the selected asteroid in the ListView
 	public int getId(int position)
 	{
 		return data[position].getId();
 	}
 	
 	// Stores all the ids in the ListView
 	// Fixes the bug where only ids in visible rows are known
 	public void setIdList(Asteroids[] data, int size)
 	{
 		for(int i = 0; i < size; i++)
 		{
 			idList[i] = data[i].id;
 		}
 	}
 	
 	public void setCheckboxState(Asteroids[] data, int size)
 	{
 		for(int i = 0; i < size; i++)
 		{
 			checkboxState[i] = data[i].notify;
 		}
 	}
 	
 	// Check if the asteroid's notification has been set already
 	public boolean isNotified(int id)
 	{
 		for (int pos = 0; pos < checkboxState.length; pos++)
 		{
 			if (id == data[pos].id)
 			{
 				return checkboxState[pos];
 			}
 		}
 		return false;
 	}	
 	
 }
