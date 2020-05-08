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
 
 package net.contextfw.web.application.configuration;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.contextfw.web.application.DocumentProcessor;
 import net.contextfw.web.application.PropertyProvider;
 import net.contextfw.web.application.SystemPropertyProvider;
 import net.contextfw.web.application.internal.configuration.BasicSettableProperty;
 import net.contextfw.web.application.internal.configuration.BindablePropertyImpl;
 import net.contextfw.web.application.internal.configuration.KeyValue;
 import net.contextfw.web.application.internal.configuration.Property;
 import net.contextfw.web.application.internal.configuration.ReloadableClassPropertyImpl;
 import net.contextfw.web.application.internal.configuration.SelfKeyValueSetPropertyImpl;
 import net.contextfw.web.application.internal.configuration.SelfSettableProperty;
 import net.contextfw.web.application.internal.configuration.StringSetPropertyImpl;
 import net.contextfw.web.application.internal.configuration.TemporalPropertyImpl;
 import net.contextfw.web.application.lifecycle.DefaultLifecycleListener;
 import net.contextfw.web.application.lifecycle.DefaultRequestInvocationFilter;
 import net.contextfw.web.application.lifecycle.LifecycleListener;
 import net.contextfw.web.application.lifecycle.RequestInvocationFilter;
 import net.contextfw.web.application.scope.DefaultWebApplicationStorage;
 import net.contextfw.web.application.scope.WebApplicationStorage;
 import net.contextfw.web.application.serialize.AttributeJsonSerializer;
 import net.contextfw.web.application.serialize.AttributeSerializer;
 import net.contextfw.web.application.util.DefaultXMLResponseLogger;
 import net.contextfw.web.application.util.XMLResponseLogger;
 
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonSerializer;
 
 /**
  * This class defines the global system properties which are set during initialization.
  * 
  * <h3>Note</h3>
  * 
  * <p>
  *  This class is immutable thus modifying or adding properties returns always a new instance
  *  of <code>Configuration</code>.
  * </p>
  *
  */
 public class Configuration {
     
     private static final String KEY_NAMESPACE = "contextfw.namespace";
     
 //    private static final String KEY_CREATE_HTTP_HEADER = "contextfw.createHttpHeader";
     
 //    private static final String KEY_UPDATE_HTTP_HEADER = "contextfw.updateHttpHeader";
 
     private static final String KEY_ATTRIBUTE_SERIALIZER = "contextfw.attributeSerializer";
 
     private static final String KEY_JSON_SERIALIZER = "contextfw.jsonSerializer";
 
     private static final String KEY_JSON_DESERIALIZER = "contextfw.jsonDeserializer";
 
     private static final String KEY_ATTRIBUTE_JSON_SERIALIZER = "contextfw.attributeJsonSerializer";
 
     private static final String KEY_REMOVAL_SCHEDULE_PERIOD = "contextfw.removalSchedulePeriod";
 
    // private static final String KEY_POLL_TIME = "contextfw.pollTime";
 
     private static final String KEY_MAX_INACTIVITY = "contextfw.maxInactivity";
 
     private static final String KEY_INITIAL_MAX_INACTIVITY = "contextfw.initialMaxInactivity";
 
   //  private static final String KEY_ERROR_TIME = "contextfw.errorTime";
 
     private static final String KEY_VIEW_COMPONENT_ROOT_PACKAGE = "contextfw.viewComponentRootPackage";
 
     private static final String KEY_RESOURCE_PATH = "contextfw.resourcePath";
 
     private static final String KEY_LIFECYCLE_LISTENER = "contextfw.lifecycleListener";
     
     private static final String KEY_REQUEST_INVOCATION_FILTER = "contextfw.requestInvocationFilter";
     
     private static final String KEY_PROPERTY_PROVIDER = "contextfw.propertyProvider";
     
     private static final String KEY_XSL_POST_PROCESSOR = "contextfw.xslPostProcessor";
 
     private static final String KEY_XML_PARAM_NAME = "contextfw.xmlParamName";
 
     //private static final String KEY_CONTEXT_PATH = "contextfw.contextPath";
 
     private static final String KEY_RESOURCES_PREFIX = "contextfw.resourcesPrefix";
 
     private static final String KEY_LOG_XML = "contextfw.logXML";
     
     private static final String KEY_XML_RESPONSE_LOGGER = "contextfw.xmlResponseLogger";
     
     private static final String KEY_RELOADABLE_ROOT_PACKAGE = "contextfw.reloadableRootPackage";
 
     private static final String KEY_DEVELOPMENT_MODE = "contextfw.developmentMode";
     
     private static final String KEY_CLASS_RELOADING_ENABLED = "contextfw.classReloadingEnabled";
     
     private static final String KEY_WEB_APPLICATION_STORAGE = "contextfw.webApplicationStorage";
 
     /**
      * Creates the default configuration.
      * 
      * <p>
      *  This is the recommended way to initialize properties.
      * </p>
      */
     public static Configuration getDefaults() {
         return new Configuration()
           .set(DEVELOPMENT_MODE, true)
           .set(CLASS_RELOADING_ENABLED, true)
           .set(LOG_XML, true)
           .set(RESOURCES_PREFIX, "/resources")
           //.set(CONTEXT_PATH, "")
           .set(XML_PARAM_NAME, null)
           .set(XML_RESPONSE_LOGGER.asInstance(new DefaultXMLResponseLogger()))
           .set(PROPERTY_PROVIDER, new SystemPropertyProvider())
           .set(REQUEST_INVOCATION_FILTER, new DefaultRequestInvocationFilter())
           .set(LIFECYCLE_LISTENER.as(DefaultLifecycleListener.class))
           .set(WEB_APPLICATION_STORAGE.as(DefaultWebApplicationStorage.class))
           .set(RESOURCE_PATH, new HashSet<String>())
           .set(VIEW_COMPONENT_ROOT_PACKAGE, new HashSet<String>())
           // .set(ERROR_TIME.inMinsAndSecs(1, 30))
           .set(INITIAL_MAX_INACTIVITY.inSeconds(30))
           //.set(POLL_TIME.inSeconds(70))
           .set(REMOVAL_SCHEDULE_PERIOD.inMinutes(1))
           .set(MAX_INACTIVITY.inMinutes(2))
           .set(NAMESPACE, new HashSet<KeyValue<String, String>>())
           .set(ATTRIBUTE_JSON_SERIALIZER, new HashSet<KeyValue<Class<?>, 
                      Class<? extends AttributeJsonSerializer<?>>>>())
            .set(JSON_SERIALIZER, new HashSet<KeyValue<Class<?>, 
                      Class<? extends JsonSerializer<?>>>>())
           .set(JSON_DESERIALIZER, new HashSet<KeyValue<Class<?>, 
                      Class<? extends JsonDeserializer<?>>>>())
           .set(ATTRIBUTE_SERIALIZER, new HashSet<KeyValue<Class<?>, 
                      Class<? extends AttributeSerializer<?>>>>());
     }
     
     /**
      * Defines whether system is run in development mode or not. 
      * 
      * <p>
      * In development mode resource changes are actively tracked during each page load or update.
      * </p>
      * <p>
      *  Default: <code>true</code>
      * </p>
      */
     public static final SettableProperty<Boolean> DEVELOPMENT_MODE = 
         createProperty(Boolean.class, KEY_DEVELOPMENT_MODE);
     
     /**
      * Defines whether page components should be reloaded when changed.
      * 
      * <p>
      *  It reloading is enabled page components are loaded through different
      *  class loader and when changes are made that class loader it disposed
      *  and new one is created.
      * </p>
      * 
      * <p>
      *  Note, class reloading works only for those classes which are not in 
      *  <code>Singleton</code>-scope. Also, this setting has effect only
      *  on development mode.
      * </p>
      * 
      * <p>
      *  Default: <code>true</code>
      * </p>
      */
     public static final SettableProperty<Boolean> CLASS_RELOADING_ENABLED = 
         createProperty(Boolean.class, KEY_CLASS_RELOADING_ENABLED);
     
     /**
      * Defines whether the XML-representation of page load or update are logged. Only suitable
      * during development mode.
      * 
      * <p>
      *  Default: <code>true</code>
      * </p>
      */
     public static final SettableProperty<Boolean> LOG_XML = 
         createProperty(Boolean.class, KEY_LOG_XML);
     
     /**
      * Defines the prefix for javascript- and css-files that are loaded with each page.
      * 
      * <p>
      *  Default: <code>/resources</code>
      * </p>
      */
     public static final SettableProperty<String> RESOURCES_PREFIX = 
         createProperty(String.class, KEY_RESOURCES_PREFIX);
 
     /**
      * Besides property <code>LOG_XML</code> it is possible to see the the page XML-representation
      * on web client. 
      * 
      * <p>
      *  This property defines an URL-parameter that is used to trigger the behavior. Note that the
      *  parameter value is irrelevant, the existence of the parameter is enough.
      * </p>
      * <p>
      *  If the value of this property is set to <code>null</code> this feature is disabled.
      * </p>
      * <p>
      *  Default: <code>xml</code>
      * </p>
      */
     public static final SettableProperty<String> XML_PARAM_NAME = 
         createProperty(String.class, KEY_XML_PARAM_NAME);
     
     /**
      * Defines the provider that is used to inject system properties to the system.
      * 
      * <p>
      *  This property takes a sub class of {@link PropertyProvider}.
      * </p>
      * <p>
      *  Default: {@link SystemPropertyProvider}
      * </p>  
      */
     public static final SettableProperty<PropertyProvider> PROPERTY_PROVIDER = 
         createProperty(PropertyProvider.class, KEY_PROPERTY_PROVIDER);
     
     /**
      * Binds a lifecycle listener to the system
      */
     public static final BindableProperty<LifecycleListener> LIFECYCLE_LISTENER = 
         createBindableProperty(LifecycleListener.class, KEY_LIFECYCLE_LISTENER);
     
     /**
      * Binds a web application storage to the system
      */
     public static final BindableProperty<WebApplicationStorage> WEB_APPLICATION_STORAGE = 
         createBindableProperty(WebApplicationStorage.class, KEY_WEB_APPLICATION_STORAGE);
 
     /**
      * Binds a request invocation filter to the system
      */
     // This cannot be a bindable property, because it is needed immediately during
     // initiallizing
     public static final SettableProperty<RequestInvocationFilter> REQUEST_INVOCATION_FILTER = 
         createProperty(RequestInvocationFilter.class, KEY_REQUEST_INVOCATION_FILTER);
 
     
     /**
      * Binds a response logger for XML.
      * 
      * <p>
      *  By default XML is logged by normal logging mechanism, but
      *  it is possible to override using this property
      * </p>
      */
     public static final BindableProperty<XMLResponseLogger> XML_RESPONSE_LOGGER = 
         createBindableProperty(XMLResponseLogger.class, KEY_XML_RESPONSE_LOGGER);
     
     /**
      * Binds a XSL-postprocessor to the system
      */
     public static final BindableProperty<DocumentProcessor> XSL_POST_PROCESSOR = 
         createBindableProperty(DocumentProcessor.class, KEY_XSL_POST_PROCESSOR);
     
     /**
      * Defines the root paths that contains components' css- and javascript-resources.
      * 
      * <p>
      *  Determined paths and their sub folder are scanned for all resources and are returned as part
      *  of a page.
      * </p>
      * 
      * <p>
      *  The value of the property can mean class package or directory. By default
      *  value interpreted as package, but by adding a prefix <code>file:</code> value is
      *  interpreted as directory path.
      * </p>
      */
     public static final AddableProperty<Set<String>, String> RESOURCE_PATH
         = new StringSetPropertyImpl(KEY_RESOURCE_PATH);
     
     /**
      * Defines the root package from within view packages are scanned.
      * 
      * <p>
      *  Default: No default value
      * </p>
      */
     public static final AddableProperty<Set<String>, String> VIEW_COMPONENT_ROOT_PACKAGE
         = new StringSetPropertyImpl(KEY_VIEW_COMPONENT_ROOT_PACKAGE);
 
     /**
      * Defines root package from within reloadable classes are scanned.
      * 
      * <p>
      *  This setting has effect only if class reloading is enabled. Also note
      *  that all view-component root packages are automatically included to
      *  reloadable packages.
      * </p>
      * 
      * <p>
      *  Default: No default value
      * </p>
      * 
      */
     public static final ReloadableClassProperty RELOADABLE_CLASSES 
         = new ReloadableClassPropertyImpl(KEY_RELOADABLE_ROOT_PACKAGE);
 
     /**
      * Defines the initial maximum inactivity until page scope is expired.
      * 
      * <p>
      *  This property is used to expire page scope early for bots and web clients incapable of using
      *  javascript, to make sure that resources are freed. 
      * </p>
      * 
      * <p>
      *  Recommended values range from 30 seconds to 5 minutes. For mobile clients it is preferred
      *  to use higher values.
      * </p>
      * 
      * <p>
      *  Default: 30 seconds
      * </p>
      */
     public static final TemporalProperty INITIAL_MAX_INACTIVITY = 
             createTemporalProperty(KEY_INITIAL_MAX_INACTIVITY);
     
     /**
      * Defines the maximum inactivity until page scope is expired.
      * 
      * <p>
      *  This property is used to expire page scope if no activity is taken in page for given maximum time. 
      *  In normal circumstances page scope is expired automatically when page is unloaded. However,
      *  in cases of network failure or misuse expiration may never be triggered. 
      * </p>
      * 
      * <p>
      *  The values of this property can range from minutes to hours depdening on need. If inactivity is 
      *  defined low (&lt; 10 minutes) system is quite safe from misuse but is more intolerable to temporary
      *  network failures and requires constant refreshing. If higher values are used (&gt; 1 hour) it is
      *  recommended to use bandwidth throttling stategies to prevent misuse.
      * </p>
      * 
      * <p>
      *  Default: 2 minutes 
      * </p>
      */
     public static final TemporalProperty MAX_INACTIVITY = 
             createTemporalProperty(KEY_MAX_INACTIVITY);
     
     /**
      * Defines the the period how often expired page scopes are purged from memory.
      * 
      * <p>
      *  There should be no need to touch this property.
      * </p>
      * <p>
      *  Default: 1 minute
      * </p>
      */
     public static final TemporalProperty REMOVAL_SCHEDULE_PERIOD = 
         createTemporalProperty(KEY_REMOVAL_SCHEDULE_PERIOD);
     
     /**
      * Defines additional namespaces to be used in XSL-templates.
      * 
      * <p>
      *  If XSL-templates are using additional namespaces they must be registered here. 
      *  The namespaces are added to the master template.
      * </p>
      */
     public static final SelfKeyValueSetProperty<String, String>
         NAMESPACE
         = new SelfKeyValueSetPropertyImpl<String, String>(KEY_NAMESPACE);
     
     /**
      * Binds a new AttributeJsonSerialiser to the system
      */
     public static final SelfKeyValueSetProperty<Class<?>, 
         Class<? extends AttributeJsonSerializer<?>>> ATTRIBUTE_JSON_SERIALIZER
            = new SelfKeyValueSetPropertyImpl<Class<?>, 
               Class<? extends AttributeJsonSerializer<?>>>(KEY_ATTRIBUTE_JSON_SERIALIZER 
               );
     
     /**
      * Binds a new Json deserialiser to the system
      */
     public static final SelfKeyValueSetProperty<Class<?>, 
     Class<? extends JsonDeserializer<?>>> JSON_DESERIALIZER
         = new SelfKeyValueSetPropertyImpl<Class<?>, 
             Class<? extends JsonDeserializer<?>>>(KEY_JSON_DESERIALIZER);
     
     /**
      * Binds a new Json serializer to the system
      */
     public static final SelfKeyValueSetProperty<Class<?>, 
     Class<? extends JsonSerializer<?>>> JSON_SERIALIZER
         = new SelfKeyValueSetPropertyImpl<Class<?>, 
             Class<? extends JsonSerializer<?>>>(KEY_JSON_SERIALIZER);
     
     /**
      *  Binds a new attribute serializer
      */
     public static final SelfKeyValueSetProperty<Class<?>, 
     Class<? extends AttributeSerializer<?>>> ATTRIBUTE_SERIALIZER
         = new SelfKeyValueSetPropertyImpl<Class<?>, 
             Class<? extends AttributeSerializer<?>>>(KEY_ATTRIBUTE_SERIALIZER);
 
     
     private final Map<String, Object> values;
 
     /**
      * Constructs a new property. Not recommended for normal usage.
      */
     public Configuration() {
         values = new HashMap<String, Object>();
     }
     
     private <T> Configuration(Map<String, Object> values, Property<T> property, T value) {
         this.values = new HashMap<String, Object>();
         this.values.putAll(values);
         this.values.put(property.getKey(), property.validate(value));
     }
     
     /**
      * Returns the value of given property
      */
     @SuppressWarnings("unchecked")
     public <T> T get(Property<T> property) {
         return property.validate((T) values.get(property.getKey()));
     }
     
     /**
      * Returns the value of given property or default if property is null
      */
     @SuppressWarnings("unchecked")
     public <T> T getOrElse(Property<T> property, T def) {
        T value = (T) values.get(property.getKey());
        return value != null ? property.validate(value) : def;
     }
     
     /**
      * Set a new property.
      * 
      * <p>
      *  Previosly set property get overriden.
      * </p>
      */
     public <T> Configuration set(SettableProperty<T> property, T value) {
         return new Configuration(values, property, value);
     }
     
     /**
      * Set a new property.
      * 
      * <p>
      *  Previosly set property get overriden.
      * </p>
      */
     public <T extends Collection<V>, V> Configuration set(AddableProperty<T, V> property, T value) {
         return new Configuration(values, property, value);
     }
     
     /**
      * Set a new property.
      * 
      * <p>
      *  Previously set property get overriden.
      * </p>
      */
     public <T extends Collection<V>, V> Configuration set(SelfAddableProperty<T, V> property, T value) {
         return new Configuration(values, property, value);
     }
     
     /**
      * Set a new property.
      * 
      * <p>
      *  Previously set property get overriden.
      * </p>
      */
     public <T> Configuration set(SelfSettableProperty<T> property) {
         return new Configuration(values, property, property.getValue());
     }
     
     /**
      * Adds a new property
      */
     public <T extends Collection<V>, V> Configuration add(AddableProperty<T, V> property, V value) {
         return new Configuration(values, property, property.add(get(property), value));
     }
     
     /**
      * Adds a new property
      */
     public <T extends Collection<V>, V> Configuration add(SelfAddableProperty<T, V> property) {
         return new Configuration(values, property, property.add(get(property), property.getValue()));
     }
     
     public static <T> SettableProperty<T> createProperty(Class<T> type, String key) {
         return new BasicSettableProperty<T>(key);
     }
     
     public static <T> BindableProperty<T> createBindableProperty(Class<T> type, String key) {
         return new BindablePropertyImpl<T>(key);
     }
     
     public static TemporalProperty createTemporalProperty(String key) {
         return new TemporalPropertyImpl(key);
     }
 }
