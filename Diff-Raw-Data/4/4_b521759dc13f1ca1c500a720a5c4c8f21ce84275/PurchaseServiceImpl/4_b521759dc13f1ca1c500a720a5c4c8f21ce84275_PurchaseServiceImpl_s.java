 package com.pms.service.service.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import com.google.gson.Gson;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 
 public class PurchaseServiceImpl extends AbstractService implements IPurchaseService {
 
     private static final Logger logger = LogManager.getLogger(PurchaseServiceImpl.class);
 	
     private ISalesContractService salesContractService;
     
     /**
      * @param scId
      */
 	public Map<String, Object> prepareBack(Map<String, Object> params) {
 		Map<String,Object> request = new LinkedHashMap<String,Object>();
 		request.put(PurchaseBack.pbStatus, PurchaseStatus.unsaved.toString());
 		request.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		mergeSalesContract(request);
 		mergeEqcost(request);
 		return request;
 	}
 
     /**
      * @param _id
      */
 	public Map<String, Object> loadBack(Map<String, Object> params) {
 		Map<String,Object> request = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 		mergeSalesContract(request);
 		mergeEqcost(request);
 		return request;
 	}
 
     /**
      * @param back object info
      */
 	public Map<String, Object> saveBack(Map<String, Object> params) {
 		Map<String,Object> newObj = new HashMap<String,Object>();
 		newObj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		newObj.put(PurchaseBack.pbDepartment, params.get(PurchaseBack.pbDepartment));
 		newObj.put(PurchaseBack.pbType, params.get(PurchaseBack.pbType));
 		newObj.put(PurchaseBack.pbComment, params.get(PurchaseBack.pbComment));
 		newObj.put(PurchaseBack.pbPlanDate, params.get(PurchaseBack.pbPlanDate));
 		newObj.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		newObj.put(PurchaseBack.pbStatus, PurchaseStatus.saved.toString());
 		newObj.putAll(countEqcostList(params));
 		
 		Map<String,Object> sc = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.scId), new String[]{SalesContractBean.SC_CODE},DBBean.SALES_CONTRACT);
 		newObj.put(PurchaseBack.scCode, sc.get(SalesContractBean.SC_CODE));
 		
 		if(params.get(PurchaseBack.pbCode) == null) {
 			newObj.put(PurchaseBack.pbCode, generateCode("BHSQ", DBBean.PURCHASE_BACK));
 		}
 		
 		if(params.get(ApiConstants.MONGO_ID) == null){
 			return dao.add(newObj, DBBean.PURCHASE_BACK);
 		}else{
 			return dao.updateById(newObj, DBBean.PURCHASE_BACK);
 		}
 	}
 
 	@Override
 	public Map<String, Object> submitBack(Map<String, Object> params) {
 		Map<String,Object> newObj = new HashMap<String,Object>();
 		newObj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		newObj.put(PurchaseBack.pbDepartment, params.get(PurchaseBack.pbDepartment));
 		newObj.put(PurchaseBack.pbType, params.get(PurchaseBack.pbType));
 		newObj.put(PurchaseBack.pbComment, params.get(PurchaseBack.pbComment));
 		newObj.put(PurchaseBack.pbPlanDate, params.get(PurchaseBack.pbPlanDate));
 		newObj.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		newObj.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());
 		newObj.put(PurchaseBack.pbSubmitDate, DateUtil.getDateString(new Date()));
 		newObj.putAll(countEqcostList(params));
 		
 		
 		Map<String,Object> sc = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.scId), new String[]{SalesContractBean.SC_CODE},DBBean.SALES_CONTRACT);
 		newObj.put(PurchaseBack.scCode, sc.get(SalesContractBean.SC_CODE));
 		
 		if(params.get(PurchaseBack.pbCode) == null) {
 			newObj.put(PurchaseBack.pbCode, generateCode("BHSQ", DBBean.PURCHASE_BACK));
 		}
 		
 		dao.updateCount(ApiConstants.MONGO_ID, params.get(PurchaseBack.scId), SalesContractBean.SC_BACK_REQUEST_COUNT, DBBean.SALES_CONTRACT, 1);
 		
 		if(params.get(ApiConstants.MONGO_ID) == null){
 			return dao.add(newObj, DBBean.PURCHASE_BACK);
 		}else{
 			return dao.updateById(newObj, DBBean.PURCHASE_BACK);
 		}
 	}
 
 	@Override
 	public Map<String, Object> pendingBack(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.pbStatus, PurchaseStatus.interruption.toString());
 		if(PurchaseStatus.submited.toString().equals(params.get(PurchaseBack.pbStatus))){
 			dao.updateCount(ApiConstants.MONGO_ID, params.get(PurchaseBack.scId), SalesContractBean.SC_BACK_REQUEST_COUNT, DBBean.SALES_CONTRACT, -1);
 		}
 		return dao.updateById(obj, DBBean.PURCHASE_BACK);
 	}
 
 	///////////////////////////allot 调拨///////////
 	@Override
 	public Map<String, Object> submitAllot(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		obj.put(PurchaseBack.pbId, params.get(PurchaseBack.pbId));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.submited.toString());
 		obj.put(PurchaseBack.paSubmitDate, DateUtil.getDateString(new Date()));
 		obj.put(PurchaseBack.paShelfCode, params.get(PurchaseBack.paShelfCode));
 		obj.put(PurchaseBack.paCode, generateCode("DBSQ", DBBean.PURCHASE_ALLOCATE));
 		obj.putAll(countAllotEqcostList(params));
 		return dao.add(obj, DBBean.PURCHASE_ALLOCATE);
 	}
 
 	public Map<String, Object> loadAllot(Map<String, Object> params) {
 		Map<String,Object> allot = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 		mergeSalesContract(allot);
 		mergeEqcostForAllot(allot);
 		return allot;
 	}
 
 	public Map<String, Object> prepareAllot(Map<String, Object> params) {
 		Map<String,Object> obj = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 		mergeSalesContract(obj);
 		mergeEqcost(obj);
 		obj.put(PurchaseBack.pbId, params.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.unsaved.toString());
 		obj.put(ApiConstants.MONGO_ID, null);
 		obj.put(PurchaseBack.paCode, null);
 		return obj;
 	}
 	
 	public Map<String, Object> approveAllot(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.paComment, params.get(PurchaseBack.paComment));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.approved.toString());
 		obj.put(PurchaseBack.paApproveDate, DateUtil.getDateString(new Date()));
 		//obj.putAll(countAllotEqcostList(params));
 		return dao.updateById(obj, DBBean.PURCHASE_ALLOCATE);
 	}
 	
 	
 	@Override
 	public Map<String, Object> rejectAllot(Map<String, Object> params) {
 		Map<String,Object> allot = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, allot.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.rejected.toString());
 		reduceAllotEqcostList(allot);
 		return dao.updateById(obj, DBBean.PURCHASE_ALLOCATE);
 		
 		
 	}
 	
 	@Override
 	public Map<String, Object> listAllBack(Map<String, Object> params) {
 		String[] keys = new String[]{PurchaseBack.pbCode,PurchaseBack.pbType,PurchaseBack.pbStatus,
 				PurchaseBack.pbMoney,PurchaseBack.scId,PurchaseBack.pbSubmitDate, PurchaseBack.prId, PurchaseBack.poId};
 		params.put(ApiConstants.LIMIT_KEYS, keys);
 		Map<String,Object> map = dao.list(params, DBBean.PURCHASE_BACK);
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		Set<String> saleIds = new HashSet<String>();
 		for(Map<String,Object> re : list){
 			saleIds.add((String)re.get(PurchaseBack.scId));
 		}
 		saleIds.remove(null);
 		Map<String,Object> baseInfoMap = salesContractService.getBaseInfoByIds(new ArrayList(saleIds));
 		for(Map<String,Object> re : list){
 			re.putAll((Map)baseInfoMap.get(re.get(PurchaseBack.scId)));
 		}
 
 		//查询采购申请数据
 		Map<String, Object> prQuery = new HashMap<String, Object>();
 		prQuery.put(ApiConstants.LIMIT_KEYS, PurchaseCommonBean.PURCHASE_REQUEST_CODE);		
 		Map<String, Object> request = this.dao.listToOneMapAndIdAsKey(prQuery, DBBean.PURCHASE_REQUEST);
 
         for (Map<String, Object> re : list) {
             if (re.get(PurchaseBack.prId) != null) {
                 Map<String, Object> prmap = (Map<String, Object>)request.get(re.get(PurchaseBack.prId));                
                 re.put(PurchaseBack.prCode, prmap.get(PurchaseCommonBean.PURCHASE_REQUEST_CODE));
             }
         }
 		  
 		return map;
 	}
 
 	@Override
 	public Map<String, Object> listCheckedBack(Map<String, Object> params) {
 		String[] keys = new String[]{PurchaseBack.pbCode,PurchaseBack.pbType,PurchaseBack.pbStatus,
				PurchaseBack.pbMoney,PurchaseBack.scId,PurchaseBack.pbSubmitDate};
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(ApiConstants.LIMIT_KEYS, keys);
 		query.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());
 		Map<String,Object> map = dao.list(query, DBBean.PURCHASE_BACK);
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		Set<String> saleIds = new HashSet<String>();
 		for(Map<String,Object> re : list){
 			saleIds.add((String)re.get(PurchaseBack.scId));
 		}
 		saleIds.remove(null);
 		Map<String,Object> baseInfoMap = salesContractService.getBaseInfoByIds(new ArrayList(saleIds));
 		for(Map<String,Object> re : list){
 			re.putAll((Map)baseInfoMap.get(re.get(PurchaseBack.scId)));
 		}
 		return map;
 		
 	}
 
 	@Override
 	public Map<String, Object> listAllot(Map<String, Object> params) {
 		Map<String,Object> map = dao.list(null, DBBean.PURCHASE_ALLOCATE);
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		Set<String> saleIds = new HashSet<String>();
 		for(Map<String,Object> re : list){
 			saleIds.add((String)re.get(PurchaseBack.scId));
 		}
 		saleIds.remove(null);
 		if(!saleIds.isEmpty()){
 			Map<String,Object> baseInfoMap = salesContractService.getBaseInfoByIds(new ArrayList(saleIds));
 			for(Map<String,Object> re : list){
 				re.putAll((Map)baseInfoMap.get(re.get(PurchaseBack.scId)));
 			}
 		}
 		return map;		
 	}
 
 
 	@Override
 	public void destoryBack(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.PURCHASE_BACK);
 	}
 
 	
 	private Map<String,Object> mergeSalesContract(Map<String,Object> params){
 		String saleId = (String)params.get(PurchaseBack.scId);
 		List<String> ids = new ArrayList<String>();
 		ids.add(saleId);
 		Map<String,Object> saleInfoMap = salesContractService.getBaseInfoByIds(ids);
 		params.putAll((Map)saleInfoMap.get(saleId));
 		return params;
 	}
 	
 	private void mergeAllotEqcost(Map<String,Object> params){
 		
 	}
 	//加载清单信息
 	@SuppressWarnings("unchecked")
 	private void mergeEqcost(Map<String,Object> params){
 		String status = String.valueOf(params.get(PurchaseBack.pbStatus));
 		
 		//1. 获取合同清单
 		Map<String,Object> eqMap = salesContractService.listEqListBySC(params);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)eqMap.get(ApiConstants.RESULTS_DATA);
 		
 		//2. 获取备货清单
 		List<Map<String,Object>> list2 = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(list2 == null) list2 = new ArrayList<Map<String,Object>>();//预防for异常
 		Map<String,Map<String,Object>> map1 = new HashMap<String,Map<String,Object>>();
 		Map<String,Map<String,Object>> map2 = new HashMap<String,Map<String,Object>>();
 		
 		for(Map<String,Object> obj : list1){
 			map1.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(Map<String,Object> obj : list2){
 			map2.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		
 		//3.1 比较：若已提交，则只加载已提交的清单信息	list1(list2)
 		if(PurchaseStatus.submited.toString().equals(status)){
 			for(String id : map2.keySet()){
 				if(map1.get(id) != null){
 					map2.get(id).putAll(map1.get(id));//加载货物信息
 				}
 			}
 			params.put(PurchaseBack.eqcostList, list2);
 		} else {
 		//3.2 比较：若未提交，加载合同下所有的清单	list2(list1)
 			for(String id : map1.keySet()){
 				if(map2.get(id) != null){
 					map1.get(id).putAll(map2.get(id));//加载货物信息
 				}else{
 					map1.get(id).put(PurchaseBack.pbTotalCount, 0);
 				}
 			}
 			params.put(PurchaseBack.eqcostList, list1);
 		}
 	}
 
 	private void mergeEqcostForAllot(Map<String,Object> params){
 		//1. 获取备货清单
 		Map<String, Object> back = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.pbId), DBBean.PURCHASE_BACK);
 		mergeEqcost(back);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)back.get(PurchaseBack.eqcostList);
 		if(list1 == null) list1 = new ArrayList<Map<String,Object>>();//预防for异常
 		
 		//2. 获取上传的调拨清单
 		List<Map<String,Object>> list2 = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(list2 == null) list2 = new ArrayList<Map<String,Object>>();//预防for异常
 		Map<String,Map<String,Object>> map1 = new HashMap<String,Map<String,Object>>();
 		Map<String,Map<String,Object>> map2 = new HashMap<String,Map<String,Object>>();
 		
 		for(Map<String,Object> obj : list1){
 			map1.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(Map<String,Object> obj : list2){
 			map2.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		
 		List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
 		//3.1 检验货物有效性 ID 数量
 		for(String id : map2.keySet()){
 			if(map1.get(id) != null){
 				map2.get(id).putAll(map1.get(id));
 			}
 		}
 		params.put(PurchaseBack.pbCode, back.get(PurchaseBack.pbCode));
 		params.put(PurchaseBack.pbDepartment, back.get(PurchaseBack.pbDepartment));
 		params.put(PurchaseBack.pbPlanDate, back.get(PurchaseBack.pbPlanDate));
 		params.put(PurchaseBack.pbType, back.get(PurchaseBack.pbType));
 		params.put(PurchaseBack.eqcostList, list2);
 	}	
 	
 	//验证并记录申请货物清单列表，计算申请总额
 	public Map<String,Object> countEqcostList(Map<String,Object> params) {
 		
 		//1. 获取合同清单
 		Map<String, Object> eqMap = salesContractService.listEqListBySC(params);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)eqMap.get(ApiConstants.RESULTS_DATA);
 		
 		//2. 获取上传的备货清单
 		List<Map<String,Object>> list2 = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(list2 == null) list2 = new ArrayList<Map<String,Object>>();//预防for异常
 		Map<String,Map<String,Object>> map1 = new HashMap<String,Map<String,Object>>();
 		Map<String,Map<String,Object>> map2 = new HashMap<String,Map<String,Object>>();
 		
 		for(Map<String,Object> obj : list1){
 			map1.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(Map<String,Object> obj : list2){
 			map2.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		Double money = 0.0;
 		List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
 		//3.1 检验货物有效性 ID 数量
 		for(String id : map2.keySet()){
 			if(map1.get(id) != null){
 				Double leftCount = ApiUtil.getDouble(map1.get(id), SalesContractBean.SC_EQ_LIST_LEFT_AMOUNT,0);
 				Double price = ApiUtil.getDouble(map1.get(id), SalesContractBean.SC_EQ_LIST_BASE_PRICE,0);
 				
 				Double backCount = ApiUtil.getDouble(map2.get(id), PurchaseBack.pbTotalCount,0);
 				String comment =  (String)map2.get(id).get(PurchaseBack.pbComment);
 				if(backCount > leftCount){
 					//throw new ApiResponseException("申请失败，请核实申请数量", ResponseCodeConstants.ADMIN_EDIT_DISABLED); 
 				}
 				money += price*backCount;
 				Map<String,Object> item = new HashMap<String,Object>();
 				item.put(ApiConstants.MONGO_ID, id);
 				item.put(PurchaseBack.pbTotalCount, backCount);
 				item.put(PurchaseBack.pbLeftCount, backCount);
 				item.put(PurchaseBack.pbComment, comment);
 				itemList.add(item);
 			}
 		}
 		Map<String,Object> result = new HashMap<String,Object>();
 		result.put(PurchaseBack.eqcostList, itemList);
 		result.put(PurchaseBack.pbMoney, money);
 		return result;
 	}
 	
 	public Map<String,Object> countAllotEqcostList(Map<String,Object> params) {
 		//1. 获取备货清单
 		Map<String, Object> back = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.pbId), DBBean.PURCHASE_BACK);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)back.get(PurchaseBack.eqcostList);
 		if(list1 == null) list1 = new ArrayList<Map<String,Object>>();//预防for异常
 		
 		//2. 获取上传的调拨清单
 		List<Map<String,Object>> list2 = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(list2 == null) list2 = new ArrayList<Map<String,Object>>();//预防for异常
 		Map<String,Map<String,Object>> map1 = new HashMap<String,Map<String,Object>>();
 		Map<String,Map<String,Object>> map2 = new HashMap<String,Map<String,Object>>();
 		
 		for(Map<String,Object> obj : list1){
 			map1.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(Map<String,Object> obj : list2){
 			map2.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		
 		List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
 		//3.1 检验货物有效性 ID 数量
 		for(String id : map2.keySet()){
 			if(map1.get(id) != null){
 				Double backLeftCount = ApiUtil.getDouble(map1.get(id), PurchaseBack.pbLeftCount,0);
 				
 				Double allotCount = ApiUtil.getDouble(map2.get(id), PurchaseBack.paCount,0);
 				String comment =  (String)map2.get(id).get(PurchaseBack.paComment);
 				if(allotCount > backLeftCount){
 					//throw new ApiResponseException("申请失败，请核实申请数量", ResponseCodeConstants.ADMIN_EDIT_DISABLED); 
 				}
 				Map<String,Object> item = new HashMap<String,Object>();
 				item.put(ApiConstants.MONGO_ID, id);
 				item.put(PurchaseBack.paCount, allotCount);
 				item.put(PurchaseBack.paComment, comment);
 				itemList.add(item);
 				
 				map1.get(id).put(PurchaseBack.pbLeftCount, backLeftCount-allotCount);
 			}
 		}
 		dao.updateById(back, DBBean.PURCHASE_BACK);
 		Map<String,Object> result = new HashMap<String,Object>();
 		result.put(PurchaseBack.eqcostList, itemList);
 		return result;
 	}
 	
 	
 	public void reduceAllotEqcostList(Map<String,Object> params) {
 		//1. 获取备货清单
 		Map<String, Object> back = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.pbId), DBBean.PURCHASE_BACK);
 		List<Map<String,Object>> list1 = (List<Map<String,Object>>)back.get(PurchaseBack.eqcostList);
 		if(list1 == null) list1 = new ArrayList<Map<String,Object>>();//预防for异常
 		
 		//2. 获取上传的调拨清单
 		List<Map<String,Object>> list2 = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(list2 == null) list2 = new ArrayList<Map<String,Object>>();//预防for异常
 		Map<String,Map<String,Object>> map1 = new HashMap<String,Map<String,Object>>();
 		Map<String,Map<String,Object>> map2 = new HashMap<String,Map<String,Object>>();
 		
 		for(Map<String,Object> obj : list1){
 			map1.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(Map<String,Object> obj : list2){
 			map2.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 		}
 		for(String id : map2.keySet()){
 			if(map1.get(id) != null){
 				Double backLeftCount = ApiUtil.getDouble(map1.get(id), PurchaseBack.pbLeftCount,0);
 				Double allotCount = ApiUtil.getDouble(map2.get(id), PurchaseBack.paCount,0);
 				map1.get(id).put(PurchaseBack.pbLeftCount, backLeftCount+allotCount);
 			}
 		}
 		dao.updateById(back, DBBean.PURCHASE_BACK);	
 	}
 	
 	/**{_id:allotCount}*/
 	public Map<String,Double> getAllotEqCountBySalesContractId(String saleId){
 		Map<String,Double> result = new HashMap<String,Double>(); 
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(PurchaseBack.scId, saleId);
 		query.put(PurchaseBack.paStatus, PurchaseStatus.approved.toString());
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{PurchaseBack.eqcostList});
 		Map<String,Object> map = dao.list(query, DBBean.PURCHASE_ALLOCATE);
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		for(Map<String,Object> obj : list){
 			List<Map<String,Object>> eqList = (List<Map<String,Object>>)obj.get(PurchaseBack.eqcostList);
 			for(Map<String,Object> eq : eqList){
 				String key = (String)eq.get(ApiConstants.MONGO_ID);
 				Double value = ApiUtil.getDouble(eq, PurchaseBack.paCount,0);
 				if(result.containsKey(key)){
 					result.put(key, result.get(key)+value);
 				}else{
 					result.put(key, value);
 				}
 			}
 		}
 		return result;
 	}
 	
     @Override
 	public Map<String, Double> getBackEqCountBySalesContractId(String saleId) {
 		Map<String,Double> result = new HashMap<String,Double>(); 
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(PurchaseBack.scId, saleId);
 		query.put(PurchaseBack.pbStatus, PurchaseStatus.approved.toString());
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{PurchaseBack.eqcostList});
 		
 		List<Object> list = dao.listLimitKeyValues(query, DBBean.PURCHASE_ALLOCATE);
 		for(Object obj : list){
 			Map<String, Double> eqMap = (Map<String, Double>)obj;
 			for(Map.Entry<String, Double> entry: eqMap.entrySet()){
 				String key = entry.getKey();
 				Double value = entry.getValue();
 				if(result.containsKey(key)){
 					result.put(key, result.get(key)+value);
 				}else{
 					result.put(key, value);
 				}
 			}
 		}
 		return result;
 	}
 
 	public enum PurchaseStatus {
     	unsaved,saved,submited,approved,checked,rejected,interruption,allotting,allotted,requested;
 		@Override
 		public String toString() {
 			String value = "undefine";
 			switch(this){
 				case unsaved: value="未保存"; break;
 				case saved: value="草稿"; break;
 				case submited: value="已提交"; break;
 				case approved: value="已批准"; break;
 				case checked: value="已审核"; break;
 				case rejected: value="已拒绝"; break;
 				case allotting: value="调拨中"; break;
 				case allotted: value="已调拨"; break;
 				case requested: value="已采购申请"; break;
 				case interruption: value="已中止"; break;
 				default: break;
 			}
 			return value;
 		}
     }
 	
 	public ISalesContractService getSalesContractService() {
 		return salesContractService;
 	}
 
 	public void setSalesContractService(ISalesContractService salesContractService) {
 		this.salesContractService = salesContractService;
 	}
 
 	@Override
 	public String geValidatorFileName() {
 		return "purchase";
 	}
 	
 }
