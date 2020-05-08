 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.query;
 
 import javax.xml.namespace.QName;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.metadata.Model;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.MetaDataModel;
 import org.eclipse.jst.jsf.common.metadata.internal.TaglibDomainMetaDataModelContextImpl;
 import org.eclipse.jst.jsf.common.metadata.internal.MetaDataModelManager;
 import org.eclipse.jst.jsf.common.metadata.query.internal.SimpleEntityQueryVisitorImpl;
 import org.eclipse.jst.jsf.common.metadata.query.internal.SimpleTraitQueryVisitorImpl;
 import org.eclipse.jst.jsf.common.metadata.query.internal.HierarchicalSearchControl;
 
 
 /**
  * Helper class with static methods to simplify querying of a metadata model. 
  * 
  * <p>Steps for use:
  * 	<br>1) Get the ITaglibDomainMetaDataModelContext using createMetaDataModelContext or createTagLibraryDomainMetaDataModelContext
  * 	<br>2) Use appropriate getXXX methods using the ITaglibDomainMetaDataModelContext.
  * <p><b>Provisional API - subject to change</b></p>
  * @see IResultSet
  * @see IEntityQueryVisitor
  * @see ITraitQueryVisitor
  * @see ITaglibDomainMetaDataModelContext
  * @see Model
  * @see Entity
  * @see Trait
  */
 public final class TaglibDomainMetaDataQueryHelper{
 	/**
 	 * Domain id for Tag library domain of metatdata  
 	 */
 	public static final String TAGLIB_DOMAIN = "TagLibraryDomain"; //need better place for this
 	
 	/**
 	 * private constructor
 	 */
 	private TaglibDomainMetaDataQueryHelper (){
 		super();
 	}
 	
 	/**
 	 * Convenience method for creating {@link ITaglibDomainMetaDataModelContext}s for TAGLIB_DOMAIN
 	 * @param project
 	 * @param uri
 	 * @return ITaglibDomainMetaDataModelContext
 	 */
 	public static ITaglibDomainMetaDataModelContext createMetaDataModelContext(IProject project, String uri){
 		return new TaglibDomainMetaDataModelContextImpl(TAGLIB_DOMAIN, project, uri);
 	}
 	/**
 	 * @param modelContext
 	 * @return Model object for given context.   May return null if not located.
 	 */
 	public static Model getModel(final ITaglibDomainMetaDataModelContext modelContext) {
 		MetaDataModel model = getMDModel(modelContext);
 		//we may want to throw error that model is empty
 		if (model != null && !model.isEmpty()){			
 			return (Model)model.getRoot();
 		}
 		return null;
 	}
 
 	/**
 	 * @param modelContext
 	 * @param entityKey relative to root of the model
 	 * @return the first entity match from the root of the model.   May return null.
 	 */
 	public static Entity getEntity(final ITaglibDomainMetaDataModelContext modelContext,
 			final String entityKey) {
 		IEntityQueryVisitor visitor = new SimpleEntityQueryVisitorImpl(new HierarchicalSearchControl(1, HierarchicalSearchControl.SCOPE_ALL_LEVELS));
 		IResultSet/*<Entity>*/ rs = getEntities(modelContext,entityKey,  visitor);
 		Entity e = null;
 		try {
 			if (! rs.getResults().isEmpty()){
 				e = (Entity)rs.getResults().get(0);				
 			}
 			rs.close();
 		} catch (MetaDataException ex) {
 			JSFCommonPlugin.log(IStatus.ERROR, "Error in Helper.getEntity() - 1", ex);
 		}
 
 		return e;
 	}
 
 	/**
 	 * @param modelContext 
 	 * @param entityKey relative to root of model 
 	 * @param visitor 
 	 * @return an IResultSet of entity objects
 	 */
 	public static IResultSet/*<Entity>*/ getEntities(final ITaglibDomainMetaDataModelContext modelContext,
 				final String entityKey, final IEntityQueryVisitor visitor){
 		Model model = getModel(modelContext);
 		//we may want to throw error that model is empty
 		return getEntities(model, entityKey, visitor);
 		
 	}
 
 	/**
 	 * @param entity
 	 * @param traitKey
 	 * @return a trait or null for the given entity and traitKey using a SimpleEntityQueryVisitorImpl 
 	 */
 	public static Trait getTrait(final Entity entity, final String traitKey){
 		ITraitQueryVisitor visitor = new SimpleTraitQueryVisitorImpl();	
 		Trait t= null;
 		IResultSet/*<Trait>*/ rs = getTraits(entity, traitKey, visitor);
 		try {
 			if (! rs.getResults().isEmpty()){
 				t = (Trait)rs.getResults().get(0);				
 			}
 			rs.close();
 		} catch (MetaDataException ex) {
 			JSFCommonPlugin.log(IStatus.ERROR, "Error in Helper.getTrait()", ex);
 		}
 
 		return t;
 	}
 
 	/**
 	 * @param entity
 	 * @param traitKey
 	 * @param traitQueryVisitor
 	 * @return an IResultSet of trait objects using supplied traitQueryVisitor.  IResultSet should NOT be null.
 	 */
 	public static IResultSet/*<Trait>*/ getTraits(Entity entity, String traitKey,
 			ITraitQueryVisitor traitQueryVisitor) { 
 		IResultSet/*<Trait>*/ rs = traitQueryVisitor.findTraits(entity, traitKey);
 		return rs;
 	}
 
 	/**
 	 * @param initialEntityContext
 	 * @param entityKey relative to initial passed entity
 	 * @return the first entity located by key using SimpleEntityQueryVisitorImpl
 	 */
 	public static Entity getEntity(Entity initialEntityContext, String entityKey) {
 		IEntityQueryVisitor visitor = new SimpleEntityQueryVisitorImpl(new HierarchicalSearchControl(1, HierarchicalSearchControl.SCOPE_ALL_LEVELS));
 		Entity e= null;
 		IResultSet/*<Entity>*/ rs = getEntities(initialEntityContext, entityKey, visitor);
 		try {
 			if (! rs.getResults().isEmpty()){
 				e = (Entity)rs.getResults().get(0);				
 			}
 			rs.close();
 		} catch (MetaDataException ex) {
 			JSFCommonPlugin.log(IStatus.ERROR, "Error in Helper.getEntity() - 0", ex);
 		}		
 
 		return e;		
 	}
 
 	/**
 	 * @param initialEntityContext
 	 * @param entityQuery relative to initial passed entity
 	 * @param entityKeyQueryVisitor
 	 * @return IResultSet of entities located by key using entityQueryVisitor.  IResultSet should NOT be null.
 	 */
 	public static IResultSet/*<Entity>*/ getEntities(Entity initialEntityContext, String entityQuery,
 			IEntityQueryVisitor entityKeyQueryVisitor) {
 				
 		return entityKeyQueryVisitor.findEntities(initialEntityContext, entityQuery);	
 	}
 
 	
 	/**
 	 * Retrieve the MetaDataModel from the ModelManager for given key
 	 * @param modelContext
 	 * @return MetaDataModel
 	 */
	private synchronized static MetaDataModel getMDModel(final ITaglibDomainMetaDataModelContext modelContext){
 		MetaDataModelManager mgr = null;
 		if (modelContext.getProject() != null)
 			mgr = MetaDataModelManager.getInstance(modelContext.getProject());
 		else //temp(?)
 			mgr = MetaDataModelManager.getSharedInstance();	

 		return mgr.getModel(modelContext);
 	}
 
 	/**
 	 * @param modelContext
 	 * @param entityKey
 	 * @param traitKey
 	 * @return first trait found for entity and trait key starting from root of the model using SimpleMetaDataQueryImpl
 	 */
 	public static Trait getTrait(final ITaglibDomainMetaDataModelContext modelContext,
 			final String entityKey, final String traitKey) { 
 		Entity entity = getEntity(modelContext, entityKey);
 		Trait t = null;
 		if (entity != null){			
 			t = getTrait(entity, traitKey);
 		}
 		return t;
 	}	
 	
 	/**
 	 * @param tagEntity
 	 * @return QName for tag entity
 	 */
 	public static QName getQNameForTagEntity(Entity tagEntity) {
 		Assert.isTrue(tagEntity != null);
 		return new QName(tagEntity.getModel().getCurrentModelContext().getUri(), tagEntity.getId());
 	}
 }
