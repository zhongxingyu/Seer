 package com.dc2f.frontent.web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.logging.Logger;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import com.dc2f.datastore.ContentRepository;
 import com.dc2f.datastore.Node;
 import com.dc2f.datastore.impl.filejson.SimpleFileContentRepository;
 import com.dc2f.renderer.ContentRenderResponse;
 import com.dc2f.renderer.NodeRenderer;
 import com.dc2f.renderer.NodeRendererFactory;
 import com.dc2f.renderer.impl.ContentRenderRequestImpl;
 import com.dc2f.renderer.url.URLMapper;
 
 public class DC2FServlet implements Servlet {
 
 	private ServletConfig config;
 	
 	private static final Logger logger = Logger.getLogger(DC2FServlet.class.getName());
 	
 	@Override
 	public void destroy() {
 
 	}
 
 	@Override
 	public ServletConfig getServletConfig() {
 		return config;
 	}
 
 	@Override
 	public String getServletInfo() {
 		return null;
 	}
 
 	@Override
 	public void init(ServletConfig configuration) throws ServletException {
 		config = configuration;
 
 	}
 
 	@Override
 	public void service(ServletRequest request, ServletResponse response)
 			throws ServletException, IOException {
 		NodeRendererFactory factory = NodeRendererFactory.getInstance();
 		NodeRenderer renderer = factory.getRenderer("com.dc2f.renderer.web");
 		File crdir = new File(System.getProperty("crdir", "../../design/example"));
 		if (crdir == null || !crdir.exists()) {
 			System.out.println("Please specify a crdir :) ( -Dcrdir=xxx)");
 		}
 		ContentRepository cr = new SimpleFileContentRepository(crdir);
 		ServletURLMapper mapper = new ServletURLMapper(cr, getServletConfig());
 		Node node = mapper.getNode(request);
 		logger.info("We got a node: {" + node + "}");
 		
 		Writer writer = response.getWriter();
 		
		renderer.renderNode(new ContentRenderRequestImpl(cr, cr.getNodesInPath("/cmsblog/articles/my-first-article"), mapper),
 					new ServletRenderResponse(writer, null));
 		
 		logger.info("We rendered something: " + writer.toString());
 	}
 	
 	private class ServletRenderResponse implements ContentRenderResponse {
 
 		private Writer writer;
 		
 		private OutputStream outputStream;
 
 		public ServletRenderResponse(Writer wr, OutputStream os) {
 			writer = wr;
 			outputStream = os;
 		}
 		
 		@Override
 		public OutputStream getOutputStream() {
 			return outputStream;
 		}
 
 		@Override
 		public Writer getWriter() {
 			return writer;
 		}
 		
 	}
 
 }
