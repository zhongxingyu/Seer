 package com.df.blobstore.image.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.resteasy.util.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.df.blobstore.image.Image;
 import com.df.blobstore.image.ImageFormat;
 import com.df.blobstore.image.ImageKey;
 import com.df.blobstore.image.ImageService;
 import com.df.core.common.utils.StringUtils;
 
 public abstract class ImageServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	private ImageService imageService;
 
 	private ImageReferenceFactory imageReferenceFactory;
 
 	private ObjectMapper objectMapper;
 
 	private static final Logger logger = LoggerFactory.getLogger(ImageServlet.class);
 
 	public void setImageReferenceFactory(ImageReferenceFactory imageReferenceFactory) {
 		this.imageReferenceFactory = imageReferenceFactory;
 	}
 
 	protected ImageReferenceFactory getImageReferenceFactory() {
 		return imageReferenceFactory;
 	}
 
 	protected ObjectMapper getObjectMapper() {
 		return objectMapper;
 	}
 
 	public void setObjectMapper(ObjectMapper objectMapper) {
 		this.objectMapper = objectMapper;
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		initImageService(config);
 		initImageReferenceFactory(config);
 		initObjectMapper(config);
 	}
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		ImageKey imageKey = this.getImageKeyFromRequest(req);
 		if (imageKey == null) {
 			resp.setStatus(404);
 		} else {
 			InputStream in = null;
 			OutputStream out = null;
 			try {
 				Image image = imageService.fetchImage(imageKey);
 				ImageFormat format = image.getImageAttributes().getFormat();
 				resp.setContentType(format.getMediaType());
 				in = image.getBundleValue().getDataInBundle();
 				resp.setContentLength(image.getBundleValue().getSize());
 				out = resp.getOutputStream();
 				byte[] buf = new byte[1024];
 				int count = 0;
 				while ((count = in.read(buf)) >= 0) {
 					out.write(buf, 0, count);
 				}
 			} catch (Throwable ex) {
 				String msg = "Cannot get image from key " + imageKey.toString();
 				logger.error(msg, ex);
 				resp.setStatus(404);
 			} finally {
 				if (in != null) {
 					try {
 						in.close();
 					} catch (Throwable ex) {
 					}
 				}
 				if (out != null) {
 					try {
 						out.close();
 					} catch (Throwable ex) {
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		ImageKey imageKey = this.getImageKeyFromRequest(req);
 		if (imageKey != null) {
 			try {
 				imageService.deleteImage(imageKey);
 			} catch (Throwable ex) {
 				logger.warn("Failed to delete image " + imageKey, ex);
 			}
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		if (StringUtils.isEmpty(getTenantFromRequest(req))) {
 			resp.setStatus(400);
 			resp.getWriter().write("Tenant context is not set");
 			return;
 		}
 		ServletInputStream in = req.getInputStream();
 		byte[] base64 = new byte[req.getContentLength()];
 		in.read(base64);
 		byte[] imageData = Base64.decode(base64);
 		ImageKey key = imageService.uploadImage(new ByteArrayInputStream(imageData), getTenantFromRequest(req));
 		ImageReference reference = imageReferenceFactory.createImageReference(key);
 		objectMapper.writeValue(resp.getOutputStream(), reference);
 	}
 
 	protected abstract ImageKey getImageKeyFromRequest(HttpServletRequest request);
 
 	protected abstract String getTenantFromRequest(HttpServletRequest request);
 
 	protected abstract void initImageService(ServletConfig config);
 
 	protected abstract void initImageReferenceFactory(ServletConfig config);
 
 	protected abstract void initObjectMapper(ServletConfig config);
 
 	protected void setImageService(ImageService imageService) {
 		this.imageService = imageService;
 	}
 
 }
