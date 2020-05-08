 package org.motechproject.ananya.kilkari.web.interceptors;
 
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 public class KilkariChannelInterceptor extends HandlerInterceptorAdapter {
 
     public static final String IVR_CHANNEL = "ivr";
     public static final String IVR_RESPONSE_FORMAT = "var response = ";
 
     @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, java.lang.Object handler) throws java.lang.Exception {
        String channelValue = request.getParameter("channel");
        if (channelValue != null && channelValue.equals(IVR_CHANNEL)) {
            response.getOutputStream().print(IVR_RESPONSE_FORMAT);
        }
         return true;
     }
 }
