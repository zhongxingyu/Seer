 package org.huamuzhen.oa.server.controller;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.huamuzhen.oa.biz.QueryManager;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 @RequestMapping("/query")
 public class QueryController {
 
 	@Resource
 	private QueryManager queryManager;
 	
 	@RequestMapping(value = { "", "/" })
 	public ModelAndView queryReportForm(HttpServletRequest request){
 		ModelAndView mav = new ModelAndView("query");
 		
 		String param1 = request.getParameter("param1");
 		String param2 = request.getParameter("param2");
 		String param3 = request.getParameter("param3");
 		String value1 = request.getParameter("value1");
 		String value2 = request.getParameter("value2");
 		String value3 = request.getParameter("value3");
 		String status = request.getParameter("status");
 		if( null != param1 && null != param2 && null != param3 && null != value1 && null != value2 && null != value3 && null != status){
 			List<ReportForm> reportFormList = queryManager.queryForm(param1,value1,param2,value2,param3,value3,status);
 			mav.addObject("reportFormList", reportFormList);
 		}
 		
 		return mav;	
 		
 	}
 }
