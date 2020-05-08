 package com.huhuo.cmcar.cartype;
 
 import java.io.OutputStream;
import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.huhuo.carservicecore.cust.car.ModelCarType;
 import com.huhuo.carservicecore.cust.car.ModelChargeStandard;
 import com.huhuo.integration.base.BaseCtrl;
 import com.huhuo.integration.db.mysql.Condition;
 import com.huhuo.integration.db.mysql.Dir;
 import com.huhuo.integration.db.mysql.Order;
 import com.huhuo.integration.db.mysql.Page;
 import com.huhuo.integration.exception.HuhuoException;
 import com.huhuo.integration.web.Message;
 import com.huhuo.integration.web.Message.Status;
 
 
 @Controller("cmcarCtrlCarType")
 @RequestMapping(value="/cmcar/cartype")
 public class CtrlCarType extends BaseCtrl {
 	
 	protected String basePath = "/car-module-car";
 	
 	@Resource(name = "cmcarServCarType")
 	private IServCarType iservCarType;
 	
 	@Resource(name = "cmcarServChargeStandards")
 	private IServChargeStandard iServChargeStandard;
 	
 	/*************************************************************
 	 * car type management
 	 *************************************************************/
 	
 	@RequestMapping(value="/index.do")
 	public String index(Model model) {	// car type management page
 		logger.debug("---> access car type management page");
 		Condition<ModelCarType> condition = new Condition<ModelCarType>();
 		condition.setOrderList(new Order("createTime", Dir.DESC), new Order("updateTime", Dir.DESC));
 		condition.setPage(new Page(0, 20));
 		List<ModelCarType> list = iservCarType.findByCondition(condition, true);
 		model.addAttribute("list", list);
 		return basePath + "/cartype/index";
 	}
 	
 	@RequestMapping(value="/condition/get.do")
 	public String get(Model model, Condition<ModelCarType> condition, ModelCarType t){
 		try {
 			condition.setT(t);
 			condition.setOrderList(new Order("createTime", Dir.DESC), new Order("updateTime", Dir.DESC));
 			logger.debug("---> server receive: condition={}", condition);
 			List<ModelCarType> records = iservCarType.findByCondition(condition);
 //			write(ExtUtils.getJsonStore(records, records.size()), out);
 			model.addAttribute("records", records);
 		} catch (HuhuoException e) {
 			logger.warn(e.getMessage());
 			model.addAttribute("msg", new Message<String>(Status.FAILURE, e.getMessage()));
 		} catch (Exception e) {
 			logger.error(ExceptionUtils.getFullStackTrace(e));
 			model.addAttribute("msg", new Message<String>(Status.ERROR, e.getMessage()));
 		}
 		return basePath + "/cartype/grid-condition";
 	}
 	
 	@RequestMapping(value="/add-ui.do")
 	public String addUI(Model model) {
 		logger.debug("==> access add ui");
 		return basePath + "/cartype/edit-ui";
 	}
 	
 	@RequestMapping(value="/add.do")
 	public void huhuoForm(ModelCarType carType, ModelChargeStandard chargeStandard, OutputStream out){
 		try {
 			logger.debug("---> server receive: carType={}, chargeStandard={}", carType, chargeStandard);
 			// add charge standard
			chargeStandard.setStatus(1);
			chargeStandard.setCreateTime(new Date());
			chargeStandard.setUpdateTime(new Date());
 			iServChargeStandard.add(chargeStandard);
 			carType.setChargeStandardId(chargeStandard.getId());
 			// add car type
			carType.setStatus(1);
			carType.setCreateTime(new Date());
			carType.setUpdateTime(new Date());
 			iservCarType.add(carType);
 			write(new Message<ModelCarType>(Status.SUCCESS, "add new cartype success!", carType), out);
 		} catch (HuhuoException e) {
 			logger.warn(e.getMessage());
 			write(new Message<String>(Status.FAILURE, e.getMessage()), out);
 		} catch (Exception e) {
 			logger.error(ExceptionUtils.getFullStackTrace(e));
 			write(new Message<String>(Status.ERROR, e.getMessage()), out);
 		}
 	}
 }
