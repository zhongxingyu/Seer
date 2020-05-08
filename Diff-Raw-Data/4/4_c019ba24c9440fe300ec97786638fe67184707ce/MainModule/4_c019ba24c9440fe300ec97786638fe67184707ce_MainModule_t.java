 /*
  * Copyright 2011 Proofpoint, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.proofpoint.event.blackhole;
 
 import com.google.inject.Binder;
 import com.google.inject.Inject;
 import com.google.inject.Module;
 import com.google.inject.Provider;
 import com.google.inject.Scopes;
 import com.proofpoint.discovery.client.ServiceAnnouncement;
 import com.proofpoint.discovery.client.ServiceAnnouncement.ServiceAnnouncementBuilder;
 import com.proofpoint.http.server.HttpServerInfo;
 
 import static com.proofpoint.configuration.ConfigurationModule.bindConfig;
 import static com.proofpoint.discovery.client.DiscoveryBinder.discoveryBinder;
 import static com.proofpoint.discovery.client.ServiceAnnouncement.serviceAnnouncement;
 import static org.weakref.jmx.guice.MBeanModule.newExporter;
import static com.proofpoint.event.client.EventBinder.eventBinder;
 
 public class MainModule
         implements Module
 {
     public void configure(Binder binder)
     {
         binder.requireExplicitBindings();
         binder.disableCircularProxies();
 
        eventBinder(binder).bindEventClient(BlackholeEvent.class);

         binder.bind(BlackholeResource.class).in(Scopes.SINGLETON);
         bindConfig(binder).to(BlackholeConfig.class);
         newExporter(binder).export(BlackholeResource.class).withGeneratedName();
         discoveryBinder(binder).bindServiceAnnouncement(BlackholeAnnouncementProvider.class);
     }
 
     private static class BlackholeAnnouncementProvider
             implements Provider<ServiceAnnouncement>
     {
         private HttpServerInfo httpServerInfo;
         private String serviceAnnouncement;
 
         @Inject
         BlackholeAnnouncementProvider(BlackholeConfig config, HttpServerInfo httpServerInfo)
         {
             serviceAnnouncement = config.getServiceAnnouncement();
             this.httpServerInfo = httpServerInfo;
         }
 
         @Override
         public ServiceAnnouncement get()
         {
             ServiceAnnouncementBuilder builder = serviceAnnouncement(serviceAnnouncement);
 
             if (httpServerInfo.getHttpUri() != null) {
                 builder.addProperty("http", httpServerInfo.getHttpUri().toString());
             }
             if (httpServerInfo.getHttpsUri() != null) {
                 builder.addProperty("https", httpServerInfo.getHttpsUri().toString());
             }
 
             return builder.build();
         }
     }
 }
