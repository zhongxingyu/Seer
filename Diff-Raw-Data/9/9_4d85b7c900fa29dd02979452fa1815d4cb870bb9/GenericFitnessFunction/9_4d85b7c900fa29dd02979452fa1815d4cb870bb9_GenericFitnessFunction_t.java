 package de.ubercode.javaunitevolution.core;
 
 import org.apache.log4j.*;
 import org.jgap.gp.*;
 import org.junit.runner.*;
 import org.junit.runner.notification.*;
 
 /**
  * A fitness function that will invoke the supplied test case to measure
  * program fitness.
  */
 class GenericFitnessFunction extends GPFitnessFunction {
     private static Logger LOGGER =
         Logger.getLogger(GenericFitnessFunction.class);
     private static final long serialVersionUID = 1L;
     private Class<?> testClass;
 
     /**
      * Creates a new fitness function using the supplied test class.
      * @param testClass The class containing the test cases used to assess
      *                  the fitness of a program.
      */
     public GenericFitnessFunction(Class<?> testClass) {
         this.testClass = testClass;
     }
 
     @Override
     protected double evaluate(IGPProgram subject) {
         JavaUnitEvolution.currentProgram = subject;
         Result result = JUnitCore.runClasses(testClass);
         if (result.getFailureCount() == 0)
             return 0.0;
         double delta = 0.0;
         for (Failure failure : result.getFailures()) {
             Throwable t = failure.getException();
             if (t instanceof AssertionError)
                 delta += extractDelta((AssertionError) t);
         }
         LOGGER.debug("Calculated delta: " + delta);
         return delta;
     }
 
     /**
      * Extracts the delta between the two values of an error.
      * @param error The error from which to extract the delta.
      * @return The calculated delta.
      */
     private double extractDelta(AssertionError error) {
         // XXX: Make this more robust.
         String message = error.getMessage();
         LOGGER.debug("Extracting assertion message: " + message);
         String expected = message.substring(message.indexOf("<") + 1,
                                             message.indexOf(">"));
         String actual = message.substring(message.lastIndexOf("<") + 1,
                                           message.lastIndexOf(">"));
         try {
             return Math.abs(Double.valueOf(expected) - Double.valueOf(actual));
         } catch (NumberFormatException e1) {
             try {
                return Math.abs(Float.valueOf(expected)
                                - Float.valueOf(actual));
             } catch (NumberFormatException e2) {
                 try {
                    return Math.abs(Integer.valueOf(expected)
                                    - Integer.valueOf(actual));
                 } catch (NumberFormatException e3) {
                     // TODO: Try other data types
                     return 1.0;
                 }
             }
         }
     }
 }
