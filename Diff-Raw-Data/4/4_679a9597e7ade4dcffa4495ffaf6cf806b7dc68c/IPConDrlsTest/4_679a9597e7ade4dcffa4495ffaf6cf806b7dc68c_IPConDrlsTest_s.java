 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.ObjectFilter;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
 import uk.ac.imperial.presage2.core.network.NetworkAddress;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * @author dave
  *
  */
 public class IPConDrlsTest {
 	
 	final private Logger logger = Logger.getLogger(IPConDrlsTest.class);
 
 	Injector injector;
 	RuleStorage rules;
 	StatefulKnowledgeSession session;
 
 	@Before
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(new RuleModule()
 				.addClasspathDrlFile("IPCon_Institutional_Facts.drl")
 				.addClasspathDrlFile("IPConPowPer.drl")
 				.addClasspathDrlFile("IPConOblSan.drl")
				.addClasspathDrlFile("IPConUtils.drl")
				
 				.addClasspathDrlFile("IPCon.drl"));
 		rules = injector.getInstance(RuleStorage.class);
 		session = injector.getInstance(StatefulKnowledgeSession.class);
 		session.setGlobal("logger", this.logger);
 		session.setGlobal("session", session);
 		session.setGlobal("storage", null);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		session.dispose();
 	}
 	
 	@Test
 	public void basicTest() throws Exception {
 		NetworkAddress agent = new NetworkAddress(Random.randomUUID());
 		Integer revision = 0;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		session.insert(new ArrogateLeadership(agent, revision, issue, cluster));
 		rules.incrementTime();
 		
 		Collection<Object> objects = session.getObjects();
 		logger.info("Objects: " + objects.toString());
 	}
 
 }
