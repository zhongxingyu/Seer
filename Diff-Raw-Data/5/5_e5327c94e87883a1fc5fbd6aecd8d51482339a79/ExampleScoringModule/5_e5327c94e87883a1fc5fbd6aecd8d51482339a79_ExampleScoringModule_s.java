 package com.ijg.darklightnova.modules;
 
 import com.ijg.darklightnova.engine.AssessmentModule;
 import com.ijg.darklightnova.engine.Module;
 import com.ijg.darklightnova.engine.Vulnerability;
 
 public class ExampleScoringModule extends Module {
 	/*
 	 * This is an example scoring module
 	 */
 	
 	// static vulnerability declarations
 	static Vulnerability exampleVulnerability = new Vulnerability("Example Vulnerability", "This is an example vulnerability for the core IJGSec engine");
 	
 	public ExampleScoringModule(AssessmentModule assessModule) {
 		/*
 		 * the numVulns is assigned in the super constructor
 		 * call to allow for telling the AssessmentModule
 		 * how many vulns this module has without manually
		 * doing it with a seperate call to the AssessmentModule
 		 */
 		super(assessModule, numVulns = 2);
 	}
 	
 	private boolean fixedExampleVulnerability() {
 		/*
 		 * Private methods are used to check whether
 		 * or not the vulnerability has been found, usually
 		 * they are booleans, though sometimes other
 		 * return types are warranted.
 		 */
 		return false;
 	}
 	
 	public void fixed() {
 		/*
 		 * The fixed() method, inherited from the superclass,
 		 * should be overridden in every scoring module class.
 		 * This method should:
 		 * 1. Check what vulnerabilities are found, if they are
 		 *    found set their found boolean to true, if not set
 		 *    it to false
		 * 2. Clear the inherited vulnerablities array list
 		 * 3. Add all vulnerabilities contained in the module
 		 *    to the vulnerabilities array list
 		 */
 		
 		if (fixedExampleVulnerability()) exampleVulnerability.found = true;
 		else exampleVulnerability.found = false;
 		
 		vulnerabilities.clear();
 		vulnerabilities.add(exampleVulnerability);
 	}
 }
