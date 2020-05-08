 package fr.cg95.cvq.service.users.impl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 
 import com.google.gson.JsonObject;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.UserAction;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.business.users.UserWorkflow;
 import fr.cg95.cvq.business.users.UserEvent;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IUserWorkflowService;
 import fr.cg95.cvq.util.translation.ITranslationService;
 
 public class UserWorkflowService implements IUserWorkflowService, ApplicationEventPublisherAware {
 
     private ApplicationEventPublisher applicationEventPublisher;
 
     private ILocalAuthorityRegistry localAuthorityRegistry;
 
     private ITranslationService translationService;
 
     private IHomeFolderService homeFolderService;
 
     private IHomeFolderDAO homeFolderDAO;
 
     private Map<String, UserWorkflow> workflows = new HashMap<String, UserWorkflow>();
 
     @Override
     public UserState[] getPossibleTransitions(UserState state) {
         return getWorkflow().getPossibleTransitions(state);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public UserState[] getPossibleTransitions(Individual user) {
         UserState[] allStates = getPossibleTransitions(user.getState());
         List<UserState> result = new ArrayList<UserState>(allStates.length);
         for (UserState state : allStates) result.add(state);
         if (homeFolderService.getHomeFolderResponsible(user.getHomeFolder().getId()).getId()
             .equals(user.getId())) {
             for (Individual i : user.getHomeFolder().getIndividuals()) {
                 if (!i.getId().equals(user.getId()) && !UserState.ARCHIVED.equals(i.getState())) {
                     result.remove(UserState.ARCHIVED);
                     break;
                 }
             }
         }
         return result.toArray(new UserState[result.size()]);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public UserState[] getPossibleTransitions(HomeFolder user) {
         return getPossibleTransitions(user.getState());
     }
 
     @Override
     public boolean isValidTransition(UserState from, UserState to) {
         return getWorkflow().isValidTransition(from, to);
     }
 
     @Override
     public UserState[] getStatesBefore(UserState state) {
         return getWorkflow().getStatesBefore(state);
     }
 
     @Override
     public UserState[] getStatesWithProperty(String propertyName) {
         return getWorkflow().getStatesWithProperty(propertyName);
     }
 
     private UserWorkflow getWorkflow() {
         String name = SecurityContext.getCurrentSite().getName();
         UserWorkflow workflow = workflows.get(name);
         if (workflow == null) {
             File file = localAuthorityRegistry.getLocalAuthorityResourceFileForLocalAuthority(name,
                 LocalAuthorityResource.Type.XML, "userWorkflow", false);
             if (file.exists()) {
                 workflow = UserWorkflow.load(file);
                 workflows.put(name, workflow);
             } else {
                 workflow = workflows.get("default");
                 if (workflow == null) {
                     file = localAuthorityRegistry.getReferentialResource(
                         LocalAuthorityResource.Type.XML, "userWorkflow");
                     workflow = UserWorkflow.load(file);
                     workflows.put("default", workflow);
                 }
             }
         }
         return workflow;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void changeState(HomeFolder homeFolder, UserState state)
         throws CvqModelException, CvqInvalidTransitionException {
         if (!isValidTransition(homeFolder.getState(), state))
             throw new CvqInvalidTransitionException(
                 translationService.translate(
                     "user.state." + homeFolder.getState().toString().toLowerCase()),
                 translationService.translate(
                     "user.state." + state.toString().toLowerCase()));
         if (UserState.VALID.equals(state)) {
             for (Individual individual : homeFolder.getIndividuals()) {
                 if (!UserState.VALID.equals(individual.getState())
                     && !UserState.ARCHIVED.equals(individual.getState()))
                     throw new CvqModelException("");
             }
         }
         homeFolder.setState(state);
         JsonObject payload = new JsonObject();
         payload.addProperty("state", state.toString());
         UserAction action = new UserAction(UserAction.Type.STATE_CHANGE, homeFolder.getId(), payload);
         homeFolder.getActions().add(action);
         homeFolderDAO.update(homeFolder);
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void changeState(Individual individual, UserState state)
         throws CvqModelException, CvqInvalidTransitionException {
         if (!isValidTransition(individual.getState(), state))
             throw new CvqInvalidTransitionException(
                 translationService.translate(
                     "user.state." + individual.getState().toString().toLowerCase()),
                 translationService.translate(
                     "user.state." + state.toString().toLowerCase()));
         individual.setState(state);
         individual.setLastModificationDate(new Date());
         if (SecurityContext.isBackOfficeContext()) {
             individual.setQoS(null);
         }
         HomeFolder homeFolder = individual.getHomeFolder();
         if (UserState.ARCHIVED.equals(state) && individual.getId().equals(
             homeFolderService.getHomeFolderResponsible(homeFolder.getId()).getId())) {
             for (Individual i : homeFolder.getIndividuals()) {
                 if (!UserState.ARCHIVED.equals(i.getState()))
                     throw new CvqModelException("user.state.error.mustArchiveResponsibleLast");
             }
         }
         JsonObject payload = new JsonObject();
         payload.addProperty("state", state.toString());
         UserAction action = new UserAction(UserAction.Type.STATE_CHANGE, individual.getId(), payload);
         individual.getHomeFolder().getActions().add(action);
         homeFolderDAO.update(individual.getHomeFolder());
         applicationEventPublisher.publishEvent(new UserEvent(this, action));
         if (UserState.INVALID.equals(state) && !UserState.INVALID.equals(homeFolder.getState()))
             changeState(individual.getHomeFolder(), UserState.INVALID);
         else if (UserState.VALID.equals(state) || UserState.ARCHIVED.equals(state)) {
             UserState homeFolderState = state;
             for (Individual i : individual.getHomeFolder().getIndividuals()) {
                 if (UserState.VALID.equals(i.getState())) {
                     homeFolderState = UserState.VALID;
                 } else if (!UserState.ARCHIVED.equals(i.getState())) {
                     homeFolderState = null;
                     break;
                 }
             }
            if (homeFolderState != null
                    && homeFolderState.equals(UserState.VALID)
                    && !UserState.VALID.equals(individual.getHomeFolder().getState()))
                changeState(individual.getHomeFolder(), homeFolderState);
         }
     }
 
     @Override
     public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;
     }
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setHomeFolderDAO(IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 }
