 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.web.controllers;
 
 import com.miniinf.OSPManager.data.Address;
 import com.miniinf.OSPManager.data.Operation;
 import com.miniinf.OSPManager.data.repositories.FireFighterRepository;
 import com.miniinf.OSPManager.data.repositories.FireTruckRepository;
 import com.miniinf.OSPManager.data.repositories.OperationRepository;
 import com.miniinf.OSPManager.data.services.UnitService;
 import com.miniinf.OSPManager.jasper.ReportPath;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.math.BigInteger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: asus
  * Date: 02.01.13
  * Time: 12:50
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("operation")
 @ReportPath("/operation/")
 public class OperationController extends AbstractController<OperationRepository, Operation, BigInteger> {
 
     @Autowired
     OperationRepository repository;
 
     @Autowired
     FireFighterRepository FFRepository;
 
     @Autowired
     FireTruckRepository FTRepository;
 
     @Autowired
     UnitService unitService;
 
     public OperationController() {
         super(Operation.class);
     }
 
     @Override
     protected OperationRepository getRepository() {
         return repository;
     }
 
     @Override
     @PreAuthorize("hasRole('admin')")
     @RequestMapping(value = "/create")
     public void form(Model uiModel) throws IllegalAccessException, InstantiationException {
         Operation entity = new Operation();
         entity.setNumber(unitService.getCounter());
         uiModel.addAttribute("entity", entity);
         uiModel.addAttribute("firefighters", FFRepository.findAll());
         uiModel.addAttribute("firetrucks", FTRepository.findAll());
     }
 
     @Override
     @PreAuthorize("hasRole('admin')")
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(@Valid Operation entity, BindingResult bindingResult, Model uiModel) {
         if (bindingResult.hasErrors()) {
             if (bindingResult.getFieldErrorCount("address.street") > 0) {
                 uiModel.addAttribute("information", "com.miniinf.OSPManager.simplepropertyfix");
                 Address ad = entity.getPlace();
                 ad.setStreet(StringUtils.capitalize(ad.getStreet()));
                 entity.setPlace(ad);
             }
             if (bindingResult.getFieldErrorCount("address.city") > 0) {
                 uiModel.addAttribute("information", "com.miniinf.OSPManager.simplepropertyfix");
                 Address ad = entity.getPlace();
                 ad.setCity(StringUtils.capitalize(ad.getCity()));
                 entity.setPlace(ad);
             }
             uiModel.addAttribute("firefighters", FFRepository.findAll());
            uiModel.addAttribute("firefighters", FFRepository.findAll());
         }
         unitService.setCounter(entity.getNumber() + 1);
         return super.create(entity, bindingResult, uiModel);
     }
 }
