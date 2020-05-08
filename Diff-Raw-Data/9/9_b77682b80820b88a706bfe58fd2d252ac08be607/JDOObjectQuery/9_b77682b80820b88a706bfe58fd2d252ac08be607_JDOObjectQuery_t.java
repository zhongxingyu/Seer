 package org.objectquery.jdoobjectquery;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.objectquery.ObjectQuery;
 import org.objectquery.generic.GenericObjectQuery;
 import org.objectquery.generic.ObjectQueryException;
 
 public class JDOObjectQuery {
 
 	public static JDOQLQueryGenerator jdoqlGenerator(ObjectQuery<?> objectQuery) {
 		if (objectQuery instanceof GenericObjectQuery<?>)
 			return new JDOQLQueryGenerator((GenericObjectQuery<?>) objectQuery);
 		throw new ObjectQueryException("The Object query instance of unconvertable implementation ", null);
 	}
 
 	public static Object execute(ObjectQuery<?> objectQuery, PersistenceManager persistenceManager) {
 		JDOQLQueryGenerator qg = JDOObjectQuery.jdoqlGenerator(objectQuery);
 		Query query = persistenceManager.newQuery(qg.getQuery());
 		query.setClass(((GenericObjectQuery<?>) objectQuery).getTargetClass());
		return query.executeWithMap(qg.getParameters());
 	}
 }
