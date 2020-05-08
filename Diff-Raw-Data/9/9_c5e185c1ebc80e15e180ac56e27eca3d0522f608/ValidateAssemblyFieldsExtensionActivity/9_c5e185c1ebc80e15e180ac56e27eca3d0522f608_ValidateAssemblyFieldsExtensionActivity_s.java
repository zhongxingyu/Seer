 package com.varian.production.service.plugin;
 import java.util.List;
 import com.sap.me.appconfig.FindSystemRuleSettingRequest;
 import com.sap.me.appconfig.SystemRuleSetting;
 import com.sap.me.common.ObjectReference;
 import com.sap.me.production.AssemblyDataField;
 import com.sap.me.production.AssemblyDataValidationRequest;
 import com.sap.me.production.SfcKeyData;
 import com.varian.hook.exception.InvalidGlassSerialNumberException;
 import com.visiprise.frame.service.ext.ActivityInterface;
 import com.sap.me.extension.Services;
 import com.sap.me.production.SfcStateServiceInterface;
 import com.sap.me.appconfig.SystemRuleServiceInterface;
 
 public class ValidateAssemblyFieldsExtensionActivity implements
 		ActivityInterface<AssemblyDataValidationRequest> {
 
 	private static final String COM_SAP_ME_APPCONFIG = "com.sap.me.appconfig";
 	private static final String SYSTEM_RULE_SERVICE = "SystemRuleService";
 	private static final String SFC_STATE_SERVICE = "SfcStateService";
 	private static final String COM_SAP_ME_PRODUCTION = "com.sap.me.production";
 	private static final long serialVersionUID = 1L;
 	private SfcStateServiceInterface sfcStateService;
 	private SystemRuleServiceInterface systemRuleService;
 	
 	public void execute(com.sap.me.production.AssemblyDataValidationRequest dto)
 		throws Exception {
 		initServices();
 		boolean sysruleval = false;
 		String ruleName = "Z_ASSEMBLY_DATA_VALIDATION";
 		FindSystemRuleSettingRequest findsysrulereq = new FindSystemRuleSettingRequest();
 		findsysrulereq.setRuleName(ruleName);
 		SystemRuleSetting sysrulesetting = systemRuleService.findSystemRuleSetting(findsysrulereq);
 		sysruleval =  Boolean.valueOf(sysrulesetting.getSetting().toString());	
 		if (sysruleval){
 		String sfcSerial = null;	
 		String assemblyData = null;
 		String lastthree = null;
 		String lastpiece = null;
 		String split[] = null;
 		String sfcPrefix =null;
 		int secondlength = 0;
 		ObjectReference objsfcRef = new ObjectReference(dto.getSfcRef().toString());
 		SfcKeyData sfcKeyData = sfcStateService.findSfcKeyDataByRef(objsfcRef);
 		String sfcNumber = sfcKeyData.getSfc(); 
 		try{
 		lastthree = sfcNumber.substring((sfcNumber.length()-4),sfcNumber.length());
      	lastpiece = null;        
         split = sfcNumber.split("-");
         sfcPrefix = split[0];
         secondlength = split[1].length();
 		} catch (Exception e) {
 			throw new InvalidGlassSerialNumberException(20119, sfcNumber);
 		}
         if( (split.length>1) && (secondlength>3)){        
         	if (!lastthree.contains("-")){
         		sfcSerial = sfcNumber.substring(3,sfcNumber.length());
             } else {
             lastpiece = sfcNumber.substring((sfcNumber.lastIndexOf("-")+1),sfcNumber.length());
          		if (lastpiece.length() > 2){
          			throw new InvalidGlassSerialNumberException(20124, sfcNumber);
          		} else if (lastpiece.length() == 0){
    	        		throw new InvalidGlassSerialNumberException(20122, sfcNumber);
    	        	} else {
          			sfcSerial =  sfcNumber.substring(3,sfcNumber.lastIndexOf("-"));
          		}
             }
         } else {
         	throw new InvalidGlassSerialNumberException(20122, sfcNumber);
         }
      	
 		
 		List<AssemblyDataField> assemblyDataFields = dto.getAssemblyDataFields();
 		for (AssemblyDataField assemblyDataField : assemblyDataFields) {
 			if ("GLASS_SERIAL_NUMBER".equals(assemblyDataField.getAttribute())) {
 				assemblyData = assemblyDataField.getValue();
 					if (assemblyData == null || sfcSerial == null){
 						throw new InvalidGlassSerialNumberException(20116, sfcNumber);
 					}
 					if (!assemblyData.equals(sfcSerial)){
 					throw new InvalidGlassSerialNumberException(20117, sfcNumber);
 					}				
 			}
 		if ("SFC".equals(assemblyDataField.getAttribute())) {
 			String csfc = assemblyDataField.getValue();
 			String clastthree  =null;
 			 String clastpiece = null;
 			 String csplit[] = null;
 			 String assemblyDataPrefix = null;
 			 int seclength = 0;
 			 try {
 			clastthree = csfc.substring((csfc.length()-4),csfc.length());			
 			csplit = csfc.split("-");
 			seclength = csplit[1].length();
 			assemblyDataPrefix = csplit[0];
 			 } catch (Exception e) {
 					throw new InvalidGlassSerialNumberException(20119, sfcNumber);
 			 }
 			if (sfcPrefix.length()==2 && assemblyDataPrefix.length()==2){
 				if (sfcPrefix.equals("UR")){
 					if (!assemblyDataPrefix.equals("FM")){
 						throw new InvalidGlassSerialNumberException(20120,sfcNumber);
 					}
 				} else if (sfcPrefix.equals("TR")){
 					if (!assemblyDataPrefix.equals("UR")){
 						throw new InvalidGlassSerialNumberException(20121,sfcNumber);
 					}
 				} else {
 					throw new InvalidGlassSerialNumberException(20119,sfcNumber);
 				}
 				
 			} else {
 				throw new InvalidGlassSerialNumberException(20119, sfcNumber);
 			}
 	       
 	        if( (csplit.length>1) && (seclength>3)){
 	   	        if (!clastthree.contains("-")){
 	        	assemblyData = csfc.substring(3,csfc.length());
 	   	        } else {
 	   	        clastpiece = csfc.substring((csfc.lastIndexOf("-")+1),csfc.length());
 	   	        	if (clastpiece.length() > 2){
 	   	        		throw new InvalidGlassSerialNumberException(20125, csfc);
 	   	        	} else if (clastpiece.length() == 0){
 	   	        		throw new InvalidGlassSerialNumberException(20123, csfc);
 	   	        	} else {	   	        	
 	   	        		assemblyData =  csfc.substring(3,csfc.lastIndexOf("-"));
 	   	        	}
 	   	        }
 	        } else {
 	        	throw new InvalidGlassSerialNumberException(20123, csfc);
 	        }
 			
 			if (assemblyData == null || sfcSerial == null){
 				throw new InvalidGlassSerialNumberException(20116, sfcNumber);	
 			}
 			if (!assemblyData.equals(sfcSerial)){
 				throw new InvalidGlassSerialNumberException(20118, sfcNumber);
 				}		
 			}
 		}
 		
 		}
 		}
 
 	private void initServices(){
 		sfcStateService = Services.getService(COM_SAP_ME_PRODUCTION,SFC_STATE_SERVICE);
 		systemRuleService = Services.getService(COM_SAP_ME_APPCONFIG,SYSTEM_RULE_SERVICE);
 	}
 	}
