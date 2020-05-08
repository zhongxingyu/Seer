 package tb.tartifouette.web.servlet;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import tb.tartifouette.utlog.AliasManager;
 import tb.tartifouette.utlog.Stats;
 import tb.tartifouette.web.analyzer.Input;
 import tb.tartifouette.web.analyzer.LogAnalyzer;
 import tb.tartifouette.web.report.ReportGenerator;
 
 /**
  * Servlet implementation class UtLogUploadServlet
  */
 public class UtLogUploadServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger log = Logger
 			.getLogger(UtLogUploadServlet.class);
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public UtLogUploadServlet() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		request.getRequestDispatcher("/jsp/FileUpload.jsp").forward(request,
 				response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 
 			FileItemFactory factory = new DiskFileItemFactory();
 
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload(factory);
 
 			// Parse the request
 			List<FileItem> items = upload.parseRequest(request);
 			List<Input> inputs = getInputFromPostedFiles(items);
 			AliasManager aliasManager = getAliasManager(items, request);
 			LogAnalyzer analyzer = new LogAnalyzer(inputs, aliasManager);
 			Stats stats = analyzer.analyze();
 			String date = analyzer.getDateHint();
 			ReportGenerator generator = new ReportGenerator(stats);
 			generator.generateReport(aliasManager);
 			byte[] content = generator.getBaos().toByteArray();
 
 			response.setContentType("application/zip");
 			if (date != null) {
 				response.setHeader("Content-Disposition",
 						"attachment; filename=stats-" + date + ".zip");
 			} else {
 				response.setHeader("Content-Disposition",
 						"attachment; filename=stats.zip");
 			}
 			ServletOutputStream op = response.getOutputStream();
 			op.write(content);
 			op.close();
 
 		} catch (Exception e) {
 			throw new IOException("error on file upload or analyze", e);
 		}
 
 	}
 
 	private AliasManager getAliasManager(List<FileItem> items,
 			HttpServletRequest request) throws IOException {
 		AliasManager mgr = new AliasManager();
 		Properties props = new Properties();
 		for (FileItem fileItem : items) {
 			if ("alias.file".equals(fileItem.getFieldName())) {
 				if (fileItem.getSize() > 0) {
 					log.info("Loading alias.properties from uploaded file "
 							+ fileItem.getName());
 					props.load(fileItem.getInputStream());
 				}
 			} else if ("alias.url".equals(fileItem.getFieldName())) {
 				String aliasUrl = fileItem.getString();
				if (StringUtils.trimToNull(aliasUrl) != null) {
 					props.putAll(tryToLoadAliasesFromUrl(aliasUrl));
 				}
 			}
 		}
 		mgr.init(props);
 		return mgr;
 	}
 
 	private Properties tryToLoadAliasesFromUrl(String aliasUrl)
 			throws IOException {
 		Properties props = new Properties();
 		InputStream is = null;
 		log.info("Loading alias.properties from URL " + aliasUrl);
 		try {
 			URL url = new URL(aliasUrl);
 			is = url.openStream();
 			props.load(is);
 		} finally {
 			IOUtils.closeQuietly(is);
 		}
 		return props;
 	}
 
 	private List<Input> getInputFromPostedFiles(List<FileItem> items)
 			throws IOException {
 		List<Input> inputs = new ArrayList<Input>();
 		for (FileItem item : items) {
 			if ("logfile".equals(item.getFieldName())) {
 				if (item.getName().endsWith(".zip")) {
 					inputs.addAll(getInputsFromZipFile(item));
 				} else {
 					inputs.add(new Input(item.getName(), item.getInputStream()));
 				}
 			}
 		}
 
 		return inputs;
 	}
 
 	private List<Input> getInputsFromZipFile(FileItem item) throws IOException {
 		log.info("Extracting from zip file " + item.getName());
 		List<Input> inputs = new ArrayList<Input>();
 		ZipInputStream zis = new ZipInputStream(item.getInputStream());
 		ZipEntry entry = null;
 		while ((entry = zis.getNextEntry()) != null) {
 			log.info("Extracting zip entry " + entry.getName());
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			IOUtils.copy(zis, os);
 			InputStream is = new ByteArrayInputStream(os.toByteArray());
 			inputs.add(new Input(entry.getName(), is));
 		}
 		return inputs;
 	}
 }
