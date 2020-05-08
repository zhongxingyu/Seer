 /**
  * 
  */
 package org.cotrix.web.publish.server.util;
 
 import static org.cotrix.repository.Queries.*;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.Codelist;
 import org.cotrix.io.CloudService;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.lifecycle.State;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.query.CodelistQuery;
 import org.cotrix.web.publish.server.publish.PublishStatus;
 import org.cotrix.web.publish.shared.Format;
 import org.cotrix.web.publish.shared.UIRepository;
 import org.cotrix.web.share.server.util.Codelists;
 import org.cotrix.web.share.server.util.FieldComparator.ValueProvider;
 import org.cotrix.web.share.server.util.Repositories;
 import org.cotrix.web.share.server.util.OrderedList;
 import org.cotrix.web.share.server.util.ValueUtils;
 import org.cotrix.web.share.shared.codelist.UICodelist;
 import org.cotrix.web.share.shared.codelist.UIQName;
 import org.slf4j.Logger;
 
 import org.virtualrepository.AssetType;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.csv.CsvCodelistType;
 import org.virtualrepository.csv.CsvGenericType;
 import org.virtualrepository.sdmx.SdmxCodelistType;
 import org.virtualrepository.sdmx.SdmxGenericType;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @SuppressWarnings("serial")
 @SessionScoped
 public class PublishSession implements Serializable {
 	
 	protected static final ValueProvider<UICodelist> CODELIST_NAME = new ValueProvider<UICodelist>() {
 
 		@Override
 		public String getValue(UICodelist item) {
 			return item.getName();
 		}
 	};
 	
 	protected static final ValueProvider<UICodelist> CODELIST_VERSION = new ValueProvider<UICodelist>() {
 
 		@Override
 		public String getValue(UICodelist item) {
 			return item.getVersion();
 		}
 	};
 	
 	protected static final ValueProvider<UICodelist> CODELIST_STATE = new ValueProvider<UICodelist>() {
 
 		@Override
 		public String getValue(UICodelist item) {
 			return item.getState().toString();
 		}
 	};
 	
 	protected static final ValueProvider<UIRepository> REPOSITORY_NAME = new ValueProvider<UIRepository>() {
 
 		@Override
 		public String getValue(UIRepository item) {
 			return item.getName().getLocalPart();
 		}
 	};
 	
 	protected static final ValueProvider<UIRepository> REPOSITORY_PUBLISH_TYPE = new ValueProvider<UIRepository>() {
 
 		@Override
 		public String getValue(UIRepository item) {
 			return item.getPublishedTypes();
 		}
 	};
 	
 	transient Logger logger = LoggerFactory.getLogger(PublishSession.class);
 	
 	@Inject
 	transient CodelistRepository repository;
 	
 	@Inject
 	transient CloudService cloud;
 	
 	@Inject
 	transient LifecycleService lifecycleService;
 	
 	protected OrderedList<UICodelist> orderedCodelists;
 	protected Map<String, UICodelist> indexedCodelists;
 	
 	protected PublishStatus publishStatus;
 	
 	protected OrderedList<UIRepository> orderedRepositories;
 	protected Map<QName, RepositoryService> indexedRepositories;
 	
 	public PublishSession() {
 		orderedCodelists = new OrderedList<UICodelist>();
 		orderedCodelists.addField(UICodelist.NAME_FIELD, CODELIST_NAME);
 		orderedCodelists.addField(UICodelist.VERSION_FIELD, CODELIST_VERSION);
 		orderedCodelists.addField(UICodelist.STATE_FIELD, CODELIST_STATE);
 		indexedCodelists = new HashMap<String, UICodelist>();
 		
 		orderedRepositories = new OrderedList<UIRepository>();
 		orderedRepositories.addField(UIRepository.NAME_FIELD, REPOSITORY_NAME);
 		orderedRepositories.addField(UIRepository.PUBLISHED_TYPES_FIELD, REPOSITORY_PUBLISH_TYPE);
 		indexedRepositories = new HashMap<QName, RepositoryService>();
 	}
 
 	public void loadCodelists()
 	{
 		CodelistQuery<Codelist> query =	allLists();
 		Iterator<Codelist> it = repository.queryFor(query).iterator();
 		
 		orderedCodelists.clear();
 		indexedCodelists.clear();
 
 		while(it.hasNext()) {
 			Codelist codelist = it.next();
			State state = lifecycleService.start(codelist.id()).state();
 			UICodelist uiCodelist = Codelists.toUICodelist(codelist, state);
 			orderedCodelists.add(uiCodelist);
 			indexedCodelists.put(uiCodelist.getId(), uiCodelist);
 		}
 		
 		logger.trace("loaded {} codelists", orderedCodelists.size());
 	}
 	
 	public List<UICodelist> getOrderedCodelists(String sortingField) {
 		return orderedCodelists.getSortedList(sortingField);
 	}
 	
 	public UICodelist getUiCodelist(String id)
 	{
 		return indexedCodelists.get(id);
 	}
 	
 	public void loadRepositories() {
 		orderedRepositories.clear();
 		for (RepositoryService repository: cloud.repositories()) {
 			Format type = selectType(repository.publishedTypes());
 			if (type == null) continue;
 			UIRepository uiRepository = new UIRepository();
 			uiRepository.setId(ValueUtils.safeValue(repository.name()));
 			uiRepository.setName(ValueUtils.safeValue(repository.name()));
 			uiRepository.setPublishedTypes(Repositories.toString(repository.publishedTypes()));
 			uiRepository.setPublishedType(type);
 			orderedRepositories.add(uiRepository);
 			
 			indexedRepositories.put(repository.name(), repository);
 		}
 	}
 	
 	protected Format selectType(Collection<AssetType> types) {
 		Iterator<AssetType> typesIterator = types.iterator();
 		boolean foundCSV = false;
 		while(typesIterator.hasNext()) {
 			AssetType type = typesIterator.next();
 			if (type instanceof CsvGenericType || type instanceof CsvCodelistType) foundCSV = true;
 			if (type instanceof SdmxGenericType || type instanceof SdmxCodelistType) return Format.SDMX;
 		}
 		
 		if (foundCSV) return Format.CSV;
 		return null;
 	}
 	
 	public List<UIRepository> getOrderedRepositories(String sortingField) {
 		return orderedRepositories.getSortedList(sortingField);
 	}
 	
 	public RepositoryService getRepositoryService(UIQName id) {
 		return indexedRepositories.get(ValueUtils.toQName(id));
 	}
 
 	/**
 	 * @return the publishStatus
 	 */
 	public PublishStatus getPublishStatus() {
 		return publishStatus;
 	}
 
 	/**
 	 * @param publishStatus the publishStatus to set
 	 */
 	public void setPublishStatus(PublishStatus publishStatus) {
 		this.publishStatus = publishStatus;
 	}
 	
 }
