 package es.gob.catastro.service.core.aop;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 
 @Aspect
 public class LoggerInterceptor {
 
 	private static final Log log = LogFactory.getLog(LoggerInterceptor.class);
 
 
 	@Around("this(java.rmi.Remote)")
 	public Object executeCall(ProceedingJoinPoint pjp) throws Throwable {
 		String call = pjp.getTarget().getClass().getName()+"."+pjp.getSignature().getName();
 
 		log.debug("CALL: " + call);
 		try {
 			long ini = System.currentTimeMillis();
 			Object obj = pjp.proceed();
 			long fin = System.currentTimeMillis();
 			log.info("OK: " + call+" duracion llamada (mseg): "+(fin-ini));
 			return obj;
		} catch (Throwable ex) {
 			log.info("ERR: " + call, ex);
			throw ex;
 		}
 	}
 
 }
