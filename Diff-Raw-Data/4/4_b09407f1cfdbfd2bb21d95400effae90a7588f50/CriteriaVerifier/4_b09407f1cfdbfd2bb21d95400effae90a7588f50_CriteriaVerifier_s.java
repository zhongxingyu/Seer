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
 
 /**
  * Represents a verifier for a single criteria, which can verify
  * itself and present the results of its verification.
  * 
  * @author <a href="mailto:dan@jbehave.org">Dan North</a>
  */
 public class CriteriaVerifier {
     private final Class spec;
     private final Method method;
     private final Object specInstance;
 
     public CriteriaVerifier(Method method) {
         try {
             this.method = method;
             this.spec = method.getDeclaringClass();
             this.specInstance = spec.newInstance();
         }
         catch (Exception e) {
             throw new BehaviourFrameworkError("Unable to instantiate instance of " + method.getDeclaringClass().getName());
         }
     }
 
     public CriteriaVerifier(Method method, Object specInstance) {
         this.method = method;
         this.spec = method.getDeclaringClass();
         this.specInstance = specInstance;
     }
 
     public String getName() {
         return method.getName();
     }
     
     public String getSpecName() {
         String className = spec.getName();
         int lastDot = className.lastIndexOf('.');
         return className.substring(lastDot + 1);
     }
     
     public Class getSpec() {
         return spec;
     }
 
     /**
     * Verify an individual criteria.<br>
     * <br>
      * We call the lifecycle methods <tt>setUp</tt> and <tt>tearDown</tt>
      * in the appropriate places if either of them exist.<br>
      * <br>
      * The {@link Listener} is alerted before and after the verification,
      * with calls to {@link Listener#criteriaVerificationStarting(CriteriaVerifier)
      * beforeCriteriaVerificationStarts(this)} and
      * {@link Listener#criteriaVerificationEnding(CriteriaVerification)
      * afterCriteriaVerificationEnds(result)} respectively.
      */
     public CriteriaVerification verifyCriteria(Listener listener) {
         CriteriaVerification result = null;
         try {
             listener.criteriaVerificationStarting(this);
             setUp();
             method.invoke(specInstance, new Object[0]);
             result = createVerification(null);
         } catch (InvocationTargetException e) {
             // method failed
             result = createVerification(e.getTargetException());
         } catch (Exception e) {
             throw new BehaviourFrameworkError(
                     "Problem invoking " + spec.getName() + "#" + method.getName(), e);
         }
         finally {
             try {
 				tearDown();
 			} catch (InvocationTargetException e) {
                 // tearDown failed - override if result would have succeeded
                 if (result != null && result.succeeded()) {
                     result = createVerification(e.getTargetException());
                 }
             } catch (Exception e) {
                 // anything else is bad news
                 throw new BehaviourFrameworkError(e);
             }
         }
         listener.criteriaVerificationEnding(result);
         return result;
     }
 
 	private void setUp() throws InvocationTargetException {
         try {
             Method setUp = getSpec().getMethod("setUp", new Class[0]);
             setUp.invoke(specInstance, new Object[0]);
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
 
     private void tearDown() throws InvocationTargetException {
         try {
             Method tearDown = getSpec().getMethod("tearDown", new Class[0]);
             tearDown.invoke(specInstance, new Object[0]);
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
      * Create a {@link CriteriaVerification}, possibly based on an error condition.
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
     private CriteriaVerification createVerification(Throwable targetException) {
         
         // propagate thread death otherwise Bad Things happen (or rather Good Things don't)
         if (targetException instanceof ThreadDeath) {
             throw (ThreadDeath)targetException;
         }
         else {
             return new CriteriaVerification(method.getName(), spec.getName(), targetException);
         }
     }
     
     public String toString() {
         return "[CriteriaVerifier: " + getSpecName() + "." + method.getName() + "]";
     }
 }
