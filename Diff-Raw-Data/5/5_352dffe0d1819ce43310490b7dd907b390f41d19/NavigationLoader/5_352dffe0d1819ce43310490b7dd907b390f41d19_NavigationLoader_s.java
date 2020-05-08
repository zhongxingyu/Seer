 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.m4us.controller;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author arka
  */
 public class NavigationLoader {
     public static ResourceBundle navigationRules = null;
     private static NavigationRule previousRule;
     public static void loadNavigationRules(){
         try {
            navigationRules = new PropertyResourceBundle(new FileReader("/home/arka/NetBeansProjects/movies4us/navigation.properties"));
         } catch (IOException ex) {
             Logger.getLogger(NavigationLoader.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static NavigationRule getRuleDetails(String ruleName){
         if(navigationRules==null)
             loadNavigationRules();
         String[] handlers = navigationRules.getString(ruleName).split(",");
         String successJSP = navigationRules.getString(ruleName+".successJSP");
         successJSP = (successJSP.equalsIgnoreCase("CURRENT"))?previousRule.ruleSuccessJSP:successJSP;
         String errorJSP = navigationRules.getString(ruleName+".errorJSP");
         List<String> handlerList = Arrays.asList(handlers);
         previousRule =  new NavigationRule(ruleName, successJSP, errorJSP, handlerList);
         return previousRule;
     }
 }
