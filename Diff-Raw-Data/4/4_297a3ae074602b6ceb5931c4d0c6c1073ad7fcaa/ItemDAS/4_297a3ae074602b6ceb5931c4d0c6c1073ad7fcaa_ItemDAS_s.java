 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.item.db;
 
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 
 public class ItemDAS extends AbstractDAS<ItemDTO> {
 
     /**
      * Returns a list of all items for the given item type (category) id.
      * If no results are found an empty list will be returned.
      *
      * @param itemTypeId item type id
      * @return list of items, empty if none found
      */
     @SuppressWarnings("unchecked")
     public List<ItemDTO> findAllByItemType(Integer itemTypeId) {
         Criteria criteria = getSession().createCriteria(getPersistentClass())
                 .createAlias("itemTypes", "type")
                 .add(Restrictions.eq("type.id", itemTypeId));
 
         return criteria.list();
     }
 
     /**
      * Returns a list of all items with item type (category) who's
      * description matches the given prefix.
      *
      * @param prefix prefix to check
      * @return list of items, empty if none found
      */
     @SuppressWarnings("unchecked")
     public List<ItemDTO> findItemsByCategoryPrefix(String prefix) {
         Criteria criteria = getSession().createCriteria(getPersistentClass())
                 .createAlias("itemTypes", "type")
                 .add(Restrictions.like("type.description", prefix + "%"));
 
         return criteria.list();
     }    
     
     public List<ItemDTO> findItemsByInternalNumber(String internalNumber) {
         Criteria criteria = getSession().createCriteria(getPersistentClass())
                 .add(Restrictions.eq("internalNumber", internalNumber));
 
         return criteria.list();
     }
     
     /**
      * Get all dependencies from item_dependency table
      * @return A list containing all the rows of the table with columns item_i and child_item_id.
      */
     @SuppressWarnings("unchecked")
 	public List<Object[]> getAllDependencies(){
 		
       	 Query query = getSession()
                    .createSQLQuery("select a.item_id,a.child_item_id from item_dependency a " +
                    		"ORDER BY a.item_id ASC");
       	 	
        	return query.list();
        }
 	
 	/**
 	 * Get parent itemIds for the given child from item_dependency table
 	 * @param childId
 	 * @return List of Integer containing itemId of parent products
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Integer> getParents(Integer childId){
       	 Query query = getSession()
                    .createSQLQuery("select a.item_id from item_dependency a " +
                    		"WHERE a.child_item_id=:childId ORDER BY a.item_id ASC")
                    		.setParameter("childId", childId);
       	 
       	 if(query.list()!=null && !query.list().isEmpty() && query.list().get(0) instanceof BigDecimal){
       		 List<Integer> x=new ArrayList<Integer>();
       		 List<BigDecimal> a=query.list();
       		for(int i=0;i<a.size();i++){
           		x.add(Integer.valueOf(a.get(i).intValue()));
           	}
       		return x;
       	 }
       	 else{
       		return query.list();
       	 }
        }
 	
 	/**
 	 * Get child itemIds for the given parent from item_dependency table
 	 * @param parentdId
 	 * @return List of Integer containing itemId of child products
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Integer> getChildren(Integer parentId){
       	 Query query = getSession()
                    .createSQLQuery("select a.child_item_id from item_dependency a " +
                    		"WHERE a.item_id=:parentId ORDER BY a.item_id ASC")
                    		.setParameter("parentId", parentId);
       	 if(query.list()!=null && !query.list().isEmpty() && query.list().get(0) instanceof BigDecimal){
       		 List<Integer> x=new ArrayList<Integer>();
       		 List<BigDecimal> a=query.list();
       		for(int i=0;i<a.size();i++){
           		x.add(Integer.valueOf(a.get(i).intValue()));
           	}
       		return x;
       	 }
       	 else{
       		return query.list();
       	 }
       	
       	
        	
        }
 	
 	/**
 	 * Returns the double linked children for the given parent
 	 * @param parentId
 	 * @return List of Integer containing child_item_id s
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Integer> getDoubleLinkedChildren(Integer parentId) {
 		Query query = getSession()
                 .createSQLQuery("select a.child_item_id from item_dependency a " +
                 		"WHERE a.item_id=:parentId AND double_linked=1 ORDER BY a.item_id ASC")
                 		.setParameter("parentId", parentId);
    
 		if(query.list()!=null && !query.list().isEmpty() && query.list().get(0) instanceof BigDecimal){
      		 List<Integer> x=new ArrayList<Integer>();
      		 List<BigDecimal> a=query.list();
      		for(int i=0;i<a.size();i++){
          		x.add(Integer.valueOf(a.get(i).intValue()));
          	}
      		return x;
      	 }
      	 else{
      		return query.list();
      	 }
 	}
 	
 	/**
 	 * Returns the double linked parents for the given child
 	 * @param childId
 	 * @return List of Integer containing parent item_id s
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Integer> getDoubleLinkedParents(Integer childId) {
 		Query query = getSession()
                 .createSQLQuery("select a.item_id from item_dependency a " +
                 		"WHERE a.child_item_id=:childId AND double_linked=1 ORDER BY a.item_id ASC")
                 		.setParameter("childId", childId);
    
 		if(query.list()!=null && !query.list().isEmpty() && query.list().get(0) instanceof BigDecimal){
     		 List<Integer> x=new ArrayList<Integer>();
     		 List<BigDecimal> a=query.list();
     		for(int i=0;i<a.size();i++){
         		x.add(Integer.valueOf(a.get(i).intValue()));
         	}
     		return x;
     	 }
     	 else{
     		return query.list();
     	 }
 	}
 	
 	/**
 	 * Insert a row into item_dependency containing parentId, childId and doubleLinked
 	 * @param childId
 	 * @param parentId
 	 * @param isDoubleLinked
 	 */
 	@SuppressWarnings("unchecked")
 	public void setParent(Integer childId, Integer parentId,Integer isDoubleLinked){
 		
 			Object result = (Object) getSession()
                     .createSQLQuery("select * from item_dependency where item_id=:parentId AND child_item_id=:childId")
                     .setParameter("parentId", parentId)
                     .setParameter("childId", childId)
                     .uniqueResult();
 			
        	 	Query query=null; 
        	 	if(result!=null){
        	 		query = getSession().createSQLQuery(
      	    			"UPDATE item_dependency SET item_id=:parentId, child_item_id=:childId, double_linked=:doubleLinked  where item_id=:parentId AND child_item_id=:childId")
      	    			.setParameter("parentId", parentId)
                 		.setParameter("childId", childId)
                 		.setParameter("doubleLinked",isDoubleLinked);
        	 	}
        	 	else{
        	 			query = getSession()
       	 					.createSQLQuery("insert into item_dependency values (:parentId,:childId,:doubleLinked)")
        	 					.setParameter("parentId", parentId)
                     		.setParameter("childId", childId)
                     		.setParameter("doubleLinked",isDoubleLinked);
        	 	
        	 	}
        	 query.executeUpdate();
        }
 	
 	/**
 	 * Removes all parents for the given child
 	 * @param childId
 	 */
 	@SuppressWarnings("unchecked")
 	public void removeAllParents(Integer childId){
 		
       	 Query query = getSession()
                    .createSQLQuery("delete from item_dependency where child_item_id=:childId")
                    		.setParameter("childId", childId);
       	query.executeUpdate();
        	
        }
 	
 	/**
 	 * Returns the item period
 	 * @param itemId
 	 * @return A string containing the period of an item. E.g: "One time" or "Yearly"
 	 */
 	@SuppressWarnings("unchecked")
 	public String getItemPeriod(Integer itemId) {
 		Query query = getSession()
                 .createSQLQuery("select a.period from item_period a " +
                 		"WHERE a.item_id=:itemId")
                 		.setParameter("itemId", itemId);
    
     	return (String)query.uniqueResult();
 	}
 
 	/**
 	 * Sets a row into item_period containing parentId, childId and doubleLinked
 	 * @param itemId
 	 * @param period
 	 * @param quantityToOne
 	 */
 	public void setItemPeriod(Integer itemId, String period, Integer quantityToOne) {
 		Object result = (Object) getSession()
                 .createSQLQuery("select * from item_period where item_id=:itemId")
                 .setParameter("itemId", itemId)
                 .uniqueResult();
    	 	Query query=null; 
    	 	if(result!=null){
    	 		query = getSession().createSQLQuery(
  	    			"UPDATE item_period SET period=:period, quantity_invoice_one=:quantityToOne WHERE item_id=:itemId")
  	    			.setParameter("itemId", itemId)
  	    			.setParameter("period", period)
  	    			.setParameter("quantityToOne", quantityToOne);
    	 	}
    	 	else{
    	 		query = getSession()
                 .createSQLQuery("insert into item_period values (:itemId,:period,:quantityToOne)")
                 	.setParameter("itemId", itemId)
                 		.setParameter("period", period)
                 		.setParameter("quantityToOne", quantityToOne);
    	 	
    	 	}
    	 
 		
    	 	query.executeUpdate();
 	}
 
 	/**
 	 * Deletes the rows containing the given itemId from item_dependency
 	 * @param itemId
 	 */
 	public void deleteItemDependencies(Integer itemId) {
 		Query query = getSession()
                 .createSQLQuery("delete from item_dependency where item_id=:itemId OR child_item_id=:itemId")
             	.setParameter("itemId", itemId);
 		query.executeUpdate();
 		
 	}
 
 	/**
 	 * Deletes the row containing the given itemId from item_period
 	 * @param itemId
 	 */
 	public void deleteItemPeriod(Integer itemId) {
 		Query query = getSession()
                 .createSQLQuery("delete from item_period where item_id=:itemId")
             	.setParameter("itemId", itemId);
 		query.executeUpdate();
 		
 	}
 
 	/**
 	 * Some products must have 1 as quantity when invoicing. API and Installation fees for example.
 	 * @param itemId
 	 * @return 1 if has to be quantity one, else 0.
 	 */
 	public Integer hasToBeQuantityOne(int itemId) {
 		Query query = getSession()
                 .createSQLQuery("select quantity_invoice_one from item_period where item_id=:itemId")
             	.setParameter("itemId", itemId);
 		
 		Object result=query.uniqueResult();
 
 		if(result==null){
    	 		return new Integer(0);
    	 	}
    	 	else if(query.list().get(0) instanceof BigDecimal){
    	 		return Integer.valueOf(((BigDecimal)result).intValue());
    	 	}
    	 	else{
    	 		return (Integer)result;
    	 	}
 		
 	}
 	
 	/**
 	 * Returns the number of minimum items for a product,
 	 * when the item requires a minumum quantity. OLAP needs 5 for example.
 	 * @param itemId
 	 * @return Integer. Number of minimum items for a product itself.
 	 */
 	@SuppressWarnings("unchecked")
 	public Integer getMinItems(Integer itemId){
       	 Query query = getSession()
                    .createSQLQuery("select a.min_items from item_dependency a " +
                    		"WHERE a.item_id=:itemId AND a.child_item_id=:itemId")
                    		.setParameter("itemId", itemId);
       	 
       	Object result=query.uniqueResult();
 
 		if(result==null){
    	 		return new Integer(0);
    	 	}
    	 	else if(query.list().get(0) instanceof BigDecimal){
    	 		return Integer.valueOf(((BigDecimal)result).intValue());
    	 	}
    	 	else{
    	 		return (Integer)result;
    	 	}
        }
 
 	/**
 	 * Sets the number of minimum items for a product.
 	 * @param itemId
 	 * @param minItems
 	 */
 	public void setMinItems(Integer itemId, Integer minItems) {
 		Object result = (Object) getSession()
                 .createSQLQuery("select * from item_dependency where item_id=:itemId AND child_item_id=:itemId")
                 .setParameter("itemId", itemId)
                 .uniqueResult();
    	 	Query query=null; 
    	 	if(result!=null){
    	 		query = getSession().createSQLQuery(
  	    			"UPDATE item_dependency SET min_items=:minItems WHERE item_id=:itemId AND child_item_id=:itemId")
  	    			.setParameter("itemId", itemId)
  	    			.setParameter("minItems", minItems);
    	 	}
    	 	else{
    	 		query = getSession()
                 .createSQLQuery("insert into item_dependency values (:itemId,:itemId,0,:minItems)")
                 	.setParameter("itemId", itemId)
                 		.setParameter("minItems", minItems);
    	 	
    	 	}
    	 	query.executeUpdate();
 	}
 	
 	
 }
