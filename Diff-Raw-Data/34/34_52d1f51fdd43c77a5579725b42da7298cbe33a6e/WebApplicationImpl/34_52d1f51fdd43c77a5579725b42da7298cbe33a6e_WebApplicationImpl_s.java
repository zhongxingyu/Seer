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
 
 package net.contextfw.web.application.internal.service;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.HttpContext;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.WebApplicationHandle;
 import net.contextfw.web.application.component.Component;
 import net.contextfw.web.application.component.DOMBuilder;
 import net.contextfw.web.application.internal.ComponentUpdateHandler;
 import net.contextfw.web.application.internal.ComponentUpdateHandlerFactory;
 import net.contextfw.web.application.internal.WebResponder;
 import net.contextfw.web.application.internal.WebResponder.Mode;
 import net.contextfw.web.application.internal.component.ComponentBuilder;
 import net.contextfw.web.application.internal.component.ComponentRegister;
 import net.contextfw.web.application.internal.component.WebApplicationComponent;
 import net.contextfw.web.application.internal.initializer.InitializerContextImpl;
 import net.contextfw.web.application.internal.servlet.UriMapping;
 import net.contextfw.web.application.internal.util.AttributeHandler;
 import net.contextfw.web.application.lifecycle.PageScoped;
 import net.contextfw.web.application.lifecycle.ResourceView;
 import net.contextfw.web.application.remote.ResourceBody;
 import net.contextfw.web.application.remote.ResourceResponse;
 import net.contextfw.web.application.scope.Provided;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 @PageScoped
 public class WebApplicationImpl implements WebApplication {
 
     @Inject
     @Provided
     private Gson gson;
     
     @Inject
     @Provided
     private ComponentUpdateHandlerFactory euhf;
 
     @Inject
     @Provided
     private ComponentBuilder builder;
 
     private static volatile Map<String, ComponentUpdateHandler> updateHandlers = new HashMap<String, ComponentUpdateHandler>();
 
     @Inject
     @Provided
     private Injector injector;
 
     @Inject
     private HttpContext httpContext;
 
 //    @Inject
 //    private PageFlowFilter pageFlowFilter;
 
     private final ComponentRegister componentRegister = new ComponentRegister();
 
     private final WebApplicationComponent rootComponent =
             new WebApplicationComponent(componentRegister);
 
     private List<Class<? extends Component>> chain;
 
     private InitializerContextImpl context;
 
     @Inject
     @Provided
     private WebResponder responder;
 
     private Mode mode = Mode.INIT;
 
     @Inject
     @Provided
     private AttributeHandler attributes;
 
     @Inject
     private WebApplicationHandle webApplicationHandle;
 
     //private final String contextPath;
     
     private final WebApplicationConf conf;
     
     @Inject
     public WebApplicationImpl(WebApplicationConf conf) {
         this.conf = conf;
     }
 
     @Override
     public void initState(UriMapping mapping) {
         context = new InitializerContextImpl(
                 builder,
                 mapping,
                 httpContext.getRequestURI()
                     .substring(httpContext.getRequest().getContextPath().length()),
                 injector,
                 httpContext.getRequest(),
                 chain);
         getRootComponent().registerChild(context.initChild());
     }
 
     @Override
     public boolean sendResponse() {
 
         try {
             if (mode == Mode.INIT) {
                 if (httpContext.getRedirectUrl() != null) {
                     httpContext.getResponse().sendRedirect(httpContext.getRedirectUrl());
                     return true;
                 } else if (httpContext.getErrorCode() != null) {
                     httpContext.getResponse().sendError(httpContext.getErrorCode(), httpContext.getErrorMsg());
                     return true;
                 } else if (httpContext.isReload()) {
                     StringBuilder sb = new StringBuilder(httpContext.getRequestURI());
                     if (httpContext.getQueryString() != null) {
                         sb.append("?").append(httpContext.getQueryString());
                     }
                     httpContext.getResponse().sendRedirect(sb.toString());
                 }
             }
 
             try {
                 if (mode == Mode.INIT && context.getLeaf() instanceof ResourceView) {
                     return sendResourceResponse();
                 } else {
                     sendNormalResponse();
                     return false;
                 }
             } catch (Exception e) {
                 if (e instanceof WebApplicationException) {
                     throw (WebApplicationException) e;
                 } else {
                     throw new WebApplicationException("Exception while trying to init state", e);
                 }
             }
 
         } catch (IOException e) {
             throw new WebApplicationException(e);
         }
     }
 
     private boolean sendResourceResponse() throws IOException {
         boolean expire = true;
         
         ResourceView leaf = (ResourceView) context.getLeaf();
         try {
             ResourceBody annotation = leaf.getClass().getMethod("getResponse")
                 .getAnnotation(ResourceBody.class);
             
             if (annotation != null) {
                 expire = annotation.expire();
             }
         } catch (SecurityException e) {
             throw new WebApplicationException(e);
         } catch (NoSuchMethodException e) {
             throw new WebApplicationException(e);
         }
         
         Object retVal = leaf.getResponse();
         if (retVal == null) {
             return expire;
         }
         if (retVal instanceof ResourceResponse) {
             ((ResourceResponse) retVal).serve(
                     httpContext.getRequest(), 
                     httpContext.getResponse());
         } else {
             HttpServletResponse response = httpContext.getResponse();
             setHeaders(response);
             response.setContentType("application/json; charset=UTF-8");
             gson.toJson(retVal, response.getWriter());
         }
         return expire;
     }
 
     private void sendNormalResponse() throws ServletException, IOException {
         httpContext.getResponse().setContentType("text/html; charset=UTF-8");
 
         DOMBuilder d;
 
         if (mode == Mode.INIT) {
             d = new DOMBuilder("WebApplication", attributes, builder, conf.getNamespaces());
         } else {
             d = new DOMBuilder("WebApplication.update", 
                                attributes, 
                                builder,
                                conf.getNamespaces());
         }
 
         d.attr("handle", webApplicationHandle.toString());
         d.attr("contextPath", httpContext.getRequest().getContextPath());
 
         if (context.getLocale() != null) {
             d.attr("xml:lang", context.getLocale().toString());
             d.attr("lang", context.getLocale().toString());
         }
         if (mode == Mode.INIT) {
             getRootComponent().buildChild(d);
         } else if (httpContext.getRedirectUrl() != null) {
             d.descend("Redirect").attr("href", httpContext.getRedirectUrl());
         } else if (httpContext.getErrorCode() != null) {
             d.descend("Error").attr("code", httpContext.getErrorCode()).text(httpContext.getErrorMsg());
         } else if (httpContext.isReload()) {
             d.descend("Reload");
         } else {
             getRootComponent().buildChildUpdate(d, builder);
         }
 
         getRootComponent().clearCascadedUpdate();
 
         if (conf.getXmlParamName() == null
                 || httpContext.getRequest().getParameter(conf.getXmlParamName()) == null) {
             responder.sendResponse(d.toDocument(), httpContext.getResponse(), mode);
         } else {
             responder.sendResponse(d.toDocument(), httpContext.getResponse(), Mode.XML);
         }
     }
 
     @Override
     public UpdateInvocation updateState(String componentId, String method) {
         mode = Mode.UPDATE;
         return updateElements(componentId, method);
     }
 
     @SuppressWarnings("unchecked")
     protected UpdateInvocation updateElements(final String id, final String method) {
         try {
             Component element = componentRegister.findComponent(id);
             String key = ComponentUpdateHandler.getKey(element.getClass(), method);
 
                 if (!updateHandlers.containsKey(key) || conf.isDevelopmentMode()) {
                     updateHandlers.put(key, euhf.createHandler(element.getClass(), method));
                 }
 
                 ComponentUpdateHandler handler = updateHandlers.get(key);
 
                 if (handler != null) {
                     if (handler.getDelayed() == null
                                 || !injector.getInstance(handler.getDelayed().value())
                                   .isUpdateDelayed(element, httpContext.getRequest())) {
                         return new UpdateInvocation(
                                 handler.isResource(),
                                 handler.invoke(rootComponent, element, httpContext.getRequest())
                                 );
                     } else {
                         return UpdateInvocation.DELAYED;
                     }
                 } else {
                    return UpdateInvocation.NOT_DELAYED;
                 }
         } catch (Exception e) {
             if (e instanceof WebApplicationException) {
                 throw (WebApplicationException) e;
             } else {
                 throw new WebApplicationException("Failed to update elements", e);
             }
         }
     }
 
     @Override
     public void setInitializerChain(List<Class<? extends Component>> chain) {
         this.chain = chain;
     }
     
     public void setHeaders(HttpServletResponse response) {
         response.addHeader("Expires", "Sun, 19 Nov 1978 05:00:00 GMT");
         response.addHeader("Last-Modified", new Date().toString());
         response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
         // response.addHeader("Cache-Control","post-check=0, pre-check=0");
         response.addHeader("Pragma", "no-cache");
         response.setHeader("Connection", "Keep-Alive");
     }
 
     public WebApplicationComponent getRootComponent() {
         return rootComponent;
     }
 }
