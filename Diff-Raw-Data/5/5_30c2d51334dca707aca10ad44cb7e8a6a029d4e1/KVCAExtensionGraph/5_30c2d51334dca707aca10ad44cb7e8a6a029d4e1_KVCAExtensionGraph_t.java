 package er.highcharts.model;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.annotate.JsonAnyGetter;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonRawValue;
 
 import com.webobjects.foundation.NSKeyValueCoding;
 import com.webobjects.foundation.NSKeyValueCodingAdditions;
 
 
 /**
  * KVCAExtensionGraph extends the KVCA pattern to allow arbitrary key paths to be mapped. 
  * 
  * You may nominate any key path you wish to set an object on (including another KVCAExtensionGraph object if you want) 
  * 
  * @author matt
  * 
  */
 public class KVCAExtensionGraph implements NSKeyValueCoding, NSKeyValueCodingAdditions {
 
 	/**
 	 * Standard Logger
 	 */
 	private static Logger log = Logger.getLogger(KVCAExtensionGraph.class);
 	
 	/**
 	 * The nodes. We tell Jackson to ignore this property,
 	 * as the @JsonUnwrapped annotation doesn't play with Map objects.
 	 * Instead, we use the @JsonAnyGetter annotation in a separate object
 	 * in this class: {@code #erHighchartsAnyGetterPolicy}
 	 * 
 	 */
 	@JsonIgnore
 	public Map<String, Object> nodes = new HashMap<String, Object>();
 
 	/**
 	 * The solution to the @JsonUnwrapped issues with Maps.
 	 * 
 	 * This method is called <code>erHighchartsAnyGetterPolicy</code> as it's unlikely
 	 * a user or process will be setting objects on this object with that key.
 	 * 
 	 * @return
 	 */
 	@JsonAnyGetter
 	@JsonRawValue
 	public Map<String, Object> erHighchartsAnyGetterPolicy(){
 		return nodes;
 	}
 	
 	/**
 	 * Constructor
 	 */
 	public KVCAExtensionGraph(){
 		super();
 	}
 	
 	@Override
 	public void takeValueForKeyPath(Object value, String keyPath) {
 		if (keyPath == null) {
 			return;
 		}
 
 		if(keyPath.contains(NSKeyValueCodingAdditions.KeyPathSeparator)){
 			String key = keyPath.substring(0, keyPath.indexOf(NSKeyValueCodingAdditions.KeyPathSeparator));
 			
 			String nextPath = keyPath.substring( key.length() + NSKeyValueCodingAdditions.KeyPathSeparator.length() );
 			
 			Object localObject = nodes.get(key);
 			
 			if(localObject==null){
 
 				localObject = new KVCAExtensionGraph();
 				
 			}
 			
 			if(localObject instanceof KVCAExtensionGraph//needed?
 					|| NSKeyValueCodingAdditions.class.isAssignableFrom(localObject.getClass())
 					){
 
 				((NSKeyValueCodingAdditions)localObject).takeValueForKeyPath(value, nextPath);
 				
 			}
 			
 			nodes.put(key, localObject);			
 			
 		}else{
 			this.takeValueForKey(value, keyPath);//there is no next path, so try to store locally.
 		}
 	
 	}
 
 	@Override
 	public Object valueForKeyPath(String keyPath) {
 		if (keyPath == null) {
 			return null;
 		}
 		if(keyPath.contains(NSKeyValueCodingAdditions.KeyPathSeparator)){
 			String key = keyPath.substring(0, keyPath.indexOf(NSKeyValueCodingAdditions.KeyPathSeparator));
 			
 			String nextPath = keyPath.substring(  key.length() + NSKeyValueCodingAdditions.KeyPathSeparator.length() );
 			
 			Object localObject = nodes.get(key);
 			
 			
 			Object returnValue = null;
 			
 			if(localObject==null){
 				
 				
 			}else{
 
 				if(localObject instanceof KVCAExtensionGraph){//the object is an extension stack
 					
 					returnValue = ((KVCAExtensionGraph)localObject).valueForKeyPath(nextPath);
 					
 				}else if (localObject instanceof List){
 					
 					
				}
				else if( NSKeyValueCodingAdditions.class.isAssignableFrom(localObject.getClass()) ){//the object implements NSKVCA
					returnValue = ((NSKeyValueCodingAdditions)localObject).valueForKeyPath(nextPath);
 					
 				}else{
 					returnValue = null;
 				}
 				
 			}
 			
 			return returnValue;
 		}else{
 			return valueForKey(keyPath);//there is no next path, so try to get what in the local storage.
 		}
 	
 
 	}
 
 	@Override
 	public void takeValueForKey(Object value, String key) {
 		nodes.put(key, value);
 	}
 
 	@Override
 	public Object valueForKey(String key) {
 		return nodes.get(key);
 	}
 
 
 	/**
 	 * Apply all values of the argument to this.
 	 * 
 	 * @param supp - the supplementary Graph to apply
 	 */
 	public void apply(KVCAExtensionGraph supp) {
 		if(supp!=null){
 			//this.nodes.putAll(supp.nodes);//for now we just do a put all, but it really should be smarter than that.
 			
 			Set<String> keys = supp.nodes.keySet();
 			for (String key : keys){
 				this.takeValueForKeyPath(supp.valueForKeyPath(key), key);
 			}
 			
 		}
 	}
 
 }
