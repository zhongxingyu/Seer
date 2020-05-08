 /**
  * 
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.IProblemRequestor;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.parser.IProblem;
 import org.rubypeople.rdt.internal.core.parser.Error;
 import org.rubypeople.rdt.internal.core.parser.RdtWarnings;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.parser.TaskParser;
 
 /**
  * @author Chris
  * 
  */
 public class RubyScriptProblemFinder {
 
 	public static void process(RubyScript script, char[] charContents, IProblemRequestor problemRequestor, IProgressMonitor pm) {
 		RdtWarnings warnings = new RdtWarnings();
 		RubyParser parser = new RubyParser(warnings);
 		String contents = new String(charContents);
 		try {
 			parser.parse(script.getElementName(), new StringReader(contents));
 		} catch (SyntaxException e) {
			problemRequestor.acceptProblem(new Error(e.getPosition(), e.getMessage()));
 		}
 
 		IEclipsePreferences preferences = RubyCore.getInstancePreferences();
 		TaskParser taskParser = new TaskParser(preferences);
 		taskParser.parse(contents);
 
 		List problems = new ArrayList();
 		problems.addAll(warnings.getWarnings());
 		problems.addAll(taskParser.getTasks());
 		for (Iterator iter = problems.iterator(); iter.hasNext();) {
 			IProblem problem = (IProblem) iter.next();
 			problemRequestor.acceptProblem(problem);
 		}
 	}
 
 }
