 package app.model;
 
 import optimizer.Coordenada;
 
 public class EnhancedCoordinate {
 
 	private Coordenada coordenada;
 	private final String deviceName;
 	
 	public EnhancedCoordinate(double latitude, double longitude, String deviceName) {
 		this.coordenada = new Coordenada(latitude, longitude);
 		this.deviceName = deviceName;
 	}
 	
 	public double getLatitude() {
 		return this.coordenada.getLatitude();
 	}
 	
 	public double getLongitude() {
 		return this.coordenada.getLongitude();
 	}
 	
	public Coordenada getCoordenada() {
		return coordenada;
	}
	
 	public String getDeviceName() {
 		return deviceName;
 	}
 }
