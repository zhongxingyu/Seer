 /**
  * 
  */
 package compiler.lex;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import compiler.Utils;
 
 /**
  * Tracks the 1-based position and line number while supporting the mark()
  * operation with arbitrary lookahead.
  * 
  * @author Michael
  */
 public class LineNumberAndPositionBufferedReader extends Reader {
 	private final Reader reader;
 	private final StringBuilder buffer = new StringBuilder();
 	private int lineNumber = 0, position = 0, markLineNumber, markPosition,
 			nextReadBufferIndex = 0;
 	private boolean sawLineFeed = true, markSet = false, markSawLineFeed;
 
 	public LineNumberAndPositionBufferedReader(Reader reader) {
 		this.reader = reader;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Reader#close()
 	 */
 	@Override
 	public void close() throws IOException {
 		this.reader.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Reader#read(char[], int, int)
 	 */
 	@Override
 	public int read(char[] cbuf, int off, int len) throws IOException {
 		int maxLen = Math.min(len, cbuf.length - off);
 		for (int i = 0; i < maxLen; i++) {
 			int ch = this.read();
 			if (ch == -1) {
 				return -1;
 			}
 			cbuf[off + i] = (char) ch;
 		}
 
 		return maxLen;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Reader#read()
 	 */
 	@Override
 	public int read() throws IOException {
 		int ch;
 
 		// if there's nothing left to read from the buffer...
 		if (this.nextReadBufferIndex >= this.buffer.length()) {
 			ch = this.reader.read();
 
 			// EOF case: don't update line and position info
 			if (ch == -1) {
 				return ch;
 			}
 
 			// if we read from the stream and we have a mark set,
 			// save the (non-EOF) character
 			if (this.markSet) {
 				this.buffer.append((char) ch);
 			}
 		}
 		// still need to read chars from the buffer
 		else {
 			ch = this.buffer.charAt(this.nextReadBufferIndex);
 		}
 
 		this.nextReadBufferIndex++;
 
 		// if the last character was \n, this is the first character
 		// of a new line, so update line and position
 		if (this.sawLineFeed) {
 			this.lineNumber++;
 			this.position = 1;
 		} else {
 			this.position++;
 		}
 
 		this.sawLineFeed = (ch == '\n');
 
 		return ch;
 	}
 
 	/**
 	 * As read() but throws an unchecked exception on failure
 	 */
 	public int uncheckedRead() {
 		try {
 			return this.read();
 		} catch (IOException ex) {
 			throw Utils.err(ex);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Reader#mark(int)
 	 */
 	@Override
 	public void mark(int readAheadLimit) throws IOException {
 		this.mark();
 	}
 
 	public void mark() {
 		// save positional info
 		this.markLineNumber = this.lineNumber;
 		this.markPosition = this.position;
 		this.markSawLineFeed = this.sawLineFeed;
 
 		// delete any buffered characters we've already read since
 		// now they're behind the mark
 		this.buffer.delete(0, this.nextReadBufferIndex);
 		this.nextReadBufferIndex = 0;
 		this.markSet = true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Reader#reset()
 	 */
 	@Override
 	public void reset() {
 		Utils.check(this.markSet, "Cannot reset without first setting a mark!");
 
 		// restore positional info
 		this.lineNumber = this.markLineNumber;
 		this.position = this.markPosition;
 		this.sawLineFeed = this.markSawLineFeed;
 
 		this.nextReadBufferIndex = 0;
 	}
 
 	/**
	 * The number of characters read since the last call to mark().
 	 */
 	public int offsetFromMark() {
		Utils.check(this.markSet, "Cannot reset without first setting a mark!");
 
 		return this.nextReadBufferIndex;
 	}
 
 	/**
 	 * The 1-based line number of the last character read. A \n character is
 	 * considered to be the last character on a line.
 	 */
 	public int lineNumber() {
 		return this.lineNumber;
 	}
 
 	/**
 	 * The 1-based position of the last character read in the current line.
 	 */
 	public int position() {
 		return this.position;
 	}
 }
