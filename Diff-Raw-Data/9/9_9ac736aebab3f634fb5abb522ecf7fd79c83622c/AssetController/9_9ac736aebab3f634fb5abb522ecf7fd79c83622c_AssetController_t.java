 package com.thoughtworks.is.controllers;
 
 import com.thoughtworks.services.Asset;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 @Controller
 public class AssetController {
 
     @RequestMapping("/")
     public String printHelloWorld(Model model) {
 //       model.addAttribute("message", "APURVA");
          return "redirect:/add_asset_type";
     }
     @RequestMapping("/add_asset_type")
     public String addAssetType(Model model){
         return "new";
     }
     @RequestMapping(value="/new",method = RequestMethod.POST)
     public String createAsset(Model model,@ModelAttribute("asset")Asset asset, BindingResult result)
     {
//        System.out.println(asset.getAssetType());
         model.addAttribute("assetType",asset.getAssetType());
         return "index";
     }
 
 
 
 }
