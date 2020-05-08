 /**
  * Copyright (c) 2011, yMock.com
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met: 1) Redistributions of source code must retain the above
  * copyright notice, this list of conditions and the following
  * disclaimer. 2) Redistributions in binary form must reproduce the above
  * copyright notice, this list of conditions and the following
  * disclaimer in the documentation and/or other materials provided
  * with the distribution. 3) Neither the name of the yMock.com nor
  * the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.ymock.util.formatter;
 
 import com.ymock.util.Logger;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 
 
 /**
  * Class is a log helper used to fmt logging arguments.
  */
 public final class FormatterManager {
 
     /**
      * Singleton instance of the FormatterManager class.
      */
     private static FormatterManager instance;
 
     /**
      * Storage of all the registered formatters.
      */
     private Map<String, FormatterBean> formatters;
 
     /**
      * Private constructor.
      * Initialize the object, registers all available formatters -
      * annotated with {@link FormatGroup} and {@link Format} annotetions
      *
      * @see #registerFormatters()
      */
     private FormatterManager() {
         this.formatters = new HashMap<String, FormatterBean>();
         this.registerFormatters();
     }
 
 
     /**
      * Registers all the available formatters, looks up in classpath for all
      * the classes annotated with {@link FormatGroup} and methods
      * annotated with {@link Format} annotetions and registers them
      * to the manager.
      */
     /**
      * @todo #19 The basic list of formatters should be defined and they should
      *           be implemented on the basis of {@link FormatGroup} and
      *           {@link Format} interfaces
      */
     protected void registerFormatters() {
         final Set<URL> urls = ClasspathHelper.getUrlsForPackagePrefix("");
         final Reflections reflections = new Reflections(
             new ConfigurationBuilder()
                 .setUrls(urls)
                 .setScanners(new TypeAnnotationsScanner()));
         final Set<Class<?>> formatGroups = reflections.
             getTypesAnnotatedWith(FormatGroup.class);
         for (Class<?> formatGroup : formatGroups) {
             final FormatGroup annotationFormatGroup =
                 formatGroup.getAnnotation(FormatGroup.class);
             for (Method m : formatGroup.getMethods()) {
                 if (m.isAnnotationPresent(Format.class)) {
                     final Format annotationFormat =
                         m.getAnnotation(Format.class);
                     final String errorMsg =
                         "Cannot create formatter for class: %s method: %s";
                     try {
                         final FormatterBean formatterBean =
                             new FormatterBean(formatGroup.newInstance(), m);
                         this.formatters.put(annotationFormatGroup.value()
                             + "." + annotationFormat.value(), formatterBean);
                     } catch (InstantiationException e) {
                         Logger.warn(this, errorMsg, formatGroup, m);
                     } catch (IllegalAccessException e) {
                         Logger.warn(this, errorMsg, formatGroup, m);
                     }
 
                 }
             }
 
         }
     }
 
     /**
      * Returns the singleton instance of {@link FormatterManager}.
      *
      * @return the singleton instance of {@link FormatterManager}
      */
 
     public static FormatterManager getInstance() {
        if (FormatterManager.instance == null) {
            FormatterManager.instance = new FormatterManager();
         }
        return FormatterManager.instance;
     }
 
     /**
      * Formats the passed args according to the formatter defined by
      * key argument.
      * Is used by {@link Logger#fmt(String, Object...)}
      *
      * @param key  key for the formatter to be used to fmt the arguments
      * @param args arguments to be formatted
      * @return formatted arguments string
      */
     public String fmt(final String key, final Object... args) {
         final FormatterBean formatterBean = this.formatters.get(key);
         if (formatterBean == null) {
             Logger.warn(this, "Formatter is not registered for key: %s", key);
         } else {
             final String errorMsg =
                 "Error invoking formatter method for class: %s method: %s";
             try {
                 return formatterBean.getMethod().
                     invoke(formatterBean.getGroup(), args).toString();
             } catch (IllegalAccessException e) {
                 Logger.warn(this, errorMsg, formatterBean.getGroup().getClass()
                     , formatterBean.getMethod());
             } catch (InvocationTargetException e) {
                 Logger.warn(this, errorMsg, formatterBean.getGroup().getClass()
                     , formatterBean.getMethod());
             }
         }
         String unformattedReturn = null;
         if (args.length == 1) {
             unformattedReturn = args[0].toString();
         } else {
             unformattedReturn = Arrays.toString(args);
         }
         return unformattedReturn;
     }
 
     /**
      * Internal class holding info to cal formatter method.
      */
     class FormatterBean {
         /**
          * formatter group.
          */
         private Object group;
 
         /**
          * formatter method.
          */
         private Method method;
 
         /**
          * FormatterBean constructor.
          *
          * @param groupParam  formatter group
          * @param methodParam formatter method
          */
         FormatterBean(final Object groupParam, final Method methodParam) {
             this.group = groupParam;
             this.method = methodParam;
         }
 
         /**
          * Return formatter group.
          *
          * @return formatter group
          */
         public Object getGroup() {
             return this.group;
         }
 
         /**
          * Returns formatter method.
          *
          * @return formatter method
          */
         public Method getMethod() {
             return this.method;
         }
     }
 }
