 package com.umeng.ufp.publisher.controller;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpException;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.umeng.core.utils.DateUtil;
 import com.umeng.core.utils.ExportUtil;
 import com.umeng.ufp.core.domain.AdSlot;
 import com.umeng.ufp.core.domain.Ad;
 import com.umeng.ufp.core.domain.ReportCounter;
 import com.umeng.ufp.core.domain.User;
 import com.umeng.ufp.publisher.utils.Constants;
 
 @Controller
 @RequestMapping(value = "/report")
 public class ReportCounterController extends BaseController<ReportCounter, Integer> {
 
     public static final String ALLOWED_LOGIN_ADDR = "adpm";
     
     private Logger log = Logger.getLogger(getClass());
 
     @RequestMapping
     public String index(HttpServletRequest request, HttpServletResponse response, Model model) {
         /*
          * 查询分页数据
          */
         return "report/list";
     }
     
     
     @RequestMapping(value = "app")
     public String appList(HttpServletRequest request, 
     		HttpServletResponse response, 
     		Model model) {
     	
         return "report/app";
     }
     
     @RequestMapping(value = "ad")
     public String adList(HttpServletRequest request, 
     		HttpServletResponse response, 
     		Model model) {
     	
         return "report/ad";
     }
     
     @RequestMapping(value = "adslot")
     public String adSlotList(HttpServletRequest request, 
     		HttpServletResponse response, 
     		Model model) {
         
         return "report/adslot";
     }
     
     @RequestMapping(value = "adorder")
     public String adOrderList(HttpServletRequest request, 
     		HttpServletResponse response, 
     		Model model) {
         
         return "report/adorder";
     }
     
     
     @RequestMapping(value = "total")
     @ResponseBody
     public Map<String,Object> totalList(HttpServletRequest request, 
     		HttpServletResponse response, 
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		@RequestParam(value="category", required = false) String category,
     		Model model) {
     	User user = getLoginUser(request);
     	
     	ReportCounter sumCounter = null;
     	List<ReportCounter> reportList = null;
     	Map<String, Object> resultParams = new HashMap<String, Object>();
     	if(user != null) {
     		try {
 	    		List<Integer> adSlotIds = adSlotService.getAdSlotIdListByUserId(user.getId());
 	    		if(adSlotIds.size() > 0) {
 	    			Map<String, Object> date = getDate(startDate, endDate, period);
 	    			startDate = (String)date.get("startDate");
 	    			endDate = (String)date.get("endDate");
 	    			reportList = reportCounterService.findByTotal(adSlotIds, startDate, endDate, category);
 	    			reportList = autoPadding(reportList, startDate, endDate);
 	    			
 	    			
 	    			if(category != null && category.equals(Constants.CATEGORY_XP)) {
 	    				for(Integer adSlotId:adSlotIds){
 				        	AdSlot adSlot = adSlotService.getById(adSlotId);
 				        	try{
			    	    		if(adSlot.getXpEnable() && adSlot.getAppkey() != null) 
 			    	    			reportList = getXPReportList(reportList, adSlot.getAppkey(), startDate, endDate);
 				        	} catch (Exception e) {
 				        		resultParams.put("status", "failed");
 				        		resultParams.put("message", e.getMessage());
 				        		return resultParams;
 				        	}
 	    				
 	    				}
 	    			}
 	    			
 	    			sumCounter = summary(reportList);
 	    	    		
 	    		}
 	    		resultParams.put("status", "ok");
 	        	resultParams.put("summary", sumCounter);
 	        	resultParams.put("reportList", reportList);
     		} catch (Exception e) {
     			resultParams.put("message", e.getMessage());
     		}
     	}
     	return resultParams;
     }
     
     @RequestMapping(value = "ad/{adId}")
     @ResponseBody
     public Map<String,Object> adReportList(@PathVariable int adId, HttpServletRequest request, 
     		HttpServletResponse response, 
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		Model model) {
     	User user = getLoginUser(request);
     	List<ReportCounter> reportList = null;
     	Map<String, Object> resultParams = new HashMap<String, Object>();
     	ReportCounter sumCounter = null;
     	
     	if(user != null) {
     		try {
 	    		List<Integer> adIds = adService.getAdIdListByUserId(user.getId());
 	    		if(adIds.size() > 0 && adIds.contains(adId)) {
 			    	Map<String, Object> date = getDate(startDate, endDate, period);
 					startDate = (String)date.get("startDate");
 					endDate = (String)date.get("endDate");
 			    	reportList = reportCounterService.findByAdId(adIds, adId, startDate, endDate);
 			    	sumCounter = summary(reportList);
 	    	    	reportList = autoPadding(reportList, startDate, endDate);
 	    		}
 	    		resultParams.put("status", "ok");
 	        	resultParams.put("summary", sumCounter);
 	        	resultParams.put("reportList", reportList);
     		} catch(Exception e) {
     			resultParams.put("message", e.getMessage());
     		}
     	}
         return resultParams;
     }
 
     @RequestMapping(value = "ad_export/{exportType}")
     @ResponseBody
     public void exportAd(@PathVariable String exportType, HttpServletRequest request, 
     		HttpServletResponse response, 
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		Model model) {
     	User user = getLoginUser(request);
     	Map<String, List<Object>> resultList = new HashMap<String, List<Object>>(0);
     	List<String> dateList = new ArrayList<String>(0);
     	
     	if(user != null) {
     		try {
 	    		List<Integer> adIds = adService.getAdIdListByUserId(user.getId());
 	    		if(adIds.size() > 0) {
 			    	Map<String, Object> date = getDate(startDate, endDate, period);
 					startDate = (String)date.get("startDate");
 					endDate = (String)date.get("endDate");
 					for (Integer adId: adIds){
 						List<ReportCounter> currentReportList = null;
 						currentReportList = reportCounterService.findByAdId(adIds, adId, startDate, endDate);
 						currentReportList = autoPadding(currentReportList, startDate, endDate);
 						List<Object> exportList = generateInfo(currentReportList, exportType);
 						Ad ad = adService.getById(adId);
 						String adName = ad.getName();
 						resultList.put(adName, exportList);	
 					}
 					dateList = getDateList(endDate, startDate);
 					String fileName = exportType + "_" + startDate + "_" + endDate + ".csv";
 					ExportUtil.writeCsv(response, dateList, resultList, fileName);
 	    		}
     		} catch(Exception e) {
     			e.printStackTrace();
     		}
     	}
     }
     
     @RequestMapping(value = "adslot/{adSlotId}")
     @ResponseBody
     public Map<String,Object> adSlotReportList(@PathVariable int adSlotId, HttpServletRequest request, 
     		HttpServletResponse response, 
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		@RequestParam(value="category", required = false) String category,
     		Model model) {
     	User user = getLoginUser(request);
     	List<ReportCounter> reportList = null;
     	Map<String, Object> resultParams = new HashMap<String, Object>();
     	ReportCounter sumCounter = null;
     	
     	if(user != null) {
     		try {
     			List<Integer> adSlotIds = adSlotService.getAdSlotIdListByUserId(user.getId());
 	    		if(adSlotIds.size() > 0 && adSlotIds.contains(adSlotId)) {
 		    		Map<String, Object> date = getDate(startDate, endDate, period);
 		    		startDate = (String)date.get("startDate");
 		    		endDate = (String)date.get("endDate");
 		        	reportList = reportCounterService.findByAdSlotId(adSlotIds, adSlotId, startDate, endDate, category); 
 		        	reportList = autoPadding(reportList, startDate, endDate);
 	    			if(category != null && category.equals(Constants.CATEGORY_XP)) {
 			        	reportList = autoPadding(reportList, startDate, endDate);
 			        	AdSlot adSlot = adSlotService.getById(adSlotId);
 			        	try{
 		    	    		if(adSlot.getXpEnable() && adSlot.getAppkey() != null) 
 		    	    			reportList = getXPReportList(reportList, adSlot.getAppkey(), startDate, endDate);
 			        	} catch (Exception e) {
 			        		resultParams.put("status", "failed");
 			        		resultParams.put("message", e.getMessage());
 			        		return resultParams;
 			        	}
 	    				
 	    			}
     				sumCounter = summary(reportList);
 	    		}
 	    		resultParams.put("status", "ok");
 	        	resultParams.put("summary", sumCounter);
 	        	resultParams.put("reportList", reportList);
     		} catch(Exception e) {
     			resultParams.put("message", e.getMessage());
     		}
     	}
         return resultParams;
     
     }
     
     @RequestMapping(value = "app/{appId}")
     @ResponseBody
     public Map<String,Object> appReportList(@PathVariable int appId, HttpServletRequest request, 
     		HttpServletResponse response,  
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		@RequestParam(value="category", required = false) String category,
     		Model model) {
     	User user = getLoginUser(request);
     	List<ReportCounter> reportList = null;
     	Map<String, Object> resultParams = new HashMap<String, Object>();
     	ReportCounter sumCounter = null;
     	
     	if(user != null) {
     		try {
 				List<Integer> appIds = appService.getAppIdListByUserId(user.getId());
 	    		if(appIds.size() > 0 && appIds.contains(appId)) {
 	    			Map<String, Object> date = getDate(startDate, endDate, period);
 	    			startDate = (String)date.get("startDate");
 	    			endDate = (String)date.get("endDate");
 	    	    	reportList = reportCounterService.findByAppId(appIds, appId, startDate, endDate, category); 	
 	    	    	reportList = autoPadding(reportList, startDate, endDate);
 	    	    	if(category != null && category.equals(Constants.CATEGORY_XP)) {
 	    	    		//1.get adslot's appkey in app
 	    	    		List<Integer> adSlotIds = adSlotService.getAdSlotIdListByAppId(appId);
 		    	    	Set<String> appkeySet = new HashSet<String>();
 		    	    	//2.filter the same appkey
 		    	    	for(Integer adSlotId : adSlotIds) {
 		    	    		AdSlot adSlot = adSlotService.getById(adSlotId);
 		    	    		if(adSlot.getXpEnable() && adSlot.getAppkey() != null) 
 		    	    			appkeySet.add(adSlot.getAppkey());
 		    	    	}
 		    	    	//3.for every distinct appkey get xp promoter report
 		    	    	try {
 			    	    	for(String appkey : appkeySet) {
 			    	    		reportList = getXPReportList(reportList, appkey, startDate, endDate);
 			    	    	}
 		    	    	} catch (Exception e) {
 			    	    	resultParams.put("status", "failed");
 			    	    	resultParams.put("message", e.getMessage());
 			    	    	return resultParams;
 		    	    	}
 	    			}
 	    			sumCounter = summary(reportList);
 	    		}
 	    		resultParams.put("status", "ok");
 	        	resultParams.put("summary", sumCounter);
 	            resultParams.put("reportList", reportList);
     			
     		} catch(Exception e) {
     			resultParams.put("message", e.getMessage());
     		}
     	}
         return resultParams;
     }
     
     @RequestMapping(value = "adorder/{adOrderId}")
     @ResponseBody
     public Map<String,Object> adOrderReportList(@PathVariable int adOrderId, HttpServletRequest request, 
     		HttpServletResponse response, 
     		@RequestParam(value="period", required = false) String period,
     		@RequestParam(value="startDate", required = false) String startDate,
     		@RequestParam(value="endDate", required = false) String endDate,
     		Model model) {
     	
     	
     	User user = getLoginUser(request);
     	List<ReportCounter> reportList = null;
     	Map<String, Object> resultParams = new HashMap<String, Object>();
     	ReportCounter sumCounter = null;
     	
     	if(user != null) {
     		try{
 	    		List<Integer> adOrderIds = adOrderService.getAdOrderIdListByUserId(user.getId());
 	    		if(adOrderIds.size() > 0 && adOrderIds.contains(adOrderId)) {
 			    	Map<String, Object> date = getDate(startDate, endDate, period);
 					startDate = (String)date.get("startDate");
 					endDate = (String)date.get("endDate");
 			    	reportList = reportCounterService.findByAdOrderId(adOrderIds, adOrderId, startDate, endDate);
 			    	sumCounter = summary(reportList);
 			    	reportList = autoPadding(reportList, startDate, endDate);
 	    		}
 	    		resultParams.put("status", "ok");
 	        	resultParams.put("summary", sumCounter);
 	            resultParams.put("reportList", reportList);
     		} catch(Exception e) {
     			resultParams.put("message", e.getMessage());
     		}
     	}
         
         return resultParams;
     
     }
     
     private ReportCounter summary(List<ReportCounter> reportList) {
     	ReportCounter count = new ReportCounter();
     	
     	for(ReportCounter reportCounter : reportList) {
     		count.plus(reportCounter);
     	}
     	return count;
     }
     
     private Map<String, Object> getPeriodDate(String period) {
     	
 		if(period == null) return null;
 		String startDate = new String();
     	String endDate = new String();
     	
     	Map<String, Object> param = new HashMap<String, Object>();
 		String today = DateUtil.today();
 		
     	if(period.equals("today")) {
     		startDate = today;
     		endDate = today;
     	} else if (period.equals("yesterday")) {
     		endDate = DateUtil.dateplus(today, 1);
     		startDate = DateUtil.dateplus(today, 1);
     	} else if (period.equals("lastWeek")) {
     		startDate = DateUtil.getLastWeekDay(today, 1);	
     		endDate = DateUtil.getLastWeekDay(today, 7);
     	} else if (period.equals("lastMonth")) {
     		endDate = DateUtil.getLastMonthLastDate(today);
     		startDate = DateUtil.getLastMonthFirstDate(today);
     	} else if (period.equals("lastQuarter")) {
     		startDate = DateUtil.getLastQuarterFirstDate(today);
     		endDate = DateUtil.getLastQuarterLastDate(today);
     	} else {
     		return null;
     	}
     	param.put("startDate", startDate);
     	param.put("endDate", endDate);
 		return param;
     	
     }
     private Integer getPeriodDay(String period) {
 
     	Integer day = null;
     	if(period == null)
     		return null;
     	
     	if (period.startsWith("day_")) {
     		day = Integer.parseInt(period.substring(4));
     	} else if (period.startsWith("month_")) {
     		day = Integer.parseInt(period.substring(6)) * 30;
     	}
     	
     	return day;
     	
     }
     
     private Map<String,Object> getDate(String startDate, String endDate, String period) {
     	Map<String, Object> param = new HashMap<String, Object>();
     		
     	Integer day = getPeriodDay(period);
 
     	if(startDate != null && endDate == null) {
     		if(day == null || day < 0)
     			endDate = DateUtil.dateplus(startDate, -6);
     		else
     			endDate = DateUtil.dateplus(startDate, -day + 1); 			
     	} else if(startDate == null && endDate != null) {
     		if(day == null || day > 0) 
     			startDate = DateUtil.dateplus(endDate, 6);
     		else
     			startDate = DateUtil.dateplus(endDate, -day - 1);	
     	} else if(startDate == null && endDate == null) {	
     		if(day == null) {
     			Map<String, Object> date = getPeriodDate(period);
     			if(date!= null) {
     				startDate = (String)date.get("startDate");
     				endDate = (String)date.get("endDate");
     			} else {
     				endDate = DateUtil.dateplus(DateUtil.getDateString(), 1);
     				startDate = DateUtil.dateplus(endDate, 6);
     			}
     		} else if(day > 0) {
 	    		endDate = DateUtil.getDateString();
 	    		startDate = DateUtil.dateplus(endDate, 6);
     		} else {
     			endDate = DateUtil.dateplus(DateUtil.getDateString(), 1);
     			startDate = DateUtil.dateplus(endDate, -day - 1);
     		}
     	} else if(startDate.compareTo(endDate) > 0) {
     		startDate = DateUtil.dateplus(endDate, 6);
     	}
     	param.put("startDate", startDate);
     	param.put("endDate", endDate);
 		return param;
     }
     
     private List<ReportCounter> autoPadding(List<ReportCounter> list, String startDate, String endDate) {
 
     	if(startDate.compareTo(endDate) > 0)
     		return null;
     	int index = 0;
     	List<ReportCounter> reportList = new ArrayList<ReportCounter>();
     	
     	  	
 		for(String date = endDate; date.compareTo(startDate) >= 0; date = DateUtil.dateplus(date, 1)) {
 			
     		if(index < list.size() && list.get(index).getDate().equals(date)) {
     			reportList.add(list.get(index));
     			++index;
     		}
     		else
     		{
     			ReportCounter reportCounter = new ReportCounter();
     			reportCounter.setDate(date);
     			reportList.add(reportCounter);
     		}
     	}
 		
 		return reportList;
     }
     
     private List<ReportCounter> getXPReportList(List<ReportCounter> reportList, String appkey, String startDate, String endDate) throws HttpException, IOException {
 
     	
 		List<Map<String, Object>> xpReportList = new ArrayList<Map<String, Object>>(0);
 		xpReportList = xpWebService.getXPPromoterReport(appkey, startDate, endDate);
 		for(int index = 0; index < reportList.size() && index < xpReportList.size(); index++) {
 			try {
 				ReportCounter reportCounter = reportList.get(index);
 				if(reportCounter.getDate().equals(xpReportList.get(index).get("date"))) {
 					Integer promoterImpression = reportCounter.getPromoterImpression() 
 							+ Integer.parseInt((String)xpReportList.get(index).get("impression"));
 					reportCounter.setPromoterImpression(promoterImpression);
 					
 					Integer promoterClick = reportCounter.getPromoterClick() 
 							+ Integer.parseInt((String)xpReportList.get(index).get("click"))
 							+ Integer.parseInt((String)xpReportList.get(index).get("appstore_click"));
 					reportCounter.setPromoterClick(promoterClick);
 					
 					Integer promoterAppstoreClick = reportCounter.getPromoterAppstoreClick() 
 							+ Integer.parseInt((String)xpReportList.get(index).get("appstore_click"));
 					reportCounter.setPromoterAppstoreClick(promoterAppstoreClick);
 					
 					Integer promoterDownload = reportCounter.getPromoterDownload() 
 							+ Integer.parseInt((String)xpReportList.get(index).get("download"));
 					reportCounter.setPromoterDownload(promoterDownload);
 					
 					double promoterClickRate = promoterImpression == 0 ? 0.0 : (double)promoterClick /promoterImpression; 
 					reportCounter.setPromoterClickRate(promoterClickRate);
 				}
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 		return reportList;
     }
 
     private List<Object> generateInfo(List<ReportCounter> list, String exportType){
     	List<Object> result = new ArrayList<Object>(0);
     	for(ReportCounter rc: list){
     		if (exportType.equals("click")){
     			result.add(rc.getClick());
     		}
     		if (exportType.equals("impression")){
     			result.add(rc.getImpression());
     		}
     		if (exportType.equals("download")){
     			result.add(rc.getDownload());
     		}
     		if (exportType.equals("clickRate")){
     			result.add(rc.getClickRate() * 100);
     		}
     		if (exportType.equals("revenue")){
     			result.add(rc.getRevenue());
     		}
     		if (exportType.equals("conversionRate")){
     			Integer click = rc.getClick();
     			Integer download = rc.getDownload();
     			// TODO: format the conversionRate, X.XX would be acceptable.
     			Double conversionRate = (click == 0) ? 0.00 : (double)download * 100 / click;
     			String formatResult = String.format("%.2f", conversionRate);
     			result.add(formatResult);
     		}
     	}
     	return result;
     }
     
     private List<String> getDateList(String endDate, String startDate){
     	List<String> dateList = new ArrayList<String>(0);
     	for(String date = endDate; date.compareTo(startDate) >= 0; date = DateUtil.dateplus(date, 1)){
     		dateList.add(date);
     	}
     	return dateList;
     }
 }
