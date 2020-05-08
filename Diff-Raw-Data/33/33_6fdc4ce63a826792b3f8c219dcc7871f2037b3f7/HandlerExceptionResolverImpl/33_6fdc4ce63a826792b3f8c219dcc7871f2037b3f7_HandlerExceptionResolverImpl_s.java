 package pk.ip.weather.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
 
 
 public class HandlerExceptionResolverImpl implements HandlerExceptionResolver
 {
     private View view;
 
     public HandlerExceptionResolverImpl(View view)
     {
         this.view = view;
     }
 
     @Override
     public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
     {
         int statusCode = 200;
        if(ex instanceof TypeMismatchException)
        {
             statusCode = 404;
        }
        else if(ex instanceof NoSuchRequestHandlingMethodException)
         {
             statusCode = 404;
         }
         else
         {
             statusCode = 500;
         }
 
         ModelAndView mav = new ModelAndView();
         
         mav.addObject("responseCode", statusCode);
         mav.setView(view);
         
         return mav;
     }
 }
