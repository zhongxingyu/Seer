 package mock;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.drools.KnowledgeBase;
 import org.drools.compiler.DroolsParserException;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.FactHandle;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
 import actions.ProposeRuleAddition;
 import actions.ProposeRuleChange;
 import actions.ProposeRuleModification;
 import actions.ProposeRuleRemoval;
 import agents.NomicAgent;
 import exceptions.InvalidRuleProposalException;
 import exceptions.NoExistentRuleChangeException;
 import facts.Turn;
 
 @RunWith(JMock.class)
 public class NomicServiceMockTest extends TestCase {
 	Mockery context = new JUnit4Mockery();
 	
 	final EnvironmentSharedStateAccess ss = context.mock(EnvironmentSharedStateAccess.class);
 	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
 	final EventBus e = context.mock(EventBus.class);
 	final KnowledgeBase base = context.mock(KnowledgeBase.class);
 	
 	final String correctRule = "import agents.NomicAgent "
 			+ "rule \"Dynamic rule!\""
 			+ "when"
 			+ "	$agent : NomicAgent(SequentialID == 1)"
 			+ "then"
 			+ "	System.out.println(\"Found agent 1!\");"
 			+ "end";
 	
 	@Test
 	public void SingleStringRuleAdditionTest() {
 		String newRule = correctRule;
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		final NomicService service = new NomicService(ss, session, e);
 		
 		try {
 			service.addRule(newRule);
 		} catch (DroolsParserException e1) {
 			fail("Rule was not parsed correctly.");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void MultipleStringRuleAdditionTest() {
 		
 		context.checking(new Expectations() {{
 			exactly(1).of(e).subscribe(with(any(NomicService.class)));
 			exactly(3).of(session).getKnowledgeBase(); will(returnValue(base));
 			exactly(3).of(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		final NomicService service = new NomicService(ss, session, e);
 		
 		ArrayList<String> imports = new ArrayList<String>();
 		imports.add("agents.NomicAgent");
 		
 		String ruleName = "Dynamic Rule!";
 		
 		ArrayList<String> conditions = new ArrayList<String>();
 		conditions.add("$agent : NomicAgent(SequentialID == 1)");
 		
 		ArrayList<String> actions = new ArrayList<String>();
 		actions.add("System.out.println(\"Found agent 1!\");");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e1) {
 			fail("Simple rule was not parsed directly." + e1.getMessage());
 		}
 		
 		conditions.add("$agent2 : NomicAgent(SequentialID == 2)");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e2) {
 			fail("Multiple condition failure.\n" + e2.getMessage());
 		}
 		
 		actions.add("System.out.println(\"Testing multiple actions\");");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e2) {
 			fail("Multiple actions failure.\n" + e2.getMessage());
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void RemoveRuleTest() {
 		
 		final String packageName = "testPackage";
 		final String ruleName = "testRule";
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).removeRule(packageName, ruleName);
 		}});
 		
 		NomicService service = new NomicService(ss, session, e);
 		service.RemoveRule(packageName, ruleName);
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void ApplyProposedRuleTest() {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		final String newRule = correctRule;
 		
 		ProposeRuleAddition addition = new ProposeRuleAddition(mockAgent, newRule);
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		NomicService service = new NomicService(ss, session, e);
 		service.ApplyRuleChange(addition);
 		
 		context.assertIsSatisfied();
 		
 		final String oldRuleName = "Old Rule Name";
 		final String oldRulePackage = "Old Rule Package";
 		ProposeRuleRemoval removal = new ProposeRuleRemoval(mockAgent, oldRuleName, oldRulePackage);
 		
 		context.checking(new Expectations() {{
 			oneOf(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).removeRule(oldRulePackage, oldRuleName);
 		}});
 		
 		service.ApplyRuleChange(removal);
 		
 		context.assertIsSatisfied();
 		
 		ProposeRuleModification modification = new ProposeRuleModification(mockAgent, newRule, oldRuleName, oldRulePackage);
 		
 		context.checking(new Expectations() {{
 			exactly(2).of(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).removeRule(oldRulePackage, oldRuleName);
 			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		service.ApplyRuleChange(modification);
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void ProposeRuleChangeTest() {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final ProposeRuleChange ruleChange = context.mock(ProposeRuleChange.class);
 		final EndOfTimeCycle cycle = context.mock(EndOfTimeCycle.class);
 		final FactHandle mockHandle = context.mock(FactHandle.class);
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getFactHandle(with(any(Turn.class)));
 			will(returnValue(mockHandle));
			oneOf(session).update(mockHandle,with(any(Turn.class)));
 		}});
 		
 		NomicService service = new NomicService(ss, session, e);
 		
 		try {
 			service.ProposeRuleChange(ruleChange);
 			fail("Allowed rule change proposal when it wasn't proposition stage of turn");
 		} catch (InvalidRuleProposalException e) {
 			
 		}
 		
 		service.onIncrementTime(cycle);
 		
 		try {
 			service.ProposeRuleChange(ruleChange);
 		} catch (InvalidRuleProposalException e) {
 			fail("Refused rule proposal during valid turn stage");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void GetProposedRuleChangeTest() {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final ProposeRuleChange ruleChange = context.mock(ProposeRuleChange.class);
 		final EndOfTimeCycle cycle = context.mock(EndOfTimeCycle.class);
 		final FactHandle mockHandle = context.mock(FactHandle.class);
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getFactHandle(with(any(Turn.class)));
 			will(returnValue(mockHandle));
			oneOf(session).update(mockHandle,with(any(Turn.class)));
 			
 		}});
 		
 		NomicService service = new NomicService(ss, session, e);
 		
 		try {
 			service.getCurrentRuleChange();
 			fail("Returned when there was no valid rule change");
 		} catch (NoExistentRuleChangeException e) {
 			
 		}
 		
 		service.onIncrementTime(cycle);
 		
 		try {
 			service.ProposeRuleChange(ruleChange);
 		} catch (InvalidRuleProposalException e) {
 			fail("Refused rule proposal during valid turn stage");
 		}
 		
 		try {
 			assertTrue(service.getCurrentRuleChange() == ruleChange);
 		} catch (NoExistentRuleChangeException e1) {
 			fail("Failed to return a rule change after it was set");
 		}
 		
 		context.assertIsSatisfied();
 	}
 }
