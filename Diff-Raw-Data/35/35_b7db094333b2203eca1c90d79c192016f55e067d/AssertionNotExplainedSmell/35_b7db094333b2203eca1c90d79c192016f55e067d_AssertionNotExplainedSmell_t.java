 package org.ita.neutrino.testsmells.smells;
 
 import java.util.Collection;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.ISelection;
import org.ita.neutrino.abstracttestparser.AbstractTestParser;
 import org.ita.neutrino.abstracttestparser.Assertion;
 import org.ita.neutrino.abstracttestparser.TestMethod;
import org.ita.neutrino.addexplanation.AddExplanationAction;
 import org.ita.neutrino.eclipseaction.ActionException;
 import org.ita.neutrino.testsmells.core.EclipseQuickFix;
 import org.ita.neutrino.testsmells.core.MarkerManager;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Collections2;
import com.google.inject.Inject;
 
 public class AssertionNotExplainedSmell extends MethodTestCodeSmell {
 
	private final AssertionNotExplainedQuickFix assertionNotExplainedQuickFix;
	
	@Inject
	public AssertionNotExplainedSmell(
			AssertionNotExplainedQuickFix assertionNotExplainedQuickFix) {
		this.assertionNotExplainedQuickFix = assertionNotExplainedQuickFix;
	}
	
 	@Override
 	public void checkForPresence(TestMethod method, MarkerManager markerSink) throws JavaModelException, CoreException {
 		@SuppressWarnings("unchecked")
 		Collection<Assertion> allAssertions = (Collection<Assertion>)
 			Collections2.filter(method.getStatements(), isAssertionFilter);
 		
 		if (allAssertions.size() > 1) {
 			for (Assertion assertion: allAssertions) {
 				if (Strings.isNullOrEmpty(assertion.getExplanation())) {
 					markerSink.addMarker(assertion.getCodeElement(),
 						"Assertion is missing explanation", this.getClass());
 				}
 			}
 		}
 	}
 
 	@Override
 	public EclipseQuickFix[] getQuickFixes() {
		return new EclipseQuickFix[] { assertionNotExplainedQuickFix };
 	}
 	
 	public static class AssertionNotExplainedQuickFix implements EclipseQuickFix {
 		
		private final AbstractTestParser testParser;
		
		@Inject
		public AssertionNotExplainedQuickFix(AbstractTestParser testParser) {
			this.testParser = testParser;
		}
		
 		@Override
 		public String title() {
 			return "Add explanation";
 		}
 
 		@Override
 		public void run(ISelection selection) throws ActionException {	
			AddExplanationAction action = new AddExplanationAction() {
				
				@Override
				protected AbstractTestParser instantiateParser() {
					return testParser;
				}
			};			
 			action.setSelection(selection);
 			action.run();
 		}
 
 		@Override
 		public String description() {
 			return "Opens the refactoring assistant to add an explanation to this assertion";
 		}
 	}
 }
