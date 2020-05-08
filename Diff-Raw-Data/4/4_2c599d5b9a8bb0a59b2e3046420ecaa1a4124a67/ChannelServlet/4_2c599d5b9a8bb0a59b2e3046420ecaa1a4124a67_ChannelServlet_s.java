 package org.kisst.gft.admin;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.kisst.gft.GftContainer;
 import org.kisst.gft.filetransfer.Channel;
 
 public class ChannelServlet extends BaseServlet {
 	public ChannelServlet(GftContainer gft) { super(gft);	}
 
 	public void handle(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		if (getUser(request, response)==null)
 			return;
 		response.setContentType("text/html;charset=utf-8");
 		//response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		String url=request.getRequestURI();
 		String name=url.substring("/channel/".length());
 		Channel ch=gft.channels.get(name);
 		out.println("<h1>Channel "+name+"</h1>");
 		out.println("<ul>");
		out.println("<li>FROM: <a href=\"/dir/"+ch.src.name+"/"+ch.srcdir+"\">"+ch.src.name+"/"+ch.srcdir+"</a>");
		out.println("<li>TO: <a href=\"/dir/"+ch.dest.name+"/"+ch.getDestPath("", null)+"\">"+ch.dest.name+"/"+ch.getDestPath("", null)+"</a>");
 		out.println("</ul>");
 		
 		out.println("<h2>Config</h2>");
 		out.println("<pre>");
 		out.println(ch.props);
 		out.println("</pre>");
 	}
 
 }
