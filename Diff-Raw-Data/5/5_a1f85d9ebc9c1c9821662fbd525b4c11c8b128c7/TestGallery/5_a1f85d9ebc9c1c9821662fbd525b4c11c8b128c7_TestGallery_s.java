 package plugins.TestGallery;
 
 
 import java.util.HashMap;
 import java.util.Random;
 
 import freenet.client.HighLevelSimpleClient;
 import freenet.keys.FreenetURI;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginHTTP;
 import freenet.pluginmanager.FredPluginThreadless;
 import freenet.pluginmanager.PluginHTTPException;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.api.HTTPRequest;
 
 public class TestGallery implements FredPlugin, FredPluginHTTP, FredPluginThreadless {
 	
 	private final static String DEFAULT_GALLERY_URI = "CHK@sTcjGeT~bWxycEvhidh7QYh9J9fBT6YjiXrfkzsC5fQ,~dt~6lS7idVfF09oqnzMI~nXo8V-HN4T6Y7FisfyWDU,AAEA--8";
 	boolean goon = true;
 	
 	Random rnd = new Random();
 	PluginRespirator pr;
 	private static final String plugName = "TestGallery";
 	public void terminate() {
 		goon = false;
 	}
 	
 	private String getArrayElement(String[] array, int element) {
 		try {
 			return array[element];
 		} catch (Exception e) {
 			//e.printStackTrace();
 			return "";
 		}
 	}
 	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
		throw new PluginHTTPException();
 	}
 	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		throw new PluginHTTPException();
 	}
 	
 	private HashMap getElements(String path) {
 		String[] getelements = getArrayElement(path.split("\\?"),1).split("\\&");
 		HashMap ret = new HashMap();
 		for (int i = 0; i < getelements.length ; i++) {
 			int eqpos = getelements[i].indexOf("="); 
 			if (eqpos < 1)
 				// Unhandled so far
 				continue;
 			
 			String key = getelements[i].substring(0, eqpos);
 			String value = getelements[i].substring(eqpos + 1);
 
 			ret.put(key, value);
 			/*if (getelements[i].startsWith("page="))
 				page = Integer.parseInt(getelements[i].substring("page=".length()));
 				*/
 		}
 		return ret;
 	}
 	
 	private String mkDefaultPage() {
 		StringBuffer out = new StringBuffer();
 		out.append("<HTML><HEAD><TITLE>" + plugName + "</TITLE></HEAD><BODY>\n");
 		out.append("<CENTER><H1>" + plugName + "</H1><BR/><BR/><BR/>\n");
 		out.append("Load gallery from the following key:<br/>");
 		out.append("<form method=\"GET\">");
 		out.append("<input type=text name=\"uri\" value=\""+DEFAULT_GALLERY_URI+"\"size=80/>");
 		out.append("<input type=submit value=\"Go!\"/></form>\n");
 		out.append("</CENTER></BODY></HTML>");
 		return out.toString();
 	}
 	
 	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
 		StringBuffer out = new StringBuffer();
 
 		int page = request.getIntParam("page", 1);
 		String uri = request.getParam("uri", request.getPath());
 		
 		if (uri.equals("")) {
 			return mkDefaultPage();
 		}
 		
 		try {
 			int i = 0;
 			/* Cache later! */
 			HighLevelSimpleClient hlsc = pr.getHLSimpleClient();
 			String imglist = new String(hlsc.fetch(new FreenetURI(uri)).asByteArray()).trim();
 			imglist = imglist.replaceAll("\r","\n");
 			imglist = imglist.replaceAll("\n\n", "\n");
 			imglist = imglist.replaceAll("\n\n", "\n");
 			imglist = imglist.replaceAll("\n\n", "\n");
 			imglist = imglist.replaceAll("\n\n", "\n");
 			imglist = imglist.replaceAll("\n\n", "\n");
 			/* /Cache! */
 			
 			String[] imgarr = imglist.split("\n");
 			String title = (imgarr[0].trim().replaceAll("^freenet:", "").indexOf("@") == 3)?"Untitled":imgarr[i++];
 			//imgarr[0] == title;
             out.append("<HTML><HEAD><TITLE>").append(title).append("</TITLE></HEAD><BODY>\n");
             out.append("<CENTER><H1>").append(title).append("</H1><BR/>Page ").append(page).append("<BR/><BR/>\n");
 			mkPageIndex(out, imgarr.length, page, uri+ '?');
 			out.append("<table><tr>\n");
 			int images = 0;
 			int flush = (page - 1)*6*4;
 			
 			for(i = 1 ; (i < imgarr.length && images < 6*4); i++) {
 				// url | name | size
 				if (imgarr[i].trim().length() < 5)
 					continue;
 				if (flush > 0) {
 					flush--;
 					continue;
 				}
 				images++;
 				
 				String imginfo[] = imgarr[i].split("\\|");
 				String iname = getArrayElement(imginfo, 1).trim();
 				String isname = iname;
 				if (iname.length() > 15)
 					isname = iname.substring(0,11) + "..." + iname.substring(iname.lastIndexOf("."));
 				
 				// f2="`echo "$f" | rev | cut -d . -f2- | rev | cut -c-13`...`echo "$f" | rev | cut -d . -f1 | rev`"
 				//String isize = getArrayElement(imginfo, 2).trim();
 				String iurl = getArrayElement(imginfo, 0).trim();
 				iurl = iurl.replaceAll("^URI: ", "");
 				iurl = iurl.replaceAll("^freenet:", "");
 				if (!iurl.startsWith("/"))
 					iurl = '/' + iurl;
 				
 				
 				
 				out.append("<td align=\"center\" valign=\"top\" width=\"102px\">\n");
                 out.append("  <a title=\"").append(iname).append("\" href=\"").append(iurl).append("\"><img src=\"").append(iurl).append("\" border=\"0\" width=\"100\"><br/>\n");
 				if (imginfo.length > 1) {
                     out.append("  <font size=\"-2\">\"").append(isname).append("\"</font>\n");
 				}
 				out.append("  </a>\n");
 					
 				for (int j = 2 ; j < imginfo.length ; j++)
                     out.append("  <br><font size=\"-2\">").append(imginfo[j].trim()).append("</font>\n");
 				out.append("</td>\n");
 				
 				// new row?
 				if (i%6 == 0) {
 					out.append("</tr><tr>\n");
 				}
 			}
 			out.append("</tr><table>\n");
 
 			mkPageIndex(out, imgarr.length, page, uri+ '?');
 			
 			
 			out.append("</CENTER></BODY></HTML>");
 			return out.toString();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			return e.toString();// e.printStackTrace();
 		}
 	}
 	
 	private void mkPageIndex(StringBuffer out, int imgarrlength, int page, String uri) {
 		for (int pg = 1 ; pg <= (int)Math.ceil((imgarrlength-1)/(6*4)) ; pg++) {
 			out.append("&nbsp;");
 			if (pg != page)
                 out.append("<a href=\"").append(uri).append("page=").append(pg).append("\">[").append(pg).append("]</a>");
 			else
                 out.append('[').append(pg).append(']');
 			out.append("&nbsp;\n");
 		}
 	}
 
 	public void runPlugin(PluginRespirator pr) {
 		this.pr = pr;
 		/*
 		while(goon){
 			try{
 				Thread.sleep(300000);
 			}catch (InterruptedException e) {
 			}
 		}
 		*/
 	}
 
 }
 
