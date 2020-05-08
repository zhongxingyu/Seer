 package org.cotrix.repository.memory;
 
 import static org.cotrix.common.Utils.*;
 
 import java.util.UUID;
 
 import org.cotrix.domain.Attribute;
 import org.cotrix.domain.Codebag;
 import org.cotrix.domain.Codelist;
 import org.cotrix.repository.CodebagRepository;
 import org.cotrix.repository.CodelistRepository;
 
 /**
  * An in-memory {@link CodebagRepository}.
  * 
  * @author Fabio Simeoni
  *
  */
 public class MCodebagRepository extends MRepository<Codebag,Codebag.Private> implements CodebagRepository {
 
 	private final CodelistRepository listRepository;
 	
 	/**
 	 * Creates an instance over a private {@link MStore}.
 	 */
 	public MCodebagRepository() {
 		this(new MStore());
 	}
 	
 	/**
 	 * Creates an instance over a given {@link MStore}.
 	 * @param store
 	 */
 	public MCodebagRepository(MStore store) {
 		super(store,Codebag.class,Codebag.Private.class);
 		this.listRepository = new MCodelistRepository(store);
 	}
 	
 	@Override
 	public void add(Codebag bag) {
 
 		for (Attribute a: bag.attributes())
				reveal(a,Attribute.Private.class).setId(UUID.randomUUID().toString());
 			
 		//propagate addition
 		for (Codelist list : bag.lists())
 			listRepository.add(list);
 		
 		super.add(bag);
 	}
 }
