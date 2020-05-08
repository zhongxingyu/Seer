 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.PosEchoCommandRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 /**
  * description：Implements the codec for the message pos echo command.
  * @copyright binfen.cc
  * @projectName main
  * @time 2011-10-20   下午06:41:36
  * @author Seek
  */
 public class PosEchoCommandRequestMessageCodec implements ICommandCodec {
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public ICommand decode(IoBuffer in, Charset charset)
 			throws PackageException {
 
 		// insufficient bytes give, die.
 		if (in.remaining() < ProtocolLengths.COMMAND) {
 			throw new PackageException();
 		}
 		long cmdId = in.getUnsignedInt();
 		
 		// decode data 
 		//获取
 		byte data[] = new byte[in.capacity()-in.position()];	//TODO in.remaining()  ?
		
		if(data != null && data.length != 0){
			in.get(data);
		}
 		
 		// reconstruct message.
 		PosEchoCommandRequestMessage requestMessage = new PosEchoCommandRequestMessage();
 		requestMessage.setData(data);
 		
 		return requestMessage;
 	}
 	
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 		throw new UnsupportedOperationException();
 	}
 	
 }
