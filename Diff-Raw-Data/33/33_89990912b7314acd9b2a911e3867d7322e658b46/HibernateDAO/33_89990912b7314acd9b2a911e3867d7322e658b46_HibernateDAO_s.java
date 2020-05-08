 /**
  * Fonti is a web application for billing and budgeting
  * Copyright (C) 2009  Carlos Fernandez
  *
  * This file is part of Fonti.
  *
  * Fonti is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Fonti is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.carlos.projects.billing.dao.hibernate;
 
 import com.carlos.projects.billing.dao.DAO;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * Contains common logic for Hibernate-based Data Access Objects.
  *
  * @author Carlos Fernandez
  * @date 15-Nov-2009
  */
 abstract class HibernateDAO<T, ID extends Serializable> implements DAO<T, ID> {
 
     private SessionFactory sessionFactory;
 
     HibernateDAO(SessionFactory hibernateSessionFactory) {
         this.sessionFactory = hibernateSessionFactory;
     }
 
     private Session getSession() {
         return sessionFactory.getCurrentSession();
     }
 
     public T getById(Class<T> clazz, Serializable id) {
         if (id == null) {
             return null;
         } else {
             return (T) getSession().get(clazz, id);
         }
     }
 
     public ID save(T entity) {
         Session session = getSession();
         ID id = (ID) session.save(entity);
         session.flush();
         return id;
     }
 
     public List<T> findAll(String entityName) {
         return (List<T>) getSession().createQuery("from " + entityName).list();
     }
 }
