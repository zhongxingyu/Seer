 package vinna.samples.todo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vinna.Vinna;
 import vinna.VinnaContext;
 import vinna.interceptor.InterceptorAdapter;
 import vinna.response.ResponseBuilder;
 import vinna.response.StringResponse;
 import vinna.samples.todo.controller.ApiTodoController;
 import vinna.samples.todo.controller.TodoController;
 import vinna.samples.todo.model.Todo;
 
 import java.util.Map;
 
 public class TodoApp extends Vinna {
     private static final Logger logger = LoggerFactory.getLogger(TodoApp.class);
 
     @Override
     protected void routes(Map<String, Object> config) {
         get("/css/bootstrap.min.css").pass();
 
         get("/").withController(TodoController.class).list();
         get("/new").withController(TodoController.class).create();
         post("/new").withController(TodoController.class).create(req.param("title").asString(), req.param("description").asString());
 
         get("/api").withController(ApiTodoController.class).list();
         get("/api/{id: \\d+}").withController(ApiTodoController.class).show(param("id").asLong());
        post("/api").withController(ApiTodoController.class).create(custom(JacksonArgument.class).<Todo>asT());
 
        get("/create").withController(TodoController.class).create(custom(TodoParameter.class).asTodo());
        get("/create/{title}/{description}").withController(TodoController.class).create(custom(TodoParameter.class).asTodo());
     }
 
     @Override
     protected void registerCallback(Map<String, Object> config) {
 
         registerInterceptor(new InterceptorAdapter() {
             @Override
             public void beforeMatch(VinnaContext context) {
                 context.request.setAttribute("currentTime", System.currentTimeMillis());
             }
 
             @Override
             public void afterMatch(VinnaContext context) {
                 logger.debug("Route matching: {} ms", (System.currentTimeMillis() - (Long) context.request.getAttribute("currentTime")));
             }
 
             @Override
             public void afterExecute(VinnaContext context) {
                 logger.debug("Executing time: {} ms", (System.currentTimeMillis() - (Long) context.request.getAttribute("currentTime")));
             }
         });
 
         registerInterceptor(new InterceptorAdapter() {
 
             @Override
             public void afterMatch(VinnaContext context) {
                 if (!context.isResolved() && context.request.getPath().startsWith("/css")) {
                     context.abortWith(ResponseBuilder.pass());
                 } else if (!context.isResolved()) {
                     context.abortWith(new StringResponse("Route not found"));
                 }
             }
         });
     }
 }
