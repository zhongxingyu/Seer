 package ch.ethz.iks.r_osgi.transport.mina.codec;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import org.apache.mina.common.ByteBuffer;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.apache.mina.filter.codec.ProtocolEncoderOutput;
 import org.apache.mina.filter.codec.demux.MessageDecoder;
 import org.apache.mina.filter.codec.demux.MessageDecoderResult;
 import org.apache.mina.filter.codec.demux.MessageEncoder;
 
 import ch.ethz.iks.r_osgi.messages.RemoteOSGiMessage;
 
 /**
  * <pre>
  *           0                   1                   2                   3
  *           0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  *          +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  *          |    Version    |         Function-ID           |     XID       |
  *          +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  *          |    XID cntd.  | LENGTH (4)
  *          +-+-+-+-+-+-+-+-+
  * </pre>
  * 
  * @author rjan
  * 
  */
 public abstract class RemoteOSGiMessageCodec implements MessageEncoder,
 		MessageDecoder {
 
 	private short type;
 
 	private static Charset CHARSET = Charset.forName("UTF-8");
 
 	protected static CharsetDecoder DECODER = CHARSET.newDecoder();
 
 	protected static CharsetEncoder ENCODER = CHARSET.newEncoder();
 
 	protected RemoteOSGiMessageCodec(final short type) {
 		this.type = type;
 	}
 
 	/*
 	 * 
 	 * @see org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.mina.common.IoSession,
 	 *      org.apache.mina.common.ByteBuffer)
 	 */
 	public MessageDecoderResult decodable(IoSession session, ByteBuffer in) {
 		if (in.remaining() < 5) {
 			return MessageDecoderResult.NEED_DATA;
 		}
 		final byte version = in.get();
 		if (version != 1) {
 			return MessageDecoderResult.NOT_OK;
 		}
 		final short funcID = in.getShort();
 		if (funcID != type) {
 			return MessageDecoderResult.NOT_OK;
 		}
		final Short xid = new Short(in.getShort());
 		session.setAttribute("xid", xid);
 
 		final int length = in.getInt();
 		if (in.remaining() < (length - 9)) {
 			return MessageDecoderResult.NEED_DATA;
 		}
 		return MessageDecoderResult.OK;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.mina.filter.codec.demux.MessageDecoder#decode(org.apache.mina.common.IoSession,
 	 *      org.apache.mina.common.ByteBuffer,
 	 *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
 	 */
 	public MessageDecoderResult decode(IoSession session, ByteBuffer in,
 			ProtocolDecoderOutput out) throws Exception {
 		in.skip(9);
 
 		decodeBody(session, in, out);
 		return MessageDecoderResult.OK;
 	}
 
 	public abstract MessageDecoderResult decodeBody(IoSession session,
 			ByteBuffer in, ProtocolDecoderOutput out) throws Exception;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.mina.filter.codec.demux.MessageEncoder#encode(org.apache.mina.common.IoSession,
 	 *      java.lang.Object,
 	 *      org.apache.mina.filter.codec.ProtocolEncoderOutput)
 	 */
 	public void encode(IoSession session, Object message,
 			ProtocolEncoderOutput out) throws Exception {
 		final RemoteOSGiMessage msg = (RemoteOSGiMessage) message;
 		final ByteBuffer buf = ByteBuffer.allocate(128);
 		buf.setAutoExpand(true); // Enable auto-expand for easier encoding
 
 		buf.put((byte) 1); // version
 		buf.putShort(type); // funcID
		buf.putShort(msg.getXID()); // xid
 
 		buf.skip(4); // skip length
 
 		encodeBody(session, msg, buf);
 
 		buf.putInt(5, buf.position());
 
 		buf.flip();
 		out.write(buf);
 	}
 
 	public abstract void encodeBody(IoSession session,
 			RemoteOSGiMessage message, ByteBuffer buf) throws IOException;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.mina.filter.codec.demux.MessageDecoder#finishDecode(org.apache.mina.common.IoSession,
 	 *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
 	 */
 	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
 			throws Exception {
 
 	}
 
 	protected final void encodeString(final ByteBuffer out, final String s)
 			throws IOException {
 		out.putShort((short) s.length());
 		out.putString(s, ENCODER);
 	}
 
 	protected final String decodeString(final ByteBuffer in) throws IOException {
 		final short len = in.getShort();
 		return in.getString(len, DECODER);
 	}
 
 	protected final void encodeStringArray(final ByteBuffer out,
 			final String[] strings) throws IOException {
 		final short length = (short) strings.length;
 		out.putShort(length);
 		for (short i = 0; i < length; i++) {
 			encodeString(out, strings[i]);
 		}
 	}
 
 	protected final String[] decodeStringArray(final ByteBuffer in)
 			throws IOException {
 		final short length = in.getShort();
 		final String[] result = new String[length];
 		for (short i = 0; i < length; i++) {
 			result[i] = decodeString(in);
 		}
 		return result;
 	}
 
 	protected final void encodeBytes(final ByteBuffer out, final byte[] bytes) {
 		out.putInt(bytes.length);
 		out.put(bytes);
 	}
 
 	protected final byte[] decodeBytes(final ByteBuffer in) {
 		final int len = in.getInt();
 		final byte[] bytes = new byte[len];
 		in.get(bytes);
 		return bytes;
 	}
 }
