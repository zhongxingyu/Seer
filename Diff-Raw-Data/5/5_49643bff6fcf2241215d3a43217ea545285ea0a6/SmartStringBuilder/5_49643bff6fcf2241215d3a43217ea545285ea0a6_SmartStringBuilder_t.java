 package sk.stuba.fiit.perconik.utilities;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 public final class SmartStringBuilder implements Appendable, CharSequence
 {
 	private static final String lineSeparator = System.lineSeparator();
 	
 	private final StringBuilder builder;
 	
 	private int indent;
 
 	private int delta;
 	
 	private CharSequence pad;
 	
 	private boolean newLine;
 	
 	public SmartStringBuilder()
 	{
 		this(16);
 	}
 
 	public SmartStringBuilder(int capacity)
 	{
 		this.builder = this.initialize(new StringBuilder(capacity));
 	}
 	
 	public SmartStringBuilder(CharSequence sequence)
 	{
 		this.builder = this.initialize(new StringBuilder(sequence));
 	}
 	
 	public SmartStringBuilder(String string)
 	{
 		this.builder = this.initialize(new StringBuilder(string));
 	}
 	
 	private final StringBuilder initialize(final StringBuilder builder)
 	{
 		this.indent  = 0;
 		this.delta   = 2;
 		this.pad     = " ";
 		this.newLine = true;
 		
 		return builder;
 	}
 
 	public final int length()
 	{
 		return this.builder.length();
 	}
 	
 	public final int indent()
 	{
 		return this.indent;
 	}
 
 	private final void ensureIndent()
 	{
 		if (this.newLine)
 		{
 			for (int i = 0; i < this.indent; i ++)
 			{
 				this.builder.append(this.pad);
 			}
 			
 			this.newLine = false;
 		}
 	}
 
 	public final int delta()
 	{
 		return this.delta;
 	}
 
 	public final int capacity()
 	{
 		return this.builder.capacity();
 	}
 
 	public final void ensureCapacity(int minimum)
 	{
 		this.builder.ensureCapacity(minimum);
 	}
 
 	public final void trimToSize()
 	{
 		this.builder.trimToSize();
 	}
 
 	public final void setLength(int length)
 	{
 		this.builder.setLength(length);
 	}
 	
 	public final void setIndent(int indent)
 	{
 		if (indent < 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		this.indent = indent;
 	}
 	
 	public final void setDelta(int delta)
 	{
 		if (delta < 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		this.delta = delta;
 	}
 	
 	public final void setPad(CharSequence pad)
 	{
 		if (pad == null)
 		{
 			throw new NullPointerException();
 		}
 		
 		this.pad = pad;
 	}
 
 	public final char charAt(int i)
 	{
 		return this.builder.charAt(i);
 	}
 
 	public final int codePointAt(int index)
 	{
 		return this.builder.codePointAt(index);
 	}
 
 	public final int codePointBefore(int index)
 	{
 		return this.builder.codePointBefore(index);
 	}
 
 	public final int codePointCount(int start, int end)
 	{
 		return this.builder.codePointCount(start, end);
 	}
 
 	public final int offsetByCodePoints(int index, int offset)
 	{
 		return this.builder.offsetByCodePoints(index, offset);
 	}
 
 	public final void getChars(int srcStart, int srcEnd, char[] dst, int dstStart)
 	{
 		this.builder.getChars(srcStart, srcEnd, dst, dstStart);
 	}
 
 	public final void setCharAt(int index, char c)
 	{
 		this.builder.setCharAt(index, c);
 	}
 
 	public final int indexOf(String s)
 	{
 		return this.builder.indexOf(s);
 	}
 
 	public final int indexOf(String s, int from)
 	{
 		return this.builder.indexOf(s, from);
 	}
 
 	public final int lastIndexOf(String s)
 	{
 		return this.builder.lastIndexOf(s);
 	}
 
 	public final int lastIndexOf(String s, int from)
 	{
 		return this.builder.lastIndexOf(s, from);
 	}
 
 	public final CharSequence subSequence(int start, int end)
 	{
 		return this.builder.subSequence(start, end);
 	}
 	
 	public final String subString(int start)
 	{
 		return this.builder.substring(start);
 	}
 
 	public final String subString(int start, int end)
 	{
 		return this.builder.substring(start, end);
 	}
 	
 	public final SmartStringBuilder reverse()
 	{
 		this.builder.reverse();
 		
 		return this;
 	}
 
 	@Override
 	public final String toString()
 	{
 		return this.builder.toString();
 	}
 
 	public final SmartStringBuilder append(Object o)
 	{
 		this.ensureIndent();
 		return this.append(String.valueOf(o));
 	}
 
 	public final SmartStringBuilder append(String s)
 	{
 		this.ensureIndent();
 		this.builder.append(s);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(CharSequence s)
 	{
 		this.ensureIndent();
 		this.builder.append(s);
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder append(CharSequence s, int start, int end)
 	{
 		this.ensureIndent();
 		this.builder.append(s, start, end);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(boolean b)
 	{
 		this.ensureIndent();
 		this.builder.append(b);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(char c)
 	{
 		this.ensureIndent();
 		this.builder.append(c);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(int i)
 	{
 		this.ensureIndent();
 		this.builder.append(i);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(long l)
 	{
 		this.ensureIndent();
 		this.builder.append(l);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(float f)
 	{
 		this.ensureIndent();
 		this.builder.append(f);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder append(double d)
 	{
 		this.ensureIndent();
 		this.builder.append(d);
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder appendln()
 	{
 		this.ensureIndent();
 		this.builder.append(lineSeparator);
 		
 		this.newLine = true;
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder appendln(String s)
 	{
 		this.ensureIndent();
 		this.builder.append(s);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(Object o)
 	{
 		return this.appendln(String.valueOf(o));
 	}
 
 	public final SmartStringBuilder appendln(CharSequence s)
 	{
 		this.ensureIndent();
 		this.builder.append(s);
 		
 		return this.appendln();
 	}
 	
 	public final SmartStringBuilder appendln(CharSequence s, int start, int end)
 	{
 		this.ensureIndent();
 		this.builder.append(s, start, end);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(boolean b)
 	{
 		this.ensureIndent();
 		this.builder.append(b);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(char c)
 	{
 		this.ensureIndent();
 		this.builder.append(c);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(int i)
 	{
 		this.ensureIndent();
 		this.builder.append(i);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(long l)
 	{
 		this.ensureIndent();
 		this.builder.append(l);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(float f)
 	{
 		this.ensureIndent();
 		this.builder.append(f);
 		
 		return this.appendln();
 	}
 
 	public final SmartStringBuilder appendln(double d)
 	{
 		this.ensureIndent();
 		this.builder.append(d);
 		
 		return this.appendln();
 	}
 	
 	public final SmartStringBuilder format(String format, Object ... args)
 	{
 		this.ensureIndent();		
 		this.builder.append(String.format(format, args));
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder list(Object ... objects)
 	{
 		this.ensureIndent();
 		
 		return this.list(Arrays.asList(objects));
 	}
 
 	public final SmartStringBuilder list(Iterable<?> iterable)
 	{
 		this.ensureIndent();
 		
 		return this.list(iterable, ", ");
 	}
 	
 	public final SmartStringBuilder list(Iterable<?> iterable, char delimeter)
 	{
 		this.ensureIndent();
 		
 		return this.list(iterable, String.valueOf(delimeter));
 	}
 	
 	public final SmartStringBuilder list(Iterable<?> iterable, String delimeter)
 	{
 		Iterator<?> iterator = iterable.iterator();
 		
 		if (iterator.hasNext())
 		{
 			this.ensureIndent();			
 			this.builder.append(iterator.next());
 			
 			while (iterator.hasNext())
 			{
 				this.builder.append(delimeter);
 				this.builder.append(iterator.next());
 			}
 		}
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder repeat(int n, Object o)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(o);
 	
 		return this;
 	}
 	
 	public final SmartStringBuilder repeat(int n, String s)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(s);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, CharSequence s)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(s);
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder repeat(int n, CharSequence s, int start, int end)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(s, start, end);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, boolean b)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(b);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, char c)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(c);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, int i)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(i);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, long l)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(l);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, float f)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(f);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, double d)
 	{
 		if (n < 0) throw new IllegalArgumentException();
 		if (n > 0) this.ensureIndent();
 		
 		while ((n --) > 0) this.builder.append(d);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder repeat(int n, String format, Object ... args)
 	{
 		this.repeat(n, String.format(format, args));
 		
 		return this;
 	}
 
 	public final SmartStringBuilder indent(int k)
 	{
 		this.setIndent(k);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder delta(int k)
 	{
 		this.setDelta(k);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder pad(CharSequence s)
 	{
 		this.setPad(s);
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder tab()
 	{
 		return this.tab(1);
 	}
 	
 	public final SmartStringBuilder tab(int k)
 	{
 		if (k < 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		this.indent = Math.min(this.indent + k * this.delta, Integer.MAX_VALUE);
 
 		return this;
 	}
 
 	public final SmartStringBuilder untab()
 	{
 		return this.untab(1);
 	}
 
 	public final SmartStringBuilder untab(int k)
 	{
 		if (k < 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		this.indent = Math.max(this.indent - k * this.delta, 0);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder lines(Object o)
 	{
		return this.lines(o);
 	}
 	
 	public final SmartStringBuilder lines(String s)
 	{
		for (String line: Arrays.asList(String.valueOf(s).split("\r?\n|\r")))
 		{
 			this.appendln(line);
 		}
 		
 		return this;
 	}
 
 	public final SmartStringBuilder delete(int start, int end)
 	{
 		this.builder.delete(start, end);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder deleteCharAt(int index)
 	{
 		this.builder.deleteCharAt(index);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder replace(int start, int end, String s)
 	{
 		this.builder.replace(start, end, s);
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder replaceFirst(String source, Object replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}	
 
 	public final SmartStringBuilder replaceFirst(String source, String replacement)
 	{
 		this.replaceAtIndex(this.builder.indexOf(source), source.length(), String.valueOf(replacement));
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder replaceFirst(String source, CharSequence replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, boolean replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, char replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, int replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, long replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, float replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceFirst(String source, double replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 	
 	public final SmartStringBuilder replaceLast(String source, Object replacement)
 	{
 		return this.replaceFirst(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, String replacement)
 	{
 		this.replaceAtIndex(this.builder.lastIndexOf(source), source.length(), String.valueOf(replacement));
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder replaceLast(String source, CharSequence replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, boolean replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, char replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, int replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, long replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, float replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceLast(String source, double replacement)
 	{
 		return this.replaceLast(source, String.valueOf(replacement));
 	}
 	
 	private final void replaceAtIndex(int index, int length, String replacement)
 	{
 		if (index != -1)
 		{
 			this.builder.replace(index, index + length, replacement);
 		}
 	}
 	
 	public final SmartStringBuilder replaceAll(String source, Object replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 	
 	public final SmartStringBuilder replaceAll(String source, String replacement)
 	{
 		int index;
 		
 		replacement = String.valueOf(replacement);
 		
 		while ((index = this.builder.indexOf(source)) != -1)
 		{
 			this.replace(index, index + source.length(), replacement);
 		}
 		
 		return this;
 	}
 
 	public final SmartStringBuilder replaceAll(String source, CharSequence replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, boolean replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, char replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, int replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, long replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, float replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder replaceAll(String source, double replacement)
 	{
 		return this.replaceAll(source, String.valueOf(replacement));
 	}
 
 	public final SmartStringBuilder insert(int index, char[] s, int offset, int length)
 	{
 		this.builder.insert(index, s, offset, length);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, Object o)
 	{
 		this.builder.insert(offset, o);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, String s)
 	{
 		this.builder.insert(offset, s);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, char[] s)
 	{
 		this.builder.insert(offset, s);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, CharSequence s)
 	{
 		this.builder.insert(offset, s, 0, s.length());
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, CharSequence s, int start, int end)
 	{
 		this.builder.insert(offset, s, start, end);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, boolean b)
 	{
 		this.builder.insert(offset, b);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, char c)
 	{
 		this.builder.insert(offset, c);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, int i)
 	{
 		this.builder.insert(offset, i);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, long l)
 	{
 		this.builder.insert(offset, l);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, float f)
 	{
 		this.builder.insert(offset, f);
 		
 		return this;
 	}
 
 	public final SmartStringBuilder insert(int offset, double d)
 	{
 		this.builder.insert(offset, d);
 		
 		return this;
 	}
 	
 	private final static char[] digits = new char[]
 	{
 		'0', '1', '2', '3',
 		'4', '5', '6', '7',
 		'8', '9', 'a', 'b',
 		'c', 'd', 'e', 'f'
 	};
 
 	public final SmartStringBuilder hex(byte[] bytes)
 	{
 		final int length = bytes.length;
 
 		if (length == 0)
 		{
 			return this;
 		}
 
 		final int mask = (1 << 4) - 1;
 		
 		this.ensureIndent();
 		this.builder.append(digits[(bytes[0] >>> 4) & mask]);
 		this.builder.append(digits[ bytes[0]        & mask]);
 		
 		for (int i = 1; i < length; i ++)
 		{
 			this.builder.append(' ');
 			
 			this.builder.append(digits[(bytes[i] >>> 4) & mask]);
 			this.builder.append(digits[ bytes[i]        & mask]);
 		}
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex8Bits(byte a)
 	{
 		this.hex(new byte[] {a});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex8Bits(short a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> shift) & 0xff))});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex8Bits(int a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> shift) & 0xff))});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex8Bits(long a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> shift) & 0xff))});
 		
 		return this;
 	}
 
 	public final SmartStringBuilder hex16Bits(byte a, byte b)
 	{
 		this.hex(new byte[] {a, b});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex16Bits(short a)
 	{
 		this.hex(new byte[] {(byte) (a >> 8), (byte) a});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex16Bits(int a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> (shift + 8)) & 0xff)),
 		                     ((byte) ((a >> (shift    )) & 0xff))});
 		
 		return this;
 	}
 
 	public final SmartStringBuilder hex16Bits(long a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> (shift + 8)) & 0xff)),
                              ((byte) ((a >> (shift    )) & 0xff))});
 		
 		return this;
 	}
 
 	public final SmartStringBuilder hex32Bits(byte a, byte b, byte c, byte d)
 	{
 		this.hex(new byte[] {a, b, c, d});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex32Bits(short a, short b)
 	{
 		this.hex(new byte[] {(byte) (a >> 8), (byte) a,
 		                     (byte) (b >> 8), (byte) b});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex32Bits(int a)
 	{
 		this.hex(new byte[] {(byte) (a >> 24), (byte) (a >> 16),
 		                     (byte) (a >>  8), (byte)  a       });
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex32Bits(long a, int shift)
 	{
 		this.hex(new byte[] {((byte) ((a >> (shift + 24)) & 0xff)),
                              ((byte) ((a >> (shift + 16)) & 0xff)),
                              ((byte) ((a >> (shift +  8)) & 0xff)),
                              ((byte) ((a >> (shift     )) & 0xff))});
 		
 		return this;
 	}
 
 	public final SmartStringBuilder hex48Bits(byte a, byte b, byte c, byte d, byte e, byte f)
 	{
 		this.hex(new byte[] {a, b, c, d, e, f});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex48Bits(short a, short b, short c)
 	{
 		this.hex(new byte[] {(byte) (a >> 8), (byte) a,
                              (byte) (b >> 8), (byte) b,
                              (byte) (c >> 8), (byte) c});
 		
 		return this;
 	}
 
 	public final SmartStringBuilder hex64Bits(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h)
 	{
 		this.hex(new byte[] {a, b, c, d, e, f, g, h});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex64Bits(short a, short b, short c, short d)
 	{
 		this.hex(new byte[] {(byte) (a >> 8), (byte) a,
                              (byte) (b >> 8), (byte) b,
                              (byte) (c >> 8), (byte) c,
                              (byte) (d >> 8), (byte) d});
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex64Bits(int a, int b)
 	{
 		this.hex(new byte[] {(byte) (a >> 24), (byte) (a >> 16),
                              (byte) (a >>  8), (byte)  a       ,
                              (byte) (b >> 24), (byte) (b >> 16),
                              (byte) (b >>  8), (byte)  b       });
 		
 		return this;
 	}
 	
 	public final SmartStringBuilder hex64Bits(long a)
 	{
 		this.hex(new byte[] {(byte) (a >> 56), (byte) (a >> 48),
 	                         (byte) (a >> 40), (byte) (a >> 32),
 	                         (byte) (a >> 24), (byte) (a >> 16),
 	                         (byte) (a >>  8), (byte)  a       });
 		
 		return this;
 	}
 
 	private static final String[] bits = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
 
 	private static final String[] bytes = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"};
 
 	private final SmartStringBuilder convert(long bytes, int unit, String[] symbols, int exp, int precision)
 	{
 		assert symbols != null;
 		
 		if (bytes < 0 || exp >= symbols.length || precision < 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		if (exp < 0)
 		{
 			exp = (int) (Math.log(bytes) / Math.log(unit));
 		}
 		
 	    if (bytes < unit)
 	    {
 	    	this.ensureIndent();
 	    	this.builder.append(bytes).append(' ').append(symbols[0]);
 	    	
 	    	return this;
 	    }
 
 	    this.ensureIndent();
 	    this.builder.append(String.format("%." + precision + "f %s", bytes / Math.pow(unit, exp), symbols[exp]));
 	    
 	    return this;
 	}
 
 	public final SmartStringBuilder bits(long bytes)
 	{
 		return this.bits(bytes, -1, 2);
 	}
 
 	public final SmartStringBuilder bits(long bytes, int unit, int precision)
 	{
 		return this.convert(bytes, 1000, SmartStringBuilder.bits, unit, precision);
 	}
 
 	public final SmartStringBuilder bytes(long bytes)
 	{
 		return this.bytes(bytes, -1, 2);
 	}
 
 	public final SmartStringBuilder bytes(long bytes, int unit, int precision)
 	{
 		return this.convert(bytes, 1024, SmartStringBuilder.bytes, unit, precision);
 	}
 }
