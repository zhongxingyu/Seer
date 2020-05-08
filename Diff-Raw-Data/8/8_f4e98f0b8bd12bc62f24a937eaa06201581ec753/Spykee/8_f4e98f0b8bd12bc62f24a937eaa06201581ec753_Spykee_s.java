 package fr.flafla.android.urbi.robot;
 
 import java.io.ByteArrayInputStream;
 
 import fr.flafla.android.urbi.UBinary;
 import fr.flafla.android.urbi.UCallback;
 import fr.flafla.android.urbi.UClient;
 import fr.flafla.android.urbi.UMessage;
 import fr.flafla.android.urbi.control.Axes;
 import fr.flafla.android.urbi.control.Axes.Axis;
 import fr.flafla.android.urbi.control.Camera;
 
 /**
  * This class manage a mecano spykee with urbi.<br/>
  * To use it, you have to flash the spykee with the urbi firmware.
  * 
  * @author merlin
  * 
  */
 public class Spykee extends Robot {
 	/** Frequency of the camera */
 	private static final double CAMERA_FREQUENCY = (double) 100 / 1000.;
 	/** Interval between 2 spykee movement order */
 	private static final int INTERVAL_BETWEEN_MOVEMENT = 250;
 
 	UClient uClient;
 	SpykeeMovement threadMovement;
 
 	protected class SpykeeMovement extends SmoothMovement {
 		public SpykeeMovement() {
 			super(INTERVAL_BETWEEN_MOVEMENT);
 		}
 
 		private int lastL = Integer.MAX_VALUE;
 		private int lastR = Integer.MAX_VALUE;
 
 		@Override
 		protected void movement() {
 			int trackL = axes[0].y.value;
 			int trackR = axes[1].y.value;
 			if (lastL != trackL || lastR != trackR) {
 				move(trackL, trackR);
 				lastL = trackL;
 				lastR = trackR;
 				System.out.println("move");
 			}
 		}
 	}
 
 	protected static class SpykeeCamera extends Camera {
 		private static final String UIMG = "uimg";
 		private boolean init = false;
 		private UClient uClient;
 
 		public SpykeeCamera(String ip, int port) {
 			uClient = new UClient(ip, port);
 		}
 
 		@Override
 		public void start() {
 			if (!init) {
 				uClient.addCallback(UIMG, new UCallback() {
 					@Override
 					public boolean handle(UMessage msg) {
 						if (msg instanceof UBinary)
 							notifyHandlers(new ByteArrayInputStream(((UBinary) msg).array));
 						return true;
 					}
 				});
 
 				uClient.addTag(UIMG);
 				uClient.sendScript("camera.format = 1|;");
 				uClient.sendScript("camera.getSlot(\"val\").notifyChange(uobjects_handle, function() {camera.val})|;");
 				uClient.sendScript("every(" + CAMERA_FREQUENCY + "s) {" + UIMG + "<<camera.val},");
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
 		uClient = new UClient(ip, port);
 		threadMovement = new SpykeeMovement();
 
 		this.axes = new Axes[] {
 				new Axes(null, new Axis(-100, 100)), new Axes(null, new Axis(-100, 100))
 		};
 		this.cameras = new Camera[] {
 			new SpykeeCamera(ip, port)
 		};
 		uClient.ensureSocket();
 	}
 
 	/**
 	 * Send axes values to the spykee
 	 */
 	@Override
 	public void move() {
 		// Just ensure the thread is launched
 		threadMovement.launchSmooth();
 	}
 
 	/**
 	 * Stop the spykee
 	 */
 	public void stop() {
 		move(0, 0);
 	}
 
 	/**
 	 * Force move without waiting the next thread step
 	 * @param trackL
 	 * @param trackR
 	 */
 	protected void move(int trackL, int trackR) {
		axes[0].y.value = trackL;
		axes[0].y.value = trackR;
		threadMovement.movement();
 	}
 }
