 package name.felixbecker.brokenaop.aspects;
 
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 @Aspect
 public class WorkingAspect {
 	
 	@Pointcut(value="execution(* name.felixbecker.brokenaop..*(..))")
 	public void allMethods() {}
 	
	@Before("allMethods()")
 	public void logAllMethodEnters() {
 		System.out.println("BrokenAspect: Method enter!");
 	}
 
 
 }
