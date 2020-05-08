 package veeju.parser;
 
 /**
  * Exception that is thrown when the text to be parsed has an invalid syntax.
  */
 public class InvalidSyntaxException extends RuntimeException {
     public static final long serialVersionUID = 20120310L;
 
     /**
      * The code string that has an invalid syntax.
      */
     protected final CharSequence text;
 
     /**
      * The zero-based offset that the syntax error occurred.
      */
     protected final int offset;
 
     /**
      * Creates an {@link InvalidSyntaxException} instance.
      *
      * @param text a code string that has an invalid syntax.
      * @param offset a zero-based offset that the syntax error occurred.
      * @param message the detail message (which is saved for later retrieval
      *                by the {@link #getMessage()} method).
      */
     public InvalidSyntaxException(final CharSequence text, final int offset,
                                   final String message) {
         super(message);
         this.text = text;
         if (offset < 0) throw new IllegalArgumentException("negative offset");
         else if (offset > text.length()) {
             throw new IllegalArgumentException("offset must be less than " +
                                                "the length of text");
         }
         this.offset = offset;
     }
 
     /**
      * Creates an {@link InvalidSyntaxException} instance without any message.
      *
      * @param text a code string that has an invalid syntax.
      * @param offset a zero-based offset that the syntax error occurred.
      */
     public InvalidSyntaxException(final CharSequence text, final int offset) {
         this(text, offset, null);
     }
 
     /**
      * The method for getting the code string that has an invalid syntax.
      *
      * @return a code string that has an invalid syntax.
      * @see #text
      */
     public CharSequence getText() {
         return text;
     }
 
     /**
      * Returns a zero-based offset that the syntax error occurred.
      *
      * @return a zero-based offset of the text that the parser stops.
      * @see #offset
      * @see #getColumn()
      * @see #getLine()
      */
     public int getOffset() {
         return offset;
     }
 
     /**
      * Returns a zero-based offset of the line.
      *
      * @return a zero-based offset of the line.
      * @see #getOffset()
      * @see #getColumn()
      */
     public int getLine() {
         int line = 0;
         for (int i = 0; i < offset; ++i) {
             if (text.charAt(i) != '\n') continue;
             ++line;
         }
         return line;
     }
 
     /**
      * Returns a zero-based offset of the column.
      *
      * @return a zero-based offset of the column.
      * @see #getOffset()
      * @see #getColumn()
      */
     public int getColumn() {
        int col = 0;
        for (int i = offset; i > 0 && text.charAt(i) != '\n'; --i) {
             ++col;
         }
         return col - 1;
     }
 
     /**
      * Returns a string of the line.
      *
      * @return a string of the line.
      */
     public String getLineString() {
        int i, j;
        for (i = offset; i >= 0 && text.charAt(i) != '\n'; --i);
         for (j = offset; j < text.length() && text.charAt(j) != '\n'; ++j);
         return text.subSequence(i + 1, j).toString();
     }
 }
 
