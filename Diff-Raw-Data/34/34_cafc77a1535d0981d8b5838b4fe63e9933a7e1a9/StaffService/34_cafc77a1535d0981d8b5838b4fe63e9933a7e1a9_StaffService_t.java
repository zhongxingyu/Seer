 package org.motechproject.ghana.national.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.StaffType;
 import org.motechproject.ghana.national.repository.AllStaffTypes;
 import org.motechproject.ghana.national.tools.StartsWithMatcher;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.MRSUser;
 import org.motechproject.mrs.services.MRSUserAdaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
 import static org.hamcrest.Matchers.equalTo;
 
 @Service
 public class StaffService {
     @Autowired
     private MRSUserAdaptor userAdaptor;
     @Autowired
     private AllStaffTypes allStaffTypes;
 
     public StaffService() {
     }
 
     public StaffService(AllStaffTypes allStaffTypes, MRSUserAdaptor userAdaptor) {
         this.allStaffTypes = allStaffTypes;
         this.userAdaptor = userAdaptor;
     }
 
     public Map saveUser(MRSUser mrsUser) throws UserAlreadyExistsException {
         return userAdaptor.saveUser(mrsUser);
     }

     public Map updateUser(MRSUser mrsUser) throws UserAlreadyExistsException {
         return userAdaptor.updateUser(mrsUser);
     }
 
     public String changePasswordByEmailId(String emailId) {
         String password = "";
         try {
             password = userAdaptor.setNewPasswordForUser(emailId);
         } catch (Exception e) {
             password = "";
         }
         return password;
     }
 
     public Map<String, String> fetchAllRoles() {
        Map<String, String> roles = new LinkedHashMap<String, String>();
        for (StaffType staffType : allStaffTypes.getAll()) {
            roles.put(staffType.name(), staffType.description());
        }
         return roles;
     }
 
     public List<MRSUser> getAllUsers() {
         return userAdaptor.getAllUsers();
     }
 
     public MRSUser getUserById(String userId) {
         return userAdaptor.getUserByUserName(userId);
     }
 
     public List<MRSUser> searchStaff(String systemId, String firstName, String middleName, String lastName, String phoneNumber, String role) {
         List<MRSUser> filteredMRSUsers = getAllUsers();
         filteredMRSUsers = filterUsers(on(MRSUser.class).getSystemId(), systemId, filteredMRSUsers);
         filteredMRSUsers = filterUsers(on(MRSUser.class).getFirstName(), firstName, filteredMRSUsers);
         filteredMRSUsers = filterUsers(on(MRSUser.class).getMiddleName(), middleName, filteredMRSUsers);
         filteredMRSUsers = filterUsers(on(MRSUser.class).getLastName(), lastName, filteredMRSUsers);
         filteredMRSUsers = filterByAttribute(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, phoneNumber, filteredMRSUsers);
         filteredMRSUsers = filterByAttribute(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, role, filteredMRSUsers);
        Collections.sort(filteredMRSUsers, new UserFirstNameComparator());
         return filteredMRSUsers;
     }
 
     private List<MRSUser> filterByAttribute(String searchAttribute, String searchCriteria, List<MRSUser> filteredMRSUsers) {
         final ArrayList<MRSUser> filteredList = new ArrayList<MRSUser>();
         for (MRSUser filteredMRSUser : filteredMRSUsers) {
             final List<Attribute> attributeSearchResults = filter(having(on(Attribute.class).name(), equalTo(searchAttribute)), filteredMRSUser.getAttributes());
             if (CollectionUtils.isNotEmpty(attributeSearchResults) && attributeSearchResults.get(0).value().startsWith(searchCriteria)) {
                 filteredList.add(filteredMRSUser);
             }
         }
         return (CollectionUtils.isEmpty(filteredList)) ? filteredMRSUsers : filteredList;
     }
 
     private List<MRSUser> filterUsers(String field, String matcher, List<MRSUser> filteredMRSUsers) {
         return (StringUtils.isNotEmpty(matcher)) ? filter(having(field, StartsWithMatcher.ignoreCaseStartsWith(matcher)), filteredMRSUsers) : filteredMRSUsers;
     }
 
     private class UserFirstNameComparator implements Comparator<MRSUser> {
         @Override
         public int compare(MRSUser user1, MRSUser user2) {
             return user2.getFirstName().compareTo(user1.getFirstName());
         }
     }
 }
