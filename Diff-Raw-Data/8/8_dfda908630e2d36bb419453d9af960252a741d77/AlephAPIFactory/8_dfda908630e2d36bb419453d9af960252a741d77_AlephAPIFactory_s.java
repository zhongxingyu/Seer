 package org.extensiblecatalog.ncip.v2.aleph.AlephAPI.AlephAPI;
 
 import org.extensiblecatalog.ncip.v2.aleph.AlephAPI.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.AlephAPI.AlephException;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * This is a factory class that is used to create a request forwarded directly to Aleph API.
  * 
  * @authors Rick Johnson (NDU), Jiří Kozlovský (MZK)
  */
 public class AlephAPIFactory implements Serializable {
 	
 	private static final long serialVersionUID = 65009L;
 	
 	/**
 	 * createBorAuthAlephAPI
 	 * 
	 * Create an X-Service that will make a bor-auth X-Service call
 	 * 
 	 * @param library
 	 * @param sub_library
 	 * @param patron_id
 	 * @param password
 	 * @return AlephAPI object initialized with values passed in
 	 */
 	
 	//TODO: Sensibly rename this method after AlephConstants refactored 
 	public static AlephAPI createBorAuthAlephAPI(String library, String sub_library, 
 			String patron_id, String password){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_BOR_AUTH);
 		if (library!=null) AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		if (patron_id!=null) AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID,patron_id);
 		if (password!=null) AlephAPI.addParameter(AlephConstants.PARAM_PASSWORD,password);
 		if (sub_library!=null) AlephAPI.addParameter(AlephConstants.PARAM_SUB_LIBRARY,sub_library);
 		
 		return AlephAPI;
 	}
 	
 	/**
	 * Create an AlephAPI object that will query the X-Server for bor-info (patron
 	 * info). The library and patron_id values are mandatory.  If password is null,
 	 * it will be ignored.  If getCashData is false, all other cash parameters will
 	 * be ignored.  If cashFilter is not null, cashBalanceOnly and openFinesOnly will
 	 * be ignored.  If getHolds is false, partialHoldData will be ignored. 
 	 * If getLoans is false, partialLoanData will be ignored.
 	 * If getCashData, getHolds, and getLoans are true the default value
 	 * is to get all data for a patron.
 	 * 
 	 * @param library  			Library for a user
 	 * @param patron_id 		patron id within the library
 	 * @param password 			password used for verification
 	 * 
 	 * @param cashDataType		set type of cash data returned, possible values are:
 	 * 							- Y (AlephAPI.CASH_ALL_DATA) default if cashDataType is null or unrecognized, return full cash data
 	 * 							- N (AlephAPI.CASH_NO_DATA) do not return any cash data
 	 * 							- B (AlephAPI.CASH_BALANCE_ONLY) return cash balances only
 	 * 							- O	(AlephAPI.CASH_OPEN_TRANSACTIONS_ONLY) return open cash transactions only
 	 * 							- F (AlephAPI.CASH_FILTER) use a cash filter string, cashDataType will revert to Y if cashFilter is null
 	 * @param cashFilter		Set a cash filter used for cash data retrieval
 	 * @param holdDataType		possible values are:
 	 * 							- Y (AlephAPI.HOLDS_ALL_DATA) default if holdDataType is null or unrecognized, return full hold data
 	 * 							- N (AlephAPI.HOLDS_NO_DATA) do not return any hold data
 	 * 							- P (AlephAPI.HOLDS_PARTIAL_DATA) return partial hold data
 	 * @param loanDataType		possible values are:
 	 * 							- Y (AlephAPI.LOANS_ALL_DATA) default if loanDataType is null or unrecognized, return full loan data
 	 * 							- N (AlephAPI.LOANS_NO_DATA) do not return any loan data
 	 * 							- P (AlephAPI.LOANS_PARTIAL_DATA) return partial loan data
 	 * 
 	 * @return AlephAPI object configured with the supplied values
 	 */
 	
 	//TODO: Sensibly rename this method after AlephConstants refactored 
 	public static AlephAPI createBorInfoAlephAPI(String library, String patron_id, String password,
 			String cashDataType, String cashFilter, String holdDataType, String loanDataType){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_BOR_INFO);
 		if (library!=null) AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		if (patron_id!=null) AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID,patron_id);
 		if (password!=null) AlephAPI.addParameter(AlephConstants.PARAM_PASSWORD,password);
 		
 		String cash = AlephConstants.USER_CASH_ALL_DATA;
 		if (cashDataType!=null){
 			if (cashDataType.equalsIgnoreCase(AlephConstants.USER_CASH_NO_DATA)){
 				cash = AlephConstants.USER_CASH_NO_DATA;
 			} else if (cashDataType.equalsIgnoreCase(AlephConstants.USER_CASH_FILTER)&&cashFilter!=null){
 				cash = AlephConstants.USER_CASH_FILTER;
 			} else if (cashDataType.equalsIgnoreCase(AlephConstants.USER_CASH_BALANCE_ONLY)){
 				cash = AlephConstants.USER_CASH_BALANCE_ONLY;
 			} else if (cashDataType.equalsIgnoreCase(AlephConstants.USER_CASH_OPEN_TRANSACTIONS_ONLY)){
 				cash = AlephConstants.USER_CASH_OPEN_TRANSACTIONS_ONLY;
 			}
 		}
 		AlephAPI.addParameter(AlephConstants.PARAM_CASH, cash);
 		if (cash.equals(AlephConstants.USER_CASH_FILTER)){
 			AlephAPI.addParameter(AlephConstants.PARAM_FILTER_CASH, cashFilter);
 		}
 		
 		String holds = AlephConstants.USER_HOLDS_ALL_DATA;
 		if (holdDataType!=null){
 			if (holdDataType.equalsIgnoreCase(AlephConstants.USER_HOLDS_NO_DATA)){
 				holds = AlephConstants.USER_HOLDS_NO_DATA;
 			} else if (holdDataType.equalsIgnoreCase(AlephConstants.USER_HOLDS_PARTIAL_DATA)){
 				holds = AlephConstants.USER_HOLDS_PARTIAL_DATA;
 			}
 		}
 		AlephAPI.addParameter(AlephConstants.PARAM_HOLD, holds);
 		
 		String loans = AlephConstants.USER_LOANS_ALL_DATA;
 		if (loanDataType!=null){
 			if (loanDataType.equalsIgnoreCase(AlephConstants.USER_LOANS_NO_DATA)){
 				loans = AlephConstants.USER_LOANS_NO_DATA;
 			} else if (loanDataType.equalsIgnoreCase(AlephConstants.USER_LOANS_PARTIAL_DATA)){
 				loans = AlephConstants.USER_LOANS_PARTIAL_DATA;
 			}
 		}
 		AlephAPI.addParameter(AlephConstants.PARAM_LOANS, loans);
 		
 		return AlephAPI;
 	}
 	
 	/**
 	 * 
 	 * createCircStatusAlephAPI
 	 * 
	 * Create an X-Service that will make a circ-status X-Service call
 	 * 
 	 * @param library
 	 * @param system_number
 	 * @return AlephAPI object initialized with values passed in
 	 */
 	public static AlephAPI createCircStatusAlephAPI(String library, String system_number){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_CIRC_STATUS);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		AlephAPI.addParameter(AlephConstants.PARAM_SYSTEM_NUMBER,system_number);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create Item Data AlephAPI
 	 * 
 	 * @param base
 	 * @param document_number
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createItemDataAlephAPI(String base, String document_number){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_ITEM_DATA);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER,document_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_BASE, base);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create Read Item AlephAPI based on adm library and barcode
 	 * @param library
 	 * @param barcode
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createReadItemAlephAPI(String library, String barcode){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_READ_ITEM);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		AlephAPI.addParameter(AlephConstants.PARAM_BARCODE, barcode);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create Read Item AlephAPI based on adm library, doc number, and item sequence
 	 * 
 	 * @param library
 	 * @param document_number
 	 * @param item_sequence
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createReadItemAlephAPI(String library, String document_number, String item_sequence){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_READ_ITEM);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER, document_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_SEQ_NUMBER, item_sequence);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create Find Document AlephAPI
 	 * 
 	 * @param base
 	 * @param document_number
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createFindDocAlephAPI(String base, String document_number){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_FIND_DOC);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER,document_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_BASE, base);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create Get Holding AlephAPI
 	 * 
 	 * @param base
 	 * @param document_number
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createGetHoldingAlephAPI(String base, String document_number){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_GET_HOLDING);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER,document_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_BASE, base);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI that can initiate a hold request on an item
 	 * 
 	 * @param library
 	 * @param doc_number  This is equivalent to the adm id
 	 * @param seq_number  This is the sequence number
 	 * @param bor_id   The borrower id of the person requesting the hold
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createHoldRequestAlephAPI(String library, String doc_number, String seq_number, String bor_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_HOLD_REQUEST);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY,library);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER, doc_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_SEQ_NUMBER, seq_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID, bor_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI that can initiate a hold request on an item
 	 * 
 	 * @param library
 	 * @param barcode  This does not need to be set if rec_key is set
 	 * @param bor_id   The borrower id of the person requesting the hold
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createHoldRequestAlephAPI(String library, String barcode, String bor_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_HOLD_REQUEST);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY,library);
 		AlephAPI.addParameter(AlephConstants.PARAM_BARCODE, barcode);
 		AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID, bor_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI object that can cancel a hold request
 	 * 
 	 * @param library
 	 * @param item_id  This equates to adm id plus seq number
 	 * @param hold_id
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createCancelHoldRequestAlephAPI(String library, String item_id, String hold_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_CANCEL_HOLD_REQUEST);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		AlephAPI.addParameter(AlephConstants.PARAM_REC_KEY,item_id+hold_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI object that can cancel a hold request
 	 * 
 	 * @param library
 	 * @param doc_number
 	 * @param seq_number
 	 * @param hold_id
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createCancelHoldRequestAlephAPI(String library, String doc_number, String seq_number, String hold_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_CANCEL_HOLD_REQUEST);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, library);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER,doc_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_SEQ_NUMBER,seq_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_CANCEL_SEQUENCE,hold_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI that will renew an item
 	 * 
 	 * @param library
 	 * @param doc_number  This is equivalent to the adm id
 	 * @param seq_number  This is the sequence number
 	 * @param bor_id   The borrower id of the person requesting the renewal
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createRenewAlephAPI(String library, String doc_number, String seq_number, String bor_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_RENEW);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY,library);
 		AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUMBER, doc_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_SEQ_NUMBER, seq_number);
 		AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID, bor_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create an AlephAPI that will renew an item
 	 * 
 	 * @param library
 	 * @param barcode  This does not need to be set if rec_key is set
 	 * @param bor_id   The borrower id of the person renewing the item
 	 * @return AlephAPI object
 	 */
 	public static AlephAPI createRenewAlephAPI(String library, String barcode, String bor_id){
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_RENEW);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY,library);
 		AlephAPI.addParameter(AlephConstants.PARAM_BARCODE, barcode);
 		AlephAPI.addParameter(AlephConstants.PARAM_PATRON_ID, bor_id);
 		return AlephAPI;
 	}
 	
 	/**
 	 * Create AlephAPI that takes a list of bibids and returns availability. If bibIds is 
 	 * greater than 10 it will throw an exception.
 	 * 
 	 * @param bib_library
 	 * @param bibIds
 	 * @return
 	 * @throws AlephException 
 	 */
 	public static AlephAPI createPublishAvailabilityAlephAPI(String bib_library, List<String> bibIds) throws AlephException{
 		AlephAPI AlephAPI = new AlephAPI(AlephConstants.ALEPHAPI_PUBLISH_AVAILABILITY);
 		AlephAPI.addParameter(AlephConstants.PARAM_LIBRARY, bib_library);
 		//create string separated by commas
 		String ids = "";
 		boolean first = true;
 		if (bibIds!=null){
 			if (bibIds.size()>10){
 				throw new AlephException(AlephConstants.ERROR_AVAILABILITY_LIST_TOO_LONG);
 			}
 			for (String bibId : bibIds){
 				if (bibId!=null){
 					if (!first){
 						ids += ",";
 					}
 					ids += bibId;
 					first = false;
 				}
 			}
 			AlephAPI.addParameter(AlephConstants.PARAM_DOC_NUM, ids);
 		}
 		return AlephAPI;
 	}
 }
