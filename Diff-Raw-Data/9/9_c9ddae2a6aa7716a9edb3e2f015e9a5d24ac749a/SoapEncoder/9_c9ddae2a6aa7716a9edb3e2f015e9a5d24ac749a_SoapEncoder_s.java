 package pt.com.xml.codec;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.filter.codec.ProtocolEncoderOutput;
 import org.caudexorigo.io.UnsynchByteArrayOutputStream;
 
 import pt.com.types.NetMessage;
 import pt.com.types.SimpleFramingEncoder;
 import pt.com.xml.SoapEnvelope;
 import pt.com.xml.SoapSerializer;
 
 public class SoapEncoder extends SimpleFramingEncoder
 {
 
 	@Override
 	public byte[] processBody(Object message)
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
 	public void processBody(Object message, ProtocolEncoderOutput pout)
 	{
 		if (!(message instanceof NetMessage))
 		{
 			throw new IllegalArgumentException("Not a valid message type for this encoder.");
 		}
 
 		NetMessage gcsMessage = (NetMessage) message;
 		SoapEnvelope soap = Builder.netMessageToSoap(gcsMessage);
 
 	
 		IoBuffer wbuf = IoBuffer.allocate(2048, false);
 		wbuf.setAutoExpand(true);
 		wbuf.putInt(0);
 		SoapSerializer.ToXml((SoapEnvelope) soap, wbuf.asOutputStream());
 		wbuf.putInt(0, wbuf.position() - 4);

 		wbuf.flip();
 
 		pout.write(wbuf);
 	}
 
 }
