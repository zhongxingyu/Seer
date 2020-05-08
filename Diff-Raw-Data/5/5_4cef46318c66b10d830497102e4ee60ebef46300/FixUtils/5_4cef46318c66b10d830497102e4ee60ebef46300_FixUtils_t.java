 package org.tomac.protocol.fix;
 
 import java.nio.ByteBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.tomac.protocol.fix.messaging.FixMessageInfo;
 import org.tomac.protocol.fix.messaging.FixMessageInfo.MessageTypes;
 import org.tomac.protocol.fix.messaging.FixMessageListener;
 import org.tomac.protocol.fix.messaging.FixStandardHeader;
 import org.tomac.protocol.fix.messaging.FixTags;
 
 public class FixUtils {
 	private static FixUtils						fixUtils							= new FixUtils();
 
 	public static final int						FIX_HEADER							= 5 + FixTags.BEGINSTRING_LENGTH;						// '8' + '=' + BeginString[7] + SOH + '9' + '='
 	public static final int						FIX_MESSAGE_START					= FIX_HEADER + 5;										// '8' + '=' + BeginString[7] + SOH + '9' + '=' '0000' + SOH
 	public static final int						FIX_TRAILER							= 7;													// '10' + = + checkSum[3] + SOH
 
 	public static final byte					SOH									= (byte) 0x01;
 	public static final byte					EQUALS								= (byte) '=';
 	public static final byte					DECIMAL								= (byte) '.';
 
 	public static final short					TAG_HAS_NO_VALUE					= 0;
 	public static final short					TAG_HAS_VALUE						= -1;
 	// public static final short BUFFER_HAS_TAG > 0;
 
 	public static int							FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS	= 2;
 	public static int							FIX_FLOAT_NUMBER_OF_DECIMALS		= 100;
 	public static final int						FIX_MAX_DIGITS						= 19;
 	public static final int						FIX_MAX_TAG_LENGTH					= 4;
 	public static final int						FIX_MAX_INT_LENGTH					= 19;
 	public static final int						FIX_MAX_STRING_LENGTH				= 64;
 	public static final int						UTCTIMESTAMP_LENGTH					= 24;
 	public static final int						CURRENCY_LENGTH						= 3;
 	public static final int						FIX_MAX_STRING_TEXT_LENGTH			= 128;
 	public static final int						FIX_MAX_NOINGROUP					= 5;
 
 	private static byte[]						beginsStringTmp						= new byte[FixTags.BEGINSTRING_LENGTH];
 
 	public static boolean						isNasdaqOMX							= true;
 
 	private static byte[]						calcCheckSum						= new byte[FixTags.CHECKSUM_LENGTH];
 
 	private static byte[]						currCheckSum						= new byte[FixTags.CHECKSUM_LENGTH];
 
 	public static boolean						validateChecksum					= true;
 
 	public static boolean						validateSession						= true;
 
 	public static boolean						validateSendingTime					= true;
 
 	public static boolean						validateOnlyDefinedTagsAllowed		= true;
 
 	public static final Calendar				calendarUTC							= new GregorianCalendar(TimeZone.getTimeZone("UTC"));
 
 	public static final UtcTimestampConverter	utcTimestampConverter				= fixUtils.new UtcTimestampConverter();
 
 	public static final UtcTimeOnlyConverter	utcTimeOnlyConverter				= fixUtils.new UtcTimeOnlyConverter();
 
 	public static final UtcDateOnlyConverter	utcDateOnlyConverter				= fixUtils.new UtcDateOnlyConverter();
 
 	private static byte[]						tmpMsgType							= new byte[FixTags.MSGTYPE_LENGTH];
 
 	private static byte[]						digitsBuf							= new byte[FixUtils.FIX_MAX_DIGITS];
 
 	public static int getTag(final ByteBuffer buf, final FixValidationError err) {
 		int count = 0;
 		int tag = 0;
 		int c;
 
 		while (buf.hasRemaining()) {
 			c = buf.get();
 			if (c == '=')
 				break;
 
 			if (c == FixUtils.SOH) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.NON_DATA_VALUE_INCLUDES_FIELD_DELIMITER_SOH_CHARACTER, "Tag number terminated by SOH");
 				return getNext(buf, null);
 			}
 
 			if (!FixUtils.isNumeric(c)) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.INVALID_TAG_NUMBER, "Invalid tag number");
 				return getNext(buf, null);
 			}
 
 			tag = tag * 10 + c - '0';
 
 			if (++count > FixUtils.FIX_MAX_TAG_LENGTH) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.INVALID_TAG_NUMBER, "Tag number exceeds max allowed digits");
 				return getNext(buf, null);
 			}
 		}
 		return tag;
 	}
 
 	public static int getNext(final ByteBuffer buf, final FixValidationError err) {
 		int c = 0;
 
 		while (buf.hasRemaining()) {
 			c++;
 
 			if (buf.get() == FixUtils.SOH)
 				return c;
 		}
 		if (err != null)
 			err.setError((int) FixMessageInfo.SessionRejectReason.INVALID_TAG_NUMBER, "Invalid tag number");
 		return c;
 	}
 
 	/**
 	 * Find tag in buffer. position set to tag data if found, else unchanged.
 	 * 
 	 * @param tag
 	 * @param buf
 	 * @param err
 	 * @return
 	 */
 	public static boolean getTag(final int tag, final ByteBuffer buf, final FixValidationError err) {
 		final int pos = buf.position();
 
 		while (tag != getTag(buf, err) && buf.hasRemaining()) {
 			if (err.hasError()) {
 				buf.position(pos);
 				return false;
 			}
 			getNext(buf, err);
 			if (err.hasError()) {
 				buf.position(pos);
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static int getMsgTypeAsInt(final byte[] b, final int length) {
 		int val = 0;
 
 		val |= b[0];
 
 		if (length > 1)
 			for (int i = 1; i < length; i++) {
 				if (b[i] == 0)
 					continue;
 				val <<= 8;
 				val |= b[i];
 			}
 
 		return val;
 	}
 
 	public static int getMsgTypeAsByteArray(byte[] dest, final int val) {
 		int length = dest.length;
 		fillNul(dest);
 
 		dest[0] = (byte) val; // TODO my brain is small
 
 		return val;
 	}
 
 	public static byte getTagCharValue(final ByteBuffer buf, final FixValidationError err) {
 		byte c = '0';
 
 		if (buf.hasRemaining()) {
 
 			c = buf.get();
 
 			if (c == FixUtils.SOH) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.TAG_SPECIFIED_WITHOUT_A_VALUE, "Premature end of buffer missing SOH");
 				return c;
 			}
 		}
 
 		final int count = getNext(buf, null);
 
 		if (count > 1) {
 			err.setError((int) FixMessageInfo.SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, "Integer value length exceeds one character");
 			return c;
 		}
 
 		return c;
 	}
 
 	public static long getTagFloatValue(final ByteBuffer buf, final FixValidationError err) {
 		byte c;
 		int start = 0;
 		final int end = FixUtils.FIX_MAX_DIGITS;
 
 		FixUtils.fillNul(digitsBuf);
 
 		while (buf.hasRemaining()) {
 
 			if ((c = buf.get()) == FixUtils.SOH)
 				break;
 
 			digitsBuf[start++] = c;
 
 			if (start == end) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG,
 						"Value is incorrect or out of range for this tag" + FixUtils.FIX_MAX_DIGITS);
 				return getNext(buf, null);
 			}
 		}
 		try {
 			long val = FixUtils.fixFloatValueOf(digitsBuf, start);
 			return val;
 		} catch (NumberFormatException n) {
 			err.setError((int) FixMessageInfo.SessionRejectReason.INCORRECT_DATA_FORMAT_FOR_VALUE, "Incorrect data format for value");
 		}
 		return 0;
 	}
 
 	public static int getTagIntValue(final ByteBuffer buf, final FixValidationError err) {
 		byte c;
 		int start = 0;
 		final int end = FixUtils.FIX_MAX_DIGITS;
 
 		while (buf.hasRemaining()) {
 
 			if ((c = buf.get()) == FixUtils.SOH)
 				break;
 
 			digitsBuf[start++] = c;
 
 			if (start == end) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG,
 						"Value length exceeds maximum number of digits " + FixUtils.FIX_MAX_DIGITS);
 				return getNext(buf, null);
 			}
 		}
 
 		return FixUtils.intValueOf(digitsBuf, 0, start);
 	}
 
 	public static int getTagStringValue(final ByteBuffer src, final byte[] dst, int start, final int end, final FixValidationError err) {
 		byte c;
 		final int oldPos = src.position();
 
 		FixUtils.fillNul(dst);
 
 		while (src.hasRemaining()) {
 
 			if ((c = src.get()) == FixUtils.SOH)
 				break;
 
 			if (start >= end) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, "Value length exceeds maximum of "
 						+ (end - start));
 				return getNext(src, null);
 			} else {
 				dst[start] = c;
 				start++;
 			}
 		}
 		return src.position() - oldPos - 1;
 	}
 
 	/**
 	 * Used for standard trailer in order rewind buffer situated in next tag.
 	 * 
 	 * @param tag
 	 * @param buf
 	 */
 	public static void unreadLastTag(final int tag, final ByteBuffer buf) {
 		// take the standard case first
 		if (tag == 8) {
 			buf.position(buf.position() - 2);
 			return;
 		}
 
 		int i = buf.position();
 		while (buf.get(--i) != SOH) {
 		}
 		buf.position(i + 1);
 	}
 
 	public static void copy(final byte dst[], final byte src[]) {
 		int length = src.length > dst.length ? dst.length : src.length;
 
 		System.arraycopy(src, 0, dst, 0, length);
 	}
 
 	public static boolean equals(final byte[] x, final byte[] y) {
 
 		int len = x.length < y.length ? x.length : y.length;
 		int offset = 0;
 
 		while (len > 0) {
 			if (x[offset] != y[offset])
 				return false;
 
 			offset++;
 			len--;
 		}
 
 		return true;
 	}
 
 	public static void fillNul(final byte[] buf) {
 		for (int i = 0; i < buf.length; i++)
 			buf[i] = (byte) 0;
 	}
 
	public static void fill(final byte[] buf, byte b) {
		for (int i = 0; i < buf.length; i++)
			buf[i] = b;
	}

 	public static long fixFloatValueOf(final byte[] s, int length) {
 		int start = 0;
 		boolean negative = false;
 		int decimal = -1;
 		long value;
 
 		final byte c = s[start];
 		if (c == '-') {
 			++start;
 			--length;
 			negative = true;
 		} /*
 		 * else if (c == '+') { ++start; --length; }
 		 */
 		if (length == 0)
 			throw new NumberFormatException("to short number");
 
 		for (decimal = length - 1; decimal >= 0; decimal--)
 			if (s[decimal] == (byte) '.')
 				break;
 
 		final int decimals = length - 1 - decimal;
 
 		if (decimal > -1) {
 			value = longValueOf(s, 0, decimal) * FIX_FLOAT_NUMBER_OF_DECIMALS;
 			value += intValueOf(s, decimal + 1 < length ? decimal + 1 : length - 1,
 					FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS < decimals ? FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS : decimals);
 		} else
 			value = longValueOf(s, 0, decimals) * FIX_FLOAT_NUMBER_OF_DECIMALS;
 
 		if (negative)
 			return -1 * value;
 		return value;
 
 	}
 
 	public static void generateCheckSum(final byte[] checkSum, final ByteBuffer buf, final int startPos, final int endPos) {
 		final int currPos = buf.position();
 		int cks = 0;
 		final int bufLen = endPos - startPos;
 
 		buf.position(startPos);
 		for (int i = 0; i < bufLen; i++)
 			cks += buf.get();
 		FixUtils.longToNumeric(checkSum, 0, cks % 256, checkSum.length);
 		buf.position(currPos);
 	}
 
 	public static int intValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && b[pos] == (byte) 0) {
 			pos++;
 			len--;
 		}
 
 		int sign = 1;
 
 		if (b[pos] == '+') {
 			sign = 1;
 			len--;
 			pos++;
 		} else if (b[pos] == '-') {
 			sign = -1;
 			len--;
 			pos++;
 		}
 
 		int x = 0;
 
 		while (len > 0 && b[pos] >= '0' && b[pos] <= '9') {
 
 			x *= 10;
 
 			x += b[pos] - '0';
 
 			len--;
 
 			pos++;
 		}
 
 		if (len != 0)
 			throw new NumberFormatException();
 
 		return sign * x;
 	}
 
 	public static final boolean isNumeric(final int b) {
 
 		return b >= '0' && b <= '9';
 	}
 
 	public static void longToFixFloat(final byte out[], final int offset, long l, final int length) {
 		final int radix = 10;
 
 		if (l == 0) {
 			out[offset] = (byte) '0';
 			return;
 		}
 
 		int count = 2;
 		long j = l;
 		final boolean negative = l < 0;
 		if (!negative) {
 			count = 1;
 			j = -l;
 		}
 		count++; // the decimal
 		int decimalPos = length - offset - FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS;
 
 		while ((l /= radix) != 0 && count < length - offset)
 			count++;
 
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			if (count == decimalPos - 1)
 				out[--count] = (byte) '.';
 			out[--count] = (byte) ch;
 
 		} while ((j /= radix) != 0);
 		if (negative)
 			out[0] = (byte) '-';
 
 	}
 
 	public static void longToNumeric(final byte out[], final int offset, long l, final int length) {
 		final int radix = 10;
 
 		if (l == 0) {
 			out[offset] = (byte) '0';
 			return;
 		}
 
 		int count = 2;
 		long j = l;
 		final boolean negative = l < 0;
 		if (!negative) {
 			count = 1;
 			j = -l;
 		}
 		while ((l /= radix) != 0)
 			count++;
 
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			out[--count] = (byte) ch;
 
 		} while ((j /= radix) != 0);
 		if (negative)
 			out[0] = (byte) '-';
 
 	}
 
 	public static long longValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && b[pos] == (byte) 0) {
 			pos++;
 			len--;
 		}
 
 		long x = 0;
 
 		while (len > 0 && b[pos] >= '0' && b[pos] <= '9') {
 
 			x *= 10;
 
 			x += b[pos] - '0';
 
 			len--;
 
 			pos++;
 
 		}
 
 		if (len != 0)
 			throw new NumberFormatException();
 
 		return x;
 
 	}
 
 	public static void put(final ByteBuffer out, final byte b) {
 		out.put(b);
 	}
 
 	public static void put(final ByteBuffer out, final byte[] buf) {
 		for (final byte b : buf) {
 			if (b == SOH || b == 0)
 				break;
 
 			out.put(b);
 		}
 	}
 
 	public static void put(final ByteBuffer out, long l) {
 		final int radix = 10;
 		final int endPos;
 
 		if (l == 0) {
 			out.put((byte) '0');
 			return;
 		}
 
 		int count = 2;
 		long j = l;
 		final boolean negative = l < 0;
 		if (!negative) {
 			count = 1;
 			j = -l;
 		}
 		while ((l /= radix) != 0)
 			count++;
 
 		int index = endPos = out.position() + count;
 
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			index--;
 			count--;
 			out.position(index);
 			out.put((byte) ch);
 
 		} while ((j /= radix) != 0);
 		if (negative) {
 			out.position(--index);
 			out.put((byte) '-');
 		}
 
 		out.position(endPos);
 	}
 
 	/**
 	 * Allocates byte[] copy without leading and trailing white spaces.
 	 * 
 	 * @param buf
 	 * @return byte array without trailing and leading whitespace characters
 	 */
 	public static byte[] trim(final byte[] buf) {
 		int start = 0;
 		for (final byte b : buf) {
 			if (b > 0x20)
 				break;
 			start++;
 		}
 		int end = buf.length;
 		for (int i = buf.length - 1; i > 0; i--) {
 			if (buf[i] > 0x20)
 				break;
 			end--;
 		}
 
 		final byte[] ret = new byte[end - start > 0 ? end - start : buf.length];
 		System.arraycopy(buf, start, ret, 0, ret.length);
 		return ret;
 	}
 
 	public static int getNoInGroup(FixGroup[] groups) {
 		int i = 0;
 		for (FixGroup group : groups) {
 			if (!group.hasGroup())
 				break;
 			i++;
 		}
 		return i;
 	}
 
 	/**
 	 * 
 	 * yyyyMMdd-HH:mm:SS[.sss] HH:mm:SS[.sss] yyyyMMdd
 	 * 
 	 **/
 	public static Date convert(byte[] buf, boolean getTime, boolean getDate) {
 
 		if (getTime && getDate && buf.length < 18)
 			return null;
 		else if (getTime && buf.length < 9)
 			return null;
 		else if (getDate && buf.length < 8)
 			return null;
 
 		int year = 0;
 		int month = 0;
 		int date = 0;
 		int hourOfDay = 0;
 		int minute = 0;
 		int second = 0;
 
 		int pos = 0;
 
 		if (getDate) {
 			year = (int) FixUtils.longValueOf(buf, 0, 4);
 			month = -1 + (int) FixUtils.longValueOf(buf, 4, 2);
 			date = (int) FixUtils.longValueOf(buf, 6, 2);
 			pos = 9;
 		} else {
 			year = (int) calendarUTC.get(Calendar.YEAR);
 			month = (int) calendarUTC.get(Calendar.MONTH);
 			date = (int) calendarUTC.get(Calendar.DATE);
 		}
 
 		if (getTime) {
 			hourOfDay = (int) FixUtils.longValueOf(buf, pos, 2);
 			minute = (int) FixUtils.longValueOf(buf, pos + 3, 2);
 			second = (int) FixUtils.longValueOf(buf, pos + 6, 2);
 			pos += 8;
 		}
 
 		calendarUTC.clear();
 		calendarUTC.set(year, month, date, hourOfDay, minute, second);
 		long basMillis = calendarUTC.getTimeInMillis();
 
 		long millis = 0;
 
 		if (buf.length == pos + 4) {
 			millis = FixUtils.longValueOf(buf, pos + 1, 3);
 		}
 
 		calendarUTC.setTimeInMillis(basMillis + millis);
 
 		return calendarUTC.getTime();
 	}
 
 	public static class FixFloatConverter {
 
 		public static String convert(long l) {
 			return convert(l, FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS);
 		}
 
 		public static long convert(byte[] buf) {
 			return FixUtils.fixFloatValueOf(buf, buf.length);
 		}
 
 		public static String convert(long l, int count) {
 			double d = l;
 
 			while (count > 0) {
 				d /= 10;
 				count--;
 			}
 			return String.valueOf(d);
 		}
 
 	}
 
 	public static class CharConverter {
 
 		public static String convert(char c) {
 			return String.valueOf(c);
 		}
 
 		public static byte convert(String s) {
 			byte[] buf = s.getBytes();
 			return buf.length == 1 ? buf[0] : null;
 		}
 
 	}
 
 	public static class BooleanConverter {
 
 		public static String convert(boolean b) {
 			return b ? "Y" : "N";
 		}
 
 		public static boolean convert(String s) {
 			if (s.equals("Y"))
 				return true;
 			else if (s.equals("N"))
 				return false;
 			else
 				throw new NumberFormatException();
 		}
 
 	}
 
 	public class UtcTimestampConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
 
 		public UtcTimestampConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		// "yyyyMMdd-HH:mm:SS[.sss]"
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, true, true);
 		}
 
 	}
 
 	public class UtcTimeOnlyConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("HH:mm:ss.SSS");
 
 		UtcTimeOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, true, false);
 		}
 
 	}
 
 	public class UtcDateOnlyConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("yyyyMMdd");
 
 		UtcDateOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 
 			calendarUTC.setTime(date);
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, false, true);
 		}
 
 	}
 
 	public static Date getSystemTime() {
 		return new Date();
 	}
 
 	/**
 	 * -------------------------- FIX MESSAGE PARSE ------------------------------------------
 	 **/
 	// this is the FixMessageParesr
 	/**
 	 * -------------------------- FIX MESSAGE SESSION ----------------------------------------
 	 **/
 
 	public static IFixSession crackSession(int msgTypeInt, FixMessageListener listener, long connectorID, FixStandardHeader standardHeader,
 			FixValidationError err) {
 		IFixSession session;
 		int pos = standardHeader.buf.position();
 
 		if (MessageTypes.LOGON_INT == msgTypeInt) {
 			session = listener.getSession(connectorID, standardHeader, err);
 		} else {
 			session = listener.getSession(connectorID, err);
 		}
 
 		if (!err.hasError() && session != null && MessageTypes.LOGON_INT == msgTypeInt) {
 
 			if (!err.hasError() && FixUtils.validateSession) {
 
 				if (standardHeader.getMsgSeqNum() > session.getInMsgSeqNum() + 1) { // if we get a logout continue anyhow
 
 					err.setError((int) FixEvent.MSGSEQNUM_LOGON_RESENDREQUEST, "MsgSeqNum higher than expected", FixTags.MSGSEQNUM_INT, msgTypeInt);
 					err.resendRequestMsgSeqNum = session.getInMsgSeqNum() + 1;
 
 				} else if (standardHeader.getMsgSeqNum() < session.getInMsgSeqNum() && !(standardHeader.hasPossDupFlag() && standardHeader.getPossDupFlag())) {
 
 					err.setError(FixEvent.MSGSEQNUM_LOGOUT, "MsgSeqNum too low, expecting " + (session.getInMsgSeqNum() + 1) + " but received "
 							+ standardHeader.getMsgSeqNum(), FixTags.MSGSEQNUM_INT, msgTypeInt);
 
 				}
 			}
 
 		} else if (!err.hasError() && session != null) {
 
 			err.session = session;
 
 			if (!err.hasError() && FixUtils.validateSession) {
 
 				if (standardHeader.getMsgSeqNum() > session.getInMsgSeqNum() + 1 && msgTypeInt != MessageTypes.LOGOUT_INT) { // if we get a logout continue anyhow
 
 					err.setError((int) FixEvent.MSGSEQNUM_RESENDREQUEST, "MsgSeqNum higher than expected", FixTags.MSGSEQNUM_INT, msgTypeInt);
 					err.resendRequestMsgSeqNum = session.getInMsgSeqNum() + 1;
 
 				} else if (standardHeader.getMsgSeqNum() <= session.getInMsgSeqNum()) {
 
 					if (MessageTypes.SEQUENCERESET_INT == msgTypeInt) {
 						if (standardHeader.hasPossDupFlag() && standardHeader.getPossDupFlag()) {
 							err.setError(FixEvent.IGNORE_MESSAGE, "Ignore SequenceReset");
 						} // else -> do in the parser as we need to know GapFill flag settings :-(
 					} else if (standardHeader.hasPossDupFlag() && standardHeader.getPossDupFlag()) {
 						err.setError(FixEvent.IGNORE_MESSAGE, "Ignore possdup message");
 					} else {
 						err.setError(FixEvent.MSGSEQNUM_LOGOUT, "MsgSeqNum too low, expecting " + (session.getInMsgSeqNum() + 1) + " but received "
 								+ standardHeader.getMsgSeqNum(), FixTags.MSGSEQNUM_INT, msgTypeInt);
 					}
 
 				}
 
 				if (!err.hasError() && session != null) {
 					if (!session.validate(standardHeader))
 						err.setError((int) FixMessageInfo.SessionRejectReason.COMPID_PROBLEM, "CompID problem");
 
 				}
 
 			}
 
 		} else if (session == null) {
 
 			err.setError(FixEvent.DISCONNECT, "first message not a logon");
 			// or
 			// this is not the first non-garbled messasge but I am still unable to get the session!!!!
 
 		} else {
 			// fall throu
 		}
 
 		standardHeader.buf.position(pos);
 		err.session = session;
 		// err.resendRequestMsgSeqNum = standardHeader.getMsgSeqNum();
 		return session;
 
 	}
 
 	/**
 	 * ------------------------------------------------------------------------------------------------- FIX HEADER ------------------------------------------------------------------------------------------------
 	 **/
 
 	public static int crackStandardHeader(ByteBuffer buf, FixStandardHeader standardHeader, FixValidationError err) {
 
 		int tag = getTag(buf, err);
 
 		tag = standardHeader.setBuffer(tag, buf, err);
 
 		if (!err.hasError())
 			standardHeader.hasRequiredTags(err);
 
 		FixUtils.unreadLastTag(tag, buf);
 		int pos = buf.position();
 
 		if (err.hasError()) {
 			if (err.refTagID == FixTags.TARGETCOMPID_INT || err.refTagID == FixTags.TARGETSUBID_INT || err.refTagID == FixTags.SENDERCOMPID_INT
 					|| err.refTagID == FixTags.SENDERSUBID_INT) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.COMPID_PROBLEM, "CompID problem");
 			}
 		} else {
 			validateSendingTime(err, standardHeader);
 
 		}
 		buf.position(pos);
 		return buf.position();
 	}
 
 	private static void validateSendingTime(FixValidationError err, FixStandardHeader standardHeader) {
 		if (validateSendingTime && !err.hasError()) {
 
 			try {
 
 				long now = FixUtils.getSystemTime().getTime();
 				long sendingTime = FixUtils.utcTimestampConverter.convert(standardHeader.getSendingTime()).getTime();
 
 				if (sendingTime > now + (60 * 2 * 1000L) || sendingTime < now - (60 * 2 * 1000L)) {
 
 					err.setError((int) FixMessageInfo.SessionRejectReason.SENDINGTIME_ACCURACY_PROBLEM, "SendingTime accuracy problem", FixTags.SENDINGTIME_INT);
 					err.refSeqNum = standardHeader.getMsgSeqNum();
 				}
 
 				if (standardHeader.hasPossDupFlag() && standardHeader.getPossDupFlag()) {
 					if (!standardHeader.hasOrigSendingTime()) {
 						err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "Required tag missing", FixTags.ORIGSENDINGTIME_INT);
 						err.refSeqNum = standardHeader.getMsgSeqNum();
 					} else {
 						try {
 							long origSendingTime = FixUtils.utcTimestampConverter.convert(standardHeader.getOrigSendingTime()).getTime();
 							if (origSendingTime > sendingTime) {
 								err.setError((int) FixMessageInfo.SessionRejectReason.SENDINGTIME_ACCURACY_PROBLEM, "SendingTime accuracy problem",
 										FixTags.ORIGSENDINGTIME_INT);
 								err.refSeqNum = standardHeader.getMsgSeqNum();
 							}
 						} catch (NumberFormatException n) {
 							err.setError((int) FixMessageInfo.SessionRejectReason.SENDINGTIME_ACCURACY_PROBLEM, "SendingTime accuracy problem",
 									FixTags.ORIGSENDINGTIME_INT);
 							err.refSeqNum = standardHeader.getMsgSeqNum();
 						}
 					}
 
 				}
 			} catch (NumberFormatException n) {
 				err.setError((int) FixMessageInfo.SessionRejectReason.SENDINGTIME_ACCURACY_PROBLEM, "SendingTime accuracy problem", FixTags.SENDINGTIME_INT);
 				err.refSeqNum = standardHeader.getMsgSeqNum();
 			}
 
 		}
 
 	}
 
 	/**
 	 * ----------------------- FIX MESSAGE TYPE -------------------------------------------------------
 	 **/
 
 	/**
 	 * non-intrucive cracks the msgType from IO ByteBuffer. buf position rewined to starting pos buf after read.
 	 * 
 	 * @param buf
 	 * @param backingBuf
 	 * @param err
 	 * @return
 	 */
 	public static int crackMsgType(final ByteBuffer buf, final FixValidationError err) {
 		int msgTypeInt = -1;
 		FixUtils.fillNul(tmpMsgType);
 
 		final int startPos = buf.position();
 
 		// see Fix Session Protocol v1.1 "What constitutes a garbled message"..
 		if (FixUtils.equals(buf.array(), FixMessageInfo.BEGINSTRING_VALUE_WITH_TAG) && buf.limit() >= FixMessageInfo.BEGINSTRING_VALUE_WITH_TAG.length) {
 
 			buf.position(FixMessageInfo.BEGINSTRING_VALUE_WITH_TAG.length);
 			int bodyLengthTag = FixUtils.getTag(buf, err);
 
 			if (bodyLengthTag == FixTags.BODYLENGTH_INT && !err.hasError()) {
 
 				int bodyLength = FixUtils.getTagIntValue(buf, err);
 
 				if (!err.hasError() && bodyLength >= FIX_MESSAGE_START + FIX_TRAILER) { // TODO ad FIX_MAX_MESSAGE_LENGTH check
 
 					int msgTypeTag = FixUtils.getTag(buf, err);
 
 					if (msgTypeTag == FixTags.MSGTYPE_INT && !err.hasError()) {
 
 						FixUtils.getTagStringValue(buf, tmpMsgType, 0, 2, err);
 						msgTypeInt = FixUtils.getMsgTypeAsInt(tmpMsgType, 2);
 
 						if (!err.hasError()) {
 
 							if (buf.position() + bodyLength < buf.limit() + FIX_TRAILER) {
 								if (!err.hasError() && validateChecksum) {
 
 									generateCheckSum(calcCheckSum, buf, buf.position(), bodyLength + buf.position());
 
 									buf.position(bodyLength + buf.position());
 									final int checkSumTag = getTag(buf, err);
 
 									if (!err.hasError() && checkSumTag == FixTags.CHECKSUM_INT) {
 
 										getTagStringValue(buf, currCheckSum, 0, FixTags.CHECKSUM_LENGTH, err);
 
 										if (!err.hasError()) {
 
 											if (FixUtils.equals(currCheckSum, calcCheckSum)) {
 												buf.position(startPos);
 												return msgTypeInt;
 											}
 										}
 									}
 								} else {
 									buf.position(startPos);
 									return msgTypeInt;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		buf.position(startPos);
 		err.setError(FixEvent.GARBLED, "garbled");
 		return msgTypeInt;
 	}
 
 	public static int getNasdaqMsgTypeInt(int tmpMsgType, final ByteBuffer buf, final FixValidationError err) {
 		int msgTypeInt = 0;
 		byte[] newMsgType = new byte[2];
 		final int pos = buf.position();
 
 		if (tmpMsgType == MessageTypes.EXECUTIONREPORT_INT) {
 			// this is Execution Report need to figure out sub type...
 			// get tag ExecType (150)
 			if (getTag(FixTags.EXECTYPE_INT, buf, err)) {
 				if (!err.hasError()) {
 
 					final byte c = getTagCharValue(buf, err);
 
 					if (!err.hasError()) {
 
 						newMsgType[0] = (byte) '8';
 						newMsgType[1] = c;
 
 						msgTypeInt = getMsgTypeAsInt(newMsgType, 2);
 					}
 				}
 			} else if (getTag(FixTags.EXECTYPE_INT, buf, err))
 				if (!err.hasError()) {
 
 					final byte c = getTagCharValue(buf, err);
 
 					if (err.hasError()) {
 
 						newMsgType[0] = (byte) '8';
 						newMsgType[1] = c;
 
 						msgTypeInt = getMsgTypeAsInt(newMsgType, 2);
 					}
 				}
 		} else if (tmpMsgType == MessageTypes.ORDERCANCELREJECT_INT) { // this is order reject...
 			// 434 CxlRejResponseTo
 			if (getTag(FixTags.CXLREJRESPONSETO_INT, buf, err))
 				if (!err.hasError()) {
 
 					final byte c = getTagCharValue(buf, err);
 
 					if (!err.hasError()) {
 						newMsgType[0] = (byte) '9';
 						newMsgType[1] = c;
 
 						msgTypeInt = getMsgTypeAsInt(newMsgType, 2);
 
 					}
 
 				}
 		} else
 			return -1;
 
 		buf.position(pos);
 		return msgTypeInt;
 	}
 
 	/**
 	 * ------------------------------------------------------------------------------------------------- IO ------------------------------------------------------------------------------------------------
 	 **/
 
 }
