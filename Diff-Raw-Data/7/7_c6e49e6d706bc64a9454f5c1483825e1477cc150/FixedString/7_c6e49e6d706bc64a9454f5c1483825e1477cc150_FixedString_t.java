 package yang.graphics.font;
 
 import yang.util.YangList;
 
 public class FixedString {
 
 	public static char CHAR_DOT = '.';
 	public static int CHAR_LINEBREAK = '\n';
 	public static int CHAR_TAB = '\t';
 	public static int CHAR_SPACE = ' ';
 
 	public int mCapacity;
 	public int mLength;
 	public int[] mChars;
 	public int mMarker;
 	public MarkInfo[] mFormatStringMarks;
 	protected int mSpaceCount;
 
 	public FixedString() {
 
 	}
 
 	public FixedString(int capacity) {
 		alloc(capacity);
 	}
 
 	public FixedString alloc(int capacity) {
 		if(capacity>mCapacity) {
 			mCapacity = capacity;
 			mChars = new int[capacity];
 		}
 		mMarker = 0;
 		mLength = 0;
 		return this;
 	}
 
 	public FixedString allocString(String initialString, int capacity) {
 		alloc(capacity);
 		appendString(initialString);
 		return this;
 	}
 
 	public FixedString allocString(String string) {
 		return allocString(string,string.length());
 	}
 
 	public static boolean isDigit(char character) {
 		return character>='0' && character<='9';
 	}
 
 	protected void startFormatStringParse() {
 
 	}
 
 	protected int handleMacro(String macro,int curPos,int lastMacro) {
 		return curPos;
 	}
 
 	protected void endFormatStringParse(int pos,int lastMacro) {
 
 	}
 
 	public FixedString allocFormatString(String formatString) {
 		int markerCount = 0;
 		boolean escaped = false;
 		final YangList<MarkInfo> markList = new YangList<MarkInfo>();
 		int charCount = 0;
 		mSpaceCount = 0;
 
 		for(int i=0;i<formatString.length();i++) {
 			if(escaped) {
 				escaped = false;
 				charCount++;
 			}else{
 				final char ch = formatString.charAt(i);
 				if(ch=='\\')
 					escaped = true;
 				else{
 					if(ch=='%') {
 						markerCount++;
 						i++;
 						final int preI = i;
 						while(i<formatString.length() && isDigit(formatString.charAt(i)))
 							i++;
 						final int alloc = Integer.parseInt(formatString.substring(preI, i));
 						markList.add(new MarkInfo(charCount,alloc));
 						charCount += alloc;
 						if(i<formatString.length())
 							i--;
 					}else if(ch=='[') {
 						int p = formatString.indexOf("]", i+1);
 						if(p<=i)
 							p = formatString.length()-1;
 						charCount += 2;
 						i = p;
 					}else{
 						charCount++;
 						if(ch==' ' || ch=='\n' || ch=='\t')
 							mSpaceCount++;
 					}
 				}
 			}
 		}
 
 		mFormatStringMarks = new MarkInfo[markerCount];
 		int formatPos=0;
 		for(final MarkInfo markInfo:markList) {
 			mFormatStringMarks[formatPos] = markInfo;
 			formatPos++;
 		}
 
 		alloc(charCount);
 
 		int markInfoIndex = 0;
 		int lstMacro = -1;
 		startFormatStringParse();
 		final int l = formatString.length();
 		int bufPos = 0;
 		for(formatPos=0;formatPos<l;) {
 			if(escaped) {
 				escaped = false;
 				if(formatString.charAt(formatPos)=='%')
 					mChars[bufPos] = '%';
 				if(formatString.charAt(formatPos)=='\\')
 					mChars[bufPos] = '\\';
 				if(formatString.charAt(formatPos)=='n')
 					mChars[bufPos] = '\n';
 				if(formatString.charAt(formatPos)=='t')
 					mChars[bufPos] = '\t';
 				formatPos++;
 				bufPos++;
 			}else{
 				if(formatString.charAt(formatPos)=='\\'){
 					escaped = true;
 					formatPos++;
 				}else{
 					if(formatString.charAt(formatPos)=='%') {
 						final int count = mFormatStringMarks[markInfoIndex++].mLength;
 						for(int k=0;k<count;k++)
 							mChars[bufPos++] = 0;
 						while(++formatPos<formatString.length() && isDigit(formatString.charAt(formatPos)));
 					}else if(formatString.charAt(formatPos)=='[') {
 						int p = formatString.indexOf("]", formatPos+1);
 						if(p<0) {
 							break;
 						}
 						if(p<=formatPos)
 							p = formatString.length()-1;
 						final String macro = formatString.substring(formatPos+1,p);
 
 						final int m = handleMacro(macro,bufPos,lstMacro);
 						if(m!=-1)
 							bufPos = m;
 						formatPos = p+1;
 						lstMacro = bufPos;
 					}else{
 						final char ch = formatString.charAt(formatPos++);
 						mChars[bufPos] = ch;
 //						if(ch!=' ' && ch!='\t' && ch!='\n')
 //							charPos++;
 						bufPos++;
 					}
 
 				}
 			}
 		}
 		mLength = bufPos;
 		mMarker = mLength;
 
 		endFormatStringParse(charCount,lstMacro);
 
 		//System.out.println(mLength+" "+Util.arrayToString(mFormatStringMarks));
 		return this;
 	}
 
 	public void reset() {
 		mMarker = 0;
 		mLength = 0;
 	}
 
 	public void setString(String string) {
 		if(string.length()>mCapacity) {
 			allocString(string);
 		}
 		mMarker = 0;
 		appendString(string);
 		truncAtMarker();
 	}
 
 	public void setInt(int value) {
 		mMarker = 0;
 		appendInt(value,-1);
 		truncAtMarker();
 	}
 
 	public void setFloat(float value,int fracDigits) {
 		mMarker = 0;
 		appendFloat(value,fracDigits,-1);
 		truncAtMarker();
 	}
 
 	public void appendString(String string,int minCharacterCount) {
 		final int l= string.length();
 		for(int i=0;i<l;i++) {
 			mChars[mMarker+i] = string.charAt(i);
 		}
 		mMarker += l;
 		if(minCharacterCount>0) {
 			minCharacterCount-=l;
 			for(int i=0;i<minCharacterCount;i++)
 				mChars[mMarker++] = 0;
 		}
 		if(mMarker>=mLength)
 			mLength = mMarker;
 	}
 
 	public void appendLineBreak() {
 		appendString("\n");
 	}
 
 	public void appendString(String string) {
 		final int l= string.length();
 		for(int i=0;i<l;i++) {
 			mChars[mMarker+i] = string.charAt(i);
 		}
 		mMarker += l;
 		if(mMarker>=mLength)
 			mLength = mMarker;
 	}
 
 	public void appendStringAtMark(int markIndex,String string) {
 		setMarker(mFormatStringMarks[markIndex].mPos);
 		appendString(string,mFormatStringMarks[markIndex].mLength);
 	}
 
 	public void appendInt(int value,int minCharacterCount) {
 		int c=0;
 		if(value==0) {
 			mChars[mMarker++] = '0';
 			c = 1;
 		}else{
 			if(value<0) {
 				mChars[mMarker++] = '-';
 				value = -value;
				c = 1;
 			}
 
 			while(value>0) {
 				mChars[mMarker+c] = '0'+value%10;
 				value/=10;
 				c++;
 			}
 			//Swap
 			for(int i=0;i<c/2;i++) {
 				final int h=mChars[mMarker+i];
 				mChars[mMarker+i] = mChars[mMarker+c-i-1];
 				mChars[mMarker+c-i-1] = h;
 			}
 			mMarker+=c;
 		}
 		if(minCharacterCount>0) {
 			minCharacterCount-=c;
 			for(int i=0;i<minCharacterCount;i++)
 				mChars[mMarker++] = 0;
 		}
 
 		if(mMarker>mLength)
 			mLength = mMarker;
 	}
 
 	public void appendInt(int value) {
 		appendInt(value,-1);
 	}
 
 	public void appendIntAtMark(int markIndex,int value) {
 		setMarker(mFormatStringMarks[markIndex].mPos);
 		appendInt(value,mFormatStringMarks[markIndex].mLength);
 	}
 
 	public void appendFloat(float value,int fracDigits,int minCharacterCount) {
 
 		int fac = 1;
 		for(int i=0;i<fracDigits;i++)
 			fac*=10;
 		value *= fac;
 		int intVal = (int)(value+0.4999999f);
 
		int c=0;
 		if(intVal<0) {
 			mChars[mMarker++] = '-';
 			intVal = -intVal;
			c = 1;
 		}
 
 		int lastVal = 0;
 		if(intVal<fac) {
 			intVal += fac;
 			mChars[mMarker++] = '0';
 			lastVal = 1;
 		}
 
 		while(intVal>lastVal) {
 			mChars[mMarker+c] = '0'+intVal%10;
 			intVal/=10;
 			c++;
 			if(c==fracDigits) {
 				mChars[mMarker+ c] = CHAR_DOT;
 				c++;
 			}
 		}
 		//Swap
 		for(int i=0;i<c/2;i++) {
 			final int h=mChars[mMarker+i];
 			mChars[mMarker+i] = mChars[mMarker+c-i-1];
 			mChars[mMarker+c-i-1] = h;
 		}
 		mMarker+=c;
 
 		if(minCharacterCount>0) {
 			minCharacterCount-=c+1;
 			for(int i=0;i<minCharacterCount;i++)
 				mChars[mMarker++] = 0;
 		}
 
 		if(mMarker>mLength)
 			mLength = mMarker;
 	}
 
 	public void appendFloat(float value,int fracDigits) {
 		appendFloat(value,fracDigits,-1);
 	}
 
 	public void appendFloatAtMark(int markIndex,float value,int fracDigits) {
 		setMarker(mFormatStringMarks[markIndex].mPos);
 		appendFloat(value,fracDigits,mFormatStringMarks[markIndex].mLength);
 	}
 
 	public void setMarker(int position) {
 		mMarker = position;
 	}
 
 	public void recalculateLength() {
 		for(int i=0;i<mCapacity;i++) {
 			if(mChars[i]==0) {
 				mLength = i;
 				return;
 			}
 		}
 		mLength = mCapacity;
 	}
 
 	public String createRawString() {
 		final StringBuilder result = new StringBuilder(mLength);
 		for(int i=0;i<mLength;i++) {
 			final int val = mChars[i];
 			if(val>0 && val<255)
 				result.append((char)val);
 			else
 				result.append('['+val+']');
 		}
 		return result.toString();
 	}
 
 	public String bufferToString() {
 		final StringBuilder result = new StringBuilder();
 		for(int i=0;i<mCapacity;i++) {
 			if(i==0)
 				result.append(" ");
 			result.append(mChars[i]);
 		}
 		return result.toString();
 	}
 
 	@Override
 	public String toString() {
 		return "Mark/Len/Cap="+mMarker+"/"+mLength+"/"+mCapacity+"; String="+createRawString();
 	}
 
 	public FixedString truncAt(int index) {
 		if(index<mCapacity)
 			mChars[index] = 0;
 		mLength = index;
 		return this;
 	}
 
 	public FixedString truncAtMarker() {
 		if(mMarker<mCapacity)
 			mChars[mMarker] = 0;
 		mLength = mMarker;
 		return this;
 	}
 
 	public void appendZeros(int count) {
 		for(int i=0;i<count;i++)
 			mChars[mMarker++] = 0;
 	}
 
 	public void appendZerosUntil(int index) {
 		while(mMarker<=index)
 			mChars[mMarker++] = 0;
 	}
 
 	public void appendStringRightJustified(String string, int characters) {
 		final int index = characters-string.length();
 		while(mMarker<=index)
 			mChars[mMarker++] = ' ';
 		appendString(string);
 	}
 
 }
