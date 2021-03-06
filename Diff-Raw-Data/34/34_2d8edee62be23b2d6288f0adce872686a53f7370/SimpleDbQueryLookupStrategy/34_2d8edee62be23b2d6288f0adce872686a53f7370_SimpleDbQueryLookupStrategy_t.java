 package org.springframework.data.simpledb.query;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import org.springframework.data.repository.core.NamedQueries;
 import org.springframework.data.repository.core.RepositoryMetadata;
 import org.springframework.data.repository.query.QueryLookupStrategy;
 import org.springframework.data.repository.query.RepositoryQuery;
 import org.springframework.data.simpledb.core.SimpleDbOperations;
 
 /**
  * Query lookup strategy to execute custom interface query methods <br/>
  * Multiple query lookup strategies can be <b>created and combined</b> here:
  * <ul>
  * <li>create query from method name</li>
  * <li>from custom query annotations </li>
  * </ul>
  */
 public final class SimpleDbQueryLookupStrategy {
 
     private SimpleDbQueryLookupStrategy(){
     };
 
     /**
      * {@link QueryLookupStrategy} that tries to detect a declared query declared via simple db <b>custom {@link Query} annotation</b>.
      */
     private static class AnnotationBasedQueryLookupStrategy implements QueryLookupStrategy {
 
         private final SimpleDbOperations<?, Serializable> simpleDbOperations;
 
         public AnnotationBasedQueryLookupStrategy(SimpleDbOperations<?, Serializable> simpleDbOperations) {
             this.simpleDbOperations = simpleDbOperations;
         }
 
         @Override
         public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
             SimpleDbQueryMethod queryMethod = new SimpleDbQueryMethod(method, metadata);
             RepositoryQuery query = SimpleDbRepositoryQuery.fromQueryAnnotation(queryMethod, simpleDbOperations);
 
             if (null != query) {
                 return query;
             }
 
             throw new IllegalStateException(String.format("Did not find an annotated query for method %s!", method));
         }
     }
 
     public static QueryLookupStrategy create(SimpleDbOperations<?, Serializable> simpleDbOperations, QueryLookupStrategy.Key key) {
         //TODO check in Spring data core key switching and their semantics (look in spring-data-jpa)
         return new AnnotationBasedQueryLookupStrategy(simpleDbOperations);
     }
 }
