 package fr.cg95.cvq.dao.request.hibernate;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * Hibernate implementation of the {@link IRequestDAO} interface.
  * 
  * @author bor@zenexity.fr
  */
 public class RequestDAO extends GenericDAO implements IRequestDAO {
 
     public List<Request> search(final Set<Critere> criteria, final String sort, String dir, 
             int recordsReturned, int startIndex) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from Request as request").append(" where 1 = 1 ");
 
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
         
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_ID)) {
                 sb.append(" and request.id " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and request.homeFolderId " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(request.requesterLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_LASTNAME)) {
                 sb.append(" and lower(request.subjectLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_ID)) {
                 sb.append(" and request.subjectId " + searchCrit.getSqlComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_NAME)) {
                 sb.append(" and request.requestType.category.name "
                         + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getValue());
                 parametersTypes.add(Hibernate.STRING);
             
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_ID)) {
                 sb.append(" and request.requestType.category.id "
                         + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_ID)) {
                 sb.append(" and request.requestType " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_LABEL)) {
                 sb.append(" and request.requestType.label " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_STATE)) {
                 sb.append(" and request.state " + searchCrit.getComparatif() + " ?");
                 // To ensure we put the good type in the object list
                 // FIXME : all states criteria should be sent as RequestState objects
                 if (searchCrit.getValue() instanceof RequestState)
                     parametersValues.add(searchCrit.getValue().toString());
                 else
                     parametersValues.add(searchCrit.getValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CREATION_DATE)) {
                 sb.append(" and request.creationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.TIMESTAMP);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE)) {
                 sb.append(" and request.lastModificationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.DATE);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_INTERVENING_AGENT_ID)) {
                 sb.append(" and request.lastInterveningAgentId " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(" and request.requestType.category.id in ( "
                         + searchCrit.getValue() + ")");
             } else if(searchCrit.getAttribut().equals(Request.DRAFT)) {
                 sb.append(prepareDraftQuery(parametersValues,parametersTypes,searchCrit));
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_QUALITY_TYPE)) {
                  
                 if (searchCrit.getValue().equals(Request.QUALITY_TYPE_ORANGE)) {
                  sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_RED)) {
                     sb.append(" and request.orangeAlert = false")
                     .append(" and request.redAlert = true");
                 }
             }
         }
         
         this.processDraft(sb,criteria);
         
         if (sort != null) {
             if (sort.equals(Request.SEARCH_BY_REQUEST_ID))
                 sb.append(" order by request.id");
             else if (sort.equals(Request.SEARCH_BY_HOME_FOLDER_ID))
                 sb.append(" order by request.homeFolderId");
             else if (sort.equals(Request.SEARCH_BY_REQUESTER_LASTNAME))
                 sb.append(" order by request.requesterLastName, request.requesterFirstName");
             else if (sort.equals(Request.SEARCH_BY_SUBJECT_LASTNAME))
                 sb.append(" order by request.subjectLastName, request.subjectFirstName");
             else if (sort.equals(Request.SEARCH_BY_CATEGORY_NAME))
                 sb.append(" order by request.requestType.category.name");
             else if (sort.equals(Request.SEARCH_BY_CREATION_DATE))
                 sb.append(" order by request.creationDate");
             else if (sort.equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE))
                 sb.append(" order by request.lastModificationDate");
             else if (sort.equals(Request.SEARCH_BY_LAST_INTERVENING_AGENT_ID))
                 sb.append(" order by request.lastInterveningAgentId");
             else
                 sb.append(" order by request.id");
         } else {
             // default sort order
             sb.append(" order by request.id");
         }
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
         
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
         
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         
         return query.list(); 
     }
 
     /**
      * A customized search method for cases where we just want the requests
      * count. Request is "manually" generated in order to do a single request in
      * DB, which is a <b>lot</b> more performant than Hibernate generated
      * queries
      */
     protected Long searchCount(final Set<Critere> criteria) {
 
         StringBuffer sbSelect = new StringBuffer();
         sbSelect.append("select count(*) from Request as request");
 
         StringBuffer sb = new StringBuffer(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
         
         boolean joinedWithRequestAction = false;
         
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_ID)) {
                 sb.append(" and request.id " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and request.homeFolderId " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(request.requesterLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_LASTNAME)) {
                 sb.append(" and lower(request.subjectLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_ID)) {
                 sb.append(" and request.subjectId " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_STATE)) {
                 sb.append(" and state " + searchCrit.getComparatif() + " ?");
                 // To ensure we put the good type in the object list
                 // FIXME : all states criteria should be sent as
                 // RequestState objects
                 if (searchCrit.getValue() instanceof RequestState)
                     objectList.add(searchCrit.getValue().toString());
                 else
                     objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_ID)) {
                 sb.append(" and request.requestType.category.id "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_NAME)) {
                 sb.append(" and request.requestType.category.name "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_ID)) {
                 sb.append(" and request.requestType " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_LABEL)) {
                 sb.append(" and request.requestType.label " + searchCrit.getComparatif()
                         + " ?");
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_RESULTING_STATE)) {
                 if (!joinedWithRequestAction)
                     sb.append(" and request.id = requestAction.request");
                 
                 sb.append(" and requestAction.resultingState ")
                     .append(searchCrit.getComparatif()).append(" ?");
 
                 if (!joinedWithRequestAction)
                     sbSelect.append(", RequestAction as requestAction");
                 
                 joinedWithRequestAction = true;
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CREATION_DATE)) {
                 sb.append(" and request.creationDate " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE)) {
                 sb.append(" and request.lastModificationDate " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_INTERVENING_AGENT_ID)) {
                 sb.append(" and request.lastInterveningAgentId "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_MODIFICATION_DATE)) {
                 if (!joinedWithRequestAction)
                     sb.append(" and request.id = requestAction.request");
 
                 sb.append(" and requestAction.date ")
                     .append(searchCrit.getComparatif()).append(" ? ");
 
                 if (!joinedWithRequestAction)
                     sbSelect.append(", RequestAction as requestAction");
 
                 joinedWithRequestAction = true;
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(" and request.requestType.category.id in ( "
                         + searchCrit.getValue() + ")");
             } 
             else if(searchCrit.getAttribut().equals(Request.DRAFT)) {
                 sb.append(prepareDraftQuery(objectList,typeList,searchCrit));
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_QUALITY_TYPE)) {
                  
                 if (searchCrit.getValue().equals(Request.QUALITY_TYPE_ORANGE)) {
                  sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_RED)) {
                     sb.append(" and request.orangeAlert = false")
                     .append(" and request.redAlert = true");
                 }
             }
         }
         
         this.processDraft(sb,criteria);
         sbSelect.append(sb);
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return (Long) HibernateUtil.getSession()
             .createQuery(sbSelect.toString())
             .setParameters(objectTab, typeTab)
             .iterate().next(); 
     }
 
     public Long count(final Set<Critere> criteria) {
         return searchCount(criteria).longValue();
     }
 
     public Long countByQuality(final Date startDate, final Date endDate,
             final List<String> resultingStates, final String qualityType, final Long requestTypeId,
             final Long categoryId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("select distinct(request.id) from Request request join request.actions action")
         .append(" where 1 = 1");
 
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
 
         if (categoryId != null) {
             sb.append(" and request.requestType.category.id = '").append(categoryId)
                 .append("'");
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
 
         if (requestTypeId != null) {
             sb.append(" and request.requestType.id = '").append(requestTypeId).append("'");
         }
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         
         return Long.valueOf(HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list().size());
     }
 
     public Long countByResultingState(final String[] resultingState, final Date startDate, final Date endDate,
             final Long requestTypeId, final Long categoryId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
 
         sb.append("select distinct(request.id) from Request request join request.actions action")
             .append(" where 1 = 1");
 
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
 
         if (categoryId != null) {
             sb.append(" and request.requestType.category.id = '").append(categoryId)
                 .append("'");
         }
 
         if (resultingState != null && resultingState.length > 0) {
             sb.append(" and (");
             for (int i = 0; i < resultingState.length; i++) {
                 String state = resultingState[i];
                 sb.append(" action.resultingState = '").append(state).append("'");
                 if (i < (resultingState.length - 1))
                     sb.append(" or ");
             }
             sb.append(")");
         }
         
         if (requestTypeId != null) {
             sb.append(" and request.requestType.id = '").append(requestTypeId).append("'");
         }
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         return Long.valueOf(HibernateUtil.getSession()
                 .createQuery(sb.toString())
                 .setParameters(objectTab, typeTab).list().size());
     }
 
     public List<Request> listByRequester(final Long requesterId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request ")
             .append("where request.requesterId = ?");
         objectList.add(requesterId);
         typeList.add(Hibernate.LONG);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listBySubject(final Long subjectId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request ")
             .append("where request.subjectId = ?");
         objectList.add(subjectId);
         typeList.add(Hibernate.LONG);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listBySubjectAndLabel(Long subjectId, String label, RequestState[] excludedStates) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request");
 
         sb.append(" where request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         sb.append(" and request.subjectId = ?");
         objectList.add(subjectId);
         typeList.add(Hibernate.LONG);
         
         if (excludedStates != null && excludedStates.length > 0) {
             for (int i = 0; i < excludedStates.length; i++) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedStates[i].toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listByHomeFolder(final Long homeFolderId) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request ")
             .append("where request.homeFolderId = ?");
 
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listByHomeFolderAndLabel(final Long homeFolderId, final String label,
             final RequestState[] excludedStates) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request");
 
         sb.append(" where request.homeFolderId = ?");
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         if (excludedStates != null && excludedStates.length > 0) {
             for (int i = 0; i < excludedStates.length; i++) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedStates[i].toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
 
     public List<Request> listByHomeFolderAndSeason(Long homeFolderId, String seasonUuid) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request");
 
         sb.append(" where request.homeFolderId = ?");
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and request.seasonUuid = ?");
         objectList.add(seasonUuid);
         typeList.add(Hibernate.STRING);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
 
     public List<Request> listByStateAndSeason(RequestState requestState, String seasonUuid) {
         
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from Request as request");
 
         sb.append(" where request.state = ?");
         objectList.add(requestState.toString());
         typeList.add(Hibernate.STRING);
 
         sb.append(" and request.seasonUuid = ?");
         objectList.add(seasonUuid);
         typeList.add(Hibernate.STRING);
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listByStates(final Set<RequestState> states) {
 
         return listByStatesAndType(states, null);
     }
 
     public List<Request> listByStatesAndType(final Set<RequestState> states, 
             final String requestTypeLabel) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("from Request as request ");
 
         boolean firstStatement = true;
         for (RequestState requestState : states) {
             if (firstStatement) {
                 sb.append("where request.state = ? ");
                 firstStatement = false;
             } else {
                 sb.append("or request.state = ? ");
             }
 
             objectList.add(requestState.toString());
             typeList.add(Hibernate.STRING);
         }
 
         if (requestTypeLabel != null) {
             sb.append("and request.requestType.label = ? ");
             objectList.add(requestTypeLabel);
             typeList.add(Hibernate.STRING);
         }
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Request> listByNotMatchingActionLabel(final String actionLabel) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from Request as request ").append("where request.id not in (");
        sb.append("select request.id from Request request join request.actions action ")
            .append(" where action.label = '").append(actionLabel).append("'");
         sb.append(")");
 
         return HibernateUtil.getSession().createQuery(sb.toString()).list();
     }
     
     public Long getSubjectId(Long requestId) {
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("select r.subjectId from Request as r ").append("where r.id = ?");
         
         objectList.add(requestId);
         typeList.add(Hibernate.LONG);
 
         Type[] typeTab = typeList.toArray(new Type[1]);
         Object[] objectTab = objectList.toArray(new Object[1]);
         
         return (Long)HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .uniqueResult();
     }
     
     public List<Long> listHomeFolderSubjectIds(Long homeFolderId, String label, 
                                               RequestState[] excludedStates) {
         
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer()
             .append("select request.subjectId from Request as request");
 
         sb.append(" where request.homeFolderId = ?");
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         if (excludedStates != null && excludedStates.length > 0) {
             for (RequestState excludedState : excludedStates) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedState.toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Type[] typeTab = typeList.toArray(new Type[1]);
         Object[] objectTab = objectList.toArray(new Object[1]);
 
         //noinspection unchecked
         return (List<Long>)HibernateUtil.getSession().createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
     }
     
     public List<Request> listDraftedByNotificationAndDate(String actionLabel, Date date) {
         
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
         
         StringBuffer sb = new StringBuffer();
         sb.append("select r from Request r left join r.actions a");
         sb.append(" where r.draft = true");
         sb.append(" and r.creationDate <= ?");
         sb.append(" and (a.label != ?  or a.id = null) ");
         
         typeList.add(Hibernate.TIMESTAMP);
         typeList.add(Hibernate.STRING);
         
         objectList.add(date);
         objectList.add(actionLabel);
         
         Type[] typeTab = typeList.toArray(new Type[1]);
         Object[] objectTab = objectList.toArray(new Object[1]);
         
         //noinspection unchecked
         List<Request> result = HibernateUtil.getSession()
             .createQuery(sb.toString()).setParameters(objectTab, typeTab).list();
         
         return result;
     }
     
     protected StringBuffer processDraft(StringBuffer sb, Set<Critere> criterias) {
         if(!this.existsCriteriaName(Request.DRAFT,criterias)) {
             sb.append(" and (request.draft = false or request.draft is null) ");
         }
         return sb;
     }
     
     protected String prepareDraftQuery(List<Object> values,List<Type> types,Critere crit) {
         String result = "";
         if(crit.getValue() instanceof List) {
             for(Object o : (List) crit.getValue()) {
                 if(o != null) {
                     result += " request.draft "+ crit.getComparatif() + " ? or ";
                     values.add(o);
                     types.add(Hibernate.BOOLEAN);
                 }
                 else result += " request.draft is null or";
             }
             if(result.contains("or")) { 
                 result = result.substring(0, result.length()-2);
                 result = String.format(" and ( %1$s )",result);
             }
         } else {
             result = " and request.draft " + crit.getComparatif() + " ?";
             values.add(crit.getValue());
             types.add(Hibernate.BOOLEAN);
         }
         return result;
     }
     
     
     protected boolean existsCriteriaName(String name, Set<Critere> criterias) {
         for(Critere c : criterias) {
             if(c.getAttribut().equals(name)) return true;
         }
         return false;
     }
 
     /*
      * Hacked method to bypass Hibernate mapping 'one class per subclass' strategy 
      * performance limitations.
      * - we create a 'clazz_' column initialized with value 5 in resultSet
      * - we ask Hibernate to transform the resultSet in 'VoCardRequest' object 
      * (to transport result in Capdemat)
      * 
      * note : Hibernate can instanciate a 'VoCardRequestObject' object 
      * from the added 'clazz_' information
      */
     private Request nativeSqlFindById(final Long id) {
         Object request = HibernateUtil.getSession()
              .createSQLQuery(
                      "SELECT id," 
                          +"creation_date," 
                          +"last_modification_date," 
                          +"requester_id," 
                          +"request_type_id," 
                          +"state," 
                          +"request_step," 
                          +"home_folder_id," 
                          +"data_state," 
                          +"last_intervening_agent_id," 
                          +"orange_alert," 
                          +"red_alert," 
                          +"validation_date," 
                          +"subject_table_name," 
                          +"subject_id," 
                          +"season_uuid," 
                          +"means_of_contact_id,"
                          +"5 AS clazz_"
                      +" FROM request WHERE id=" + id)
              .addEntity(VoCardRequest.class)
              .uniqueResult();
         
          return (Request)request;
      }
 }
