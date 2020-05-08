 /*
 *Chris Card
 *Nathan Harvey
 *10/26/12
 *This Class is responsible for updating the widget view with new material
 */
 package csci422.CandN.to_dolist;
 
 
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.database.Cursor;
 import android.widget.RemoteViews;
 
 public class WidgetService extends IntentService {
 
 	public WidgetService() 
 	{
 		super("WidgetService");
 	}
 	@Override
 	public void onHandleIntent(Intent intent) 
 	{
 		ComponentName me = new ComponentName(this, AppWidget.class);
 		RemoteViews updateViews = new RemoteViews("to_dolist", R.layout.widget);
 		ToDoHelper helper = new ToDoHelper(this);
 		AppWidgetManager mgr = AppWidgetManager.getInstance(this);
 		try 
 		{
 			Cursor c = helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM todos", null);
 			c.moveToFirst();
 			int count = c.getInt(0);
 			c.close();
 			if (count > 0) 
 			{
 				int offset = (int)(count*Math.random());
 				String args[] = {String.valueOf(offset)};
 				c = helper.getReadableDatabase().rawQuery("SELECT _ID, title FROM todos LIMIT 1 OFFSET ?", args);
 				c.moveToFirst();
 				updateViews.setTextViewText(R.id.name, c.getString(1));//TODO change add to the textView we are changing.
 				Intent i = new Intent(this, DetailForm.class); 
				i.putExtra(DetailForm.DETAIL_EXTRA, c.getString(0));
 				PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
 				updateViews.setOnClickPendingIntent(R.id.name, pi);
 				c.close();
 			}
 			else 
 			{
				updateViews.setTextViewText(R.id.name, this.getString(R.string.empty));
 			}
 		}
 		finally 
 		{
 			helper.close();
 		}
 		Intent i = new Intent(this, WidgetService.class);
 		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
 		updateViews.setOnClickPendingIntent(R.id.add1, pi);//TODO same deal here.
 		mgr.updateAppWidget(me, updateViews);
 	}
 }
