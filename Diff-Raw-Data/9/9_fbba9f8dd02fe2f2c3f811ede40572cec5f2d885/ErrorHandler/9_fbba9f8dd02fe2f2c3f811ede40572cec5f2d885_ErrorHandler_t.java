 package pt.com.broker.core;
 
 import java.io.PrintWriter;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.text.StringBuilderWriter;
 import org.caudexorigo.text.StringUtils;
 import org.jibx.runtime.JiBXException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.codec.xml.EndPointReference;
 import pt.com.broker.codec.xml.SoapEnvelope;
 import pt.com.broker.codec.xml.SoapFault;
 import pt.com.broker.codec.xml.SoapHeader;
 import pt.com.broker.types.CriticalErrors;
 import pt.com.gcs.conf.GcsInfo;
 
 /**
  * It is used to deal with uncaught exceptions.
  * 
  */
 
 public class ErrorHandler
 {

 	static
 	{
 		Throwable t = new RuntimeException();
 		ErrorAnalyser.findRootCause(t);
 		CriticalErrors.exitIfCritical(t);
 	}

 	private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
 
 	public void exceptionCaught(Throwable cause)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(cause);
 		if (log.isWarnEnabled())
 		{
 			log.error("Unexpected exception.", rootCause);
 		}
 		exitIfCritical(rootCause);
 	}
 
 	public static void checkAbort(Throwable t)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(t);
 		exitIfCritical(rootCause);
 	}
 
 	public static WTF buildSoapFault(Throwable ex)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(ex);
 		exitIfCritical(rootCause);
 
 		String ereason = "soap:Receiver";
 		if (rootCause instanceof JiBXException)
 		{
 			ereason = "soap:Sender";
 		}
 		else if (rootCause instanceof IllegalArgumentException)
 		{
 			ereason = "soap:Sender";
 		}
 
 		return _buildSoapFault(ereason, rootCause);
 	}
 
 	public static WTF buildSoapFault(String faultCode, Throwable ex)
 	{
 		if (StringUtils.isBlank(faultCode))
 		{
 			return buildSoapFault(ex);
 		}
 
 		Throwable rootCause = ErrorAnalyser.findRootCause(ex);
 		exitIfCritical(rootCause);
 		return _buildSoapFault(faultCode, rootCause);
 	}
 
 	private static WTF _buildSoapFault(String faultCode, Throwable ex)
 	{
		String reason = StringUtils.isBlank(ex.getMessage()) ? ex.getClass().getCanonicalName() : ex.getMessage();

 		String detail = buildStackTrace(ex);
 
 		SoapEnvelope faultMessage = new SoapEnvelope();
 		SoapFault sfault = new SoapFault();
 		sfault.faultCode.value = faultCode;
 		sfault.faultReason.text = reason;
 		sfault.detail = detail;
 		faultMessage.body.fault = sfault;
 
 		SoapHeader soap_header = new SoapHeader();
 		EndPointReference epr = new EndPointReference();
 		epr.address = GcsInfo.getAgentName();
 		soap_header.wsaFrom = epr;
 		faultMessage.header = soap_header;
 
 		WTF wtf = new WTF();
 		wtf.Cause = ex;
 		wtf.Message = faultMessage;
 
 		return wtf;
 	}
 
 	public static String buildStackTrace(Throwable ex)
 	{
 		StringBuilderWriter sw = new StringBuilderWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		ex.printStackTrace(pw);
 		return sw.toString();
 	}
 
 	public static class WTF
 	{
 		public SoapEnvelope Message;
 
 		public Throwable Cause;
 	}
 
 	private static void exitIfUlimit(Throwable t)
 	{
 		String ul_error = "Too many open files".toLowerCase();
 		if (t.getMessage() != null)
 		{
 			String emsg = t.getMessage().toLowerCase();
 			if (emsg.contains(ul_error))
 			{
 				Shutdown.now();
 			}
 		}
 	}
 
 	private static void exitIfCritical(Throwable cause)
 	{
 		CriticalErrors.exitIfCritical(cause);
 		exitIfUlimit(cause);
 	}
 }
