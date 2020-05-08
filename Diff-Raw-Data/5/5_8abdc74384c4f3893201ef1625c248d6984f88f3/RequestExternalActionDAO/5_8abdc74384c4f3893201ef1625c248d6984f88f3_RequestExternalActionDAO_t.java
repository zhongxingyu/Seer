 package fr.cg95.cvq.dao.request.external.hibernate;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.external.RequestExternalAction;
 import fr.cg95.cvq.dao.jpa.JpaTemplate;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.external.IRequestExternalActionDAO;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * @author jsb@zenexity.fr
  *
  */
 public final class RequestExternalActionDAO extends JpaTemplate<RequestExternalAction,Long> implements IRequestExternalActionDAO {
 
     @Override
     @SuppressWarnings("unchecked")
     public List<RequestExternalAction> get(Set<Critere> criteriaSet, String sort,
         String dir, int count, int offset, boolean lastOnly) {
         StringBuffer sb = new StringBuffer();
         sb.append("select * from request_external_action");
         if (lastOnly) {
             List<BigInteger> ids = HibernateUtil.getSession()
                 .createSQLQuery("select max(id) from request_external_action group by key").list();
             if (ids.isEmpty()) {
                 return Collections.emptyList();
             }
             String stringIds[] = new String[ids.size()];
             int i = 0;
             for (BigInteger id : ids)
                 stringIds[i++] = id.toString();
             sb.append(" where id in (");
             sb.append(StringUtils.join(stringIds, ", "));
             sb.append(')');
         } else {
             sb.append(" where 1 = 1 ");
         }
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
         for (Critere searchCrit : criteriaSet) {
             if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(
                     " and (select category_id from request_type rt where rt.id = (select request_type_id from request r where r.id = key)) in ( "
                     + searchCrit.getValue() + ")");
             } else if ("homeFolderId".equals(searchCrit.getAttribut())) {
                 sb.append(" and (select home_folder_id from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             } else if (RequestExternalAction.SEARCH_BY_REQUEST_TYPE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select request_type_id from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             } else if (RequestExternalAction.SEARCH_BY_REQUEST_STATE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select state from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                parametersValues.add(((RequestState)searchCrit.getValue()).name());
                 parametersTypes.add(Hibernate.STRING);
             } else if (RequestExternalAction.SEARCH_BY_COMPLEMENTARY_DATA.equals(searchCrit.getAttribut())) {
                 sb.append(" and id in (select id from request_external_action_complementary_data where key = ? and value ")
                     .append(searchCrit.getSqlComparatif()).append(" ?)");
                 Map.Entry<String, Serializable> entry = (Map.Entry<String, Serializable>)searchCrit.getValue();
                 parametersValues.add(entry.getKey());
                 parametersTypes.add(Hibernate.STRING);
                 parametersValues.add(entry.getValue());
                 parametersTypes.add(Hibernate.SERIALIZABLE);
             } else {
                 sb.append(" and ").append(searchCrit.getAttribut()).append(searchCrit.getSqlComparatif()).append(" ?");
                 if (RequestExternalAction.SEARCH_BY_DATE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getDateValue());
                     parametersTypes.add(Hibernate.TIMESTAMP);
                 } else if (RequestExternalAction.SEARCH_BY_ID.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY_OWNER.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_MESSAGE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_NAME.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_STATUS.equals(searchCrit.getAttribut())) {
                     parametersValues.add(RequestExternalAction.Status.forString(searchCrit.getValue().toString()).name());
                     parametersTypes.add(Hibernate.STRING);
                 }
             }
         }
         sb.append(" order by ");
         if (sort != null) {
             sb.append(sort);
         } else {
             sb.append(RequestExternalAction.SEARCH_BY_DATE);
         }
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
         else sb.append(" asc");
         Query query = HibernateUtil.getSession().createSQLQuery(sb.toString()).addEntity(RequestExternalAction.class);
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
         if (count > 0)
             query.setMaxResults(count);
         query.setFirstResult(offset);
         return (List<RequestExternalAction>)query.list();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<String> getKeys(Set<Critere> criterias) {
         StringBuffer sb = new StringBuffer();
         sb.append("select distinct key from request_external_action");
         sb.append(" where 1 = 1 ");
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
         for (Critere searchCrit : criterias) {
             if (RequestExternalAction.SEARCH_BY_REQUEST_TYPE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select request_type_id from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             } else if (RequestExternalAction.SEARCH_BY_REQUEST_STATE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select state from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(
                     " and (select category_id from request_type rt where rt.id = (select request_type_id from request r where r.id = key)) in ( "
                     + searchCrit.getValue() + ")");
             } else if (RequestExternalAction.SEARCH_BY_COMPLEMENTARY_DATA.equals(searchCrit.getAttribut())) {
                 sb.append(" and id in (select id from request_external_action_complementary_data where key = ? and value ")
                     .append(searchCrit.getSqlComparatif()).append(" ?)");
                 Map.Entry<String, Serializable> entry = (Map.Entry<String, Serializable>)searchCrit.getValue();
                 parametersValues.add(entry.getKey());
                 parametersTypes.add(Hibernate.STRING);
                 parametersValues.add(entry.getValue());
                 parametersTypes.add(Hibernate.SERIALIZABLE);
             } else if (RequestExternalAction.SEARCH_BY_STATUS.equals(searchCrit.getAttribut())) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Set<RequestExternalAction.Status> statuses =
                         (Set<RequestExternalAction.Status>) searchCrit.getValue();
                     String[] values = new String[statuses.size()];
                     int i = 0;
                     for (RequestExternalAction.Status status : statuses) {
                         values[i++] = "'" + status.name() + "'";
                     }
                     sb.append(" and ").append(searchCrit.getAttribut()).append(" ").append(searchCrit.getComparatif())
                             .append(" (").append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and ").append(searchCrit.getAttribut()).append(searchCrit.getSqlComparatif()).append(" ?");
                     parametersValues.add(RequestExternalAction.Status.forString(searchCrit.getValue().toString()).name());
                     parametersTypes.add(Hibernate.STRING);
                 }
             } else {
                 sb.append(" and ").append(searchCrit.getAttribut()).append(searchCrit.getSqlComparatif()).append(" ?");
                 if (RequestExternalAction.SEARCH_BY_DATE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getDateValue());
                     parametersTypes.add(Hibernate.TIMESTAMP);
                 } else if (RequestExternalAction.SEARCH_BY_ID.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY_OWNER.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_MESSAGE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_NAME.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 }
             }
         }
         Query query = HibernateUtil.getSession().createSQLQuery(sb.toString());
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
         return query.list();
     }
 
     @Override
     public Long getCount(Set<Critere> criteriaSet, boolean lastOnly) {
         StringBuffer sb = new StringBuffer();
         sb.append("select count(*) from request_external_action");
         if (lastOnly) {
             List<BigInteger> ids = HibernateUtil.getSession()
                 .createSQLQuery("select max(id) from request_external_action group by key").list();
             if (ids.isEmpty()) {
                 return 0L;
             }
             String stringIds[] = new String[ids.size()];
             int i = 0;
             for (BigInteger id : ids)
                 stringIds[i++] = id.toString();
             sb.append(" where id in (");
             sb.append(StringUtils.join(stringIds, ", "));
             sb.append(')');
         } else {
             sb.append(" where 1 = 1 ");
         }
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
         for (Critere searchCrit : criteriaSet) {
             if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(
                     " and (select category_id from request_type rt where rt.id = (select request_type_id from request r where r.id = key)) in ( "
                     + searchCrit.getValue() + ")");
             } else if ("homeFolderId".equals(searchCrit.getAttribut())) {
                 sb.append(" and (select home_folder_id from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             } else if (RequestExternalAction.SEARCH_BY_REQUEST_TYPE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select request_type_id from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             } else if (RequestExternalAction.SEARCH_BY_REQUEST_STATE.equals(searchCrit.getAttribut())) {
                 sb.append(" and (select state from request where id = key) ")
                     .append(searchCrit.getSqlComparatif()).append(" ?");
                parametersValues.add(((RequestState)searchCrit.getValue()).name());
                 parametersTypes.add(Hibernate.STRING);
             } else if (RequestExternalAction.SEARCH_BY_COMPLEMENTARY_DATA.equals(searchCrit.getAttribut())) {
                 sb.append(" and id in (select id from request_external_action_complementary_data where key = ? and value ")
                     .append(searchCrit.getSqlComparatif()).append(" ?)");
                 @SuppressWarnings("unchecked") Map.Entry<String, Serializable> entry =
                     (Map.Entry<String, Serializable>)searchCrit.getValue();
                 parametersValues.add(entry.getKey());
                 parametersTypes.add(Hibernate.STRING);
                 parametersValues.add(entry.getValue());
                 parametersTypes.add(Hibernate.SERIALIZABLE);
             } else if (RequestExternalAction.SEARCH_BY_STATUS.equals(searchCrit.getAttribut())) {
                     if (Critere.IN.equals(searchCrit.getComparatif())) {
                         Set<RequestExternalAction.Status> statuses =
                             (Set<RequestExternalAction.Status>) searchCrit.getValue();
                         String[] values = new String[statuses.size()];
                         int i = 0;
                         for (RequestExternalAction.Status status : statuses) {
                             values[i++] = "'" + status.name() + "'";
                         }
                         sb.append(" and ").append(searchCrit.getAttribut()).append(" ").append(searchCrit.getComparatif())
                                 .append(" (").append(StringUtils.join(values, ", ")).append(')');
                     } else {
                         sb.append(" and ").append(searchCrit.getAttribut()).append(searchCrit.getSqlComparatif()).append(" ?");
                         parametersValues.add(RequestExternalAction.Status.forString(searchCrit.getValue().toString()).name());
                         parametersTypes.add(Hibernate.STRING);
                     }
             } else {
                 sb.append(" and ").append(searchCrit.getAttribut()).append(searchCrit.getSqlComparatif()).append(" ?");
                 if (RequestExternalAction.SEARCH_BY_DATE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getDateValue());
                     parametersTypes.add(Hibernate.TIMESTAMP);
                 } else if (RequestExternalAction.SEARCH_BY_ID.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 } else if (RequestExternalAction.SEARCH_BY_KEY_OWNER.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_MESSAGE.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 } else if (RequestExternalAction.SEARCH_BY_NAME.equals(searchCrit.getAttribut())) {
                     parametersValues.add(searchCrit.getSqlStringValue());
                     parametersTypes.add(Hibernate.STRING);
                 }
             }
         }
         Query query = HibernateUtil.getSession().createSQLQuery(sb.toString());
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
         return ((BigInteger)query.uniqueResult()).longValue();
     }
 
     @Override
     public List<Long> getRequestsWithoutExternalAction(Long requestTypeId, String externalServiceLabel) {
         return HibernateUtil.getSession().createQuery(
             "select id from RequestData r where r.requestType.id = :rt and state in (:complete, :validated, :notified) and (select count(*) from RequestExternalAction where name = :name and r.id = key) = 0")
                 .setLong("rt", requestTypeId)
                 .setString("complete", RequestState.COMPLETE.name())
                 .setString("validated", RequestState.VALIDATED.name())
                 .setString("notified", RequestState.NOTIFIED.name())
                 .setString("name", externalServiceLabel).list();
     }
 }
