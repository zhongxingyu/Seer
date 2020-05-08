 package sbc.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Egg extends Product {
 
 	private static final long serialVersionUID = 8436930076543223209L;
 	
 	private int colorCount = -1;
 	
 	private boolean colored;
 	private List<String> color = null;
 	private List<Integer> colorer_id = null;
 	
 	public Egg()	{
 		super();
 		colored = false;
 	}
 	
 	/**
 	 * should not be used
 	public Egg(int producer)	{
 		super(producer);
 	}
 	 */
 	
 	public Egg(int producer, int colorCount)	{
 		super(producer);
 		this.color = new ArrayList<String>(colorCount);
 		this.colorer_id = new ArrayList<Integer>(colorCount);
 		this.colorCount = colorCount;
 	}
 	
 	public boolean isColored() {
 		return colored;
 	}
 
 	public void setColored(boolean colored) {
 		this.colored = colored;
 	}
 
 	public void setColorer_id(List<Integer> colorer_id) {
 		this.colorer_id = colorer_id;
 	}
 
 	public List<Integer> getColorer_id() {
 		return colorer_id;
 	}
 
 	
 	public void addColorer_id(int colorer_id)	{
 		if(this.colorer_id == null)	{
 			this.colorer_id = new ArrayList<Integer>();
 		}
 		this.colorer_id.add(colorer_id);
 	}
 	
 	/**
 	 * should not be used
 	 */
 	public void setColorCount(int colorCount) {
 		this.colorCount = colorCount;
 		this.color = new ArrayList<String>(colorCount);
 		this.colorer_id = new ArrayList<Integer>(colorCount);
 	}
 	public int getColorCount() {
 		return colorCount;
 	}
 	
 	public List<String> getColor() {
 		return color;
 	}
 
 	/**
 	 * adds color + colorer id
 	 * @param color
 	 * @param colorer_id
 	 */
 	public void addColor(String color, int colorer_id)	{
 		if(this.color == null)	{
 			this.color = new ArrayList<String>();
 		}
 		
 		if(this.color.size() >= this.colorCount || this.colored)	{
 			return;
 		}
 		
 		this.color.add(color);
 		this.addColorer_id(colorer_id);
 		if(this.color.size() >= this.colorCount)	{
 			this.colored = true;
 		}
 	}
 	
 	/**
 	 * @return the colors + colorer ids as a map
 	 * 		COLOR : COLORER_ID
 	 */
 	public Map<String, Integer> getColorsAsMap()	{
 		if(this.color == null || this.colorer_id == null || this.color.size() != this.colorer_id.size())
 			return null;
 		return new HashMap<String, Integer>(){{
 			for(int i=0; i < color.size();i++)	{
 				put(color.get(i), colorer_id.get(i));
 			}
 		}};
 	}
 	
 	private String colorToString()	{
 		String ret = "[";
		for (String s : color)	{
			ret += s + "|";
 		}
 		return ret.substring(0, ret.length()-1) + "]";
 	}
 	
 	public String toString()	{
 		return "EGG: [id: " + id + ", producer_id: " + producer_id + ", colorCount: " + this.colorCount + ", colors: " + colorToString() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((color == null) ? 0 : color.hashCode());
 		result = prime * result + colorCount;
 		result = prime * result + (colored ? 1231 : 1237);
 		result = prime * result
 				+ ((colorer_id == null) ? 0 : colorer_id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Egg other = (Egg) obj;
 		if (color == null) {
 			if (other.color != null)
 				return false;
 		} else if (!color.equals(other.color))
 			return false;
 		if (colorCount != other.colorCount)
 			return false;
 		if (colored != other.colored)
 			return false;
 		if (colorer_id == null) {
 			if (other.colorer_id != null)
 				return false;
 		} else if (!colorer_id.equals(other.colorer_id))
 			return false;
 		return true;
 	}
 
 }
