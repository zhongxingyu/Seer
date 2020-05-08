 package controllers;
 
 import static module.GuiceHolder.injector;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.libs.Akka;
 import play.libs.F.Promise;
 import play.mvc.Controller;
 import play.mvc.Http.Context;
 import play.mvc.Result;
 
 import com.github.drashid.api.ApiOp;
 import com.github.drashid.api.Async;
 
 public class Api extends Controller {
 
   private static final Logger LOG = LoggerFactory.getLogger(Api.class);
   
  private static final String API_PATH = "controllers.api";
 
   private static final Map<String, Class<?>> nameMap = new HashMap<String, Class<?>>();
   
   static {
     Reflections refs = new Reflections(API_PATH);
     Set<Class<? extends ApiOp>> apiCalls = refs.getSubTypesOf(ApiOp.class);
     for(Class<? extends ApiOp> cls : apiCalls){
       if(nameMap.containsKey(cls.getSimpleName())){
         throw new IllegalStateException("Conflicting API simple class names " + cls.getSimpleName());
       }
       nameMap.put(cls.getSimpleName(), cls);
     }
   }
   
   public static Result api(String path) throws Exception {
     // Find class name
     StringBuffer sb = new StringBuffer(API_PATH).append(".").append(path.replace('/', '.'));
     try{
       Class<?> cls = Class.forName(sb.toString());
       return execute(cls);
     }catch(ClassNotFoundException cnf){
       return badRequest();
     }
   }
 
   public static Result named(String name){
     Class<?> apiCls = nameMap.get(name);
     if(apiCls == null){
       return badRequest();
     }
     return execute(apiCls);
   }
   
   private static Result execute(Class<?> apiCls){
     try {
       long startTime = System.currentTimeMillis();
       ApiOp op = (ApiOp)injector().getInstance(apiCls);
       
       if(op.getClass().isAnnotationPresent(Async.class)){
         return async(apiToPromise(Context.current(), op, startTime));
       } else {
         try {
           return op.call();
         } finally {
           LOG.info("Executed API [{}] in {} ms", op.getClass().getCanonicalName(), System.currentTimeMillis() - startTime);
         }
       }
     } catch (Exception e) {
       LOG.error("Could not execute API call", e);
       return internalServerError();
     }
   }
   
   private static Promise<Result> apiToPromise(final Context current, final ApiOp op, final long startTime) {
     return Akka.future(new Callable<Result>() {
       @Override
       public Result call() throws Exception {
         try {
           Context.current.set(current);
           return op.call();
         } finally {
           LOG.info("Executed API [{}] in {} ms", op.getClass().getCanonicalName(), System.currentTimeMillis() - startTime);
         }
       }
     });
   }
 
 }
