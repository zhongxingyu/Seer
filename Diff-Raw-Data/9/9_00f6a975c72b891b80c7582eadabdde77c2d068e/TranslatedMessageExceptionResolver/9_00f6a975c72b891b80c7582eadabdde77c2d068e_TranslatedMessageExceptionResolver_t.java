 /**
  * ***************************************************************************
  * Copyright (c) 2010 Qcadoo Limited
  * Project: Qcadoo MES
  * Version: 0.1
  *
  * This file is part of Qcadoo.
  *
  * Qcadoo is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  * ***************************************************************************
  */
 
 package com.qcadoo.mes.application;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 
 import com.qcadoo.mes.controller.ErrorController;
 
 public final class TranslatedMessageExceptionResolver extends SimpleMappingExceptionResolver {
 
     private static final Logger LOG = LoggerFactory.getLogger(TranslatedMessageExceptionResolver.class);
 
     private final Map<String, String> translations = new HashMap<String, String>();
 
     @Autowired
     private ErrorController errorController;
 
     @PostConstruct
     public void init() {
         translations.put("Trying delete entity in use", "illegalStateException.objectInUse");
         translations.put("Entity.+ cannot be found", "illegalStateException.entityNotFound");
        translations.put("PrintError:DocumentNotGenerated", "illegalStateException.printErrorDocumentNotGenerated");
     }
 
     @Override
     protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
             final Object handler, final Exception ex) {
         ModelAndView mv = super.doResolveException(request, response, handler, ex);
         if (mv != null) {
 
             String codeStr = mv.getViewName();
             int code = Integer.parseInt(codeStr);
 
             String exceptionMessage = ex.getMessage();
             String predefinedExceptionMessage = null;
             for (Map.Entry<String, String> translation : translations.entrySet()) {
                 if (exceptionMessage.matches(translation.getKey())) {
                     predefinedExceptionMessage = translation.getValue();
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Adding exception message to view: " + predefinedExceptionMessage);
                     }
                     break;
                 }
             }
 
             return errorController.getAccessDeniedPageView(code, ex, predefinedExceptionMessage,
                     retrieveLocaleFromRequestCookie(request));
 
         }
         return mv;
     }
 
     private Locale retrieveLocaleFromRequestCookie(final HttpServletRequest request) {
         Locale locale = request.getLocale();
         Cookie cookies[] = request.getCookies();
         if ((cookies != null) && (cookies.length > 0)) {
             for (int i = 0; i < cookies.length; i++) {
                 Cookie c = cookies[i];
                 if ("clientLanguage".equals(c.getName())) {
                     locale = new Locale(c.getValue(), "");
                     break;
                 }
             }
         }
         return locale;
     }
 }
