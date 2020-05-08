 package com.dianping.wizard.widget.interceptor;
 
 import com.dianping.wizard.config.Configuration;
 import com.dianping.wizard.widget.InvocationContext;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 
 /**
  * @author ltebean
  */
 public class ExceptionInterceptor implements Interceptor{
 
     private String handling="log";
 
     private Logger logger= Logger.getLogger(this.getClass());
 
     public ExceptionInterceptor() {
         this.handling = Configuration.get("exception","log",String.class);
     }
 
     @Override
     public String intercept(InvocationContext invocation) throws Exception {
         try {
             return invocation.invoke();
         } catch(Exception e) {
             if(handling.equals("print")){
                 invocation.setOutput(ExceptionUtils.getFullStackTrace(e));
                 return InvocationContext.SUCCESS;
             } else {
                logger.error("error in rendering widget: " + invocation.getWidget().name, e);
                 return InvocationContext.NONE;
             }
         }
     }
 }
