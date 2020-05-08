 package utils.externalsort;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  *
  * @author fedin
  */
 public class Merger implements Runnable {
 
     public static Merger create(BlockingQueue<File> queue, AtomicBoolean stop, int bufferSize) {
         return create(queue, stop, bufferSize, 2);
     }
 
     public static Merger create(BlockingQueue<File> queue, AtomicBoolean stop, int bufferSize, int maxMerge) {
         Merger merger = new Merger();
         merger.queue = queue;
         merger.stop = stop;
         merger.maxMerge = maxMerge;
        final int tmpSize = bufferSize / (maxMerge + 1);
        merger.bufferSize = tmpSize - (tmpSize % 4);
         return merger;
     }
     private int maxMerge;
     private int bufferSize;
     private BlockingQueue<File> queue;
     AtomicBoolean stop;
 
     private Merger() {
     }
 
     private File merge(List<File> inputs) throws IOException {
         final int length = inputs.size();
         final File result = File.createTempFile(Utils.TEMP_FILE_PREFIX, null);
         DataOutputStream out = null;
         FileInputStream[] fis = new FileInputStream[length];
         try {
             out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(result), bufferSize));
             FileChannel[] ch = new FileChannel[length];
             ByteBuffer[] buf = new ByteBuffer[length];
             int[] heads = new int[length];
             boolean[] finished = new boolean[length];
             for (int i = 0; i < length; i++) {
                 fis[i] = new FileInputStream(inputs.get(i));
                 ch[i] = fis[i].getChannel();
                 buf[i] = ByteBuffer.allocate(bufferSize);
                 if (ch[i].read(buf[i]) < Utils.BYTE_TO_INT) {
                     finished[i] = true;
                 } else {
                     buf[i].flip();
                     heads[i] = buf[i].getInt();
                 }
             }
             while (!Utils.and(finished)) {
                 int min = Integer.MAX_VALUE;
                 int idx = -1;
                 for (int i = 0; i < length; i++) {
                     if (finished[i]) {
                         continue;
                     }
                     if (heads[i] <= min) {
                         min = heads[i];
                         idx = i;
                     }
                 }
                if (buf[idx].remaining() > 0) {
                     heads[idx] = buf[idx].getInt();
                 } else {
                     buf[idx].clear();
                    if (ch[idx].read(buf[idx]) >= 0) {
                         buf[idx].flip();
                         heads[idx] = buf[idx].getInt();
                     } else {
                         finished[idx] = true;
                     }
                 }
                 out.writeInt(min);
             }
             return result;
         } finally {
             for (FileInputStream in : fis) {
                 Utils.close(in);
             }
             Utils.close(out);
             for (File file : inputs) {
                 if (!file.delete()) {
                     throw new RuntimeException("Cannot delete file " + file);
                 }
             }
             System.out.format("Merged to file %s, size = %,dB\n", result, result.length());
         }
     }
 
     public void run() {
        System.out.println(Thread.currentThread() + "Merger start");
         while (true) {
             List<File> list = new ArrayList<File>();
             try {
                 queue.drainTo(list, maxMerge);
                 if (list.size() == 1) {
                     queue.put(list.get(0));
                     if (stop.get()) {
                         return;
                     }
                 } else if (list.size() > 1) {
                     queue.put(merge(list));
                 }
             } catch (Exception ex) {
                 throw new RuntimeException(ex);
             }
         }
     }
 }
