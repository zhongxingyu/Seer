 package fr.cg95.cvq.service.users.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import fr.cg95.cvq.business.QoS;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.dao.users.IAdultDAO;
 import fr.cg95.cvq.dao.users.IChildDAO;
 import fr.cg95.cvq.dao.users.IHomeFolderDAO;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.users.IUserSearchService;
 import fr.cg95.cvq.util.Critere;
 
 public class UserSearchService implements IUserSearchService {
 
     private IHomeFolderDAO homeFolderDAO;
 
     private IIndividualDAO individualDAO;
 
     private IAdultDAO adultDAO;
 
     private IChildDAO childDAO;
 
     @Override
     public List<Individual> get(Set<Critere> criterias, Map<String,String> sortParams,
         Integer max, Integer offset) {
         return individualDAO.search(criterias,sortParams,max,offset);
     }
 
     @Override
     public Integer getCount(Set<Critere> criterias) {
         return individualDAO.searchCount(criterias);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Adult> match(final Set<Critere> criteriaSet) {
 //        return adultDAO.match(criteriaSet, new UserState[] { UserState.ARCHIVED });
         return Collections.emptyList();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE},
             privilege = ContextPrivilege.READ)
     public Individual getById(Long id) {
         return individualDAO.findById(id);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE},
             privilege = ContextPrivilege.READ)
     public Adult getAdultById(Long id) {
         Individual individual = adultDAO.findById(id);
         return (individual instanceof Adult) ? (Adult) individual : null;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Child getChildById(Long id) {
         Individual individual = childDAO.findById(id);
         return (individual instanceof Child) ? (Child) individual : null;
     }
 
     @Override
     public Adult getByLogin(String login) {
         return adultDAO.findByLogin(login);
     }
 
     @Override
     public Individual getByFederationKey(final String federationKey) {
         return individualDAO.findByFederationKey(federationKey);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listTasks(QoS qoS, int max) {
         return individualDAO.listTasks(qoS, max);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Long countTasks(QoS qoS) {
         return individualDAO.countTasks(qoS);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Long countDuplicates() {
         return adultDAO.countDuplicates();
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Adult> listDuplicates(int max) {
         return adultDAO.listDuplicates(max);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<HomeFolder> getAll(boolean filterArchived, boolean filterTemporary) {
         return homeFolderDAO.listAll(filterArchived, filterTemporary);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE}, privilege = ContextPrivilege.READ)
     public final HomeFolder getHomeFolderById(final Long id) {
         return homeFolderDAO.findById(id);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<Child> getChildren(final Long homeFolderId, UserState... states) {
         return childDAO.listChildrenByHomeFolder(homeFolderId, states);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public final List<Adult> getAdults(final Long homeFolderId, UserState... states) {
         return adultDAO.listAdultsByHomeFolder(homeFolderId, states);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> getIndividuals(Long homeFolderId) {
         return individualDAO.listByHomeFolder(homeFolderId);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE}, privilege = ContextPrivilege.READ)
     public List<Individual> getExternalIndividuals(final Long homeFolderId) {
         Set<Individual> externalIndividuals = new HashSet<Individual>();
         externalIndividuals.addAll(individualDAO.listByHomeFolderRoles(homeFolderId, RoleType.values(), true));
         for (Individual individual : getIndividuals(homeFolderId))
             externalIndividuals.addAll(individualDAO.listBySubjectRoles(individual.getId(), RoleType.values(), true));
         return new ArrayList<Individual>(externalIndividuals);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Adult getHomeFolderResponsible(Long homeFolderId) {
         List<Individual> individuals =
             individualDAO.listByHomeFolderRole(homeFolderId, RoleType.HOME_FOLDER_RESPONSIBLE);
         // here we can make the assumption that we properly enforced the role
         return (Adult) individuals.get(0);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listByHomeFolderRole(Long homeFolderId, RoleType role) {
         return individualDAO.listByHomeFolderRole(homeFolderId, role);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listByHomeFolderRoles(Long homeFolderId, RoleType[] roles) {
         return individualDAO.listByHomeFolderRoles(homeFolderId, roles, false);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listBySubjectRole(Long subjectId, RoleType role) {
         return individualDAO.listBySubjectRole(subjectId, role);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<Individual> listBySubjectRoles(Long subjectId, RoleType[] roles) {
         return individualDAO.listBySubjectRoles(subjectId, roles, false);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public Set<Child> havingAsOnlyResponsible(Adult adult) {
         Set<Child> children = new HashSet<Child>();
 
         for (IndividualRole role : adult.getIndividualRoles()) {
            if(role.getIndividualId()!=null){
                List<Individual> responsibles = listBySubjectRole(role.getIndividualId(), null);
                if (responsibles.size() == 1){
                    children.add((Child)individualDAO.findById(role.getIndividualId()));}
            }
         }
 
         return children;
     }
 
     public void setHomeFolderDAO(IHomeFolderDAO homeFolderDAO) {
         this.homeFolderDAO = homeFolderDAO;
     }
 
     public void setIndividualDAO(IIndividualDAO individualDAO) {
         this.individualDAO = individualDAO;
     }
 
     public void setAdultDAO(IAdultDAO adultDAO) {
         this.adultDAO = adultDAO;
     }
 
     public void setChildDAO(IChildDAO childDAO) {
         this.childDAO = childDAO;
     }
 }
