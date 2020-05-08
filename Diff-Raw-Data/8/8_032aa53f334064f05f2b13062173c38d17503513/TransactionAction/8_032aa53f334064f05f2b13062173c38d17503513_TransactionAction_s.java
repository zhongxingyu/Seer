 package com.appnomic.noc.action.transaction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.struts2.convention.annotation.ParentPackage;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Namespace;
 
 import com.appnomic.domainobject.ApplicationData;
 import com.appnomic.domainobject.Cluster;
 import com.appnomic.domainobject.Cluster.ComponentData;
 import com.appnomic.domainobject.Component;
 import com.appnomic.domainobject.Host;
 import com.appnomic.domainobject.Transaction;
 import com.appnomic.domainobject.TransactionGroup;
 import com.appnomic.domainobject.TransactionSummary;
 import com.appnomic.noc.action.AbstractNocAction;
 import com.appnomic.noc.action.TimeUtility;
 import com.appnomic.noc.viewobject.availability.ClusterVO;
 import com.appnomic.noc.viewobject.availability.ComponentDataVO;
 import com.appnomic.noc.viewobject.availability.ComponentVO;
 import com.appnomic.noc.viewobject.availability.InstanceVO;
 import com.appnomic.noc.viewobject.transaction.ApplicationVO;
 import com.appnomic.noc.viewobject.transaction.TransactionAppDataVO;
 import com.appnomic.noc.viewobject.transaction.TransactionDataVO;
 import com.appnomic.noc.viewobject.transaction.TransactionGroupDataVO;
 import com.appnomic.noc.viewobject.transaction.TransactionGroupVO;
 import com.appnomic.noc.viewobject.transaction.TransactionVO;
 import com.appnomic.service.ApplicationDataService;
 import com.appnomic.service.ClusterDataService;
 import com.appnomic.service.ComponentDataService;
 import com.appnomic.service.TransactionDataService;
 
 /*
  * This action always retrieves the 'Meta' for the Grid to be displayed
  * The retrieved data is sent back to client as JSON - refer to the XML to find json there
  * Note: there is NO Query parameter here. The exact meta to send back is configured by the user in a 
  * separate configuration console. So this Action gets input from that configuration
  */
 @SuppressWarnings("serial")
 @ParentPackage("json-default")
 @Namespace("/transaction")
 public class TransactionAction extends AbstractNocAction  {
 
 	private ApplicationDataService applicationDataService;
 	private TransactionDataService transactionDataService;
 	
 	private ApplicationVO [] applicationVO;
 	private TransactionDataVO txDataVO;
 	private TransactionAppDataVO appDataVO;
 	private Map<String, String[]> param;
 	
 	enum TxResponseStatus {
 		OKAY, SLOW, FAIL
 	};
 	
 	public TransactionDataService getTransactionDataService() {
 		return transactionDataService;
 	}
 
 	public void setTransactionDataService(
 			TransactionDataService transactionDataService) {
 		this.transactionDataService = transactionDataService;
 	}
 
 	public TransactionAppDataVO getAppDataVO() {
 		return appDataVO;
 	}
 
 	public void setAppDataVO(TransactionAppDataVO appDataVO) {
 		this.appDataVO = appDataVO;
 	}
 
 	public ApplicationDataService getApplicationDataService() {
 		return applicationDataService;
 	}
 
 	public void setApplicationDataService(
 			ApplicationDataService applicationDataService) {
 		this.applicationDataService = applicationDataService;
 	}
 
 	public TransactionDataVO getTxDataVO() {
 		return txDataVO;
 	}
 
 	public void setTxDataVO(TransactionDataVO txDataVO) {
 		this.txDataVO = txDataVO;
 	}
 
 	public ApplicationVO[] getApplicationVO() {
 		return applicationVO;
 	}
 
 	public void setApplicationVO(ApplicationVO[] applicationVO) {
 		this.applicationVO = applicationVO;
 	}
 
 	public TransactionAction() {
 	}
 	
 	public Map<String, String[]> getParam() {
 		return param;
 	}
 
 	public void setParam(Map<String, String[]> param) {
 		this.param = param;
 	}
 	
 	public String nocAction() {
 		return SUCCESS;
 	}
 	
 	private void setDummy(int id, String transactionName){
 		txDataVO = new TransactionDataVO();
 		txDataVO.setAlerts(true);
 		txDataVO.setStatus(TxResponseStatus.SLOW.name());
 		txDataVO.setTxId(id);
 		txDataVO.setTxName(transactionName);
 		txDataVO.setVolume("2k");
 		txDataVO.setResponse("1ms");
 	}
 	
 	@Action(value="/transaction/Data", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "parameters,session,SUCCESS,ERROR,transactionDataService,applicationDataService,applicationVO,appDataVO",
 	        		"enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String transactionData() {
 		param = getParameters();
 		
 		String keyVal = "Transaction Data: ";
 		for(String key : parameters.keySet()) {
 			keyVal += "[ " + key + " = ";
 			for(String value : parameters.get(key)) {
 				keyVal += value + ", ";
 			}
 			keyVal += "] ";
 		}
 		System.out.println("key value map = " + keyVal);
 		
 		String transactionName = (parameters.get("name")[0]);
 		int id = Integer.parseInt(parameters.get("id")[0]);
 		System.out.println("transaction data being assembled = " + transactionName);
 		
 		setDummy(id, transactionName);
 		return SUCCESS;
 	}
 	
 	private void setDummyAppData(int appId, String appName) {
 		appDataVO = new TransactionAppDataVO();
 		appDataVO.setAppName(appName);
 		appDataVO.setId(appId);
 		
 		int groups = 2;
 		TransactionGroupDataVO [] txGroupVO = new TransactionGroupDataVO [groups];
 		appDataVO.setTxGroupVO(txGroupVO);
 		
 		for(int i=0;i<groups;i++) {
 			txGroupVO[i] = new TransactionGroupDataVO();
 			txGroupVO[i].setGroupName("txGroup_"+i);
 			txGroupVO[i].setId(i);
 			
 			int txCount = 3;
 			TransactionDataVO [] txVO = new TransactionDataVO[txCount];
 			txGroupVO[i].setTxDataVO(txVO);
 			
 			for(int j=0;j<txCount;j++) {
 				txVO[j] = new TransactionDataVO();
 				txVO[j].setTxId(j);
 				txVO[j].setTxName("txName_"+j);
 				txVO[j].setVolume("2k");
 				txVO[j].setAlerts(true);
 				txVO[j].setStatus(TxResponseStatus.FAIL.name());
 				txVO[j].setResponse("0.1ms");
 			}
 		}
 	}
 	
 	@Action(value="/transaction/AppData", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "parameters,session,SUCCESS,ERROR,transactionDataService,applicationDataService,applicationVO,txDataVO",
 	        		"enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String transactionAppData() {
 		param = getParameters();
 		
 		String keyVal = "Transaction App Data: ";
 		for(String key : parameters.keySet()) {
 			keyVal += "[ " + key + " = ";
 			for(String value : parameters.get(key)) {
 				keyVal += value + ", ";
 			}
 			keyVal += "] ";
 		}
 		System.out.println("key value map = " + keyVal);
 		
 		String appName = (parameters.get("name")[0]);
 		int id = Integer.parseInt(parameters.get("id")[0]);
 		System.out.println("transaction app data being assembled for app = " + appName);
 		
 		//setDummyAppData(id, appName);
 		
 		appDataVO = new TransactionAppDataVO();
 		appDataVO.setAppName(appName);
 		appDataVO.setId(id);
 		
 		String[] startEndTimes = TimeUtility.get5MinStartEnd();
 		Map<Integer, TransactionSummary> txSummary = transactionDataService.getTransactionSummaryForApp(id, startEndTimes[0], startEndTimes[1]);
 		
 		
 		List<Transaction> appTransactions = transactionDataService.getTransactionPerApplication(id);
 		Map<String, ArrayList<Transaction>> txGroupMap = new HashMap<String, ArrayList<Transaction>>();
 		for(Transaction appTx : appTransactions) {
 			ArrayList<Transaction> txList = txGroupMap.get(appTx.getGroupName());
 			if(txList == null || txList.size() == 0) {
 				txList = new ArrayList<Transaction>();
 				txGroupMap.put(appTx.getGroupName(), txList);
 			}
 			txList.add(appTx);
 		}
 		
 		int j = 0;
 		TransactionGroupDataVO [] transactionGroups = new TransactionGroupDataVO[txGroupMap.size()];
 		appDataVO.setTxGroupVO(transactionGroups);
 		
 		Set<String> groupNames = txGroupMap.keySet();
 		for(String groupName:groupNames){
 			transactionGroups[j] = new TransactionGroupDataVO();
 			transactionGroups[j].setGroupName(groupName);
 			transactionGroups[j].setId(0);
 			
 			List<Transaction> txList = txGroupMap.get(groupName);
 			TransactionDataVO [] transactions = new TransactionDataVO[txList.size()];
 			transactionGroups[j].setTxDataVO(transactions);
 			
 			int k = 0;
 			for(Transaction tx: txList) {
 				transactions[k] = new TransactionDataVO();
 				transactions[k].setTxId(tx.getId());
 				transactions[k].setTxName(tx.getName());
 				
 				if(txSummary != null) {
 					TransactionSummary summary = txSummary.get(tx.getId());
 					if( summary.getAlertsCount() == null || summary.getAlertsCount() == 0) {
 						transactions[k].setAlerts(false);	
 					} else {
 						transactions[k].setAlerts(true); // true ==> alerts are present
 					}
 					
 					if(summary.getFailedCount() > 0 || summary.getTimedOutCount()>0) {
 						transactions[k].setStatus(TxResponseStatus.FAIL.name());
 					} else if(summary.getSlowCount() > 0) {
 						transactions[k].setStatus(TxResponseStatus.SLOW.name());
 					} else {
 						transactions[k].setStatus(TxResponseStatus.OKAY.name());
 					}
 					
 					transactions[k].setResponse(summary.getAvgResponseTime().toString());
 					transactions[k].setVolume(summary.getVolume().toString());
 				} else {
					transactions[j].setVolume("2k");
					transactions[j].setAlerts(true);
					transactions[j].setStatus(TxResponseStatus.FAIL.name());
					transactions[j].setResponse("0.1ms");
 				}
 				k++;
 			}
 			j++;
 		}
 		return SUCCESS;
 	}
 	
 	public void setDummyMeta() {
 		int appCount = 10;
 		applicationVO = new ApplicationVO[appCount];
 		for(int i=0;i<10;i++) {
 			applicationVO[i] = new ApplicationVO();
 			applicationVO[i].setApplicationName("App_"+i);
 			applicationVO[i].setId(i);
 		
 			int txGroupCount = 5;
 			TransactionGroupVO [] transactionGroups = new TransactionGroupVO[txGroupCount];
 			applicationVO[i].setTransactionGroups(transactionGroups);
 			for(int j=0;j<txGroupCount;j++) {
 				transactionGroups[j] = new TransactionGroupVO();
 				transactionGroups[j].setGroupName("Group_"+j);
 				transactionGroups[j].setId(j);
 				
 				int txCount = 8; // 10 * 5 * 8 = 400 = approx num of Tx in HDFC
 				TransactionVO [] transactions = new TransactionVO[txCount];
 				transactionGroups[j].setTransactions(transactions);
 				
 				for(int k=0;k<txCount;k++) {
 					transactions[k] = new TransactionVO();
 					transactions[k].setId(k);
 					transactions[k].setName("tx_"+k);
 				}
 			}
 		}	
 	}
 
 	@Action(value="/transaction/Meta", results = {
 	        @Result(name="success", type="json", params = {
 	        		"excludeProperties",
 	                "parameters,session,SUCCESS,ERROR,transactionDataService,applicationDataService,txDataVO,appDataVO",
 	        		"enableGZIP", "true",
 	        		"encoding", "UTF-8",
 	                "noCache","true",
 	                "excludeNullProperties","true"
 	            })})
 	public String transactionMeta() {
 		param = getParameters();
 		
 		List<ApplicationData> applications = applicationDataService.getAll();
 		applicationVO = new ApplicationVO[applications.size()];
 		int i = 0;
 		for(ApplicationData application : applications) {
 			applicationVO[i] = new ApplicationVO();
 			applicationVO[i].setApplicationName(application.getName());
 			applicationVO[i].setId(application.getId());
 			
 			List<Transaction> appTransactions = transactionDataService.getTransactionPerApplication(application.getId());
 			Map<String, ArrayList<Transaction>> txGroupMap = new HashMap<String, ArrayList<Transaction>>();
 			for(Transaction appTx : appTransactions) {
 				ArrayList<Transaction> txList = txGroupMap.get(appTx.getGroupName());
 				if(txList == null || txList.size() == 0) {
 					txList = new ArrayList<Transaction>();
 					txGroupMap.put(appTx.getGroupName(), txList);
 				}
 				txList.add(appTx);
 				System.out.println("added tx = " + appTx.getName() + " of group = " + appTx.getGroupName() + " of app = " + appTx.getAppName());
 			}
 			
 			int j = 0;
 			TransactionGroupVO [] transactionGroups = new TransactionGroupVO[txGroupMap.size()];
 			applicationVO[i].setTransactionGroups(transactionGroups);
 			
 			Set<String> groupNames = txGroupMap.keySet();
 			for(String groupName:groupNames){
 				transactionGroups[j] = new TransactionGroupVO();
 				transactionGroups[j].setGroupName(groupName);
 				transactionGroups[j].setId(0);
 				
 				List<Transaction> txList = txGroupMap.get(groupName);
 				TransactionVO [] transactions = new TransactionVO[txList.size()];
 				transactionGroups[j].setTransactions(transactions);
 				
 				int k = 0;
 				for(Transaction tx: txList) {
 					transactions[k] = new TransactionVO();
 					transactions[k].setId(tx.getId());
 					transactions[k].setName(tx.getName());
 					k++;
 				}
 				j++;
 			}
 			i++;
 		}
 		return SUCCESS;
 	}
 
 	@Override
 	public void setDummyData() {
 		// TODO Auto-generated method stub		
 	}
 	
 }
