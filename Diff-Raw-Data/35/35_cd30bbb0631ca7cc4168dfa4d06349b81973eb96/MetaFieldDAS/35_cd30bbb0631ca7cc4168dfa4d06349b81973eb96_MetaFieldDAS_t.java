 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.metafields.db;
 
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 import org.hibernate.Session;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Brian Cowdery
  * @since 03-Oct-2011
  */
 public class MetaFieldDAS extends AbstractDAS<MetaField> {
 
     @SuppressWarnings("unchecked")
     public List<MetaField> getAvailableFields(Integer entityId, EntityType entityType) {
         DetachedCriteria query = DetachedCriteria.forClass(MetaField.class);
         query.add(Restrictions.eq("entity.id", entityId));
         query.add(Restrictions.eq("entityType", entityType));
         query.addOrder(Order.asc("displayOrder"));
         return getHibernateTemplate().findByCriteria(query);
     }
 
     @SuppressWarnings("unchecked")
     public MetaField getFieldByName(Integer entityId, EntityType entityType, String name) {
         DetachedCriteria query = DetachedCriteria.forClass(MetaField.class);
         query.add(Restrictions.eq("entity.id", entityId));
         query.add(Restrictions.eq("entityType", entityType));
         query.add(Restrictions.eq("name", name));
         List<MetaField> fields = getHibernateTemplate().findByCriteria(query);
         return !fields.isEmpty() ? fields.get(0) : null;
     }
 
     public void deleteMetaFieldValuesForEntity(EntityType entityType, int metaFieldId) {
         Session session = getSession();
        List<String> deleteEntitiesList = new ArrayList<String>();
        
         switch (entityType) {
            case INVOICE:
        	   deleteEntitiesList.add(" invoice_meta_field_map ");
                break;
            case CUSTOMER:
        	   deleteEntitiesList.add(" customer_meta_field_map ");
        	   deleteEntitiesList.add(" partner_meta_field_map ");
                break;
            case PRODUCT:
        	   deleteEntitiesList.add(" item_meta_field_map ");
                break;
            case ORDER:
        	   deleteEntitiesList.add(" order_meta_field_map ");
                break;
            case PAYMENT:
        	   deleteEntitiesList.add(" payment_meta_field_map ");
                break;
         }
        
        String deleteFromSql = "delete from ";
        String deleteWhereSql = " where meta_field_value_id in " +
                 "(select val.id from meta_field_value val where meta_field_name_id = " + metaFieldId + " )";
        
        for (String deleteSingleEntity : deleteEntitiesList) {
        	
        	StringBuilder sqlBuilder = new StringBuilder();
        	sqlBuilder.append(deleteFromSql).append(deleteSingleEntity).append(deleteWhereSql);
        	session.createSQLQuery(sqlBuilder.toString()).executeUpdate();
        }
 
         String deleteValuesHql = "delete from " + MetaFieldValue.class.getSimpleName() + " where field.id = ?";
         getHibernateTemplate().bulkUpdate(deleteValuesHql, metaFieldId);
     }
 }
