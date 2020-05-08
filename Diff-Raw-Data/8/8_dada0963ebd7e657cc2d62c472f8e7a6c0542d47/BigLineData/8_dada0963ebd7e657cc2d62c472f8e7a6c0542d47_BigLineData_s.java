 package info.kyorohiro.helloworld.util;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CoderResult;
 import java.util.ArrayList;
 
 import android.graphics.Paint;
 import android.graphics.Typeface;
 
 public class BigLineData {
 	public static int FILE_LIME = 100;
 
 	private File mPath;
 	private String mCharset = "utf8";
 	private RandomAccessFile mReader = null;
 	
 	// todo
 	private Paint mPaint = null;
 	private int mWidth =800;
 
 	private long mCurrentPosition = 0;
 	private long mLastPosition = 0;
 	private long mLinePosition = 0;
 	private long mLastLinePosition = 0;
 	private ArrayList<Long> mPositionPer100Line = new ArrayList<Long>();
 
 	public BigLineData(File path) throws FileNotFoundException {
 		init(path, mCharset);
 	}
 
 	public BigLineData(File path, String charset, int textSize, int screenWidth) throws FileNotFoundException {
 		init(path, charset);
 		mWidth = screenWidth;
 		mPaint = new Paint();
 		mPaint.setTextSize(12);
 		mPaint.setTextSize(textSize);
 		mPaint.setTypeface(Typeface.SANS_SERIF);
 	}
 
 	private void init(File path, String charset) throws FileNotFoundException {
 		mPath = path;
 		mCharset = charset;
 		mReader = new RandomAccessFile(mPath, "r");
 		mPositionPer100Line.add(0l);
 	}
 
 	public long getLastLinePosition() {
 		return mLastLinePosition;
 	}
 
 	public boolean stackedLinePosition() {
 		if (mLastPosition >= getDataSize()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public int getStackedLinePer100() {
 		return mPositionPer100Line.size();
 	}
 
 	public boolean moveLinePer100(int index) throws IOException {
 		if (index < mPositionPer100Line.size()) {
 			long filePointer = mPositionPer100Line.get(index);
 			// mReader.seek(0);
 			mReader.seek(filePointer);
 			mLinePosition = index * BigLineData.FILE_LIME;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean wasEOF() {
 		try {
 			if (mReader.length() <= mLastPosition) {
 				return true;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean isEOF() {
 		try {
 			if (mReader.length() <= mReader.getFilePointer()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public long getDataSize() {
 		return mPath.length();
 	}
 
 	public CharSequence readLine() throws IOException {
 		CharSequence tmp = new TODOCRLFString("", TODOCRLFString.MODE_INCLUDE_LF);
 		int retPosition = (int) mLinePosition;
 		try {
 
 			tmp = decode(Charset.forName(mCharset));
 			mCurrentPosition = mReader.getFilePointer();
 			mLinePosition += 1;
 			if (mLastLinePosition < mLinePosition) {
 				mLastLinePosition = mLinePosition;
 			}
 			if (mLinePosition % FILE_LIME == 0) {
 				int index = mPositionPer100Line.size() - 1;
 				long stackedPosition = 0;
 				if (index > 0) {
 					stackedPosition = mPositionPer100Line
 							.get(mPositionPer100Line.size() - 1);
 				}
 				if (stackedPosition < mCurrentPosition) {
 					mPositionPer100Line.add(mCurrentPosition);
 				}
 			}
 			if (mLastPosition < mCurrentPosition) {
 				mLastPosition = mCurrentPosition;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// 0n܂!! mLinePosition-1
 		return new LineWithPosition(tmp, mCurrentPosition, retPosition, 
 				((TODOCRLFString)tmp).mMode);
 	}
 
 	public void close() throws IOException {
 		mReader.close();
 	}
 
 	public CharSequence decode(Charset cs) throws IOException {
 		StringBuilder b = new StringBuilder();
 		CharsetDecoder decoder = cs.newDecoder();
 		boolean end = false;
 		ByteBuffer bb = ByteBuffer.allocateDirect(32); // bb͏߂
 		CharBuffer cb = CharBuffer.allocate(16); // cb͏߂
 
 		//todo
 		int mode = TODOCRLFString.MODE_INCLUDE_LF;
 		long todoPrevPosition = mReader.getFilePointer();
 		outside:do {
 			int d = mReader.read();
 			if (d >= 0) {
 				bb.put((byte) d); // bbւ̏
 			} else {
 				// todo
 				break;
 			}
 			bb.flip();
 			CoderResult cr = decoder.decode(bb, cb, end);
 
 			cb.flip();
 			char c = ' ';
 			boolean added = false;
 			while (cb.hasRemaining()) {
 				added = true;
 				bb.clear();
 				c = cb.get();
 				b.append(c);
 				if(mPaint != null){
 					// todo kiyohiro unefficient coding
 					String tmp = b.toString();
 					int len =mPaint.breakText(tmp, true, mWidth, null);
					if (len != tmp.length()) {
 						//ЂƂOŉs
 						b.deleteCharAt(b.length()-1);
 						mReader.seek(todoPrevPosition);
 						mode = TODOCRLFString.MODE_EXCLUDE_LF;
 						break outside;
 					} else {
 						todoPrevPosition = mReader.getFilePointer();
 					}
 				}
 			}
 			if (c == '\n') {
 				break;
 			}
 
 			if (!added) {
 				bb.compact();
 			}
 			cb.clear(); // cbݏԂɕύX
 		} while (!end);
 		return new TODOCRLFString(b.toString(), mode);
 	}
 
 	
 	public static class TODOCRLFString implements CharSequence {
 		public static int MODE_INCLUDE_LF = 1;
 		public static int MODE_EXCLUDE_LF = 0;
 		public String mContent = null;
 		public int mMode = MODE_EXCLUDE_LF;
 		
 		public TODOCRLFString(String str, int mode) {
 			mContent = str;
 			mMode = mode;
 		}
 
 		@Override
 		public char charAt(int index) {
 			return mContent.charAt(index);
 		}
 
 		@Override
 		public int length() {
 			return mContent.length();
 		}
 
 		@Override
 		public CharSequence subSequence(int start, int end) {
 			return mContent.subSequence(start, end);
 		}
 		
 		@Override
 		public String toString() {
 			return mContent.toString();
 		}
 	}
 
 	public static class LineWithPosition implements CharSequence {
 		private CharSequence mLine = "";
 		private long mPosition = 0;
 		private long mLinePosition = 0;
 		private int mMode = 0;
 
 		public LineWithPosition(CharSequence line, long position, long linenum, int mode) {
 			mLine = line;
 			mPosition = position;
 			mLinePosition = linenum;
 			mMode = mode;
 		}
 
 		public boolean includeLF(){
 			if(mMode == TODOCRLFString.MODE_INCLUDE_LF){
 				return true;
 			} else {
 				return false;
 			}
 		}
 		public char charAt(int index) {
 			return mLine.charAt(index);
 		}
 
 		public int length() {
 			return mLine.length();
 		}
 
 		public CharSequence subSequence(int start, int end) {
 			return new LineWithPosition(mLine.subSequence(start, end),
 					mPosition, mLinePosition, mMode);
 		}
 
 		@Override
 		public String toString() {
 			
 			if (mLine == null) {
 				return "";
 			}
 			return mLine.toString();
 			
 			//return "----"+getLinePosition()+mLine.toString();
 		}
 
 		public long getColor() {
 			return mPosition;
 		}
 
 		public long getLinePosition() {
 			return mLinePosition;
 		}
 	}
 }
