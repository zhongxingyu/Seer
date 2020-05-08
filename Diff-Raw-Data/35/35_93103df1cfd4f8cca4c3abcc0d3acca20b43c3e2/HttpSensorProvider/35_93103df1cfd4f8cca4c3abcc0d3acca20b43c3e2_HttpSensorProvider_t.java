 /*
  * HttpSensorProvider.java
  */
 package at.roadrunner.android.sensor;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Class HttpSensorProvider
  * 
  * @author matthias schmid
  * @date 25.03.2011
  */
 public class HttpSensorProvider {
 
 	
 	/**
 	 * The Container Id
 	 */
 	protected Integer _container_id;
 	
 	protected SensorConnectionFactory _connection_fact;
 	
 	/**
 	 * Constructor
 	 * @param cId Integer Container Id
 	 */
 	public HttpSensorProvider(Integer cId) {
 		_container_id = cId;
 		_connection_fact = new SensorConnectionFactory();
 	}
 	
 	
 	/**
 	 * Discovers all available sensors 
 	 * 
 	 * @return List<Sensor> the registered sensors of container
 	 * @throws IOException 
 	 */
 	public List<Sensor> discover() throws IOException {
 		
 		String[] uris = getSensorUris(_container_id);
 		List<Sensor> sensors = new ArrayList<Sensor>();
 		for (String s : uris) {
 			URL url = new URL(s);
 			sensors.add(new HttpSensor(url, _connection_fact));
 		}
 		return sensors;
 	}
 	
 	/**
 	 * Gets the URIs of all Sensors in Container _container_id 
 	 * 
 	 * @param cId
 	 * @return String[] 
 	 */
 	private String[] getSensorUris(Integer cId) {
 		String[] sensorUris =  new String[1];
 		
 		sensorUris[0] = "http://roadrunner.server:4711";
 		return sensorUris;
 	}
 }
