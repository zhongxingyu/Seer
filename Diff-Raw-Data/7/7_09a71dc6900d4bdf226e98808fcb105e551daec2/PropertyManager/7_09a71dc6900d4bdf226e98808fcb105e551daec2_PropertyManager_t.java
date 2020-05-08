 package team.ApiPlus.Manager;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import team.ApiPlus.API.PropertyHolder;
 
 /** PropertyManager of API+ - Contains useful methods for transferring properties between PropertyHolders or retrieving data 
  * @author Atlan1
  * @version 1.0
  */
 public class PropertyManager {
 
 	
 	/**Method used to copy all properties from one PropertyHolder to another
 	 * @param input The PropertyHolder to copy from
 	 * @param result The PropertyHolder to copy to
 	 * @param replace If true already existing properties of result will be replaced, else not
 	 */
 	public static void copyProperties(PropertyHolder input, PropertyHolder result, boolean replace){
 			for(String id:new HashSet<String>(input.getProperties().keySet())){
 				if(result.getProperty(id)!=null^replace)
 					result.setProperty(id, input.getProperty(id));
 			}
 		}
 	
 	/** Method used to copy the specified keys from one PH to another
 	 * @param input PH to copy from
 	 * @param result PH to copy to
 	 * @param keys The keys to copy
 	 */
 	public static void copySpecifiedKeys(PropertyHolder input, PropertyHolder result, String[] keys){
 		copySpecifiedKeys(input, result, Arrays.asList(keys));
 	}
 	
 	/** Method used to copy the specified keys from one PH to another
 	 * @param input PH to copy from
 	 * @param result PH to copy to
 	 * @param keys The keys to copy
 	 */
 	public static void copySpecifiedKeys(PropertyHolder input, PropertyHolder result, List<String> keys){
 		for(String s : keys){
 			if(input.getProperty(s)!=null){
 				result.setProperty(s, input.getProperty(s));
 			}
 		}
 	}
 	
 
 	/** Method used to get all Properties that are instances of a certain class
 	 * @param p PropertyHolder to retrieve data from
 	 * @pararm c Class to search properties from
 	 * @param invert If true this method will search for all properties that are NOT instances of the given class
 	 * @return Found Properties
 	 */
 	public static Map<String, Object> getPropertiesInstanceOf(PropertyHolder p, Class<?> c, boolean invert) {
 		Map<String, Object> values = new HashMap<String, Object>();
 		for(Object o : new HashSet<Object>(p.getProperties().values())){
//			System.out.print("Object: "+o.getClass()+"; Class: "+c+"; Invert: "+invert+"; Check: "+(c.isInstance(o)^invert));
			if(c.isInstance(o)^invert){
 				for (Entry<String, Object> entry : p.getProperties().entrySet()) {
 			         if (o.equals(entry.getValue())) {
 			             values.put(entry.getKey(), o);
 			         }
 			     }
 			}
 		}
 		return values;
 	}
 	
 	/** Method used to get all Properties that are assignable to a certain class
 	 * @param p PropertyHolder to retrieve data from
 	 * @pararm c Class to search properties from
 	 * @param invert If true this method will search for all properties that are NOT assignable from the given class
 	 * @return Found Properties
 	 */
 	public static Map<String, Object> getPropertiesAssignableFrom(PropertyHolder p, Class<?> c, boolean invert) {
 		Map<String, Object> values = new HashMap<String, Object>();
 		for(Object o : new HashSet<Object>(p.getProperties().values())){
//			System.out.print("Object: "+o.getClass()+"; Class: "+c+"; Invert: "+invert+"; Check: "+(c.isAssignableFrom(o.getClass())^invert));
			if(c.isAssignableFrom(o.getClass())^invert){
 				for (Entry<String, Object> entry : p.getProperties().entrySet()) {
 			         if (o.equals(entry.getValue())) {
 			             values.put(entry.getKey(), o);
 			         }
 			     }
 			}
 		}
 		return values;
 	}
 	
 }
