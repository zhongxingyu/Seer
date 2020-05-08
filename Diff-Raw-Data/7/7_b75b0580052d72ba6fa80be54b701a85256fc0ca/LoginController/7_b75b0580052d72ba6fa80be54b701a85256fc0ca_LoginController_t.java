 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ${package}.web.controller;
 
 /*
  * #%L
  * Web Archetype
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import com.abada.extjs.ExtjsStore;
 import com.abada.springframework.web.servlet.menu.MenuEntry;
 import com.abada.springframework.web.servlet.menu.MenuService;
 import java.util.Arrays;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mobile.device.Device;
 import org.springframework.security.authentication.AbstractAuthenticationToken;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author katsu
  */
 @Controller
 public class LoginController {
 
     private static final Log logger = LogFactory.getLog(LoginController.class);
     @Autowired
     private MenuService menuService;
 
     @RequestMapping(value = {"/login.htm"}, method = RequestMethod.GET)
     public String getLogin(HttpServletRequest request, Model model, Device device) {
         request.getSession().invalidate();
         if (device.isMobile() || device.isTablet()) {            
             model.addAttribute("isDesktop", false);
             model.addAttribute("js", Arrays.asList("js_m/login.js"));
             return "dynamic/login";
         }else{
             model.addAttribute("isDesktop", device.isNormal());
             model.addAttribute("js", Arrays.asList("js/login.js"));
             return "dynamic/login";
         }
     }
 
     @RequestMapping(value = "/exit.htm", method = RequestMethod.GET)
     @MenuEntry(icon = "images/logout.png", menuGroup = "Salir", order = 0, text = "Salir",devices = {com.abada.springframework.web.servlet.menu.Device.DESKTOP, com.abada.springframework.web.servlet.menu.Device.MOBILE, com.abada.springframework.web.servlet.menu.Device.TABLET})
     public String getExit(HttpServletRequest request, Model model) {
         model.addAttribute("js", Arrays.asList("js/exit.js"));
         return "dynamic/login";
     }
 
     @RequestMapping(value = "/error.htm", method = RequestMethod.GET)
     public String getError(HttpServletRequest request, Model model) {
         model.addAttribute("js", Arrays.asList("js/error.js"));
         return "dynamic/login";
     }
 
     @RequestMapping(value = "/main.htm", method = RequestMethod.GET)
     public String getMain(HttpServletRequest request, Model model, Device device) {
         if (device.isMobile() || device.isTablet()) {          
             model.addAttribute("isDesktop", false);
             model.addAttribute("js", Arrays.asList("js_m/menu.js"));
             return "dynamic/main";
         }else{
             model.addAttribute("isDesktop", device.isNormal());
             return "dynamic/main";
         }
     }
 
     @RequestMapping(value = "/mainmenu.do")
     public ExtjsStore getMenu(HttpServletRequest request, Device device,Locale locale) {
         String[] roles = null;
         if (request.getUserPrincipal() instanceof AbstractAuthenticationToken) {
             AbstractAuthenticationToken user = (AbstractAuthenticationToken) request.getUserPrincipal();
             roles = new String[user.getAuthorities().size()];
             GrantedAuthority[] ga = user.getAuthorities().toArray(new GrantedAuthority[0]);
             for (int i = 0; i < user.getAuthorities().size(); i++) {
                 roles[i] = ga[i].getAuthority();
             }
         }
         ExtjsStore result = new ExtjsStore();
         com.abada.springframework.web.servlet.menu.Device deviceAux;
         if (device.isMobile())
             deviceAux=com.abada.springframework.web.servlet.menu.Device.MOBILE;
         else if (device.isNormal())
             deviceAux=com.abada.springframework.web.servlet.menu.Device.DESKTOP;
         else
             deviceAux=com.abada.springframework.web.servlet.menu.Device.TABLET;
         result.setData(this.menuService.getMenus(request.getContextPath(),deviceAux,locale, roles));
         return result;
     }
 }
