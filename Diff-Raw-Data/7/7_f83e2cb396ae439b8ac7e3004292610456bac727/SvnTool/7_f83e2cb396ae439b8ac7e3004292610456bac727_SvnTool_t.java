 package net.paissad.paissadtools.svn;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import net.paissad.paissadtools.api.ITool;
 import net.paissad.paissadtools.svn.exception.SVNToolException;
 
 import org.tmatesoft.svn.cli.SVN;
 
 /**
  * SVN tool.
  * 
  * @author paissad
  */
 public class SvnTool implements ITool {
 
     private final ProcessBuilder processBuilder;
 
     public SvnTool() {
         this.processBuilder = new ProcessBuilder(new String[] {});
     }
 
     /**
      * Sets the working directory to use before executing a command.
      * 
      * @param directory - The directory where to change to.
      * @throws IllegalArgumentException If the specified argument is null or is
      *             not a directory.
      * @see #execute(String[])
      */
     public void cd(final File directory) throws IllegalArgumentException {
         if (directory == null || !directory.isDirectory()) {
             throw new IllegalArgumentException("Must not be null and must be a directory.");
         }
         this.processBuilder.directory(directory);
     }
 
     /**
      * Returns the current working directory from where this tool will execute
      * the command.
      * 
      * @return The current working directory.
      */
     public String getCwd() {
         final File cwd = this.processBuilder.directory();
         return (cwd != null) ? cwd.getAbsolutePath() : System.getProperty("user.dir");
     }
 
     /**
      * <p>
      * This method is a proxy to the real command line tool served by this class
      * {@link SVN}.
      * </p>
      * <p>
      * It acts as the same as the SVN command line used from the command line
      * interfaces (CLI).
      * </p>
     * <p>
     * The returned {@link SvnToolResult} will contain the STDOUT & STDERR
     * outputs & the exit code status.
     * </p>
      * 
      * @param args - The arguments to pass to the SVN command line tool.
      * @return The result.
      * @throws IllegalArgumentException If the specified argument is
      *             <code>null</code>.
      * @throws SVNToolException If an error while executing the SVN command.
      * @see #execute(String[], OutputStream, OutputStream)
      */
     public SvnToolResult execute(final String[] args) throws IllegalArgumentException, SVNToolException {
 
         if (args == null) throw new IllegalArgumentException("The arguments cannot be null.");
         try {
             final List<String> command = this.buildCommand(args);
             this.processBuilder.command(command);
             final Process process = this.processBuilder.start();
             final int exitCode = process.waitFor();
             return new SvnToolResult(exitCode, process.getInputStream(), process.getErrorStream());
 
         } catch (final Exception e) {
             throw new SVNToolException("Error while executing the SVN command.", e);
         }
     }
 
     /**
      * <p>
      * This method is a proxy to the real command line tool served by this class
      * {@link SVN}.
      * </p>
      * <p>
      * It acts as the same as the SVN command line used from the command line
      * interfaces (CLI).
      * </p>
      * 
      * @param args - The arguments to pass to the SVN command line tool.
      * @param stdout - Where to send the STDOUT result. May be <tt>null</tt>.
      * @param stderr - Where to send the STDERR result. May be <tt>null</tt>.
      * @return The exit code. <span style='color:orange'><b>NOTE : </b></span>By
      *         convention, an exit code of '0' means that the command exited
      *         successfully. But it is not entirely guaranteed this will be
      *         always the case.
      * @throws IllegalArgumentException If the specified argument is
      *             <code>null</code>.
      * @throws SVNToolException If an error while executing the SVN command.
      * @see #execute(String[])
      */
     public int execute(final String[] args, final OutputStream stdout, final OutputStream stderr)
             throws IllegalArgumentException, SVNToolException {
 
         if (args == null) throw new IllegalArgumentException("The arguments cannot be null.");
         try {
             Lock lock = null;
             if (stdout != null && stderr != null && stdout.equals(stderr)) {
                 lock = new ReentrantLock(true);
             }
             final List<String> command = this.buildCommand(args);
             this.processBuilder.command(command);
             final Process process = this.processBuilder.start();
             this.copyStreamAsync(process.getInputStream(), stdout, lock);
             this.copyStreamAsync(process.getErrorStream(), stderr, lock);
             return process.waitFor();
 
         } catch (final Exception e) {
             throw new SVNToolException("Error while executing the SVN command.", e);
         }
     }
 
     private List<String> buildCommand(final String[] svnArgs) {
         final List<String> command = new LinkedList<String>();
         final String classpath = System.getProperty("java.class.path");
         command.add("java");
         command.add("-cp");
         command.add(classpath);
         command.add(SVN.class.getName());
         for (final String arg : svnArgs) {
             command.add(arg);
         }
         return command;
     }
 
     /**
      * Copy an InputStream to an OutputStream
      * 
      * @param in
      * @param out
      * @param lock - If not null, the lock will be used to prevent concurrent
      *            write on the OutputStream. If for example, the STDERR & STDOUT
      *            are served by the same OutputStream, it is by far wiser to
      *            make the writing operation quite fair.
      */
     private void copyStreamAsync(final InputStream in, final OutputStream out, final Lock lock) {
         if (in == null || out == null) return;
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 final byte data[] = new byte[8192];
                 int bytesRead;
                 try {
                     while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                         try {
                             if (lock != null) {
                                while (!lock.tryLock(1, TimeUnit.MILLISECONDS)) {
                                     // retry lock acquisition
                                 }
                             }
                             out.write(data, 0, bytesRead);
                         } finally {
                             if (lock != null) lock.unlock();
                         }
                     }
                     out.flush();
                 } catch (IOException e) { // do nothing
                 } catch (InterruptedException e) { // do nothing
                 }
             }
         }).start();
     }
 
 }
