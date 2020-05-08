 package edu.mayo.cts2.framework.plugin.service.exist.profile.update;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.tools.ant.util.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.xmldb.api.base.Resource;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
import edu.mayo.cts2.framework.model.association.Association;
 import edu.mayo.cts2.framework.model.core.ChangeDescription;
 import edu.mayo.cts2.framework.model.core.ChangeSetElementGroup;
 import edu.mayo.cts2.framework.model.core.ChangeableElementGroup;
 import edu.mayo.cts2.framework.model.core.IsChangeable;
 import edu.mayo.cts2.framework.model.core.OpaqueData;
 import edu.mayo.cts2.framework.model.core.SourceReference;
 import edu.mayo.cts2.framework.model.core.types.ChangeCommitted;
 import edu.mayo.cts2.framework.model.core.types.ChangeType;
 import edu.mayo.cts2.framework.model.core.types.FinalizableState;
 import edu.mayo.cts2.framework.model.exception.Cts2RuntimeException;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
 import edu.mayo.cts2.framework.model.updates.ChangeSet;
 import edu.mayo.cts2.framework.model.updates.ChangeableResource;
 import edu.mayo.cts2.framework.plugin.service.exist.dao.ExistResourceDao;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.ChangeableResourceHandler;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.ResourceMarshaller;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.ResourceUnmarshaller;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.StateChangeCallback;
import edu.mayo.cts2.framework.plugin.service.exist.profile.association.ExistAssociationReadService;
 import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
import edu.mayo.cts2.framework.service.profile.association.name.AssociationReadId;
 import edu.mayo.cts2.framework.service.profile.update.ChangeSetService;
 
 @Component
 public class ExistChangeSetService implements ChangeSetService {
 
 	@javax.annotation.Resource
 	private ChangeSetResourceInfo changeSetResourceInfo;
 	
 	protected Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private ExistResourceDao existResourceDao;
 	
 	@Autowired
	private ExistAssociationReadService existAssociationReadService;
	
	@Autowired
 	private ResourceUnmarshaller resourceUnmarshaller;
 	
 	@Autowired
 	private ResourceMarshaller resourceMarshaller;
 	
 	private List<ChangeableResourceHandler> changeableResourceHandlers;
 	
 	@javax.annotation.Resource
 	private StateChangeCallback stateChangeCallback;
 	
 	@Override
 	public ChangeSet readChangeSet(String changeSetUri) {
 		String name = ExistServiceUtils.uriToExistName(changeSetUri);
 		
 		Resource resource = existResourceDao.getResource(changeSetResourceInfo.getResourceBasePath(), name);
 		
 		return (ChangeSet) this.resourceUnmarshaller.unmarshallResource(resource);
 	}
 
 	@Override
 	public ChangeSet createChangeSet() {
 		ChangeSet changeSet = doCreateNewChangeSet();
 		
 		String name = this.changeSetResourceInfo.getExistResourceNameFromResource(changeSet);
 		
 		this.existResourceDao.storeResource(changeSetResourceInfo.getResourceBasePath(), name, changeSet);
 		
 		return changeSet;
 	}
 	
 	protected ChangeSet doCreateNewChangeSet(){
 		String changeSetUri = this.createNewChangeSetUri();
 		ChangeSet changeSet = new ChangeSet();
 		changeSet.setChangeSetURI(changeSetUri);
 		changeSet.setCreationDate(new Date());
 		changeSet.setState(FinalizableState.OPEN);
 		
 		return changeSet;
 	}
 	
 	protected String createNewChangeSetUri(){
 		return "urn:uuid:" + UUID.randomUUID().toString();
 	}
 
 	@Override
 	public void rollbackChangeSet(String changeSetUri) {
 		//remove the actual ChangeSet
 		String name = ExistServiceUtils.uriToExistName(changeSetUri);
 		this.existResourceDao.deleteResource(changeSetResourceInfo.getResourceBasePath(), name);
 		
 		//remove the ChangeSet contents and tmp directory
 		String changeSetDir = ExistServiceUtils.getTempChangeSetContentDirName(changeSetUri);
 		this.existResourceDao.removeCollection(changeSetDir);
 	}
 
 	@Override
 	public void commitChangeSet(String changeSetUri) {
 		String changeSetDir = ExistServiceUtils.getTempChangeSetContentDirName(changeSetUri);
 		
 		ResourceSet resources = this.existResourceDao.query(changeSetDir, "/*", 0, -1);
 		
 		try {
 			ResourceIterator itr = resources.getIterator();
 			
 			while(itr.hasMoreResources()){
 				Resource resource = itr.nextResource();
 				
 				String parentCollectionName = resource.getParentCollection().getName();
 				
 				parentCollectionName = parentCollectionName.replace(changeSetDir, "");
 
 				String resourceId = StringUtils.removeSuffix(resource.getId(), ".xml");
 				
 				if(log.isDebugEnabled()){
 					log.debug("Moving resource: " + resource.getParentCollection().getName() + resourceId +
 						"To: " + parentCollectionName + resourceId);
 				}
 		
				IsChangeable resourceObj = 
 						(IsChangeable)this.resourceUnmarshaller.unmarshallResource(resource);
 				
 				ChangeDescription changeDescription = 
						resourceObj.getChangeableElementGroup().getChangeDescription();
 				
 				if(changeDescription.getChangeType().equals(ChangeType.DELETE)){
					
					if (resourceObj instanceof Association)
					{
						String id = ((Association)resourceObj).getAssociationID();
						
						LocalIdAssociation localToDelete = existAssociationReadService.read(new AssociationReadId(id), null);
						resourceId = localToDelete.getLocalID();
					}

					//TODO DELETE processing is broken for many types if IsChangeable - anything else that uses generated identifiers.
					//https://github.com/cts2/exist-service/issues/17
					
 					this.existResourceDao.deleteResource(parentCollectionName, resourceId);
 				} else {
 					changeDescription.setCommitted(ChangeCommitted.COMMITTED);
 			
 					resource.setContent(
							resourceMarshaller.marshallResource(resourceObj));
 	
 					this.existResourceDao.storeResource(parentCollectionName, resourceId, resource);
 				}
 			}
 			
 			this.existResourceDao.removeCollection(changeSetDir);
 			
 			ChangeSet changeSet = this.readChangeSet(changeSetUri);
 			
 			for(ChangeableResource change : changeSet.getMember()){
 				if(change.getChangeableElementGroup() == null){
 					change.setChangeableElementGroup(new ChangeableElementGroup());
 				}
 				
 				if(change.getChangeableElementGroup().getChangeDescription() == null){
 					ChangeDescription description = new ChangeDescription();
 					description.setContainingChangeSet(changeSet.getChangeSetURI());
 					description.setChangeType(ChangeType.CREATE);
 					description.setChangeDate(new Date());
 					change.getChangeableElementGroup().setChangeDescription(description);
 				}
 				
 				change.getChangeableElementGroup().
 					getChangeDescription().setCommitted(ChangeCommitted.COMMITTED);
 			}
 	
 			changeSet.setState(FinalizableState.FINAL);
 			changeSet.setCloseDate(new Date());
 			
 			String name = this.changeSetResourceInfo.getExistResourceNameFromResource(changeSet);
 			
 			this.existResourceDao.storeResource(
 					changeSetResourceInfo.getResourceBasePath(), name, changeSet);
 			
 		} catch (XMLDBException e) {
 			throw new Cts2RuntimeException(e);
 		}
 	}
 
 	@Override
 	public String importChangeSet(ChangeSet changeSet) {
 		try
 		{
 			String name = this.changeSetResourceInfo.getExistResourceNameFromResource(changeSet);
 			
 			ArrayList<ChangeableResource> members = new ArrayList<ChangeableResource>();
 			members.addAll(changeSet.getMemberAsReference());
 			changeSet.removeAllMember();
 			changeSet.setState(FinalizableState.OPEN);
 			
 			this.existResourceDao.storeResource(changeSetResourceInfo.getResourceBasePath(), name, changeSet);
 			stateChangeCallback.beginBulkLoadMode(changeSet.getChangeSetURI());
 			
 			int threads = Runtime.getRuntime().availableProcessors() - 2;
 			if (threads < 1)
 			{
 				threads = 1;
 			}
 			
 			//For now, only multi-thread when the change type consists entirely of CREATE items.
 			//eventually, we could be clever, and multi-thread inter-mingled change sets in smaller batches...
 			for (ChangeableResource cr : changeSet.getMemberAsReference())
 			{
 				if (!cr.getChangeableElementGroup().getChangeDescription().getChangeType().equals(ChangeType.CREATE))
 				{
 					threads = 1;
 					break;
 				}
 			}
 			
 			log.info("Processing changeSet items with '" + threads + "' threads");
 			
 			ExecutorService threadPool = new ThreadPoolExecutor(threads, threads, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new ThreadFactory()
 			{
 				@Override
 				public Thread newThread(Runnable r)
 				{
 					Thread thread = new Thread(r);
 					thread.setDaemon(true);
 					thread.setName("ChangeSetImportThread");
 					return thread;
 				}
 			});
 			
 			ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
 			
 			for(final ChangeableResource member : members)
 			{
 				results.add(threadPool.submit(new Callable<Boolean>()
 				{
 					@Override
 					public Boolean call()
 					{
 						try
 						{
 							log.info("Processing change set item with entryOrder of '" + member.getEntryOrder() + "'");
 							for(ChangeableResourceHandler handler : ExistChangeSetService.this.changeableResourceHandlers){
 								handler.handle(member);
 							}
 							return true;
 						}
 						catch (RuntimeException e)
 						{
 							log.error("Death on changeSet item with entryOrder" + member.getEntryOrder(), e);
 							throw e;
 						}
 					}
 				}));
 			}
 			
 			threadPool.shutdown();
 			
 			//Make sure none threw exceptions
 			for (Future<Boolean> f : results)
 			{
 				try
 				{
 					f.get();
 				}
 				catch (InterruptedException e)
 				{
 					throw new Cts2RuntimeException(e);
 				}
 				catch (ExecutionException e)
 				{
 					throw new Cts2RuntimeException(e);
 				}
 			}
 			return changeSet.getChangeSetURI();
 		}
 		finally
 		{
 			stateChangeCallback.endBulkLoadMode(changeSet.getChangeSetURI());
 		}
 	}
 	
 	private void addChangeSetElementGroupIfNecessary(ChangeSet changeSet){
 		if(changeSet.getChangeSetElementGroup() == null){
 			changeSet.setChangeSetElementGroup(new ChangeSetElementGroup());
 		}
 	}
 
 	@Override
 	public void updateChangeSetMetadata(
 			String changeSetUri, 
 			SourceReference creator,
 			OpaqueData changeInstructions, 
 			Date officialEffectiveDate) {
 		ChangeSet changeSet = this.readChangeSet(changeSetUri);
 		
 		if(creator != null){
 			this.addChangeSetElementGroupIfNecessary(changeSet);
 			changeSet.getChangeSetElementGroup().setCreator(creator);
 		}
 		if(changeInstructions != null){
 			this.addChangeSetElementGroupIfNecessary(changeSet);
 			changeSet.getChangeSetElementGroup().setChangeInstructions(changeInstructions);
 		}
 		if(officialEffectiveDate != null){
 			changeSet.setOfficialEffectiveDate(officialEffectiveDate);
 		}
 		
 		String name = this.changeSetResourceInfo.getExistResourceNameFromResource(changeSet);
 		
 		this.existResourceDao.storeResource(
 				changeSetResourceInfo.getResourceBasePath(), name, changeSet);
 	}
 
 	@Autowired
 	public void setChangeableResourceHandlers(
 			List<ChangeableResourceHandler> changeableResourceHandlers) {
 		this.changeableResourceHandlers = changeableResourceHandlers;
 	}
 
 }
