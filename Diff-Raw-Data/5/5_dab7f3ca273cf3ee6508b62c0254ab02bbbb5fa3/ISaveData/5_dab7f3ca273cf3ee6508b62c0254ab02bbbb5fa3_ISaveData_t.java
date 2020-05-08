 package pl.spaceshooters.aurora.util;
 
 public interface ISaveData {
 	
 	void setBoolean(String key, boolean value);
 	
 	int setByte(String key, int value);
 	
 	void setUByte(String key, int value);
 	
 	void setShort(String key, int value);
 	
 	void setUShort(String key, int value);
 	
 	void setInt(String key, int value);
 	
 	void setUInt(String key, long value);
 	
 	void setLong(String key, long value);
 	
 	void setFloat(String key, float value);
 	
 	void setDouble(String key, double value);
 	
 	void setString(String key, String value);
 	
 	boolean getBoolean(String key, boolean defaultValue);
 	
 	boolean getBoolean(String key);
 	
 	int getByte(String key, int defaultValue);
 	
 	int getByte(String key);
 	
 	int getUByte(String key, int defaultValue);
 	
 	int getUByte(String key);
 	
 	int getShort(String key, int defaultValue);
 	
 	int getShort(String key);
 	
 	int getUShort(String key, int defaultValue);
 	
 	int getUShort(String key);
 	
 	int getInt(String key, int defaultValue);
 	
 	int getInt(String key);
 	
 	long getUInt(String key, long defaultValue);
 	
 	long getUInt(String key);
 	
 	long getLong(String key, long defaultValue);
 	
 	long getLong(String key);
 	
 	float getFloat(String key, float defaultValue);
 	
 	float getFloat(String key);
 	
	double getDouble(String key, double defaultValue);
 	
	double getDouble(String key);
 	
 	String getString(String key, String defaultValue);
 	
 	String getString(String key);
 }
