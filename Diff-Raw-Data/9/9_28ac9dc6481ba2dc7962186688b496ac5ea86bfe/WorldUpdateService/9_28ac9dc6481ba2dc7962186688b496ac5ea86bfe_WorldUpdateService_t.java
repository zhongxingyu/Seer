 package update;
 
 import java.util.Timer;
 
 import ui.UnabomberMap;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 public class WorldUpdateService extends Service {
 	private Timer timer = new Timer();
 	private static UnabomberMap activity;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		timer.scheduleAtFixedRate(new UpdateWorldTask(activity), 0, 4000);
 	}
 
 	@Override
 	public void onDestroy() {
 		timer.cancel();
 	}
 
 	public static void setActivity(UnabomberMap map) {
 		activity = map;
 	}
 }
