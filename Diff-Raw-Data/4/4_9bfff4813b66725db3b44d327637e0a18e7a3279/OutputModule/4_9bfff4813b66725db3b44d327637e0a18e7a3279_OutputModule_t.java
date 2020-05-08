 package confdb.data;
 
 import java.util.Iterator;
 import java.util.ArrayList;
 
 
 /**
  * OutputModule
  * ------------
  * @author Philipp Schieferdecker
  *
  * Explicit model for OutputModules, which are not -- like
  * ModuleInstances -- bound to a ModuleTemplate and don't have an
  * arbitrary list of parameters. OutputModules are linked to a stream
  * instead, and the values of their two parameters SelectEvents and
  * outputCommands are directly derived from teh associated Stream and
  * its parent EventContent.
  */
 public class OutputModule extends ParameterContainer implements Referencable
 {
     //
     // data members
     // 
     
     /** name of the class */
     private String className = "ShmStreamConsumer";
 
     /** name of this OutputModule */
     private String name = "";
 
     /** reference to the parent stream */
     private Stream parentStream = null;
 
     /** vstring SelectEvents parameter, which contains the paths */
     private VStringParameter vstringSelectEvents = null;
 
     /** vstring outputCommands parameter, defining the data format */
     private VStringParameter vstringOutputCommands = null;
 
     /** list of references to output module (within reference containers) */
     private ArrayList<OutputModuleReference> references =
 	new ArrayList<OutputModuleReference>();
     
 
     //
     // construction
     //
 
     /** standard constructor */
     public OutputModule(String name, Stream parentStream)
     {
 	this.name = name;
 	this.parentStream = parentStream;
 
 	PSetParameter psetSelectEvents =
 	    new PSetParameter("SelectEvents","",false);
 	
 	vstringSelectEvents =
 	    new VStringParameter("SelectEvents","",true);
 	psetSelectEvents.addParameter(vstringSelectEvents);
 	
 	vstringOutputCommands =
 	    new VStringParameter("outputCommands","",false);
 
 	addParameter(psetSelectEvents);
 	addParameter(vstringOutputCommands);
     }
     
     
     //
     // member functions
     //
 
     /** Object: toString() */
     public String toString() { return name(); }
 
     /** ParameterContainer: indicate wether parameter is at its default */
     public boolean isParameterAtItsDefault(Parameter p)  { return false; }
     
     /** ParameterContainer: indicate wether a parameter can be removed */
     public boolean isParameterRemovable(Parameter p)
     {
 	int index = indexOfParameter(p);
 	if (index<2) return false; // protect SelectEvents & outputCommands!
 	return true;
     }
     
     /** ParameterContainer: remove a parameter */
     public void removeParameter(Parameter parameter)
     {
 	if (parameter.name().equals("SelectEvents")||
 	    parameter.name().equals("outputCommands")) return;
 	super.removeParameter(parameter);
     }
     
     /** ParameterContainer: clear() */
     public void clear()
     {
 	System.err.println("OutputModule ERROR: don't you dare to clear()!");
     }
     
     /** ParameterContainer: retrieve i-th parameter */
     public Parameter parameter(int i)
     {
 	updateSelectEvents();
 	updateOutputCommands();
 	return super.parameter(i);
     }
 
     /** ParameterContainer: retrieve parameter iterator */
     public Iterator<Parameter> parameterIterator()
     {
 	updateSelectEvents();
 	updateOutputCommands();
 	return super.parameterIterator();
     }
 
     /** ParameterContainer: get parameter by name */
     public Parameter parameter(String name)
     {
 	updateSelectEvents();
 	updateOutputCommands();
 	return super.parameter(name);
     }
     
     /** ParameterContainer: get parameter by name AND type */
     public Parameter parameter(String name,String type)
     {
 	updateSelectEvents();
 	updateOutputCommands();
 	return super.parameter(name,type);
     }
 
     /** ParameterContainer: update a parameter with a new value */
     public boolean updateParameter(int index,String valueAsString)
     {
 	if (index<2) return false;
 	return super.updateParameter(index,valueAsString);
     }
     
     /** update a parameter when the value is changed */
     public boolean updateParameter(String name,String type,String valueAsString)
     {
 	if (name.equals("SelectEvents")||name.equals("outputCommands"))
 	    return false;
 	return super.updateParameter(name,type,valueAsString);
     }
     
 
     /** retrieve the class name of the output module */
     public String className() { return className; }
 
     /** retrieve the parent stream of the output module */
     public Stream parentStream() { return parentStream; }
 
     /** Referencable: name() */
     public String name() { return name; }
     
     /** Referenable: setName() */
     public void setName(String name) throws DataException { this.name = name; }
     
     /** Referencable: retrieve the parent configuration */
     public IConfiguration config() { return parentStream().config(); }
     
     /** Referencable: create a reference of this output module */
     public Reference createReference(ReferenceContainer container, int i)
     {
 	OutputModuleReference reference =
 	    new OutputModuleReference(container,this);
 	references.add(reference);
 	container.insertEntry(i,reference);
 	return reference;
     }
     
     /** Referencable: number of references */
     public int referenceCount() { return references.size(); }
 
     /** Referencable: retrieve the i-th reference */
     public Reference reference(int i) { return references.get(i); }
 
     /** Referencable: test if specific reference refers to this o-module */
     public boolean isReferencedBy(Reference reference)
     {
 	return references.contains(reference);
     }
     
     /** Referencable: remove a reference to this output module */
     public void removeReference(Reference reference)
     {
 	int index = references.indexOf(reference);
 	references.remove(index);
 	//if (referenceCount()==0) remove();
     }
    
     
     /** Referencable: get list of parent paths */
     public Path[] parentPaths()
     {
 	ArrayList<Path> list = new ArrayList<Path>();
 	for (int i=0;i<referenceCount();i++) {
 	    Path[] paths = reference(i).parentPaths();
 	    for (Path p : paths) list.add(p);
 	}
 	return list.toArray(new Path[list.size()]);
     }
 
     
     //
     // private member functions
     //
 
     /** update value of 'SelectEvents' parameter */
     private void updateSelectEvents()
     {
 	StringBuffer valueAsString = new StringBuffer();
 	Iterator<Path> itP = parentStream().pathIterator();
 	while (itP.hasNext()) {
 	    if (valueAsString.length()>0) valueAsString.append(",");
 	    valueAsString.append(itP.next().name());
 	}
 	vstringSelectEvents.setValue(valueAsString.toString());
     }
     
     /** update value of 'outputCommands' parameter */
     private void updateOutputCommands()
     {
 	// first collect all commands as strings, remove duplicates
 	ArrayList<String> listOfCommands = new ArrayList<String>();
 	Iterator<OutputCommand> itOC =
 	    parentStream().parentContent().commandIterator();
 	while (itOC.hasNext()) {
 	    String commandAsString = itOC.next().toString();
 	    if (listOfCommands.indexOf(commandAsString)<0)
 		listOfCommands.add(commandAsString);
 	}
 	
 	// now reformat them according to vstring requirements
 	StringBuffer valueAsString = new StringBuffer();
 	Iterator<String> itS = listOfCommands.iterator();
 	while (itS.hasNext()) {
 	    if (valueAsString.length()>0) valueAsString.append(",");
 	    valueAsString.append(itS.next());
 	}
 	vstringOutputCommands.setValue(valueAsString.toString());
     }
 
     
     public boolean hasChanged(){
 	return parentStream.hasChanged();
     }
 
    public void setHasChanged(){
        parentStream.setHasChanged();
    }

     public int databaseId(){
 	return parentStream.databaseId();
     }    
 }
 
