 package giulio.frasca.silencesched;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class BackroundProcReciever extends BroadcastReceiver {
	//private boolean serviceRunning = true;
     @Override
     public void onReceive(Context context, Intent intent) {
    	Intent serviceIntent = new Intent(context, BackroundService.class);
		context.startService(serviceIntent);
//        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
         	logcatPrint("SILENCESCHED STARTED");
//        	if (!serviceRunning){
//				serviceRunning=true;
//				Intent serviceIntent = new Intent();
//				serviceIntent.setAction("com.wissen.startatboot.MyService");
//				context.startService(serviceIntent);
//				//context.startService(new Intent(BackroundService.class.getName()));
//				
//			}
//			else{
//				//do nothing
//				//serviceRunning=false;
//				//context.stopService(new Intent(BackroundService.class.getName()));
//			}
//            
//        }
     }
     
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
 }
 
