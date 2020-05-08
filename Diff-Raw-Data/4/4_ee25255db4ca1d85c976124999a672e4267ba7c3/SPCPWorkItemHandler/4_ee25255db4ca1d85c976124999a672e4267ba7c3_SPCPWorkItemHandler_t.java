 package com.redhat.brmsworkshop;
 
 import java.util.Collection;
 import java.util.Random;
 
 import org.drools.ClassObjectFilter;
 import org.drools.definition.type.FactType;
 import org.drools.process.instance.WorkItemHandler;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.WorkItem;
 import org.drools.runtime.process.WorkItemManager;
 
 public class SPCPWorkItemHandler implements WorkItemHandler{
 
 	private StatefulKnowledgeSession ksession;
 	
 	public SPCPWorkItemHandler(StatefulKnowledgeSession ksession) {
 		this.ksession = ksession;
 	}
 	
 	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
 		System.out.println("Executing work item");
 		
 		int randomScore = new Random().nextInt(100);
 		System.out.println("SCPC Score: " + randomScore);
 
 		FactType factType = ksession.getKnowledgeBase().getFactType("cleartech", "Customer");
		Collection<Object> factHandles = ksession.getObjects(new ClassObjectFilter(factType.getFactClass()));
 		Object fact = factHandles.toArray()[0];
 		factType.set(fact, "scpcScore", randomScore);
 		
 		workItemManager.completeWorkItem(workItem.getId(), null);
 	}
 
 }
