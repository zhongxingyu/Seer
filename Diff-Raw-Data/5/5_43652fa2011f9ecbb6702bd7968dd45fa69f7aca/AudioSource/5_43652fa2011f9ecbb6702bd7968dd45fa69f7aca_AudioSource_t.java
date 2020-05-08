 package talkingnet.audiodevices;
 
 import java.io.IOException;
 import java.io.InputStream;
 import javax.sound.sampled.*;
 import talkingnet.core.Element;
 import talkingnet.core.io.Pulling;
 import talkingnet.core.io.Pushing;
 import talkingnet.core.io.channel.PushChannel;
 
 /**
  *
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  */
 public class AudioSource extends Element implements Pushing, LineListener {
 
     private PullingThread thread;
     protected Mixer mixer;
     protected AudioFormat format;
     protected int bufferLength;
     protected DataLine line;
     protected InputStream lineStream;
     protected PushChannel channel_out;
     protected boolean muted = false;
     protected boolean running = false;
 
     public AudioSource(
             Mixer mixer, AudioFormat format, int bufferLength,
             PushChannel channel_out, String title) {
         super(title);
         this.mixer = mixer;
         this.format = format;
         this.channel_out = channel_out;
         this.bufferLength = bufferLength;
     }
 
     @Override
     public void push_out(byte[] data, int size) {
         try {
             channel_out.write(data, size);
         } catch (IOException ex) {
             System.out.println(ex);
         }
     }
 
     public void update(LineEvent event) {
         if (event.getType().equals(LineEvent.Type.STOP)) {
             System.out.println(title + ": STOP event");
         } else if (event.getType().equals(LineEvent.Type.START)) {
             System.out.println(title + ": START event");
         } else if (event.getType().equals(LineEvent.Type.OPEN)) {
             System.out.println(title + ": OPEN event");
         } else if (event.getType().equals(LineEvent.Type.CLOSE)) {
             System.out.println(title + ": CLOSE event");
         }
     }
 
     public void start() throws Exception {
         startThread();
 
         if (line == null) {
             throw new Exception(title + ": cannot start");
         }
 
         line.flush();
         line.start();
         running = true;
 
         if (thread != null) {
             synchronized (thread) {
                 thread.notifyAll();
             }
         }
     }
 
     private void startThread() {
         if (thread != null && (thread.isTerminating() || channel_out == null)) {
             thread.terminate();
             thread = null;
         }
 
         if ((thread == null) && (channel_out != null)) {
             thread = new PullingThread();
             thread.start();
         }
     }
 
     public void open() throws Exception {
         close();
         destroyLine();
         createLine();
         openLine();
     }
     
     public void close() {
        closeLine();
        destroyLine();
    }
    
    private void closeLine() {
         if (thread != null){
             thread.terminate();
             thread.waitFor();
         }
         if (line != null) {
             line.flush();
             line.stop();
             line.close();
             running = false;
             System.out.println(title + ": line closed.");
         }
     }
     
     private void destroyLine() {
         if (line != null) {
             line.removeLineListener(this);
         }
         line = null;
     }
     
     private void createLine() throws Exception {
         try {
             line = null;
             doCreateLine();
             line.addLineListener(this);
             System.out.println("Got line for " + title + ": " + line.getClass());
         } catch (LineUnavailableException ex) {
             throw new Exception("Unable to open " + title + ": " + ex.getMessage());
         }
     }
     
     protected void doCreateLine() throws Exception {
         System.out.println(title+": creating TargetDataLine");
         DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
         if (mixer != null) {
             line = (TargetDataLine) mixer.getLine(info);
         } else {
             line = AudioSystem.getTargetDataLine(format);
         }
     }
     
     private void openLine() throws Exception {
         try {
             bufferLength -= bufferLength % format.getFrameSize();
             doOpenLine();
             System.out.println(title + ": opened line");
             bufferLength = line.getBufferSize();
             System.out.println(title + ": buffer length:" + bufferLength + " bytes.");
         } catch (LineUnavailableException ex) {
             running = false;
             throw new Exception("Unable to open " + title + ": " + ex.getMessage());
         }
     }
     
     protected void doOpenLine() throws Exception {
         System.out.println(title+": opening TargetDataLine and creating TargetDataLineAIS");
         TargetDataLine tdl = (TargetDataLine) line;
         tdl.open(format, bufferLength);
         lineStream = new PullingStream(tdl);
     }
 
     private class PullingThread extends Thread implements Pulling {
 
         private volatile boolean doTerminate = false;
         private volatile boolean terminated = false;
         int offset = 0;
 
         @Override
         public void run() {
             byte[] buffer = new byte[bufferLength];
             while (!doTerminate) {
                 pull_in(buffer, buffer.length);
                 push_out(buffer, buffer.length);
             }
             terminated = true;
         }
 
         @Override
         public void pull_in(byte[] data, int size) {
             try {
                 tryRead(data, size);
             } catch (IOException ex) {
                 System.out.println(ex);
             }
         }
 
         private void tryRead(byte[] data, int size) throws IOException {
             while ((offset < size) && (!doTerminate)) {
                 int read = lineStream.read(data, offset, size);
                 if (read == 0) {
                     continue;
                 }
                 offset += read;
             }
             offset = 0;
         }
 
         public synchronized void terminate() {
             doTerminate = true;
             this.notifyAll();
         }
 
         public synchronized boolean isTerminating() {
             return doTerminate && (terminated == false);
         }
 
         public synchronized void waitFor() {
             if (terminated == false) {
                 try {
                     this.join();
                 } catch (InterruptedException ie) {
                     ie.printStackTrace();
                 }
             }
         }
     }
     
     private class PullingStream extends InputStream {
 
         private TargetDataLine line;
 
         PullingStream(TargetDataLine line) {
             this.line = line;
         }
 
         @Override
         public int available() throws IOException {
             return line.available();
         }
 
         @Override
         public int read() throws IOException {
             throw new IOException("Illegal call to PullingStream.read()!");
         }
 
         @Override
         public int read(byte[] b, int off, int len) throws IOException {
             if (line == null) {
                 return -1;
             }
             try {
                 int ret = line.read(b, off, len);
                 
                 // TODO: if muted                
                 
                 return ret;
             } catch (IllegalArgumentException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         @Override
         public void close() throws IOException {
             if (line.isActive()) {
                 line.flush();
                 line.stop();
             }
             line.close();
         }
 
         @Override
         public int read(byte[] b) throws IOException {
             return read(b, 0, b.length);
         }
 
         @Override
         public long skip(long n) throws IOException {
             return 0;
         }
 
         @Override
         public void mark(int readlimit) {
         }
 
         @Override
         public void reset() throws IOException {
         }
 
         @Override
         public boolean markSupported() {
             return false;
         }
     }
 
     public void setMuted(boolean muted) {
         this.muted = muted;
         thread.notifyAll();
     }
     
     public boolean isStarted() {
         return (line != null) && running;
     }
 
     public boolean isOpen() {
         return (line != null) && (line.isOpen());
     }
 
     public AudioFormat getFormat() {
         return format;
     }
 }
