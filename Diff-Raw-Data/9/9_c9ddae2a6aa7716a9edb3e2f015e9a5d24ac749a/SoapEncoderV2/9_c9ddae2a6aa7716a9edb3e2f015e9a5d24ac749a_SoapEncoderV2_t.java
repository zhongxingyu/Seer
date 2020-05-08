 package pt.com.xml.codec;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.filter.codec.ProtocolEncoderOutput;
 import org.caudexorigo.io.UnsynchByteArrayOutputStream;
 
 import pt.com.types.NetMessage;
 import pt.com.types.SimpleFramingEncoderV2;
 import pt.com.xml.SoapEnvelope;
 import pt.com.xml.SoapSerializer;
 
 public class SoapEncoderV2 extends SimpleFramingEncoderV2
 {
 	@Override
 	public byte[] processBody(Object message, Short protocolType, Short protocolVersion)
 	{
 		if (!(message instanceof NetMessage))
 		{
 			throw new IllegalArgumentException("Not a valid message type for this encoder.");
 		}
 		NetMessage gcsMessage = (NetMessage) message;
 		SoapEnvelope soap = Builder.netMessageToSoap(gcsMessage);
 		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
 		SoapSerializer.ToXml(soap, holder);
 		return holder.toByteArray();
 	}
 
 	@Override
 	public void processBody(Object message, ProtocolEncoderOutput pout, Short protocolType, Short protocolVersion)
 	{
 		if (!(message instanceof NetMessage))
 		{
 			throw new IllegalArgumentException("Not a valid message type for this encoder.");
 		}
 
 		NetMessage gcsMessage = (NetMessage) message;
 		SoapEnvelope soap = Builder.netMessageToSoap(gcsMessage);
 
 		IoBuffer wbuf = IoBuffer.allocate(2048, false);
 		wbuf.setAutoExpand(true);
		wbuf.putShort(protocolType.shortValue());
		wbuf.putShort(protocolVersion.shortValue());
 		SoapSerializer.ToXml(soap, wbuf.asOutputStream());
 		wbuf.putShort(4, (short) (wbuf.position() - 6));
 		wbuf.flip();
 
 		pout.write(wbuf);
 	}
 
 }
