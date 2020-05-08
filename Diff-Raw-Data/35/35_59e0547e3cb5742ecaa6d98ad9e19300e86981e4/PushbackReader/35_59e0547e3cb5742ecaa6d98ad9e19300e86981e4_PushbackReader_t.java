 package de.skuzzle.polly.core.parser;
 
 import java.io.FilterReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.LinkedList;
 
 /**
  * <p>Reader implementations that allows to push back characters into the stream and 
  * provides position tracking. In order to be able to properly track positions of pushed 
  * back characters, this stream distinguishes between normal push backs and artificial 
  * push backs. Also note that this stream uses a FIFO buffer for pushed back
  * characters.</p>
  * 
  * <p>A normal push back may only be used if previously a character has been read from
  * the stream. This may be used to replace the next character with another or to simply
  * push back a character that was read by mistake. The following sample replaces the
  * next character with another one:</p>
  * <pre>
  * void replaceNextWith(PushbackReader reader, int c) {
  *     reader.read();
  *     reader.pushback(c);
  * </pre>
  * <p>The next call to read() yields the pushed back character <code>c</code>. The 
  * position of the stream is not modified, as for each character that was read another 
  * one was pushed back.</p>
  * 
  * <p>Artificial push backs may be used to insert characters that do not occur in the
  * original stream and should thus not modify the position of the stream.</p>
  * <pre>
  * void insert(PushbackReader reader, int c) {
  *     reader.pushbackArtificial(c);
  * }
  * </pre>
  * <p>The next call to read() yields the pushed back character, but as long as no further
  * characters are read from the original stream, this streams position will not change.
  * </p>
  * 
  * <p>This reader does not support mark()/reset().</p>
  *  
  * @author Simon Taddiken
  */
 public class PushbackReader extends FilterReader {
 
     private final static class Pushback {
         private final int character;
         private final boolean artificial;
         
         public Pushback(int character, boolean artificial) {
             super();
             this.character = character;
             this.artificial = artificial;
         }
     }
     
     
     
     private final LinkedList<Pushback> buffer;
     private int position;
     private boolean eos;
     private boolean wasArtificial;
     
     
     
     public PushbackReader(Reader backend) {
         super(backend);
         this.buffer = new LinkedList<Pushback>();
     }
 
     
     
     /**
      * Determines whether there are more characters to read. This is the case if, and 
      * only if the underlying stream has no more characters to read and this stream's
      * push back buffer is empty.
      *  
      * @return Whether there are more characters to read.
      */
     public boolean eos() {
         return this.eos && this.buffer.isEmpty();
     }
     
     
     
     /**
      * <p>Gets the current index within the original input. Position will be updated by 
      * calls to {@link #read()} and push back actions.</p>
      * 
      * <p>Return value of this method is semantically the number of characters that have 
      * been read from the original stream.</p>
      * 
      * @return The current index within the input.
      */
     public int getPosition() {
         return this.position;
     }
     
     
     
     /**
      * Pushes back a character into this stream's FIFO buffer without modifying its 
      * current position.
      * 
      * @param c The character to push back.
      */
     public void pushbackArtificial(int c) {
         this.pushback(c, true);
     }
     
     
     
     /**
      * <p>Pushes back a character into this streams FIFO buffer and decreases the current 
      * stream position by 1. This method can only be used if previously a characters has 
      * been read from the stream. The rule is, that for each call to {@link #read()} which
      * read a non-artificial character, you are allowed to push back one character.</p>
      * 
      * <p>Whether the last read character was an artificial can be determined with
      * {@link #wasArtificial()}.</p>
      * 
      * <p>If you push back more characters than actually have been read so far, proper 
      * position tracking can no longer be guaranteed. Also, if you try to push back a 
      * character at the beginning of the stream or after the last character has been 
      * read, this method will throw an {@link IllegalStateException}.</p>
      * 
      * @param c The character to push back.
      */
     public void pushback(int c) {
         this.pushback(c, false);
     }
     
     
     
     /**
      * Whether the last call to {@link #read()} yielded an artificial character.
      * @return Whether the last call to {@link #read()} yielded an artificial character.
      */
     public boolean wasArtificial() {
         return this.wasArtificial;
     }
     
     
     
     private void pushback(int c, boolean artificial) {
         artificial |= c == -1;
        //if (!artificial && (this.position == 0 || this.eos)) {
            //throw new IllegalStateException("illegal artificial pushback");
        //}
         this.buffer.add(new Pushback(c, artificial));
         this.position -= artificial ? 0 : 1;
         this.eos = artificial ? this.eos : false;
     }
 
     
     
 
     /**
      * Reads the next character. If the push back buffer is empty, a character is read
      * from the underlying reader and position of this stream is increased by 1.
      * If there is a character in the push back buffer, it is removed. Position will only 
      * be increased if that character was not pushed artificial.
      *  
      * @return The next character.
      * @throws IOException If an I/O error occurred.
      */
     public int read() throws IOException {
         if (this.buffer.isEmpty()) {
             final int next = super.read();
             this.eos = next == -1;
             this.position += this.eos ? 0 : 1;
             this.wasArtificial = false;
             return next;
         } else {
             final Pushback pb = this.buffer.poll();
             this.position += pb.artificial ? 0 : 1;
             this.wasArtificial = pb.artificial;
             return pb.character;
         }
     }
     
     
 
     @Override
     public int read(char[] cbuf, int off, int len) throws IOException {
         if (len <= 0) {
             if (len < 0 || off < 0 || off > cbuf.length) {
                 throw new IndexOutOfBoundsException();
             }
             // len is 0
             return 0;
         }
         
         // first, read characters from buffer
         int avail = this.buffer.size();
         if (avail > 0) {
             if (len < avail) {
                 avail = len;
             }
             for (int i = 0; i < avail; ++i) {
                 cbuf[off + i] = (char) this.read();
             }
             off += avail;
             len -= avail;
         }
         
         // read characters from stream
         if (len > 0) {
             len = super.read(cbuf, off, len);
             if (len == -1) {
                 return avail == 0 ? - 1 : avail;
             }
             return avail + len;
         }
         return avail;
     }
 
     
     
     @Override
     public void close() throws IOException {
         this.buffer.clear();
         super.close();
     }
     
     
     
     @Override
     public boolean markSupported() {
         return false;
     }
     
     
     
     @Override
     public void mark(int readAheadLimit) throws IOException {
         throw new IOException("mark not supported");
     }
     
     
     
     @Override
     public void reset() throws IOException {
         throw new IOException("reset not supported");
     }
 }
