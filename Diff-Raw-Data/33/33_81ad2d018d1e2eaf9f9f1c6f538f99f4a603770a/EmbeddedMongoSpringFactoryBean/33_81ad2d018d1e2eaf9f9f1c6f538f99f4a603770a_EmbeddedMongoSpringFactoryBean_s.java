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
