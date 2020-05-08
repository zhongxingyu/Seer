import com.sun.xml.internal.ws.wsdl.writer.document.http.Operation;
 import pl.gajowy.kernelEstimator.*;
 
import javax.swing.text.html.Option;

 public class Main {
 
     public static void main(String[] args) {
         Arguments arguments;
         try {
             arguments = new Arguments(args);
             computeAndShowResults(arguments);
         } catch (InvalidProgramArgumentsException e) {
             System.err.println(e.getMessage());
             System.err.println(Arguments.getUsageMessage());
         }
     }
 
     private static void computeAndShowResults(Arguments arguments) {
         float[] dataPoints = arguments.getDataPoints();
         SamplingSettings samplingSettings = arguments.getSampling();
         float h = arguments.getBandwidth();
 
         KernelEstimatorSampling kernelEstimatorSampling = new KernelEstimatorSampling(h, dataPoints, samplingSettings);
         CalculationOutcome calculationOutcome = kernelEstimatorSampling.calculateUsing(new OpenCLTwoDEstimationEngine());
         writeOut(calculationOutcome.getEstimationPoints());
         if (arguments.showTimesDefined()) {
             System.out.println("Time: " + calculationOutcome.getElapsedTime() / 1000);
             Long profiledTime = calculationOutcome.getProfiledTime();
             System.out.println("Time (from profiling): " + (profiledTime == null ? "NA" : "" + (profiledTime / 1000)));
         }
         if (arguments.verifyFlagDefined()) {
             VerificationOutcome verificationOutcome = new Verifier().verify(kernelEstimatorSampling, calculationOutcome);
             System.out.println(verificationOutcome);
         }
     }
 
     private static void writeOut(float[] estimationPoints) {
         for (int i = 0; i < estimationPoints.length; i++) {
             float estimate = estimationPoints[i];
             System.out.println(estimate);
         }
     }
 }
