 package nl.bhit.mtor.dao.hibernate;
 
 import java.util.List;
 
 import nl.bhit.mtor.dao.MessageDao;
 import nl.bhit.mtor.model.MTorMessage;
 import nl.bhit.mtor.model.Status;
 import nl.bhit.mtor.model.User;
 
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 @Repository("messageDao")
 public class MessageDaoHibernate extends GenericDaoHibernate<MTorMessage, Long> implements MessageDao {
 
     public MessageDaoHibernate() {
         super(MTorMessage.class);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<MTorMessage> getMessagesWithTimestamp(MTorMessage message) {
         String queryString = "from MTorMessage where timestamp <= :timeStamp and project = :project";
         log.trace("runing hql: " + queryString);
         log.trace("timeStamp: " + message.getTimestamp());
         log.trace("project: " + message.getProject().getId());
 
         Query query = getSession().createQuery(queryString);
         query.setTimestamp("timeStamp", message.getTimestamp());
         query.setLong("project", message.getProject().getId());
 
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getAllByUser(User user) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where u = :user");
         query.setLong("user", user.getId());
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getAllByUser(User user, boolean resolved) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where m.resolved = :resolved AND u = :user");
         query.setLong("user", user.getId());
         query.setBoolean("resolved", resolved);
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public MTorMessage getAliveByProject(Long projectId) {
         String hql = "select m as message from MTorMessage as m left join m.project as p where p = :project and m.content like '%alive%'";
         log.trace("running hql:" + hql);
         Query query = getSession().createQuery(hql);
         query.setLong("project", projectId);
         List<MTorMessage> results = query.list();
         if (results != null && results.size() != 0) {
             log.trace("found message" + results.get(0));
             return results.get(0);
         }
         return null;
     }
 
     @Override
     public List<MTorMessage> getUnresolvedAll(User user) {
         return getUnresolvedAll(user.getId());
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getUnresolvedAll(Long userId) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where m.resolved=false AND u = :user");
         query.setLong("user", userId);
         return query.list();
     }
 
 	@Override
 	public Long getMessageNumberByProject(Long projectId, Status... status) {
 		Query query = buildProjectStatusHQL(projectId, "select count(*)", status);
         Long num = (Long)query.uniqueResult();
         return num;
 	}
 
 	@Override
 	public List<MTorMessage> getLastNMessagesByProject(Long projectId, int numberOfMessages, Status... status) {
         Query query = buildProjectStatusHQL(projectId, "select m as message", status);
         @SuppressWarnings("unchecked")
 		List<MTorMessage> lstAux = (List<MTorMessage>)query.list();
         return lstAux == null ? null : lstAux.subList(0, numberOfMessages > lstAux.size() ? lstAux.size() : numberOfMessages);
 	}
 	
 	/**
	 * Helper method in order to build a Query instance based on project id and status.
 	 * 
 	 * @param projectId
 	 * 					Project id which we want to filter.
 	 * @param selectStr
 	 * 					Select statement that will be the beginning of the query.
 	 * @param status
 	 * 					Status which we want to filter.
 	 * @return
 	 * 					Query builded and ready to execute.
 	 */
 	private Query buildProjectStatusHQL(Long projectId, String selectStr, Status... status) {
 		final StringBuffer sbHQL = new StringBuffer(selectStr);
 		sbHQL.append(" from MTorMessage as m left join m.project as p where p = :project");
 		if (status != null && status.length > 0) {
 			sbHQL.append(" and m.status in (");
 			for (int i = 0, nStatus = status.length, nextToLast = nStatus - 1; i < nStatus; i++) {
 				if (status[i] == null) {
 					continue;
 				}
 				sbHQL.append(":status").append(i);
 				if (i < nextToLast) {
 					sbHQL.append(", ");
 				}
 			}
 			sbHQL.append(")");
 		}
 		sbHQL.append(" order by m.timestamp desc");
 		
         Query query = getSession().createQuery(sbHQL.toString());
         query.setLong("project", projectId);
         if (status != null && status.length > 0) {
         	for (int i = 0, nStatus = status.length; i < nStatus; i++) {
         		if (status[i] == null) {
 					continue;
 				}
         		query.setParameter("status".concat(String.valueOf(i)), status[i]);
 			}
 		}
         
         return query;
 	}
 	
 }
