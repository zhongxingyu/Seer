 package com.github.rabid_fish.example8;
 
 import https.github_com.rabid_fish.ContactType;
 import https.github_com.rabid_fish.ObjectFactory;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.drools.KnowledgeBase;
 import org.drools.runtime.StatelessKnowledgeSession;
 
 import com.github.rabid_fish.ExampleParent;
 import com.github.rabid_fish.Number;
 import com.github.rabid_fish.example7.JaxbObjectsIntoRulesExample;
 
 public class DifferentObjectsIntoRulesExample extends ExampleParent {
 
 	public void executeRules(KnowledgeBase kbase) {
 
 		System.out.println("Instantiating jaxb objects and passing them into some rules");
 
 		System.out.println(new Date());
 		StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
 		System.out.println(new Date());
 
 		ObjectFactory objectFactory = new ObjectFactory();
 		ContactType contact = JaxbObjectsIntoRulesExample.createContact(
				objectFactory, "John Deer", "16", "male");
 
 		Number number = new Number("3");
 
 		List<Object> list = new ArrayList<Object>();
 		list.add(contact);
 		list.add(number);
 
 		ksession.execute(list);
 
 		 System.out.println(contact.getName() + " is in risk group '" + contact.getRisk() + "'");
 	}
 
 }
