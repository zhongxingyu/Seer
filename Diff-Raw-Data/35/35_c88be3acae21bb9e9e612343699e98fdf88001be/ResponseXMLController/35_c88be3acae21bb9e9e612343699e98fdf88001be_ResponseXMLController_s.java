 package com.towne.framework.springmvc.controller;
 
 
 
 import java.util.List;
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
 import com.towne.framework.common.model.Trader;
import com.towne.framework.hibernate.bo.Moment;
 import com.towne.framework.springmvc.model.MomentVO;
 import com.towne.framework.springmvc.model.Moments;
 
 /**
  * return xml format data
  * @author towne
  *
  */
 @Controller
@RequestMapping(value="/",method={RequestMethod.GET})
 public class ResponseXMLController {
 	
 	@Autowired
 	IFacadeService ifacadeService;
 	
 	@Autowired
 	private Cache cache;
 	/**
 	 * query a xml date by id
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(value="/moment/{id}",produces=MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Moment getContactInXML(@PathVariable(value="id")int id){
 		Trader trader = new Trader();
		Moment moment = ifacadeService.findById(trader, id);
		return moment;
 	}
 	
 	
 	/**
 	 * query multiple xml data
 	 * @return
 	 */
 	@RequestMapping(value="/moments",produces=MediaType.APPLICATION_XML_VALUE)
 	public @ResponseBody Moments getContactsInXML(){
 		Trader trader = new Trader();
 		List<MomentVO> mo = ifacadeService.query(trader, "select a from Moment a , Page b where a.idMOMENT=b.moment.idMOMENT");
 		Moments moments=new Moments();
 		moments.setTname("towne");
 		moments.setMoments(mo);
 		try {
 			System.out.println(">>>>>> "+cache.get("USER_LOGVO_127.0.0.1",SerializationType.PROVIDER));
 			System.out.println(">>>>>> "+cache.get("USER_SESSION_127.0.0.1",SerializationType.PROVIDER));
 		} catch (TimeoutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CacheException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return moments;
 	}
 }
