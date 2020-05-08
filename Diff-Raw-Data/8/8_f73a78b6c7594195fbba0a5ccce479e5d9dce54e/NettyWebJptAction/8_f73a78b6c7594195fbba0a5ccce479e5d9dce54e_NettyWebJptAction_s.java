 package org.caudexorigo.jpt.web.netty;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.caudexorigo.http.netty.HttpAction;
 import org.caudexorigo.jpt.JptConfiguration;
 import org.caudexorigo.jpt.JptInstance;
 import org.caudexorigo.jpt.JptInstanceBuilder;
 import org.caudexorigo.jpt.web.HttpJptContext;
 import org.caudexorigo.jpt.web.HttpJptController;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class NettyWebJptAction extends HttpAction
 {
 	private static final Logger log = LoggerFactory.getLogger(NettyWebJptAction.class);
 	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
 
 	private final URI _templateURI;
 	private final boolean _showFullErrorInfo;
 
 	public NettyWebJptAction(URI templateURI)
 	{
 		this(templateURI, JptConfiguration.fullErrors());
 	}
 
 	public NettyWebJptAction(URI templateURI, boolean showFullErrorInfo)
 	{
 		super();
 		_templateURI = templateURI;
 		_showFullErrorInfo = showFullErrorInfo;
 	}
 
 	@Override
 	public void service(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
 	{
 		try
 		{
 			NettyJptProcessor aweb_jpt_processor = new NettyJptProcessor(ctx, request, response);
 			JptInstance jpt = JptInstanceBuilder.getJptInstance(getTemplateURI());
 
 			HttpJptContext jpt_ctx = new HttpJptContext(aweb_jpt_processor, getTemplateURI());
 			HttpJptController page_controller = (HttpJptController) Class.forName(jpt.getCtxObjectType()).newInstance();
 
 			page_controller.setHttpContext(jpt_ctx);
 			page_controller.init();
 
 			int http_status = jpt_ctx.getStatus();
 
 			boolean allowsContent = allowsContent(http_status);
 
 			if (allowsContent)
 			{
 				Map<String, Object> renderContext = new HashMap<String, Object>();
 				renderContext.put("$this", page_controller);
 				renderContext.put("$jpt", jpt_ctx);
 
 				jpt.render(renderContext, aweb_jpt_processor.getWriter());
 				aweb_jpt_processor.getWriter().flush();
 
 				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
 			}
 		}
 		catch (Throwable t)
 		{
 			throw new RuntimeException(t);
 		}
 	}
 
 	// code values must be ordered
	private static final int[] NO_CONTENTT_STATUS_CODES = new int[] { 204, 205, 301, 302, 303, 304, 305, 307 };
 
 	private static boolean allowsContent(int httpStatusCode)
 	{
		for (int i = 0; i != NO_CONTENTT_STATUS_CODES.length && NO_CONTENTT_STATUS_CODES[i] <= httpStatusCode; ++i)
 		{
			if (NO_CONTENTT_STATUS_CODES[i] == httpStatusCode)
 				return false;
 		}
 
 		return true;
 	}
 
 	public URI getTemplateURI()
 	{
 		return _templateURI;
 	}
 
 	@Override
 	public boolean getShowFullErrorInfo()
 	{
 		return _showFullErrorInfo;
 	}
 }
