 package de.weltraumschaf.caythe.frontend;
 
 import de.weltraumschaf.caythe.message.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class Source implements MessageProducer {
 
     /**
      * End of line character.
      */
     public static final char EOL = (char) 10;
 
     /**
      * End of file character.
      */
     public static final char EOF = (char) 0;
 
     /**
      * Handles source messages.
      */
     protected final MessageHandler messageHandler;
 
     /**
      * The input stream.
      */
     private final BufferedReader reader;
 
     /**
      * The current line number.
      *
      * Starts by 1 and increment on each line break.
      * If this is 0 it means that nextChar() never was called.
      */
     private int lineNumber;
 
     /**
      * The current column.
      *
      * Starts by 1 and is reset to 1 on each line break.
      * If this is 0 it means that nextChar() never was called.
      */
     private int columnNumber;
 
     /**
      * Current position in the input string.
      */
     private int currentPos;
 
     /**
      * True if end of file reached.
      */
     private boolean atEof;
 
     /**
      * Holds all read characters.
      */
     private final StringBuffer buffer;
 
     /**
      * Constructs source with {@link MessageHandler} as handler.
      *
      * @param r
      */
     public Source(BufferedReader r) {
         this(r, new MessageHandlerImpl());
     }
 
     /**
      * Designated constructor.
      *
      * @param r
      * @param h
      */
     public Source(BufferedReader r, MessageHandler h) {
         reader         = r;
         messageHandler = h;
         lineNumber     = 0;
         columnNumber   = 0;
         currentPos     = -1;
         atEof          = false;
         buffer         = new StringBuffer();
     }
 
     /**
      * Gets the current position in the input string.
      *
      * @return
      */
     public int getCurrentPos() {
         return currentPos;
     }
 
     /**
      * Gets the current column in source.
      *
      * @return
      */
     public int getColumnNumber() {
         return columnNumber;
     }
 
     /**
      * Gets the current line number in source.
      *
      * @return
      */
     public int getLineNumber() {
         return lineNumber;
     }
 
     /**
      * Gets the current character of the source.
      *
      * @return
      */
     public char currentChar() {
         if (atEof()) {
             return EOF;
         } else {
             try {
                 return buffer.charAt(getCurrentPos());
             } catch (StringIndexOutOfBoundsException ex) {
                 throw new RuntimeException("Invoke Source#nextChar() first!", ex);
             }
         }
     }
 
     /**
      * Gets the next character by advancing current position.
      *
      * @throws IOException On read error in the buffered reader.
      */
     public void nextChar() throws IOException {
         if (atEof()) {
             return;
         }
 
         if (0 == lineNumber) {
             lineNumber++;
         }
 
         if ((currentPos + 1) >= buffer.length()) {
             int chr = reader.read();
 
             if (-1 == chr) {
                 atEof = true;
             } else {
                 atEof = false;
 
                 if (-1 < currentPos && EOL == currentChar()) {
                     lineNumber++;
                     columnNumber = 0;
                 }
 
                 buffer.append((char)chr);
             }
         }
 
         currentPos++;
         columnNumber++;
     }
 
     /**
      * Look ahead one character.
      *
      * Gets the next character without advancing current position.
      *
      * @return
      * @throws IOException On read error in the buffered reader.
      */
     public char peekChar() throws IOException {
         int chr = reader.read();
 
         if (-1 == chr) {
             return EOF;
         } else {
             char peak = (char)chr;
             buffer.append(peak);
             return peak;
         }
     }
 
     /**
      * Closes the {@link BufferedReader}.
      *
      * @throws IOException
      */
     public void close() throws IOException {
         reader.close();
     }
 
     /**
      * Returns true if end of line reached.
      *
      * @return
      * @throws Exception
      */
     public boolean atEol() {
         return currentPos > -1 && currentChar() == EOL;
     }
 
     /**
      * Returns true if end of source reached.
      *
      * @return
      * @throws IOException
      */
     public boolean atEof() {
         return atEof;
     }
 
     /**
      * Skips all characters until the next line begins.
      */
     public void skipToNextLine() {
         try {
             do {
                 nextChar();
             } while (currentChar() != EOL);
             nextChar(); // Skip the new line.
         }
         catch (IOException ex) {
            // Nothing to do if we can't move forward.
         }
     }
 
     @Override
     public void addMessageListener(MessageListener listener) {
         messageHandler.addListener(listener);
     }
 
     @Override
     public void removeMessageListener(MessageListener listener) {
         messageHandler.removeListener(listener);
     }
 
     @Override
     public void sendMessage(Message message) {
         messageHandler.sendMessage(message);
     }
 }
