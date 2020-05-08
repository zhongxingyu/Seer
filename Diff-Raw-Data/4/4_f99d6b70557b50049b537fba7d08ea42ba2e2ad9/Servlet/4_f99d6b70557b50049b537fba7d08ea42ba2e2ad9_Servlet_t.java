 // Copyright (c) 2010 Jeffrey D. Brennan
 //
 // License: http://www.opensource.org/licenses/mit-license.php
 
 package rainbow.gae;
 
 import rainbow.Console;
 import rainbow.functions.Environment;
 import rainbow.types.*;
 import rainbow.vm.VM;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Map;
 
 public class Servlet extends HttpServlet {
   private static final Pair NIL = ArcObject.NIL;
 
   public void init()
           throws ServletException {
     try {
       String uri = getInitParameter("init.uri");
       if (uri == null) uri = "/webapp.arc";
       System.out.println("init " + uri);
       ServletContext context = getServletContext();
       InputStream in = context.getResourceAsStream(uri);
       if (in == null) {
         throw new ServletException("Application not found: " + uri);
       }
       initRainbow(in);
       (Symbol.mkSym("*context*")).setValue(JavaObject.wrap(context));
     } catch (Exception ex) {
       throw new ServletException("Initialization failure", ex);
     }
   }
 
   static final Symbol RESPOND = Symbol.mkSym("respond");
 
   public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
     VM vm = new VM();
     System.out.println("inside doGet");
     if (request.getParameter("reload") != null) init(); // For rapid testing
 
     // we're going to invoke (def respond (str op args cooks ip)
     String uri = request.getRequestURI();
     System.out.println("doGet " + uri);
 
     Symbol op = Symbol.mkSym(uri.substring(1)); // uri "/foo" -> op "foo"
 
     Pair args = buildArgs(request.getParameterMap());
     System.out.println("args are " + args);
 
     Pair cooks = buildCookies(request.getCookies());
     System.out.println("cooks are " + cooks);
 
     ArcString ip = ArcString.make(request.getRemoteAddr());
     ArcObject respond = RESPOND.value();
     OutputStream str = new ServletOutput(response);
     try {
       respond.invokeAndWait(vm, Pair.buildFrom(new Output(new PrintStream(str)), op, args, cooks, ip));
     } catch (ThreadDeath td) {
       throw td;
     } catch (Throwable e) {
       new PrintStream(response.getOutputStream()).print(e);
     }
   }
 
   private Pair buildCookies(Cookie[] cookies) {
     Pair result = NIL;
    if (cookies == null) {
      return result;
    }

     for (Cookie cookie : cookies) {
       String name = cookie.getName();
       String value = cookie.getValue();
       Pair kv = Pair.buildFrom(ArcString.make(name), ArcString.make(value));
       result = new Pair(kv, result);
     }
     return result;
   }
 
   private Pair buildArgs(Map parameterMap) {
     Pair result = ArcObject.NIL;
     for (Object entry: parameterMap.keySet()) {
       String[] values = (String[]) parameterMap.get(entry);
       for (Object value : values) {
         Pair kv = Pair.buildFrom(ArcString.make(entry.toString()), ArcString.make(value.toString()));
         result = new Pair(kv, result);
       }
     }
     return result;
   }
 
   public void doPost(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
     doGet(request, response);
   }
 
   private void initRainbow(InputStream webapp)
           throws Exception {
     VM vm = new VM();
     if (!Symbol.mkSym("sig").bound()) {
       Environment.init();
       (Symbol.mkSym("*env*")).setValue(new Hash()); // Is this required?
       (Symbol.mkSym("call*")).setValue(new Hash());
       (Symbol.mkSym("sig")).setValue(new Hash());
       for (String file : new String[]{"arc",
               "strings",
               "lib/bag-of-tricks",
               "rainbow/rainbow",
               "rainbow/rainbow-gae-init"}) {
         // loadFile won't work in Tomcat since "." is not
         // the root of the war file in Tomcat.
         // But it will work in the App Engine dev server.
         // Not sure if it will work in the real App Engine (yet!)
         Console.loadFile(vm, new String[]{"lib"}, file);
       }
     }
     Console.load(vm, webapp);
   }
 
 }
