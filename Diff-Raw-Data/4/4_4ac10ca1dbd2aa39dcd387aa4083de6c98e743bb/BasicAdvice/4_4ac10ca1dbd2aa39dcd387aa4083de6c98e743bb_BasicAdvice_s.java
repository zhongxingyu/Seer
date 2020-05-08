 package bonfire.springaop.aspects;
 
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 
 @Aspect
 public class BasicAdvice {
	//@Before("bonfire.springaop.aspects.BasicAspect.anyOldTransfer()")
	@Before("execution(* transfer(..))")
 	public void doAccessCheck() {
 		System.out.println("This is the proof that the advice ran");
 	}
 }
