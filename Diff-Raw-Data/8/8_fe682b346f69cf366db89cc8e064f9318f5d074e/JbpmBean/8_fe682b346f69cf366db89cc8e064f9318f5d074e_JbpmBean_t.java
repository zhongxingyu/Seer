 
 import org.drools.KnowledgeBase;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
 @Named
 @Stateful
 @RequestScoped
 public class JbpmBean { 
     public void startProcess() {
 		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 		
 		kbuilder.add(ResourceFactory.newClassPathResource("process.bpmn"), ResourceType.BPMN2);
 
 		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
 		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
 
 		ksession.startProcess("project.process");
 		ksession.dispose();
     }
 }
