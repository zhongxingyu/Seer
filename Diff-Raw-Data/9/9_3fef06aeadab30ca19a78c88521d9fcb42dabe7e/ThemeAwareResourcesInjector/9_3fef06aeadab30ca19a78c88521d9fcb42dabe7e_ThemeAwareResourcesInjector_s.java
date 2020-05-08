 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.codehaus.griffon.runtime.theme;
 
 import griffon.core.GriffonApplication;
 import griffon.core.resources.NoSuchResourceException;
 import griffon.core.resources.ResourceResolver;
 import griffon.core.resources.ResourcesInjector;
 import griffon.plugins.theme.ThemeAware;
 import griffon.plugins.theme.ThemeManager;
 import griffon.plugins.theme.ThemeManagerHolder;
 import griffon.util.GriffonNameUtils;
 import griffon.util.RunnableWithArgs;
 import groovy.lang.MissingMethodException;
 import org.codehaus.griffon.runtime.core.resources.AbstractResourcesInjector;
 import org.codehaus.griffon.runtime.core.resources.DefaultResourceResolver;
 import org.codehaus.griffon.runtime.core.resources.DefaultResourcesInjector;
 import org.codehaus.groovy.runtime.InvokerHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.util.*;
 
 import static griffon.util.ConfigUtils.getConfigValue;
 
 /**
  * @author Andres Almiray
  */
 public class ThemeAwareResourcesInjector extends AbstractResourcesInjector {
     private static final Logger LOG = LoggerFactory.getLogger(ThemeAwareResourcesInjector.class);
     private final Map<String, ResourceResolver> themeResolvers = new LinkedHashMap<String, ResourceResolver>();
     private final InstanceStore instanceStore = new InstanceStore();
     private final ResourcesInjector defaultResourcesInjector;
 
     public ThemeAwareResourcesInjector(GriffonApplication app) {
         super(app);
 
         defaultResourcesInjector = new DefaultResourcesInjector(app);
 
         List<String> basenames = (List<String>) getConfigValue(app.getConfig(), "themes.basenames", null);
         if (basenames == null || basenames.isEmpty()) {
             throw new IllegalArgumentException("Must define a config value for 'themes.basenames'");
         }
 
         for (String basename : basenames) {
             themeResolvers.put(basename, new DefaultResourceResolver(basename));
         }
 
         final ThemeManager themeManager = new ThemeManager(app, themeResolvers.keySet());
         ThemeManagerHolder.setThemeManager(themeManager);
         themeManager.setCurrentTheme(basenames.get(0));
         themeManager.addPropertyChangeListener(ThemeManager.CURRENT_THEME_PROP, new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent event) {
                 LOG.info("Theme changed to " + event.getNewValue());
                 for (Object instance : instanceStore) {
                     injectResources(instance);
                 }
             }
         });
 
         app.addApplicationEventListener(GriffonApplication.Event.DESTROY_INSTANCE.getName(), new RunnableWithArgs() {
             @Override
             public void run(Object[] args) {
                 Object instance = args[2];
                 if (instanceStore.contains(instance)) {
                     instanceStore.remove(instance);
                 }
             }
         });
 
         app.addPropertyChangeListener("locale", new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                 for (Object instance : instanceStore) {
                     injectResources(instance);
                 }
             }
         });
     }
 
     @Override
     public void injectResources(Object instance) {
         if (null == instance) return;
         if (instance.getClass().getAnnotation(ThemeAware.class) == null) {
             defaultResourcesInjector.injectResources(instance);
         } else {
             super.injectResources(instance);
             if (!instanceStore.contains(instance)) {
                 instanceStore.add(instance);
             }
         }
     }
 
     @Override
     protected Object resolveResource(String key, String[] args) {
         try {
             return fetchResourceResolver().resolveResource(key, args, getApp().getLocale());
         } catch (NoSuchResourceException nsre) {
             return getApp().resolveResource(key, args, getApp().getLocale());
         }
     }
 
     @Override
     protected Object resolveResource(String key, String[] args, String defaultValue) {
         return fetchResourceResolver().resolveResource(key, args, defaultValue, getApp().getLocale());
     }
 
     private ResourceResolver fetchResourceResolver() {
         String currentTheme = ThemeManagerHolder.getThemeManager().getCurrentTheme();
         return themeResolvers.get(currentTheme);
     }
 
     @Override
     protected void setFieldValue(Object instance, Field field, Object value, String fqFieldName) {
         String setter = GriffonNameUtils.getSetterName(field.getName());
         try {
             InvokerHelper.invokeMethod(instance, setter, value);
         } catch (MissingMethodException mme) {
             super.setFieldValue(instance, field, value, fqFieldName);
         }
     }
 
     private static class InstanceStore implements Iterable {
         private final List<WeakReference<Object>> instances = new LinkedList<WeakReference<Object>>();
 
         private void add(Object instance) {
             if (null == instance) return;
             instances.add(new WeakReference<Object>(instance));
         }
 
         private void remove(Object instance) {
             if (null == instance) return;
             for (Iterator<WeakReference<Object>> it = instances.iterator(); it.hasNext(); ) {
                Object candidate = it.next().get();
                 if (instance.equals(candidate)) {
                    it.remove();
                     break;
                 }
             }
         }
 
         private boolean contains(Object instance) {
             if (null == instance) return false;
             for (Iterator<WeakReference<Object>> it = instances.iterator(); it.hasNext(); ) {
                 Object candidate = it.next().get();
                 if (instance.equals(candidate)) {
                     return true;
                 }
             }
             return false;
         }
 
         public Iterator<Object> iterator() {
             final Iterator<WeakReference<Object>> it = instances.iterator();
             return new Iterator<Object>() {
                 public boolean hasNext() {
                     return it.hasNext();
                 }
 
                 public Object next() {
                     return it.next().get();
                 }
 
                 public void remove() {
                     it.remove();
                 }
             };
         }
     }
 }
