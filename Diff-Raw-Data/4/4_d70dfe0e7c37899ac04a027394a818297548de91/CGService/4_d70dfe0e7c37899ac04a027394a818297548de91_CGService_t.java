 package com.rusticisoftware.cheddargetter.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import sun.misc.BASE64Encoder;
 
 public class CGService {
 	private static Logger log = Logger.getLogger(CGService.class.toString());
 	
 	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 	static {
 		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 	
 	private static String CG_SERVICE_ROOT = "https://cheddargetter.com/xml";
 	
 	private String userName;
 	private String password;
 	private String productCode;
 
 	public String getUserName(){
 		return userName;
 	}
 	public void setUserName(String userName){
 		this.userName = userName;
 	}
 	public String getPassword(){
 		return password;
 	}
 	public void setPassword(String password){
 		this.password = password;
 	}
 	public String getProductCode(){
 		return productCode;
 	}
 	public void setProductCode(String productCode){
 		this.productCode = productCode;
 	}
 	
 	public CGService(String userName, String password, String productCode){
 		setUserName(userName);
 		setPassword(password);
 		setProductCode(productCode);
 	}
 	
 	public CGCustomer getCustomer(String custCode) throws Exception {
 		Document doc = makeServiceCall("/customers/get/productCode/" + getProductCode() + "/code/" + custCode, null);
 		Element root = doc.getDocumentElement();
 		Element customer = XmlUtils.getFirstChildByTagName(root, "customer");
 		return new CGCustomer(customer);
 	}
 	
 	public boolean customerExists(String custCode) {
 		boolean exists = false;
 		try {
 			CGCustomer cust = getCustomer(custCode);
 			if(cust != null){
 				exists = true;
 			}
 		}
 		catch (Exception e) {}
 		return exists;
 	}
 	
 	public Document getAllCustomers() throws Exception {
 		return makeServiceCall("/customers/get/productCode/" + getProductCode(), null);
 	}
 	
 	public Document createNewCustomer(String custCode, String firstName, String lastName, 
 			String email, String company, String subscriptionPlanCode, String ccFirstName,
 			String ccLastName, String ccNumber, String ccExpireMonth, String ccExpireYear, 
 			String ccZip) throws Exception {
 		
 		HashMap<String, String> paramMap = new HashMap<String, String>();
 		paramMap.put("code", custCode);
 		paramMap.put("firstName", firstName);
 		paramMap.put("lastName", lastName);
 		paramMap.put("email", email);
 		if(company != null){
 			paramMap.put("company", company);
 		}
 		
 		paramMap.put("subscription[planCode]", subscriptionPlanCode);
 		
 		//If plan is free, no cc information needed, so we just check
 		//ccNumber field and assume the rest are there or not
 		if(ccNumber != null){
 			paramMap.put("subscription[ccFirstName]", ccFirstName);
 			paramMap.put("subscription[ccLastName]", ccLastName);
 			paramMap.put("subscription[ccNumber]", ccNumber);
 			paramMap.put("subscription[ccExpiration]", ccExpireMonth + "/" + ccExpireYear);
 			paramMap.put("subscription[ccZip]", ccZip);
 		}
 		
 		return makeServiceCall("/customers/new/productCode/" + getProductCode(), paramMap);
 	}
 	
 	public Document updateSubscription(String customerCode, String planCode, String ccFirstName, String ccLastName,
 			String ccNumber, String ccExpireMonth, String ccExpireYear, String ccZip) throws Exception {
 		
 		HashMap<String, String> paramMap = new HashMap<String, String>();
 		paramMap.put("planCode", planCode);
 		
 		//If plan is free, no cc information needed, so we just check
 		//ccNumber field and assume the rest are there or not
 		if(ccNumber != null){
 			paramMap.put("ccFirstName", ccFirstName);
 			paramMap.put("ccLastName", ccLastName);
 			paramMap.put("ccNumber", ccNumber);
 			paramMap.put("ccExpiration", ccExpireMonth + "/" + ccExpireYear);
 			paramMap.put("ccZip", ccZip);
 		}
 		
 		String relativeUrl = "/customers/edit-subscription/productCode/" + getProductCode() + "/code/" + customerCode;
 		return makeServiceCall(relativeUrl, paramMap);
 	}
 	
 	public Document addItemQuantity(String customerCode, String itemCode) throws Exception {
 	    return addItemQuantity(customerCode, itemCode, 1);
 	}
 	
 	public Document addItemQuantity(String customerCode, String itemCode, int quantity) throws Exception {
 	    HashMap<String, String> paramMap = new HashMap<String, String>();
	    paramMap.put("quantity", String.valueOf(quantity));
 	    
 	    String relativeUrl = "/customers/add-item-quantity/productCode/" + getProductCode() + 
 	                         "/code/" + customerCode + "/itemCode/" + itemCode;
 	    return makeServiceCall(relativeUrl, paramMap);
 	    
 	}
 	
 	public CreditCardData getLatestCreditCardData(String customerCode) throws Exception {
 		CGCustomer cgCustomer;
 		try { cgCustomer = getCustomer(customerCode); }
 		catch (Exception e) { return null; }
 		
 		List<CGSubscription> subs = cgCustomer.getSubscriptions();
 		if(subs == null || subs.size() == 0){
 			return null;
 		}
 		
 		CGSubscription sub = subs.get(0);
 		if(sub.getCcExpirationDate() == null){
 			return null;
 		}
 		
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 		cal.setTime(sub.getCcExpirationDate());
 		return new CreditCardData(sub.getCcType(), sub.getCcLastFour(), 
 				cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
 	}
 	
 	protected Document makeServiceCall(String path, Map<String,String> paramMap) throws Exception {
 		String fullPath = CG_SERVICE_ROOT + path;
 		String encodedParams = encodeParamMap(paramMap);
 		String response = postTo(fullPath, getUserName(), getPassword(), encodedParams);
 		Document responseDoc = XmlUtils.parseXmlString(response);
 		try {
 			checkResponseForError(responseDoc);
 		} catch (CGException cge) {
 			log.log(Level.WARNING, "Error calling service at " + path, cge);
 			throw cge;
 		}
 		return responseDoc;
 	}
 	
 	protected String postTo(String urlStr, String userName, String password, String data) throws Exception {
 
 		log.fine("Sending this data to this url: " + urlStr + " data = " + data);
 		
 		//Create a new request to send this data...
 		URL url = new URL(urlStr);
 		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 		
 		//Put authentication fields in http header, and make the data the body
 		BASE64Encoder enc = new BASE64Encoder();
 		//connection.setRequestProperty("Content-Type", "text/xml");
 		String auth = userName + ":" + password;
 		connection.setRequestProperty("Authorization", "Basic " + enc.encode(auth.getBytes()));
 		
 		
 		connection.setRequestMethod("POST");
 		connection.setDoOutput(true);
 		connection.setDoInput(true);
 		connection.setUseCaches(false);
 		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
 		PrintWriter output = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
 		output.write(data);
 		output.flush();
 		output.close();
 
 		//Get response
 		BufferedReader rd;
 		try {
 			rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 		} catch (IOException ioe) {
 			rd = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
 		}
 		
 		StringBuilder response = new StringBuilder();
 		String responseLine = null;
 		while((responseLine = rd.readLine()) != null){
 			response.append(responseLine);
 		}
 		
 		log.fine("Got this back from CG: " + response.toString());
 		
 		return response.toString();
 	}
 	
 	protected String encodeParamMap(Map<String, String> paramMap) throws Exception {
 		if(paramMap == null || paramMap.keySet().size() == 0){
 			return "";
 		}
 		StringBuilder encoded = new StringBuilder();
 		for (String name : paramMap.keySet()){
 			encoded.append(getEncodedParam(name, paramMap.get(name)) + "&");
 		}
 		//Cutoff last ampersand
 		encoded.delete(encoded.length() - 1, encoded.length());
 		
 		return encoded.toString();
 	}
 	
 	protected String getEncodedParam(String paramName, String paramVal) throws Exception {
 		return URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(paramVal, "UTF-8");
 	}
 	
 	protected boolean checkResponseForError(Document doc) throws CGException {
 		Element root = doc.getDocumentElement();
 		if(root.getNodeName().equals("error")){
 			String code = root.getAttribute("code");
 			String message = root.getTextContent();
 			throw new CGException(Integer.parseInt(code), message);
 		}
 		return true;
 	}
 	
 	public static Date parseCgDate(String cgDate) {
 		if(cgDate == null || cgDate.length() == 0){
 			return null;
 		}
 		
 		try{
 			return sdf.parse(fixDateFormat(cgDate));
 		}
 		catch (Exception e){
 			log.log(Level.WARNING, "Exception parsing date " + cgDate, e);
 			return null;
 		}
     }
 	
 	public static String fixDateFormat(String cgDate){
     	//CG's dates have annoying ':' symbol in middle of timezone part
     	//So here we take it out
     	int tzIndex = cgDate.lastIndexOf("+");
     	String tz = cgDate.substring(tzIndex, cgDate.length());
     	String modifiedTz = tz.replace(":", "");
     	return cgDate.substring(0, tzIndex) + modifiedTz;
 	}
 }
