 package org.youfood.resources.async;
 
 import org.youfood.services.NotificationService;
import org.youfood.services.NotificationServiceListener;
 
 import javax.ejb.EJB;
 import javax.servlet.AsyncContext;
 import javax.servlet.AsyncEvent;
 import javax.servlet.AsyncListener;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
 @WebServlet(urlPatterns = {"/notifications"}, asyncSupported = true)
 public class OrderNotification extends HttpServlet{
 
     @EJB
    private NotificationServiceListener notificationService;
 
     private Map<String, AsyncContext> asyncContexts = new ConcurrentHashMap<String, AsyncContext>();
     private BlockingQueue<String> messages = new LinkedBlockingQueue<String>();
     private Thread notifier = new Thread(new Runnable() {
         @Override
         public void run() {
             while (true) {
                 try {
                     String message = messages.take();
                     for (AsyncContext asyncContext : asyncContexts.values()) {
                         try {
                             sendMessage(message, asyncContext);
                         } catch (Exception e) {
                             asyncContexts.values().remove(asyncContext);
                         }
                     }
                 } catch (InterruptedException e) {
                     break;
                 }
             }
         }
     });
 
     @Override
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         notificationService.setMessages(messages);
         notifier.start();
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         response.setContentType("text/plain");
         response.setCharacterEncoding("utf-8");
         response.setHeader("Acces-Control-Allow-Origin", "*");
 
         PrintWriter writer = response.getWriter();
         final String id = UUID.randomUUID().toString();
         writer.print(id);
         writer.print(';');
         // Padding
         for (int i = 0; i < 1024; i++) {
             writer.print(' ');
         }
         writer.print(';');
         writer.flush();
         final AsyncContext ac = request.startAsync();
         ac.addListener(new AsyncListener() {
             public void onComplete(AsyncEvent event) throws IOException {
                 asyncContexts.remove(id);
             }
 
             public void onTimeout(AsyncEvent event) throws IOException {
                 asyncContexts.remove(id);
             }
 
             public void onError(AsyncEvent event) throws IOException {
                 asyncContexts.remove(id);
             }
 
             public void onStartAsync(AsyncEvent event) throws IOException {
             }
         });
         asyncContexts.put(id, ac);
     }
 
     private void sendMessage(String message, AsyncContext asyncContext) throws IOException {
         PrintWriter writer = asyncContext.getResponse().getWriter();
         writer.print(message);
         writer.flush();
     }
 }
