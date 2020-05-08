 /**
  * Copyright (c) 2012 Andrew Eells
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  */
 
 package com.aeells.hibernate.profiling;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 // Log4j configuration (TRACE) triggers whether performance statistics are recorded
 public final class HibernateProfilingInterceptor
 {
     private static final Logger LOGGER = Logger.getLogger(HibernateProfilingInterceptor.class);
 
     public void profileWrites(final ProceedingJoinPoint call, final Object model) throws Throwable
     {
         if (LOGGER.isTraceEnabled())
         {
             final DateTime start = new DateTime();
             call.proceed();
             logProfileCall(call, model, (new Duration(start, new DateTime()).getMillis()));
         }
         else
         {
             call.proceed();
         }
     }
 
     public Object profileFind(final ProceedingJoinPoint call) throws Throwable
     {
         if (LOGGER.isTraceEnabled())
         {
             final DateTime start = new DateTime();
             final Object model = call.proceed();
             logProfileCall(call, model, (new Duration(start, new DateTime()).getMillis()));
             return model;
         }
         else
         {
             return call.proceed();
         }
     }
 
     public Object profileFindList(final ProceedingJoinPoint call) throws Throwable
     {
         if (LOGGER.isTraceEnabled())
         {
             final DateTime start = new DateTime();
             @SuppressWarnings({"unchecked"}) final List<Object> models = (List<Object>) call.proceed();
             logProfileCall(call, models, (new Duration(start, new DateTime()).getMillis()));
             return models;
         }
         else
         {
             return call.proceed();
         }
     }
 
     private void logProfileCall(final ProceedingJoinPoint call, final Object model, final long duration)
     {
         // model is null on login
         if (model != null && model.getClass().isAnnotationPresent(HibernateProfiled.class))
         {
             try
             {
                 LOGGER.trace(new StringBuilder(StringUtils.substringBefore(call.getSignature().getDeclaringType().getSimpleName(), "$")).append("|").
                         append(call.getSignature().getName()).append("|").append(getClassNameAndPersistentId(model)).append("|").append(duration));
             }
             catch (final Exception e)
             {
                 LOGGER.error("unable to profile class: " + model.getClass());
             }
         }
     }
 
     private void logProfileCall(final ProceedingJoinPoint call, final List<Object> models, final long duration)
     {
         if (models != null && !models.isEmpty())
         {
             LOGGER.trace(new StringBuilder(StringUtils.substringBefore(call.getSignature().getDeclaringType().getSimpleName(), "$")).
                     append("|").append(call.getSignature().getName()).append("|").append(getClassNameAndPersistentIds(models)).append(duration));
         }
     }
 
     private String getClassNameAndPersistentId(final Object model) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
     {
         final String identifier = model.getClass().getAnnotation(HibernateProfiled.class).identifier();
         return new StringBuilder(model.getClass().getSimpleName()).append("|").append(BeanUtils.getProperty(model, identifier)).toString();
     }
 
     private String getClassNameAndPersistentIds(final List<Object> models)
     {
         final StringBuilder sb = new StringBuilder();
         for (final Object model : models)
         {
             if (model.getClass().isAnnotationPresent(HibernateProfiled.class))
             {
                 if (sb.length() == 0)
                 {
                     sb.append(model.getClass().getSimpleName()).append("|");
                 }
 
                 try
                 {
                     sb.append(BeanUtils.getProperty(model, model.getClass().getAnnotation(HibernateProfiled.class).identifier())).append("|");
                 }
                 catch (final Exception e)
                 {
                     LOGGER.error("unable to profile class: " + model.getClass());
                 }
             }
         }
 
         return sb.toString();
     }
 }
