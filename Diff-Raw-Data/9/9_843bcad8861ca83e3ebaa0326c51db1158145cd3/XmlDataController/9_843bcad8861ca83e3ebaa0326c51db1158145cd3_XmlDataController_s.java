 package org.hackystat.sensor.xmldata;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.hackystat.sensor.xmldata.option.OptionFactory;
 import org.hackystat.sensor.xmldata.option.OptionHandler;
 import org.hackystat.sensorshell.SensorProperties;
 
 /**
  * The class which parses the command-line arguments specified by the user,
  * validates the created options and their parameters, and executes the options.
  * @author Austen Ito
  * 
  */
 public class XmlDataController {
   /** True if the verbose mode is on, false if verbose mode is turned off. */
   private boolean isVerbose = false;
   /** The sensor data type name used by all data sent to the sensorbase. */
   private String sdtName = "";
   /** The class that manages the options created from the user's arguments. */
   private OptionHandler optionHandler = null;
   /** The class which encapsulates the firing of messages. */
   private MessageDelegate messageDelegate = null;
   /** The list of command-line arguments. */
   private List<String> arguments = new ArrayList<String>();
   /** True if all command-line arguments have been parsed correctly. */
   private boolean hasParsed = true;
 
   /**
    * Constructs this controller with the classes that help manage the
    * command-line arguments and message capabilities.
    */
   public XmlDataController() {
     this.optionHandler = new OptionHandler(this);
     this.messageDelegate = new MessageDelegate(this);
   }
 
   /**
    * This method parses the specified command-line arguments and creates
    * options, which can be validated and executed.
    */
   private void processArguments() {
     Map<String, List<String>> optionToArgs = new HashMap<String, List<String>>();
     String currentOption = "";
 
     // Iterate through all -option <arguments>... pairings to build a mapping of
     // option -> arguments.
     for (String argument : this.arguments) {
       // If the current string is an option flag, create a new mapping.
       if (this.optionHandler.isOption(argument)) {
         if (optionToArgs.containsKey(argument)) {
          String msg = "The option, " + argument + ", may not have duplicates.";
          this.fireMessage(msg);
           this.hasParsed = false;
           break;
         }
         optionToArgs.put(argument, new ArrayList<String>());
         currentOption = argument;
       }
       // Else, add the argument to the option flag list.
       else {
         List<String> args = optionToArgs.get(currentOption);
         args.add(argument);
         optionToArgs.put(currentOption, args);
       }
     }
 
     // Next, let's create the options using the command-line information.
     for (Entry<String, List<String>> entry : optionToArgs.entrySet()) {
       String optionName = entry.getKey();
       List<String> optionParams = entry.getValue();
       this.optionHandler.addOption(OptionFactory.getInstance(this, optionName, optionParams));
     }
 
     // Finally, process the options, which may instance variables.
     this.optionHandler.processOptions();
   }
 
   /**
    * Processes the command-line arguments and creates the objects that can be
    * executed.
    * @param arguments the specified list of command-line arguments.
    */
   public void processArguments(List<String> arguments) {
     this.arguments = arguments;
     this.optionHandler.clearOptions();
     this.processArguments();
   }
 
   /** Executes all of the options specified by the user */
   public void execute() {
     if (this.hasParsed && this.optionHandler.isOptionsValid()
         && this.optionHandler.hasRequiredOptions()) {
       this.optionHandler.execute();
     }
   }
 
   /**
    * Displays the specified message. The same message is displayed even if the
    * verbose option is enabled.
    * @param message the specified message to display.
    */
   public void fireMessage(String message) {
     this.messageDelegate.fireMessage(message);
   }
 
   /**
    * Displays the specified message if verbose mode is enabled.
    * @param message the specified message to display.
    */
   public void fireVerboseMessage(String message) {
     this.messageDelegate.fireVerboseMessage(message);
   }
 
   /**
    * Displays the specified message is verbose mode is disabled or the verbose
    * message if verbose mode is enabled.
    * @param message the specified message.
    * @param verboseMessage the specified verbose message.
    */
   public void fireMessage(String message, String verboseMessage) {
     this.messageDelegate.fireMessage(message, verboseMessage);
   }
 
   /**
    * Sets the sdt name specified by the user. If this sdt string is set, all
    * entries processed by this controller without an sdt attribute will use the
    * specified sdt string.
    * @param sdtName the specified sdt name.
    */
   public void setSdtName(String sdtName) {
     this.sdtName = sdtName;
   }
 
   /**
    * Returns the sensor data type name that is associated with all data sent via
    * sensorshell to the sensorbase.
    * @return the sensor data type name.
    */
   public String getSdtName() {
     return this.sdtName;
   }
 
   /**
    * Returns true if verbose mode is enabled, false if not.
    * @return true if verbose mode is on, false if not.
    */
   public boolean isVerbose() {
     return this.isVerbose;
   }
 
   /**
    * Enables the verbosity of this controller if true, disables if false.
    * @param isVerbose true to enable verbose mode, false to disable.
    */
   public void setVerbose(boolean isVerbose) {
     this.isVerbose = isVerbose;
   }
 
   /**
    * Returns the hackystat host stored in the sensor properties file.
    * @return the hackystat host string.
    */
   public String getHost() {
     SensorProperties properties = new SensorProperties();
     return properties.getHackystatHost();
   }
 }
