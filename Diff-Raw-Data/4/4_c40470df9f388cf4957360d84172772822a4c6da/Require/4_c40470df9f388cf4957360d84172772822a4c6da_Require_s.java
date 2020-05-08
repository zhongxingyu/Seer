 package org.antstudio.moon.tag;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.antstudio.moon.tag.util.RequestUtils;
 import org.antstudio.utils.PropertiesUtils;
 import org.apache.log4j.Logger;
 /**
  * 
  * @author Gavin
  * @date 2013-8-6 上午12:27:21
  */
 public class Require extends TagSupport{
 
 	private static final long serialVersionUID = -2307579042925929168L;
 	@Override
 	public int doStartTag() throws JspException {
 		return EVAL_PAGE;
 	}
 	private Logger log = Logger.getLogger(getClass());
 	private String type;
 	private String src;
 	private Properties p;
 	{
 		try {
 			p = PropertiesUtils.loadPropertiesFile("~system~requireTag.properties");
 			p.putAll(PropertiesUtils.loadPropertiesFileIfExist("requireTag.properties"));
 		} catch (FileNotFoundException e) {
 			log.error("require初始化失败,未找到配置文件");
 			e.printStackTrace();
 		} catch (IOException e1) {
 			log.error("require初始化失败,读取配置文件出错");
 			e1.printStackTrace();
 		}
 	}
 	
 	
 	
 	@Override
 	public int doEndTag() throws JspException {
 		JspWriter out = this.pageContext.getOut();
 		StringBuilder sb =  new StringBuilder();
 		String contextPath = RequestUtils.getContextPath(pageContext);
 		if(type==null||"js".equals(type)){//for js
 			for(String s:src.split(",")){
 				if(p.containsKey("js."+s)){
 					sb.append("<script type=\"text/javascript\" src=\""+contextPath+p.getProperty("js."+s)+"\"></script>\n");
 				}else{
					sb.append("<script type=\"text/javascript\" src=\""+contextPath+s+"\"></script>\n");
 				}
 				if(p.containsKey("css."+s)){
 					sb.append(" <link rel=\"stylesheet\" href=\""+contextPath+p.getProperty("css."+s)+"\" type=\"text/css\" />\n");
 				}
 			}
 		}else{//for css
 			for(String s:src.split(",")){
 				if(p.containsKey("css."+s)){
 					sb.append(" <link rel=\"stylesheet\" href=\""+contextPath+p.getProperty("css."+s)+"\" type=\"text/css\" />\n");
 				}else{
 					sb.append(" <link rel=\"stylesheet\" href=\""+contextPath+s+"\" type=\"text/css\" />\n");
 				}
 			}
 		}
 		try {
 			out.print(sb);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return EVAL_PAGE;
 	}
 	
 	
 	public void setType(String type) {
 		this.type = type;
 	}
 	public void setSrc(String src) {
 		this.src = src;
 	}
 }
