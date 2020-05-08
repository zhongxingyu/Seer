 /**
  * Copyright 2010 Marko Lavikainen
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
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.contextfw.web.application;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Pattern;
 
 import net.contextfw.web.application.component.Component;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.internal.WebApplicationServletModule;
 import net.contextfw.web.application.internal.component.AutoRegisterListener;
 import net.contextfw.web.application.internal.configuration.KeyValue;
 import net.contextfw.web.application.internal.page.PageScope;
 import net.contextfw.web.application.internal.service.DirectoryWatcher;
 import net.contextfw.web.application.internal.util.AttributeHandler;
 import net.contextfw.web.application.internal.util.ObjectAttributeSerializer;
 import net.contextfw.web.application.lifecycle.LifecycleListener;
 import net.contextfw.web.application.lifecycle.PageFlowFilter;
 import net.contextfw.web.application.lifecycle.PageScoped;
 import net.contextfw.web.application.lifecycle.RequestInvocationFilter;
 import net.contextfw.web.application.serialize.AttributeJsonSerializer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonSerializer;
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.TypeLiteral;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.spi.TypeEncounter;
 import com.google.inject.spi.TypeListener;
 
 public final class WebApplicationModule extends AbstractModule {
 
     private final Configuration configuration;
     
     private PageScope pageScope;
     
     @Inject
     private PageFlowFilter pageFlowFilter;
 
     private Logger logger = LoggerFactory.getLogger(WebApplicationModule.class);
 
     @SuppressWarnings("rawtypes")
     private AutoRegisterListener autoRegisterListener 
             = new AutoRegisterListener();
 
     public WebApplicationModule(Configuration configuration) {
         this.configuration = configuration;
     }
 
     @Override
     protected void configure() {
         pageScope = new PageScope();
         bindScope(PageScoped.class, pageScope);
         bind(PageScope.class).toInstance(pageScope);
         bind(HttpContext.class).toProvider(pageScope.scope(Key.get(HttpContext.class), null));
         bind(WebApplicationHandle.class).toProvider(pageScope.scope(Key.get(WebApplicationHandle.class), null));
         bind(ObjectAttributeSerializer.class).to(AttributeHandler.class);
         bind(Configuration.class).toInstance(configuration);
         bind(PropertyProvider.class).toInstance(configuration.get(Configuration.PROPERTY_PROVIDER));
         bind(RequestInvocationFilter.class).toInstance(configuration.get(Configuration.REQUEST_INVOCATION_FILTER));        
         handlePageFlowFilter();
         handleLifecycleListener();
         this.bindListener(Matchers.any(), new TypeListener() {
             @SuppressWarnings("unchecked")
             @Override
             public <I> void hear(TypeLiteral<I> typeLiteral,
                     TypeEncounter<I> typeEncounter) {
                 if (Component.class.isAssignableFrom(typeLiteral
                         .getRawType())) {
                     typeEncounter.register(autoRegisterListener);
                 }
             }
         });
 
         WebApplicationServletModule servletModule =
                 new WebApplicationServletModule(configuration,
                         configuration.get(Configuration.PROPERTY_PROVIDER));
 
         install(servletModule);
         requestInjection(this);
         requestInjection(autoRegisterListener);
     }
 
     @SuppressWarnings("unchecked")
     private void handlePageFlowFilter() {
         Object obj = configuration.get(Configuration.PAGEFLOW_FILTER);
         if (obj instanceof PageFlowFilter) {
             bind(PageFlowFilter.class).toInstance((PageFlowFilter) obj);
         } else {
             bind(PageFlowFilter.class).to((Class<PageFlowFilter>) obj);
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     private void handleLifecycleListener() {
         Object obj = configuration.get(Configuration.LIFECYCLE_LISTENER);
         if (obj instanceof LifecycleListener) {
             bind(LifecycleListener.class).toInstance((LifecycleListener) obj);
         } else {
             bind(LifecycleListener.class).to((Class<LifecycleListener>) obj);
         }
     }
 
     @Singleton
     @Provides
     public Gson provideGson(Injector injector) {
 
         GsonBuilder builder = new GsonBuilder();
 
         for (KeyValue<Class<?>, Class<? extends JsonSerializer<?>>> entry : configuration
                 .get(Configuration.JSON_SERIALIZER)) {
             builder.registerTypeAdapter(entry.getKey(), injector.getInstance(entry.getValue()));
         }
 
         for (KeyValue<Class<?>, Class<? extends JsonDeserializer<?>>> entry : configuration
                 .get(Configuration.JSON_DESERIALIZER)) {
             builder.registerTypeAdapter(entry.getKey(), injector.getInstance(entry.getValue()));
         }
 
         for (KeyValue<Class<?>, Class<? extends AttributeJsonSerializer<?>>> entry : configuration
                 .get(Configuration.ATTRIBUTE_JSON_SERIALIZER)) {
             builder.registerTypeAdapter(entry.getKey(), injector.getInstance(entry.getValue()));
         }
 
         return builder.create();
     }
 
     public void startExpiredPagesRemoval() {
         Timer timer = new Timer(true);
         logger.info("Starting scheduled removal for expired web applications");
 
         timer.schedule(new TimerTask() {
             public void run() {
                 pageScope.removeExpiredPages(pageFlowFilter);
             }
         }, configuration.get(Configuration.REMOVAL_SCHEDULE_PERIOD),
                 configuration.get(Configuration.REMOVAL_SCHEDULE_PERIOD));
     }
 
     @Provides
     @Singleton
     public DirectoryWatcher resourceDirectoryWatcher() {
         List<String> paths = null;
         if (configuration.get(Configuration.DEVELOPMENT_MODE)) {
             paths = new ArrayList<String>();
             paths.addAll(configuration.get(Configuration.RESOURCE_PATH));
         }
         
        Pattern matcher = Pattern.compile(".+\\.(xsl|css|js|properties)", Pattern.CASE_INSENSITIVE);
         
         if (!configuration.get(Configuration.CLASS_RELOADING_ENABLED)) {
            matcher = Pattern.compile(".+\\.(xsl|css|js|class|properties)", Pattern.CASE_INSENSITIVE);
         }
         return new DirectoryWatcher(paths, matcher); 
     }
 }
