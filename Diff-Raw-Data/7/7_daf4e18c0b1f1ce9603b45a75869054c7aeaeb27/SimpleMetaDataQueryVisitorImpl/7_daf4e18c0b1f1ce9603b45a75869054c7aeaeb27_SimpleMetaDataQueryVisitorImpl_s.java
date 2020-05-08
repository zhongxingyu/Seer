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
 package org.eclipse.jst.jsf.common.metadata.internal.provisional.query;
 
 import java.util.Iterator;
 import java.util.Stack;
 
 import org.eclipse.jst.jsf.common.metadata.internal.provisional.Entity;
 import org.eclipse.jst.jsf.common.metadata.internal.provisional.EntityGroup;
 import org.eclipse.jst.jsf.common.metadata.internal.provisional.Trait;
 
 
 /**
  * A simple metadata query visitor implementing {@link IEntityQueryVisitor} and {@link ITraitQueryVisitor}.
  * - simple find entity and traits by id only 	
  * - Does not allow for wild card searchs
  */
 public class SimpleMetaDataQueryVisitorImpl implements IEntityQueryVisitor, ITraitQueryVisitor  {
 
 	private String traitQuery;
 	private SearchControl control;
 	private boolean _stop;
 
 	private EntityQueryComparator entityComparator;
 	private SimpleResultSet/*<Entity>*/ entityResultSet;
 	private SimpleResultSet/*<Trait>*/ traitResultSet;
 
 	/**
 	 * Constructor that also creates a default SearchControl
 	 */
 	public SimpleMetaDataQueryVisitorImpl() {
 		super();
 		control = new SearchControl();
 	}
 	
 	/**
 	 * Constructor
 	 * @param control
 	 */
 	public SimpleMetaDataQueryVisitorImpl(SearchControl control) {
 		super();
 		this.control = control;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.ITraitQueryVisitor#findTraits(org.eclipse.jst.jsf.common.metadata.internal.provisional.Entity, java.lang.String)
 	 */
 	public IResultSet/*<Trait>*/ findTraits(final Entity entity, final String traitQuery){
 		
 		resetQuery();
 		if (entity != null){			
 			this.traitQuery = traitQuery;			
 			for (Iterator/*<Trait>*/ it=entity.getTraits().iterator();it.hasNext();){
 				Trait t = (Trait)it.next();
 				t.accept(this);
 				if (stopVisiting())
 					break;
 			}
 		}
 		return getTraitResultSet();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.ITraitVisitor#visit(org.eclipse.jst.jsf.common.metadata.internal.provisional.Trait)
 	 */
 	public void visit(Trait trait) {		
 		if (trait.equals(traitQuery))
 			getTraitResultSet().addItem(trait);		
 		
 		checkShouldStopVisitingTraits();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.IEntityQueryVisitor#findEntities(org.eclipse.jst.jsf.common.metadata.internal.provisional.Entity, java.lang.String)
 	 */
 	public IResultSet/*<Entity>*/ findEntities(Entity initialEntityContext,
 			String entityKey) {
 		
 		resetQuery();
 		
 		if (initialEntityContext != null){
 			entityComparator = new EntityQueryComparator(entityKey);			
 			initialEntityContext.accept(this);			
 		}
 		
 		return getEntityResultSet();
 	}
 
 	private void resetQuery() {
 		_stop = false;
 	}
 
 	/**
 	 * @return @return lazy init of a SimpleResultSet of Entities
 	 */
 	private SimpleResultSet/*<Entity>*/ getEntityResultSet(){
 		if (entityResultSet == null){
 			entityResultSet = new SimpleResultSet/*<Entity>*/();
 		}
 		return entityResultSet;
 	}
 
 	/**
 	 * @return lazy init of a SimpleResultSet of Traits
 	 */
 	private SimpleResultSet/*<Trait>*/ getTraitResultSet(){
 		if (traitResultSet == null){
 			traitResultSet = new SimpleResultSet/*<Trait>*/();
 		}
 		return traitResultSet;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.IEntityVisitor#visit(org.eclipse.jst.jsf.common.metadata.internal.provisional.Entity)
 	 */
 	public void visit(Entity key) {		
 		switch (entityComparator.compareTo(key)){
 			case 0:
 				getEntityResultSet().addItem(key);
 				break;
 			default:
 				break;
 
 		}
 		checkShouldStopVisitingEntities();
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.IEntityVisitor#visitCompleted()
 	 */
 	public void visitCompleted() {
 		entityComparator.popContext();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.provisional.query.IMetaDataVisitor#stopVisiting()
 	 */
 	public boolean stopVisiting() {
 		return _stop;
 	}
 
 	private void checkShouldStopVisitingEntities(){
 		//implement how to set stop to signal to the entity accept() to skip visiting
 		if (control.getCountLimit()== getEntityResultSet().size() && control.getCountLimit() != SearchControl.COUNT_LIMIT_NONE )
 			_stop = true;
 	}
 	
 	private void checkShouldStopVisitingTraits(){
 		//this simple visitor will only find a single trait as it checks for exact match only currently 
 		if (getTraitResultSet().size() > 0 )
 			_stop = true;
 	}
 
 
 	/**
 	 * Simple comparator that compares that an entity's id for with another
 	 */
 	private class EntityQueryComparator implements Comparable/*<Entity>*/{
 
 		private String entityKey;
 		private EntityStack stack;
 
 		public EntityQueryComparator(String entityKey){
 			this.entityKey = entityKey;		
 			stack = new EntityStack();
 		}
 		
 		public int compareTo(Object entity) {			
 			stack.push(entity);
 			return entityKey.compareTo(getRelativeId());			
 		}
 		
 		public void popContext(){
 			stack.pop();
 		}
 		
 		private String getRelativeId(){
 			int size = stack.size();
 			int i = 1;
 			StringBuffer buf = new StringBuffer();
 			while(i < size){
 				Entity e = (Entity)stack.elementAt(i);
 				if (!(e instanceof EntityGroup)){
 					if (buf.length()>0) 
 						buf.append("/");
 					buf.append(e.getId());
 				}
 				i++;
 			}
 			return buf.toString();
 		}
 	}
 	
 	/**
 	 * Stack used for determining relative key of an entity from the initial context.
 	 */
 	private class EntityStack extends Stack/*<Entity>*/ {
		public EntityStack(){
 			super();
 		}
 	}
 
 }
