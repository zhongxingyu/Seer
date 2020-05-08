 // Copyright (C) 2013 The Android Open Source Project
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.googlesource.gerrit.plugins.gitiles;
 
 import com.google.common.collect.Lists;
 import com.google.gerrit.server.CurrentUser;
 import com.google.gerrit.server.IdentifiedUser;
 import com.google.gitiles.BaseServlet;
 import com.google.gitiles.GitilesUrls;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 class MenuFilter implements Filter {
   private final Provider<CurrentUser> userProvider;
   private final GitilesUrls urls;
 
   @Inject
   MenuFilter(Provider<CurrentUser> userProvider, GitilesUrls urls) {
     this.userProvider = userProvider;
     this.urls = urls;
   }
 
   @Override
   public void doFilter(ServletRequest request, ServletResponse response,
       FilterChain chain) throws IOException, ServletException {
     HttpServletRequest req = (HttpServletRequest) request;
     CurrentUser user = userProvider.get();
     List<Object> entries = Lists.newArrayListWithCapacity(2);
     if (user instanceof IdentifiedUser) {
       entries
           .add(BaseServlet.menuEntry(((IdentifiedUser) user).getName(), null));
       entries.add(BaseServlet.menuEntry("Sign Out", urls.getBaseGerritUrl(req)
          + "logout"));
     } else {
       entries.add(BaseServlet.menuEntry("Sign In", urls.getBaseGerritUrl(req)
          + "login"));
     }
     BaseServlet.putSoyData(req, "menuEntries", entries);
     chain.doFilter(request, response);
   }
 
   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
   }
 
   @Override
   public void destroy() {
   }
 }
