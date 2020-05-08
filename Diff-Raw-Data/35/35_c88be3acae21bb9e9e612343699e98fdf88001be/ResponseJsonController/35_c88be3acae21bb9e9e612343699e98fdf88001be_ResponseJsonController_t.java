 package com.towne.framework.springmvc.controller;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.google.code.ssm.Cache;
 import com.google.code.ssm.api.format.SerializationType;
 import com.google.code.ssm.providers.CacheException;
 import com.towne.framework.common.service.IFacadeService;
 import com.towne.framework.springmvc.model.MomentVO;
 import com.towne.framework.springmvc.model.Moments;
 import com.towne.framework.springmvc.model.PageVO;
 import com.towne.framework.common.model.Trader;
 import com.towne.framework.core.utils.GsonUtil;
 import com.towne.framework.hibernate.bo.Moment;
 
 /**
  * return json format data
  * @author towne
  *
  */
 @Controller
@RequestMapping(value="/json",method={RequestMethod.GET})
 public class ResponseJSONController {
 	
 	@Autowired
 	IFacadeService ifacadeService;
 	
 	@Autowired
 	private Cache cache;
 	
 	@RequestMapping(value="/moment/{id}",produces=MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<PageVO> getContactInJSON(@PathVariable(value="id")long id) throws TimeoutException, CacheException{
 		Trader trader = new Trader();
 		trader.setTraderName("towne");
 		trader.setTraderPassword("123");
 		List<PageVO> pvs = ifacadeService.findPagesByMomentId(trader, id);
 		System.out.println(">>>>>> "+cache.get("USER_LOGVO_127.0.0.1",SerializationType.PROVIDER));
 		System.out.println(">>>>>> "+cache.get("USER_SESSION_127.0.0.1",SerializationType.PROVIDER));
 		return pvs;
 	}
 	
 	
 	@RequestMapping(value="/moments",produces=MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody Moments getContactsInJSON(){
 		Trader trader = new Trader();
 		List<MomentVO> mo = ifacadeService.query(trader, "select a from Moment a , Page b where a.idMOMENT=b.moment.idMOMENT");
 		Moments moments=new Moments();
 		moments.setTname("towne");
 		moments.setMoments(mo);
 		return moments;
 	}
 	
 	@RequestMapping(value="/list/{jparam}",produces=MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody Moment tetete(@PathVariable(value="jparam") String jp)
 	{
 		System.out.println(jp); 
 		Moment ctt = (Moment) GsonUtil.jsonToModel(jp,Moment.class);
 //		List<Contact> ss =  (List<Contact>) GsonUtil.getJsonValue(jp, "contacts");
 		Map<?, ?> map = GsonUtil.jsonToMap(jp);
 		System.out.println(map.get("trader"));
 		Trader trader = (Trader) GsonUtil.getJsonValue(jp, "trader", Trader.class);
 	     ; 
 		System.out.println(trader);
 //		return trader;
 		return ctt;
 	}
 	
 }
