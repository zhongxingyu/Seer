 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.solairis.yourcarslife.controller;
 
 import com.solairis.yourcarslife.data.domain.FuelLog;
 import com.solairis.yourcarslife.service.LogService;
 import com.solairis.yourcarslife.service.VehicleService;
 import java.beans.PropertyEditor;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 /**
  *
  * @author josh
  */
 @Controller
 public class FuelLogController {
 
 	@Autowired
 	private LogService logService;
 	@Autowired
 	private VehicleService vehicleService;
 	@Autowired
 	private org.springframework.validation.Validator fuelLogFormDataValidator;
 	@Autowired
 	private PropertyEditor customDateEditor;
 
 	@InitBinder
 	protected void initBinder(WebDataBinder binder) {
 //		binder.setValidator(this.fuelLogFormDataValidator);
 	}
 
 	@RequestMapping(value= "/api/vehicle/{vehicleId}/log/fuel", method= RequestMethod.GET)
 	@Transactional
 	@ResponseBody
 	public List<FuelLog> list(@PathVariable("vehicleId") long vehicleId, @RequestParam(value="page", defaultValue="1") int page, @RequestParam(value="numResults", defaultValue="20") int numResults) {
 		return this.logService.getFuelLogsForVehicle(vehicleId, page, numResults > 1000 ? 1000 : numResults);
 	}
 
 	@RequestMapping(value="/api/vehicle/{vehicleId}/log/fuel/{logId}", method= RequestMethod.GET)
 	@Transactional
 	@ResponseBody
 	public FuelLog get(@PathVariable("vehicleId") long vehicleId, @PathVariable("logId") long logId) {
 		return this.logService.getFuelLog(logId);
 	}
 
 	@RequestMapping(value="/api/vehicle/{vehicleId}/log/fuel", method= RequestMethod.POST)
 	@Transactional
 	@ResponseBody
 	public void save(@PathVariable("vehicleId") long vehicleId, @RequestBody FuelLog fuelLog) {
 		fuelLog.setVehicle(this.vehicleService.getVehicle(vehicleId));
 		this.logService.save(fuelLog);
 	}
 
 	@RequestMapping(value = "/api/vehicle/{vehicleId}/log/fuel/{logId}", method = RequestMethod.PUT)
 	@Transactional
 	@ResponseBody
 	public void put(@PathVariable("vehicleId") long vehicleId, @PathVariable("logId") Long logId, @RequestBody FuelLog fuelLog) {
 		if (logId != fuelLog.getLogId()) {
 			throw new IllegalArgumentException("Log ID of "+logId+" passed on URL does not match the id "+fuelLog.getLogId()+ "passed in the body");
 		}
 		this.logService.save(fuelLog);
 	}
 }
