 package com.huhuo.cmstatis.car;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.alibaba.fastjson.JSON;
 import com.huhuo.cmsystem.SystemBaseCtrl;
 import com.huhuo.integration.util.TimeUtils;
 
 
 @Controller("cmstatisCtrlCarGeneral")
 @RequestMapping(value="/cmstatis/car/general")
 public class CtrlCarGeneral extends SystemBaseCtrl {
 	
 	protected String basePath = "/car-module-statis/car/general";
 	
 	@Resource(name = "cmstatisServCar")
 	private IServCar iservCar;
 	
 	/*************************************************************
 	 * car type distribution analysis
 	 *************************************************************/
 	@RequestMapping(value="/cartype.do")
 	public String cartype(Model model) {
 		logger.debug("==> cartype distribution analysis");
		Date begin = TimeUtils.offsetMonth(-12, new Date());
 		List<Map<String, Object>> list = iservCar.getCountTrendMonthCartype(
 				TimeUtils.getMonthBegin(begin), TimeUtils.getMonthEnd(new Date()));
 		model.addAttribute("list", JSON.toJSONString(list));
 		return basePath + "/cartype";
 	}
 	
 	/*************************************************************
 	 * store distribution analysis
 	 *************************************************************/
 	@RequestMapping(value="/store.do")
 	public String store(Model model) {
 		logger.debug("==> store distribution analysis");
		Date begin = TimeUtils.offsetMonth(-12, new Date());
 		List<Map<String, Object>> list = iservCar.getCountTrendMonthStore(
 				TimeUtils.getMonthBegin(begin), TimeUtils.getMonthEnd(new Date()));
 		model.addAttribute("list", JSON.toJSONString(list));
 		return basePath + "/store";
 	}
 	
 }
