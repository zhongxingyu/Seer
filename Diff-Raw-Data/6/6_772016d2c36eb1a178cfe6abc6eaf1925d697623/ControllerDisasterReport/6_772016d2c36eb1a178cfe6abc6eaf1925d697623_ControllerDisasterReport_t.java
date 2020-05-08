 package org.dlug.disastercenter.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.dlug.disastercenter.common.ConstantDisasterType;
 import org.dlug.disastercenter.common.ConstantReportType;
 import org.dlug.disastercenter.model.ModelImpl;
 import org.dlug.disastercenter.model.ModelReport;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 @RequestMapping("/disaster_report")
 public class ControllerDisasterReport extends ControllerPages{
 	
 	@Autowired
 	private ModelReport modelReport;
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String disasterNews(Locale locale, Model model) {
 /*	TODO: Remove if Process Perfectly
 		List<Map<String, Object>> articleList = modelReport.getReportList(1);
 		
 		if(articleList == null){
 			return "error";
 		}
 		
 		List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
 		
 		for(Map<String, Object> article: articleList){
 			Map<String, Object> tmpItem = new HashMap<String, Object>();
 			
 			tmpItem.put("idx", article.get("idx"));
 			
 			tmpItem.put("lat", article.get("loc_lat"));
 			tmpItem.put("lng", article.get("loc_lng"));
 			tmpItem.put("accuracy", article.get("loc_accuracy"));
 			tmpItem.put("loc_name", article.get("loc_name"));
 			
 			tmpItem.put("type_report", ConstantReportType.code2string((Integer) article.get("type_report")));
 			tmpItem.put("type_disaster", ConstantDisasterType.code2string((Integer) article.get("type_disaster")));
 			
 			String datetime = ModelImpl.sdf.format(((Date) article.get("datetime")));
 			tmpItem.put("datetime", datetime);
 			
 			tmpList.add(tmpItem);
 		}
 		
 		model.addAttribute("list", tmpList);
 */		
 		return "disaster_report_home";
 	}
 	
 	@RequestMapping(value = "/{idx}", method = RequestMethod.GET)
 	public String disasterNewsWithIdx(Model model, 
 			@PathVariable("idx") long idx) {
 		
 		Map<String, Object> article = modelReport.getReport(idx);
 		
 		if(article == null){
 			return "error";
 		}
 		
 		String typeDisaster = ConstantDisasterType.code2string((Integer) article.get("type_disaster"));
 		
 		model.addAttribute("idx", article.get("idx"));
 		
 		model.addAttribute("lat", article.get("loc_lat"));
 		model.addAttribute("lng", article.get("loc_lng"));
 		model.addAttribute("accuracy", article.get("loc_accuracy"));
 		model.addAttribute("loc_name", article.get("loc_name"));
 		model.addAttribute("title", typeDisaster + "(" + article.get("loc_name") + ")");
 		
		model.addAttribute("type_report", (Integer) article.get("type_report"));
		model.addAttribute("type_disaster", (Integer) article.get("type_disaster"));
		model.addAttribute("type_report_string", ConstantReportType.code2string((Integer) article.get("type_report")));
		model.addAttribute("type_disaster_string", typeDisaster);
 		
 		String datetime = ModelImpl.sdf.format(((Date) article.get("datetime")));
 		model.addAttribute("datetime", datetime);
 		model.addAttribute("content", article.get("content"));
 		
 		return "disaster_report_article";
 	}
 	
 }
