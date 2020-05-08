 package fr.flafla.android.urbi.robot;
 
 import java.io.ByteArrayInputStream;
 
 import fr.flafla.android.urbi.UBinary;
 import fr.flafla.android.urbi.UCallback;
 import fr.flafla.android.urbi.UMessage;
 import fr.flafla.android.urbi.control.Axes;
 import fr.flafla.android.urbi.control.Axes.Axis;
 import fr.flafla.android.urbi.control.Camera;
 
 /**
  * Classe de gestion du robot Spykee avec urbi.
  * 
  * @author merlin
  * 
  */
 public class Spykee extends Robot {
 
 	protected class SpykeeCamera extends Camera {
 		private static final String UIMG = "uimg";
 		private boolean init = false;
 
 		@Override
 		public void start(int freq) {
 			if (!init) {
 				addCallback(UIMG, new UCallback() {
 					@Override
 					public boolean handle(UMessage msg) {
						notifyHandlers(new ByteArrayInputStream(((UBinary) msg).array));
 						return true;
 					}
 				});
 
 				addTag(UIMG);
 				sendScript("camera.format = 1|;");
 				sendScript("camera.getSlot(\"val\").notifyChange(uobjects_handle, function() {camera.val})|;");
 				sendScript("every(" + (double) freq / 1000. + "s) {" + UIMG + "<<camera.val},");
 			}
 		}
 
 		@Override
 		public void stop() {
 
 		}
 		
 	}
 
 	/**
 	 * Initialize the spykee with 2 axes with one axis
 	 * @param ip
 	 * @param port
 	 */
 	public Spykee(String ip, int port) {
 		super(ip, port);
 		this.axes = new Axes[] {
 				new Axes(null, new Axis(-100, 100)), new Axes(null, new Axis(-100, 100))
 		};
 		this.cameras = new Camera[] {
 			new SpykeeCamera()
 		};
 		ensureSocket();
 	}
 
 	/**
 	 * Send axes values to the spykee
 	 */
 	@Override
 	public void move() {
 		int trackL = axes[0].y.value;
 		int trackR = axes[1].y.value;
		sendScript("trackL.val="+trackL+"&trackR.val="+trackR+";");
 	}
 }
