 package com.zotyo.photos.servlet;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.net.URLDecoder;
 
 import javax.imageio.ImageIO;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.support.SpringBeanAutowiringSupport;
 
 import org.apache.log4j.Logger;
 import com.zotyo.diary.web.DiaryHelper;
 import com.zotyo.photos.pojo.Photo;
 import com.zotyo.photos.pojo.PhotoData;
 
 import com.zotyo.photos.service.PhotoService;
 import com.zotyo.photos.util.PhotoDataEnum;
 
 import static org.imgscalr.Scalr.*;
 
 public class PhotoServlet extends HttpServlet {
 	
 	private static Logger logger = Logger.getLogger(PhotoServlet.class); 
 	
 	@Autowired
 	private PhotoService photoService;
 	
 	@Autowired
 	private DiaryHelper diaryHelper;
 	
 	private String keyword;
 	
 	public void init() throws ServletException {
 		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
 		try {
 			InputStream inputStream = ClassLoader.getSystemResourceAsStream("diary.properties");
 			Properties props = new Properties();
 			props.load(inputStream);
 			
 			keyword = props.getProperty("keyword");
 		} catch(IOException ioex) {
 			ioex.printStackTrace();
 		}
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 												throws ServletException, IOException {
 		String cmd = request.getParameter("cmd");
  
 		if ("thumbdata".equals(cmd)) {
 			String fileName = request.getParameter("filename");
 			if (fileName != null && !fileName.isEmpty()) {
             	response.setContentType("image/jpeg");
             	PhotoData photoData = photoService.getDataByFilename(fileName, PhotoDataEnum.THUMB_ONLY);
             	if (photoData != null && photoData.getThumbdata() != null) {
             		response.getOutputStream().write(photoData.getThumbdata());
             	}
 			}
 		}
 		else if ("data".equals(cmd)) {
 			String fileName = request.getParameter("filename");
 			if (fileName != null && !fileName.isEmpty()) {
             	response.setContentType("image/jpeg");
             	PhotoData photoData = photoService.getDataByFilename(fileName, PhotoDataEnum.PICTURE_ONLY);
             	if (photoData != null && photoData.getData() != null) {
             		response.getOutputStream().write(photoData.getData());
             	}
 			}
 		}
 		else if ("keywords".equals(cmd)) {
 			String term = URLDecoder.decode(request.getParameter("term"), "UTF-8");
 			List<String> keywords = photoService.searchKeywords(term);
                         
 			response.setContentType("application/json; charset=UTF-8");
 			PrintWriter out = response.getWriter();
 			ObjectMapper mapper = new ObjectMapper();
 			mapper.writeValue(out, keywords);
 			return;
                         
 		}
 		else if ("photos".equals(cmd)) {
 			response.setContentType("application/json; charset=UTF-8");
 			List<Photo> photos = photoService.findByCategory("baba");
 			ObjectMapper mapper = new ObjectMapper();
 
 			mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,	false);
 
 			try {
 				mapper.writeValue(response.getOutputStream(), photos);
 			} catch (JsonGenerationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JsonMappingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else {
 			String isAdmin = (String) request.getSession().getAttribute("admin");
 			
 			if (Boolean.valueOf(isAdmin)) {
 				List<Photo> photos = photoService.findAll();
 				request.setAttribute("photos", photos);
 				
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/adminphotos.jsp");
 				rd.forward(request, response);
 				return;
 			} else {
 				request.getSession().setAttribute("redirect_to", "photos");
 				response.sendRedirect("/admin/adminphotos.jsp");	
 			}
 		}
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		boolean isXHR = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")) ? true : false;
 		Boolean[] result = new Boolean[2];
 		
 		if (isMultipart) {
 			result = processMultipartRequest(request, response, isXHR);
 		}
 		else {
 			result[0] = Boolean.valueOf(request.getParameter("isMobile"));
 			String cmd = request.getParameter("cmd");
 			if ("delete".equals(cmd)) {
 				String fileName = request.getParameter("file2delete");
 
 				if (fileName != null && !fileName.isEmpty()) {
 					photoService.deleteByFilename(fileName);
 				}
 			}
 			else if ("login".equals(cmd)) {
 				String password = request.getParameter("password");
 				if (!diaryHelper.md5(password).equals(keyword)) {
 					RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/login.jsp");
 					rd.forward(request, response);
 					return;
 				}
 				
 				List<Photo> photos = photoService.findAll();
 				request.setAttribute("photos", photos);
 		        
 				request.getSession().setAttribute("admin", "true");
 				
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/adminphotos.jsp");
 		        rd.forward(request, response);
 		        return;
 			}
 			else if ("search".equals(cmd)) {
 				String term = request.getParameter("term");
 				List<Photo> photos = photoService.searchPhotos(term);
 				request.setAttribute("photos", photos);
 				
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/photos.jsp");
 				rd.forward(request, response);
 				return;
 			}
 			
 		}
 		
 		if (isXHR) {
 			PrintWriter out = response.getWriter();
 			if (result[1]) {
 				out.print("success");
 			} else {
 				out.print("failure");
 			}
 			return;
 		}
 		
 		if (result[0]) {
 			response.sendRedirect("/m/naplo?cmd=photos");
 		} else {
 			response.sendRedirect("/photos");
 		}
 	}
 	
 	private Boolean[] processMultipartRequest(HttpServletRequest request, HttpServletResponse response, boolean isXHR) throws ServletException, IOException {
 		boolean isMobile = false;
 		
 		try {
 			SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
 			
 			List<FileItem> items;
             FileItemFactory factory = new DiskFileItemFactory();
             ServletFileUpload upload = new ServletFileUpload(factory);
             items = upload.parseRequest(request);
             Iterator it = items.iterator();
             
             byte[] data = null;
             String fileName = "";
             String description = "";
             String category = "";
             String createdateStr = "";
             String kw = "";
             while (it.hasNext()) {
                 FileItem item = (FileItem) it.next();
                 if (!item.isFormField()) {
                     if (item.getFieldName().equals("file") && item.getString() != null && !item.getString().trim().equals("")) {
                         data = item.get();
                         fileName = item.getName().toLowerCase();
                     }
                 } else {
                     if (item.getFieldName().equals("category")) {
                     	category = item.getString();
                     }
                     if (item.getFieldName().equals("description")) {
                     	description = item.getString("utf-8");
                     }
                     if (item.getFieldName().equals("createdate")) {
                     	createdateStr = item.getString();
                     }
                     if (item.getFieldName().equals("isMobile")) {
                     	isMobile = Boolean.valueOf(item.getString());
                     	logger.info("IsMobile: " + isMobile);
                     }
                     if (item.getFieldName().equals("keyword")) {
                     	kw = item.getString();
                     }
                 }
             }
             if (category.isEmpty()) {
             	category = "baba";
             }
             if (isMobile || isXHR) {
             	if (!diaryHelper.md5(kw).equals(keyword)) {
 					return new Boolean[] { isMobile, false};
 				}
             }
             
             Date createDate = null;
     		try {
     			createDate = format.parse(createdateStr);
     		} catch (ParseException e) {
     			createDate = new Date();
     		}
             if (data != null && !fileName.isEmpty()) {
             	byte[] thumbdata = createThumbnail(data);
 				photoService.save(
 					new Photo(description, fileName, category, createDate),
 					new PhotoData(data, thumbdata)
 					);
				return new Boolean[] { isMobile, true };
             }
         } catch (FileUploadException fuex) {
         	fuex.printStackTrace();
         }
        return new Boolean[] { isMobile, false };
 	}
 	
 	private byte[] createThumbnail(byte[] data) throws IOException {
     	InputStream in = new ByteArrayInputStream(data);
     	BufferedImage img = ImageIO.read(in);
 
 		img = resize(img, Method.ULTRA_QUALITY, 150, OP_ANTIALIAS, OP_BRIGHTER);
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ImageIO.write(img, "jpg", baos);
 		baos.flush();
 		byte[] imageInByte = baos.toByteArray();
 		baos.close();
 		
 		return imageInByte;
 	}
 }
