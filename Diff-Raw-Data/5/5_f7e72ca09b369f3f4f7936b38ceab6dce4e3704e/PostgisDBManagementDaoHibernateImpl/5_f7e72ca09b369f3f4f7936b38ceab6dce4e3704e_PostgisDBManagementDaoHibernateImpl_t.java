 /*
  * 
  * 
  * Copyright (C) 2012
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  * 
  * 
  */
 package com.emergya.persistenceGeo.dao.impl;
 
 import java.math.BigInteger;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.exception.SQLGrammarException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Repository;
 
 import com.emergya.persistenceGeo.dao.DBManagementDao;
 
 /**
  * Implementacion de ExecuterSQL dao para hibernate
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  *
  */
 @Repository
 public class PostgisDBManagementDaoHibernateImpl extends HibernateDaoSupport implements DBManagementDao{
 	
 	private final String getSize = "pg_table_size";
 	private final String getSizeText = "pg_size_pretty";
 	
 	@Autowired
 	public void init(SessionFactory sessionFactory){
 		setSessionFactory(sessionFactory);
 	}
 
 	/**
 	 * Calcula cuanto ocupa una tabla en la base de datos
 	 * 
 	 * @return espacio en bytes
 	 */
 	public long getTableSize(String table_name){
 		
        String sql = "SELECT "+getSize+"(\'"+table_name+"\');";
         
         
         Long result;
         try{
         	result = ((BigInteger) getSession().createSQLQuery(sql).uniqueResult()).longValue();
         }catch(SQLGrammarException e){
         	result = -1L;
         }
     	
         return result;
 	}
 	
 	/**
 	 * Calcula cuanto ocupa una tabla en la base de datos 
 	 * 
 	 * @return resultado de la consulta como cadena de texto
 	 */
 	public String getTableSizeText(String table_name){
 		
        String sql = "SELECT "+getSizeText+"("+getSize+"(\'"+table_name+"\'));";
         
         String result;
         try{
         	result = (String) getSession().createSQLQuery(sql).uniqueResult();
         }catch(SQLGrammarException e){
         	result = "";
         }
         
     	return result;
     	
         
 		
 	}
 	
 	
 	
 }
