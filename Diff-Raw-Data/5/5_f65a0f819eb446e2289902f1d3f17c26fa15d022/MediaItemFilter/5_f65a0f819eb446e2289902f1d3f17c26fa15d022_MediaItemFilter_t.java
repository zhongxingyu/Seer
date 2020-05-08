 package com.xlthotel.core.filter;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.google.common.collect.Maps;
 import com.xlthotel.core.service.PhotoService;
 import com.xlthotel.core.service.imp.PhotoServiceImpl;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.constants.PhotoScalerType;
 
 public class MediaItemFilter implements Filter {
 	private static final Logger logger = LoggerFactory.getLogger(MediaItemFilter.class);
 	private ServletContext servletContext;
 	
 	private static final Map<String, String> mimeTable = new HashMap<String, String>();
 	{
 		mimeTable.put("JPG", "jpeg");
 		mimeTable.put("JPEG", "jpeg");
 		mimeTable.put("PNG", "png");
 		mimeTable.put("GIF", "gif");
 		mimeTable.put("BMP", "jpeg");
 	}
 	
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		servletContext = filterConfig.getServletContext();
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest) request;
 		HttpServletResponse resp = (HttpServletResponse) response;
 		String uri = req.getRequestURI();
		if (!uri.contains("/servlet/media/")) {
 			chain.doFilter(request, response);
 			return;
 		}
 		try {
 			PhotoService photoService = getSpringContext().getBean("photoServiceImpl", PhotoServiceImpl.class);
			String path = uri.substring(uri.indexOf("/servlet/media/") + "/servlet/media/".length(), uri.length());
 			
 			String scalerType = SimpleServletRequestUtils.getStringParameter(req, "scalerType", null);
 			int width = SimpleServletRequestUtils.getIntParameter(req, "width", 0);
 			int hight = SimpleServletRequestUtils.getIntParameter(req, "hight", 0);
 			byte[] photoStream = null;
 			
 			if (StringUtils.isBlank(scalerType)) {
 				photoStream = photoService.getPhoto(path, width, hight);
 			} else {
 				try {
 					PhotoScalerType photoScalerType = PhotoScalerType.valueOf(scalerType);
 					photoStream = photoService.getPhoto(path, photoScalerType);
 				} catch (Exception e) {
 					photoStream = photoService.getPhoto(path, -1, -1);
 				}
 			}
 			String suffix = path.substring(path.lastIndexOf(".") + 1, path.length());
 			resp.setHeader("Cache-Control", "max-age=3600, must-revalidate");
 			resp.setContentType("image/" + mimeTable.get(suffix.toUpperCase()));
 			resp.setCharacterEncoding("UTF-8");
 			ServletOutputStream out = resp.getOutputStream();
 			out.write(photoStream);
 			out.close();
 			return;
 		} catch (Exception ex) {
 			logger.error("Can not find the media item[" + uri + "]", ex);
 			chain.doFilter(request, response);
 		}
 	}
 
 	@Override
 	public void destroy() {
 		servletContext = null;
 	}
 
 	private WebApplicationContext getSpringContext() {
 		return WebApplicationContextUtils.getWebApplicationContext(servletContext);
 	}
 }
