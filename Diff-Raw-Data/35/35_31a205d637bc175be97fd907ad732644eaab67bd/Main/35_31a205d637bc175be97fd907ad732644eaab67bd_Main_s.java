 package Global; /**
  * Author: artemlobachev
  * Date: 20.09.13
  * Time: 23:27
  */
 import Global.Imps.*;
 import Global.MsgSystem.MessageSystemImp;
 import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
 import org.eclipse.jetty.rewrite.handler.RewriteHandler;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 
 public class Main {
 
     private static final String MAIN_PAGE_ADDRESS = "/auth";
     private static final String STATIC_DIR = "static";
     private static final int DEFAULT_SERVER_PORT = 8080;
 
     private static final String TN_FRONTEND = "Frontend";
     private static final String TN_DB_SERVICE = "DB";
 
     /**
      * Создание объекта сервера.
      * Внутри осуществляется выбор номера порта.
      * @param args параметры запуска приложения.
      * @return объект Server.
      */
     private static Server makeServer(String args[]) {
         //проверяем наличие параметра порт.
         // Если не передан - запускаемся на порту по умолчанию(переменная DEFAULT_SERVER_PORT).
         int port;
         if (args.length < 1) {
             System.out.append("No port in parametrs. Using default port " + DEFAULT_SERVER_PORT + ".\n");
             port = DEFAULT_SERVER_PORT;
         } else {
             String portString = args[0];
             port = Integer.valueOf(portString);
             System.out.append("Starting at port: ").append(portString).append('\n');
         }
 
         return new Server(port);
     }
 
     /**
      * Создание и настройка обработчиков для внутренних нужд сервера.
      * @return Список обработчиков.
      */
     private static HandlerList makeServerHandlers() {
         ResourceHandler resource_handler = new ResourceHandler();
         resource_handler.setDirectoriesListed(false); // не показывать содержание директории при переходе по /
         resource_handler.setResourceBase(STATIC_DIR); //путь к папке статики от корня проекта
 
         //переадресация пользователя с / на нужный нам адрес
         RewriteHandler rewriteHandler = new RewriteHandler();
         rewriteHandler.setRewriteRequestURI(true);
         rewriteHandler.setRewritePathInfo(true);
         rewriteHandler.setOriginalPathAttribute("requestedPath");
 
         RedirectRegexRule rule = new RedirectRegexRule();
         rule.setRegex("/");  //здесь устанавливаем откуда перенаправлять
         rule.setReplacement(MAIN_PAGE_ADDRESS);  //а здесь - куда
         rewriteHandler.addRule(rule);
 
         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[] {rewriteHandler, resource_handler});
         return handlers;
     }
 
     public static void main(String args[]) throws Exception {
         MessageSystem ms = new MessageSystemImp();
        DBService dbService = new DBServiceImp(ms);
         Frontend frontend = new Frontend(ms);
 
         ThreadPool threadPool = new ThreadPoolImp();
         threadPool.startThread(frontend, TN_FRONTEND);
         threadPool.startThread(dbService, TN_DB_SERVICE);
 
         Server server = makeServer(args);
 
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.addServlet(new ServletHolder(frontend), "/*");
 
         HandlerList handlers = makeServerHandlers();
         handlers.addHandler(context);
         server.setHandler(handlers);
 
         server.start();
         server.join();
     }
 }
