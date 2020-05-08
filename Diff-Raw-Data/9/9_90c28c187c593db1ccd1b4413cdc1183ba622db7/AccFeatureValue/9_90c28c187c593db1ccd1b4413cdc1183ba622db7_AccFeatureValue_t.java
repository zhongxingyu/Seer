 package eu.liveandgov.wp1.backend.SensorValueObjects;
 
 import java.util.List;
 
 import eu.liveandgov.wp1.backend.sensorLoop.FeatureHelper;
 
 public class AccFeatureValue {
 	public long startTime;
 	public float xMean;
 	public float yMean;
 	public float zMean;
 	public float S2Mean;
 	public float S2Sd;
 	
 	public AccFeatureValue(AccFeatureValue v) {
 		startTime = v.startTime;
 		xMean = v.xMean;
 		yMean = v.yMean;
 		zMean =  v.zMean;
		S2Mean = v.S2Mean;
 		S2Sd = v.S2Mean;
 	}
 	
 	public AccFeatureValue() {
 	}
 
 	public static AccFeatureValue fromWindow(AccSampleWindow window) {
 		AccFeatureValue o = new AccFeatureValue();
 		o.startTime = window.getStartTime();
 
 		List<AccSensorValue> values =  window.getValues();
 		
 		o.xMean = FeatureHelper.mean(window.getX());
 		o.yMean = FeatureHelper.mean(window.getY());
 		o.zMean = FeatureHelper.mean(window.getZ());
 		
 		float[] S2 = FeatureHelper.S2(window.getX(), window.getY(), window.getZ()); 
 		o.S2Mean = FeatureHelper.mean(S2);
 		o.S2Sd   = (float) Math.pow(FeatureHelper.var(S2), 0.5 );
 		
 		return o;
 	}
 	
 	public String toString(){
 		return String.format("AFV - ts:%d xMean:%f ", startTime, xMean);
 	}
 }
