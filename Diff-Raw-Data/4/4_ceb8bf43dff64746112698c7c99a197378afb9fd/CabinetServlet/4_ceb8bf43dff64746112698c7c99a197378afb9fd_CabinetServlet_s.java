 package com.horsefire.filecabinet.web;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.inject.Injector;
 import com.horsefire.filecabinet.FileCabinet;
 import com.horsefire.filecabinet.file.Cabinet;
 import com.horsefire.filecabinet.file.Document;
 
 @SuppressWarnings("serial")
 public class CabinetServlet extends HttpServlet {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(CabinetServlet.class);
 
 	public static final String PATH = "/cabinet";
 
 	private final Cabinet m_cabinet;
 
 	public CabinetServlet() {
 		Injector i = FileCabinet.INJECTOR.get();
 		m_cabinet = i.getInstance(Cabinet.class);
 	}
 
 	private Map<String, Map<String, Object>> getDocuments() throws IOException {
 		Map<String, Map<String, Object>> docs = new HashMap<String, Map<String, Object>>();
 		for (Document doc : m_cabinet.getDocuments()) {
 			Map<String, Object> docInfo = new HashMap<String, Object>();
 
 			docInfo.put("id", doc.getId());
 			docInfo.put("unseen", doc.unseen());
 			docInfo.put("thumb", doc.hasThumbnail());
 			docInfo.put("filename", doc.getFilename());
 			docInfo.put("tags", doc.getTags());
			docInfo.put("uploaded", doc.getUploaded().toString());
			docInfo.put("effective", doc.getEffective().toString());
 
 			docs.put(doc.getId(), docInfo);
 		}
 		return docs;
 	}
 
 	private Set<String> getTags() throws IOException {
 		Set<String> tags = new HashSet<String>();
 		for (Document doc : m_cabinet.getDocuments()) {
 			tags.addAll(doc.getTags());
 		}
 		return tags;
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		Map<String, Object> properties = new HashMap<String, Object>();
 
 		properties.put("docs", getDocuments());
 		properties.put("tags", getTags());
 
 		resp.setContentType("text/javascript");
 		resp.getWriter().println(new Gson().toJson(properties));
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String id = req.getParameter("id");
 
 		Document doc = m_cabinet.getDocument(id);
 		if (doc == null) {
 			resp.sendError(HttpURLConnection.HTTP_NOT_FOUND,
 					"That file doesn't exist");
 			return;
 		}
 
 		String action = req.getParameter("action");
 		if ("createThumbnail".equals(action)) {
 			LOG.debug("Generating thumbnail for {}", doc.getId());
 			doc.createThumbnail();
 		} else if ("saveDoc".equals(action)) {
 			Set<String> tagSet = new HashSet<String>();
 			for (String tag : req.getParameterValues("tags")) {
 				tagSet.add(tag.toLowerCase().trim());
 			}
 			if (!doc.getTags().equals(tagSet)) {
 				doc.setTags(tagSet);
 				LOG.debug("Saving doc {} tags {}", doc.getId(), tagSet);
 			}
 
 			if (doc.unseen()) {
 				if (!Boolean.parseBoolean(req.getParameter("unseen"))) {
 					doc.setSeen();
 					LOG.debug("Setting doc {} as seen", doc.getId());
 				}
 			}
 			resp.setContentType("text/javascript");
 			resp.getWriter().print("{}");
 		} else {
 			resp.sendError(HttpURLConnection.HTTP_BAD_REQUEST,
 					"Unknown action parameter");
 		}
 	}
 }
