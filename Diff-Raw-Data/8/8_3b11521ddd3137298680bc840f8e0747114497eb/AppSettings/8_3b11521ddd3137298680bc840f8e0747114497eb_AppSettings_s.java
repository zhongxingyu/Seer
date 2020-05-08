 package org.integration.payments.server.ws.tradeshift.dto;
 
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

 import java.util.HashMap;
 import java.util.Map;
 
 public class AppSettings {
 	private Map<String, Object> settings = new HashMap<String, Object>();
 
 	public Map<String, Object> getSettings() {
 		return settings;
 	}
 
 	public void setSettings(Map<String, Object> settings) {
 		this.settings = settings;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("{");
 
 		sb.append("settings:").append(settings);
 
 		return sb.append("}").toString();
 	}
 }
