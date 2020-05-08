 package yeti;
 
 import java.util.HashMap;
 
 /**
  * Class that represents a unit of testing. Typically for Java this would be a class or a package, for C a header file.
  * A module contains a list of routines to test. The strategy will iterate through them.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  */
 public class YetiModule {
 	
 	/**
 	 * The name of the module
 	 */
 	protected String moduleName;
 
 	/**
 	 * The modules from which it was combined.
 	 */
 	private YetiModule []combiningModules = null;
 	
 	/**
 	 * Gets the modules from which it was combined.
 	 * 
 	 * @return the array containing all modules, null if not composed.
 	 */
 	public YetiModule[] getCombiningModules() {
 		return combiningModules;
 	}
 
 	/**
 	 * Sets the modules that were combined to obtain this one. 
 	 * 
 	 * @param combiningModules the array of combined modules.
 	 */
 	public void setCombiningModules(YetiModule[] combiningModules) {
 		this.combiningModules = combiningModules;
 	}
 
 	/**
 	 * A HashMap of all existing modules.
 	 */
 	public static HashMap <String, YetiModule> allModules =new HashMap<String,YetiModule>();
 
 	/**
 	 * A HashMap of all routines in this module.
 	 */
 	public HashMap <String, YetiRoutine> routinesInModule =new HashMap<String,YetiRoutine>();
 	
 	/**
 	 * A simple Constructor.
 	 * 
 	 * @param moduleName takes the name of the module as a parameter.
 	 */
 	public YetiModule(String moduleName) {
 		super();
 		this.moduleName = moduleName;
 	}
 
 	/**
 	 * Add a routine to the list of routine in module.
 	 * 
 	 * @param routine the routine to add.
 	 */
 	public void addRoutineInModule(YetiRoutine routine){
 		routinesInModule.put(routine.name.toString(),routine);
 	}
 	
 	/**
 	 * Return a routine from this module with a given name.
 	 * 
 	 * @param name the name of the routine asked
 	 * @return the routine selected
 	 */
 	public YetiRoutine getRoutineFromModuleWithName(String name){
 		return routinesInModule.get(name);
 	}
 
 	
 	/**
 	 * Adds a module to the general list of modules.
 	 * 
 	 * @param module the module to add.
 	 */
 	public static void addModuleToAllModules(YetiModule module){
 		allModules.put(module.getModuleName(),module);
 	}
 	
 	/**
 	 * Get a routine at random.
 	 * 
 	 * @return the routine selected.
 	 */
 	public YetiRoutine getRoutineAtRandom(){
 		double d=Math.random();
 		int i=(int) Math.floor(d*routinesInModule.size());
 		
 		return (YetiRoutine)(routinesInModule.values().toArray()[i]);
 	}
 	
 	
 	/**
 	 * Getter for the module name.
 	 * 
 	 * @return the module name.
 	 */
 	public String getModuleName(){
 		return moduleName;
 	}
 
 	/**
 	 * Setter for the module name.
 	 * 
 	 * @param name the name to set.
 	 */
 	public void setModuleName(String name){
 		moduleName=name;
 	}
 	
 	/**
 	 * Method used to to combine two modules into one.
 	 * 
 	 * @param modules the array of modules to combine.
 	 * @return a module that combined all modules.
 	 */
 	public static YetiModule combineModules(YetiModule []modules) {
 		YetiModule result = new YetiModule(YetiName.getFreshNameFrom("__yeti_test_module").value);
 		result.setCombiningModules(modules);
 		for (YetiModule mod0: modules) {
 			for (YetiRoutine rout0: mod0.routinesInModule.values())
 				result.addRoutineInModule(rout0);
 		}
 		YetiModule.addModuleToAllModules(result);
 		return result;
 	}
 	
 	/**
 	 * Checks that the trace contains refers to the module(s) in its trace.
 	 * 
 	 * @param trace the trace to check.
 	 * @return true if the throwable is relevant.
 	 */
 	public boolean isThrowableInModule(String trace) {
 		YetiModule []combModules = this.combiningModules;
 		if (combModules!=null) {
 			for (YetiModule mod: combModules) {
				YetiLog.printDebugLog(mod.getModuleName(), this);
 				if (mod.isThrowableInModule(trace)) return true;
 			}
 		}
		return false;
 	}
 }
