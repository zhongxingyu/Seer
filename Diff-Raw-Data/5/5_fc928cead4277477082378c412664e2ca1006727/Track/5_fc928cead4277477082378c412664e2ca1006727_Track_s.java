 package it.polito.atlas.alea2;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public abstract class Track {
 
 	private String name;
 	public Track (String name) {
 		this.setName(name);
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	public enum Types {
 		Video, LIS, Text
 	}		
 	protected Types type;
 	
 	/**
 	 * @return the type of Track
 	 */
 	public Types getType() {
 		return type;
 	}
 	
 	public String getTypeString() {
 		return getTypeString(this.type);
 	}
 	
 	public static String getTypeString(Types type) {
 		String typeStr = null;
 		switch (type) {
 			case Video:
 				typeStr = "video";
 				break;
 			case LIS:
 				typeStr = "lis";
 				break;
 			case Text:
 				typeStr = "text";
 				break;
 		}
 		return typeStr;
 	}
 	
 	public Collection <Slice> slices = new ArrayList<Slice>();
 	
 	public long getEndTime() {
 		long tmp, end = -1;
 		for (Slice s : slices) {
 			tmp = s.getEndTime();
 			if ( tmp > end)
 				end = tmp;
 		}
 		return end;
 	}
 	
 	public Collection <Slice> getSlices() {
 		return slices;
 	}
 
 	public boolean addSlices(Collection<Slice> slices) {
		slices.addAll(slices);
 		return true;
 	}
 
 	public boolean addSlice(Slice slice) {
		slices.add(slice);
 		return true;
 	}
 
 	/**
 	 * Collegamento ad un oggetto che rappresenta la traccia
 	 */
 	public Object link;
 }
