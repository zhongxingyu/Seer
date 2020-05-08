 package com.mbrindam.samples.aspect;
 
 import org.apache.log4j.Logger;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 
 @Aspect
 public class LoggingAspect {
 	
 	@Before("com.mbrindam.samples.aspect.SystemArchitectureAspect.inSamples()")
 	public void logBefore(JoinPoint joinPoint) {
  
 		Logger logger = Logger.getLogger(joinPoint.getTarget().getClass());
 		logger.debug("logBefore() is running!");
 		logger.debug("hijacked : " + joinPoint.getSignature().getName());
 		logger.debug("******");
 	}
 
 }
