 package weka.subspaceClusterer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TestRunner {
 
     static String metrics, outputPath, dataSetFilename, command, classPath, javaExecutable;
     static int numProcessors;
     static ProcessBuilder procBuilder;
     static ArrayList<Process> runningProcs = new ArrayList<Process>();
 
     static void dispatch(List<String> commands) throws IOException, InterruptedException {
 
         // Keep the system busy, but don't overwhelm it.
         if (runningProcs.size() < numProcessors) {
             runningProcs.add(forkProcess(commands));
         }
 
         else {
 
             /*
              * TODO: Use a thread pool to manage each forked process, so that as soon as a process complted, the thread
              * is returned to the pool ready to be used. In the meanwhile, assume that the oldest process will be the
              * first to complete.
              */
             Process oldest = runningProcs.get(0);
             oldest.waitFor();
             runningProcs.remove(oldest);
             runningProcs.add(forkProcess(commands));
 
         }// else
     }// method
 
     static Process forkProcess(List<String> commands) throws IOException {
         if (procBuilder == null) {
             procBuilder = new ProcessBuilder();
             procBuilder.inheritIO();
         }
 
         procBuilder.command(commands);
         Process proc = procBuilder.start();
         return proc;
 
     }// method
 
     public static void main(String[] args) throws IOException, InterruptedException {
 
         // Set state
         numProcessors = Runtime.getRuntime().availableProcessors();
         metrics = "F1Measure:Accuracy:Entropy";
         outputPath = "C:\\results";
         classPath = "\\Users\\ahoffer\\Documents\\GitHub\\sepc\\workspace\\OpenSubspace\\lib\\*;";
         javaExecutable = "javaw.exe";
 
         // TODO: RUN EXPERIMENTS FOR MULTIPLE DATASETS
         // ArrayList<String> dataSetFilenames;
         dataSetFilename = "breast.arff";
 
         // Run tests
         run();
 
         // Pull all the results into one file
        Consolidator.consolidate(outputPath, outputPath + "\\results.csv");
 
         // Avoid the error
         // JDWP exit error AGENT_ERROR_NO_JNI_ENV
         System.exit(0);
 
     }
 
     static void run() throws IOException, InterruptedException {
         int experimentLabel = 1;
         List<List<String>> argLines = MoccaBuilder.getArgLines();
         System.out.printf("Number of experiments to run=%,d\n", argLines.size());
 
         for (List<String> args : argLines) {
 
             // Build final command line by prepending/appending as necessary.
             // PREPEND
             args.add(0, "weka.subspaceClusterer.MySubspaceClusterEvaluation");
             args.add(0, classPath);
             args.add(0, "-cp");
             args.add(0, javaExecutable);
             // APPEND
             args.add("-label");
             args.add("" + experimentLabel);
             args.add("-M");
             args.add(metrics);
             args.add("-path");
             args.add(outputPath);
             args.add("-c");
             args.add("last");
             args.add("-t");
             args.add(dataSetFilename);
 
             dispatch(args);
 
             // Set the ID for the next experiment to run
             experimentLabel++;
        }// for
     }// method
 }// class
