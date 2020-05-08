 /*
  * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
  * All rights reserved
  */
 
 package pl.touk.framework.logging.aop;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.AfterThrowing;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 
 import pl.touk.framework.logging.logGetters.PointcutLogGetterInterface;
 import pl.touk.framework.logging.logGetters.SignatureDeclaringTypeLogGetter;
 import pl.touk.security.context.SecurityContextInterface;
 
 import java.util.Collection;
 
 /**
  * Aspect (in AOP sense) responsible for logging.
  * Whenever you want to log anything, use this aspect. Do not use logging (log4j, commons.logging) directly.
  *
  * @author witek wolejszo
  * @author <a href="mailto:jnb@touk.pl">Jakub Nabrdalik</a>.
  */
 @Aspect
 public class LogAspect {
 
     protected SecurityContextInterface securityContext;
     
     //Default implementation, useful when LogAspect is not instantiated in Spring
     protected PointcutLogGetterInterface logGetter = new SignatureDeclaringTypeLogGetter();
 
     public LogAspect() {
     }
 
     /**
      * Logs entrance to annotated method.
      *
      * @param joinPoint JoinPoint automatically filled by aspectj
      */
     @Before(value = "@annotation(pl.touk.framework.logging.aop.LogMethodEntranceInfo)")
     public void logMethodEntranceInfo(JoinPoint joinPoint) {
         Log log = getLogGetter().getLog(joinPoint);
 
         if (log.isInfoEnabled()) {
         	
     		StringBuilder sb = new StringBuilder();
 			
 			sb.append("entering  ---------------------\n");
 			sb.append("  method: ").append(joinPoint.getSignature().getName()).append("\n");
 			sb.append("      at: ").append(joinPoint.getSourceLocation().getWithinType()).append("\n");
 
             String argStringValue = "";
             for (Object arg : joinPoint.getArgs()) {
                 // TODO: dekompozycja do obiektow, parametr adnotacji powinien to definiować.
                 if (arg == null) {
                     argStringValue = "NULL";
                 } else if (arg instanceof Collection) {
                     argStringValue = buildStringValue((Collection) arg);
                 } else {
                     argStringValue = buildStringValue(arg);
                 }
 
                 sb.append("   w/arg: ").append(argStringValue).append("\n");
             }
             
             sb.append("         ---------------------");
 			log.info(sb.toString());
         }
 
     }
 
     /**
      * Logs exit from annotated method.
      * @param joinPoint JoinPoint automatically filled by aspectj
      */
 	@AfterReturning(value = "@annotation(pl.touk.framework.logging.aop.LogMethodExitInfo)", returning = "retValue")
 	public void logMethodExitInfo(JoinPoint joinPoint, Object retValue) {
 		Log log = getLogGetter().getLog(joinPoint);
 
 		if (log.isInfoEnabled()) {
 			
 			StringBuilder sb = new StringBuilder();
 			
 			sb.append("leaving  ---------------------\n");
 			sb.append(" method: ").append(joinPoint.getSignature().getName()).append("\n");
 			sb.append("     at: ").append(joinPoint.getSourceLocation().getWithinType()).append("\n");
 			sb.append("  value: ").append(retValue).append("\n");
 			sb.append("         ---------------------");
 			
 			log.info(sb.toString());
 		}
 
 	}
 
     /**
      * Logs method exception.
      * @param joinPoint JoinPoint automatically filled by aspectj
      */
 	@AfterThrowing(value = "@annotation(pl.touk.framework.logging.aop.LogMethodExceptionError)", throwing = "throwable")
 	public void logMethodExceptionError(JoinPoint joinPoint, Throwable throwable) {
 		Log log = getLogGetter().getLog(joinPoint);
 
 		if (log.isErrorEnabled()) {
 			
 			StringBuilder sb = new StringBuilder();
 			sb.append("exception  ---------------------\n");
 			sb.append("   method: ").append(joinPoint.getSignature().getName()).append("\n");
 			sb.append("       at: ").append(joinPoint.getSourceLocation().getWithinType()).append("\n");
 			sb.append("  message: ").append(throwable.getMessage()).append("\n");
 			sb.append("           ---------------------");
 			
 			log.error(sb.toString(), throwable);
 		}
 
 	}
 	
     protected String buildStringValue(Object arg) {
         return new ReflectionToStringBuilder(arg, ToStringStyle.MULTI_LINE_STYLE).toString();
     }
 
     protected String buildStringValue(Collection arg) {
         String argStringValue = arg.getClass().getSimpleName() + " [ ";
 
         Collection collectionToIterate = (Collection) arg;
         for (Object o : collectionToIterate) {
             argStringValue += new ReflectionToStringBuilder(o, ToStringStyle.MULTI_LINE_STYLE).toString();
         }
 
         argStringValue += " ] \n";
         return argStringValue;
     }
 
     //setters and getters
 
     protected SecurityContextInterface getSecurityContext() {
         return securityContext;
     }
 
     public void setSecurityContext(SecurityContextInterface securityContext) {
         this.securityContext = securityContext;
     }
 
     protected PointcutLogGetterInterface getLogGetter() {
         return logGetter;
     }
 
     public void setLogGetter(PointcutLogGetterInterface logGetter) {
         this.logGetter = logGetter;
     }
 
 }
