 package csci498.rlopez.lunchlist;
 
 import org.mcsoxford.rss.RSSFeed;
 import org.mcsoxford.rss.RSSReader;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 
 public class FeedService extends IntentService {
 	
	public static final String EXTRA_URL = "csci498.rlopez.lunchlist.EXTRA_URL";
	public static final String EXTRA_MESSENGER = "csci498.rlopez.lunchlist.EXTRA_MESSENGER";
 	
 	public FeedService() {
 		super("FeedService");
 	}
 	
 	@Override
 	public void onHandleIntent(Intent i) {
 		
 		RSSReader reader = new RSSReader();
 		Messenger messenger = (Messenger)i.getExtras().get(EXTRA_MESSENGER);
 		Message msg = Message.obtain();
 		
 		try {
 			
 			RSSFeed result = reader.load(i.getStringExtra(EXTRA_URL));
 			msg.arg1 = Activity.RESULT_OK;
 			msg.obj = result;
 			
 		} catch (Exception e) {
 			Log.e("LunchList", "Exception parsing feed", e);
			msg.arg1 = Activity.RESULT_CANCELED;
			msg.obj = e;
 		}
 		
 		try {
 			messenger.send(msg);
 		} catch (Exception e) {
 			Log.w("LunchList", "Exception sending results to activity", e );
 		}
 	}
 	
 }
