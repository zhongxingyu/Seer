 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.objectweb.asm.ClassReader;
 
 import agave.exception.AgaveException;
 import agave.exception.DestinationException;
 import agave.exception.HandlerException;
 import agave.exception.RequestBindingException;
 import agave.exception.ResponseBindingException;
 import agave.internal.ClassEnvironment;
 import agave.internal.HandlerDescriptor;
 import agave.internal.HandlerDescriptorImpl;
 import agave.internal.HandlerIdentifier;
 import agave.internal.HandlerRegistry;
 import agave.internal.HandlerRegistryImpl;
 import agave.internal.HandlerScanner;
 import agave.internal.MultipartRequestImpl;
 import agave.internal.ParameterBinder;
 import agave.internal.ParameterBinderImpl;
 import agave.internal.PartBinder;
 import agave.internal.PartBinderImpl;
 import agave.internal.SimpleClassEnvironment;
 
 /**
  * Scans the classes directory of a deployed context for any configured handlers
  * and forwards HTTP requests to the handlers if they match the requested URI.
  * 
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class AgaveFilter implements Filter {
 
     private static final Logger LOGGER = Logger.getLogger(AgaveFilter.class.getName());
     
     private FilterConfig config;
     private ClassEnvironment classEnvironment;
     private HandlerRegistry handlerRegistry;
 
     /**
      * Initializes the {@code AgaveFilter} by scanning for handler classes and
      * populating a {@link agave.internal.HandlerRegistry HandlerRegistry} with
      * them. Then, this initializes the dependency injection container (if any)
      * by instantiation a {@link agave.internal.ClassEnvironment}.
      * 
      * @param config
      *            the supplied filter configuration object
      * @throws ServletException
      */
     public void init(FilterConfig config) throws ServletException {
         this.config = config;
         try {
             setHandlerRegistry(new HandlerRegistryImpl());
             File classesDirectory = null;
             
             if (config.getInitParameter("classesDirectory") != null) {
                 classesDirectory = new File(config.getInitParameter("classesDirectory"));
             } else {
                 classesDirectory = new File(config.getServletContext().getRealPath("/WEB-INF/classes"));
             }
             
             scanClassesDirForHandlers(classesDirectory);
             classEnvironment = new SimpleClassEnvironment();
             classEnvironment.initializeEnvironment();
         } catch (Exception ex) {
             throw new ServletException(ex);
         }
         
         if (!handlerRegistry.getDescriptors().isEmpty()) {
             for (HandlerDescriptor descriptor : handlerRegistry.getDescriptors()) {
                 LOGGER.fine(descriptor.getHandlerClass().getName() + "#" 
                         + descriptor.getHandlerMethod().getName() + "() registered -> "
                         + descriptor.getPattern());
             }
         } else {
             LOGGER.fine("No handlers registered");
         }
         LOGGER.info("AgaveFilter successfully initialized");
     }
 
     public void destroy() {
         config = null;
         handlerRegistry = null;
         classEnvironment = null;
     }
 
     /**
      * <p>
      * Handles the routing of HTTP requests through the framework. The algorithm
      * used internally is as follows:
      * </p>
      * 
      * <ol>
      * <li>
      * {@link agave.internal.ClassEnvironment#createFormInstance Instantiate a
      * form if necessary}
      * <ol>
      * <li>{@link agave.internal.ParameterBinder#bindRequestParameters Bind
      * request parameters if necessary}</a></li>
      * <li>{@link agave.internal.ParameterBinder#bindURIParameters Bind URI
      * parameters if necessary}</li>
      * </ol>
      * </li>
      * <li>{@link agave.internal.ClassEnvironment#createHandlerInstance
      * Instantiate a handler}</li>
      * <li>Bind the request to the handler if necessary</li>
      * <li>Bind the response to the handler if necessary</li>
      * <li>Invoke the handler method with the instantiated form as the only
      * argument</li>
      * </ol>
      * 
      * <p>
      * When one of the two supported encoding methods is selected, then this
      * filter will field the HTTP request that was made and will prevent future
      * execution of the filter chain. If an unsupported encoding type is
      * requested or if a handler is not configured for the requested URI, this
      * filter will simply continue with execution of the filter chain. The two
      * encoding types that are supported by this method are:
      * </p>
      * 
      * <ul>
      * <li><a
      * href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
      * application/x-www-form-urlencoded</a></li>
      * <li><a
      * href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2">
      * multipart/form-data</a></li>
      * </ul>
      * 
      * @param req
      *            the Servlet request object; it will be cast to an {@code
      *            HttpServletRequest}
      * @param resp
      *            the Servlet response object; it will be cast to an {@code
      *            HttpServletResponse}
      * @param chain
      *            the filter chain this filter is a member of
      * @throws IOException
      *             if an I/O error occurs
      * @throws ServletException
      *             if a Servlet error occurs
      * @see <a
      *      href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4">
      *      W3C Form Encoding Types< /a>
      * @see agave.HandlesRequestsTo
      * @see agave.BindsInput
      * @see agave.ConvertWith
      * @see agave.BindsRequest
      * @see agave.BindsResponse
      */
     public final void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
             throws IOException, ServletException {
         HttpServletRequest request = (HttpServletRequest) req;
         HttpServletResponse response = (HttpServletResponse) resp;
 
         HandlerDescriptor descriptor = handlerRegistry.findMatch(request);
         if (descriptor != null) {
             
             LOGGER.fine(request.getRequestURI() + " -> " + descriptor.getHandlerClass().getName() + "#" + 
                     descriptor.getHandlerMethod() + "()");
 
             if (MultipartRequestImpl.isMultipart(request)) {
                 request = new MultipartRequestImpl(request);
             }
 
             Object formInstance = classEnvironment.createFormInstance(descriptor);
             if (formInstance != null) {
                 ParameterBinder binder = new ParameterBinderImpl(formInstance, descriptor);
                 binder.bindRequestParameters(request);
                 binder.bindURIParameters(request);
 
                 if (MultipartRequestImpl.isMultipart(request)) {
                     PartBinder partBinder = new PartBinderImpl(formInstance, descriptor);
                     partBinder.bindParts((MultipartRequest) request);
                 }
             }
 
             Object handlerInstance = classEnvironment.createHandlerInstance(descriptor);
 
             if (descriptor.getRequestSetter() != null) {
                 try {
                     descriptor.getRequestSetter().invoke(handlerInstance, request);
                 } catch (IllegalAccessException ex) {
                     throw new RequestBindingException(descriptor, ex);
                 } catch (InvocationTargetException ex) {
                     throw new RequestBindingException(descriptor, ex);
                 }
             }
 
             if (descriptor.getResponseSetter() != null) {
                 try {
                     descriptor.getResponseSetter().invoke(handlerInstance, response);
                 } catch (IllegalAccessException ex) {
                     throw new ResponseBindingException(descriptor, ex);
                 } catch (InvocationTargetException ex) {
                     throw new ResponseBindingException(descriptor, ex);
                 }
             }
             
             if (descriptor.getServletContextSetter() != null) {
                 try {
                     descriptor.getServletContextSetter().invoke(handlerInstance, config.getServletContext());
                 } catch (IllegalAccessException ex) {
                     throw new ResponseBindingException(descriptor, ex);
                 } catch (InvocationTargetException ex) {
                     throw new ResponseBindingException(descriptor, ex);
                 }
             }
 
             Object result = null;
 
             try {
                 if (formInstance != null) {
                     if (descriptor.getHandlerMethod().getReturnType() != null) {
                         result = descriptor.getHandlerMethod().invoke(handlerInstance, formInstance);
                     } else {
                         descriptor.getHandlerMethod().invoke(handlerInstance, formInstance);
                     }
                 } else {
                     if (descriptor.getHandlerMethod().getReturnType() != null) {
                         result = descriptor.getHandlerMethod().invoke(handlerInstance);
                     } else {
                         descriptor.getHandlerMethod().invoke(handlerInstance);
                     }
                 }
             } catch (InvocationTargetException ex) {
                 if (ex.getCause() instanceof AgaveException) {
                     throw (AgaveException) ex.getCause();
                 } else if (ex.getCause() instanceof IOException) {
                     throw (IOException) ex.getCause();
                 } else if (ex.getCause() instanceof RuntimeException) {
                     throw (RuntimeException) ex.getCause();
                 } else {
                     throw new HandlerException(ex.getMessage(), ex.getCause());
                 }
             } catch (IllegalAccessException ex) {
                 throw new HandlerException(descriptor, ex);
             }
 
             if (result != null && !response.isCommitted()) {
                 
                 URI uri = null;
                 boolean redirect = false;
                 
                 if (result instanceof Destination) {
                     Destination destination = (Destination)result;
                     try {
                         uri = new URI(null, destination.encode(config.getServletContext()), null);
                         if (destination.getRedirect() == null) {
                             if ("POST".equalsIgnoreCase(request.getMethod())) {
                                 redirect = true;
                             }
                         } else {
                             redirect = destination.getRedirect();
                         }
                     } catch (URISyntaxException ex) {
                         throw new DestinationException(ex.getMessage(), ex.getCause());
                     }
                 } else if (result instanceof URI) {
                     uri = (URI)result;
                     redirect = true;
                 } else {
                     throw new DestinationException(descriptor);
                 }
                 
                 if (redirect) {
                     response.sendRedirect(uri.toASCIIString());
                 } else {
                     request.getRequestDispatcher(uri.toASCIIString()).forward(request, response);
                 }
                 
             }
         } else {
             chain.doFilter(req, resp);
         }
     }
 
     /**
      * Scans the supplied directory for handlers. Handlers in turn are inspected
      * and have a {@link agave.internal.HandlerDescriptor HandlerDescriptor}
      * generated for them which then gets registered in the
      * {@link agave.internal.HandlerRegistry HandlerRegistry} as handlers are
      * found.
      * 
      * @param root
      *            the root directory to scan files for, typically {@code
      *            /WEB-INF/classes}
      */
     protected void scanClassesDirForHandlers(File root) throws FileNotFoundException, IOException,
            ClassNotFoundException, AgaveException {
         if (root != null && root.canRead()) {
             for (File node : root.listFiles()) {
                 if (node.isDirectory()) {
                     scanClassesDirForHandlers(node);
                 } else if (node.isFile() && node.getName().endsWith(".class")) {
                     ClassReader classReader = new ClassReader(new FileInputStream(node));
                     Collection<HandlerIdentifier> handlerIdentifiers = new ArrayList<HandlerIdentifier>();
                     classReader.accept(new HandlerScanner(handlerIdentifiers), ClassReader.SKIP_CODE);
                     for (HandlerIdentifier handlerIdentifier : handlerIdentifiers) {
                         handlerRegistry.addDescriptor(new HandlerDescriptorImpl(handlerIdentifier));
                     }
                 }
             }
         }
     }
 
     protected void setConfig(FilterConfig config) {
         this.config = config;
     }
 
     protected FilterConfig getConfig() {
         return config;
     }
 
     protected void setHandlerRegistry(HandlerRegistry handlerRegistry) {
         this.handlerRegistry = handlerRegistry;
     }
 
     protected HandlerRegistry getHandlerRegistry() {
         return handlerRegistry;
     }
 }
