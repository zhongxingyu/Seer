 package at.roadrunner.android.model;
 
 import at.roadrunner.android.sensor.Protocol;
 import at.roadrunner.android.sensor.SensorType;
 
 
 
 public class TemperatureSensor extends Sensor {
 	private double _minTemperature;
 	private double _maxTemperature;
 	
 	public TemperatureSensor(String url, Protocol protocol) {
 		super(url, protocol);
 		_type = SensorType.Temperature;
 	}
 	
	public TemperatureSensor(String url, double minTemperature, double maxTemperature) {
		super(url);
 		_type = SensorType.Temperature;
 		_minTemperature = minTemperature;
 		_maxTemperature = maxTemperature;
 	}
 
 	public double getMinTemperature() {
 		return _minTemperature;
 	}
 
 	public void setMinTemperature(double minTemperature) {
 		_minTemperature = minTemperature;
 	}
 
 	public double getMaxTemperature() {
 		return _maxTemperature;
 	}
 
 	public void setMaxTemperature(double maxTemperature) {
 		_maxTemperature = maxTemperature;
 	}
 }
