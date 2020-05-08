 package org.openmrs.module.dataintegrity.web.controller;
 
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.dataintegrity.DataIntegrityConstants;
 import org.openmrs.module.dataintegrity.IntegrityCheck;
 import org.openmrs.module.dataintegrity.DataIntegrityService;
 import org.openmrs.module.dataintegrity.IntegrityCheckColumn;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 
 @Controller
 public class IntegrityCheckFormController {
 	private static final String EDIT_VIEW = "/module/dataintegrity/editCheck";
 	private static final String SUCCESS_VIEW = "redirect:list.htm";
 	
 	private final Log log = LogFactory.getLog(this.getClass());
 	
 	private DataIntegrityService getDataIntegrityService() {
         return (DataIntegrityService)Context.getService(DataIntegrityService.class);
     }
 
 	/**
 	 * handle initial edit request
 	 *
 	 * @param checkId
 	 * @param modelMap
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value="/module/dataintegrity/edit.htm")
 	public String editIntegrityCheck(@RequestParam(value="checkId", required=true) Integer checkId, ModelMap modelMap) {
 		modelMap.put("check", getDataIntegrityService().getIntegrityCheck(checkId));
 		modelMap.put("columnDatatypes", DataIntegrityConstants.COLUMN_DATATYPES);
         return EDIT_VIEW;
 	}
 
 	@RequestMapping(value="/module/dataintegrity/new.htm")
 	public String newIntegrityCheck(ModelMap modelMap) {
 		modelMap.put("check", new IntegrityCheck());
 		modelMap.put("columnDatatypes", DataIntegrityConstants.COLUMN_DATATYPES);
         return EDIT_VIEW;
 	}
 
 	@RequestMapping(value="/module/dataintegrity/delete.htm")
 	public String deleteIntegrityCheck(@RequestParam(value="checkId", required=true) Integer checkId, WebRequest request) {
 		DataIntegrityService service = Context.getService(DataIntegrityService.class);
 		MessageSourceService mss = Context.getMessageSourceService();
 
 		String checkTitle = mss.getMessage("dataintegrity.delete.checktitle");
 		String deleted = mss.getMessage("dataintegrity.delete.deleted");
 		String notDeleted = mss.getMessage("dataintegrity.delete.notdeleted");
 		String notFound = mss.getMessage("dataintegrity.delete.notfound");
 		String success = "";
 		String error = "";
 		
 		IntegrityCheck check = service.getIntegrityCheck(checkId);
 		if (check != null)
 			try {
 				String name = check.getName();
 				service.deleteIntegrityCheck(check);
 				success = checkTitle + " #" + checkId + " (" + name + ") " + deleted;
 			} catch (Exception e) {
 				error = checkTitle + " #" + checkId + " " + notDeleted + ": " + e.getMessage();
 			}
 		else
 			error = checkTitle + " #" + checkId + " " + notDeleted + ": " + notFound;
 		
 		if (!success.equals(""))
 			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success, WebRequest.SCOPE_SESSION);
 		if (!error.equals(""))
 			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, error, WebRequest.SCOPE_SESSION);
 
 		return SUCCESS_VIEW;
 	}
 	
 	@RequestMapping(value="/module/dataintegrity/save.htm")
 	public String saveIntegrityCheck(WebRequest request,
 			@RequestParam(value="checkId", required=false) Integer checkId,
 			@RequestParam(value="name", required=true) String name,
 			@RequestParam(value="description", required=true) String description,
 			@RequestParam(value="checkLanguage", required=false) String checkLanguage,
 			@RequestParam(value="checkCode", required=false) String checkCode,
 			@RequestParam(value="failureType", required=false) String failureType,
 			@RequestParam(value="failureOperator", required=false) String failureOperator,
 			@RequestParam(value="failureThreshold", required=false) Integer failureThreshold,
 			@RequestParam(value="useTotal", required=false) Boolean useTotal,
 			@RequestParam(value="totalLanguage", required=false) String totalLanguage,
 			@RequestParam(value="totalCode", required=false) String totalCode,
 			@RequestParam(value="useDiscoveryForResults", required=false) Boolean useDiscoveryForResults,
 			@RequestParam(value="resultsLanguage", required=false) String resultsLanguage,
 			@RequestParam(value="resultsCode", required=false) String resultsCode,
 			@RequestParam(value="columns[]", required=false) String[] columns) {
 
 		DataIntegrityService service = (DataIntegrityService)Context.getService(DataIntegrityService.class);
 		MessageSourceService mss = Context.getMessageSourceService();
 		
 		try {
 			// validate code fields
 			validateCode(checkCode);
 			validateCode(totalCode);
 			validateCode(resultsCode);
 			
 			// set booleans if they didn't come through
 			if (useTotal == null)
 				useTotal = false;
 			if (useDiscoveryForResults == null)
 				useDiscoveryForResults = false;
 
 			// get Integrity Check if it exists
 			IntegrityCheck check = service.getIntegrityCheck(checkId);
 			if (check == null) {
 				// create integrity check
 				check = new IntegrityCheck();
 			}
 
 			// set all the other fields (ignore id if it was not found)
 			check.setName(name);
 			check.setDescription(description);
 			check.setCheckCode(checkCode);
 			check.setCheckLanguage(checkLanguage);
 			check.setFailureType(failureType);
 			check.setFailureThreshold(failureThreshold.toString());
 			check.setFailureOperator(failureOperator);
 			check.setTotalLanguage(useTotal ? totalLanguage : null);
 			check.setTotalCode(useTotal ? totalCode : null);
 			check.setResultsLanguage(useDiscoveryForResults ? null : resultsLanguage);
 			check.setResultsCode(useDiscoveryForResults ? null : resultsCode);
 
			// replace existing columns with new ones
			check.setResultsColumns(null);
			for (IntegrityCheckColumn col: generateColumns(columns))
				check.addResultsColumn(col);
 					
 			service.saveIntegrityCheck(check);
 
 			// TODO figure out how to send this value back in an annotated controller :-/
 			String success = name + " " + mss.getMessage("dataintegrity.addeditCheck.saved");
 			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success, WebRequest.SCOPE_SESSION);
 			
 			return SUCCESS_VIEW;
 			
 		} catch (Exception e) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(mss.getMessage("dataintegrity.edit.failed"));
 			sb.append(": ");
 			sb.append(name);
 			sb.append(", Message: ");
 			sb.append(e.getMessage());
 
 			log.warn(sb.toString(), e);
 			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, sb.toString(), WebRequest.SCOPE_SESSION);
 			
 			// TODO retain the data from the form so it can be repopulated
 			return EDIT_VIEW;
 		}
 	}
 
 	private void validateCode(String code) throws Exception {
 		// TODO make this better
 		List<String> tokens = Arrays.asList(code.toLowerCase().split(" "));
 		if (tokens.contains("delete") || tokens.contains("update") || tokens.contains("insert"))
 			throw new Exception("Code has potential to modify database, so it is not allowed.");
 	}
 
 	private Set<IntegrityCheckColumn> generateColumns(String[] columns) {
 		Set<IntegrityCheckColumn> results = new LinkedHashSet<IntegrityCheckColumn>();
 		for(String column: columns) {
 			// deserialize the column
 			String[] items = column.split(":", 7);
 			
 			// properties should be in order [id:show:uid:name:display]
 			IntegrityCheckColumn c = new IntegrityCheckColumn();
 			try {
 				c.setColumnId(StringUtils.hasText(items[0]) ? Integer.parseInt(items[0]) : null);
 			} catch (NumberFormatException e) {
 				log.warn("could not interpret IntegrityCheckColumn id of " 
 						+ items[0] + " as an Integer; defaulting to null.");
 				c.setColumnId(null);
 			}
 			c.setShowInResults(OpenmrsUtil.nullSafeEquals(items[1], "true"));
 			c.setUsedInUid(OpenmrsUtil.nullSafeEquals(items[2], "true"));
 			c.setName(StringUtils.hasText(items[3]) ? items[3] : null);
 			c.setDatatype(StringUtils.hasText(items[4]) ? items[4] : null);
 			c.setUuid(StringUtils.hasText(items[5]) ? items[5] : null);
 			c.setDisplayName(StringUtils.hasText(items[6]) ? items[6] : null);
 			
 			// convert "null" to null in UUID
 			if (OpenmrsUtil.nullSafeEquals(c.getUuid(), "null"))
 				c.setUuid(null);
 			
 			// if the name is null, there's a problem ... probaby a dud
 			if (c.getName() != null)
 				results.add(c);
 		}
 		return results;
 	}
 
 }
