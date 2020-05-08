 package org.cotrix.neo.domain;
 
 import static java.lang.Math.*;
 import static org.cotrix.common.async.TaskUpdate.*;
 import static org.cotrix.neo.domain.Constants.NodeType.*;
 
 import org.cotrix.common.async.TaskContext;
 import org.cotrix.domain.attributes.Definition;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.codelist.CodelistLink;
 import org.cotrix.domain.common.NamedStateContainer;
 import org.cotrix.neo.NeoTransaction;
 import org.cotrix.neo.domain.Constants.Relations;
 import org.cotrix.neo.domain.utils.NeoContainer;
 import org.cotrix.neo.domain.utils.NeoStateFactory;
 import org.neo4j.graphdb.Node;
 
 public class NeoCodelist extends NeoVersioned implements Codelist.State {
 
 	//asfismaxBatchSizetchSizeced, fairly arbitrary. see it as an attempt to avoid OOM errors :)
 	final static int maxBatchSize = 15000;
 	
 	public static final NeoStateFactory<Codelist.State> factory = new NeoStateFactory<Codelist.State>() {
 		
 		@Override
 		public Codelist.State beanFrom(Node node) {
 			return new NeoCodelist(node);
 		}
 		
 		@Override
 		public Node nodeFrom(Codelist.State state) {
 			
 			return new NeoCodelist(state).node();
 		}
 	};
 	
 	public NeoCodelist(Node node) {
 		super(node);
 	}
 	
 	
 	public NeoCodelist(Codelist.State state) {
 		
 		super(CODELIST,state);	
 	
 		TaskContext context = new TaskContext();
 		
 		float progress=0f;
 		
		int total = state.codes().size()+state.definitions().size()+state.links().size()+state.attributes().size();
 		
 		for (Definition.State def : state.definitions())
 			node().createRelationshipTo(NeoDefinition.factory.nodeFrom(def),Relations.DEFINITION);
 		
 		progress = progress + state.definitions().size();
 		
 		context.save(update(progress/total, "added "+state.definitions().size()+" definitions"));
 		
 		for (CodelistLink.State l : state.links())
 			node().createRelationshipTo(NeoCodelistLink.factory.nodeFrom(l),Relations.LINK);
 		
 		progress = progress + state.links().size();
 		
 		context.save(update(progress/total, "added "+state.links().size()+" links"));
 		
 		int i = 0;
 	
 		int codes = state.codes().size();
 		
 		//arbitrary
 		long step = round(max(10,floor(codes/10)));
 		
 		for (Code.State c : state.codes()) {
 		
 			if (i==maxBatchSize) {
 				NeoTransaction.current().split();
 				i=0;
 			}
 			
 			if (i%step==0) {
 				
				progress = progress + i;
				
 				context.save(update(progress/total, "added "+i+" codes"));
 			}
 				
 			
 			node().createRelationshipTo(NeoCode.factory.nodeFrom(c),Relations.CODE);
 			
 			i++;
 		}
 		
 		context.save(update(1f, "added "+codes+" codes"));
 	}
 	
 	public Codelist.Private entity() {
 		return new Codelist.Private(this);
 	}
 	
 	@Override
 	public NamedStateContainer<Code.State> codes() {
 		return new NeoContainer<>(node(),Relations.CODE,NeoCode.factory);
 	}
 	
 	@Override
 	public NamedStateContainer<CodelistLink.State> links() {
 		return new NeoContainer<>(node(), Relations.LINK, NeoCodelistLink.factory);
 	}
 	
 	@Override
 	public NamedStateContainer<Definition.State> definitions() {
 		return new NeoContainer<>(node(), Relations.DEFINITION, NeoDefinition.factory);
 	}
 	
 }
