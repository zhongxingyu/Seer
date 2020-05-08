 package vindsiden;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 public class Measurement {
 
 	private int DataID;
 	private int StationID;
 	private DateTime Time;
 	private Double WindAvg;
 	private Double WindVectorAvg;
 	private Double WindStDev;
 	private Double WindMax;
 	private Double WindMin;
 	private int DirectionAvg;
 	private int DirectionVectorAvg;
 	private Double DirectionStDev;
 	private Double Temperature1;
 	private Double Temperature2;
 	private int Light;
 	private Double Battery;
 
 	public int getDataID() {
 		return DataID;
 	}
 
 	public void setDataID(int dataID) {
 		DataID = dataID;
 	}
 
 	public int getStationID() {
 		return StationID;
 	}
 
 	public void setStationID(int stationID) {
 		StationID = stationID;
 	}
 
 	public DateTime getTime() {
 		return Time;
 	}
 
 	public void setTime(DateTime time) {
 		Time = time;
 	}
 
 	public Double getWindAvg() {
 		return WindAvg;
 	}
 
 	public void setWindAvg(Double windAvg) {
 		WindAvg = windAvg;
 	}
 
 	public Double getWindVectorAvg() {
 		return WindVectorAvg;
 	}
 
 	public void setWindVectorAvg(Double windVectorAvg) {
 		WindVectorAvg = windVectorAvg;
 	}
 
 	public Double getWindStDev() {
 		return WindStDev;
 	}
 
 	public void setWindStDev(Double windStDev) {
 		WindStDev = windStDev;
 	}
 
 	public Double getWindMax() {
 		return WindMax;
 	}
 
 	public void setWindMax(Double windMax) {
 		WindMax = windMax;
 	}
 
 	public Double getWindMin() {
 		return WindMin;
 	}
 
 	public void setWindMin(Double windMin) {
 		WindMin = windMin;
 	}
 
 	public int getDirectionAvg() {
 		return DirectionAvg;
 	}
 
 	public void setDirectionAvg(int directionAvg) {
 		DirectionAvg = directionAvg;
 	}
 
 	public int getDirectionVectorAvg() {
 		return DirectionVectorAvg;
 	}
 
 	public void setDirectionVectorAvg(int directionVectorAvg) {
 		DirectionVectorAvg = directionVectorAvg;
 	}
 
 	public Double getDirectionStDev() {
 		return DirectionStDev;
 	}
 
 	public void setDirectionStDev(Double directionStDev) {
 		DirectionStDev = directionStDev;
 	}
 
 	public Double getTemperature1() {
 		return Temperature1;
 	}
 
 	public void setTemperature1(Double temperature1) {
 		Temperature1 = temperature1;
 	}
 
 	public Double getTemperature2() {
 		return Temperature2;
 	}
 
 	public void setTemperature2(Double temperature2) {
 		Temperature2 = temperature2;
 	}
 
 	public int getLight() {
 		return Light;
 	}
 
 	public void setLight(int light) {
 		Light = light;
 	}
 
 	public Double getBattery() {
 		return Battery;
 	}
 
 	public void setBattery(Double battery) {
 		Battery = battery;
 	}
 
 	public String toXml() {
 		XStream xs = new XStream();
 		xs.alias("Measurement", Measurement.class);
 		xs.registerConverter(new JodaTimeConverter());
 		return xs.toXML(this);
 
 	}
 	
 	public String toVindSidenUrl() {
 		return "http://www.vindsiden.no/wrm.aspx"
 				+ "?Id=" + getStationID()
 				+ "&Vind=" + getWindAvg()  
 				+ "&VindMin=" + getWindMin()
 				+ "&VindMax=" + getWindMax()
 				+ "&Retning=" + getDirectionAvg()
				+ "&Temp=" + getTemperature1();
 	}
 
 	public static class JodaTimeConverter implements Converter {
 		@Override
 		public boolean canConvert(final Class type) {
 			return DateTime.class.isAssignableFrom(type);
 		}
 
 		@Override
 		public void marshal(Object source, HierarchicalStreamWriter writer,
 				MarshallingContext context) {
 			DateTime dateTime = new DateTime(source.toString());
 			writer.setValue(dateTime.toString(ISODateTimeFormat
 					.dateTimeNoMillis()));
 		}
 
 		@Override
 		public Object unmarshal(HierarchicalStreamReader reader,
 				com.thoughtworks.xstream.converters.UnmarshallingContext arg0) {
 			return new DateTime(reader.getValue());
 		}
 	}
 
 }
