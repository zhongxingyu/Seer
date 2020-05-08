 package br.com.depasser.web.resource;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.JspFragment;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ResourceTag extends SimpleTagSupport {
 	
 	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_SERVLET_PATH = "resources.do";
 	
 	private String servletPath = DEFAULT_SERVLET_PATH;
 	private String encoding = DEFAULT_ENCODING;
 	
 	private Logger logger = LoggerFactory.getLogger(ResourceTag.class);
 	
 	@Override
 	public void doTag() throws JspException, IOException {
 		PageContext context = (PageContext)getJspContext();
 		
 		// Check for the servlet path from a init parameter
 		String servletPath = context.getServletContext().getInitParameter(ResourceTag.class.getName().concat("servletPath"));
 		if (servletPath != null) {
 			this.servletPath = servletPath;
 		}
 		
 		// Check for encoding to be used from a init parameter
 		String encoding = context.getServletContext().getInitParameter(ResourceTag.class.getName().concat("encoding"));
 		if (encoding != null) {
 			this.encoding = encoding;
 		}
 		
 		JspFragment body = getJspBody();
 
 		// read the body content
 		StringWriter writer = new StringWriter();
 		body.invoke(writer);
 		String bodyContent = writer.toString();
 
 		// Group requested files by extension
 		String [] files = bodyContent.split("\n");
 		Map<String, List<String>> filesByExtension = new HashMap<String, List<String>>();
 		for (String file : files) {
 			file = file.trim();
 			if ("".equals(file)) continue;
 			
 			String extension = FileUtils.getExtension(file);
 			
 			List<String> filesForExtension = filesByExtension.get(extension);
 			if (filesForExtension == null) {
 				filesForExtension = new ArrayList<String>();
 				filesByExtension.put(extension, filesForExtension);
 			}
 			
 			logger.debug("File: {}, Extension: {}", file, extension);
 			filesForExtension.add(file);
 		}
 		
 		StringBuilder result = new StringBuilder();
 		
 		// Create a tag for each type
 		for (String extension : filesByExtension.keySet()) {
 			Type type = TypeFactory.getType(extension);
 			if (type == null) {
 				throw new IllegalArgumentException("Type not found for: " + extension);
 			}
 			result.append(createTag(filesByExtension.get(extension), type));
 		}
 		
 		// write it to the output
 		context.getOut().write(result.toString());
 	}
 	
 	private String createTag(List<String> files, Type type) throws UnsupportedEncodingException {
 		StringBuilder tag = new StringBuilder();
 		tag.append("<");
 		tag.append(type.getTagName());
 		
 		// Set the path to the servlet
 		tag.append(" ");
 		tag.append(type.getAttributeName());
 		tag.append("=\"");
		tag.append(URLEncoder.encode(servletPath, encoding));
 		tag.append("?");
 		
 		// Add the files to be loaded as parameter
 		for (String file : files) {
 			tag.append("file=");
 			tag.append(URLEncoder.encode(file, encoding));
 			tag.append("&");
 		}
 		
 		// Remove the last "&"
 		tag.setLength(tag.length() - 1);
 		
 		// Close the attribute
 		tag.append("\"");
 		
 		// Add all other attributes
 		Map<String, String> otherAttributes = type.getOtherAttributes();
 		if (otherAttributes != null) {
 			for (Entry<String, String> attribute : otherAttributes.entrySet()) {
 				tag.append(attribute.getKey());
 				tag.append("=\"");
 				tag.append(attribute.getValue());
 				tag.append("\" ");
 			}
 		}
 		
 		// Close the tag
 		tag.append("></");
 		tag.append(type.getTagName());
 		tag.append(">");
 		
 		return tag.toString();
 	}
 	
 }
