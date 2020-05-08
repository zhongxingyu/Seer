 package org.cotrix.application.changelog;
 
 import static java.lang.Math.*;
 import static java.lang.System.*;
 import static java.util.Collections.*;
 import static org.cotrix.common.CommonUtils.*;
 import static org.cotrix.domain.attributes.CommonDefinition.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.managed.ManagedCode.*;
 import static org.cotrix.domain.managed.ManagedCodelist.*;
 import static org.cotrix.domain.utils.DomainUtils.*;
 import static org.cotrix.repository.CodelistQueries.*;
 
 import java.lang.reflect.Type;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.cotrix.common.async.CancelledTaskException;
 import org.cotrix.common.async.TaskContext;
 import org.cotrix.common.async.TaskUpdate;
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.common.NamedContainer;
 import org.cotrix.domain.common.NamedStateContainer;
 import org.cotrix.domain.managed.ManagedCode;
 import org.cotrix.domain.managed.ManagedCodelist;
 import org.cotrix.domain.trait.Status;
 import org.cotrix.repository.CodelistRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.reflect.TypeToken;
 
 @Singleton
 public class ChangelogManager {
 
 	private static final Logger log = LoggerFactory.getLogger(ChangelogManager.class);
 
 	private static Type logtype = new TypeToken<List<ChangelogEntry>>() {
 	}.getType();
 
 	@Inject
 	private CodelistRepository codelists;
 
 	public void trackAfter(Codelist changeset) {
 	
 		//we know this will succeed, an update has already taken place 
 		Codelist list = codelists.lookup(changeset.id());
 		
 		//changelog requires lineage
 		if (manage(list).hasno(PREVIOUS_VERSION)) 		
 			return;
 		
 		Codelist.Private plist = reveal(list);
 		Codelist.Private pchangeset = reveal(changeset);
 		
 		trackPunctual(plist, pchangeset);
 	}
 	
 	public void track(Codelist list) {
 	
 		trackBulk(reveal(list));
 	}
 	
 	private void trackBulk(Codelist.Private list) {
 		
 		//this seems coarse: any bulk change triggers a full traversal
 		//in practice we do expect shared defs to occur in most-to-all codes.
 		//even if the impacted codes were a few, how much overhead would we be adding to the 
 		//traversal required underneatah to find the impact right subset of codes? 
 		//would need to measure. surely, fine-grain requires more work and we should not
 		
 		TaskContext context = new TaskContext();
 		float progress=0f;
 		
 		NamedContainer<? extends Code.Private> codes =  reveal(list).codes();
 		
 		int total = list.codes().size();
 		
 		//arbitrary
 		long step = round(max(10,floor(codes.size()/10)));
 		
 		int i=0;
 		
 		long time = currentTimeMillis();
 		
 		
 		ManagedCodelist mlist = ManagedCodelist.manage(list);
 		
 		Date listCreated = mlist.created();
 		
 		System.out.println(listCreated);
 		
 		for (Code.Private code : codes) {
 		
 			i++;
 			progress++;
 			
 			ManagedCode mcode = manage(code);
 
 			System.out.println(mcode.lastUpdated());
 			
 			//process only if it has changed since the list was created
			if (mcode.lastUpdated().after(listCreated)) {
 			
 				handleModifiedMarkerWith(code);
 			}
 			
 			if (i%step==0)
 				
 				if (context.isCancelled()) {
 					log.info("cahngelog tracking aborted on user request after {} codes.",i);
 					throw new CancelledTaskException("changelog tracking aborted on user request");
 				}
 			
 				else {
 				
 					context.save(TaskUpdate.update(progress/total, "tracked "+i+" codes"));
 				}
 		}
 		
 
 		log.trace("tracked changelog for {} in {} msec.",signatureOf(list),currentTimeMillis()-time);
 	}
 
 	private void trackPunctual(Codelist.Private list, Codelist.Private changeset) {
 
 		for (Code.Private change : changeset.codes()) {
 
 			Status status = reveal(change).status();
 
 			// changelog is pointless if the code is to be removed anyway
 			if (status == Status.DELETED)
 				continue;
 
 			Code.Private code = list.codes().lookup(change.id());
 
 			trackCode(code,change);
 		}
 		
 	}
 
 	private void trackCode(Code.Private changed, Code.Private change) {
 
 		if (change.status() == null)
 
 			aaddNewMarkerTo(changed.state().attributes());
 
 		else
 
 			handleModifiedMarkerWith(changed);
 
 	}
 
 	private void aaddNewMarkerTo(NamedStateContainer<Attribute.State> attributes) {
 
 		attributes.add(stateof(attribute().instanceOf(NEW).value("TRUE")));
 	}
 
 	private void handleModifiedMarkerWith(Code.Private changed) {
 
 		NamedStateContainer<Attribute.State> attributes = changed.state().attributes();
 
 		ManagedCode managed = manage(changed);
 
 		Attribute modified = managed.attribute(MODIFIED);
 
 		String originId = managed.originId();
 
 		// by now we know codelist has lineage, but this update may be for a new
 		// code
 		if (notmarked(originId))
 			return;
 
 		// fetch the past
 		Code origin = codelists.get(code(originId));
 
 		if (notmarked(origin)) {
 			log.error("cannot compute changelog for code {} as its lineage {} can't be retrieved.", changed.id(), originId);
 			return;
 		}
 
 		ChangelogProducer producer = new ChangelogProducer();
 
 		Changelog log = producer.changesBetween(origin, changed);
 
 		if (log.isEmpty()) {
 
 			if (marked(modified))
 
 				attributes.remove(modified.id());
 
 			return;
 
 		}
 
 		if (marked(modified)) {
 
 			String oldlog = modified.value();
 
 			List<ChangelogEntry> oldentries = jsonBinder().fromJson(oldlog, logtype);
 
 			log.addAll(oldentries);
 
 		} else {
 
 			attributes.add(stateof(attribute().instanceOf(MODIFIED)));
 			modified = managed.attribute(MODIFIED); // remember persistence
 
 		}
 
 		List<ChangelogEntry> entries = log.entries();
 
 		sort(entries);
 
 		stateof(modified).value(jsonBinder().toJson(entries, logtype));
 
 	}
 
 	private boolean marked(Object o) {
 		return o != null;
 	}
 
 	private boolean notmarked(Object o) {
 		return o == null;
 	}
 }
