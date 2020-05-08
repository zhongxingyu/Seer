 package fr.fcamblor.demos.sbjd.web.exceptions;
 
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
 
 import java.util.List;
 
 /**
  * @author fcamblor
  * Exception resolver used to manage globally exception handlers : instead of having
  * several @ExceptionHandler(X.class) in every @Controller,
  */
 public class GlobalExceptionHandlerMethodExceptionResolver extends ExceptionHandlerExceptionResolver {
 
     private static final ExceptionHandlerMethodResolver CURRENT_CLASS_EXCEPTION_HANDLER_RESOLVER =
             new ExceptionHandlerMethodResolver(GlobalExceptionHandlerMethodExceptionResolver.class);
 
     @Override
     // Instead of looking at handlerMethod.bean instance, looking at current exception resolver instance
     // which should contain @ExceptionHandler annotated methods
    	protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
        return new ServletInvocableHandlerMethod(this, CURRENT_CLASS_EXCEPTION_HANDLER_RESOLVER.resolveMethod(exception));
     }
 
     // These @ExceptionHandler methods will be executed instead of the @Controller ones !
     @ExceptionHandler(BindException.class)
     @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
     public @ResponseBody
     List<ObjectError> handleBindingFailure(BindException exception) {
         return exception.getBindingResult().getAllErrors();
     }
 
     @ExceptionHandler(MethodArgumentNotValidException.class)
     @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
     public @ResponseBody
     List<ObjectError> handleBindingFailure(MethodArgumentNotValidException exception) {
         return exception.getBindingResult().getAllErrors();
     }
 }
