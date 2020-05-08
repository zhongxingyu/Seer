 package services;
 
 import java.io.StringReader;
 import java.util.Collection;
 
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.compiler.DroolsParserException;
 import org.drools.definition.KnowledgePackage;
 import org.drools.io.Resource;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.event.EventBus;
 
 import com.google.inject.Inject;
 
 public class NomicService extends EnvironmentService {
 
 	StatefulKnowledgeSession session;
 	
 	@Inject
 	public NomicService(EnvironmentSharedStateAccess sharedState,
 			StatefulKnowledgeSession session, EventBus e) {
 		super(sharedState);
 		
 		this.session = session;
 		e.subscribe(this);
 	}
 	
 	public void addRule(Collection<String> imports, String ruleName,
 			Collection<String> conditions, Collection<String> actions)
 					throws DroolsParserException {
 		String rule = "";
 		
 		for(String importe : imports) {
 			rule += "import " + importe + " ";
 		}
 		
 		rule += "rule \"" + ruleName + "\" ";
 		
 		rule += "when ";
 		
 		for (String condition : conditions) {
 			rule += condition + " ";
 		}
 		
 		rule += "then ";
 		
 		for (String action : actions) {
 			rule += action + " ";
 		}
 		
 		rule += "end";
 		
 		addRule(rule);
 	}
 	
 	public void addRule(String rule) throws DroolsParserException {
 		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 		
 		Resource myResource = ResourceFactory.newReaderResource(new StringReader(rule));
 		kbuilder.add(myResource, ResourceType.DRL);
 		
 		if (kbuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule.\n"
					+ kbuilder.getErrors().toString());
 		}
 		
 		Collection<KnowledgePackage> packages = kbuilder.getKnowledgePackages();
 		
 		session.getKnowledgeBase().addKnowledgePackages(packages);
 	}
 }
