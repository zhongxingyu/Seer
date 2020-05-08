 package pk.ip.weather.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 
 
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
        
        if(ex instanceof TypeMismatchException || ex instanceof NoSuchRequestHandlingMethodException || ex instanceof EmptyResultDataAccessException)
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
