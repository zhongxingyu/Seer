 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nttdata.masterthesis.javabackend.interceptor;
 
 import java.util.Arrays;
 import java.util.Date;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.InvocationContext;
 import javax.ws.rs.core.Response;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author MATHAF
  */
 // @Interceptor //  it's not necessary for @Interceptors() bindings, and in a CDI deployment must specify bindings (as per the spec).
 public class ServicesLoggingInterceptor {
     
     static final Logger LOG = LoggerFactory.getLogger(ServicesLoggingInterceptor.class);
     
     /**
     * Interceptor Methode logged die übergebenen Parameter, falls Parameter existieren. Im zweiten Schritt wird die
     * Interceptor-Chain aufgerufen und die Zeit der benötigten dauer gemessen, diese wir zusammen mit dem Rückgabewert
     * in das Log-File geschrieben. Tritt eine Exception auf, so wird diese ins Log geschrieben und weitergeworfen.
     * 
     * @param ctx
     *            Interception Context
     * @return proceed object for call chain
     * @throws Exception
     *             Exception
     */
    @AroundInvoke
    public Object log( InvocationContext ctx ) throws Exception
    {
            Class<?> callerClass = ctx.getMethod().getDeclaringClass();
 
            Object ret = null;
            long duration = -1;
 
            Object[] parameters = ctx.getParameters();
            String stringParam = Arrays.toString( parameters );
 
            if (parameters.length > 0)
            {
                if(LOG.isDebugEnabled()){
                    Object[] args = { callerClass.getName(), ctx.getMethod().getName(), stringParam };
                    LOG.debug("Class: {}, Method: {}, Parameters: {}", args );
                }
            }
            try
            {
                    long startMethod = new Date().getTime();
                    ret = ctx.proceed();
                    long endMethod = new Date().getTime();
 
                    duration = endMethod - startMethod;
            }
            catch (Exception e)
            {
                if(LOG.isErrorEnabled()){
                    Object[] args = {callerClass.getName(), ctx.getMethod().getName(),e.getMessage()};
                    LOG.error( "Class: {}, Method: {}, {}", args);
                }
                    throw e;
            }

           Object[] args = {callerClass.getName(), ctx.getMethod().getName(), ((Response)ret).getEntity(), new Long(duration)};
            
            if(LOG.isErrorEnabled()){
                 LOG.info("Class: {}, Method: {}, Return: {}, Durration: {} ms", args);
            }
 
            return ret;
    }
 }
