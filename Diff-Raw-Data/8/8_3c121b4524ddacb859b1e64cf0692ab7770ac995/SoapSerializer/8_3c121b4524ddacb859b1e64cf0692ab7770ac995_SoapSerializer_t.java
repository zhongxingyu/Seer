 package pt.com.broker.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.jibx.runtime.IMarshallingContext;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.messaging.BrokerMessage;
 
 public class SoapSerializer
 {
 	private static final Logger log = LoggerFactory.getLogger(SoapSerializer.class);
 
 	public static void ToXml(SoapEnvelope soapEnv, OutputStream out)
 	{
 		try
 		{
 			IMarshallingContext mctx = JibxActors.getMarshallingContext();
 			mctx.marshalDocument(soapEnv, "UTF-8", null, out);
 		}
 		catch (JiBXException e)
 		{
 			if (soapEnv.body.notification != null)
 			{
 				BrokerMessage bmsg = soapEnv.body.notification.brokerMessage;
 				StringBuilder buf = new StringBuilder();
 				buf.append("\ncorrelationId: " + bmsg.correlationId);
 				buf.append("\ndestinationName: " + bmsg.destinationName);
 				buf.append("\nexpiration: " + bmsg.expiration);
 				buf.append("\nmessageId: " + bmsg.messageId);
 				buf.append("\npriority: " + bmsg.priority);
 				buf.append("\ntextPayload: " + bmsg.textPayload);
 				buf.append("\ntimestamp: " + bmsg.timestamp);
 
 				log.error("Unable to marshal soap envelope:" + buf.toString());
 			}
 
 			JibxActors.reload();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static SoapEnvelope FromXml(InputStream in)
 	{
 		try
 		{
 			IUnmarshallingContext uctx = JibxActors.getUnmarshallingContext();
 			Object o = uctx.unmarshalDocument(in, "UTF-8");
 			if (o instanceof SoapEnvelope)
 				return (SoapEnvelope) o;
 			else
 				return new SoapEnvelope();
 		}
 		catch (JiBXException e)
 		{
 
 			JibxActors.reload();
			
 			try
 			{
 				String invalidMessage = slurp(in);
 				log.error("\n" + invalidMessage + "\n");
 			}
 			catch (IOException ioe)
 			{
 				// ignore this exception
 			}
			
			throw new RuntimeException(e);
 		}
 	}
 
 	private static String slurp(InputStream in) throws IOException
 	{
 		in.reset();
 		StringBuilder out = new StringBuilder();
 		byte[] b = new byte[4096];
 		for (int n; (n = in.read(b)) != -1;)
 		{
 			out.append(new String(b, 0, n));
 		}
 		return out.toString();
 	}
 
 }
