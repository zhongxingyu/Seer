 package dinaBOT.sensor;
 
 import dinaBOT.sensor.USSensorListener.*;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.SensorPort;
 
 public class USSensor implements Runnable {
 	
 	public static USSensor high_sensor = new USSensor(SensorPort.S3, Position.HIGH);
 	public static USSensor low_sensor = new USSensor(SensorPort.S1, Position.LOW);
 	
 	UltrasonicSensor sensor;
 	USSensorListener[] listeners;
 	int listeners_pointer;
 	
 	Thread us_sensor_thread;
 	boolean running;
 	
 	Position position;
 	int[] latest_values;
 	
 	public USSensor(SensorPort port, Position position) {
 		sensor = new UltrasonicSensor(port);
 		
		listeners = new UltrasonicSensor[3];
 		listeners_pointer = 0;
 		
 		latest_values = new int[8];
 		this.position = position;
 		sensor.off();
 		
 		start();
 	}
 	
 	public void run() {
 		while(running) {
 			sensor.ping();
 			try {
 				Thread.sleep(50);
 			} catch(Exception e) {
 				
 			}
 			sensor.getDistances(latest_values);
 			notifyListeners();
 		}
 	}
 	
 	void notifyListeners() {
 		for(int i = 0;i < listeners.length;i++) {
			listeners[i].newValues(latest_values, position);
 		}
 	}
 	
 	public void registerListener(USSensorListener listener) {
 		this.listeners[listeners_pointer%listeners.length] = listener;
 		listeners_pointer++;
 	}
 	
 	public void start() {
 		stop();
 		running = true;
 		us_sensor_thread = new Thread(this);
 		us_sensor_thread.setDaemon(true);
 		us_sensor_thread.start();
 	}
 	
 	public void stop() {
 		if(running) {
 			running = false;
 			while(us_sensor_thread != null && us_sensor_thread.isAlive()) Thread.yield();
 		}
 	}
 	
 	
 	
 	
 }
