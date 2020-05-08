 package org.hbs;
 
 import static java.util.Arrays.asList;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.hbs.web.ContentType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Iterables;
 import com.google.common.io.ByteStreams;
 
 public class Dvsg extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String RESOURCES_PATH = "resources";
	private static final String INDEX_FILE = "/index.html";
 	private static final String ROOT_PATH = "/web-app";
 
 	protected static final Logger LOGGER = LoggerFactory.getLogger(Dvsg.class);
 
 	public static void main(String[] args) throws Exception {
 		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		server.setHandler(context);
 		context.addServlet(new ServletHolder(new Dvsg()), "/*");
 		server.start();
 		server.join();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
 			IOException {
 
 		List<String> pathSegments = asList(request.getPathInfo().split("/"));
 		try {
 			if ((pathSegments.size() > 0)
 					&& (Iterables.get(pathSegments, 1).equalsIgnoreCase(RESOURCES_PATH))) {
 				handleResource(request, response);
 			} else {
 				handleFile(request, response);
 			}
 		} finally {
 			/*
 			try {
 				response.getWriter().close();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			*/
 			LOGGER.debug(request.getPathInfo() + " - ");
 		}
 	}
 
 	private void handleResource(HttpServletRequest request, HttpServletResponse response) {
 		// TODO
 	}
 
 	private void handleFile(HttpServletRequest request, HttpServletResponse response) {
		String fileToServePath = getFileToServePath(request.getPathInfo().toString());
 		ContentType contentType = ContentType.getContentType(fileToServePath);
 		response.setContentType(contentType.toString());
 		try {
 			InputStream resourceAsStream = Dvsg.class.getResourceAsStream(fileToServePath);
 
 			if (resourceAsStream != null) {
 				ByteStreams.copy(resourceAsStream, response.getOutputStream());
 			} else {
 				LOGGER.error("File not found for " + request.getPathInfo() + ".");
 				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			}
 		} catch (FileNotFoundException fnfe) {
 			LOGGER.error("File not found for " + request.getPathInfo() + ".");
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		} catch (IOException ioe) {
 			LOGGER.error("IO Excpetion for " + request.getPathInfo() + ".");
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	private String getFileToServePath(String path) {
 		StringBuilder fileToServePath = new StringBuilder(ROOT_PATH);
 		if ("/".equalsIgnoreCase(path)) {
 			fileToServePath.append(INDEX_FILE);
 		} else {
 			fileToServePath.append(path);
 		}
 		return fileToServePath.toString();
 	}
 
 }
