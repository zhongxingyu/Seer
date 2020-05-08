 package gov.usgs.gdp.servlet;
 
 import gov.usgs.gdp.bean.AckBean;
 import gov.usgs.gdp.bean.ErrorBean;
 import gov.usgs.gdp.bean.FeatureBean;
 import gov.usgs.gdp.bean.FilesBean;
 import gov.usgs.gdp.bean.ShapeFileSetBean;
 import gov.usgs.gdp.bean.XmlReplyBean;
 import gov.usgs.gdp.helper.CookieHelper;
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class FileFeatureServlet
  */
 public class FileFeatureServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static org.apache.log4j.Logger log = Logger.getLogger(FileAttributeServlet.class);
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FileFeatureServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Long start = Long.valueOf(new Date().getTime());
 		String command = request.getParameter("command");
 		XmlReplyBean xmlReply = null;
 		if ("listfeatures".equals(command)) {
 			log.debug("User has chosen to list shapefile features");
 			String shapefile = request.getParameter("shapefile");
 			String attribute = request.getParameter("attribute");
 			if (attribute == null || "".equals(attribute)
 					|| shapefile == null || "".equals(shapefile)) {
 				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_PARAM));
 				RouterServlet.sendXml(xmlReply, start, response);
 				return;			
 			}
 			
			Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");			
			String userDirectory = "";
			if (userDirectoryCookie != null) {
				if (FileHelper.doesDirectoryOrFileExist(userDirectoryCookie.getValue())) {
					userDirectory = userDirectoryCookie.getValue();
				}
 			}
 			
 			List<FilesBean> filesBeanList = FilesBean.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
 			if (filesBeanList == null) {
 				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
 				RouterServlet.sendXml(xmlReply, start,response);
 				return;
 			}
 			ShapeFileSetBean shapeFileSetBean = ShapeFileSetBean.getShapeFileSetBeanFromFilesBeanList(filesBeanList, shapefile);
 			shapeFileSetBean.setChosenAttribute(attribute);
 			// Pull Feature Lists
 			List<String> features = null;
             try {
 				features = ShapeFileSetBean.getFeatureListFromBean(shapeFileSetBean);
 			} catch (IOException e) {
 				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FEATURES_NOT_FOUND));
 				RouterServlet.sendXml(xmlReply, start,response);
 				return;
 			}
 			
 			if (features != null && !features.isEmpty()) {
 				FeatureBean featureBean = new FeatureBean(features);
 				featureBean.setFilesetName(shapefile);
 				xmlReply = new XmlReplyBean(AckBean.ACK_OK, featureBean);
 				RouterServlet.sendXml(xmlReply, start,response);
 				return;
 			}
 			xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FEATURES_NOT_FOUND));
 			RouterServlet.sendXml(xmlReply, start,response);
 			return;
 		}
 	}
 
 }
