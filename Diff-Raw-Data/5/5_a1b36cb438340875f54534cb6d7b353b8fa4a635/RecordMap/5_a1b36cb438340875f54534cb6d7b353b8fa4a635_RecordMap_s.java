 package framework.db;
 
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.LinkedHashMap;
 
 public class RecordMap extends LinkedHashMap<String, Object> {
 	private static final long serialVersionUID = 8720579036875443934L;
 
 	@Override
 	public Object put(String key, Object value) {
 		return super.put(key.toLowerCase(), value);
 	}
 
 	public String getString(String key) {
 		if (get(key) == null) {
 			return "";
 		}
 		return get(key).toString().trim();
 	}
 
 	public int getInt(String key) {
 		return getBigDecimal(key).intValue();
 	}
 
 	public int getInteger(String key) {
 		return getBigDecimal(key).intValue();
 	}
 
 	public long getLong(String key) {
 		return getBigDecimal(key).longValue();
 	}
 
 	public double getDouble(String key) {
 		return getBigDecimal(key).doubleValue();
 	}
 
 	public BigDecimal getBigDecimal(String key) {
 		if (get(key) == null) {
 			return BigDecimal.valueOf(0);
 		}
 		return new BigDecimal(get(key).toString());
 	}
 
 	public float getFloat(String key) {
 		return getBigDecimal(key).floatValue();
 	}
 
 	public Date getDate(String key) {
 		return Date.valueOf(getString(key).substring(0, 10));
 	}
 
 	public Timestamp getTimestamp(String key) {
 		if (get(key) == null) {
 			return null;
 		} else {
 			return Timestamp.valueOf(get(key).toString());
 		}
 	}
 }
