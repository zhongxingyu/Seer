 package com.pms.service.service.impl;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.PayMoneyBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.service.impl.PurchaseServiceImpl.PurchaseStatus;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 
 public class PurchaseContractServiceImpl extends AbstractService implements IPurchaseContractService {
 
     private static final String APPROVED = PurchaseRequest.STATUS_APPROVED;
     private static final Logger logger = LogManager.getLogger(PurchaseContractServiceImpl.class);
     
     
     private IPurchaseService backService;
     
     private ISalesContractService scs;
     
     @Override
     public String geValidatorFileName() {
         return null;
     }
 
 
     @Override
     public Map<String, Object> listPurchaseContracts(Map<String, Object> parameters) {
 
         Map<String, Object> results = dao.list(parameters, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         for(Map<String, Object> data: list){
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, data.get("supplier"));
             
             
             Map<String, Object> supplier = this.dao.findOneByQuery(query, DBBean.SUPPLIER);
             
             data.put("supplierName", supplier.get("supplierName"));
         }
         
         return results;
         
     }
     
     public Map<String, Object> listContractsForRepositorySelect() {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "eqcostList.projectId", "supplier" });
         
 
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
 
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<String> projectIds = new ArrayList<String>();
         for (Map<String, Object> data : list) {
             List<Map<String, Object>> pList = (List<Map<String, Object>>) data.get("eqcostList");
 
             for (Map<String, Object> p : pList) {
                 projectIds.add(p.get("projectId").toString());
             }            
         }
         
         Map<String, Object> projectQuery = new HashMap<String, Object>();
         projectQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, projectIds));
         projectQuery.put(ApiConstants.LIMIT_KEYS, ProjectBean.PROJECT_NAME);
 
         Map<String, Object> projects = this.dao.list(projectQuery, DBBean.PROJECT);
         return projects;
     }
     
     public Map<String, Object> listContractsSuppliersByProjectId(Map<String, Object> contract) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "supplier" });
         query.put("eqcostList.projectId", contract.get("projectId"));
 
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
 
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<Object> supplierIds = new ArrayList<Object>();
         for (Map<String, Object> data : list) {
             supplierIds.add(data.get("supplier"));
         }
 
         Map<String, Object> supplierQuery = new HashMap<String, Object>();
         supplierQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, supplierIds));
         supplierQuery.put(ApiConstants.LIMIT_KEYS, "supplierName");
 
         Map<String, Object> suppliers = this.dao.list(supplierQuery, DBBean.SUPPLIER);
 
         return suppliers;
 
     }
     
     public Map<String, Object> listContractsByProjectAndSupplier(Map<String, Object> params) {
         Map<String, Object> query = new HashMap<String, Object>();
         Object projectId = params.get("projectId");
         query.put("eqcostList.projectId", projectId);
         query.put("supplier", params.get("supplier"));
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         
         Map<String, Object>  results = this.dao.list(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<Map<String, Object>> eqclist = new ArrayList<Map<String, Object>>();
         
         for (Map<String, Object> data : list) {
             List<Map<String, Object>> pList = (List<Map<String, Object>>) data.get("eqcostList");
 
             for (Map<String, Object> p : pList) {
                 if(p.get("projectId").equals(projectId)){
                     eqclist.add(p);
                 }
             }            
         }
         
         Map<String, Object> lresult= new HashMap<String, Object>();
         lresult.put("data", eqclist);
         return lresult;
 
     }
     
     public Map<String, Object> listContractsByProjectId(Map<String, Object> contract){
         
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put("eqcostList.projectId", contract.get("projectId"));
 
         return this.dao.list(query, DBBean.PURCHASE_CONTRACT);
         
     }
     
     
     public List<Map<String,Object>> listApprovedPurchaseContractCosts(String salesContractCode){
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_EQ_LIST});
         query.put(PurchaseRequest.PROCESS_STATUS, APPROVED);
 
         Map<String, Object>  results = this.dao.findOneByQuery(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String,Object>> list = (List<Map<String, Object>>) results.get("eqcostList");
         return list;
     }
     
     public Map<String, Object> getPurchaseContract(Map<String, Object> parameters){
         
         return this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_CONTRACT);
     }
 
 
     @Override
     public void deletePurchaseContract(Map<String, Object> contract) {
         List<String> ids = new ArrayList<String>();
         ids.add(contract.get(ApiConstants.MONGO_ID).toString());
         dao.deleteByIds(ids, DBBean.PURCHASE_CONTRACT);
 
     }
 
     @Override
     public Map<String, Object> updatePurchaseContract(Map<String, Object> contract) {
         return updatePurchase(contract, DBBean.PURCHASE_CONTRACT);
     }
  
 
     @Override
     public Map<String, Object> listPurchaseOrders(Map<String, Object> parameters) {
         
         Map<String, Object> results = dao.list(parameters, DBBean.PURCHASE_ORDER);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         
         for(Map<String, Object> data: list){
             
             
             Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(data);
             data.put("customerName", relatedProjectInfo.get(ProjectBean.PROJECT_CUSTOMER));
             data.put("projectName", relatedProjectInfo.get("projectName"));
             data.put("projectManager", relatedProjectInfo.get("projectManager"));
         }
         
         return results;
         
     }
     
     public Map<String, Object> listApprovedPurchaseOrderForSelect() {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_APPROVED);
         
         return dao.list(query, DBBean.PURCHASE_ORDER);
 
     }
 
     @Override
     public void deletePurchaseOrder(Map<String, Object> contract) {
         List<String> ids = new ArrayList<String>();
         ids.add(contract.get(ApiConstants.MONGO_ID).toString());
         dao.deleteByIds(ids, DBBean.PURCHASE_ORDER);
 
     }
 
     @Override
     public Map<String, Object> updatePurchaseOrder(Map<String, Object> parameters) {
         PurchaseRequest request = (PurchaseRequest) new PurchaseRequest().toEntity(parameters);
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequest.STATUS_DRAFT);
             }
             request.setApprovedDate(null);
             request.setPurchaseOrderCode("order_" + String.valueOf(new Date().getTime()));
             Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);            
             parameters = request.toMap();
             parameters.put(SalesContractBean.SC_EQ_LIST, eqList);
 
         }
 
         return updatePurchase(parameters, DBBean.PURCHASE_ORDER);
     }
     
     public Map<String, Object> getPurchaseOrder(Map<String, Object> parameters){
         Map<String, Object> result =  this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ORDER);        
         return mergeProjectInfo(result);
     }
 
     public Map<String, Object> approvePurchaseContract(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_CONTRACT, APPROVED);
     }
     
     
     public Map<String, Object> processRequest(Map<String, Object> request, String db, String status){
         
         Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, request.get(ApiConstants.MONGO_ID), db);
         request.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
         request.put(PurchaseRequest.PROCESS_STATUS, status);
         request.put(PurchaseRequest.APPROVED_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 
         Map<String, Object> result =  dao.updateById(request, db);
         
         if(cc.get(PurchaseRequest.SALES_CONTRACT_CODE)!=null){
             updateSummaryUnderContract(db, cc.get(PurchaseRequest.SALES_CONTRACT_CODE).toString());
         }
         
         return result;
         
     }
 
     public Map<String, Object> rejectPurchaseContract(Map<String, Object> order) {       
         return processRequest(order, DBBean.PURCHASE_CONTRACT, PurchaseRequest.STATUS_REJECTED);
     }
 
     public Map<String, Object> approvePurchaseOrder(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, APPROVED);
     }
 
     public Map<String, Object> rejectPurchaseOrder(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, PurchaseRequest.STATUS_REJECTED);
     }
 
     /**
      * 
      * 选择已批准的备货申请，返回_id, pbCode, scCode 字段
      * 
      */
     public Map<String, Object> listBackRequestForSelect() {        
         Map<String, Object> query = new HashMap<String, Object>();      
         query.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());        
         query.put(ApiConstants.LIMIT_KEYS, new String[] { PurchaseBack.pbCode, PurchaseBack.scCode});
         return dao.list(query, DBBean.PURCHASE_BACK);
     }
     
     
     public Map<String, Object> listPurchaseRequests(Map<String, Object> params){
         
         Map<String, Object> roleQuery = new HashMap<String, Object>();
         
        if(params.get("approvePage")!=null){           
            params.remove("approvePage");
             params.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.NOT_IN, new String[] { PurchaseRequest.STATUS_DRAFT, PurchaseRequest.STATUS_CANCELLED}));
         }
         
         if(isDepartmentManager()){
             params.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_NEW);
         }
         
         if (isPurchase()) {
             params.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.MANAGER_APPROVED, PurchaseRequest.STATUS_APPROVED, PurchaseRequest.STATUS_REJECTED }));
         }
         
         Map<String, Object> results = dao.list(params, DBBean.PURCHASE_REQUEST);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         
         
         
         for (Map<String, Object> data : list) {
      
             Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(data);
 
             if (relatedProjectInfo != null) {
                 data.put("customerName", relatedProjectInfo.get(ProjectBean.PROJECT_CUSTOMER));
                 data.put("projectName", relatedProjectInfo.get("projectName"));
                 data.put("projectManager", relatedProjectInfo.get("projectManager"));
             }
         }
         
         return results;
     }
     
     
     public Map<String, Object> getRelatedProjectInfo(Map<String, Object> params){
 
         //FIXME: code refine
         PurchaseCommonBean request = (PurchaseCommonBean) new PurchaseCommonBean().toEntity(params);
         
         Map<String, Object> project = dao.findOne(ApiConstants.MONGO_ID, request.getProjectId(), DBBean.PROJECT);
 
         Map<String, Object> pmQuery = new HashMap<String, Object>();
         pmQuery.put(ApiConstants.LIMIT_KEYS, new String[] { UserBean.USER_NAME });
         pmQuery.put(ApiConstants.MONGO_ID, project.get(ProjectBean.PROJECT_MANAGER));
 
         Map<String, Object> pmData = dao.findOneByQuery(pmQuery, DBBean.USER);
         project.put(ProjectBean.PROJECT_MANAGER, pmData.get(UserBean.USER_NAME));
 
         
         String cId = (String) project.get(ProjectBean.PROJECT_CUSTOMER);
         
         Map<String, Object> customerQuery = new HashMap<String, Object>();
         customerQuery.put(ApiConstants.LIMIT_KEYS, new String[] {CustomerBean.NAME});
         customerQuery.put(ApiConstants.MONGO_ID, cId);
         Map<String, Object> customerData = dao.findOneByQuery(customerQuery, DBBean.CUSTOMER);
         
         project.put(ProjectBean.PROJECT_CUSTOMER, customerData.get(CustomerBean.NAME));
         
         return project;
     }
     
     public Map<String, Object> listApprovedPurchaseRequestForSelect(){
         Map<String, Object> query = new HashMap<String, Object>();
 //        query.put(PurchaseRequestBean.STATUS, APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "purchaseRequestCode" });
         return dao.list(query, DBBean.PURCHASE_REQUEST);
     }
     
     public Map<String, Object> updatePurchaseRequest(Map<String, Object> parameters) {
         PurchaseRequest request = (PurchaseRequest) new PurchaseRequest().toEntity(parameters);
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequest.STATUS_DRAFT);
             }
             
             //根据销售合同id查询项目和客户ID
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, request.getSalesContractId());
             query.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_CUSTOMER_ID});
             
             Map<String, Object> sc = this.dao.findOneByQuery(query, DBBean.SALES_CONTRACT);
             request.setProjectId(sc.get(SalesContractBean.SC_PROJECT_ID).toString());
             request.setPurchaseRequestCode("request_" + String.valueOf(new Date().getTime()));
             Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);
             parameters = request.toMap();
             parameters.put(SalesContractBean.SC_EQ_LIST, eqList);
         }
         
 
         return updatePurchase(parameters, DBBean.PURCHASE_REQUEST);
     }
     
     public Map<String, Object> updatePurchase(Map<String, Object> parameters, String db) {
         Map<String, Object> result = null;
 
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             result = this.dao.add(parameters, db);
         } else {
             result = dao.updateById(parameters, db);
         }
 
 //        Map<String, Object> cc = this.dao.findOne(ApiConstants.MONGO_ID, result.get(ApiConstants.MONGO_ID), new String[] { PurchaseRequestOrder.SALES_CONTRACT_CODE }, db);
 //        if (cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE) != null) {
 //            updateSummaryUnderContract(db, cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE).toString());
 //        }
 
         return result;
 
     }
 
 
     public Map<String, Object> approvePurchaseRequest(Map<String, Object> request){
         
         if(isDepartmentManager()){
             return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.MANAGER_APPROVED);
         }else{
             return processRequest(request, DBBean.PURCHASE_REQUEST, APPROVED);
         }
     }
     
     public Map<String, Object> cancelPurchaseRequest(Map<String, Object> request){
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.STATUS_CANCELLED);
     }
     
     public Map<String, Object> rejectPurchaseRequest(Map<String, Object> request){
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.STATUS_REJECTED);
     }
     
     public Map<String, Object> getPurchaseRequest(Map<String, Object> parameters){
 
         Map<String, Object> result =  this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_REQUEST);
         return mergeProjectInfo(result);
 
     }
 
     
     public void deletePurchaseRequest(Map<String, Object> order){
         
     }
        
     
     public Map<String, Object> getBackRequestForSelect(Map<String, Object> parameters) {
         return backService.loadBack(parameters);
 
     }
 
 
     private Map<String, Object> mergeProjectInfo(Map<String, Object> result) {
            
            Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(result);
            
            result.put("projectName", relatedProjectInfo.get(ProjectBean.PROJECT_NAME));
            result.put("projectCode", relatedProjectInfo.get(ProjectBean.PROJECT_CODE));
 
            return result;
     }
     
     
     private void updateSummaryUnderContract(String db, String scId){
         
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.SALES_CONTRACT_CODE, scId);
         query.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequest.STATUS_APPROVED, PurchaseRequest.STATUS_NEW}));
         //TODO: query requried keys
         //        query.put(ApiConstants.LIMIT_KEYS, new String[]{});
         
         Map<String, Object> results = this.dao.list(query, db);
         
         if(results !=null){
             List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
             
             float totalPercent = 0;
             float totalMoneyPercent = 0;
             
             for(Map<String, Object> result: list){
                 result.put("requestTotalOfCountract", list.size());
                 if(result.get("numbersPercentOfContract")!=null){
                     totalPercent = totalPercent + Float.parseFloat(result.get("numbersPercentOfContract").toString());
                 }
                 
                 if(result.get("moneyPercentOfContract")!=null){
                     totalMoneyPercent = totalMoneyPercent + Float.parseFloat(result.get("moneyPercentOfContract").toString());
                 }
             }
             
             for(Map<String, Object> result: list){
                 result.put("allRequestedNumbersOfCountract", totalPercent);
                 result.put("totalRequestedMoneyOfContract", totalMoneyPercent);
                 this.dao.updateById(result, db);
             }
             
         }
     }
 
 
     @Override
     public Map<String, Object> listRepositoryRequests(Map<String, Object> params) {
         return this.dao.list(params, DBBean.REPOSITORY);
     }
 
 
     @Override
     public Map<String, Object> addRepositoryRequest(Map<String, Object> parserListJsonParameters) {
         return updatePurchase(parserListJsonParameters, DBBean.REPOSITORY);
         
     }
 
 
     @Override
     public Map<String, Object> getRepositoryRequest(Map<String, Object> parameters) {
         return this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.REPOSITORY);
     }
 
 
     @Override
     public void deleteRepositoryRequest(Map<String, Object> parserJsonParameters) {
         
     }
 
 
     @Override
     public Map<String, Object> updateRepositoryRequest(Map<String, Object> parameters) {
         if (parameters.get("inDate") != null) {
             try {
                 parameters.put("inDate", DateUtil.getDate(parameters.get("inDate").toString()));
             } catch (ParseException e) {
                 logger.error(e);
             }
 
         }
 
         return updatePurchase(parameters, DBBean.REPOSITORY);
 
     }
 
 
     @Override
     public Map<String, Object> approveRepositoryRequest(Map<String, Object> parserJsonParameters) {
         return null;
     }
 
 
     @Override
     public Map<String, Object> rejectRepositoryRequest(Map<String, Object> parserJsonParameters) {
         return null;
     }
     
     //采购合同列表为付款
 	public Map<String, Object> listSelectForPayment(Map<String, Object> params) {
     	Map<String,Object> query = new HashMap<String,Object>();
     	query.put(ApiConstants.LIMIT_KEYS, new String[]{"purchaseContractCode","supplierName"});
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
         return results;
 	}
 
 	@Override
 	public Map<String, Object> listPaymoney(Map<String, Object> params) {
 		Map<String,Object> query1 = new HashMap<String,Object>();
 		Map<String,Object> map1 = dao.list(query1, DBBean.PAY_MONEY);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)map1.get(ApiConstants.RESULTS_DATA);
 		
 		Set<String> suIds = new HashSet<String>();
 		for(Map<String,Object> obj : list1){
 			suIds.add((String)obj.get(PayMoneyBean.supplierId));
 		}
 		suIds.remove(null);
 		if(!suIds.isEmpty()){
 		Map<String,Object> query02 = new HashMap<String,Object>();
 		query02.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList(suIds)));
 		Map<String,Object> map2 = dao.listToOneMapAndIdAsKey(query02, DBBean.SUPPLIER);
 		for(Map<String,Object> obj : list1){
 			String id = (String)obj.get(PayMoneyBean.supplierId);
 			if(map2.containsKey(id)){
 				Map<String,Object> su = (Map<String,Object>)map2.get(id);
 				obj.put("supplierName", su.get("supplierName"));
 			}
 		}
 		}
 		return map1;
 	}
 
 	@Override
 	public Map<String, Object> addPaymoney(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(PayMoneyBean.payMoney, ApiUtil.getDouble(params,PayMoneyBean.payMoney));
 		obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
 		obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
 		obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
 		obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
 		Map<String,Object> pc = dao.findOne(ApiConstants.MONGO_ID, params.get(PayMoneyBean.purchaseContractId),new String[]{"supplierName","purchaseContractCode"}, DBBean.PURCHASE_CONTRACT);
 		obj.put(PayMoneyBean.purchaseContractCode, pc.get("purchaseContractCode"));
 		obj.put(PayMoneyBean.supplierId, pc.get("supplierName"));
 		return dao.add(obj, DBBean.PAY_MONEY);
 	}
 
 	@Override
 	public Map<String, Object> updatePaymoney(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		obj.put(PayMoneyBean.payMoney, params.get(PayMoneyBean.payMoney));
 		obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
 		obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
 		obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
 		obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
 		return dao.updateById(obj, DBBean.PAY_MONEY);
 	}
 
 	@Override
 	public Map<String, Object> listGetInvoice(Map<String, Object> params) {
 		return dao.list(null, DBBean.GET_INVOICE);
 	}
 
 	@Override
 	public Map<String, Object> addGetInvoice(Map<String, Object> params) {
 		return dao.add(params, DBBean.GET_INVOICE);
 	}
 
 	@Override
 	public Map<String, Object> updateGetInvoice(Map<String, Object> params) {
 		return dao.updateById(params, DBBean.GET_INVOICE);
 	}
 
 	@Override
 	public void destroyGetInvoice(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.GET_INVOICE);
 	}
 
 	public ISalesContractService getScs() {
         return scs;
     }
 
     public void setScs(ISalesContractService scs) {
         this.scs = scs;
     }
 
     public IPurchaseService getBackService() {
         return backService;
     }
 
 
     public void setBackService(IPurchaseService backService) {
         this.backService = backService;
     }
 
 }
