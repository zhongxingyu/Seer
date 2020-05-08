 package org.motechproject.ghana.national.web;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jdt.internal.compiler.impl.Constant;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.UserType;
 import org.motechproject.ghana.national.service.EmailTemplateService;
 import org.motechproject.ghana.national.service.UserService;
 import org.motechproject.ghana.national.web.form.CreateUserForm;
 import org.motechproject.ghana.national.web.form.SearchFacilityForm;
 import org.motechproject.ghana.national.web.form.SearchUserForm;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.User;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.util.*;
 
 @Controller
 @RequestMapping(value = "/admin/users")
 public class UserController {
     private UserService userService;
     private MessageSource messageSource;
 
     public static final String USER_ID = "userId";
     public static final String USER_NAME = "userName";
     public static final String EMAIL = "email";
     public static final String CREATE_USER_FORM = "createUserForm";
     public static final String USER_ALREADY_EXISTS = "user_email_already_exists";
     public static final String NEW_USER_VIEW = "users/new";
     public static final String CREATE_USER_SUCCESS_VIEW = "users/success";
     private EmailTemplateService emailTemplateService;
     public static final String SEARCH_USER_FORM = "searchUserForm";
     public static final String SEARCH_USER = "users/search";
 
     public UserController() {
     }
 
     @Autowired
     public UserController(UserService userService, MessageSource messageSource, EmailTemplateService emailTemplateService) {
         this.userService = userService;
         this.messageSource = messageSource;
         this.emailTemplateService = emailTemplateService;
     }
 
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newUser(ModelMap modelMap) {
         modelMap.addAttribute("createUserForm", new CreateUserForm());
         modelMap.addAttribute("roles", userService.fetchAllRoles());
         return NEW_USER_VIEW;
     }
 
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String createUser(@Valid CreateUserForm createUserForm, BindingResult bindingResult, ModelMap model) {
         User user = new User();
         user.firstName(createUserForm.getFirstName()).middleName(createUserForm.getMiddleName()).lastName(createUserForm.getLastName());
         user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_EMAIL, createUserForm.getEmail()));
         user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, createUserForm.getPhoneNumber()));
         String roleOfStaff = createUserForm.getRole();
         user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, roleOfStaff));
 
         user.securityRole(UserType.Role.securityRoleFor(roleOfStaff));
         if (UserType.Role.isAdmin(roleOfStaff)) user.id(createUserForm.getEmail());
         try {
             HashMap userData = userService.saveUser(user);
             model.put(USER_ID, userData.get("userLoginId"));
             model.put(USER_NAME, user.getFullName());
             if (roleOfStaff.equals("Super Admin") || roleOfStaff.equals("Facility Admin") || roleOfStaff.equals("CallCenter Admin"))
                 emailTemplateService.sendEmailUsingTemplates((String) userData.get("userLoginId"), (String) userData.get("password"));
         } catch (UserAlreadyExistsException e) {
             bindingResult.addError(new FieldError(CREATE_USER_FORM, EMAIL, messageSource.getMessage(USER_ALREADY_EXISTS, null, Locale.getDefault())));
             model.addAttribute("roles", userService.fetchAllRoles());
             model.mergeAttributes(bindingResult.getModel());
             return NEW_USER_VIEW;
         }
         return CREATE_USER_SUCCESS_VIEW;
     }
 
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String searchFacilityForm(ModelMap modelMap) {
         modelMap.put(SEARCH_USER_FORM, new SearchUserForm());
         modelMap.addAttribute("roles", userService.fetchAllRoles());
         return SEARCH_USER;
     }
 
     @RequestMapping(value = "searchUsers", method = RequestMethod.POST)
     public String searchUsers(@Valid final SearchUserForm searchUserForm, BindingResult bindingResult, ModelMap modelMap) {
         List<User> allUsers = userService.getAllUsers();
         List<List<String>> requestedUsers = new ArrayList<List<String>>();
         Map<Integer,String> searchFields = new HashMap<Integer, String>() {{
             put(1, searchUserForm.getStaffID());
             put(2, searchUserForm.getFirstName().toLowerCase());
             put(3, searchUserForm.getMiddleName().toLowerCase());
             put(4, searchUserForm.getLastName().toLowerCase());
             put(5, searchUserForm.getPhoneNumber());
             put(6, searchUserForm.getRole());
         }};
         int numberOfFieldsForSearch = 1;
 
         Map<Integer,String> searchFieldsCombination = new HashMap<Integer, String>();
         for (int loopCounter = 1; loopCounter <= 6; loopCounter++) {
             if (!searchFields.get(loopCounter).equals("")) {
                 searchFieldsCombination.put(loopCounter, searchFields.get(loopCounter));
                 numberOfFieldsForSearch++;
             } else {
                 searchFieldsCombination.put(loopCounter, "fieldNotRequiredForSearch");
             }
         }
         if (numberOfFieldsForSearch == 1) {
             modelMap.put("requestedUsers", requestedUsers);
             modelMap.addAttribute("roles", userService.fetchAllRoles());
             return SEARCH_USER;
         }
         for (User searchUser : allUsers) {
             List<Attribute> attributes = searchUser.getAttributes();
             Map<String, String> userAttributes = new HashMap<String,String>();
             for (Attribute attribute : attributes) {
                 String attributeKey = attribute.name();
                 if (attributeKey.equals(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER) || attributeKey.equals(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE))
                     userAttributes.put(attributeKey, attribute.value());
             }
             try {
                 int combinationFieldSize = 1;
                 for (int fieldIndex = 1; fieldIndex <= searchFields.size(); fieldIndex++) {
                     String searchValue = (String) searchFieldsCombination.get(fieldIndex);
                    combinationFieldSize = compareSearchFields(searchUser, userAttributes, combinationFieldSize, searchValue.toLowerCase());
                 }
                 if (combinationFieldSize == numberOfFieldsForSearch) {
                     List<String> userDataList = new ArrayList<String>();
                     userDataList.add(searchUser.getId());
                     userDataList.add(searchUser.getFirstName());
                     userDataList.add(searchUser.getMiddleName());
                     userDataList.add(searchUser.getLastName());
                     userDataList.add(userAttributes.get(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER));
                     userDataList.add(userAttributes.get(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE));
                     requestedUsers.add(userDataList);
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         modelMap.put("requestedUsers", requestedUsers);
         modelMap.addAttribute("roles", userService.fetchAllRoles());
         return SEARCH_USER;
     }
 
     private int compareSearchFields(User searchUser, Map<String, String> userAttributes, int combinationFieldSize, String searchValue) {
         if (StringUtils.equals(searchUser.getId(), searchValue)
                 || searchUser.getFirstName().toLowerCase().startsWith(searchValue)
                 || searchUser.getMiddleName().toLowerCase().startsWith(searchValue)
                 || searchUser.getLastName().toLowerCase().startsWith(searchValue)
                 || StringUtils.equals(searchValue, StringUtils.substring(userAttributes.get(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER), 0, 3))
                 || StringUtils.equals(searchValue, userAttributes.get(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE))) {
 
             combinationFieldSize++;
         }
         return combinationFieldSize;
     }
 }
