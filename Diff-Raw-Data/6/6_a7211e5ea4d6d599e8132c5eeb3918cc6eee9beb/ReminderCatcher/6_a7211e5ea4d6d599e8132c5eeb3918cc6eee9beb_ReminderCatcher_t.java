 package com.aragaer.reminder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class ReminderCatcher extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		int glyph_width = getResources().getDimensionPixelSize(R.dimen.notification_height);
 		int position = (int) ReminderService.x / glyph_width;
 		Intent i;
		if (ReminderService.list == null
				|| position >= ReminderService.list.size())
 			i = new Intent(this, ReminderListActivity.class);
 		else {
 			final ReminderItem item = ReminderService.list.get(position);
 			if (item._id == -1)
 				i = new Intent(this, ReminderCreateActivity.class);
 			else {
 				i = new Intent(this, ReminderViewActivity.class);
				i.putExtra("reminder_id", item._id);
 			}
 		}
 		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 				| Intent.FLAG_ACTIVITY_CLEAR_TOP
 				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 		startActivity(i);
 		finish();
 	}
 }
