 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sf.taverna.portal.wireit.module;
 
 import net.sf.taverna.portal.utils.DelimiterURI;
 import net.sf.taverna.portal.wireit.exception.WireItRunException;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.json.JSONException;
 import org.json.JSONObject;
 import net.sf.taverna.portal.wireit.RunWireit;
 import net.sf.taverna.portal.wireit.event.OutputFirer;
 import net.sf.taverna.portal.wireit.event.OutputListener;
 import net.sf.taverna.portal.commandline.CommandLineRun;
 import net.sf.taverna.portal.commandline.CommandLineWrapper;
 import net.sf.taverna.portal.commandline.ProcessException;
 import net.sf.taverna.portal.commandline.TavernaException;
 import net.sf.taverna.portal.commandline.TavernaInput;
 import net.sf.taverna.portal.baclava.DataThingBasedBaclava;
 import net.sf.taverna.portal.workflow.TavernaWorkflow;
 import net.sf.taverna.portal.workflow.XMLBasedT2Flow;
 import net.sf.taverna.portal.utils.Resolver;
 
 /**
  * This module wraps the running of a taverna workflow.
  * <p>
  * The current implentation is written around A Taverna command line Wrapper written for Ondex.
  * That tool does not provide the security nor all the options that Taverna Server does.
  * It is therefor recommended that this class be updated to use Taverna Server.
  * <p>
  * The main functionality (which should stay even with a server versions) is.
  * <ul>
  *    <li> Extract the required inputs and outputs from the Workflow 
  *    <li> Allow Wiring to connect to terminals with that match does in the workflow
  *    <li>Listen for the required inputs from other modules
  *    <ul> 
  *        <li> Could be a Baclava ULI
  *        <li> If individual inputs are used this module will them. All are required for the next steps.
  *    </ul>
  *    <li> Pass the inputs to Taverna
  *    <li> Execute the workflow
  *    <li> Obtain the resulting Baclava output file
  *    <li> Extract the indiviual results
  *    <li> Pass the idividual results to any listeners
  *    <li> Pass the Baclava outout to and Listeners on the Baclava port
  * </ul>
  * @author Christian
  */
 public class TavernaModule extends Module{
 
     /** Wrapper around Taverna Command Line. Original written for Ondex*/
     private CommandLineWrapper commandLine;
     
     /** Map of Listeners. 
      *  One for each input port. (Exceluding the Baclava input)
      *  React to any data from upstream Modules.    
      */
     private Map<String,ValueListener> inputPorts;
 
     /**
      * Map of input storage classes.
      * One for each input port. (Exceluding the Baclava input)
      * Store data from upstream Modules.    
      * <p>
      * TavernaInput was written for the ONDEX project.
      * The functionality includes
      * <ul>
      *    <li>Allowing different types of inputs to be stored.
      *    <ul>
      *        <li>Depth 0 value (ast String)
      *        <li>Depth 0 URI
      *        <li>Depth 1 arrays of Values (as Strings)
      *        <li>depth 1 URI and delimiter
      *    </ul>
      *    <li>Single method to say if it has ai input or not
      *    <li>Ability to add the required command line arguements for the input passed in.
      *    <ul>
      *        <li>Correct parameter flag  
      *        <li>Name of the port
      *        <li>Value / uri
      *        <li>Where applicable Delimiter 
      *        <li>Array fo Strings conctanated to a Single String with a suitable Delimiter
      * </ul>     
      * <p>
      * Even if using Taverna Server something like this will be required to handle the inputs.
      */
     private Map<String,TavernaInput> tavernaInputs;
     
     /** For Baclava input the URI is saved as a String. */
     private String baclavaInput;
 
     /** Map of Firers. One for each output port. Passing to any data from downstream Modules */    
     private Map<String,OutputFirer> outputPorts;
     
     /** Firer for anyone Listening ofr the Baclava */
     private OutputFirer baclavaOutput;
     
     /** Flag to avoid run causing execution if inputs alread have */
     private boolean alreadyRun = false;
     
     /** Handles absolute to relative URI mapping */
     private Resolver resolver;
     
     /** Name of the folder to put outputs in */
     private static final String OUTPUT_DIR = "Outputs";
             
     /**
      * Constructor which sets up the module based on the json.
      * <p>
      * See setWorkflow for specific json required.
      * <p>
      * Taverna home must be set either from the environment variable "TAVERNA_HOME"  
      * or from RunWireit which gets it from the servlet context InitParameter(TAVERNA_CMD_HOME_PARAMETER);
      * @param json JSON object including "wfURI" in the "config"
      * @param resolver  Handles absolute to relative URI mapping
      * @throws JSONException Thrown if the json is not in the expected format.
      * @throws TavernaException Thrown if a setting travena home or the workflow fails
      * @throws IOException  Thrown if Workflow can not be read
      */
     public TavernaModule(JSONObject json, Resolver resolver) 
             throws JSONException, TavernaException, IOException{
         super(json);
         commandLine = new CommandLineWrapper();
         setTavernaHome(System.getenv("TAVERNA_HOME"));
         setTavernaHome(RunWireit.getTavernaHome());
         this.resolver = resolver;
         commandLine.setOutputRootDirectory(resolver.getRelativeFile(OUTPUT_DIR));
         
         setWorkflow();  
        // URLRoot = URL.substring(0, URL.lastIndexOf("/"));
     }
     
     /**
      * Sets the location of the Taverna Commandline.
      * 
      * @param tavernaHome Either the path to commandLine or null.
      * @throws TavernaException If a none NULL path does not have the required file. 
      * @throws IOException If creating the File fails.
      */
     public final void setTavernaHome(String tavernaHome) throws TavernaException, IOException {
         if (tavernaHome != null && !tavernaHome.isEmpty()){
             commandLine.setTavernaHome(new File(tavernaHome));        
         } 
     }
     
     /**
      * Gets and parses a workflow from the config's "wfURI".
      * <p>
      * The Workflow is parsed to get the Inputs and the outputs.
      * <p>
      * Current implementation uses an XML Hack to read the workflow.
      * This should be replaced with proper taverna code.
      * Similarly T2 and scufl should be supported.
      * 
      * @throws TavernaException Any error parsing the workflow
      * @throws IOException Any error reading the workflow
      */
     private void setWorkflow() throws TavernaException, IOException{
         String fileSt = config.optString("wfURI");
         //Checks for security. Change as required
         if (fileSt.contains("..")){
             throw new TavernaException ("Security exception uris can not contain \"..\"");
         }
         File workflowFile = resolver.getRelativeFile(fileSt);
         commandLine.setWorkflowFile(workflowFile);
         
         TavernaWorkflow workflow = new XMLBasedT2Flow(workflowFile);
         setInputs(workflow);
         setOutputs(workflow);
     }
         
     /**
      * Sets up the TavernaInputs and the Listeners including the Baclava one.
      * 
      * @param workflow Interface that gives the workflow name, input ports and output ports
      * @throws TavernaException Thrown if an unexpected port name is received.
      */
     private void setInputs(TavernaWorkflow workflow) throws TavernaException{
         //removeNullandEmptyValues();
         Map<String,Integer> inputs = workflow.getInputs();  
         inputPorts = new HashMap<String,ValueListener>();
         tavernaInputs = new HashMap<String,TavernaInput>();
         for (String key:inputs.keySet()){
             TavernaInput tavernaInput = new TavernaInput(key, inputs.get(key));
             tavernaInputs.put(key, tavernaInput);
             ValueListener port = new ValueListener(tavernaInput);
             inputPorts.put(key, port);
         }
         baclavaInput = null;
     }
 
     /**
      * Sets up the output firers including the Baclava one.
      * @param workflow Interface that gives the workflow name, input ports and output ports
      */
     private void setOutputs(TavernaWorkflow workflow){
         outputPorts = new HashMap<String,OutputFirer>();
         List<String> outputs = workflow.getOutputs();
         for (String output: outputs){
             outputPorts.put(output, new OutputFirer());
         }
         baclavaOutput = new OutputFirer();
     }
     
     //Will only run the workflow if no input is required.
     @Override
     public void run(StringBuilder outputBuilder) throws WireItRunException {
         //Just in case their are no inputs are all set as values.
         if (!alreadyRun) { 
             runIfReady(outputBuilder);
         }
     }
     
     @Override
    /**
      * Returns an listner for the requested port.
      * <p>
      * See Wiring.java for more details.
      * <p>
      * The allowed port names are:
      * <ul>
      *     <li>An input port in the workflow
      *     <li>"in_"concat An input port in the workflow
      *     <ul>
      *         <li>this is required if an input and an output port share the same name.
      *     </ul>
      *     <li>"Baclava Input"
      * </ul>
      * @param terminal Name of the Input port to be attached.
      * @return The Listener that will handles the Object coming in.
      * @throws JSONException If the terminal name does not match on of the legal values see above.
      */
     public OutputListener getOutputListener(String terminal) throws JSONException {
         if (inputPorts.containsKey(terminal)){
             return inputPorts.get(terminal);
         } else if (terminal.startsWith("in_") && inputPorts.containsKey(terminal.substring(3))) {
             return inputPorts.get(terminal.substring(3));            
         } else if (terminal.equals("Baclava Input")){
             return new BaclavaListener();
         } else {
             String portNames = "";
             for (String key:inputPorts.keySet()){
                 portNames = portNames + key + ", ";
             }
             throw new JSONException("No input Port found with name " + terminal + " Ports are: " + portNames);
         }
     }
 
     @Override
     /**
      * Adds the Listener as one of the downstream modules to this one.
      * <p>
      * The allowed port names are:
      * <ul>
      *     <li>An outnput port in the workflow
      *     <li>"out_"concat An output port in the workflow
      *     <ul>
      *         <li>this is required if an input and an output port share the same name.
      *     </ul>
      *     <li>"Baclava Output"
      * </ul>     * 
      * @param terminal Name of the output port to be attached.
      * @param listener Listener to be used.
      * @throws JSONException It is WireIt's responsibility that the "wires" array correctly matches the "modules" array.
      *    If this is not the case an exception is thrown.
      */
     public void addOutputListener(String terminal, OutputListener listener) throws JSONException {
         if (outputPorts.containsKey(terminal)){
             outputPorts.get(terminal).addOutputListener(listener);
         } else if (terminal.equals("Baclava Output")){
             baclavaOutput.addOutputListener(listener);
         } else if (terminal.startsWith("out_") && outputPorts.containsKey(terminal.substring(4))) {
             outputPorts.get(terminal.substring(4)).addOutputListener(listener);           
         } else {
             String portNames = "";
             for (String key:outputPorts.keySet()){
                 portNames = portNames + key + ", ";
             }
             throw new JSONException("No output Port found with name " + terminal + " Ports are: " + portNames);
         }
     }
     
     /**
      * Check if all inputs have a value associated with them.
      * @return true if and only if all inputs have been set.
      */
     private boolean allValuesSet(){        //ystem.out.println("in allValuesSet" + values.size());
         for (String key:tavernaInputs.keySet()){
             if (!tavernaInputs.get(key).hasValue()){
                 return false;
             }
         }
         return true;
     }
      
     /**
      * Runs the workflow based on indivudual inputs.
      * <p>
      * Obtains the TavernaInputs from the map and passes these to the command line wrapper.
      * <p>
      * The runs the workflow and obtains the Baclava file.
      * @return Baclava Output File
      * @throws TavernaException Unable to start the process
      * @throws ProcessException Error running the process
      */ 
     private File runWorkflowWithInputs() throws TavernaException, ProcessException{
         System.out.println("Workflow ready based on inputs!");
         TavernaInput[] inputArray = new TavernaInput[0];
         inputArray = tavernaInputs.values().toArray(inputArray);
         commandLine.setInputs(inputArray);
         System.out.println("ready to run");
         CommandLineRun run = commandLine.runWorkFlow();
         System.out.println("ready started");
         File output = run.getOutputFile();
         System.out.println("Workflow ran");
         return output;
     }
 
    /**
      * Runs the workflow based on Baclava input.
      * <p>
      * Send the Baclava input to the command line wrapper.
      * <p>
      * The runs the workflow and obtains the Baclava file.
      * @return Baclava Output File
      * @throws TavernaException Unable to start the process
      * @throws ProcessException Error running the process
      */ 
     private File runWorkflowWithBaclava() throws TavernaException, ProcessException{
         System.out.println("Workflow ready based on Baclava!" + baclavaInput);
         commandLine.setInputsURI(baclavaInput);        
         CommandLineRun run = commandLine.runWorkFlow();
         File output = run.getOutputFile();
         System.out.println("Workflow ran");
         return output;
     }
 
     /**
      * Checks to see if the required inputs are there and if so runs and processes output.
      * <p>
      * A workflow will run if it requires no input.
      * <p>
      * A workflow will run based on inputs if all the inputs declared in the workflow have had some value assigned.
      * <p>
      * The workflow will run if a Baclava value has been set.
      * <p>
      * The resulting Baclava file is processed (values passed on).
      * @param outputBuilder Logging buffer.
      * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
      */
     private void runIfReady(StringBuilder outputBuilder) throws WireItRunException {
         File output;
         if (allValuesSet()){
             try {
                 output = runWorkflowWithInputs();             
             } catch (Exception ex) {
                  throw new WireItRunException("Error running workflow: " + name + "  " + ex.getMessage(), ex);
             } 
             processRun(output, outputBuilder);
         } else if (baclavaInput != null) {
             try {
                 output = runWorkflowWithBaclava();             
             } catch (Exception ex) {
                  throw new WireItRunException("Error running workflow: " + name + "  " + ex.getMessage(), ex);
             } 
             processRun(output, outputBuilder);
         }
     }
     
     /**
      * Process the Baclava file sending both Individual Values and the whole Baclava to relative Listeners.
      * <p>
      * For Each putput port the associated value is obtained (as an Object) and passed to any registered listeners.
      * <p>
      * The output file is converted to a URI and passed to any listners on the Baclava output
      * <p>
      * This is slighlty sub optimal as all the output values are extracted 
      *    even ones where there is never a connected Listener.
      * @param output Baclava File output by either runWorkflowWithXXX method.
      * @param outputBuilder Logging buffer.
      * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
      *     These could come from downstream modules.
      */
     private void processRun(File output, StringBuilder outputBuilder) throws WireItRunException {
         alreadyRun = true;
         DataThingBasedBaclava baclava;
         outputBuilder.append("Workflow ");
         outputBuilder.append(name);
         outputBuilder.append(" ran successfully.\n");
         try {
             baclava = new DataThingBasedBaclava(output);
         } catch (TavernaException ex) {
             throw new WireItRunException ("Unable to read baclava from " + name, ex);
         }
         for (String key:outputPorts.keySet()){
             //ystem.out.print (key + ": ");
             Object value;
             try {
                 value = baclava.getValue(key);
             } catch (TavernaException ex) {
                 throw new WireItRunException ("Unable to read value " + key + " from baclava form " + name, ex);
             }
             //ystem.out.println(value);
             outputPorts.get(key).fireOutputReady(value, outputBuilder);
         }
         URI uri = resolver.FileAndParentToURI(OUTPUT_DIR, output);
         baclavaOutput.fireOutputReady(uri, outputBuilder);
    }
     
     /**
      * This class receives input values and passes then to the correct method of the relative Taverna Input.
      * <p>
      * The Type of input Depth 0 vs 1, value vs URI is based on the Type of the Object received.
      * <p>
      * This is a prototype so it throws an Exception if an unexpected type is received.
      * This should be expanded as new types are uncovered. 
      */
     private class ValueListener implements OutputListener{
 
         /** Link to the same Input wrapper as the matching terminal is mapped to. */
         private TavernaInput myInput;
         
         /**
          * Creates a Listener associated with this input wrapper.
          * @param input input storage classes
          */
         private ValueListener(TavernaInput input){
             myInput = input;
         }
         
         @Override
         /**
          * Receives the Object on to any upstream modules.
          * <p>
          * The Type of input Depth 0 vs 1, value vs URI is based on the Type of the Object received.
          * Depending on the type different methods are called on the inputWrapper.
          * <p>
          * After receiving an input the module checks if it now has al required inputs and if so runs.
          * <p>
          * This is a prototype so it throws an Exception if an unexpected type is received.
          * This should be expanded as new types are uncovered. 
          * 
          * @param output Information being passed from one module to another.
          * @param outputBuilder Logging buffer. 
          * @throws WireItRunException Something has gone wrong. This could be caused by exectution 
          *    or even one of the downstream modules.
          */
         public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
              try {
                 if (output instanceof String){
                    System.out.println("Setting string");
                     myInput.setStringInput(output.toString());
                 } else if (output instanceof byte[]){
                     //This is a hack. 
                     //A nicer way would be to save to file and then pass as file.
                     byte[] array = (byte[])output;
                     String asString = new String(array);
                     myInput.setStringInput(asString);
                 } else if (output instanceof String[]){
                     //TavernaInputs will throw an exception is depth is not 1
                    System.out.println("Setting string array");
                     myInput.setStringsInput((String[])output);
                 } else if (output instanceof URI){
                    System.out.println("setting URI");
                     myInput.setSingleURIInput(resolver.getURIObjectToRelativeURIString(output));                    
                 } else if (output instanceof DelimiterURI){
                     //TavernaInputs will throw an exception is depth is not 1
                     DelimiterURI delimiterURI = (DelimiterURI)output;
                     myInput.setListURIInput(delimiterURI.getURI().toString(), delimiterURI.getDelimiter());
                } else {
                     //I could have done output.toString() but for now want to check every type is handled correctly.
                      throw new WireItRunException ("Unknown input type " + output.getClass() + " in " + name);
                 }
             } catch (TavernaException ex) {
                 throw new WireItRunException ("Error setting Taverna input for " + name + ": " + ex.getMessage(), ex);
             }
             runIfReady(outputBuilder);
         }
     }
 
     /**
      * Listener for Baclava Input.
      */
     private class BaclavaListener implements OutputListener{
        
         private BaclavaListener(){
         }
         
         //@Override
         /**
          * Receives a baclava file and causes the module to execute.
          * 
          * @param output URI to a Baclava file.
          * @param outputBuilder Logging buffer. 
          * @throws WireItRunException Something has gone wrong. This could be caused by exectution 
          *    or even one of the downstream modules.
          */
         public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException{
             if (output instanceof URI){
                 baclavaInput = resolver.getURIObjectToRelativeURIString(output);
                 runIfReady(outputBuilder);
             } else {
                  throw new WireItRunException ("Unknown inpiut type " + output.getClass() + " in " + name);
             }
         }
     }
 
 }
