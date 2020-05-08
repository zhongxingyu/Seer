

 package confdb.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 /**
  * Instance
  * --------
  * @author Philipp Schieferdecker
  *
  * abstract base class for Service, EDSource, ESSource, and Module Instances.
  */
 abstract public class Instance extends DatabaseEntry implements Comparable<Instance>
 {
     //
     // member data
     //
     
     /** name of the instance*/
     protected String name = null;
 
     /** reference to the template of this instance */
     private Template template = null;
 
     /** list of parameters */
     private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
 
     /** parent configuration of instance */
     private IConfiguration config = null;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public Instance(String name,Template template) throws DataException
     {
 	this.template = template;
 	setName(name);
 	for (int i=0;i<template.parameterCount();i++)
 	    parameters.add(template.parameter(i).clone(this));
     }
     
     
     //
     // non-abstract member functions
     //
     
     /** overload toString */
     public String toString() { return name(); }
     
     /** Comparable: compareTo() */
     public int compareTo(Instance i) { return toString().compareTo(i.toString()); }
 
     /** name of the instance */
     public String name() { return name; }
     
     /** get the template */
     public Template template() { return template; }
     
     /** get the configuration */
     public IConfiguration config() { return config; }
     
     /** number of parameters */
     public int parameterCount() { return parameters.size(); }
     
     /** get i-th parameter */
     public Parameter parameter(int i) { return parameters.get(i); }
     
     /** get parameter by name and type */
     public Parameter parameter(String name)
     {
 	for (Parameter p : parameters)
 	    if (name.equals(p.name())) return p;
 	return null;
     }
     
     /** get parameter by name and type */
     public Parameter parameter(String name,String type)
     {
 	for (Parameter p : parameters)
 	    if (name.equals(p.name())&&type.equals(p.type())) return p;
 	return null;
     }
     
     /** parameter iterator */
     public Iterator<Parameter> parameterIterator() { return parameters.iterator(); }
 
     /** recursively retrieve parameters to all levels */
     public Iterator<Parameter> recursiveParameterIterator()
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	Parameter.getParameters(parameterIterator(),params);
 	return params.iterator();
     }
 
     /** get all parameters (recursively) with specified name */
     public Parameter[] findParameters(String name)
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	Iterator<Parameter> itP = recursiveParameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 	    String fullParamName = p.fullName();
 	    if (fullParamName.equals(name)||
 		(!fullParamName.equals(name)&&
 		 fullParamName.endsWith("::"+name))) params.add(p);
 	}
 	return params.toArray(new Parameter[params.size()]);
     }
     
     /** find  parameter (recursively) with specified name */
     public Parameter findParameter(String name)
     {
 
 	Iterator<Parameter> itP = recursiveParameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 	    String fullParamName = p.fullName();
 	    if (fullParamName.equals(name)) return p ;
 	}
 	return null;
     }
 
     /** get all parameters (recursively) with specified name *and* type */
     public Parameter[] findParameters(String name,String type)
     {
 	if (type==null) return findParameters(name);
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	Iterator<Parameter> itP = recursiveParameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 	    String fullParamName = p.fullName();
 	    if ((fullParamName.equals(name)||
 		 (!fullParamName.equals(name)&&
 		  fullParamName.endsWith("::"+name)))&&
 		p.type().equals(type)) params.add(p);
 	}
 	return params.toArray(new Parameter[params.size()]);
     }
 
 
     /** get all parameter (recursively) with specified strict name *and* type */
     public Parameter findParameter(String name,String type)
     {
 	if (type==null) return findParameter(name);
 
 	Iterator<Parameter> itP = recursiveParameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 	    String fullParamName = p.fullName();
 	    if ((fullParamName.equals(name))&&
 		p.type().equals(type)) return p;
 	}
 	return null;
     }
     
 
 	/** get all parameters (recursively) with specified name *and* type */
     public Parameter[] findParameters(String name,String type,String value)
     {
 	if (type==null&&value==null) return findParameters(name);
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	Iterator<Parameter> itP = recursiveParameterIterator();
 	while (itP.hasNext()) {
 	    Parameter p = itP.next();
 		String paramType = p.type();
 	    String fullParamName = p.fullName();
 	    String paramValue = p.valueAsString();
 
 		boolean typeMatch = (type==null) ? true : paramType.equals(type);
 		boolean nameMatch = (name==null) ? true :
 			((fullParamName.equals(name))||
 			 (!fullParamName.equals(name)&&fullParamName.endsWith("::"+name)));
 		boolean valueMatch = (value==null) ? true : paramValue.equals(value);
 		if (typeMatch&&nameMatch&&valueMatch) params.add(p);
 	}
 	return params.toArray(new Parameter[params.size()]);
     }
 
     /** get the index of a parameter */
     public int indexOfParameter(Parameter p) { return parameters.indexOf(p); }
     
     /** set the name of this instance */
     public void setName(String name) throws DataException
     {
 	if (template().hasInstance(name)||
 	    (config!=null&&!config.isUniqueQualifier(name)))
 	    throw new DataException("Instance.setName() ERROR: " +
 				    "name '"+name+"' is not unique!");
 	this.name = name;
 	setHasChanged();
     }
     
     /** set the parent configuration of the instance */
     public void setConfiguration(IConfiguration config) { this.config = config; }
 
     /** update a parameter when the value is changed */
     public boolean updateParameter(int index,String valueAsString)
     {
 	String  oldValueAsString = parameter(index).valueAsString();
 	if (valueAsString.equals(oldValueAsString)) return true;
 	
 	String  defaultAsString  = "";
 	if(index<template.parameterCount())
 	    defaultAsString = template.parameter(index).valueAsString();
 	else
 	    System.out.println("Setting the value of an untracked parameter with out any default value");
 
 	if (parameter(index).setValue(valueAsString,defaultAsString)) {
 	    setHasChanged();
 	    return true;
 	}
 	return false;
     }
     
     /** update a parameter when the value is changed */
     public boolean updateParameter(String name,String type,String valueAsString)
     {
 	Parameter param = findParameter(name,type);
 	
 	if (param!=null) {
 	    
 	    int index = indexOfParameter(param);
 	    if (index>=0) return updateParameter(index,valueAsString);
 	    
 	    String a[] = param.fullName().split("::");
 	    if (a.length>1) {
 		String b[] = a[0].split("\\[");
 		Parameter parentParam = parameter(b[0]);
 		int parentIndex = indexOfParameter(parentParam);
 		if (parentIndex>=0&&parentIndex<template.parameterCount()) {
 		    param.setValue(valueAsString,"");
 		    String defaultAsString = template.parameter(parentIndex).valueAsString();
 		    if (parentParam.setValue(parentParam.valueAsString(),
 					     defaultAsString)) {
 			setHasChanged();
 			return true;
 		    }
 		    return false;
 		}else if(parentIndex>=template.parameterCount()){
 		    param.setValue(valueAsString,"");
 		    setHasChanged();
 		    return true;
 		}
 		
 	    }
 	}else{
 	    Parameter parameterNew = ParameterFactory.create(type, name,valueAsString,false,false);
 	    System.out.println("Adding an untracked parameter with out any default value");
 	    parameters.add(parameterNew);
	    parameterNew.setParent(this);
 	    setHasChanged();
 	    return true;
 	}
 	System.err.println("Instance.updateParameter ERROR: "+
 			   "no parameter '"+name+"' of type '"+type+"' "+
 			   "in "+template.name()+"."+name());
 	return false;
     }
     
     /** set parameters */
     public boolean setParameters(ArrayList<Parameter> newParameters)
     {
 	for (int i=0;i<newParameters.size();i++) {
 	    Parameter parameter     = newParameters.get(i);
 	    String    parameterName = parameter.name();
 	    String    parameterType = parameter.type();
 	    String    valueAsString = parameter.valueAsString();
 	    if (!updateParameter(parameterName,parameterType,valueAsString))
 		return false;
 	}
 	return true;
     }
 
     /** remove this instance */
     public void remove()
     {
 	try {
 	    template.removeInstance(name);
 	}
 	catch (DataException e) {
 	    System.err.println("Instance.remove ERROR: "+e.getMessage());
 	}
     }
 
     /** number of unset tracked parameters */
     public int unsetTrackedParameterCount()
     {
 	int result = 0;
 	for (Parameter p : parameters) {
 	    if (p instanceof VPSetParameter) {
 		VPSetParameter vpset = (VPSetParameter)p;
 		result += vpset.unsetTrackedParameterCount();
 	    }
 	    else if (p instanceof PSetParameter) {
 		PSetParameter pset = (PSetParameter)p;
 		result += pset.unsetTrackedParameterCount();
 	    }
 	    else {
 		if (p.isTracked()&&!p.isValueSet()) result++;
 	    }
 	}
 	return result;
     }
 
     public boolean removeUntrackedParameter(Parameter paramRemove)
     {
  	try{
 	    
 	    int iRemoveIndex=parameters.indexOf(paramRemove);
 	    if(iRemoveIndex<template.parameterCount())
 		return false;
 	    parameters.remove(iRemoveIndex);
 	    return true;
 	    
  	}catch(Exception e){
  	    return false;
  	}
     } 
 
 
 }
