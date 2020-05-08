 package com.bgg.farmstoryback.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.bgg.farmstoryback.common.DateUtil;
 import com.bgg.farmstoryback.service.StatsService;
 import com.google.api.services.analytics.model.GaData;
 import com.google.api.services.analytics.model.GaData.ColumnHeaders;
 
 @Controller
 public class StatsController {
 	
 	private Logger logger = LoggerFactory.getLogger(StatsController.class);
 	
 	@Autowired
 	private StatsService statsService;
 	
 	@Autowired
 	private DateUtil dateUtil;
 	
 	@RequestMapping(value = "/stats/getCode.do")
 	public String getCode(Model model,  @RequestParam Map parameter) {
 		
 		return String.format("redirect:%s", statsService.getCodeUrl());
 	}
 	
 	@RequestMapping(value = "/stats/saveAccessToken.do")
 	public String saveAccessToken(Model model,  @RequestParam Map<String, String> parameter) {
 		
 		//TODO 엑세스 토큰 생성 실패시 시나리오?
 		model.addAttribute("accessToken", statsService.saveAccessToken(parameter.get("code")));
 		return "stats/setting";
 	}
 	
 	@RequestMapping(value = "/stats/checkAccessToken.do")
 	public String checkAccessToken(Model model,  @RequestParam Map<String, String> parameter) {
 		
 		model.addAttribute("isValidAccessToken", statsService.checkAccessToken());
 		return "stats/setting";
 	}
 	
 	@RequestMapping(value = "/stats/revoke.do")
 	public String revoke(Model model,  @RequestParam Map<String, String> parameter) {
 		
 		return String.format("redirect:https://accounts.google.com/o/oauth2/revoke?token=%s", statsService.getAccessToken());
 	}
 	
 	@RequestMapping(value = "/stats/view.do")
 	public String view(Model model,  @RequestParam Map parameter) {
 		
 		logger.debug("My access token is {}", statsService.getAccessToken());
		{//선차트용 데이터 가져오기
			String dimension = "ga:date"; //year,month,week
			model.addAttribute("lineChartData", statsService.getVisitor(dimension, dateUtil.add(-30), dateUtil.today()));
		}
 		
 		{//평균정보 데이터 가져오기
 			
 			String metrics = "ga:visitors,ga:visits,ga:avgTimeOnSite,ga:pageviews,ga:avgTimeOnPage";
 			model.addAttribute("averageData", statsService.getAverage(metrics, dateUtil.add(-30), dateUtil.today()));
 			
 			//새로운 데이터용
 			model.addAttribute("gaData", statsService.getLately("ga:visits,ga:newVisits,ga:avgTimeOnSite,ga:pageviewsPerVisit", "ga:date", dateUtil.add(-30), dateUtil.today()));
 		}
 		
 		{//국가/브라우져별 방문수
 			model.addAttribute("countryData", statsService.getLately("ga:visits", "ga:country", dateUtil.add(-30), dateUtil.today()));
 			model.addAttribute("browserData", statsService.getLately("ga:visits", "ga:browser", dateUtil.add(-30), dateUtil.today()));
 		}
 
 		return "stats/view";
 	}
 	
 	@RequestMapping(value = "/stats/setting.do")
 	public String setting(Model model,  @RequestParam Map parameter) {
 		
 		return "stats/setting";
 	}
 	
 }
