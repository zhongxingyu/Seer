 package com.miniinf.OSPManager.rest.controllers;
 
 import com.miniinf.OSPManager.data.FireTruck;
 import com.miniinf.OSPManager.data.repositories.FireTruckRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szubster
  * Date: 05.11.12
  * Time: 15:05
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/fireTruck/")
 public class FireTruckController {
     @Autowired
     private FireTruckRepository repository;
 
     @RequestMapping(value = "add", method = RequestMethod.PUT)
    private FireTruck addTruck(@RequestParam String number, @RequestParam String licensePlate) {
         FireTruck ft = new FireTruck();
         ft.setLicensePlate(licensePlate);
         ft.setNumber(number);
         ft = repository.save(ft);
         return ft;
     }
 }
