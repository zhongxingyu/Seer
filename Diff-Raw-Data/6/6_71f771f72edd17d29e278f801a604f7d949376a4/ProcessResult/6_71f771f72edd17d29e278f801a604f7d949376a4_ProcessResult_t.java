 package org.skife.ssh;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.CharStreams;
 import com.google.common.io.InputSupplier;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Arrays;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.regex.Pattern;
 
 public class ProcessResult
 {
     private final int    exitCode;
     private final byte[] stdout;
     private final byte[] stderr;
 
     ProcessResult(int exitCode, byte[] stdout, byte[] stderr)
     {
         this.exitCode = exitCode;
         this.stdout = stdout;
         this.stderr = stderr;
     }
 
     static ProcessResult run(boolean inheritOut, boolean inheritErr, String... cmd) throws InterruptedException, IOException
     {
         ProcessBuilder pb = new ProcessBuilder().command(cmd);
 
         if (inheritOut) {
             pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
         }
 
         if (inheritErr) {
             pb.redirectError(ProcessBuilder.Redirect.INHERIT);
         }
 
         ExecutorService es = Executors.newFixedThreadPool(2);
         try {
             final Process p = pb.start();
             Future<byte[]> stdout = es.submit(new Callable<byte[]>()
             {
                 @Override
                 public byte[] call() throws Exception
                 {
                     return ByteStreams.toByteArray(p.getInputStream());
                 }
             });
 
             Future<byte[]> stderr = es.submit(new Callable<byte[]>()
             {
                 @Override
                 public byte[] call() throws Exception
                 {
                     return ByteStreams.toByteArray(p.getErrorStream());
                 }
             });
 
             int exit = p.waitFor();
             return new ProcessResult(exit, stdout.get(), stderr.get());
         }
         catch (ExecutionException e) {
             throw new RuntimeException(e);
         }
         finally {
             es.shutdown();
         }
 
 
     }
 
     ProcessResult explodeOnError()
     {
         if (exitCode != 0) {
             throw new RuntimeException(new String(stdout));
         }
         return this;
     }
 
     public int getExitCode()
     {
         return exitCode;
     }
 
     public byte[] getStdout()
     {
         return stdout;
     }
 
     public byte[] getStderr()
     {
         return stderr;
     }
 
     public InputSupplier<? extends Reader> getStdoutSupplier()
     {
         return CharStreams.newReaderSupplier(new String(stdout));
     }
 
     public InputSupplier<? extends Reader> getStderrSupplier()
     {
         return CharStreams.newReaderSupplier(new String(stderr));
     }
 
     public String getStdoutString() throws IOException
     {
         return CharStreams.toString(getStdoutSupplier());
     }
 
     public String getStderrString() throws IOException
     {
         return CharStreams.toString(getStderrSupplier());
     }
 
     @Override
     public String toString()
     {
         try {
             return getStdoutString();
         }
         catch (IOException e) {
             return "ProcessResult IOException: " + e.getMessage();
         }
     }
 
     public ProcessResult errorUnlessExitIn(int... exits)
     {
         for (int exit : exits) {
             if (this.exitCode == exit) {
                 return this;
             }
         }

        throw new CommandFailed("Expected exit code in " + Arrays.toString(exits) + " but it was " + exitCode + "\n" + new String(this.getStderr()));
     }
 
     public ProcessResult errorIfExitIn(int... exits)
     {
         for (int exit : exits) {
             if (this.exitCode == exit) {
                throw new CommandFailed("exit code " + exitCode + " in " + Arrays.toString(exits) + "\n" + new String(this.getStderr()));
             }
         }
         return this;
     }
 
 
     public ProcessResult errorUnlessStdoutContains(String needle) throws IOException
     {
         if (this.getStdoutString().contains(needle)) {
             return this;
         }
         else {
             throw new CommandFailed("Expected '" + needle + "' in stdout, but it was not present:\n"
                                     + getStdoutString());
         }
     }
 
     public ProcessResult errorIfStdoutContains(String needle) throws IOException
     {
         if (this.getStdoutString().contains(needle)) {
             throw new CommandFailed("Found '" + needle + "' in stdout:\n"
                                     + getStdoutString());
         }
         else {
             return this;
         }
     }
 
     public ProcessResult errorUnlessStderrContains(String needle) throws IOException
     {
         if (this.getStderrString().contains(needle)) {
             return this;
         }
         else {
             throw new CommandFailed("Expected '" + needle + "' in stderr, but it was not present:\n"
                                     + getStderrString());
         }
     }
 
     public ProcessResult errorIfStderrContains(String needle) throws IOException
     {
         if (this.getStderrString().contains(needle)) {
             throw new CommandFailed("Found '" + needle + "' in stderr:\n"
                                     + getStderrString());
         }
         else {
             return this;
         }
     }
 
 }
