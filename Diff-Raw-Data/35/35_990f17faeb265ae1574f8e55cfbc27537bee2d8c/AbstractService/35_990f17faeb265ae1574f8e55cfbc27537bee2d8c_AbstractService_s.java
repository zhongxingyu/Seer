 package com.pms.service.service;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.validator.Arg;
 import org.apache.commons.validator.Field;
 import org.apache.commons.validator.Form;
 import org.apache.commons.validator.Validator;
 import org.apache.commons.validator.ValidatorAction;
 import org.apache.commons.validator.ValidatorException;
 import org.apache.commons.validator.ValidatorResources;
 import org.apache.commons.validator.ValidatorResult;
 import org.apache.commons.validator.ValidatorResults;
 import org.apache.poi.hssf.util.HSSFColor.ROYAL_BLUE;
 
 import com.pms.service.annotation.RoleValidConstants;
 import com.pms.service.dao.ICommonDao;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.dbhelper.DBQueryUtil;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.RoleBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.impl.PurchaseServiceImpl.PurchaseStatus;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.validators.ValidatorUtil;
 
 public abstract class AbstractService {
 
     protected ICommonDao dao = null;
     
     protected ISalesContractService scs;
 
 
 
     public abstract String geValidatorFileName();
 
     @SuppressWarnings({ "rawtypes", "deprecation" })
     public void validate(Map<String, Object> map, String validatorForm) {
         ValidatorUtil.init();
         if (map == null) {
             map = new HashMap<String, Object>();
         }
         map.put("dao", this.getDao());
 
         ValidatorResources resources = ValidatorUtil.initValidatorResources().get(geValidatorFileName());
 
         // Create a validator with the ValidateBean actions for the bean
         // we're interested in.
         Validator validator = new Validator(resources, validatorForm);
         // Tell the validator which bean to validate against.
         validator.setParameter(Validator.BEAN_PARAM, map);
         ValidatorResults results = null;
 
         try {
             results = validator.validate();
         } catch (ValidatorException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Start by getting the form for the current locale and Bean.
         Form form = resources.getForm(Locale.CHINA, validatorForm);
         // Iterate over each of the properties of the Bean which had messages.
         Iterator propertyNames = results.getPropertyNames().iterator();
         while (propertyNames.hasNext()) {
             String propertyName = (String) propertyNames.next();
 
             // Get the Field associated with that property in the Form
             Field field = form.getField(propertyName);
 
             // Get the result of validating the property.
             ValidatorResult result = results.getValidatorResult(propertyName);
 
             // Get all the actions run against the property, and iterate over
             // their names.
             Map actionMap = result.getActionMap();
             Iterator keys = actionMap.keySet().iterator();
             String msg = "";
             while (keys.hasNext()) {
                 String actName = (String) keys.next();
                 // Get the Action for that name.
                 ValidatorAction action = resources.getValidatorAction(actName);
                 String actionMsgKey = field.getArg(0).getKey() + "." + action.getName();
                 // Look up the formatted name of the field from the Field arg0
                 String prettyFieldName = ValidatorUtil.intiBundle().getString(field.getArg(0).getKey());
 
                 boolean customMsg = false;
                 if (isArgExists(actionMsgKey, field)) {
                     customMsg = true;
                     prettyFieldName = ValidatorUtil.intiBundle().getString(actionMsgKey);
                 }
 
                 if (!result.isValid(actName)) {
                     String message = "{0}";
                     if (!customMsg) {
                         message = ValidatorUtil.intiBundle().getString(action.getMsg());
                     }
                     Object[] argsss = { prettyFieldName };
                     try {
                         msg = msg.concat(new String(MessageFormat.format(message, argsss).getBytes("ISO-8859-1"), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         e.printStackTrace();
                     }
                 }
             }
             if (!ApiUtil.isEmpty(msg)) {
                 throw new ApiResponseException(String.format("Validate [%s] failed with paramters [%s]", validatorForm, map), null, msg);
             }
         }
 
         map.remove("dao");
     }
 
 
 
     private boolean isArgExists(String key, Field field) {
 
         Arg[] args = field.getArgs("");
 
         for (Arg arg : args) {
             if (arg.getKey().equalsIgnoreCase(key)) {
                 return true;
             }
         }
 
         return false;
 
     }
     
     
     
     protected boolean isCoo() {
 
         return inGroup(GroupBean.COO_VALUE);
 
     }
 
     //部门助理
     protected boolean isProjectAssistant() {
 
         return inGroup(GroupBean.PROJECT_ASSISTANT_VALUE);
 
     }
     
     
     protected boolean isSalesAssistant() {
 
         return inGroup(GroupBean.SALES_ASSISTANT_VALUE);
 
     }
 
     //部门经理
     protected boolean isSalesManager() {
 
         return inGroup(GroupBean.SALES_MANAGER_VALUE) || inGroup(GroupBean.PROJECT_MANAGER_VALUE);
 
     }
 
     protected boolean isAdmin() {
 
         return inGroup(GroupBean.GROUP_ADMIN_VALUE);
 
     }
 
     //采购
     protected boolean isPurchase() {
 
         return inGroup(GroupBean.PURCHASE_VALUE);
 
     }
     
     //采购
     protected boolean isPM() {
 
         return inGroup(GroupBean.PM);
 
     }
 
     
     //库管
     protected boolean isDepotManager() {
 
         return inGroup(GroupBean.DEPOT_MANAGER_VALUE);
 
     }
     
     //财务
     protected boolean isFinance() {
 
         return inGroup(GroupBean.FINANCE);
 
     }
     
     protected String getCurrentUserId() {
 
         return ApiThreadLocal.getCurrentUserId();
     }
     
     protected boolean isInDepartment(String depart) {
     	Map<String,Object> query = new HashMap<String,Object>();
     	query.put(UserBean.DEPARTMENT, new DBQuery(DBQueryOpertion.IN, depart));
     	query.put(ApiConstants.MONGO_ID, ApiThreadLocal.getCurrentUserId());
         return dao.exist(query, DBBean.USER);
     }
     
     private boolean inGroup(String groupName){
         if(ApiThreadLocal.get(UserBean.USER_ID) == null){
             return false;
         }else{
             String userId = ApiThreadLocal.get(UserBean.USER_ID).toString();
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
             query.put(GroupBean.GROUP_NAME, groupName);
             Map<String, Object> group = this.dao.findOneByQuery(query, DBBean.USER_GROUP);
             String id = group == null? null : group.get(ApiConstants.MONGO_ID).toString();
             
             
             Map<String, Object> userQuery = new HashMap<String, Object>();
             userQuery.put(ApiConstants.MONGO_ID, userId);
             userQuery.put(UserBean.GROUPS, new DBQuery(DBQueryOpertion.IN, id));
             
             return this.dao.exist(userQuery, DBBean.USER);
             
         }
 
     }
     
     private boolean inRole(String roleId) {
 
         if (ApiThreadLocal.get(UserBean.USER_ID) == null) {
             return false;
         } else {
             String userId = ApiThreadLocal.get(UserBean.USER_ID).toString();
 
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(RoleBean.ROLE_ID, roleId);
             query.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
 
             Map<String, Object> role = this.dao.findOneByQuery(query, DBBean.ROLE_ITEM);
 
             List<String> roles = listUserRoleIds(userId);
 
             return roles.contains(role.get(ApiConstants.MONGO_ID).toString());
 
         }
 
     }
 
 
     public List<String> listUserRoleIds(String userId) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.MONGO_ID, userId);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { UserBean.GROUPS });
         Map<String, Object> user = dao.findOneByQuery(query, DBBean.USER);
         List<String> groups = (List<String>) user.get(UserBean.GROUPS);
         
         Map<String, Object> limitQuery = new HashMap<String, Object>();
         limitQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, groups));
         limitQuery.put(ApiConstants.LIMIT_KEYS, new String[]{GroupBean.ROLES});
         
         List<Object> list = dao.listLimitKeyValues(limitQuery, DBBean.USER_GROUP);
         List<String> roles = new ArrayList<String>();
 
         for(Object role: list){
             roles.addAll((Collection<? extends String>) role);
         }
         
         if(user.get(UserBean.OTHER_ROLES)!=null){
             roles.addAll((List<? extends String>) user.get(UserBean.OTHER_ROLES));
         }
 
         return roles;
     }
     
     protected void mergeDataRoleQuery(Map<String, Object> param) {
 //        Map<String, Object> pmQuery = new HashMap<String, Object>();
 //
 //        if (isAdmin() || isFinance() || isPurchase() || isCoo() || isDepotManager() || isSalesAssistant()) {
 //            // query all data
 //        } else {
 //            pmQuery.put(ProjectBean.PROJECT_MANAGER, getCurrentUserId());
 //            pmQuery.put(ApiConstants.CREATOR, getCurrentUserId());
 //
 //            Map<String, Object> userQuery = new HashMap<String, Object>();
 //            userQuery.put(ApiConstants.MONGO_ID, getCurrentUserId());
 //            userQuery.put(ApiConstants.LIMIT_KEYS, UserBean.DEPARTMENT);
 //            Map<String, Object> user = this.dao.findOneByQuery(userQuery, DBBean.USER);
 //
 //            if (user.get(UserBean.DEPARTMENT) != null) {
 //                List<String> departements = (List<String>)user.get(UserBean.DEPARTMENT);
 //                List<String> rolesIn = new ArrayList<String>();
 //                for(String dep:  departements){
 //                    if(dep.equalsIgnoreCase(UserBean.USER_DEPARTMENT_PROJECT)){
 //                        rolesIn.add(ProjectBean.PROJECT_TYPE_PROJECT);
 //                    }else{
 //                        rolesIn.add(ProjectBean.PROJECT_TYPE_PRODUCT);
 //                        rolesIn.add(ProjectBean.PROJECT_TYPE_SERVICE);
 //                    }
 //                }
 //                
 //                pmQuery.put(ProjectBean.PROJECT_TYPE, new DBQuery(DBQueryOpertion.IN, rolesIn));
 //            }
 //
 //            // list creator or manager's data
 //            param.put(ProjectBean.PROJECT_MANAGER, DBQueryUtil.buildQueryObject(pmQuery, false));
 //        }
     }
     
     
 
     protected Map<String, Object> getMyApprovedQuery() {
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());      
         Map<String, Object> statusQuery = new HashMap<String, Object>();      
         statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequest.STATUS_APPROVED, ShipBean.SHIP_STATUS_APPROVE, PurchaseRequest.STATUS_IN_REPOSITORY}));
         statusQuery.put(PurchaseBack.paStatus, PurchaseStatus.approved.toString());
         //or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
 
     protected Map<String, Object> getMyRejectedQuey() {
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());      
         Map<String, Object> statusQuery = new HashMap<String, Object>();      
         statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequest.STATUS_REJECTED, ShipBean.SHIP_STATUS_REJECT}));
         statusQuery.put(PurchaseBack.pbStatus, PurchaseStatus.rejected.toString());
         statusQuery.put(PurchaseBack.paStatus, PurchaseStatus.rejected.toString());
 
         //or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
 
     protected Map<String, Object> getMyInprogressQuery(String type) {
         //我的待批
         Map<String, Object> ownerQuery = new HashMap<String, Object>();
         ownerQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());
 
         //FIXME 根据部门查询数据
         if (type.equalsIgnoreCase(DBBean.PURCHASE_REQUEST)) {
             if (inRole(RoleValidConstants.PURCHASE_REQUEST_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.PURCHASE_ORDER)) {
             if (inRole(RoleValidConstants.PURCHASE_ORDER_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.PURCHASE_CONTRACT)) {
             if (inRole(RoleValidConstants.PURCHASE_CONTRACT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.BORROWING)) {
             if (inRole(RoleValidConstants.BORROWING_MANAGEMENT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.PURCHASE_ALLOCATE)) {
             if (inRole(RoleValidConstants.PURCHASE_ALLOCATE_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.REPOSITORY)) {
            if (inRole(RoleValidConstants.PURCHASE_CONTRACT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (type.equalsIgnoreCase(DBBean.SHIP)) {
             if (inRole(RoleValidConstants.SHIP_MANAGEMENT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
         
         
         Map<String, Object>  statusQuery = new HashMap<String, Object>();
         statusQuery.put("status", new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.STATUS_NEW, PurchaseRequest.STATUS_REPOSITORY_NEW, ShipBean.SHIP_STATUS_SUBMIT }));
         statusQuery.put(PurchaseBack.paStatus, PurchaseStatus.submited.toString());
         // or query
         ownerQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
 
         
         return ownerQuery;
     }
 
     protected Map<String, Object> getMyDraftQuery() {
         // 我的草稿
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());
 
         Map<String, Object> statusQuery = new HashMap<String, Object>();
        statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.STATUS_DRAFT, ShipBean.SHIP_STATUS_DRAFT }));
         statusQuery.put(PurchaseBack.pbStatus, PurchaseStatus.saved.toString());
 
         // or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
     
     protected void mergeMyTaskQuery(Map<String, Object> param, String type) {
 
         if (ApiThreadLocal.getMyTask() != null) {
 
             String task = ApiThreadLocal.getMyTask();
 
             if (task.equalsIgnoreCase("draft")) {
                 param.putAll(getMyDraftQuery());
             } else if (task.equalsIgnoreCase("inprogress")) {
                 param.putAll(getMyInprogressQuery(type));
             } else if (task.equalsIgnoreCase("rejected")) {
                 param.putAll(getMyRejectedQuey());
             } else if (task.equalsIgnoreCase("approved")) {
                 param.putAll(getMyApprovedQuery());
             } else if (task.equalsIgnoreCase("tip")) {
 
             }
         }
     }
     
 
     public ISalesContractService getScs() {
         return scs;
     }
 
     public void setScs(ISalesContractService scs) {
         this.scs = scs;
     }
 
     public String generateCode(String prefix, String db) {
         return prefix + "-" + this.dao.count(null, db) + 1;
     }
 
     public ICommonDao getDao() {
         return dao;
     }
 
     public void setDao(ICommonDao dao) {
         this.dao = dao;
     }
     
     
 
 }
