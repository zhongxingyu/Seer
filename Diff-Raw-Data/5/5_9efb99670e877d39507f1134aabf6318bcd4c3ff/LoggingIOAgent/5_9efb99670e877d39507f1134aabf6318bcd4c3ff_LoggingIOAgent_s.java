 package org.spoofax.interpreter.library;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.Writer;
 
 /**
  * An IO Agent class that logs all console output.
  * 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LoggingIOAgent extends IOAgent {
     
     private final LoggingOutputStream stdoutLog = new LoggingOutputStream(System.out);
     
     private final LoggingOutputStream stderrLog = new LoggingOutputStream(System.err);
     
     private final PrintStream stdout = new PrintStream(stdoutLog, true);
     
     private final PrintStream stderr = new PrintStream(stderrLog, true);
     
     private final OutputStreamWriter stdoutWriter = new OutputStreamWriter(stdoutLog);
     
     private final OutputStreamWriter stderrWriter = new OutputStreamWriter(stderrLog);
     
     final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
     
     public String getLog() {
     	return bytes.toString();
     }
     
     public void clearLog() {
         bytes.reset();
     }
     
     @Override
     public OutputStream internalGetOutputStream(int fd) {
         switch (fd) {
             case CONST_STDOUT:
                stderrLog.stream = System.out; // might have changed
                 return stdout;
             case CONST_STDERR:
                 stderrLog.stream = System.err; // might have changed
                 return stderr;
             default:
                 return super.internalGetOutputStream(fd);
         }
     }
     
     @Override
     public Writer getWriter(int fd) {
         switch (fd) {
             case CONST_STDOUT:
                stderrLog.stream = System.out; // might have changed
                 return stdoutWriter;
             case CONST_STDERR:
                 stderrLog.stream = System.err; // might have changed
                 return stderrWriter;
             default:
                 return super.getWriter(fd);
         }
     }
     
     private class LoggingOutputStream extends OutputStream {
         OutputStream stream;
         
         public LoggingOutputStream(OutputStream stream) {
             this.stream = stream;
         }
         
         @Override
         public void write(int b) throws IOException {
             stream.write(b);
             bytes.write(b); 
         }
         
         @Override
         public void write(byte[] b) throws IOException {
             stream.write(b);
             bytes.write(b);
         }
         
         @Override
         public void write(byte[] b, int off, int len) throws IOException {
             stream.write(b, off, len);
             bytes.write(b, off, len);
         }
         
         @Override
         public void flush() throws IOException {
             stream.flush();
         }
         
         @Override
         public void close() throws IOException {
             // UNDONE: closing console streams is asking for trouble
             // if (stream != System.out && stream != System.err)
             //    stream.close();
             stream.flush();
         }
     }
 }
