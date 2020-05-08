 package org.alt60m.html;
 
 
 public class SelectAoA implements java.io.Serializable {
 
    String[] AoAs = {" ",  "Austrailia and South Pacific", "Canada", "East Asia", "Eastern Europe", "Nigeria and Western Africa (NAWA)", "Namestan", "North America", "Western Europe", "South Asia", "Southeast Asia", "Southern and Eastern Africa", "Francophone Africa", "Latin America", "Cntrl America, Mex, and Carribbean", "Eastern Europe/Russia" };
 	String currentAoA;
     String name;
 
     public SelectAoA() {}
 
     public void setName(String aName) { name = aName; }
 
     public void setCurrentValue(String aValue) {
 		currentAoA = new String(aValue);
     }
 
 
     public String print() {
 	StringBuffer sb = new StringBuffer();
 	int i;
 
 	sb.append("<select name=\"" + name + "\">");
 
	for (i = 0; i < 16; ++i) {
 		if (AoAs[i].equals(currentAoA)) {
 			sb.append("<option value=\"" + AoAs[i] + "\" selected>" + AoAs[i] + "</option>");
 	    } else {
 		sb.append("<option value=\"" + AoAs[i] + "\">" + AoAs[i] + "</option>");
 	    }
 	}
 	sb.append("</select>");
 	return sb.toString();
     }
 }
