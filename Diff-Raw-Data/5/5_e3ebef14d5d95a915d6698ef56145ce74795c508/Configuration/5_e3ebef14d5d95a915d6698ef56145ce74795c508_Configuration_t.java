 package com.malcom.library.android.module.config;
 
 import org.json.JSONObject;
 
 import com.malcom.library.android.exceptions.ConfigurationException;
 
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * This class parses the configuration module required config.json file data.
  * 
  * 
  * Example of the format for config.json:
  * 
  * { 
  *   "splashImageName":{"value":"gato-negro.jpg","type":"STRING"},
  *   "splashImageUrl":{"value":"http://assets.local.mymalcom.com/7417c323-5064-43cd-8fe9-818d7e13d3ee/3/splash","type":"STRING"},
  *   "animationDelay":{"value":"5","type":"INTEGER"},
  *   
  *   "showInterstitial":{"value":"true","type":"BOOLEAN"},
  *   "interstitialWeb":{"value":"http://javocsoft.blogspot.com","type":"STRING"},
  *   "interstitialTimesToShow":{"value":"2","type":"INTEGER"},
  *   "interstitialVersion":{"value":"1.0","type":"STRING"},
  *   "interstitialVersionCondition":{"value":"GREATER_EQUAL","type":"STRING"},
  *         
  *   "alertType":{"value":"INFO","type":"STRING"},
  *   "alertMsg_es":{"value":"Test de alerta.","type":"STRING"},
  *   "alertMsg_en":{"value":"Alert Test","type":"STRING"},
  *   "defaultLanguage":{"value":"es","type":"STRING"},
  *   "versionCondition":{"value":"GREATER_EQUAL","type":"STRING"},
  *   "appStoreVersion":{"value":"1.0","type":"STRING"},
  *    
  *   "TestPropertyCustom":{"value":"testing custom property","type":"STRING"}
  *  }
  * 
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012 
  */
 public class Configuration {
 
 	public static final String CONFIG_PROPERTY_SPLASH_IMAGE_NAME = "splashImageName";
     public static final String CONFIG_PROPERTY_SPLASH_IMAGE_URL = "splashImageUrl";
     public static final String CONFIG_PROPERTY_SPLASH_AMIMATION_DELAY = "animationDelay";
     
     public static final String CONFIG_PROPERTY_INTERSTITIAL_SHOW = "showInterstitial";
     public static final String CONFIG_PROPERTY_INTERSTITIAL_WEB = "interstitialWeb";
     public static final String CONFIG_PROPERTY_INTERSTITIAL_TIMES_TO_SHOW = "interstitialTimesToShow";
     public static final String CONFIG_PROPERTY_INTERSTITIAL_VERSION = "interstitialVersion";
     public static final String CONFIG_PROPERTY_INTERSTITIAL_VERSION_CONDITION = "interstitialVersionCondition";
         
     public static final String CONFIG_PROPERTY_ALERT_TYPE = "alertType";
     public static final String CONFIG_PROPERTY_ALERT_DEFAULT_LANGUAGE = "defaultLanguage";    
     public static final String CONFIG_PROPERTY_ALERT_MESSAGE_PREFIX = "alertMsg_";
     public static final String CONFIG_PROPERTY_ALERT_VERSION_CONDITION = "versionCondition";
     public static final String CONFIG_PROPERTY_ALERT_VERSION = "appStoreVersion";
    public static final String CONFIG_PROPERTY_ALERT_URL_APPSTORE = "urlAppStore";
     

     private JSONObject jsonObject;
     private String deviceLanguage;
 	
 	private String splashImageName;
 	private String splashImageUrl;
     private Integer splashAnimationDelay ;
     private boolean isSplash;
     
     private Boolean interstitialShow;
     private String interstitialWeb;
     private Integer interstitialTimesToShow;
     private String interstitialVersion;
     private String interstitialVersionCondition;
     private boolean isInterstitial;
         
     private String alertType;
     private String alertMsg;
     private String alertDefaultLanguage;
     private String alertVersionCondition;
     private String alertAppStoreVersion;
     private String alertUrlAppStore;
     private boolean isAlert;
 	
     private String configDataRaw;
     
     
     public Configuration(JSONObject jsonObject, String deviceLanguage) throws ConfigurationException{
     	if(jsonObject==null)
     		throw new ConfigurationException("No configuration data!",ConfigurationException.CONFIGURATION_EXCEPTION_NO_CONFIG_DATA);
     	
     	this.jsonObject = jsonObject;
     	this.deviceLanguage = deviceLanguage;
     	this.configDataRaw = jsonObject.toString();
     	
     	init();
     }
     
     private void init(){    
     	prepareSplash();
     	prepareInterstitial();
     	prepareAlert();
     }
     
     
     private void prepareSplash(){
     	
     	this.splashImageName = getPropertyValue(CONFIG_PROPERTY_SPLASH_IMAGE_NAME);
     	this.splashImageUrl = getPropertyValue(CONFIG_PROPERTY_SPLASH_IMAGE_URL);
     	this.splashAnimationDelay = getPropertyValue(CONFIG_PROPERTY_SPLASH_AMIMATION_DELAY)!=null?Integer.valueOf(getPropertyValue(CONFIG_PROPERTY_SPLASH_AMIMATION_DELAY)):1;
     	Log.d("Configuration", "Splash: "+this.splashImageName + " - "+this.splashImageUrl + " - "+this.splashAnimationDelay);
     	if(TextUtils.isEmpty(splashImageName) || 
     	   TextUtils.isEmpty(splashImageUrl) || 
     	   splashAnimationDelay==null){
     		isSplash = false;
     	}else{
     		isSplash = true;
     	}	
     }
     
     private void prepareInterstitial(){
     	
     	this.interstitialShow = getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_SHOW)!=null?Boolean.valueOf(getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_SHOW)):null;
     	this.interstitialWeb = getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_WEB);
     	this.interstitialTimesToShow = getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_TIMES_TO_SHOW)!=null?Integer.valueOf(getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_TIMES_TO_SHOW)):null;
     	this.interstitialVersion = getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_VERSION);
     	this.interstitialVersionCondition = getPropertyValue(CONFIG_PROPERTY_INTERSTITIAL_VERSION_CONDITION);
     	if(interstitialShow==null || (interstitialShow!=null && !interstitialShow) || 
     	  (interstitialShow!=null && interstitialShow && TextUtils.isEmpty(interstitialWeb)) ||    	   
     	   (!TextUtils.isEmpty(interstitialVersion) && TextUtils.isEmpty(interstitialVersionCondition)) ||
  	       (!TextUtils.isEmpty(interstitialVersionCondition) && (!interstitialVersionCondition.equals("NONE") && TextUtils.isEmpty(interstitialVersion)))
     	){
     		isInterstitial = false;
     	}else{
     		isInterstitial = true;
     	}
     }
 
     private void prepareAlert(){
     	    		
 		this.alertType = getPropertyValue(CONFIG_PROPERTY_ALERT_TYPE);		
     	this.alertDefaultLanguage = getPropertyValue(CONFIG_PROPERTY_ALERT_DEFAULT_LANGUAGE);
     	this.alertVersionCondition = getPropertyValue(CONFIG_PROPERTY_ALERT_VERSION_CONDITION);
     	this.alertAppStoreVersion = getPropertyValue(CONFIG_PROPERTY_ALERT_VERSION);
     	this.alertUrlAppStore = getPropertyValue(CONFIG_PROPERTY_ALERT_URL_APPSTORE);
     	String alertMessage = getPropertyValue(CONFIG_PROPERTY_ALERT_MESSAGE_PREFIX+deviceLanguage);
     	if(!TextUtils.isEmpty(alertMessage)){    		
     		this.alertMsg = alertMessage;
     	}else{
     		if(this.alertDefaultLanguage!=null){
     			String defaultAlertMessage = getPropertyValue(CONFIG_PROPERTY_ALERT_MESSAGE_PREFIX+this.alertDefaultLanguage);
     			
     			if(!TextUtils.isEmpty(defaultAlertMessage)){
     				this.alertMsg = defaultAlertMessage;
     			}
     		}
     	}   	
     	
     	if( TextUtils.isEmpty(this.alertType) || TextUtils.isEmpty(this.alertDefaultLanguage) ||	    			
     	   (!TextUtils.isEmpty(this.alertVersionCondition)  && !this.alertVersionCondition.equals("NONE") && TextUtils.isEmpty(this.alertAppStoreVersion)) ||
  	       ( !TextUtils.isEmpty(this.alertAppStoreVersion) && (  
  	    		  TextUtils.isEmpty(this.alertVersionCondition) || 
  	  	    	 (!TextUtils.isEmpty(this.alertVersionCondition) && this.alertVersionCondition.equals("NONE"))  )
  	       ) ||	
     	   
  	       (!TextUtils.isEmpty(this.alertType) && this.alertType.equals("NONE")) || 
     	   (!TextUtils.isEmpty(this.alertType) && !this.alertType.equals("NONE") && TextUtils.isEmpty(this.alertMsg)) ||
     	
     	   (!TextUtils.isEmpty(this.alertType) && this.alertType.equals("FORCE") && (TextUtils.isEmpty(this.alertMsg) || TextUtils.isEmpty(this.alertUrlAppStore)) )
     	   ){
     		isAlert = false;
     	}else{
     		isAlert = true;
     	}    	
     }
 
 	
     
     //GETTERS
     
     public String getSplashImageUrl() {
 		return splashImageUrl;
 	}
 
 	public Integer getSplashAnimationDelay() {
 		return splashAnimationDelay;
 	}
 
 	public boolean isSplash() {
 		return isSplash;
 	}
 
 	public String getInterstitialWeb() {
 		return interstitialWeb;
 	}
 
 	public Integer getInterstitialTimesToShow() {
 		return interstitialTimesToShow;
 	}
 
 	public String getInterstitialVersion() {
 		return interstitialVersion;
 	}
 
 	public String getInterstitialVersionCondition() {
 		return interstitialVersionCondition;
 	}
 
 	public boolean isInterstitial() {
 		return (isInterstitial && (interstitialShow!=null && interstitialShow));
 	}
 
 	public String getAlertType() {
 		return alertType;
 	}
 
 	public String getAlertMsg() {
 		return alertMsg;
 	}
 
 	public String getAlertVersionCondition() {
 		return alertVersionCondition;
 	}
 
 	public String getAlertAppStoreVersion() {
 		return alertAppStoreVersion;
 	}
 
 	public boolean isAlert() {
 		return this.isAlert;
 	}
     
 	public String getAlertUrlAppStore() {
 		return alertUrlAppStore;
 	}
 
 	public Object getProperty(String propertyKey){
 		return getPropertyValue(propertyKey);		
 	}
 
 	public String getConfigDataRaw() {
 		return configDataRaw;
 	}
 	
 	
 	private String getPropertyValue(String property){
 		
 		try{
 			JSONObject jObject = (JSONObject)jsonObject.get(property);
 			String res = jObject.getString("value");
 			if(TextUtils.isEmpty(res)){
 				return null;
 			}
 			
 			return res;
 		} catch(Exception e){ 
 			return null;
 		}
 	}
 }
