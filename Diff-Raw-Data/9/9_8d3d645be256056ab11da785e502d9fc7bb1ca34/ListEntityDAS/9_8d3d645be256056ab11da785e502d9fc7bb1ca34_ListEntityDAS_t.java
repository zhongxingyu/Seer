 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jBilling.
 
     jBilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jBilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jBilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.list.db;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 import java.util.Date;
 
 public class ListEntityDAS extends AbstractDAS<ListEntityDTO> {
 
     public ListEntityDTO create(ListDTO list, Integer entityId, Integer count) {
 
         CompanyDTO company = new CompanyDAS().find(entityId);
 
         ListEntityDTO entity = new ListEntityDTO();
         entity.setEntity(company);
         entity.setList(list);
         entity.setTotalRecords(count);
         entity.setLastUpdate(new Date());
 
         return save(entity);
     }
 

     public ListEntityDTO findByEntity(Integer listId, Integer entityId) {
 
         Criteria criteria = getSession().createCriteria(ListEntityDTO.class);
        criteria.createAlias("list", "list").add(
                Restrictions.eq("list.id", listId.intValue()))
        .createAlias("entity", "entity").add(
                 Restrictions.eq("entity.id", entityId.intValue()))
         .setComment("ListEtntityDAS.findByEntity");
 
         return (ListEntityDTO) criteria.uniqueResult();
     }

 }
