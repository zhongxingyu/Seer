 package net.canadensys.dataportal.vascan.controller.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.canadensys.dataportal.vascan.APIService;
 import net.canadensys.dataportal.vascan.model.api.APIErrorResult;
 import net.canadensys.dataportal.vascan.model.api.VascanAPIResponse;
 
import org.apache.commons.lang3.math.NumberUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.MissingServletRequestParameterException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /**
  * Controller responsible for API calls.
  * @author canadensys
  *
  */
 @Controller
 public class APIController {
 	
 	@Autowired
 	private APIService apiService;
 	
 	//Own Jackson Object Mapper to add JSONP support
 	public static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
 	
 	private static final APIErrorResult NOT_FOUND_RESULT = new APIErrorResult("not found");
 	private static final APIErrorResult BAD_REQUEST_RESULT = new APIErrorResult("bad request");
 	
 	@RequestMapping(value="/api/{version}/search",method={RequestMethod.GET})
 	public @ResponseBody Object handleGetSearch(@RequestParam String q, @PathVariable String version, HttpServletResponse response){
 		if(!apiService.getAPIVersion().equals(version)){
 			return onNotFound(response);
 		}
 		
 		String[] dataParts = q.split(APIControllerHelper.DATA_SEPARATOR,2);
 		
 		if(dataParts.length == 1){
 			int possibleTaxonID = NumberUtils.toInt(q, -1);
 			if(possibleTaxonID == -1){
 				return apiService.search(null,dataParts[0]);
 			}
 			return apiService.search(possibleTaxonID);
 		}
 		else{
 			return apiService.search(dataParts[0],dataParts[1]);
 		}
 	}
 	
 	 @ExceptionHandler(value=MissingServletRequestParameterException.class)
 	 @ResponseStatus(HttpStatus.BAD_REQUEST)
 	 @ResponseBody
 	 public APIErrorResult handleException(MissingServletRequestParameterException ex) {
 		 return BAD_REQUEST_RESULT;
 	 }
 	
 	@RequestMapping(value="/api/{version}/search",method={RequestMethod.POST})
 	public @ResponseBody Object handlePostSearch(@RequestParam String q, @PathVariable String version, HttpServletResponse response){
 		if(!apiService.getAPIVersion().equals(version)){
 			return onNotFound(response);
 		}
 		List<String> dataList = new ArrayList<String>();
 		List<String> idList = new ArrayList<String>();
 		APIControllerHelper.splitIdAndData(q, dataList, idList);
 		
 		return apiService.search(idList,dataList);
 	}
 	
 	/**
 	 * Handle JSONP API calls
 	 * @param q
 	 * @param callback
 	 * @param response
 	 */
 	@RequestMapping(value={"/api/{version}/search.json"}, method=RequestMethod.GET, params="callback")
 	public void handleJSONP(@RequestParam String q, @RequestParam String callback,
 			 HttpServletRequest request, HttpServletResponse response){
 		
 		//make sure the answer is set as UTF-8
 		response.setCharacterEncoding("UTF-8");
 		response.setContentType(APIControllerHelper.JSONP_CONTENT_TYPE);
 		
 		if(APIControllerHelper.JSONP_ACCEPTED_CHAR_PATTERN.matcher(callback).matches()){
 			//JSONP handling
 			VascanAPIResponse apiResponse;
 			
 			String json = "";
 			String[] dataParts = q.split(APIControllerHelper.DATA_SEPARATOR,2);
 			
 			if(dataParts.length == 1){
 				int possibleTaxonID = NumberUtils.toInt(q, -1);
 				if(possibleTaxonID == -1){
 					apiResponse = apiService.search(null,dataParts[0]);
 				}
 				apiResponse = apiService.search(possibleTaxonID);
 			}
 			else{
 				apiResponse = apiService.search(dataParts[0],dataParts[1]);
 			}
 			
 			try {
 				json = JACKSON_MAPPER.writeValueAsString(apiResponse);
 				
 				String responseTxt = callback + "("+json+");";
 				response.getWriter().print(responseTxt);
 				response.setContentLength(responseTxt.length());
 				response.getWriter().close();
 				
 				//LOGGER.info("Date(jsonp)|{}|{}|{}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
 				
 			} catch (JsonProcessingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else{
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param response used to set the 404 status
 	 * @return NOT_FOUND_RESULT
 	 */
 	private APIErrorResult onNotFound(HttpServletResponse response){
 		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		return NOT_FOUND_RESULT;
 	}
 }
