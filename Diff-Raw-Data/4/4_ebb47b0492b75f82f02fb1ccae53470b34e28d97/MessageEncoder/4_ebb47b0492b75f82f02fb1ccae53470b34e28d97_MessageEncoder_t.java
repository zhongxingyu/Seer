 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolEncoder;
 import org.apache.mina.filter.codec.ProtocolEncoderOutput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.protocol.CmdCodecFactory;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ErrorBodyMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.HeadMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 import com.chinarewards.qqgbvpn.main.session.SessionKeyCodec;
 
 public class MessageEncoder implements ProtocolEncoder {
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private Charset charset;
 	
 	private PackageHeadCodec packageHeadCodec;
 	
 	protected CmdCodecFactory cmdCodecFactory;
 
 	SessionKeyCodec skCodec = new SessionKeyCodec();
 	
 	/**
 	 * XXX can 'injector' be skipped?
 	 * 
 	 * @param charset
 	 * @param injector
 	 * @param cmdCodecFactory
 	 */
 	public MessageEncoder(Charset charset, CmdCodecFactory cmdCodecFactory) {
 		this.charset = charset;
 		this.cmdCodecFactory = cmdCodecFactory;
 		this.packageHeadCodec = new PackageHeadCodec();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.mina.filter.codec.ProtocolEncoder#dispose(org.apache.mina.
 	 * core.session.IoSession)
 	 */
 	@Override
 	public void dispose(IoSession session) throws Exception {
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core
 	 * .session.IoSession, java.lang.Object,
 	 * org.apache.mina.filter.codec.ProtocolEncoderOutput)
 	 */
 	@Override
 	public void encode(IoSession session, Object message,
 			ProtocolEncoderOutput out) throws Exception {
 		
 		log.debug("encode message start");
 		if (session != null && session.isConnected()) {
 			log.trace("Mina session ID: {}", session.getId());
 		}
 		
 		Message msg = (Message) message;
 		HeadMessage headMessage = msg.getHeadMessage();
 		ICommand bodyMessage = msg.getBodyMessage();
 
 		long cmdId = bodyMessage.getCmdId();
 		byte[] bodyByte = null;
 		log.debug("cmdId to send: ({})", cmdId);
 		
 		if (bodyMessage instanceof ErrorBodyMessage) {
 			bodyByte = new byte[ProtocolLengths.COMMAND + 4];
 			ErrorBodyMessage errorBodyMessage = (ErrorBodyMessage) bodyMessage;
 			Tools.putUnsignedInt(bodyByte, errorBodyMessage.getCmdId(), 0);
 			Tools.putUnsignedInt(bodyByte, errorBodyMessage.getErrorCode(),
 					ProtocolLengths.COMMAND);
 		} else {
 //				throw new RuntimeException("cmd id is not exits,cmdId is :"+cmdId);
 //			}
 			//Dispatcher
 			//IBodyMessageCoder bodyMessageCoder = injector.getInstance(Key.get(IBodyMessageCoder.class, Names.named(cmdName)));
 			
 			// XXX handles the case no codec is found for the command ID.
 			
 			ICommandCodec bodyMessageCoder = cmdCodecFactory.getCodec(cmdId);
 			log.trace("bodyMessageCoder = {}", bodyMessageCoder);
 			bodyByte = bodyMessageCoder.encode(bodyMessage, charset);
 		}
 
 		//head process
 		headMessage.setMessageSize(ProtocolLengths.HEAD + bodyByte.length);
 		byte[] headByte = packageHeadCodec.encode(headMessage);
 		
 		
 		byte[] result = new byte[ProtocolLengths.HEAD + bodyByte.length];
 
 		Tools.putBytes(result, headByte, 0);
 		Tools.putBytes(result, bodyByte, ProtocolLengths.HEAD);
 		
 		// make the 
 		int checkSumVal = Tools.checkSum(result, result.length);
 		Tools.putUnsignedShort(result, checkSumVal, 10);
 		log.debug("Encoded message checkum: 0x{}", Integer.toHexString(checkSumVal));
 		
 		byte[] serializedSessionKey = null;
 		if ((headMessage.getFlags() & HeadMessage.FLAG_SESSION_ID) != 0) {
 			log.debug("message header indicates session ID presence, will encode");
 			
 			// session key is present
 			// FIXME should respect the version ID in the session key.
 			serializedSessionKey = skCodec.encode(headMessage.getSessionKey());
 		}
 
 		// write to Mina session
 		int length = result.length;
 		if (serializedSessionKey != null) {
 			length += serializedSessionKey.length;
 		}
 		// prepare buffer
 		IoBuffer buf = IoBuffer.allocate(length);
 		// write header (16 byte)
		buf.put(headByte);
 		// write session key (conditional)
 		if (serializedSessionKey != null) {
 			buf.put(serializedSessionKey);
 		}
		buf.put(bodyByte);
 		
 		// debug print
 		log.debug("Encoded byte content");
 		// TODO make the '96' be configurable.
 		CodecUtil.hexDumpForLogging(log, buf.array(), 96);
 		
 		buf.flip();
 		out.write(buf);
 		log.debug("encode message end");
 	}
 
 }
