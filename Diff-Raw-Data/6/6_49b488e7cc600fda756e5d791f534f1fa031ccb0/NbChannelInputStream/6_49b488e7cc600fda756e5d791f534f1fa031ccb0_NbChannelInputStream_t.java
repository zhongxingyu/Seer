 /**
  * Copyright 2005-2011 Noelios Technologies.
  * 
  * The contents of this file are subject to the terms of one of the following
  * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
  * "Licenses"). You can select the license that you prefer but you may not use
  * this file except in compliance with one of these Licenses.
  * 
  * You can obtain a copy of the LGPL 3.0 license at
  * http://www.opensource.org/licenses/lgpl-3.0.html
  * 
  * You can obtain a copy of the LGPL 2.1 license at
  * http://www.opensource.org/licenses/lgpl-2.1.php
  * 
  * You can obtain a copy of the CDDL 1.0 license at
  * http://www.opensource.org/licenses/cddl1.php
  * 
  * You can obtain a copy of the EPL 1.0 license at
  * http://www.opensource.org/licenses/eclipse-1.0.php
  * 
  * See the Licenses for the specific language governing permissions and
  * limitations under the Licenses.
  * 
  * Alternatively, you can obtain a royalty free commercial license with less
  * limitations, transferable or non-transferable, directly at
  * http://www.noelios.com/products/restlet-engine
  * 
  * Restlet is a registered trademark of Noelios Technologies.
  */
 
 package org.restlet.engine.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.util.logging.Level;
 
 import org.restlet.Context;
 import org.restlet.util.SelectionListener;
 import org.restlet.util.SelectionRegistration;
 
 // [excludes gwt]
 /**
  * Input stream connected to a non-blocking readable channel.
  * 
  * @author Jerome Louvel
  */
 public class NbChannelInputStream extends InputStream implements
         BufferProcessor {
 
     /** The internal byte buffer. */
     private final Buffer buffer;
 
     /** The channel to read from. */
     private final ReadableByteChannel channel;
 
     /** Indicates if further reads can be attempted. */
     private volatile boolean endReached;
 
     /** The optional selectable channel to read from. */
     private final SelectableChannel selectableChannel;
 
     /** The optional selection channel to read from. */
     private final SelectionChannel selectionChannel;
 
     /** The registered selection registration. */
     private volatile SelectionRegistration selectionRegistration;
 
     /**
      * Constructor.
      * 
      * @param channel
      *            The channel to read from.
      */
     public NbChannelInputStream(ReadableByteChannel channel) {
         this.channel = channel;
 
         if (channel instanceof ReadableSelectionChannel) {
             this.selectionChannel = (ReadableSelectionChannel) channel;
             this.selectableChannel = null;
         } else if (channel instanceof SelectableChannel) {
             this.selectionChannel = null;
             this.selectableChannel = (SelectableChannel) channel;
         } else if (channel instanceof SelectionChannel) {
             this.selectionChannel = (SelectionChannel) channel;
             this.selectableChannel = null;
         } else {
             this.selectionChannel = null;
             this.selectableChannel = null;
         }
 
         this.buffer = new Buffer(IoUtils.BUFFER_SIZE);
         this.endReached = false;
         this.selectionRegistration = null;
     }
 
     /**
      * Indicates if the processing loop can continue.
      * 
      * @return True if the processing loop can continue.
      */
     public boolean canLoop() {
         return true;
     }
 
     /**
      * Indicates if the buffer could be filled again.
      * 
      * @return True if the buffer could be filled again.
      */
     public boolean couldFill() {
         return !this.endReached;
     }
 
     /**
      * Returns the internal byte buffer.
      * 
      * @return The internal byte buffer.
      */
     protected Buffer getBuffer() {
         return buffer;
     }
 
     /**
      * Drains the byte buffer by returning available bytes as
      * {@link InputStream} bytes.
      * 
      * @param buffer
      *            The IO buffer to drain.
      * @param args
      *            The optional arguments to pass back to the callbacks.
     * @return The number of bytes drained.
      * @throws IOException
      */
     public int onDrain(Buffer buffer, Object... args) throws IOException {
         int result = 0;
 
         if (args.length == 1) {
             // Let's return the next one
            args[0] = getBuffer().drain();
            result = 1;
         } else if (args.length == 3) {
             byte[] targetArray = (byte[]) args[0];
             int offset = ((Integer) args[1]).intValue();
             int length = ((Integer) args[2]).intValue();
             result = Math.min(length, getBuffer().remaining());
 
             // Let's return the next ones
             getBuffer().drain(targetArray, offset, result);
         }
 
         return result;
     }
 
     /**
      * Fills the byte buffer by reading the source channel.
      * 
      * @param buffer
      *            The IO buffer to drain.
      * @param args
      *            The optional arguments to pass back to the callbacks.
      * @throws IOException
      */
     public int onFill(Buffer buffer, Object... args) throws IOException {
         int result = buffer.fill(this.channel);
 
         if (result == 0) {
             // No bytes were read, try to register
             // a select key to get more
             if (selectionChannel != null) {
                 try {
                     if (this.selectionRegistration == null) {
                         this.selectionRegistration = this.selectionChannel
                                 .getRegistration();
                         this.selectionRegistration
                                 .setInterestOperations(SelectionKey.OP_READ);
                         this.selectionRegistration
                                 .setListener(new SelectionListener() {
                                     public void onSelected() {
                                         if (Context.getCurrentLogger()
                                                 .isLoggable(Level.FINER)) {
                                             Context.getCurrentLogger()
                                                     .log(Level.FINER,
                                                             "NbChannelInputStream selected");
                                         }
 
                                         // Stop listening at this point
                                         selectionRegistration.suspend();
 
                                         // Unblock the user thread
                                         selectionRegistration.unblock();
                                     }
                                 });
                     } else {
                         this.selectionRegistration.resume();
                     }
 
                     // Block until new content arrives or a timeout occurs
                     this.selectionRegistration.block();
 
                     // Attempt to read more content
                     result = buffer.fill(this.channel);
                 } catch (Exception e) {
                     Context.getCurrentLogger()
                             .log(Level.FINE,
                                     "Exception while registering or waiting for new content",
                                     e);
                 }
             } else if (selectableChannel != null) {
                 Selector selector = null;
                 SelectionKey selectionKey = null;
 
                 try {
                     selector = SelectorFactory.getSelector();
 
                     if (selector != null) {
                         selectionKey = this.selectableChannel.register(
                                 selector, SelectionKey.OP_READ);
                         selector.select(IoUtils.TIMEOUT_MS);
                     }
                 } finally {
                     NioUtils.release(selector, selectionKey);
                 }
 
                 result = buffer.fill(this.channel);
             }
         }
 
         if (result == -1) {
             this.endReached = true;
 
             if (this.selectionRegistration != null) {
                 this.selectionRegistration.setCanceling(true);
                 this.selectionRegistration.setListener(null);
             }
         }
 
         return result;
     }
 
     @Override
     public int read() throws IOException {
         int result = 0;
         Object[] args = new Object[1];
         int bytesDrained = getBuffer().process(this, args);
 
         if (bytesDrained == -1) {
             result = -1;
         } else if (bytesDrained == 1) {
             result = ((Integer) args[0]).intValue();
         } else {
             Context.getCurrentLogger().warning(
                     "Too much bytes drained. Only one byte was needed.");
         }
 
         return result;
     }
 
     @Override
     public int read(byte[] targetArray, int offset, int length)
             throws IOException {
         return getBuffer().process(this, targetArray, offset, length);
     }
 
 }
