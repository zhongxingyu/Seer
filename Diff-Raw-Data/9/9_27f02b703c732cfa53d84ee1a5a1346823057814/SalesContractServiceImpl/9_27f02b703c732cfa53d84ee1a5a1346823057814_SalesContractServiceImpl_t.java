 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.mongodb.DBObject;
 import com.mongodb.QueryBuilder;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.dbhelper.DBQueryUtil;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.InvoiceBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 
 public class SalesContractServiceImpl extends AbstractService implements ISalesContractService {
 
 	@Override
 	public String geValidatorFileName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> listSC(Map<String, Object> params) {
 
 		String[] limitKeys = {SalesContractBean.SC_CODE, SalesContractBean.SC_AMOUNT, 
 				SalesContractBean.SC_DATE, SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_RUNNING_STATUS};
 		params.put(ApiConstants.LIMIT_KEYS, limitKeys);
 
 		mergeDataRoleQuery(params);
 		
 		Map<String, Object> result = dao.list(params, DBBean.SALES_CONTRACT);
 		
 		mergeProjectInfoForSC(result);
 		return result;
 	}
 
 	@Override
 	public Map<String, Object> addSC(Map<String, Object> params) {
 		String _id = (String) params.get(ApiConstants.MONGO_ID);
 		
 		//构造合同信息
 		Map<String, Object> contract = new HashMap<String, Object>();
 		contract.put(SalesContractBean.SC_PROJECT_ID, params.get(SalesContractBean.SC_PROJECT_ID));
 //		contract.put(SalesContractBean.SC_CUSTOMER, params.get(SalesContractBean.SC_CUSTOMER));
 		contract.put(SalesContractBean.SC_AMOUNT, params.get(SalesContractBean.SC_AMOUNT));
 		contract.put(SalesContractBean.SC_INVOICE_TYPE, params.get(SalesContractBean.SC_INVOICE_TYPE));
 		contract.put(SalesContractBean.SC_ESTIMATE_EQ_COST0, params.get(SalesContractBean.SC_ESTIMATE_EQ_COST0));
 		contract.put(SalesContractBean.SC_ESTIMATE_EQ_COST1, params.get(SalesContractBean.SC_ESTIMATE_EQ_COST1));
 		contract.put(SalesContractBean.SC_ESTIMATE_SUB_COST, params.get(SalesContractBean.SC_ESTIMATE_SUB_COST));
 		contract.put(SalesContractBean.SC_ESTIMATE_PM_COST, params.get(SalesContractBean.SC_ESTIMATE_PM_COST));
 		contract.put(SalesContractBean.SC_ESTIMATE_DEEP_DESIGN_COST, params.get(SalesContractBean.SC_ESTIMATE_DEEP_DESIGN_COST));
 		contract.put(SalesContractBean.SC_ESTIMATE_DEBUG_COST, params.get(SalesContractBean.SC_ESTIMATE_DEBUG_COST));
 		contract.put(SalesContractBean.SC_ESTIMATE_OTHER_COST, params.get(SalesContractBean.SC_ESTIMATE_OTHER_COST));
 //		contract.put(SalesContractBean.SC_DEBUG_COST_TYPE, params.get(SalesContractBean.SC_DEBUG_COST_TYPE));
 //		contract.put(SalesContractBean.SC_TAX_TYPE, params.get(SalesContractBean.SC_TAX_TYPE));
 		contract.put(SalesContractBean.SC_CODE, params.get(SalesContractBean.SC_CODE));
 		contract.put(SalesContractBean.SC_PERSON, params.get(SalesContractBean.SC_PERSON));
 		contract.put(SalesContractBean.SC_TYPE, params.get(SalesContractBean.SC_TYPE));
 		contract.put(SalesContractBean.SC_ARCHIVE_STATUS, params.get(SalesContractBean.SC_ARCHIVE_STATUS));
 		contract.put(SalesContractBean.SC_RUNNING_STATUS, params.get(SalesContractBean.SC_RUNNING_STATUS));
 		contract.put(SalesContractBean.SC_DATE, params.get(SalesContractBean.SC_DATE));
 		contract.put(SalesContractBean.SC_DOWN_PAYMENT, params.get(SalesContractBean.SC_DOWN_PAYMENT));
 		contract.put(SalesContractBean.SC_PROGRESS_PAYMENT, params.get(SalesContractBean.SC_PROGRESS_PAYMENT));
 		contract.put(SalesContractBean.SC_QUALITY_MONEY, params.get(SalesContractBean.SC_QUALITY_MONEY));
 		contract.put(SalesContractBean.SC_MEMO, params.get(SalesContractBean.SC_MEMO));
 		
 		
 		mergeCommonProjectInfo(contract, contract.get(SalesContractBean.SC_PROJECT_ID));
 		
 		List<Map<String, Object>> eqcostList = new ArrayList<Map<String, Object>>();
 		eqcostList = (List<Map<String, Object>>)params.get(SalesContractBean.SC_EQ_LIST);
 		//eqcostList = new Gson().fromJson(params.get(SalesContractBean.SC_EQ_LIST).toString(), List.class);
 		//contract.put(SalesContractBean.SC_EQ_LIST, eqcostList);
 		
 		Map<String, Object> addedContract = null;
 		if (_id == null){//Add
 			contract.put(SalesContractBean.SC_MODIFY_TIMES, 0);
 			addedContract = dao.add(contract, DBBean.SALES_CONTRACT);
 			
 			//添加成本设备清单记录
 			if (eqcostList != null && !eqcostList.isEmpty()){
 				addEqCostListForContract(eqcostList, (String)addedContract.get(ApiConstants.MONGO_ID), 
 						(String)addedContract.get(SalesContractBean.SC_PROJECT_ID) );
 			}
 			
 			return addedContract;
 		}else{//Update
 			contract.put(ApiConstants.MONGO_ID, _id);
 			
 			//更新销售合同变更次数
 			Map<String, Object> existContractQuery = new HashMap<String, Object>();
 			existContractQuery.put(ApiConstants.MONGO_ID, _id);
 			existContractQuery.put(ApiConstants.LIMIT_KEYS, new String[] {SalesContractBean.SC_PROJECT_ID, 
 					SalesContractBean.SC_MODIFY_TIMES, SalesContractBean.SC_AMOUNT});
 			Map<String, Object> existContract = dao.findOneByQuery(existContractQuery, DBBean.SALES_CONTRACT);
 			float oldAmount = ApiUtil.getFloatParam(existContract, SalesContractBean.SC_AMOUNT);
 			float newAmount = ApiUtil.getFloatParam(params, SalesContractBean.SC_AMOUNT);
 			if (oldAmount != newAmount){
 				int newVersion = ApiUtil.getIntegerParam(existContract, SalesContractBean.SC_MODIFY_TIMES);
 				contract.put(SalesContractBean.SC_MODIFY_TIMES, newVersion++);
 			}
 			
 			//添加成本设备清单记录
 			if (!eqcostList.isEmpty()){
 				addEqCostListForContract(eqcostList, _id, (String) existContract.get(SalesContractBean.SC_PROJECT_ID));
 			}
 			
 			return dao.updateById(contract, DBBean.SALES_CONTRACT);
 		}
 	}
 	
 	private void addEqCostListForContract(List<Map<String, Object>> eqcostList, String cId, String proId){
 		for (Map<String, Object> item : eqcostList){
 
             Map<String, Object> realItemQuery = new HashMap<String, Object>();
             realItemQuery.put(EqCostListBean.EQ_LIST_MATERIAL_CODE, item.get(EqCostListBean.EQ_LIST_MATERIAL_CODE));
             realItemQuery.put(EqCostListBean.EQ_LIST_PRODUCT_NAME, item.get(EqCostListBean.EQ_LIST_PRODUCT_NAME));
             realItemQuery.put(EqCostListBean.EQ_LIST_REAL_AMOUNT, new DBQuery(DBQueryOpertion.NOT_NULL));
             realItemQuery.put(ApiConstants.LIMIT_KEYS, EqCostListBean.EQ_LIST_REAL_AMOUNT);
             Map<String, Object> realItem = dao.findOneByQuery(realItemQuery, DBBean.EQ_COST);
             if (realItem == null) {
                 item.put(EqCostListBean.EQ_LIST_REAL_AMOUNT, item.get(EqCostListBean.EQ_LIST_AMOUNT));
                 item.put(EqCostListBean.EQ_LIST_LEFT_AMOUNT, item.get(EqCostListBean.EQ_LIST_REAL_AMOUNT));
             } else {
                 int applyAmount = (int)Float.parseFloat(item.get(EqCostListBean.EQ_LIST_AMOUNT).toString());
                 int totalAmount = applyAmount + (int)Float.parseFloat(realItem.get(EqCostListBean.EQ_LIST_REAL_AMOUNT).toString());
 
                 realItem.put(EqCostListBean.EQ_LIST_REAL_AMOUNT, totalAmount);
                 realItem.put(EqCostListBean.EQ_LIST_LEFT_AMOUNT, totalAmount);
                 this.dao.updateById(realItem, DBBean.EQ_COST);
             }
 		    
 			item.put(SalesContractBean.SC_PROJECT_ID, proId);
 			item.put(EqCostListBean.EQ_LIST_SC_ID, cId);
 			dao.add(item, DBBean.EQ_COST);
 		}
 	}
 
 	@Override
 	public Map<String, Object> listSCsForSelect(Map<String, Object> params) {
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_CODE, SalesContractBean.SC_PROJECT_ID});
 		
 		Map<String, Object> projectQuery = new HashMap<String, Object>();
 		projectQuery.put(ApiConstants.LIMIT_KEYS,ProjectBean.PROJECT_NAME);
 		
 		Map<String, Object> projects = this.dao.listToOneMapAndIdAsKey(projectQuery,DBBean.PROJECT);
 		Map<String, Object>  scResults = dao.list(query, DBBean.SALES_CONTRACT);
 		List<Map<String, Object>> scList = (List<Map<String, Object>>) scResults.get(ApiConstants.RESULTS_DATA);
 		for (Map<String, Object> item : scList){
 		    Map<String, Object> project = (Map<String, Object>) projects.get(item.get(SalesContractBean.SC_PROJECT_ID));
 		    item.put(ProjectBean.PROJECT_NAME, project.get(ProjectBean.PROJECT_NAME));
 		}
 		return scResults;
 	}
 
 	@Override
 	public Map<String, Object> listMergedEqListBySC(Map<String, Object> params) {
 		// TODO Auto-generated method stub
 		String cId = (String) params.get(EqCostListBean.EQ_LIST_SC_ID);
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(EqCostListBean.EQ_LIST_SC_ID, cId);
 		query.put(EqCostListBean.EQ_LIST_REAL_AMOUNT, new DBQuery(DBQueryOpertion.NOT_NULL));
 		Map<String, Object> result = dao.list(query, DBBean.EQ_COST);
 		return result;
 	}
 	
 	public Map<String,Object> getBaseInfoByIds(List<String> ids){
 		String[] keys = new String[]{SalesContractBean.SC_CODE,SalesContractBean.SC_AMOUNT, SalesContractBean.SC_PROJECT_ID,
 				SalesContractBean.SC_CUSTOMER_ID,SalesContractBean.SC_BACK_REQUEST_COUNT,SalesContractBean.SC_INVOICE_TYPE};
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(ApiConstants.LIMIT_KEYS, keys);
 		query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, ids));
 		Map<String,Object> result = dao.list(query, DBBean.SALES_CONTRACT);
 		mergeProjectInfoForSC(result);
 		List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		Map<String,Object> data = new HashMap<String,Object>();
 		for(Map<String,Object> obj : list){
 			data.put((String)obj.get(ApiConstants.MONGO_ID), obj);
 			obj.remove(ApiConstants.MONGO_ID);
 		}
 		return data;
 	}
 	/**id:map*/
 	public Map<String,Object> getEqBaseInfoBySalesContractIds(String id){
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(EqCostListBean.EQ_LIST_SC_ID, id);
 		return dao.listToOneMapAndIdAsKey(query,DBBean.EQ_COST);
 	}
 	
 	/**id:map*/
 	public Map<String,Object> getEqBaseInfoByIds(String ids){
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, ids));
 		return dao.listToOneMapAndIdAsKey(query,DBBean.EQ_COST);
 	}
 	
 	private void mergeProjectInfoForSC(Map<String, Object> result){
 		List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		
 		List<String> pIdList = new ArrayList<String>();
 		List<String> pmIds = new ArrayList<String>();
 		List<String> custIds = new ArrayList<String>();
 		for (Map<String, Object> sc:list){
 			String proId = (String) sc.get(SalesContractBean.SC_PROJECT_ID);
 			if (proId != null && proId.length() > 0){
 				pIdList.add(proId);
 			}
 		}
 		
 		Map<String, Object> queryProject = new HashMap<String, Object>();
 		queryProject.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pIdList));
 		queryProject.put(ApiConstants.LIMIT_KEYS, new String[] {ProjectBean.PROJECT_NAME, 
 				ProjectBean.PROJECT_MANAGER, ProjectBean.PROJECT_CODE, ProjectBean.PROJECT_CUSTOMER});
 		Map<String, Object> pInfoMap = dao.listToOneMapAndIdAsKey(queryProject, DBBean.PROJECT);
 		
 		pInfoMap.remove(ApiConstants.RESULTS_DATA);
 		pInfoMap.remove(ApiConstants.PAGENATION);
 		for (Entry<String, Object> pro : pInfoMap.entrySet()){
 			Map<String, Object> value = (Map<String, Object>) pro.getValue(); 
 			pmIds.add((String) value.get(ProjectBean.PROJECT_MANAGER));
 			custIds.add((String) value.get(ProjectBean.PROJECT_CUSTOMER));
 		}
 		
 		Map<String, Object> pmQuery = new HashMap<String, Object>();
 		pmQuery.put(ApiConstants.LIMIT_KEYS, new String[] {UserBean.USER_NAME});
 		pmQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pmIds));
 		Map<String, Object> pmData = dao.listToOneMapAndIdAsKey(pmQuery, DBBean.USER);
 		
 		Map<String, Object> customerQuery = new HashMap<String, Object>();
 		customerQuery.put(ApiConstants.LIMIT_KEYS, new String[] {CustomerBean.NAME});
 		customerQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, custIds));
 		Map<String, Object> customerData = dao.listToOneMapAndIdAsKey(customerQuery, DBBean.CUSTOMER);
 		
 		for (Map<String, Object> sc:list){
 			String pId = (String) sc.get(SalesContractBean.SC_PROJECT_ID);
 			Map<String, Object> pro = (Map<String, Object>) pInfoMap.get(pId);
 			if (pro != null){
 				String pmId = (String) pro.get(ProjectBean.PROJECT_MANAGER);
 				String cusId =(String) pro.get(ProjectBean.PROJECT_CUSTOMER); 
 				
 				sc.put(ProjectBean.PROJECT_CODE, pro.get(ProjectBean.PROJECT_CODE));
 				sc.put(ProjectBean.PROJECT_NAME, pro.get(ProjectBean.PROJECT_NAME));
 				
 				Map<String, Object> pmInfo = (Map<String, Object>) pmData.get(pmId);
 				if (pmInfo != null){
 					sc.put(ProjectBean.PROJECT_MANAGER, pmInfo.get(UserBean.USER_NAME));
 				}
 				
 				Map<String, Object> cusInfo = (Map<String, Object>) customerData.get(cusId);
 				if (cusInfo != null){
 					sc.put(ProjectBean.PROJECT_CUSTOMER, cusInfo.get(CustomerBean.NAME));
 				}
 			}
 		}
 	}
 
 	@Override
 	public Map<String, Object> getSC(Map<String, Object> params) {
 		String _id = (String) params.get(ApiConstants.MONGO_ID);
 		Map<String, Object> sc = dao.findOne(ApiConstants.MONGO_ID, _id, DBBean.SALES_CONTRACT);
 		
 		//获取相关的 设备清单列表数据
 		Map<String, Object> eqCostQuery = new HashMap<String, Object>();
 		eqCostQuery.put(EqCostListBean.EQ_LIST_SC_ID, _id);
 		Map<String, Object> eqList = dao.list(eqCostQuery, DBBean.EQ_COST);
 		List<Map<String, Object>> eqListData = (List<Map<String, Object>>) eqList.get(ApiConstants.RESULTS_DATA);
 		
 		//获取相关开票信息列表数据
 		Map<String, Object> invoiceQuery = new HashMap<String, Object>();
 		invoiceQuery.put(SalesContractBean.SC_ID, _id);
 		Map<String, Object> invoiceList = dao.list(invoiceQuery, DBBean.SC_INVOICE);
 		List<Map<String, Object>> invoiceListData = (List<Map<String, Object>>) invoiceList.get(ApiConstants.RESULTS_DATA); 
 		
 		//获取相关收款信息列表数据
 		Map<String, Object> gotMoneyQuery = new HashMap<String, Object>();
 		gotMoneyQuery.put(SalesContractBean.SC_ID, _id);
 		Map<String, Object> gotMoneyList = dao.list(gotMoneyQuery, DBBean.SC_GOT_MONEY);
 		List<Map<String, Object>> gotMoneyListData = (List<Map<String, Object>>) gotMoneyList.get(ApiConstants.RESULTS_DATA);
 		
 		//获取相关 按月发货金额
 		Map<String, Object> monthShipmentsQuery = new HashMap<String, Object>();
 		monthShipmentsQuery.put(SalesContractBean.SC_ID, _id);
 		Map<String, Object> monthShipmentsList = dao.list(gotMoneyQuery, DBBean.SC_MONTH_SHIPMENTS);
 		List<Map<String, Object>> monthShipmentsListData = (List<Map<String, Object>>) monthShipmentsList.get(ApiConstants.RESULTS_DATA);
 		
 		//获取相关 按年发货金额
 		Map<String, Object> yearShipmentsQuery = new HashMap<String, Object>();
 		yearShipmentsQuery.put(SalesContractBean.SC_ID, _id);
 		Map<String, Object> yearShipmentsList = dao.list(yearShipmentsQuery, DBBean.SC_YEAR_SHIPMENTS);
 		List<Map<String, Object>> yearShipmentsListData = (List<Map<String, Object>>) yearShipmentsList.get(ApiConstants.RESULTS_DATA);
 		
 		sc.put(SalesContractBean.SC_EQ_LIST, eqListData);
 		sc.put(SalesContractBean.SC_INVOICE_INFO, invoiceListData);
 		sc.put(SalesContractBean.SC_GOT_MONEY_INFO, gotMoneyListData);
 		sc.put(SalesContractBean.SC_MONTH_SHIPMENTS_INFO, monthShipmentsListData);
 		sc.put(SalesContractBean.SC_YEAR_SHIPMENTS_INFO, yearShipmentsListData);
 		return sc;
 	}
 
 	@Override
 	public Map<String, Object> addInvoiceForSC(Map<String, Object> params) {
 		Map<String, Object> invoice = new HashMap<String, Object>();
 		invoice.put(InvoiceBean.salesContractId, params.get(InvoiceBean.salesContractId));
 		invoice.put(SalesContractBean.SC_INVOICE_TYPE, params.get(SalesContractBean.SC_INVOICE_TYPE));
 		invoice.put(InvoiceBean.payInvoiceComment, params.get(InvoiceBean.payInvoiceComment));
 		invoice.put(InvoiceBean.payInvoiceDepartment, params.get(InvoiceBean.payInvoiceDepartment));
 		invoice.put(InvoiceBean.payInvoicePlanDate, params.get(InvoiceBean.payInvoicePlanDate));
 		invoice.put(InvoiceBean.payInvoiceReceivedMoneyStatus, params.get(InvoiceBean.payInvoiceReceivedMoneyStatus));
 		invoice.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusSubmit);
 		invoice.put(InvoiceBean.payInvoiceSubmitDate, DateUtil.getDateString(new Date()));
 		invoice.put("projectId", params.get("projectId"));
 		invoice.put("contractCode", params.get("contractCode"));
 		
 		//add salesCotract's contractType into invoice, so department manager can approve their invoice request
 		Map<String,Object> sc = dao.findOne(ApiConstants.MONGO_ID, params.get(InvoiceBean.salesContractId), DBBean.SALES_CONTRACT);
 		invoice.put(SalesContractBean.SC_TYPE, sc.get(SalesContractBean.SC_TYPE));
 		
 		List<Map<String,Object>> items = (List<Map<String,Object>>) params.get(InvoiceBean.payInvoiceItemList);
 		double total = 0.0;
 		for(Map<String,Object> item : items){
 			total += ApiUtil.getDouble(item, InvoiceBean.itemMoney, 0);
 		}
 		invoice.put(InvoiceBean.payInvoiceItemList, items);
 		invoice.put(InvoiceBean.payInvoiceMoney, total);
 		invoice.put(InvoiceBean.payInvoiceProposerId, ApiThreadLocal.getCurrentUserId());
 		return dao.add(invoice, DBBean.SC_INVOICE);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String,Object> prepareInvoiceForSC(Map<String, Object> params){
 		String scId = (String)params.get("contractCode");
 		List<String> ids = new ArrayList<String>();
 		ids.add(scId);
 		Map<String,Object> map = getBaseInfoByIds(ids);
 		Map<String,Object> info = (Map<String,Object>)map.get(scId);
 		Map<String,Object> newObj = new HashMap<String,Object>();
 		newObj.put(InvoiceBean.salesContractId, scId);
 		newObj.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusUnSubmit);
 		newObj.put(InvoiceBean.payInvoiceItemList, new ArrayList());
 		newObj.put(InvoiceBean.payInvoiceSubmitDate, DateUtil.getDateString(new Date()));
 		newObj.putAll(info);
 		
 		////////////////////////////////////
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(InvoiceBean.salesContractId, scId);
 		query.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusDone);
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{InvoiceBean.payInvoiceMoney});
 		List<Object> payInvoiceList = dao.listLimitKeyValues(query, DBBean.SC_INVOICE);
 		Double totalPayInvoiceMoney = 0.0;//统计已开票的总金额
 		for(Object obj : payInvoiceList){
 			totalPayInvoiceMoney += ApiUtil.getDouble(String.valueOf(obj));
 		}
 		//【临时字段】合同金额， 已开票额、 已收款额、 应收账款额、付款方式
 		newObj.put("totalPayInvoiceMoney", totalPayInvoiceMoney);
 		newObj.put("totalGotMoney", 0);
 		newObj.put("leftGotMoney", 0);
 		newObj.put("scPayType", "现金");
 		return newObj;
 	}
 	
 	@Override
 	public Map<String, Object> approveInvoiceForSC(Map<String, Object> params) {
 		String id = (String) params.get(ApiConstants.MONGO_ID);
 		Map<String,Object> payInvoice = dao.findOne(ApiConstants.MONGO_ID, id, DBBean.SC_INVOICE);
 		String oldStatus = String.valueOf(payInvoice.get(InvoiceBean.payInvoiceStatus));
 		String creatorId = String.valueOf(payInvoice.get(ApiConstants.CREATOR));
 		
 		//check creator permission
 		Map<String,Object> uInvoice = new HashMap<String,Object>();
 		uInvoice.put(ApiConstants.MONGO_ID, id);
 		
 		if(InvoiceBean.statusSubmit.equals(oldStatus)){
 			uInvoice.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusManagerApprove);
 		} else if(InvoiceBean.statusManagerApprove.equals(oldStatus)) {
 			uInvoice.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusFinanceApprojve);
 			uInvoice.put(InvoiceBean.payInvoiceManagerId, ApiThreadLocal.getCurrentUserId());
 		} else if(InvoiceBean.statusFinanceApprojve.equals(oldStatus)){
 			uInvoice.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusDone);
 			uInvoice.put(InvoiceBean.payInvoiceActualMoney, params.get(InvoiceBean.payInvoiceActualMoney));
 			uInvoice.put(InvoiceBean.payInvoiceActualDate, params.get(InvoiceBean.payInvoiceActualDate));
 			uInvoice.put(InvoiceBean.payInvoiceActualInvoiceNum, params.get(InvoiceBean.payInvoiceActualInvoiceNum));
 			uInvoice.put(InvoiceBean.payInvoiceActualSheetCount, params.get(InvoiceBean.payInvoiceActualSheetCount));
 		} else {
 			throw new ApiResponseException("No Permission","No Permission");
 		}
 		return dao.updateById(uInvoice, DBBean.SC_INVOICE);
 	}
 
 	@Override
 	public Map<String, Object> rejectInvoiceForSC(Map<String, Object> params) {
 		Map<String,Object> payInvoice = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SC_INVOICE);
 		String status = String.valueOf(payInvoice.get(InvoiceBean.payInvoiceStatus));
 		if(!InvoiceBean.statusSubmit.equals(status) && !InvoiceBean.statusManagerApprove.equals(status)){
 			//exception status not right.	
 		}
 		payInvoice.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusReject);
 		return dao.updateById(payInvoice, DBBean.SC_INVOICE);
 	}	
 	
 	@Override
 	public Map<String, Object> loadInvoiceForSC(Map<String, Object> params) {
 		Map<String,Object> newObj = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SC_INVOICE);
 		String scId = (String)newObj.get(InvoiceBean.salesContractId);
 		List<String> ids = new ArrayList<String>();
 		ids.add(scId);
 		Map<String,Object> map = getBaseInfoByIds(ids);
 		Map<String,Object> info = (Map<String,Object>)map.get(scId);
 		newObj.putAll(info);
 		
 		////////////////////////////////////
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(InvoiceBean.salesContractId, scId);
 		query.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusDone);
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{InvoiceBean.payInvoiceMoney});
 		List<Object> payInvoiceList = dao.listLimitKeyValues(query, DBBean.SC_INVOICE);
 		Double totalPayInvoiceMoney = 0.0;//统计已开票的总金额
 		for(Object obj : payInvoiceList){
 			totalPayInvoiceMoney += ApiUtil.getDouble(String.valueOf(obj));
 		}
 		//【临时字段】合同金额， 已开票额、 已收款额、 应收账款额、付款方式
 		newObj.put("totalPayInvoiceMoney", totalPayInvoiceMoney);
 		newObj.put("totalGotMoney", 0);
 		newObj.put("leftGotMoney", 0);
 		newObj.put("scPayType", "现金");
 		return newObj;
 	}
 
 	@Override
 	public Map<String, Object> listInvoiceForSC(Map<String, Object> params) {
/*		DBObject exp0 = null;
 		DBObject exp1 = null;
 		Map<String,Object> query0 = new HashMap<String,Object>();
 		query0.put(InvoiceBean.payInvoiceProposerId, getCurrentUserId());
 		exp0 = DBQueryUtil.buildQueryObject(query0, true);
 		 
 		Map<String,Object> query1 = new HashMap<String,Object>();
		if(true){//是部门经理
 			query1.put(InvoiceBean.payInvoiceStatus, InvoiceBean.statusSubmit);
 			List<String> typeList = new ArrayList<String>();
 			if(isInDepartment(UserBean.USER_DEPARTMENT_PROJECT)){//工程部门
 				typeList.add("弱电工程");
 				typeList.add("产品集成（灯控/布线）");
 				typeList.add("产品集成（楼控）");
 				typeList.add("产品集成（其他）");
 			}
 			if(isInDepartment(UserBean.USER_DEPARTMENT_SALES)){
 				typeList.add("维护及服务");
 				typeList.add("产品销售（灯控/布线）");
 				typeList.add("产品销售（楼控）");
 				typeList.add("产品销售（其他）");
 			}
 			query1.put("contractType", new DBQuery(DBQueryOpertion.IN, typeList));
 			exp1 = DBQueryUtil.buildQueryObject(query1, true);
 		} else if(isFinance()){
 			List<String> list = new ArrayList<String>();
 			list.add(InvoiceBean.statusManagerApprove);
 			list.add(InvoiceBean.statusFinanceApprojve);
 			list.add(InvoiceBean.statusDone);
 			query1.put(InvoiceBean.payInvoiceStatus, new DBQuery(DBQueryOpertion.IN, list));
 			exp1 = DBQueryUtil.buildQueryObject(query1, true);
 		}
 		Map<String,Object> query3 = new HashMap<String,Object>();
 		if(exp0 != null){
 			query3.put("exp0", exp0);
 		}
 		if(exp1 != null){
 			query3.put("exp1", exp1);
 		}
 		DBObject searchExp = DBQueryUtil.buildQueryObject(query3, false);
		return dao.list(null,searchExp, DBBean.SC_INVOICE);*/
		return dao.list(null, DBBean.SC_INVOICE);
 	}
 	
 	@Override
 	public Map<String, Object> addGotMoneyForSC(Map<String, Object> params) {
 		String _id = (String) params.get(ApiConstants.MONGO_ID);
 		if (_id == null || _id.length() == 0){
 			Map<String, Object> gm = new HashMap<String, Object>();
 			gm.put(SalesContractBean.SC_GOT_MONEY, params.get(SalesContractBean.SC_GOT_MONEY));
 			gm.put(SalesContractBean.SC_GOT_MONEY_DATE, params.get(SalesContractBean.SC_GOT_MONEY_DATE));
 			gm.put(SalesContractBean.SC_ID, params.get(SalesContractBean.SC_ID));
 			return dao.add(gm, DBBean.SC_GOT_MONEY);
 		}else{
 			
 		}
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> listGotMoneyForSC(Map<String, Object> params) {
 		String scId = (String) params.get(SalesContractBean.SC_ID);
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(SalesContractBean.SC_ID, scId);
 		return dao.list(query, DBBean.SC_GOT_MONEY);
 	}
 
 	@Override
 	public Map<String, Object> getRelatedProjectInfo(Map<String, Object> params) {
 		String scId = (String) params.get(SalesContractBean.SC_ID);
 		Map<String, Object> querySC = new HashMap<String, Object>();
 		querySC.put(ApiConstants.MONGO_ID, scId);
 		querySC.put(ApiConstants.LIMIT_KEYS, new String[] {SalesContractBean.SC_PROJECT_ID});
 		Map<String, Object> sc = dao.findOneByQuery(querySC, DBBean.SALES_CONTRACT);
 		
 		String pId = (String) sc.get(SalesContractBean.SC_PROJECT_ID);
 		return dao.findOne(ApiConstants.MONGO_ID, pId, DBBean.PROJECT);
 	}
 
 	@Override
 	public Map<String, Object> addMonthShipmentsForSC(Map<String, Object> params) {
 		String _id = (String) params.get(ApiConstants.MONGO_ID);
 		if (_id == null || _id.length() == 0){//Add
 			Map<String, Object> ms = new HashMap<String, Object>();
 			ms.put(SalesContractBean.SC_SHIPMENTS_MONEY, params.get(SalesContractBean.SC_SHIPMENTS_MONEY));
 			ms.put(SalesContractBean.SC_MONTH_SHIPMENTS_MONTH, params.get(SalesContractBean.SC_MONTH_SHIPMENTS_MONTH));
 			ms.put(SalesContractBean.SC_ID, params.get(SalesContractBean.SC_ID));
 			return dao.add(ms, DBBean.SC_MONTH_SHIPMENTS);
 		}else{//Update
 			
 		}
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> listMonthShipmentsForSC(
 			Map<String, Object> params) {
 		String scId = (String) params.get(SalesContractBean.SC_ID);
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(SalesContractBean.SC_ID, scId);
 		return dao.list(query, DBBean.SC_MONTH_SHIPMENTS);
 	}
 
 	/**
 	 * 根据销售合同_id，获取销售合同相关的 信息：销售合同编号，客户名称
 	 * 支持批量获取
 	 */
 	@Override
 	public Map<String, Object> getSCAndCustomerInfo(Map<String, Object> params) {
 		Object scId = params.get(SalesContractBean.SC_ID);
 		List<String> scIds = new ArrayList<String>();
 		if (scId instanceof String){
 			scIds.add(scId.toString());
 		}else{
 			scIds = (List<String>) scId;
 		}
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, scIds));
 		query.put(ApiConstants.LIMIT_KEYS, new String[] {SalesContractBean.SC_CODE, SalesContractBean.SC_PROJECT_ID});
 		Map<String, Object> result = dao.list(query, DBBean.SALES_CONTRACT);
 		List<Map<String, Object>> resultListData = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA); 
 		
 		List<String> pids = new ArrayList<String>();
 		for (Map<String, Object> sc : resultListData){
 			String pId = (String) sc.get(SalesContractBean.SC_PROJECT_ID);
 			if (!ApiUtil.isEmpty(pId)){
 				pids.add(pId);
 			}
 		}
 		Map<String, Object> pQuery = new HashMap<String, Object>();
 		pQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pids));
 		pQuery.put(ApiConstants.LIMIT_KEYS, new String[] {ProjectBean.PROJECT_CUSTOMER});
 		Map<String, Object> pResult = dao.listToOneMapAndIdAsKey(pQuery, DBBean.PROJECT);
 //		List<Map<String, Object>> pResultListData = (List<Map<String, Object>>) pResult.get(ApiConstants.RESULTS_DATA);
 		pResult.remove(ApiConstants.RESULTS_DATA);
 		pResult.remove(ApiConstants.PAGENATION);
 		
 		List<String> cids = new ArrayList<String>();
 		for (Entry<String, Object> en:pResult.entrySet()){
 			Map<String, Object> p = (Map<String, Object>) en.getValue();
 			String cId = (String) p.get(ProjectBean.PROJECT_CUSTOMER);
 			if (!ApiUtil.isEmpty(cId)){
 				cids.add(cId);
 			}
 		}
 		
 		Map<String, Object> cusQuery = new HashMap<String, Object>();
 		cusQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, cids));
 		cusQuery.put(ApiConstants.LIMIT_KEYS, new String[] {CustomerBean.NAME});
 		Map<String, Object> cResult = dao.listToOneMapAndIdAsKey(cusQuery, DBBean.CUSTOMER);
 		
 		for (Entry<String, Object> en:pResult.entrySet()){
 			Map<String, Object> p = (Map<String, Object>) en.getValue();
 			Map<String, Object> c = (Map<String, Object>) cResult.get(p.get(ProjectBean.PROJECT_CUSTOMER));
 			p.put(ProjectBean.PROJECT_CUSTOMER, c.get(CustomerBean.NAME));
 		}
 		
 		for (Map<String, Object> sc : resultListData){
 			String pId = (String) sc.get(SalesContractBean.SC_PROJECT_ID);
 			Map<String, Object> p = (Map<String, Object>) pResult.get(pId);
 			sc.put(ProjectBean.PROJECT_CUSTOMER, p.get(ProjectBean.PROJECT_CUSTOMER));
 		}
 		
 		return result;
 	}
 
 	@Override
 	public Map<String, Object> getSCeqByIds(Map<String, Object> params) {
 		Object eqIds = params.get("eqIds");
 		Map<String, Object> query = new HashMap<String, Object>();
 		List<String> ids = null;
 		if (eqIds instanceof String){
 			query.put(ApiConstants.MONGO_ID, (String)eqIds);
 		} else {
 			ids = (List<String>) eqIds;
 			query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, ids));
 		}
 		return dao.list(query, DBBean.EQ_COST);
 	}
 
 	@Override
 	public Map<String, Object> listSCByProject(Map<String, Object> params) {
 		String pId = (String) params.get(SalesContractBean.SC_PROJECT_ID);
 		if (ApiUtil.isEmpty(pId)){
 			throw new ApiResponseException(String.format("Project id is empty", params), ResponseCodeConstants.PROJECT_ID_IS_EMPTY.toString());
 		}
 		Map<String, Object> project = dao.findOne(ApiConstants.MONGO_ID, pId, DBBean.PROJECT);
 		Map<String, Object> customer = dao.findOne(ApiConstants.MONGO_ID, project.get(ProjectBean.PROJECT_CUSTOMER), DBBean.CUSTOMER);
 		String cName = (String) customer.get(CustomerBean.NAME);
 
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(SalesContractBean.SC_PROJECT_ID, pId);
 		query.put(ApiConstants.LIMIT_KEYS, new String[] {SalesContractBean.SC_CODE});
 		Map<String, Object> result = dao.list(query, DBBean.SALES_CONTRACT);
 		List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		for (Map<String, Object> sc : resultList){
 			sc.put(ProjectBean.PROJECT_CUSTOMER, cName);
 		}
 		return result;
 	}
 	
 	
 	   
     public void mergeCommonFieldsFromSc(Map<String, Object> data, Object scId){
         Map<String, Object> scQuery = new HashMap<String, Object>();
         scQuery.put(ApiConstants.MONGO_ID, scId);
         scQuery.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_TYPE});
         
         Map<String, Object> sc = this.dao.findOneByQuery(scQuery, DBBean.SALES_CONTRACT);
         data.put(SalesContractBean.SC_PROJECT_ID, sc.get(SalesContractBean.SC_PROJECT_ID));
         data.put(SalesContractBean.SC_TYPE, sc.get(SalesContractBean.SC_TYPE));
        
         mergeCommonProjectInfo(data, sc.get(SalesContractBean.SC_PROJECT_ID));              
  
     }
 
     private void mergeCommonProjectInfo(Map<String, Object> data, Object projectId) {
         Map<String, Object> projectQuery = new HashMap<String, Object>();
         projectQuery.put(ApiConstants.MONGO_ID, projectId);       
         projectQuery.put(ApiConstants.LIMIT_KEYS, new String[]{ProjectBean.PROJECT_MANAGER, ProjectBean.PROJECT_CUSTOMER, ProjectBean.PROJECT_TYPE});
               
         Map<String, Object> project = this.dao.findOneByQuery(projectQuery, DBBean.PROJECT);
         data.put(ProjectBean.PROJECT_MANAGER, project.get(ProjectBean.PROJECT_MANAGER));
         data.put(ProjectBean.PROJECT_CUSTOMER, project.get(ProjectBean.PROJECT_CUSTOMER));
         data.put(ProjectBean.PROJECT_TYPE, project.get(ProjectBean.PROJECT_TYPE));
     }
 }
