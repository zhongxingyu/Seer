 package com.crowdstore.web.common.exceptions;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.validation.BindException;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.method.HandlerMethod;
 import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
 import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
 import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * @author fcamblor
  *         Exception resolver used to manage globally exception handlers : instead of having
  *         several @ExceptionHandler(X.class) in every @Controller,
  */
 public class GlobalExceptionHandlerMethodExceptionResolver extends ExceptionHandlerExceptionResolver {
 
     private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerMethodExceptionResolver.class);
 
     /**
      * Used because ExceptionHandlerMethodResolver instanciation costs performance
      */
     private static final ConcurrentMap<Class, ExceptionHandlerMethodResolver> CACHED_EXCEPTIONS_HANDLER_RESOLVERS = new ConcurrentHashMap<>();
 
     static {
         CACHED_EXCEPTIONS_HANDLER_RESOLVERS.putIfAbsent(GlobalExceptionHandlerMethodExceptionResolver.class,
                 new ExceptionHandlerMethodResolver(GlobalExceptionHandlerMethodExceptionResolver.class));
     }
 
     @Override
     // Instead of looking at handlerMethod.bean instance, looking at current exception resolver instance
     // which should contain @ExceptionHandler annotated methods
     protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
         Class<?> handlerMethodType = handlerMethod.getBeanType();
         if(!CACHED_EXCEPTIONS_HANDLER_RESOLVERS.containsKey(handlerMethodType)){
             // Surrounding the putIfAbsent() method with a containsKey test because instanciation of ExceptionHandlerMethodResolver
             // is costly
             CACHED_EXCEPTIONS_HANDLER_RESOLVERS.putIfAbsent(handlerMethodType,
                     new ExceptionHandlerMethodResolver(handlerMethodType));
         }
         Object handler = null;
         Method resolvedMethod = CACHED_EXCEPTIONS_HANDLER_RESOLVERS.get(handlerMethodType).resolveMethod(exception);
         if(resolvedMethod != null){
             handler = handlerMethod.getBean();
         } else {
             // If method handler is not found in handlerMethodType, falling back to current class
             // handling exceptions globally
             resolvedMethod = resolvedMethod != null ? resolvedMethod : CACHED_EXCEPTIONS_HANDLER_RESOLVERS.get(this.getClass()).resolveMethod(exception);
             if(resolvedMethod != null){
                 handler = this;
             }
         }
 
 
         return (resolvedMethod != null ? new ServletInvocableHandlerMethod(handler, resolvedMethod) : null);
     }
 
     // These @ExceptionHandler methods will be executed instead of the @Controller ones !
     @ExceptionHandler(BindException.class)
     @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
     public
     @ResponseBody
     List<ObjectError> handleBindingFailure(BindException exception) {
         return exception.getBindingResult().getAllErrors();
     }
 
     @ExceptionHandler(MethodArgumentNotValidException.class)
     @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
     public
     @ResponseBody
     List<ObjectError> handleBindingFailure(MethodArgumentNotValidException exception) {
         return exception.getBindingResult().getAllErrors();
     }
 
     @ExceptionHandler(Exception.class)
     @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
     public
     @ResponseBody
     void handleException(HttpServletRequest request, Exception exception) {
         logger.error("Error on "+request.getRequestURI(), exception);
     }
 }
