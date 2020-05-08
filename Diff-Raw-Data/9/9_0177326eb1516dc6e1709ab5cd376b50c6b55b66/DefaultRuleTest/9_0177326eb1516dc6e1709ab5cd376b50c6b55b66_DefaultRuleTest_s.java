 package de.fernuni.pi3.interactionmanager.rules;
 
 import de.fernuni.pi3.interactionmanager.Event;
 import de.fernuni.pi3.interactionmanager.InstanceVars;
 import de.fernuni.pi3.interactionmanager.rules.DefaultRule;
 import de.fernuni.pi3.interactionmanager.rules.Rule;
 
 public class DefaultRuleTest extends AbstractRuleTest {
 
 	@Override
 	public void setUpTestData() {
 		Event inEvent = new Event();
 		inEvent.setAppType("MyApp");
 		inEvent.setAppInstanceId("MyApp1");
 		inEvent.setName("TestEvent");
 		
		Event expectedEvent = new Event();
 		expectedEvent.setAppType(inEvent.getAppType());
 		expectedEvent.setAppInstanceId(inEvent.getAppInstanceId());
 		expectedEvent.setName(inEvent.getName());
 		
 		InstanceVars inVars  = new InstanceVars();
 		InstanceVars inVars1 = new InstanceVars();
 		inVars1.put("countEvents", 3);
 		
 		InstanceVars expectedVars  = new InstanceVars();
 		InstanceVars expectedVars1 = new InstanceVars();
 		expectedVars.put("countEvents", 1);
 		expectedVars1.put("countEvents", 4);
 		
 		addTestData(inEvent, inVars, expectedEvent, expectedVars);
 		addTestData(inEvent, inVars1, expectedEvent, expectedVars1);
 	}
 
 	@Override
 	protected Rule getRule() {
 		return new DefaultRule();
 	}
 }
