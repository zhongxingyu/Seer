 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icefaces.ace.component.themeselect;
 
 import org.icefaces.ace.util.Constants;
 
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ThemeSelect extends ThemeSelectBase {
 
     public Collection<String> getThemeList(FacesContext context) throws IOException {
         Map<String, Object> appMap = context.getExternalContext().getApplicationMap();
         String THEME_LIST = Constants.THEME_PARAM + ".list";
         Collection<String> themeList = (Collection<String>) appMap.get(THEME_LIST);
         if (themeList != null) {
             return themeList;
         }
         themeList = new ArrayList<String>();
 
         ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
         Resource resource;
         for (String theme : new String[]{"rime", "sam"}) {
             resource = resourceHandler.createResource("themes/" + theme + "/theme.css", "icefaces.ace");
             if (resource != null) {
                 themeList.add(theme);
             }
         }
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         Enumeration<URL> urls = classLoader.getResources("META-INF/resources");
         URL url;
        Matcher matcher = Pattern.compile("((jar)|(vfs)):.*/WEB-INF/lib/(.+)\\.jar!?/META-INF/resources/?").matcher("");
         String theme;
         while (urls.hasMoreElements()) {
             url = urls.nextElement();
             if (matcher.reset(url.toString()).matches()) {
                theme = matcher.group(4);
                 url = classLoader.getResource("META-INF/resources/ace-" + theme);
                 if (url != null) {
                     resource = resourceHandler.createResource("theme.css", "ace-" + theme);
                     if (resource != null) {
                         themeList.add(theme);
                     }
                 }
             }
         }
         appMap.put(THEME_LIST, themeList);
         return themeList;
     }
 
 
     public String getSelectedTheme(FacesContext context) {
         ExternalContext externalContext = context.getExternalContext();
         String defaultTheme = externalContext.getInitParameter(Constants.THEME_PARAM);
 
         String theme = (String) this.getValue();
         if (theme == null) {
             theme = null == defaultTheme ? "sam" : defaultTheme;
         }
 
         return theme;
     }
 }
