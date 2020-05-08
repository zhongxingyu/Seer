 package Server;
 
 import Domain.Article;
 import Shared.ArticleStore;
 import Domain.NotFound404Exception;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
 
 import java.rmi.RemoteException;
 import java.util.*;
 
 /**
  * @author Chris Card, Steven Ruppert
  *
  * A BulletinBoard cluster node, with persistent article storage.
  */
 public abstract class Node implements Slave
 {
     private static Logger log = LogManager.getLogger();
 
     protected final ArticleStore articleStore;
 
     public Node(String databaseName, boolean inMemory)
     {
         // connect to embedded article database
        DBI dbi = new DBI(inMemory ? "jdbc:h2:mem:"+databaseName+";DB_CLOSE_DELAY=-1" : "jdbc:h2:dbs/" + databaseName);
         articleStore = dbi.onDemand(ArticleStore.class);
         articleStore.initializeTable();
         articleStore.initializeCountTable();
     }
 
     /**
      * Replicates an article locally. Called by the master on the slaves.
      */
     public void replicateWrite(Article article)
     {
         log.debug("Replicating article {}...", article.id);
         try
         {
             articleStore.insert(article);
             log.info("Replicated article {}", article.id);
         } catch (Exception e)
         {
             // XXX some exceptions are unserializable so when RMI tries to pass them to
             // the caller, we get NotSerializableExceptions instead of a proper message.
             // Thus, wrap message in a serializable runtimeException without a cause.
             throw new RuntimeException("Couldn't replicate write: " + e.getMessage());
         }
     }
 
     /**
      * Returns the article from the local machine with the specified id
      *
      * @param id id of article to retrieve.
      * @throws RemoteException if an article with specified id is not found.
      */
     @Override
     public Article getLocalArticle(int id) throws RemoteException,NotFound404Exception
     {
         log.debug("fetching article {} locally...", id);
         Article article;
         try
         {
             article = articleStore.get(id);
         } catch (Exception e)
         {
             // XXX some exceptions are unserializable so when RMI tries to pass them to
             // the caller, we get NotSerializableExceptions instead of a proper message.
             // Thus, wrap message in a serializable runtimeException without a cause.
             throw new RuntimeException("Couldn't read article: " + e.getMessage());
         }
         if (null == article)
         {
             log.info("Article {} not found locally...", id);
             throw new NotFound404Exception("404 Article not found");
         }
         log.info("Retrieved article {} locally.", id);
         return article;
     }
 
     /**
      * Returns all articles stored locally on this node.
      */
     @Override
     public List<Article> getLocalArticles(int offset) throws RemoteException
     {
         log.debug("Retrieving all local articles...");
         try
         {
             List<Article> all = articleStore.getArticles(offset);
             log.info("Retrieved {} local articles.", all.size());
             return all;
         } catch (Exception e)
         {
             // XXX some exceptions are unserializable so when RMI tries to pass them to
             // the caller, we get NotSerializableExceptions instead of a proper message.
             // Thus, wrap message in a serializable runtimeException without a cause.
             throw new RuntimeException("Couldn't read articles: " + e.getMessage());
         }
     }
 
     @Override
     public void sync(Slave node) throws RemoteException
     {
         log.debug("Syncing state with other node...");
         long start = System.nanoTime();
         for (Article article : node.getAllArticles())
         {
             try
             {
                 articleStore.insert(article);
             } catch (UnableToExecuteStatementException ignored)
             {
                 // ignore primary key constraint violations, article already exists.
             }
         }
         log.debug("State synced in {} ms.", (System.nanoTime() - start) / 1000000);
     }
 
     @Override
     public List<Article> getAllArticles() throws RemoteException
     {
         return articleStore.getAllArticles();
     }
 }
