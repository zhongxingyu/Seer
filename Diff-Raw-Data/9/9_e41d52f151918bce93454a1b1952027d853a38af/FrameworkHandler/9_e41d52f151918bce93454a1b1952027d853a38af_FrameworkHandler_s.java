 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework;
 
 import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import cx.ath.mancel01.webframework.exception.BreakFlowException;
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.dependencyshot.graph.Binding;
 import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
 import cx.ath.mancel01.webframework.compiler.CompilationException;
 import cx.ath.mancel01.webframework.compiler.WebFrameworkClassLoader;
 import cx.ath.mancel01.webframework.http.Request;
 import cx.ath.mancel01.webframework.http.Response;
 import cx.ath.mancel01.webframework.integration.dependencyshot.DependencyShotIntegrator;
 import cx.ath.mancel01.webframework.routing.Router;
 import cx.ath.mancel01.webframework.routing.WebMethod;
 import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
 import cx.ath.mancel01.webframework.view.HtmlPage;
 import cx.ath.mancel01.webframework.view.Renderable;
 import cx.ath.mancel01.webframework.view.View;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author mathieuancelin
  */
 public class FrameworkHandler {
 
     private InjectorImpl injector;
     private WebBinder configBinder;
     private final FileGrabber grabber;
     private Map<String, Class> controllers;
     private Class rootController;
     private boolean started = false;
     private final String contextRoot;
     private final File base;
     private Class<? extends Binder> binderClass;
     private String binderClassName;
     private Router router;
 
     public FrameworkHandler(String binderClassName, String contextRoot, FileGrabber grabber) {
         this.binderClassName = binderClassName;
         controllers = new HashMap<String, Class>();
         if ("".equals(contextRoot)) {
             throw new RuntimeException("Can't have an empty context root");
         }
         this.contextRoot = contextRoot;
         this.grabber = grabber;
         this.base = grabber.getFile("public");
         this.router = new Router();
     }
 
     private void configureInjector(InjectorImpl inj) {
         inj.allowCircularDependencies(true);
         inj.registerShutdownHook();
         new DependencyShotIntegrator(inj).registerBindings();
     }
 
     public void validate() {
         if (rootController == null) {
             throw new RuntimeException("You need to register a root controler");
         } 
     }
 
     public void start() {
         WebFramework.init();
         try {
             if (!WebFramework.dev) {
                 this.binderClass =
                     (Class<? extends Binder>)
                         Class.forName(binderClassName);
             } else {
                 this.binderClass =
                     (Class<? extends Binder>)
                         new WebFrameworkClassLoader(getClass().getClassLoader())
                             .loadClass(binderClassName);
             }
             this.configBinder = (WebBinder) this.binderClass.newInstance();
             this.configBinder.setDispatcher(this);
             this.injector = DependencyShot.getInjector(configBinder);
             configureInjector(this.injector);
         } catch (Exception e) {
             throw new RuntimeException("Error at injector creation", e);
         }
         this.started = true;
     }
 
     public void stop() {
         this.started = false;
         this.injector.triggerLifecycleDestroyCallbacks();
     }
 
     public void registrerController(Class<?> clazz) {
         router.registrerController(clazz);
     }
 
     public Response process(Request request) {
         try {
             if (started) {
                 WebFramework.logger.trace("asked resource => {}", request.path);
                 Response res = new Response();
                 String path = request.path;
                 if (!"/".equals(contextRoot)) {
                    path = path.replace(contextRoot, "");
                 }
                 if (path.endsWith("favicon.ico")) {
                     path = "/public/img/favicon.ico";
                 }
                 if (path.startsWith("/public/")) {
                     File asked = new File(base, path.replace("/public/", ""));
                     res.direct = asked;
                     res.out = new ByteArrayOutputStream();
                     return res;
                 }
                 WebMethod webMethod = router.route(request, contextRoot);
                 return render(request, webMethod);
             } else {
                 throw new RuntimeException("Framework not started ...");
             }
         } catch (Throwable t) {
             final Throwable ex = t;
             t.printStackTrace();
             return new HtmlPage("Error"
                     , "<h1>Ooops, an error occured : "
                     + ex.getMessage() + "</h1>").render();
         }
     }
 
     private Response render(Request request, WebMethod webMethod) throws Exception {
         Class controllerClass = webMethod.getClazz();
         String methodName = webMethod.getMethod().getName();
         Binding devControllerBinding = null;
         InjectorImpl devInjector = null;
         if (WebFramework.dev) {
             try {
                 router.reset();
                 Class devBinderClass = new WebFrameworkClassLoader().loadClass(binderClassName);
                 WebBinder devBinder = (WebBinder) devBinderClass.newInstance();
                 devBinder.setDispatcher(this);
                 devInjector = DependencyShot.getInjector(devBinder);
                 controllerClass = new WebFrameworkClassLoader().loadClass(controllerClass.getName());
             } catch (Throwable ex) {
                 return createErrorResponse(ex);
             }
             devControllerBinding = new Binding(null, null, controllerClass, controllerClass, null, null);
         }
         long start = System.currentTimeMillis();
         Object controller = null;
         if (WebFramework.dev) {
             try {
                 controller = devControllerBinding.getInstance(devInjector, null);
                 //controller = controllerBinding.getInstance(injector, null);
             } catch (Throwable ex) {
                 return createErrorResponse(ex);
             }
         } else {
             controller = injector.getInstance(controllerClass);
         }
         WebFramework.logger.trace("controller injection : {} ms."
                 , (System.currentTimeMillis() - start));
         start = System.currentTimeMillis();
 
         Object ret = webMethod.invoke(request, controller);
         
         WebFramework.logger.trace("controller method invocation : {} ms."
                 , (System.currentTimeMillis() - start));
         if (ret instanceof Renderable) {
             Renderable renderable = (Renderable) ret;
             if (renderable instanceof View) { // ok that's not really OO but what the hell !
                 return ((View) renderable).render(methodName, controllerClass, grabber);
             }
             return renderable.render();
         } else {
             return new HtmlPage("Can't render", "<h1>Ooops, can't render an object of type : "
                     + ret.getClass().getName() + "</h1>").render();
         }
     }
 
     private Response createErrorResponse(Throwable t) {
         Throwable original = t;
         Throwable cause = null;
         while (cause == null) {
             t = t.getCause();
             if (t == null) {
                 break;
             }
             if (t instanceof CompilationException) {
                 cause = t;
             }
         }
         if (cause == null) { // TODO : error page with stacktrace
             return new HtmlPage("Error"
                 , "<h1>Error :</h1><br/>"
                 + original.getMessage().replace("\n", "<br/>")).render();
         }
         return new HtmlPage("Compilation error"
                 , "<h1>Compilation error :</h1><br/>"
                 + cause.getMessage().replace("\n", "<br/>")).render();
     }
 }
