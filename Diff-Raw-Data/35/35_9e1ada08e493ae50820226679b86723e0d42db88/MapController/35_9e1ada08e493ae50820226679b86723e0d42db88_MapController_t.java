 package com.gooki.webapp.controller;
 
 import com.gooki.model.Cong;
 import com.gooki.model.User;
 import com.gooki.service.CongManager;
 import com.gooki.service.SecurityContextService;
 import com.gooki.webapp.exception.BlockExistsException;
 import com.gooki.webapp.exception.CongNotExistsException;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.gooki.webapp.constant.TerritoryConstants;
 import com.gooki.service.BlockManager;
 import sun.rmi.runtime.Log;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 public class MapController extends TerritoryBaseController {
     @Autowired
     private CongManager congManager;
 
     @Autowired
     private SecurityContextService securityContextService;
 
     @RequestMapping(value = "/map", method = RequestMethod.GET)
     public ModelAndView handleDefaultRequest() throws Exception {
         Model model = new ExtendedModelMap();
        model.addAttribute(TerritoryConstants.CONGREGATION_LIST.getValue(), congManager.getAllUniqueCongs());
 
         return new ModelAndView("mapMain", model.asMap());
     }
 
     @RequestMapping(value = "/map-create", method = RequestMethod.GET)
     public ModelAndView handleCreateMapRequest() throws Exception {
         Model model = new ExtendedModelMap();
        model.addAttribute(TerritoryConstants.CONGREGATION_LIST.getValue(), congManager.getAllUniqueCongs());
 
         return new ModelAndView("createMapStep1", model.asMap());
     }
 
     @RequestMapping(value = "/map-create/{congName}", method = RequestMethod.GET)
     public ModelAndView handleCreateMap2Request(@PathVariable String congName) throws Exception {
         Model model = new ExtendedModelMap();
         model.addAttribute(TerritoryConstants.CONGREGATION.getValue(), congName);
 
         return new ModelAndView("createMapStep2", model.asMap());
     }
 
     @RequestMapping(value = "/map", method = RequestMethod.POST)
     public @ResponseBody Map<String, Object> handleCreateCongRequest(Cong cong) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         try {
             User currentUser = (User) securityContextService.getAuthentication().getPrincipal();
             cong.getUsers().add(currentUser);
             congManager.save(cong);
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
             map.put("message", "The map has been successfully created.");
         } catch (Exception e) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. Something went wrong and could not save the block info. Please try later.");
         }
 
         return map;
     }
 
     @RequestMapping(value = "/map/{congName}", method = RequestMethod.GET)
     public ModelAndView handleViewTerritoryMapRequest(@PathVariable String congName) throws Exception {
         Model model = new ExtendedModelMap();
         Cong cong = congManager.findByCongName(congName);
 
         if (cong == null) {
             throw new CongNotExistsException(congName);
         }
 
         model.addAttribute(TerritoryConstants.CONGREGATION.getValue(), cong);
 
         return new ModelAndView("blockMain", model.asMap());
     }
 
     @RequestMapping(value = "/map/isExists/{congName}", method = RequestMethod.GET)
     public @ResponseBody Map<String, Object> handleCheckIfExistsRequest(@PathVariable String congName) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         if (congManager.findByCongName(congName) == null) {
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
             map.put("message", "Success");
         } else {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Fail");
         }
 
         return map;
     }
 
 
 }
