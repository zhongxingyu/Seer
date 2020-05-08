 package confdb.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 /**
  * VStringParameter
  * ----------------
  * @author Philipp Schieferdecker
  *
  * for parameters of type vector<string>.
  */
 public class VStringParameter extends VectorParameter
 {
     //
     // data members
     //
 
     /** parameter type string */
     private static final String type = "vstring";
 
     /** parameter values */
     private ArrayList<String> values = new ArrayList<String>();
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public VStringParameter(String name,ArrayList<String> values,
 			    boolean isTracked)
     {
 	super(name,isTracked);
 	for (String s : values) {
 	    if (s!=null) this.values.add(new String(s));
 	    else         this.values.add(new String());
 	}
 	isValueSet = (values.size()>0);
     }
     
     /** constructor from a string */
     public VStringParameter(String name,String valuesAsString,boolean isTracked)
     {
 	super(name,isTracked);
 	setValue(valuesAsString);
     }
     
     
     //
     // member functions
     //
     
     /** make a clone of the parameter */
     public Parameter clone(Object parent)
     {
 	VStringParameter result = new VStringParameter(name,values,isTracked);
 	result.setParent(parent);
 	return result;
     }
     
     /** type of the parameter as a string */
     public String type() { return type; }
     
     /** retrieve the values of the parameter as a string */
     public String valueAsString()
     {
 	String result = new String();
	if (isValueSet) {
 	    for (String v : values) result += "\"" + v + "\"" + ", ";
 	    result = result.substring(0,result.length()-2);
 	}
 	return result;
     }
     
     /** return sorted values as a string */
     public String valueAsSortedString()
     {
 	String result = new String();
 	if (isValueSet) {
 	    ArrayList<String> sortedValues = new ArrayList<String>(values);
 	    Collections.sort(sortedValues);
 	    for (String v : sortedValues) result += "\"" + v + "\"" + ", ";
 	    result = result.substring(0,result.length()-2);
 	}
 	return result;
     }
     
 
     /** set parameter values from string */
     public boolean setValue(String valueAsString)
     {
 	values.clear();
 	if (valueAsString==null||valueAsString.length()==0) {
 	    isValueSet = false;
 	}
 	else {
 	    String delim = null;
 	    StringBuffer sb = new StringBuffer();
 	    for (int i=0;i<valueAsString.length();i++) {
 		String c =
 		    new StringBuffer()
 		    .append(valueAsString.charAt(i)).toString();
 		if (c.equals(",")&&delim==null) {
 		    values.add(stripDelimiters(sb.toString()));
 		    sb = new StringBuffer();
 		}
 		else {
 		    sb.append(c);
 		    if (delim!=null&&c.equals(delim)) delim=null;
 		    else if (c.equals("\'")||c.equals("\"")) {
 			sb = new StringBuffer();
 			sb.append(c);
 			delim = c;
 		    }
 		}
 	    }
 	    if (delim!=null)
 		System.err.println("VStringParameter::setValue WARNING: "+
 				   "value incomplete (delim="+delim+")? "+
 				   sb.toString());
 	    if (sb.length()>0) values.add(stripDelimiters(sb.toString()));
 	    
 	    isValueSet = true;
 	}
 	return true;
     }
     
     /** number of vector entries */
     public int vectorSize() { return values.size(); }
 
     /** i-th value of a vector type parameter */
     public Object value(int i) { return values.get(i); }
 
     /** set i-th value of a vector-type parameter */
     public boolean setValue(int i,String valueAsString)
     {
 	if ((valueAsString.startsWith("'")&&valueAsString.endsWith("'"))||
 	    (valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
 	    values.set(i,valueAsString.substring(1,valueAsString.length()-1));
 	else
 	    values.set(i,new String(valueAsString));
 	return true;
     }
 
     /** add value value of a vector-type parameter */
     public boolean addValue(String valueAsString)
     {
 	if ((valueAsString.startsWith("'")&&valueAsString.endsWith("'"))||
 	    (valueAsString.startsWith("\"")&&valueAsString.endsWith("\"")))
 	    values.add(valueAsString.substring(1,valueAsString.length()-1));
 	else
 	    values.add(new String(valueAsString));
 	isValueSet = (values.size()>0);
 	return true;
     }
 
     /** remove i-th value from vector type parameter */
     public Object removeValue(int i)
     {
 	Object result = values.remove(i);
 	isValueSet = (values.size()>0);
 	return result;
     }
 
     //
     // private member functions
     //
 
     /** strip string-delimiters and whitespaces */
     private String stripDelimiters(String valueAsString)
     {
 	String s = valueAsString;
 	while (s.startsWith(" ")) s = s.substring(1,s.length());
 	while (s.endsWith(" "))   s = s.substring(0,s.length()-1);
 	if ((s.startsWith("'")&&s.endsWith("'"))||
 	    (s.startsWith("\"")&&s.endsWith("\"")))
 	    s = s.substring(1,s.length()-1);
 	return s;
     }
 
 
     
 
 }
