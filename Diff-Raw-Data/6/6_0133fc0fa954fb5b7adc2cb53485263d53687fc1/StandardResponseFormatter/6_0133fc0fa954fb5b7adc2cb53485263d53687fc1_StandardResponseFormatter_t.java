 package org.caudexorigo.http.netty.reporting;
 
 import java.io.PrintWriter;
 
 import org.caudexorigo.io.UnsynchronizedStringWriter;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StandardResponseFormatter
 {
 	private static Logger log = LoggerFactory.getLogger(StandardResponseFormatter.class);
 
 	private final boolean showFullErrorInfo;
 
 	public StandardResponseFormatter(boolean showFullErrorInfo)
 	{
 		super();
 
 		this.showFullErrorInfo = showFullErrorInfo;
 	}
 
 	public void formatResponse(HttpRequest request, HttpResponse response)
 	{
 		formatResponse(request, response, null);
 	}
 
 	public void formatResponse(HttpRequest request, HttpResponse response, Throwable error)
	{
 		if (MessageBody.allow(response.getStatus().getCode()))
 		{
 			try
 			{
 				String html = String.format(ErrorTemplates.getTemplate(response.getStatus().getCode()), response.getStatus().getCode(), response.getStatus().getReasonPhrase(), request.getMethod().toString(), getStackTrace(error, showFullErrorInfo));
 				byte[] bytes = html.toString().getBytes("UTF-8");
 				ChannelBuffer out = ChannelBuffers.wrappedBuffer(bytes);
 				response.setContent(out);
 				response.addHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 			}
 		}
 	}
 
 	public String getStackTrace(Throwable error, boolean fullInfo)
 	{
 		if (error != null)
 		{
 			if (fullInfo)
 			{
				log.error(error.getMessage(), error);
 				UnsynchronizedStringWriter sw = new UnsynchronizedStringWriter();
 				PrintWriter pw = new PrintWriter(sw);
 				error.printStackTrace(pw);
 				pw.flush();
 				return sw.toString();
 			}
 			else
 			{
 				return error.getMessage();
 			}
 		}
 		else
 		{
 			return "N/A";
 		}
 	}
 }
