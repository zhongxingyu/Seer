 package com.rockontrol.yaogan.controller;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.service.ISecurityManager;
 import com.rockontrol.yaogan.service.IYaoganService;
 
 @Controller
 @RequestMapping("admin/place")
 public class ListController {
    @Autowired
    private IYaoganService _service;
    @Autowired
    private ISecurityManager _secMgr;
 
    @RequestMapping(value = "", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(Model model) {
       User caller = _secMgr.currentUser();
       List<Shapefile> list = _service.getShapefiles(caller);
       model.addAttribute("shapefiles", list);
       return "/admin/stats/shapefileList";
    }
 
    @RequestMapping("/fileList")
    public String list(Model model, @RequestParam("place") String placeName,
          @RequestParam("shootTime") String shootTime) {
       Place place = _service.getPlaceByName(placeName);
       User caller = _secMgr.currentUser();
      List<Shapefile> list = new ArrayList<Shapefile>();
       if (place != null) {
         list = _service.getShapefiles(caller, place.getId(), shootTime);
       }
      model.addAttribute("shapefiles", list);
       return "/admin/stats/shapefileList";
 
    }
 
    public void setSecMgr(ISecurityManager secMgr) {
       this._secMgr = secMgr;
    }
 
    public void setYaoganService(IYaoganService service) {
       this._service = service;
    }
 }
