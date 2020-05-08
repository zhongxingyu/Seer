 package net.praqma.hudson.test.integration.sibling;
 
 
 import net.praqma.hudson.test.BaseTestClass;
 import net.praqma.util.test.junit.LoggingRule;
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 
 import hudson.model.AbstractBuild;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.util.debug.Logger;
 
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import java.util.logging.Level;
 
 public class BaselinesFound extends BaseTestClass {
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm", "setup-interproject.xml" );
 
 	private static Logger logger = Logger.getLogger();
 		
 	public AbstractBuild<?, ?> initiateBuild( String projectName, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
 		return jenkins.initiateBuild( projectName, "sibling", "_System@" + ccenv.getPVob(), "two_int@" + ccenv.getPVob(), recommend, tag, description, fail, true );
 	}
 
 	@Test
 	public void basicSibling() throws Exception {
 		
 		Stream one = ccenv.context.streams.get( "one_int" );
 		Stream two = ccenv.context.streams.get( "two_int" );
 		one.setDefaultTarget( two );
 		
 		/* The baseline that should be built */
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		
 		AbstractBuild<?, ?> build = initiateBuild( "no-options-" + ccenv.getUniqueName(), false, false, false, false );
 
 		/* Validate */
		SystemValidator validator = new SystemValidator( build ).
                validateBuild( build.getResult() ).
                validateBuiltBaseline( PromotionLevel.BUILT, baseline, false ).
                validateCreatedBaseline( true );
 		validator.validate();
 	}
 	
 	
 }
