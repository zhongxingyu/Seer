 /*
  * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
  * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
  * triple-shield Mayo logo are trademarks and service marks of MFMER.
  *
  * Except as contained in the copyright notice above, or as used to identify 
  * MFMER as the author of this software, the trade names, trademarks, service
  * marks, or product names of the copyright holder shall not be used in
  * advertising, promotion or otherwise in connection with this software without
  * prior written authorization of the copyright holder.
  * 
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  * 
  * 		http://www.eclipse.org/legal/epl-v10.html
  * 
  */
 package edu.mayo.cts2.framework.plugin.service.exist.profile;
 
 import java.util.Date;
 
 import org.xmldb.api.base.Resource;
 
 import edu.mayo.cts2.framework.model.core.ChangeDescription;
 import edu.mayo.cts2.framework.model.core.ChangeableElementGroup;
 import edu.mayo.cts2.framework.model.core.IsChangeable;
 import edu.mayo.cts2.framework.model.core.types.ChangeCommitted;
 import edu.mayo.cts2.framework.model.core.types.ChangeType;
 import edu.mayo.cts2.framework.model.service.core.BaseMaintenanceService;
 import edu.mayo.cts2.framework.model.updates.ChangeableResource;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.validator.ChangeSetUriValidator;
 import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
 import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
 
 /**
  * The Class AbstractService.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 public abstract class AbstractExistMaintenanceService<
 	D extends IsChangeable,
 	R extends IsChangeable,
 	I,
 	T extends BaseMaintenanceService> 
 	extends AbstractExistResourceReadingService<R,I,T> 
 	implements edu.mayo.cts2.framework.service.profile.BaseMaintenanceService<D,R,I>, 
 	ChangeableResourceHandler {
 
 	@javax.annotation.Resource
 	private StateChangeCallback stateChangeCallback;
 	
 	@javax.annotation.Resource
 	private ChangeSetUriValidator changeSetUriValidator;
 	
 	@Override
 	public void updateChangeableMetadata(I identifier,
 			UpdateChangeableMetadataRequest request) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void deleteResource(I identifier, String changeSetUri) {
 		//this can either be
 		//a) A DELETE of a COMMITTED Resource
 		//b) A DELETE of a Resource in an PENDING ChangeSet
 		//check in the change set first.
 		Resource resource = this.getResource(identifier,changeSetUri);
 		if(resource == null){
 			resource = this.getResource(identifier);
 		}
 	
 		D changeable = this.doUnmarshall(resource);
 		
 		ChangeableElementGroup group = changeable.getChangeableElementGroup();
 		
 		ChangeDescription changeDescription = new ChangeDescription();
 		
 		changeDescription.setChangeDate(new Date());
 		changeDescription.setChangeType(ChangeType.DELETE);
 		changeDescription.setCommitted(ChangeCommitted.PENDING);
 		changeDescription.setContainingChangeSet(changeSetUri);
 		
 		group.setChangeDescription(changeDescription);
 
 		this.doStoreResource(changeable);
 		
 		ChangeableResource choice = new ChangeableResource();
 		
 		this.addResourceToChangeableResource(choice, changeable);
 		
 		this.stateChangeCallback.resourceDeleted(choice, changeSetUri);
 	}
 	
 	@Override
 	public void updateResource(D resource) {
 		this.doStoreResource(resource);
 		
 		ChangeableResource choice = new ChangeableResource();
 		
 		this.addResourceToChangeableResource(choice, resource);
 		
 		this.stateChangeCallback.resourceUpdated(choice);
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected D doUnmarshall(Resource resource){
 		return (D) this.getResourceUnmarshaller().unmarshallResource(resource);
 	}
 
 	public D createResource(R inputResource) {
 		
 		D resource = this.resourceToIndentifiedResource(inputResource);
 
 		this.doStoreResource(resource);
 		
 		ChangeableResource choice = new ChangeableResource();
 		
 		this.addResourceToChangeableResource(choice, resource);
 		
 		this.stateChangeCallback.resourceAdded(choice);
 		
 		return resource;
 	}
 
 	protected abstract D resourceToIndentifiedResource(R resource);
 
 	protected abstract String getPathFromResource(D inputResource);
 
 	protected void doStoreResource(D resource){
 		
 		String path = 
 				this.getPathFromResource(resource);
 
 		String name = this.getExistStorageNameForResource(resource);
 
 		ChangeableElementGroup group = resource.getChangeableElementGroup();
 		
 		String changeSetUri = group.getChangeDescription().getContainingChangeSet();
 		
 		this.changeSetUriValidator.validateChangeSet(changeSetUri);
 		
 		String changeSetDir = ExistServiceUtils.getTempChangeSetContentDirName(changeSetUri);
 		
 		String wholePath = this.createPath(changeSetDir, this.getResourceInfo().getResourceBasePath(), path);
 	
 		this.getExistResourceDao().storeResource(wholePath, name, 
 				this.processResourceBeforeStore(resource));
 	}
 	
 	protected abstract R processResourceBeforeStore(D resource);
 	
 	protected abstract String getExistStorageNameForResource(D resource);
 
 	protected abstract void addResourceToChangeableResource(ChangeableResource choice, D resource);
 	
 	/**
 	 * Gets the resource from changeable resource.
 	 * 
 	 * Return NULL if the ChangeableResource contains an element that this service cannot process.
 	 *
 	 * @param choice the choice
 	 * @return the resource from changeable resource
 	 */
 	protected abstract R getResourceFromChangeableResource(ChangeableResource choice);
 	
 	public void handle(ChangeableResource changeableResource){
 		R resource = this.getResourceFromChangeableResource(changeableResource);
 		if(resource == null){
 			return;
 		}
 		
 		ChangeType changeType = 
 			changeableResource.getChangeableElementGroup().getChangeDescription().getChangeType();
 				
 		switch(changeType){
 		case UPDATE:
			D identifiedResource = this.resourceToIndentifiedResource(resource);
 			this.updateResource(identifiedResource);
 			break;
 		case CLONE:
 			throw new UnsupportedOperationException();
 		case CREATE:
 			this.createResource(resource);
 			break;
 		case DELETE:
 			break;
 		case IMPORT:
 			this.createResource(resource);
 			break;
 		case METADATA:
 			throw new UnsupportedOperationException();
 		default:
 			throw new UnsupportedOperationException();
 		}
 	}
 }
