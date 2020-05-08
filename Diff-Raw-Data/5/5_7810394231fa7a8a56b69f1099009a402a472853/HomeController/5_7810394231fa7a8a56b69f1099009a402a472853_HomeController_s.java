 package com.visma.autosysmonitor.controller;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.visma.autosysmonitor.da.MonitorUpdater;
 import com.visma.autosysmonitor.domain.Monitor;
 import com.visma.autosysmonitor.domain.MonitorDTO;
 
 @Controller
 public class HomeController {
 
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 
 	@Autowired
 	private MonitorUpdater repo;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		repo.clear();
 		repo.readFromFile();
 		return "home";
 	}
 
 	@RequestMapping(value = "/pingSystem", method = RequestMethod.POST)
 	public @ResponseBody
 	MonitorDTO pingSystem(@RequestBody MonitorDTO system) {
 		logger.info("Getting system: " + system.getName());
		return repo.updateSystem(system).toMonitorDTO();
 	}
 
 	@RequestMapping(value = "/allSystems", method = RequestMethod.GET)
 	public @ResponseBody
 	MonitorDTO[] allSystems() {
 		List<Monitor> ret = repo.getAll();
 		MonitorDTO[] systems = new MonitorDTO[ret.size()];
 		for (int i = 0; i < ret.size(); ++i) {
 			systems[i] = ret.get(i).toMonitorDTO();
 		}
 
 		return systems;
 	}
 
 }
