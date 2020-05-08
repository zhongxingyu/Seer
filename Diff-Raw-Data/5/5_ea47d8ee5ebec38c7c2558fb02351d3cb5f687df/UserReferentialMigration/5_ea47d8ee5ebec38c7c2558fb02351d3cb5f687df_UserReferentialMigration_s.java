 package fr.cg95.cvq.util.admin;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.transform.Transformers;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 import fr.cg95.cvq.business.QoS;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequestData;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequestData;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.authority.impl.LocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.JSONUtils;
 import fr.cg95.cvq.util.UserUtils;
 
 public class UserReferentialMigration {
 
     private LocalAuthorityRegistry localAuthorityRegistry;
 
     private IRequestSearchService requestSearchService;
 
     private IUserSearchService userSearchService;
 
     private CustomDAO customDAO = new CustomDAO();
 
     private Set<Long> archived = new HashSet<Long>();
 
     private Map<Long, UserState> states = new HashMap<Long, UserState>();
 
     private Map<Long, List<Long>> creationActions = new HashMap<Long, List<Long>>();
 
     private Map<Long, Date> creationNotifications = new HashMap<Long, Date>();
 
     private Comparator<UserAction> comparator = new Comparator<UserAction>() {
         @Override
         public int compare(UserAction o1, UserAction o2) {
             return o1.getDate().compareTo(o2.getDate());
         }
     };
 
     public static void main(final String[] args) {
         ClassPathXmlApplicationContext context = SpringApplicationContextLoader.loadContext(null);
         UserReferentialMigration userReferentialMigration = new UserReferentialMigration();
         userReferentialMigration.localAuthorityRegistry = (LocalAuthorityRegistry)context.getBean("localAuthorityRegistry");
         userReferentialMigration.requestSearchService = (IRequestSearchService)context.getBean("requestSearchService");
         userReferentialMigration.userSearchService = (IUserSearchService)context.getBean("userSearchService");
         userReferentialMigration.localAuthorityRegistry.browseAndCallback(userReferentialMigration, "migrate", new Object[0]);
         System.exit(0);
     }
 
     public void migrate()
         throws CvqException {
         for (DTO dto : (List<DTO>)HibernateUtil.getSession().createSQLQuery(
             "select r.requester_id as requesterId, r.home_folder_id as homeFolderId, r.creation_date as date, i.id as individualId from individual i, request r, history_entry he where r.id = he.request_id and i.id = he.object_id and property = 'homeFolder' and old_value is not null and new_value is null")
                 .addScalar("requesterId").addScalar("homeFolderId").addScalar("date")
                 .addScalar("individualId").setResultTransformer(Transformers.aliasToBean(DTO.class))
                 .list()) {
             Individual i = userSearchService.getById(dto.individualId);
             HomeFolder homeFolder = userSearchService.getHomeFolderById(dto.homeFolderId);
             i.setHomeFolder(homeFolder);
             homeFolder.getIndividuals().add(i);
             i.setState(UserState.ARCHIVED);
             archived.add(i.getId());
             RequestAction fake = new RequestAction();
             fake.setDate(dto.date);
             fake.setAgentId(dto.requesterId);
             JsonObject payload = new JsonObject();
             payload.addProperty("state", UserState.ARCHIVED.toString());
             add(homeFolder, new UserAction(UserAction.Type.STATE_CHANGE, dto.individualId, payload), fake);
         }
         for (HomeFolder homeFolder : customDAO.all(HomeFolder.class)) {
             Set<Critere> criterias = new HashSet<Critere>();
             criterias.add(new Critere(Request.SEARCH_BY_HOME_FOLDER_ID, homeFolder.getId(), Critere.EQUALS));
            List<Request> requests = requestSearchService.get(criterias, null, null, 0, 0, true);
            Request first = requests.get(requests.size() - 1);
             if (homeFolder.isTemporary()) {
                 List<RequestAction> actions = new ArrayList<RequestAction>(first.getActions());
                 Collections.reverse(actions);
                 for (RequestAction requestAction : actions) {
                     convert(requestAction);
                 }
             } else {
                 for (RequestAction requestAction :
                     (List<RequestAction>)HibernateUtil.getSession().createSQLQuery(
                         "select * from request_action where request_id in (select id from request where home_folder_id = :homeFolderId and specific_data_class='fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequestData') or request_id = :firstId order by date asc")
                         .addEntity(RequestAction.class).setLong("homeFolderId", homeFolder.getId())
                         .setLong("firstId", first.getId()).list()) {
                     convert(requestAction);
                 }
             }
         }
         HibernateUtil.getSession().flush();
         for (HomeFolder homeFolder : customDAO.all(HomeFolder.class)) {
             if (!customDAO.hasCreationAction(homeFolder.getId()))
                 createFakeCreationAction(homeFolder, homeFolder.getId());
             for (Individual i : homeFolder.getIndividuals())
                 if (!customDAO.hasCreationAction(i.getId()))
                     createFakeCreationAction(homeFolder, i.getId());
         }
         HibernateUtil.getSession().flush();
         for (Map.Entry<Long, Date> creationNotification : creationNotifications.entrySet()) {
             for (Long id : creationActions.get(creationNotification.getKey())) {
                 UserAction action = (UserAction) customDAO.findById(UserAction.class, id);
                 JsonObject payload = JSONUtils.deserialize(action.getData());
                 payload.addProperty("notificationDate", creationNotification.getValue().toString());
                 action.setData(new Gson().toJson(payload));
                 customDAO.update(action);
             }
         }
         for (Long id : archived) {
             states.put(id, UserState.ARCHIVED);
         }
         for (Map.Entry<Long, UserState> state : states.entrySet()) {
             try {
                 Individual i = userSearchService.getById(state.getKey());
                 i.setState(state.getValue());
                 customDAO.update(i);
             } catch (CvqObjectNotFoundException e) {
                 HomeFolder homeFolder = userSearchService.getHomeFolderById(state.getKey());
                 homeFolder.setState(state.getValue());
                 customDAO.update(homeFolder);
             }
         }
         for (Adult external : customDAO.findBySimpleProperty(Adult.class, "homeFolder", null)) {
             HomeFolder homeFolder = null;
             for (IndividualRole role : (List<IndividualRole>) HibernateUtil.getSession()
                 .createSQLQuery("select * from individual_role where owner_id = :id")
                 .addEntity(IndividualRole.class).setLong("id", external.getId()).list()) {
                 if (role.getHomeFolderId() != null) {
                     try {
                         homeFolder = userSearchService.getHomeFolderById(role.getHomeFolderId());
                     } catch (CvqObjectNotFoundException e) {
                         // what a wonderful model
                     }
                 } else if (role.getIndividualId() != null) {
                     try {
                         homeFolder = userSearchService.getById(role.getIndividualId()).getHomeFolder();
                     } catch (CvqObjectNotFoundException e) {
                         // what a wonderful model
                     }
                 }
                 if (homeFolder != null) {
                     createFakeCreationAction(homeFolder, external.getId());
                     break;
                 }
             }
         }
     }
 
     private void convert(RequestAction requestAction)
         throws CvqObjectNotFoundException {
         Long requestId = ((BigInteger)HibernateUtil.getSession().createSQLQuery(
             "select request_id from request_action where id = :id")
             .setLong("id", requestAction.getId()).uniqueResult()).longValue();
         Request request = requestSearchService.getById(requestId, true);
         HomeFolder homeFolder = userSearchService.getHomeFolderById(request.getHomeFolderId());
         if (RequestActionType.CREATION.equals(requestAction.getType())) {
             UserAction.Type type =
                 HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass()) ?
                     UserAction.Type.MODIFICATION : UserAction.Type.CREATION;
             List<Long> actionIds = new ArrayList<Long>();
             actionIds.add(add(homeFolder, new UserAction(type, homeFolder.getId()), requestAction));
             states.put(homeFolder.getId(),
                 HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass()) ?
                     UserState.MODIFIED : UserState.NEW);
             for (Individual individual : homeFolder.getIndividuals()) {
                 if (isConcerned(individual, request)) {
                     individual.setLastModificationDate(requestAction.getDate());
                     actionIds.add(add(homeFolder, new UserAction(
                         UserState.NEW.equals(individual.getState()) ? UserAction.Type.CREATION : type,
                         individual.getId()), requestAction));
                     states.put(individual.getId(),
                         HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass())
                             && !UserState.NEW.equals(individual.getState()) ? UserState.MODIFIED : UserState.NEW);
                 }
             }
             creationActions.put(requestId, actionIds);
         } else if (RequestActionType.STATE_CHANGE.equals(requestAction.getType())) {
             UserState state = convert(requestAction.getResultingState(),
                 HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass()));
             if (state == null || state.equals(states.get(homeFolder.getId()))
                 || (UserState.ARCHIVED.equals(state) && !homeFolder.isTemporary()))
                 return;
             states.put(homeFolder.getId(), state);
             JsonObject payload = new JsonObject();
             payload.addProperty("state", state.toString());
             add(homeFolder, new UserAction(UserAction.Type.STATE_CHANGE, homeFolder.getId(), payload), requestAction);
             for (Individual individual : homeFolder.getIndividuals()) {
                 if (isConcerned(individual, request) && !state.equals(states.get(individual.getId()))) {
                     individual.setLastModificationDate(requestAction.getDate());
                     add(homeFolder, new UserAction(UserAction.Type.STATE_CHANGE, individual.getId(), payload), requestAction);
                     states.put(individual.getId(), state);
                 }
             }
         } else if (RequestActionType.CONTACT_CITIZEN.equals(requestAction.getType())
             && (VoCardRequestData.class.equals(request.getRequestData().getSpecificDataClass())
                 || HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass()))) {
             JsonObject payload = new JsonObject();
             JsonObject contact = new JsonObject();
             if (requestAction.getFile() != null) {
                 contact.addProperty("file", new Gson().toJson(requestAction.getFile()));
             }
             if (requestAction.getFilename() != null) {
                 contact.addProperty("filename", new Gson().toJson(requestAction.getFilename()));
             }
             contact.addProperty("message", requestAction.getMessage());
             payload.add("contact", contact);
             UserAction action = new UserAction(UserAction.Type.CONTACT, userSearchService.getHomeFolderResponsible(homeFolder.getId()).getId(), payload);
             action.setNote(requestAction.getNote());
             add(homeFolder, action, requestAction);
         } else if (RequestActionType.ORANGE_ALERT_NOTIFICATION.equals(requestAction.getType())
             || RequestActionType.RED_ALERT_NOTIFICATION.equals(requestAction.getType())) {
             JsonObject payload = new JsonObject();
             payload.addProperty("quality",
                 RequestActionType.ORANGE_ALERT_NOTIFICATION.equals(requestAction.getType()) ?
                     QoS.URGENT.toString() : QoS.LATE.toString());
             payload.addProperty("notified", true);
             add(homeFolder, new UserAction(UserAction.Type.QoS, homeFolder.getId(), payload), requestAction);
             for (Individual individual : homeFolder.getIndividuals()) {
                 if (isConcerned(individual, request))
                     add(homeFolder, new UserAction(UserAction.Type.QoS, individual.getId(), payload), requestAction);
             }
         } else if (RequestActionType.CREATION_NOTIFICATION.equals(requestAction.getType())) {
             creationNotifications.put(requestId, requestAction.getDate());
         }
     }
 
     private Long add(HomeFolder homeFolder, UserAction userAction, RequestAction requestAction) {
         userAction.setUserId(requestAction.getAgentId());
         JsonObject payload = JSONUtils.deserialize(userAction.getData());
         payload.get("user").getAsJsonObject().addProperty("id", requestAction.getAgentId());
         payload.get("user").getAsJsonObject().addProperty("name", UserUtils.getDisplayName(requestAction.getAgentId()));
         userAction.setData(new Gson().toJson(payload));
         userAction.setDate(requestAction.getDate());
         List<UserAction> actions = new ArrayList<UserAction>(homeFolder.getActions());
         actions.add(userAction);
         Collections.sort(actions, comparator);
         homeFolder.setActions(actions);
         return customDAO.create(userAction);
     }
 
     private boolean isConcerned(Individual individual, Request request) {
         if (individual == null) return false;
         if (!ArrayUtils.contains(new RequestState[] {
                 RequestState.DRAFT, RequestState.PENDING, RequestState.UNCOMPLETE, RequestState.COMPLETE
             }, request.getState()))
             return !UserState.NEW.equals(individual.getState());
         return !HomeFolderModificationRequestData.class.equals(request.getRequestData().getSpecificDataClass())
             || UserState.MODIFIED.equals(individual.getState())
             || UserState.NEW.equals(individual.getState());
     }
 
     private void createFakeCreationAction(HomeFolder homeFolder, Long targetId) {
         Date responsibleCreation = userSearchService.getHomeFolderResponsible(homeFolder.getId()).getCreationDate();
         Date firstAction = homeFolder.getActions().isEmpty() ? null : homeFolder.getActions().get(0).getDate();
         Date homeFolderCreation =
             firstAction != null && responsibleCreation.compareTo(firstAction) > 0 ? firstAction
                 : responsibleCreation;
         RequestAction fake = new RequestAction();
         fake.setAgentId(-1L);
         fake.setDate(homeFolderCreation);
         add(homeFolder, new UserAction(UserAction.Type.CREATION, targetId), fake);
     }
 
     private UserState convert(RequestState state, boolean modification) {
         if (RequestState.ARCHIVED.equals(state)) {
             return UserState.ARCHIVED;
         } else if (RequestState.CANCELLED.equals(state)) {
             return UserState.INVALID;
         } else if (RequestState.CLOSED.equals(state)) {
             return null;
         } else if (RequestState.COMPLETE.equals(state)) {
             return null;
         } else if (RequestState.DRAFT.equals(state)) {
             return null;
         } else if (RequestState.NOTIFIED.equals(state)) {
             return UserState.VALID;
         } else if (RequestState.PENDING.equals(state)) {
             return modification ? UserState.MODIFIED : null;
         } else if (RequestState.REJECTED.equals(state)) {
             return UserState.INVALID;
         } else if (RequestState.UNCOMPLETE.equals(state)) {
             return UserState.INVALID;
         } else if (RequestState.VALIDATED.equals(state)) {
             return UserState.VALID;
         }
         return null;
     }
 
     private class CustomDAO extends GenericDAO {
         public boolean hasCreationAction(Long targetId) {
             return HibernateUtil.getSession().createCriteria(UserAction.class)
                 .add(Restrictions.eq("targetId", targetId))
                 .add(Restrictions.eq("type", UserAction.Type.CREATION)).uniqueResult() != null;
         }
     }
 
     public static class DTO {
         private Long homeFolderId;
         private Long individualId;
         private Long requesterId;
         public Date date;
         public void setHomeFolderId(BigInteger homeFolderId) {
             this.homeFolderId = homeFolderId.longValue();
         }
         public void setIndividualId(BigInteger individualId) {
             this.individualId = individualId.longValue();
         }
         public void setRequesterId(BigInteger requesterId) {
             this.requesterId = requesterId.longValue();
         }
     }
 }
