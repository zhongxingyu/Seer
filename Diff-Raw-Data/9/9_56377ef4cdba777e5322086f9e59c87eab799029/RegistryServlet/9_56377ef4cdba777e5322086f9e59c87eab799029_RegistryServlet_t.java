 /*******************************************************************************
  * Copyright 2011 Ivan Shubin
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package net.mindengine.jeremy.registry;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.mindengine.jeremy.bin.Binary;
 import net.mindengine.jeremy.client.Client;
 import net.mindengine.jeremy.exceptions.DeserializationException;
 import net.mindengine.jeremy.exceptions.RemoteMethodIsNotFoundException;
 import net.mindengine.jeremy.exceptions.RemoteObjectIsNotFoundException;
 import net.mindengine.jeremy.exceptions.SerializationException;
 import net.mindengine.jeremy.messaging.LanguageHandler;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FileUtils;
 
 
 public class RegistryServlet extends HttpServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = 8419855038481651498L;
 
     private Registry registry;
     
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
         String uri = request.getRequestURI();
         if(uri.endsWith("/")) {
             uri = uri.substring(0, uri.length()-1);
         }
         
         Object output = null;
         try {
             if(uri.endsWith("~")) {
                 if(uri.matches("/.*/.*/~")) {
                     output = getListOfRemoteMethodArguments(uri);
                 }
                 else if(uri.matches("/.*/~")) {
                     output = getListOfAllRemoteMethods(uri);
                 }
                 else if(uri.matches("/~")) {
                     output = getListOfAllObjects();
                 }
             }
             else if (uri.equals("/~bin")) {
                 output = uploadBinaryObject(request);
             }
             else if(uri.matches("/.*/.*")) {
               output = invokeRemoteMethod(uri, request);
             }
             else {
                 PrintWriter out = response.getWriter();
                 response.setStatus(404);
                 out.print("\"Not found\"");
                 out.flush();
                 out.close();
                 return;
             }
         }
         catch (Throwable e) {
             output = e;
             response.addHeader(Client.CLASS_PATH, e.getClass().getName());
             e.printStackTrace();
         }
         
         try {
             if(output!=null && Binary.class.isAssignableFrom(output.getClass())){
                 printBinaryObjectToResponse(output, response);
             }
             else {
                 printObjectToResponse(output, request, response);
             }
         } catch (SerializationException e) {
             PrintWriter out = response.getWriter();
             response.setStatus(422);
             out.print("\"Unprocessable Entity\"");
             out.flush();
             out.close();
         }
     }
     
     
     
     private void printBinaryObjectToResponse(Object object, HttpServletResponse response) throws IOException, SerializationException {
         LanguageHandler languageHandler = registry.getLanguageHandler(Client.LANGUAGE_BINARY);
         
         byte[] bytes = languageHandler.serializeResponseToBytes(object);
         response.addHeader(Client.LANGUAGE_HEADER, Client.LANGUAGE_BINARY);
         
         if(object!=null) {
             response.addHeader(Client.CLASS_PATH, object.getClass().getName());
         }
         OutputStream os = response.getOutputStream();
         os.write(bytes);
         os.close();
         os.flush();
         
     }
     
     private void printObjectToResponse(Object output, HttpServletRequest request, HttpServletResponse response) throws SerializationException, IOException {
         PrintWriter out = response.getWriter();
         
         //Will use the same language in response as in request
         LanguageHandler languageHandler = registry.getLanguageHandler(request.getHeader(Client.LANGUAGE_HEADER));
         
         if(output!=null) {
             response.addHeader(Client.CLASS_PATH, output.getClass().getName());
         }
         
         String body = languageHandler.serializeResponse(output);
         if(output instanceof Throwable) {
             response.setStatus(400);
         }
         else {
             response.setStatus(200);
         }
         out.print(body);
         response.addHeader(Client.LANGUAGE_HEADER, languageHandler.getMimeType());
         out.flush();
         out.close();
     }
     
     private Object invokeRemoteMethod(String uri, HttpServletRequest request) throws Throwable {
         Pattern pattern = Pattern.compile("/(.*?)/(.*?)/");
         Matcher m = pattern.matcher(uri+"/");
         while (m.find()) {
             String objectName = m.group(1);
             String methodName = m.group(2);
             //Searching for remote object in local registry
             RemoteObject remoteObject =  registry.getRemoteObjects().get(objectName);
             if(remoteObject==null) {
                 throw new RemoteObjectIsNotFoundException("Object with name '"+objectName+"' was not found");
             }
             
             Method method = remoteObject.getRemoteMethods().get(methodName);
             if(method==null) {
                 throw new RemoteObjectIsNotFoundException("Object with name '"+objectName+"' doesn't have method '"+methodName+"'");
             }
             
           //Collecting arguments for the remote method
             Class<?>[] parameterTypes = method.getParameterTypes();
             Object[] arguments = new Object[parameterTypes.length];
             for(int i=0; i<arguments.length; i++) {
                 String parameter = request.getParameter("arg"+i);
                 if(parameter!=null) {
                     //Checking whether it is a plain serialized object in request or it is a reference to an object in cache
                     if(parameter.startsWith("~")) {
                         //Fetching object from cache
                         String key = parameter.substring(1);
                         
                         arguments[i] = registry.getObjectCache().retrieveObjectFromCache(key);
                         if(arguments[i]==null){
                             throw new NullPointerException("Couldn't find argument in cache with a key "+key);
                         }
                     }
                     else {
                         LanguageHandler languageHandler = registry.getLanguageHandler(request.getHeader(Client.LANGUAGE_HEADER));
                         
                         Class<?> parameterClass = parameterTypes[i];
                         String parameterClassName = request.getHeader(Client.CLASS_PATH+"-"+i);
                         if(parameterClassName!=null) {
                             parameterClass = Class.forName(parameterClassName);
                         }
                         arguments[i] = languageHandler.deserializeObject(parameter, parameterClass);
                     }
                 }
                 else arguments[i] = null;
             }
             try {
                 return method.invoke(remoteObject.getObject(), arguments);
             }
             catch (InvocationTargetException e) {
                 throw e.getTargetException();
             }
         }
         throw new IllegalArgumentException("Cannot find name of object and remote method in URL");
     }
 
 
     @SuppressWarnings("unchecked")
     private Map<String, String> uploadBinaryObject(HttpServletRequest request) throws FileUploadException, DeserializationException {
         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         List<FileItem> files = upload.parseRequest(request);
         
         Map<String, String> cachedObjects = new HashMap<String, String>();
         if(files!=null && files.size()>0) {
             for(FileItem fileItem : files) {
                 byte[] content = fileItem.get();
                 
                 //Looking for binary language-handler in registry
                 LanguageHandler languageHandler = registry.getLanguageHandlers().get(Client.LANGUAGE_BINARY);
                 if(languageHandler==null) {
                     throw new NullPointerException("There is no language handler specified for type: "+Client.LANGUAGE_BINARY);
                 }
                 
                 Object object = languageHandler.deserializeObject(content, null);
                 
                 //Putting object to cache so it will be used later
                 String id = registry.getObjectCache().cacheObject(object);
                 cachedObjects.put(fileItem.getFieldName(), id);
             }
             return cachedObjects;
         }
         else throw new IllegalArgumentException("Can't find any file for upload");
     }
 
 
     private RemoteMethodMetadata getListOfRemoteMethodArguments(String uri) throws RemoteObjectIsNotFoundException, RemoteMethodIsNotFoundException {
         Pattern pattern = Pattern.compile("/(.*?)/(.*?)/~");
         Matcher m = pattern.matcher(uri);
         while (m.find()) {
             String objectName = m.group(1);
             String methodName = m.group(2);
             
             RemoteObject object = registry.getRemoteObjects().get(objectName);
             if(object!=null) {
                  Method method = object.getRemoteMethods().get(methodName);
                  if(method!=null) {
                      RemoteMethodMetadata metadata = new RemoteMethodMetadata();
                      metadata.setMethod(method.getName());
                      metadata.setObject(objectName);
                      
                      List<TypeDescriptor> arguments = new LinkedList<TypeDescriptor>();
                      Class<?>[]argumentTypes =  method.getParameterTypes();
                      if(argumentTypes!=null) {
                          for(Class<?> argumentType : argumentTypes) {
                              arguments.add(TypeDescriptor.createDescriptor(argumentType));
                          }
                      }
                      
                      metadata.setArguments(arguments);
                      
                      if(!method.getReturnType().equals(Void.TYPE)) {
                          metadata.setReturns(TypeDescriptor.createDescriptor(method.getReturnType()));
                      }
                      return metadata;
                  }
                  else throw new RemoteMethodIsNotFoundException("There is no such method '"+methodName+"' for object '"+objectName+"'"); 
             }
             else throw new RemoteObjectIsNotFoundException("There is no such remote object '"+objectName+"'");
         }
         throw new IllegalArgumentException("Cannot find name of object in URL");
     }
 
 
     private Collection<String> getListOfAllRemoteMethods(String uri) throws RemoteObjectIsNotFoundException {
         Pattern pattern = Pattern.compile("/(.*?)/~");
         Matcher m = pattern.matcher(uri);
         while (m.find()) {
             String name = m.group(1);
             RemoteObject object = registry.getRemoteObjects().get(name);
             if(object!=null) {
                  return object.getRemoteMethods().keySet();
             }
             throw new RemoteObjectIsNotFoundException("There is no such remote object '"+name+"'");
         }
         throw new IllegalArgumentException("Cannot find name of object in URL");
     }
 
 
     private String[] getListOfAllObjects() {
         return registry.getRemoteObjects().keySet().toArray(new String[]{});
     }
     
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
 
     public Registry getRegistry() {
         return registry;
     }
 }
