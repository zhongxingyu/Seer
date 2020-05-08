 package giulio.frasca.silencesched;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class BackroundProcReciever extends BroadcastReceiver {
	private boolean serviceRunning = true;
     @Override
     public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
         	logcatPrint("SILENCESCHED STARTED");
        	if (!serviceRunning){
				serviceRunning=true;
				context.startService(new Intent(BackroundService.class.getName()));
				
			}
			else{
				serviceRunning=false;
				context.stopService(new Intent(BackroundService.class.getName()));
			}
            
        }
     }
     
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
 }
 
