 package info.ss12.audioalertsystem.notification;
 
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class CameraLightNotification extends AbstractNotification {
 
 	private Camera camera;
 	private Parameters light_ON;
 	private Parameters light_OFF;
 	private Timer timer;
 	private boolean lightToggle;
 	
 	public CameraLightNotification() {
 		camera = Camera.open();
		light_ON = camera.getParameters();
		light_OFF = camera.getParameters();
 		light_ON.setFlashMode(Parameters.FLASH_MODE_TORCH);
 		light_OFF.setFlashMode(Parameters.FLASH_MODE_OFF);
 		timer = new Timer();
 		lightToggle = false;
 	}
 	
 	@Override
 	public void startNotify() {
		
 		camera.startPreview();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				if (lightToggle) {
 					camera.setParameters(light_ON);
 				}
 				else {
 					camera.setParameters(light_OFF);
 				}
 				lightToggle ^= true;
 			}
 		}, 0, 500);
 	}
 
 	@Override
 	public void stopNotify() {
 		timer.cancel();
 		camera.stopPreview();
 		camera.release();
 	}
 }
