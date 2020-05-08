/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.exoplatform.acceptance.mongo;
 
 import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.support.PersistenceExceptionTranslator;
 import org.springframework.data.mongodb.core.MongoExceptionTranslator;
 
 /**
  * {@linkplain org.springframework.beans.factory.FactoryBean} for EmbedMongo that runs MongoDB as managed process
  * and exposes preconfigured instance of {@link com.mongodb.Mongo}.
  * <p/>
  * <p/>
  * <p>This is not truly embedded Mongo as there's no Java implementation of the
  * MongoDB. EmbedMongo actually downloads original MongoDB binary for your
  * platform and executes it. EmbedMongo process is stopped automatically when
  * you close connection with {@link com.mongodb.Mongo}.</p>
  */
 public class EmbeddedMongoSpringFactoryBean extends EmbeddedMongoFactoryBean implements PersistenceExceptionTranslator {
 
   private PersistenceExceptionTranslator exceptionTranslator = new MongoExceptionTranslator();
 
   /*
    * (non-Javadoc)
    * @see org.springframework.dao.support.PersistenceExceptionTranslator#translateExceptionIfPossible(java.lang.RuntimeException)
    */
   @Override
   public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
     return exceptionTranslator.translateExceptionIfPossible(ex);
   }
 
 }
