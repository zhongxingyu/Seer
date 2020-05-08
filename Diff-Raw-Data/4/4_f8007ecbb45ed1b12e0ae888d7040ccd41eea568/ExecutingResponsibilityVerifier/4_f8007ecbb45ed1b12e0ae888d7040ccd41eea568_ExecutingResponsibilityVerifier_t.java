 /*
  * Created on 28-Dec-2003
  * 
  * (c) 2003-2004 ThoughtWorks
  * 
  * See license.txt for license details
  */
 package jbehave.framework;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import jbehave.framework.exception.BehaviourFrameworkError;
 import jbehave.framework.exception.VerificationException;
import jbehave.util.StringUtils;
 
 /**
  * Represents a verifier for a single responsibility, which can verify
  * itself and present the results of its verification.
  * 
  * @author <a href="mailto:dan@jbehave.org">Dan North</a>
  */
 public class ExecutingResponsibilityVerifier extends NotifyingResponsibilityVerifier {
     /**
      * We call the lifecycle methods <tt>setUp()</tt> and <tt>tearDown()</tt>
      * in the appropriate places if either of them exist.<br>
      */
     protected ResponsibilityVerification doVerifyResponsibility(Method method, Object instance) {
         ResponsibilityVerification result = null;
         String behaviourClassName = null;
         try {
             setUp(instance);
             method.invoke(instance, new Object[0]);
             result = createVerification(behaviourClassName, method.getName(), null);
         } catch (InvocationTargetException e) {
             // method failed
             result = createVerification(behaviourClassName, method.getName(), e.getTargetException());
         } catch (Exception e) {
             throw new BehaviourFrameworkError(
                     "Problem invoking " + method.getDeclaringClass().getName() + "#" + method.getName(), e);
         }
         finally {
             try {
                 tearDown(instance);
             } catch (InvocationTargetException e) {
                 // tearDown failed - override if result would have succeeded
                 if (result != null && result.succeeded()) {
                     result = createVerification(behaviourClassName, method.getName(), e.getTargetException());
                 }
             } catch (Exception e) {
                 // anything else is bad news
                 throw new BehaviourFrameworkError(e);
             }
         }
         return result;
     }
 
     private void setUp(Object behaviourClassInstance) throws InvocationTargetException {
         try {
             Method setUp = behaviourClassInstance.getClass().getMethod("setUp", new Class[0]);
             setUp.invoke(behaviourClassInstance, new Object[0]);
         } catch (NoSuchMethodException e) {
             // there wasn't a setUp() method - never mind
         } catch (InvocationTargetException e) {
             // setUp failed - rethrow it
             throw e;
         } catch (Exception e) {
             // anything else is bad news
             throw new BehaviourFrameworkError(e);
         }
     }
 
     private void tearDown(Object behaviourClassInstance) throws InvocationTargetException {
         try {
             Method tearDown = behaviourClassInstance.getClass().getMethod("tearDown", new Class[0]);
             tearDown.invoke(behaviourClassInstance, new Object[0]);
         } catch (NoSuchMethodException e) {
             // there wasn't a tearDown() method - never mind
         } catch (InvocationTargetException e) {
             // tearDown failed - rethrow it
             throw e;
         } catch (Exception e) {
             // anything else is bad news
             throw new BehaviourFrameworkError(e);
         }
     }
 
     /**
      * Create a {@link ResponsibilityVerification}, possibly based on an error condition.
      * 
      * This will be one of the following cases:
      * <ul>
      * <li>a {@link VerificationException}, which means the verification failed.</li>
      * <li>a {@link ThreadDeath}, which should always be propagated.</li>
      * <li>some other kind of exception was thrown.</li>
      * </ul>
      * 
      * @throws ThreadDeath if the target exception itself is a <tt>ThreadDeath</tt>.
      */
     private ResponsibilityVerification createVerification(String className, String methodName, Throwable targetException) {
         
         // propagate thread death otherwise Bad Things happen (or rather Good Things don't)
         if (targetException instanceof ThreadDeath) {
             throw (ThreadDeath)targetException;
         }
         else {
            return new ResponsibilityVerification(className, StringUtils.unCamelCase(methodName), targetException);
         }
     }
 }
