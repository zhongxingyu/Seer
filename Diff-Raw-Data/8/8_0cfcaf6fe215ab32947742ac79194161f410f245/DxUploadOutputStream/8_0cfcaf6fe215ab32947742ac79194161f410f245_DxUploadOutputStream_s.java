 /*
  * Copyright (c) 2013, the authors.
  *
  *   This file is part of 'DXFS'.
  *
  *   DXFS is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   DXFS is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with DXFS.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package nextflow.fs.dx;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Phaser;
 import java.util.concurrent.TimeUnit;
 
 import com.dnanexus.DXHTTPRequest;
 import org.apache.http.HttpEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sun.nio.ch.DirectBuffer;
 
 /**
  * Gather the stream data to a byte buffer, when the buffer is full upload it
  * in background and allocate a new byte buffer to let the writer to
  *
  * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
  */
 public class DxUploadOutputStream extends OutputStream {
 
     private static Logger log = LoggerFactory.getLogger(DxUploadOutputStream.class);
 
     /* Set when the close method is invoked. It signals to stop waiting for new chunks */
     private volatile boolean closed;
 
     // note: no need to be synchronized since it is access only the queue thread
     private int chunkCount = 0;
 
     /**
      * The file-id to be uploaded.
      */
     final String fileId;
 
     private static int _1MB = 1024*1024;
 
     /**
      * Minimum size of each chunk (5M)
      *
      * Read more https://wiki.dnanexus.com/API-Specification-v1.0.0/Files#API-method%3A-%2Ffile-xxxx%2Fupload
      */
     final static int MIN_CHUNK_SIZE = 5 * _1MB ;
 
     /**
      * Maximum size of each chunk (500M)
      */
     final static long MAX_CHUNK_SIZE = 500 * _1MB ;
 
     /**
      * Default buffer capacity (20MB)
      */
     final static private int defaultCapacity = 20 * _1MB ;
 
 
     /**
      * Instead of allocate a new buffer for each chunks recycle them, putting
      * a buffer instance into this queue when the upload process is completed
      */
     final private Queue<ByteBuffer> bufferPool = new ConcurrentLinkedQueue<ByteBuffer>();
 
     /**
      * The executor service (thread pool) which manages the upload in background
      */
     final private ExecutorService executor;
 
     /**
      * DnaNexus API wrapper
      */
     final private DxRemoter remote;
 
     /**
      *
      */
     final private BlockingQueue<ByteBuffer> queue;
 
     /**
      * Sync phaser
      */
     final private Phaser phaser;
 
     /**
      * The current working buffer
      */
     private ByteBuffer buf;
 
     /**
      * Initialize the uploader output stream for the specified file
      *
      * @param fileId The target DnaNexus file
      * @param remote The DnaNexus API wrapper object
      * @param maxForks Maximum number of parallel upload jobs allowed (default: 5)
      */
     public DxUploadOutputStream(String fileId, DxRemoter remote, int maxForks) {
 
         this.fileId = fileId;
         this.queue = new ArrayBlockingQueue<>(maxForks);
         this.phaser = new Phaser();
         this.remote = remote;
         this.buf = allocate();
         this.executor = Executors.newCachedThreadPool();
         checkCapacity();
         start();
     }
 
     /**
      * Initialize the uploader output stream for the specified file using
      * up to 5 parallel upload threads.
      *
      * @param fileId The target DnaNexus file
      * @param remote The DnaNexus API wrapper object
      */
     public DxUploadOutputStream(String fileId, DxRemoter remote) {
         this(fileId, remote, 5);
     }
 
     /**
      * Create a new byte buffer to hold parallel chunks uploads. Override to use custom
      * buffer capacity or strategy e.g. {@code DirectBuffer}
      *
      * @return The {@code ByteBuffer} instance
      */
     protected ByteBuffer allocate() {
         return ByteBuffer.allocateDirect(defaultCapacity);
     }
 
     /**
      * Check the capacity of the buffer is within the min and max limits
      */
     final protected void checkCapacity() {
         if( buf == null ) return;
         
         if( buf.capacity()<MIN_CHUNK_SIZE ) {
             throw new IllegalStateException("Buffer capacity cannot be less than: " + MIN_CHUNK_SIZE);
         }
         if( buf.capacity()>MAX_CHUNK_SIZE ) {
             throw new IllegalStateException("Buffer capacity cannot be greater than: " + MAX_CHUNK_SIZE);
         } 
     }
 
 
     /**
      * When a buffer reach its capacity, this method is called.
      * It does two things:
      * <li>Flush the current buffer i.e. upload it</li>
      * <li>Get a new buffer to continue the out streaming</li>
      *
      */
     final protected void swapBuffer() {
 
         // send out the current current
         flush();
 
         // try to reuse a buffer from the poll
         buf = bufferPool.poll();
         if( buf != null ) {
             buf.clear();
         }
         else {
             // allocate a new buffer
             buf = allocate();
             checkCapacity();
         }
     }
 
     /**
      * Flush the current buffer content scheduling it for upload
      */
     @Override
     public void flush() {
         log.trace("File: {} > Flushing buffer", fileId);
         // when the buffer is empty nothing to do
         if( buf == null || buf.position()==0 ) { return; }
 
         buf.flip();
         try {
             queue.put(buf);
         }
         catch( InterruptedException e ) {
             throw new IllegalStateException(e);
         }
         buf = null;
     }
 
     @Override
     public void write (int b) throws IOException {
         if (!buf.hasRemaining()) {
             swapBuffer();
         }
 
         buf.put((byte) b);
     }
 
 
 // TODO write (byte[] bytes, int offset, int length)
 //    @Override
 //    public void write (byte[] bytes, int offset, int length) throws IOException {
 //        if (buf.remaining() < length) flush();
 //        buf.put(bytes, offset, length);
 //    }
 
     /**
      * Start the uploading process
      */
     private void start() {
         log.trace("Starting upload process");
 
         // register the phaser for the main thread
         phaser.register();
 
         Runnable watcher = new Runnable() {
             @Override
             public void run() {
                 try {
                     dequeueAndSubmit();
                 }
                 finally {
                     phaser.arriveAndDeregister();
                 }
             }
         };
 
         // submit the task for execution
         executor.submit(watcher);
 
         // register the phaser for the 'watcher' thread
         phaser.register();
     }
 
     /*
      * Wait for a chunk in the queue, take it and submit for upload
      */
     private void dequeueAndSubmit() {
         log.trace("Entering received loop");
 
         while( !closed || queue.size()>0 ) {
             ByteBuffer buffer;
             try {
                 buffer = queue.poll(1, TimeUnit.SECONDS);
                 log.trace("File: {} > Received a buffer -- limit: ", fileId, buffer.limit());
                 executor.submit( consumeBuffer0(buffer, ++chunkCount) );
             }
             catch (InterruptedException e) {
                 log.trace("File: {} > Got an interrupted exception while waiting new chunk to upload -- cause: {}", fileId, e.getMessage());
             }
         }
 
         log.trace("Exiting received loop");
     }
 
     /**
      * Upload a chunk of data
      *
      * @param buffer The buffer to be uploaded
      * @param chunkIndex The index count
      * @return
      */
     private Runnable consumeBuffer0(final ByteBuffer buffer, final int chunkIndex) {
 
         phaser.register();
 
         return new Runnable() {
             @Override
             public void run() {
                 try {
                     consumeBuffer(buffer, chunkIndex);
                 }
                 catch (IOException e) {
                     log.debug("File: {} > Error for chunk: %s -- cause: %s", fileId, chunkIndex, e.getMessage());
                     throw new IllegalStateException(e);
                 }
                 finally {
                     phaser.arriveAndDeregister();
                 }
             }
         };
 
     }
 
     @SuppressWarnings("unchecked")
     private void consumeBuffer(final ByteBuffer buffer, final int chunkIndex) throws IOException {
         log.debug("File: {} > uploading chunk: {}", fileId, chunkIndex);
 
         // request to upload a new chunk
         // note: dnanexus upload chunk index is 1-based
         Map<String,Object> upload = remote.fileUpload(fileId, chunkIndex);
         log.trace("File: {} > chunk [{}] > FileUpload: {}", fileId, chunkIndex, upload);
 
         // the response provide the url when 'post' the chunk and the
         // 'authorization' code
         String url = (String)upload.get("url");
         Map<String,Object> headers = (Map<String,Object>)upload.get("headers");
         String auth = (String)headers.get("Authorization");
 
         // create a 'post' request to upload the stuff
         HttpPost post = new HttpPost(url);
         post.setHeader("Authorization", auth);
 
         log.trace("File: {} > chunk [{}] > buffer limit: {}; remaining: {}", fileId, chunkIndex, buffer.limit(), buffer.remaining());
 
         HttpEntity payload = new InputStreamEntity(new ByteBufferBackedInputStream(buffer), buffer.limit()) ;
         post.setEntity(payload);
 
 //        HttpClient client = new DefaultHttpClient();
 //        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 //        log.trace("File: {} > chunk [{}] > Post starting: {}", fileId, chunkIndex, post);
 
         HttpEntity entity = DXHTTPRequest.getHttpClient().execute(post).getEntity();
         String response = EntityUtils.toString(entity, "UTF-8");
         log.trace("File: {} > chunk [{}] > post response: {}", fileId, chunkIndex, response);
 
 //        // close the client (maybe not really necessary)
 //        client.getConnectionManager().shutdown();
         // put the 'buffer' in the pool, so that it can be recycled
         bufferPool.offer(buffer);
 
         log.trace("File: {} > completed upload chunk: ", fileId, chunkIndex);
     }
 
 
     /**
      * Close the output streaming waiting the upload process for completion
      *
      * @throws IOException
      */
     @Override
     public void close() throws IOException {
         log.trace("Entering close");
         // flush current buffer
         flush();
 
         // close and wait on-going upload finishes
         closed = true;
         phaser.arriveAndAwaitAdvance();
         log.trace("Phaser advanced");
         executor.shutdown();
 
        if( chunkCount>0 ) {
            log.trace("Closing DX file");
            // dx file close
            remote.fileClose(fileId);
        }
 
         // dispose the buffers
         for( ByteBuffer item : bufferPool ) {
             if( item instanceof DirectBuffer ) {
                 ((DirectBuffer) item).cleaner().clean();
             }
         }
 
         log.trace("File: {} > closed -- {} chunks processed", fileId, chunkCount);
     }
 }
