 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.context;
 
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.springframework.stereotype.Component;
 import org.springframework.util.CollectionUtils;
 
 /**
  * @author Veniamin Isaias
 * @since 1.0.0
  */
 
 @Component
 public class ORMLauncher implements ApplicationListener<ContextRefreshedEvent> {
     @Override
     public void onApplicationEvent(ContextRefreshedEvent event) {
         if (event.getApplicationContext().getParent() == null) {
             if (ContextUtil.getSystemJoblet().isInstalled()) {
                 if (!CollectionUtils.containsInstance(ContextUtil.getActiveProfiles(), "installed")) {
                     ContextUtil.addActiveProfile("installed");
                     ContextUtil.refresh();
                 }
             }
         }
     }
 }
