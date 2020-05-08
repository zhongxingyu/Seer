 package team.GunsPlus.Enum;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public enum EffectSection {
	TARGETLOCATION(), TARGETENTITY(), SHOOTER(), SHOOTERLOCATION(), FLIGHTPATH(), UNDEFINED();
 	
 	private Map<String, Object> data = new HashMap<String, Object>();
 	
 	public Map<String, Object> getData(){
 		return data;
 	}
 	
 	public void setData(Map<String, Object>data){
 		this.data = data;
 	}
 	
 	public void addData(String name, Object data){
 		this.data.put(name, data);
 	}
 	
 	public void removeData(String name){
 		if(this.data.containsKey(name)) { 
 			this.data.remove(name);
 		}
 	}
 }
