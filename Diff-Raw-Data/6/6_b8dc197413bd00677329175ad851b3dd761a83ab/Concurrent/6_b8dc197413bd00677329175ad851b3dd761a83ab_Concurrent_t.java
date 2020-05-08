 package controllers;
 
 import integrations.Integration;
 import integrations.News;
 import integrations.Results;
 import integrations.Schedule;
 import play.mvc.*;
 
 import java.util.concurrent.*;
 
 public class Concurrent extends Controller {
     public static News news = new News();
     public static Results results = new Results();
     public static Schedule schedule = new Schedule();
 
 
     public static void index() throws InterruptedException, ExecutionException {
         long start = System.currentTimeMillis();
 
        Executor executor = Executors.newFixedThreadPool(3);
         ExecutorCompletionService<Result> cs = new ExecutorCompletionService<Result>(executor);
 
         submitServiceCall(cs, "n", news);
         submitServiceCall(cs, "r", results);
         submitServiceCall(cs, "s", schedule);
 
         for (int i = 0; i < 3; i++) {
            Future<Result> future = cs.take();
             Result result = future.get();
             renderArgs.put(result.name, result.value);
         }
 
         long time = System.currentTimeMillis() - start;
         renderTemplate("index.html", time);
     }
 
     private static void submitServiceCall(ExecutorCompletionService<Result> cs, final String name, final Integration service) {
         cs.submit(new Callable<Result>() {
             public Result call() throws Exception {
                 return new Result(name, service.requestData());
             }
         });
     }
 
     public static class Result {
         public final String name;
         public final String value;
 
         public Result(String name, String value) {
             this.name = name;
             this.value = value;
         }
     }
 }
