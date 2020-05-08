 package confdb.data;
 
 import java.util.ArrayList;
 
 
 /**
  * PSetParameter
  * -------------
  * @author Philipp Schieferdecker
  *
  * Model CMSSW's ParameterSet.
  */
 public class PSetParameter extends Parameter
 {
     //
     // data members
     //
     
     /** parameter type string */
     private static final String type = "PSet";
 
     /** parameters */
     private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
 
 
     //
     // construction
     //
     
     /** standard constructor */
     public PSetParameter(String name,ArrayList<Parameter> parameters,
 			 boolean isTracked,boolean isDefault)
     {
 	super(name,isTracked,isDefault);
 	for (Parameter p : parameters) this.parameters.add(p.clone(this));
     }
     
     /** constructor from a string */
     public PSetParameter(String name,String valueAsString,
 			 boolean isTracked,boolean isDefault)
     {
 	super(name,isTracked,isDefault);
 	setValue(valueAsString);
     }
     
 
     /** constructor from a string which contains all the info */
     public PSetParameter(String valueAsString)
     {
 	super("",false,false);
 	setValue(valueAsString);
     }
     
 
     //
     // member functions
     //
 
     /** make a clone of the parameter */
     public Parameter clone(Object parent)
     {
 	PSetParameter result = new PSetParameter(name,parameters,isTracked,isDefault);
 	result.setParent(parent);
 	return result;
     }
     
     /** type of the parameter as a string */
     public String type() { return type; }
     
     /** retrieve the values of the parameter set as a string */
     public String valueAsString()
     {
 	String result = new String();
 	if (isValueSet()) {
 	    result =
 		"<" + type() +
 		" name="     + name() +
 		" default="  + Boolean.toString(isDefault()) +
 		" tracked="  + Boolean.toString(isTracked()) +
 		">";
 	    for (Parameter p : parameters) {
 		if (p instanceof PSetParameter||
 		    p instanceof VPSetParameter) {
 		    result += p.valueAsString();
 		}
 		else {
 		    result +=
 			"<" + p.type() +
 			" name=" + p.name() +
 			" default=" + Boolean.toString(p.isDefault()) +
 			" tracked=" + Boolean.toString(p.isTracked()) +
 			">" + p.valueAsString() + "</" + 
 			p.type() +
 			">";
 		}
 	    }
 	    result += "</" + type() + ">";
 	}
 	return result;
     }
     
     /** set parameter values from string */
     public boolean setValue(String valueAsString)
     {
 	parameters.clear();
 	if (valueAsString.length()>0) {
 	    if (!valueAsString.startsWith("<PSet"))
 		valueAsString=
 		    "<PSet" +
 		    " name=" + name() +
 		    " default=" + Boolean.toString(isDefault()) +
 		    " tracked=" + Boolean.toString(isTracked()) +
 		    ">" + valueAsString + "</PSet>";
 	    ParameterSetParser p1 = new ParameterSetParser(valueAsString);
 	    if (!p1.parseNextParameter()) return false;
 	    String value = p1.value();
 	    if (p1.parseNextParameter()) return false;
 	    ParameterSetParser p2 = new ParameterSetParser(value);
 	    while (p2.parseNextParameter()) {
 		Parameter p = ParameterFactory.create(p2.type(),
 						      p2.name(),
 						      p2.value(),
 						      new Boolean(p2.isTracked()),
 						      new Boolean(p2.isDefault()));
 		p.setParent(this);
 		parameters.add(p);
 	    }
 	}
 	return true;
     }
     
     
     /** a pset is default if all of its children are */
     public boolean isDefault()
     {
 	for (int i=0;i<parameterCount();i++) {
 	    Parameter p = (Parameter)parameter(i);
 	    if (!p.isDefault()) return false;
 	}
 	return true;
     }
 
     /** a pset is set if all of its children are */
     public boolean isValueSet()
     {
 	for (Parameter p : parameters) if (!p.isValueSet()) return false;
 	return (parameters.size()>0);
     }
     
 
     /** number of parameters in parameter-set */
     public int parameterCount() { return parameters.size(); }
 
     /** retrieve the i-th parameter in the set */
     public Parameter parameter(int i) { return parameters.get(i); }
 
     /** index of a certain parameter */
     public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }
 
     /** set i-th parameter of the pset */
     public boolean setParameterValue(int i,String valueAsString)
     {
 	Parameter p = parameter(i);
 	if (!p.setValue(valueAsString)) return false;
 	this.isDefault = isDefault;
 	return true;
     }
     
     /** add a parameter */
     public void addParameter(Parameter p)
     {
 	p.setParent(this);
 	parameters.add(p);
     }
 
     /** remove a parameter */
     public int removeParameter(Parameter p)
     {
 	int index = parameters.indexOf(p);
 	if (index>=0) parameters.remove(index);
 	isValueSet = (parameters.size()>0);
 	return index;
     }
 
 }
 
 
 
 /**
  * ParameterSetParser
  * ------------------
  * @author Philipp Schieferdecker
  *
  * The string representation of a PSetParameter value needs to be
  * somewhat complex, sort of a xml format. this class helps decode it.
  */
 class ParameterSetParser
 {
     //
     // member data
     //
     private String  parseString = null;
     private String  type;
     private String  name;
     private String  value;
     private boolean isDefault = false;
     private boolean isTracked = false;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ParameterSetParser(String parseString)
     {
 	this.parseString = parseString;
     }
     
     //
     // member functions
     //
     
     /** parse the next parameter */
     public boolean parseNextParameter()
     {
 	String s = parseString;
 	
 	int pos = s.indexOf("<"); if (pos==-1) return false;
 	s       = s.substring(pos+1);
 	pos     = s.indexOf(" ");
 	
 	type = s.substring(0,pos);
 	
 	s   = s.substring(pos);
 	pos = s.indexOf(">");
 
 	String attStr = s.substring(1,pos);
 
 	s   = s.substring(pos);
 	
 	String[] atts = attStr.split(" ");
 	for (int i=0;i<atts.length;i++) {
 	    pos = atts[i].indexOf("=");
 	    String attName = atts[i].substring(0,pos);
 	    String attVal  = atts[i].substring(pos+1);
 	    if      (attName.equals("name"))    name     =attVal;
 	    else if (attName.equals("default"))	isDefault=Boolean.valueOf(attVal);
 	    else if (attName.equals("tracked")) isTracked=Boolean.valueOf(attVal);
 	    else return false;
 	}
 	
 	String otag = "<"  + type;
 	String ctag = "</" + type + ">";
 	
 	int opos = s.indexOf(otag);
 	int cpos = s.indexOf(ctag);
 	int skipCount = 0;
 	while (opos>=0&&opos<cpos) {
 	    opos = s.indexOf(otag,opos+1);
 	    cpos = s.indexOf(ctag,opos+1);
 	    skipCount++;
 	}
 	for (int i=0;i<skipCount;i++) cpos = s.indexOf(ctag,cpos+1);
 	
 	value       = s.substring(1,cpos);
 	parseString = s.substring(cpos+ctag.length());
 	
 	return true;
     }
     
     
     /** get last parsed parameter type */
     public String type() { return type; }
 
     /** get last parsed parameter name */
     public String name() { return name; }
     
     /** get last parsed parameter value */
     public String value() { return value; }
     
     /** get last parsed paramter default flag */
     public boolean isDefault() { return isDefault; }
     
     /** get last parsed parameter tracked flag */
     public boolean isTracked() { return isTracked; }
     
 }
