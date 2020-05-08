 package fr.keuse.rightsalert.thread;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.keuse.rightsalert.entity.ApplicationEntity;
 import fr.keuse.rightsalert.handler.LoadApplicationsHandler;
 
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Message;
 
 public class LoadApplicationsThread extends Thread {
 	PackageManager pm;
 	LoadApplicationsHandler handler;
 	private List<PackageInfo> packages;
 	
 	public LoadApplicationsThread(PackageManager pm) {
 		this.pm = pm;
 		packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
 	}
 	
 	public void setHandler(LoadApplicationsHandler handler) {
 		this.handler = handler;
 	}
 
 	@Override
 	public void run() {
 		ArrayList<ApplicationEntity> applications = new ArrayList<ApplicationEntity>();
 		
 		sendOpenPopup();
 		
 		Message msg;
 		for(PackageInfo p : packages) {
 			msg = handler.obtainMessage();
 			msg.arg1 = LoadApplicationsHandler.MSG_UPDATE_PROGRESS;
 			msg.arg2 = packages.indexOf(p);
 			msg.obj = pm.getApplicationLabel(p.applicationInfo).toString();
 			handler.sendMessage(msg);
 			
 			ApplicationEntity app = new ApplicationEntity(p, pm);
 			if(app.isDangerous())
 				applications.add(app);
 			
 			try {
 				// Sleep for 10 ms on each PackageInfo to prevent lags on the application
 				sleep(10);
 			} catch (InterruptedException e) {
				return;
 			}
 		}
 		
 		msg = handler.obtainMessage();
 		msg.arg1 = LoadApplicationsHandler.MSG_FINISH_PROGRESS;
 		msg.obj = applications;
 		handler.sendMessage(msg);
 	}
 	
 	public void sendOpenPopup() {
 		Message msg = handler.obtainMessage();
 		msg.arg1 = LoadApplicationsHandler.MSG_START_PROGRESS;
 		msg.arg2 = packages.size();
 		handler.sendMessage(msg);
 	}
 }
