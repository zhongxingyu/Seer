 /* 
  * Simple Mollie.nl iDeal Payment Class created for Play! Framework.
  * Shouldnt be hard use it for other Java applications, just load the 
  * used Libs imported for this class..
  * 
  * @author: Erik Kramer (@erikkramer - 03-2012)
  * 
  */
 package paymentproviders;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigDecimal;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import play.Logger;
 import play.libs.F.None;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.libs.XPath;
 
 public class IDeal {
 	
 	/* Minimal amount to be used */
 	private static Integer MIN_TRANS_AMOUNT = 118;
 
 	/* Your partner id and key from mollie.nl */
	private static Integer PARTNER_ID = 768917;
	private static String PROFILE_KEY = "11d8b117";
 	
 	/* Constant host/port/directory variables */
 	private static String API_HOST = "https://secure.mollie.nl";
 	private static String API_DIR = "/xml/ideal";
 	
 	/* Put this on false when you're in production, or set it in the controlling class */
 	private Boolean testMode = true;
 	
 	/* Primitive Integer because the amount is in cents, and can not be - */
 	private Integer amount;
 	private String bankId;
 	private String description;
 	
 	private String returnUrl;
 	private String reportUrl;
 
 	private String bankUrl;
 	private String paymentUrl;
 
 	private String transactionId;
 	private Boolean paidStatus = false;
 	private IDealConsumer idealConsumer;
 
 	private String errorMessage;
 	private String errorCode = "0";
 
 	public IDeal() {
 		Logger.info("Initialized iDeal Payment without BankId for partner id: " + this.PROFILE_KEY + ".");
 	}
 
 	
 	/*
 	 * Create payment for specific bank and create a payment URL
 	*/
 	public Boolean createPayment(Integer amount, String description, String returnUrl, String reportUrl){
 		
 		//TODO: Need proper init of properties and error checking in setters..
 		this.amount = amount;
 		this.description = description;
 		this.returnUrl = returnUrl;
 		this.reportUrl = reportUrl;
 		
 		if ((this.bankId.isEmpty()) || (this.amount < MIN_TRANS_AMOUNT)
 				|| (this.description.isEmpty()) || (this.reportUrl.isEmpty())
 				|| (this.returnUrl.isEmpty())) {
 
 			this.errorMessage = "Onjuiste betalings informatie ingevoerd.";
 			return false;
 		}
 				
 		// Map with the query options
 		Map<String,String> parameters = new HashMap<String,String>();
 		parameters.put("a", "fetch");
 		parameters.put("partnerid", String.valueOf(this.PARTNER_ID));
 		parameters.put("bank_id", this.bankId);
 		parameters.put("amount", String.valueOf(this.amount));
 		parameters.put("description", this.description);
 		parameters.put("reporturl", this.reportUrl);
 		parameters.put("returnurl", this.returnUrl);
 		
 		if(this.testMode){
 			parameters.put("testmode", String.valueOf(this.testMode));
 		}
 		if(!this.PROFILE_KEY.isEmpty()){
 			parameters.put("profile_key", String.valueOf(this.PROFILE_KEY));
 		}
 		
 		// call the create payment service and parse the response to a transaction id and bank url.
 		HttpResponse response = WS.url(API_HOST + API_DIR + createQueryParameters(parameters)).get();
 		Logger.info("response xml:" + response.getString());
 		if(response.getStatus() != 200 && response.getContentType().contains("xml")){
 			this.errorMessage = "Geen correct xml callback ontvangen";
 			return false;
 		}
 		
 		Logger.info("IDeal PaymentCreated Response: " + response.getString());
 		Document xmlResponse = response.getXml();
 	    this.transactionId = XPath.selectText("response//order//transaction_id", xmlResponse);
 	    this.bankUrl = XPath.selectText("response//order//URL", xmlResponse);
 	    
 		return true;
 	}
 	
 	/*
 	 * Check if a payment of a transaction is done..
 	 */
 	public Boolean checkPayment(String transactionId){
 		this.transactionId = transactionId;
 		
 		if(this.transactionId == null){
 			this.errorCode = "-10";
 			return false;
 		}
 		
 		// Map with the query options
 		Map<String,String> parameters = new HashMap<String,String>();
 		parameters.put("a", "check");
 		parameters.put("partnerid", String.valueOf(this.PARTNER_ID));
 		parameters.put("transaction_id", String.valueOf(this.transactionId));
 		
 		if(this.testMode){
 			parameters.put("testmode", String.valueOf(this.testMode));
 		}
 		
 		// call the create payment service and parse the response to a transaction id and bank url.
 		HttpResponse response = WS.url(API_HOST + API_DIR + createQueryParameters(parameters)).get();
 		if(response.getStatus() != 200 && response.getContentType().contains("xml")){
 			this.errorMessage = "Geen correct xml callback ontvangen";
 			return false;
 		}
 		
 		Logger.info("IDeal PaymentCheck XML Response: " + response.getString());
 		Document xmlResponse = response.getXml();
 	    
 		this.paidStatus = Boolean.valueOf(XPath.selectText(
 				"response//order//payed", xmlResponse));
 		this.idealConsumer = new IDealConsumer(XPath.selectText(
 				"response//order//consumer//consumerName", xmlResponse),
 				XPath.selectText("response//order//consumer//consumerAccount",
 						xmlResponse), XPath.selectText(
 						"response//order//consumer//consumerCity", xmlResponse));
 	
 		return true;
 	}
 	
 	/*
 	 * Get the current active banks from Mollie API
 	*/
 	public List<IDealBank> getBanks(){
 		
 		// Map with the query options
 		Map<String,String> parameters = new HashMap<String,String>();
 		parameters.put("a", "banklist");
 		parameters.put("partner_id", String.valueOf(PARTNER_ID));
 		if(this.testMode){
 			parameters.put("testmode", String.valueOf(this.testMode));
 		}
 		
 		// call the api, parse xml and add banks to a list for usage in the controller
 		Document response = WS.url(API_HOST + API_DIR + createQueryParameters(parameters)).get().getXml();
 		List<IDealBank> banks = new ArrayList<IDealBank>();
 		for(Node bank: XPath.selectNodes("response//bank", response)) {
 			banks.add(new IDealBank(XPath.selectText("bank_id", bank),XPath.selectText("bank_name", bank)));
 		}
 		
 		return banks;
 	}	
 
 	public String getBankUrl(){
 		return this.bankUrl;
 	}
 	
 	public void setBankUrl(String bankUrl){
 		this.bankUrl = bankUrl;
 	}
 
 
 	public Boolean getTestMode() {
 		return testMode;
 	}
 
 
 	public void setTestMode(Boolean testmode) {
 		this.testMode = testmode;
 	}
 
 
 	public String getBankId() {
 		return bankId;
 	}
 
 
 	public void setBankId(String bankId) {
 		this.bankId = bankId;
 	}
 
 
 	public Integer getAmount() {
 		return amount;
 	}
 
 
 	public void setAmount(Integer amount) {
 		this.amount = amount;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 
 	public String getReturnUrl() {
 		return returnUrl;
 	}
 
 
 	public void setReturnUrl(String returnUrl) {
 		this.returnUrl = returnUrl;
 	}
 
 
 	public String getReportUrl() {
 		return reportUrl;
 	}
 
 
 	public void setReportUrl(String reportUrl) {
 		this.reportUrl = reportUrl;
 	}
 
 
 	public String getPaymentUrl() {
 		return paymentUrl;
 	}
 
 
 	public void setPaymentUrl(String paymentUrl) {
 		this.paymentUrl = paymentUrl;
 	}
 
 
 	public String getTransactionId() {
 		return transactionId;
 	}
 
 
 	public void setTransactionId(String transactionId) {
 		this.transactionId = transactionId;
 	}
 
 
 	public Boolean getPaidStatus() {
 		return paidStatus;
 	}
 
 
 	public void setPaidStatus(Boolean paidStatus) {
 		this.paidStatus = paidStatus;
 	}
 
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 
 	public void setErrorMessage(String errorMessage) {
 		this.errorMessage = errorMessage;
 	}
 
 
 	public String getErrorCode() {
 		return errorCode;
 	}
 
 
 	public void setErrorCode(String errorCode) {
 		this.errorCode = errorCode;
 	}
 
 
 	public IDealConsumer getIdealConsumer() {
 		return idealConsumer;
 	}
 
 
 	public void setIdealConsumer(IDealConsumer idealConsumer) {
 		this.idealConsumer = idealConsumer;
 	}
 	
 	
 	/*
 	 * Util method: create query parameter String for the url..
 	 */
 	private static String createQueryParameters(Map<String, String> parameters) {
 		StringBuilder stringBuilder = new StringBuilder();
 		
 		if(!parameters.isEmpty()) stringBuilder.append("?");
 		for (String key : parameters.keySet()) {
 			if (stringBuilder.length() > 1) {
 				stringBuilder.append("&");
 			}
 			String value = parameters.get(key);
 			try {
 				stringBuilder.append((key != null ? URLEncoder.encode(key,
 						"UTF-8") : ""));
 				stringBuilder.append("=");
 				stringBuilder.append(value != null ? URLEncoder.encode(value,
 						"UTF-8") : "");
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(
 						"This method requires UTF-8 encoding support", e);
 			}
 		}
 
 		return stringBuilder.toString();
 		
 	}
 
 
 	@Override
 	public String toString() {
 		return "IDeal [testMode=" + testMode + ", amount=" + amount
 				+ ", bankId=" + bankId + ", description=" + description
 				+ ", returnUrl=" + returnUrl + ", reportUrl=" + reportUrl
 				+ ", bankUrl=" + bankUrl + ", paymentUrl=" + paymentUrl
 				+ ", transactionId=" + transactionId + ", paidStatus="
 				+ paidStatus + ", idealConsumer=" + idealConsumer
 				+ ", errorMessage=" + errorMessage + ", errorCode=" + errorCode
 				+ "]";
 	}
 
 }
