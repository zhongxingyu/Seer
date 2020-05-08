 package cz.cvut.fit.mi_mpr_dip.admission.jbpm;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 
 public interface ProcessService {
 
 	public StatefulKnowledgeSession getSession();
 		
 	public void setSession(StatefulKnowledgeSession ksession);
 }
