 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hibernate;
 
 import com.smartitengineering.dao.common.CommonDao;
 import com.smartitengineering.dao.common.CommonDaoWithList;
 import com.smartitengineering.dao.common.CommonDaoWithVarArgs;
 import com.smartitengineering.dao.common.QueryParameter;
 import com.smartitengineering.domain.PersistentDTO;
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  *
  * @author Imran M Yousuf
  */
 public abstract class AbstractCommonDaoImpl<Template extends PersistentDTO>
     extends AbstractDAO<Template>
     implements CommonDao<Template>,
                CommonDaoWithList<Template>,
                CommonDaoWithVarArgs<Template> {
 
     private Class entityClass;
 
     public void save(Template... states) {
        if(states != null && states.length <= 0) {
             return; 
         }
         if (entityClass == null) {
             entityClass = states[0].getClass();
         }
         createEntity(states);
     }
 
     public void update(Template... states) {
        if(states != null && states.length<= 0) {
             return; 
         }
         if (entityClass == null) {
             entityClass = states[0].getClass();
         }
         updateEntity(states);
     }
 
     public void delete(Template... states) {
        if(states != null && states.length <= 0) {
             return; 
         }
         if (entityClass == null) {
             entityClass = states[0].getClass();
         }
         deleteEntity(states);
     }
 
     public Template getSingle(Hashtable<String, QueryParameter> query) {
         return readSingle(entityClass, query);
     }
 
     public List<Template> getList(Hashtable<String, QueryParameter> query) {
         return readList(entityClass, query);
     }
 
     public Object getOther(Hashtable<String, QueryParameter> query) {
         return readOther(entityClass, query);
     }
 
     protected Class getEntityClass() {
         return entityClass;
     }
 
     protected void setEntityClass(Class entityClass) {
         this.entityClass = entityClass;
     }
 
     public Template getSingle(List<QueryParameter> query) {
         return readSingle(entityClass, query);
     }
 
     public List<Template> getList(List<QueryParameter> query) {
         return readList(entityClass, query);
     }
 
     public Object getOther(List<QueryParameter> query) {
         return readOther(entityClass, query);
     }
 
     public Template getSingle(QueryParameter... query) {
         return readSingle(entityClass, query);
     }
 
     public List<Template> getList(QueryParameter... query) {
         return readList(entityClass, query);
     }
 
     public Object getOther(QueryParameter... query) {
         return readOther(entityClass, query);
     }
 
     public List<? extends Object> getOtherList(
         Hashtable<String, QueryParameter> query) {
         return readOtherList(entityClass, query);
     }
 
     public List<? extends Object> getOtherList(List<QueryParameter> query) {
         return readOtherList(entityClass, query);
     }
 
     public List<? extends Object> getOtherList(QueryParameter... query) {
         return readOtherList(entityClass, query);
     }
 }
