 package uk.ac.diamond.scisoft.icatexplorer.rcp.propertiesTesters;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.icatexplorer.rcp.natures.DiscICATProjectNature;
 
 public class ICATDisconnectedPropertyTester extends PropertyTester {
 	
 	static final String DISC_ICAT_NATURE = DiscICATProjectNature.NATURE_ID;
 	
 	private static final Logger logger = LoggerFactory.getLogger(ICATDisconnectedPropertyTester.class);
 
 	public ICATDisconnectedPropertyTester() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public boolean test(Object receiver, String property, Object[] args,
 			Object expectedValue) {
 		/*
 		 * test whether selected project is a disconnected icat project
 		 */
 		// TODO Rita receiver can also not be IProject sometimes.	
		return isDisconnectedICATProject(((IProject) receiver));
 
 	}
 
 	private boolean isDisconnectedICATProject(IProject iproject) {
 		
 		try {
			return iproject.getDescription().hasNature(DISC_ICAT_NATURE);
 		} catch (CoreException e) {
 			logger.error("problem getting project nature: ", e);
 		}
 		
 		return false;
 				
 	}	
 	
 
 }
