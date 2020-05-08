 package org.objectquery.hibernate;
 
 import java.util.List;
 
 import org.hibernate.Session;
 import org.objectquery.DeleteQuery;
 import org.objectquery.InsertQuery;
 import org.objectquery.QueryEngine;
 import org.objectquery.SelectMapQuery;
 import org.objectquery.SelectQuery;
 import org.objectquery.UpdateQuery;
 
 public class HibernateQueryEngine extends QueryEngine<Session> {
 
	@SuppressWarnings("unchecked")
 	@Override
	public <RET extends List<?>> RET execute(SelectQuery<?> query, Session engineSession) {
		return (RET) HibernateObjectQuery.execute(query, engineSession);
 	}
 
 	@Override
 	public int execute(DeleteQuery<?> dq, Session engineSession) {
 		return HibernateObjectQuery.execute(dq, engineSession);
 	}
 
 	@Override
 	public boolean execute(InsertQuery<?> ip, Session engineSession) {
 		throw new UnsupportedOperationException("Hibernate Query Engine doesn't support insert query");
 	}
 
 	@Override
 	public int execute(UpdateQuery<?> query, Session engineSession) {
 		return HibernateObjectQuery.execute(query, engineSession);
 	}
 
 	@Override
	public <RET extends List<M>, M> RET execute(SelectMapQuery<?, M> query, Session engineSession) {
 		return HibernateObjectQuery.execute(query, engineSession);
 	}
 }
