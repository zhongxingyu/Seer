 package br.com.anteater.main;
 
 import br.com.anteater.script.BuildScript;
 import br.com.anteater.script.InteractiveScript;
 import br.com.anteater.script.ScriptBase;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ScriptBase script = args.length > 0 ? new BuildScript() : new InteractiveScript();
		int exitCode = script.execute(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
 	}
 }
