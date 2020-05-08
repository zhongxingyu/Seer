 package jp.gr.java_conf.neko_daisuki.fsyscall.slave;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Logging;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 
 public class Application {
 
     private static class Pipe {
 
         private PipedInputStream mIn;
         private PipedOutputStream mOut;
 
         public Pipe() throws IOException {
             mIn = new PipedInputStream();
             mOut = new PipedOutputStream(mIn);
         }
 
         public InputStream getInput() {
             return mIn;
         }
 
         public OutputStream getOutput() {
             return mOut;
         }
     }
 
     private static Logging.Logger mLogger;
 
     private List<Worker> mWorkers;
     private int mExitStatus;
 
     private Collection<Slave> mSlavesToRemove;
 
     public Application() {
         mWorkers = new LinkedList<Worker>();
         mExitStatus = 0;
 
         mSlavesToRemove = new LinkedList<Slave>();
     }
 
     public void removeSlave(Slave slave) {
         mSlavesToRemove.add(slave);
     }
 
     public void addWorker(Worker worker) {
         mWorkers.add(worker);
     }
 
     public void setExitStatus(int exitStatus) {
         mExitStatus = exitStatus;
     }
 
     public int run(InputStream in, OutputStream out) throws IOException, InterruptedException {
         mLogger.info("starting a slave application");
 
         Pipe slave2hub = new Pipe();
         Pipe hub2slave = new Pipe();
         Slave slave = new Slave(
                 this,
                 hub2slave.getInput(), slave2hub.getOutput());
         SlaveHub hub = new SlaveHub(
                 this,
                 in, out,
                 slave2hub.getInput(), hub2slave.getOutput());
         addWorker(hub);
         addWorker(slave);
 
         mLogger.info("the main loop starts.");
 
         while (1 < mWorkers.size()) {
             waitReady();
             kickWorkers();
             removeSlaves();
         }
         hub.close();
 
         return mExitStatus;
     }
 
     private void removeSlaves() {
         for (Slave slave: mSlavesToRemove) {
             mWorkers.remove(slave);
         }
 
         mSlavesToRemove.clear();
     }
 
     private void kickWorkerIfReady(Worker worker) throws IOException {
         if (!worker.isReady()) {
             return;
         }
         worker.work();
     }
 
     private void kickWorkers() throws IOException {
         for (Worker worker: mWorkers) {
             kickWorkerIfReady(worker);
         }
     }
 
     private void waitReady() throws IOException, InterruptedException {
         while (!isReady()) {
             Thread.sleep(10 /* msec */);
         }
     }
 
     private boolean isReady() throws IOException {
         boolean ready = false;
         int size = mWorkers.size();
         for (int i = 0; (i < size) && !ready; i++) {
             ready = mWorkers.get(i).isReady();
         }
         return ready;
     }
 
     private static void usage(PrintStream out) {
         out.println("usage: Main rfd wfd");
     }
 
     /**
      * This is the tester method. This class requires two parameters -- the file
      * descriptor to read and another one to write. But, Java DOES NOT HAVE ANY
      * WAYS to make InputStream/OutputStream from file descriptor number. So
      * this method opens <code>/dev/fd/<var>rfd</var></code> and
      * <code>/dev/fd/<var>wfd</var></code> to make streams. This way is
      * available on FreeBSD 9.1 with fdescfs mounted on <code>/dev/fd</code>. I
      * do not know if the same way is usable on other platforms.
      */
     public static void main(String[] args) {
         try {
             Logging.initialize();
         }
         catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }
 
         int rfd, wfd;
         try {
             rfd = Integer.parseInt(args[0]);
             wfd = Integer.parseInt(args[1]);
         }
         catch (ArrayIndexOutOfBoundsException _) {
             Application.usage(System.out);
             System.exit(1);
             return;
         }
         catch (NumberFormatException _) {
             Application.usage(System.out);
             System.exit(1);
             return;
         }
         String fmt = "/dev/fd/%d";
         String inPath = String.format(fmt, new Integer(rfd));
         String outPath = String.format(fmt, new Integer(wfd));
         InputStream in;
         OutputStream out;
         try {
             in = new FileInputStream(inPath);
             out = new FileOutputStream(outPath);
         }
         catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
             return;
         }
 
         int exitStatus;
         try {
             exitStatus = new Application().run(in, out);
         }
         catch (Throwable e) {
             e.printStackTrace();
             exitStatus = 1;
         }
         System.exit(exitStatus);
     }

    static {
        mLogger = new Logging.Logger("Application");
    }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
