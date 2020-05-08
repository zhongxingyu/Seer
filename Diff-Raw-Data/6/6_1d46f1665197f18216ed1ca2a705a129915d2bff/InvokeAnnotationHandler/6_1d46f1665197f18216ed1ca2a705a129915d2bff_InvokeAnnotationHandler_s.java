 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.ibeans.impl;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 
 import org.ibeans.annotation.Invoke;
 import org.ibeans.api.ClientAnnotationHandler;
 import org.ibeans.api.IBeansException;
 import org.ibeans.api.InvocationContext;
 import org.ibeans.api.Request;
 import org.ibeans.api.Response;
 import org.ibeans.spi.IBeansPlugin;
 
 /**
  * Used to Handle {@link org.ibeans.annotation.Invoke} annotated method calls.
  */
 public class InvokeAnnotationHandler implements ClientAnnotationHandler
 {
     private IBeansPlugin plugin;
 
     public InvokeAnnotationHandler(IBeansPlugin plugin)
     {
         this.plugin = plugin;
     }
 
     public Response invoke(InvocationContext ctx) throws Exception
     {
         Invoke invokeInfo = ctx.getMethod().getAnnotation(Invoke.class);
         Object target = ctx.getIBeanConfig().getPropertyParams().get(invokeInfo.object());
 
         if(target==null)
         {
             throw new IllegalArgumentException("No object called '" + invokeInfo.object() + "' set on the invocationContext properties");
         }
 
         Object[] args = ctx.getArgs();
         Class[] paramTypes = new Class[args.length];
 
         for (int i = 0; i < args.length; i++)
         {
             paramTypes[i] = args[i].getClass();
         }
         Method method = target.getClass().getMethod(invokeInfo.method(), paramTypes);
 
        Object result = method.invoke(target, args);
         return createResponse(result, ctx.getRequest());
     }
 
     protected Response createResponse(Object payload, Request request) throws IBeansException
     {
         //Since this is a @Template request, we can keep the headers. These are useful for debugging
         Map<String, Object> headers = new HashMap<String, Object>();
         for (String s : request.getHeaderNames())
         {
             headers.put(s, request.getHeader(s));
         }
         Map<String, DataHandler> attachments = new HashMap<String, DataHandler>();
         for (String s : request.getAttachmentNames())
         {
             attachments.put(s, request.getAttachment(s));
         }
         return plugin.createResponse(payload, headers, attachments);
     }
 
     public String getScheme(Method method)
     {
         return "invoke";
     }
 }
