 package org.dspace.webmvc.utils;
 
 import org.dspace.core.Context;
 import org.dspace.webmvc.view.helpers.DSpaceHelper;
 import org.dspace.webmvc.view.helpers.MetadataHelper;
 import org.dspace.webmvc.view.helpers.NavigationHelper;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class DSpaceModelInterceptor extends HandlerInterceptorAdapter {
     @Override
     public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && request.getAttribute("context") != null) {
            modelAndView.addObject("dspaceHelper", new DSpaceHelper((Context)request.getAttribute("context")));
            modelAndView.addObject("navigation", new NavigationHelper());
            modelAndView.addObject("metadataHelper", new MetadataHelper());
        }

         super.postHandle(request, response, handler, modelAndView);
     }
 }
