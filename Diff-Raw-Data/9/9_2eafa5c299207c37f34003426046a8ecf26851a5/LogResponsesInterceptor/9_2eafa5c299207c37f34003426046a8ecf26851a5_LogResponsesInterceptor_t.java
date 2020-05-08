 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.internal.client;
 
 import org.mule.api.MuleMessage;
 import org.mule.ibeans.api.client.AbstractCallInterceptor;
 import org.mule.ibeans.api.client.params.InvocationContext;
 import org.mule.ibeans.channels.MimeTypes;
 import org.mule.util.FileUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 
 /**
  * Logs responses from iBean invocations to a directory.  Files are stored using the pattern -
  * [Test class Name]-[Test method name]-response[count].[json/xml/rss/atom/txt/html/data]
  *
  * This is useful for tracking responses for different methods which can be used to create mock test cases
  *
  * This interceptor can be enabled by setting the VM property 'ibeans.log.responses' to the directory path where the files
  * should be stored.
  */
 public class LogResponsesInterceptor extends AbstractCallInterceptor
 {
     private File logDirectory;
 
     public LogResponsesInterceptor(String logDirectory)
     {
         this(new File(logDirectory));
     }
 
     public LogResponsesInterceptor(File logDirectory)
     {
         if(!logDirectory.exists())
         {
             throw new IllegalArgumentException("Log directory does not exist: " + logDirectory);
         }
 
         if(!logDirectory.isDirectory())
         {
             throw new IllegalArgumentException("Log directory is not a directory: " + logDirectory);
         }
 
         this.logDirectory = logDirectory;
     }
 
     @Override
     public void afterCall(InvocationContext invocationContext) throws Throwable
     {
         MuleMessage msg = ((InternalInvocationContext)invocationContext).getResponseMuleMessage();
         boolean isStream = InputStream.class.isAssignableFrom(msg.getPayload().getClass());
         String response = msg.getPayloadAsString();
 
         //Set the result on the invocationContext in case we got back a stream, this avoids StreamAlreadyClosed exceptions
         if(isStream)
         {
             //wrap it back up as a ByteArray so that transformers do no need to handle the new String payload
             invocationContext.setResult(new ByteArrayInputStream(response.getBytes()));
         }
         else
         {
             invocationContext.setResult(response);
         }
         
         String type = (String)invocationContext.getResponseHeaderParam("Content-Type");
         String testMethod = getTestMethod(invocationContext.getMethod().getName());
         String ext = getFileExtension(type);
         int i = 1;
         String filename = testMethod + "-response" + (i++) + ext;
         File responseFile = new File(logDirectory, filename);
         //make sure we have a unique file name since the same ibeans method may be called more than once from a test method
         while(responseFile.exists())
         {
             filename = testMethod + "-response" + i++ + ext;
             responseFile = new File(logDirectory, filename);
         }
 
         FileUtils.writeStringToFile(responseFile, response);
     }
 
     protected String getFileExtension(String mimeType)
     {
         int i = mimeType.indexOf(";");
         if(i > -1)
         {
             mimeType = mimeType.substring(0, i);
         }
         
         if(mimeType.equals(MimeTypes.ATOM))
         {
             return ".atom";
         }
         else if(mimeType.equals(MimeTypes.RSS))
         {
             return ".rss";
         }
        else if(mimeType.equals(MimeTypes.XML) || mimeType.equals(MimeTypes.APPLICATION_XML))
         {
             return ".xml";
         }
         else if(mimeType.equals(MimeTypes.JSON))
         {
             return ".json";
         }
         else if(mimeType.equals(MimeTypes.TEXT))
         {
             return ".txt";
         }
         else if(mimeType.equals(MimeTypes.HTML))
         {
             return ".html";
         }
         else
         {
             return ".data";
         }
     }
 
     private String getTestMethod(String name)
     {
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         for (int i = 0; i < stack.length; i++)
         {
             StackTraceElement element = stack[i];
             if(element.getMethodName().equals(name))
             {
                 //This assume that the ibean was called from the current method, not a method called by the test method
                 //This should be fine for almot all if not all ibeans test cases
                 String className = stack[i+1].getClassName();
                 int x = className.lastIndexOf(".");
                 if(x > -1)
                 {
                     className = className.substring(x+1);
                 }
                 return className + "-" + stack[i+1].getMethodName();
             }
         }
         throw new IllegalStateException("Method not found in call stack");
     }
 }
