 package fr.cg95.cvq.dao.request.hibernate;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Hibernate;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestStatisticsDAO;
 import fr.cg95.cvq.util.DateUtils;
 
 /**
  * @author bor@zenexity.fr
  */
 public class RequestStatisticsDAO  implements IRequestStatisticsDAO {
 
     public Long countByQuality(final Date startDate, final Date endDate,
             final List<RequestState> resultingStates, final String qualityType,
             final List<Long> requestTypesId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("select distinct(request.id) from RequestData request join request.actions action")
             .append(" where 1 = 1 ");
 
         if (startDate != null) {
             sb.append(" and action.date > ?");
             objectList.add(startDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
         if (endDate != null) {
             sb.append(" and action.date < ?");
             objectList.add(endDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         sb.append(" and request.requestType.id in ( ");
         for (int i = 0; i < requestTypesId.size(); i++) {
             sb.append("'").append(requestTypesId.get(i)).append("'");
             if (i != requestTypesId.size() - 1)
                 sb.append(",");
         }
         sb.substring(sb.length() - 1);
         sb.append(" )");
 
         sb.append(" and action.resultingState in (");
         for (int i = 0; i < resultingStates.size(); i++) {
            sb.append("'").append(resultingStates.get(i)).append("'");
             if (i != resultingStates.size() - 1)
                 sb.append(",");
         }
         sb.append(")");
 
         if (qualityType.equals("qualityTypeOk")) {
             sb.append(" and request.orangeAlert = false")
                 .append(" and request.redAlert = false");
         } else if (qualityType.equals("qualityTypeOrange")) {
             sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
         } else if (qualityType.equals("qualityTypeRed")) {
             sb.append(" and request.orangeAlert = false")
                 .append(" and request.redAlert = true");
         }
 
         sb.append(" and request.state != ?");
         objectList.add(RequestState.DRAFT.name());
         typeList.add(Hibernate.STRING);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         return Long.valueOf(HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list().size());
     }
 
     public Map<Long,Long> countByQualityAndType(final Date startDate, final Date endDate,
             final List<RequestState> resultingStates, final String qualityType,
             final List<Long> requestTypesId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("select request.requestType.id, count(request.id) from RequestData request")
             .append(" join request.actions action");
 
         sb.append(" where request.requestType.id in ( ");
         for (int i = 0; i < requestTypesId.size(); i++) {
             sb.append("'").append(requestTypesId.get(i)).append("'");
             if (i != requestTypesId.size() - 1)
                 sb.append(",");
         }
         sb.substring(sb.length() - 1);
         sb.append(" )");
 
         if (startDate != null) {
             sb.append(" and action.date > ?");
             objectList.add(startDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
         if (endDate != null) {
             sb.append(" and action.date < ?");
             objectList.add(endDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         sb.append(" and action.resultingState in (");
         for (int i = 0; i < resultingStates.size(); i++) {
            sb.append("'").append(resultingStates.get(i)).append("'");
             if (i != resultingStates.size() - 1)
                 sb.append(",");
         }
         sb.append(")");
 
         if (qualityType.equals("qualityTypeOk")) {
             sb.append(" and request.orangeAlert = false")
                 .append(" and request.redAlert = false");
         } else if (qualityType.equals("qualityTypeOrange")) {
             sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
         } else if (qualityType.equals("qualityTypeRed")) {
             sb.append(" and request.orangeAlert = false")
                 .append(" and request.redAlert = true");
         }
 
         sb.append(" and request.state != ?");
         objectList.add(RequestState.DRAFT.name());
         typeList.add(Hibernate.STRING);
 
         sb.append(" group by request.requestType.id");
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         List<Object[]> tempResult = HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
         Map<Long,Long> result = new HashMap<Long, Long>();
         for (Object[] object : tempResult) {
             result.put((Long) object[0], (Long) object[1]);
         }
         return result;
     }
 
     public Long countByResultingState(final String resultingState,
             final Date startDate, final Date endDate,
             final List<Long> requestTypesId) {
 
         StringBuffer sb = new StringBuffer();
 
         sb.append("select count(*) from request, request_action,")
             .append("(select request_id, max(date) as date from request_action")
             .append(" where resulting_state <> '' group by request_id) last_entries")
             .append(" where request.id=request_action.request_id")
             .append(" and request_action.request_id=last_entries.request_id")
             .append(" and request_action.date=last_entries.date");
 
         if (startDate != null) {
             sb.append(" and request_action.date > '")
                 .append(DateUtils.formatDate(startDate) + "'");
         }
         if (endDate != null) {
             sb.append(" and request_action.date < '")
                 .append(DateUtils.formatDate(endDate) + "'");
         }
 
         if (resultingState != null && !resultingState.equals("")) {
            sb.append(" and request_action.resulting_state = '").append(resultingState).append("'");
         }
 
         if (!requestTypesId.isEmpty()) {
             sb.append(" and request.request_type_id in (");
             for (int i = 0; i < requestTypesId.size(); i++) {
                 sb.append("'").append(requestTypesId.get(i)).append("'");
                 if (i != requestTypesId.size() - 1)
                     sb.append(",");
             }
             sb.substring(sb.length() - 1);
             sb.append(" )");
         }
 
         return
             ((BigInteger)HibernateUtil.getSession().createSQLQuery(sb.toString()).uniqueResult())
                 .longValue();
     }
 
     public Map<Long, Long> countByType(final Date startDate, final Date endDate,
         final List<Long> requestTypeIds) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("select request.requestType.id, count(*) from RequestData as request")
             .append(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         if (startDate != null) {
             sb.append(" and request.creationDate > ? ");
             objectList.add(startDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         if (endDate != null) {
             sb.append(" and request.creationDate < ? ");
             objectList.add(endDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         if (requestTypeIds != null && !requestTypeIds.isEmpty()) {
             sb.append(" and request.requestType.id in (");
             for (Long requestTypeId : requestTypeIds) {
                 sb.append("'").append(requestTypeId).append("',");
             }
             sb.deleteCharAt(sb.length() - 1);
             sb.append(")");
         }
 
         sb.append(" and request.state != ?");
         objectList.add(RequestState.DRAFT.name());
         typeList.add(Hibernate.STRING);
 
         sb.append(" group by request.requestType.id");
         
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         List<Object[]> tempResult =
             HibernateUtil.getSession().createQuery(sb.toString())
                 .setParameters(objectTab, typeTab).list();
         Map<Long,Long> result = new HashMap<Long, Long>();
         for (Object[] object : tempResult) {
             result.put((Long) object[0], (Long) object[1]);
         }
 
         return result;
     }
 
     public Long countByPeriod(Date startDate, Date endDate, List<Long> requestTypeIds) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("select count(*) from RequestData as request")
             .append(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         if (startDate != null) {
             sb.append(" and request.creationDate > ? ");
             objectList.add(startDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         if (endDate != null) {
             sb.append(" and request.creationDate < ? ");
             objectList.add(endDate);
             typeList.add(Hibernate.TIMESTAMP);
         }
 
         if (requestTypeIds != null && !requestTypeIds.isEmpty()) {
             sb.append(" and request.requestType.id in (");
             for (Long requestTypeId : requestTypeIds) {
                 sb.append("'").append(requestTypeId).append("',");
             }
             sb.deleteCharAt(sb.length() - 1);
             sb.append(")");
         }
 
         sb.append(" and request.state != ?");
         objectList.add(RequestState.DRAFT.name());
         typeList.add(Hibernate.STRING);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         List<Long> tempResult =
             HibernateUtil.getSession().createQuery(sb.toString())
                 .setParameters(objectTab, typeTab).list();
         return tempResult.get(0);
     }
 
     public Date getOldestRequest() {
         return (Date)HibernateUtil.getSession()
             .createQuery("select min(creationDate) from RequestData as request")
             .list().get(0);
     }
 }
